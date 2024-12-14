import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * see: https://adventofcode.com/2024/day/14
 *
 */
public class Y24Day14T {

	static Random rand = new Random(1);
	
	static Y24GUIOutput14 output;
	
	record Pos(int x, int y) {
		public Pos add(int dx, int dy) { return new Pos(x+dx,y+dy); }
		public Pos sub(int dx, int dy) { return add(-dx, -dy); }
		public Pos add(Pos v) { return add(v.x, v.y); }
		public Pos sub(Pos v) { return sub(v.x, v.y); }
		public Pos mul(int k) { return new Pos(k*x, k*y); }
		public Pos neg() { return mul(-1); }
		public Pos up() { return add(0,-1); }
		public Pos right() { return add(1,0); }
		public Pos down() { return add(0,1); }
		public Pos left() { return add(-1,0); }
		public Pos[] neighbours() { return new Pos[] {up(), right(), down(), left()}; }
		public Pos mod(int divX, int divY) { return new Pos((divX+(x%divX))%divX,(divY+(y%divY))%divY); }
		public Pos mod(Pos div) { return mod(div.x, div.y); }
		public boolean isInArea(int minX, int minY, int maxX, int maxY) { 
			return (x>=minX) && (x<=maxX) && (y>=minY) && (y<=maxY); 
		}
		@Override public String toString() { return "("+x+","+y+")"; }
	}

	static class Robot {
		Pos p;
		Pos v;
		public Robot(Pos p, Pos v) {
			this.p = p;
			this.v = v;
		}
		public void tick() {
			p = p.add(v);
		}
		@Override
		public String toString() {
			return "p="+p.x+","+p.y+" v="+v.x+","+v.y;
		}
	}

	static class World {
		List<Robot> robots;
		int maxX;
		int maxY;
		int ticks;
		public World(int maxX, int maxY) {
			robots = new ArrayList<>();
			this.maxX = maxX;
			this.maxY = maxY;
			this.ticks = 0;
		}
		public void addRobot(int px, int py, int vx, int vy) {
			robots.add(new Robot(new Pos(px,py), new Pos(vx,vy)));
		}
		private Set<Pos> getRobotPositions() {
			return robots.stream().map(rob->rob.p.mod(maxX,maxY)).collect(Collectors.toSet());
		}
		private void addTRobots(int dx, int dy) {
			addRobField( dx+ 0,dy+ 0,  dx+16,dy+ 0  );
            
			addRobField( dx+ 0,dy+ 1,  dx+ 1,dy+ 1  );
			addRobField( dx+15,dy+ 1,  dx+16,dy+ 1  );
			addRobField( dx+ 0,dy+ 2,  dx+ 0,dy+ 3  );
			addRobField( dx+16,dy+ 2,  dx+16,dy+ 3  );
                                                
			addRobField( dx+ 7,dy+ 1,  dx+ 9,dy+11  );
			                                    
			addRobField( dx+ 0,dy+ 6,  dx+ 3,dy+ 8  );
			addRobField( dx+13,dy+ 6,  dx+16,dy+ 8  );
			                                    
			addRobField( dx+ 6,dy+12,  dx+10,dy+12  );
			addRobField( dx+ 4,dy+13,  dx+12,dy+13  );
//			
//			addRobField(  0, 0,  16, 0  );
//			
//			addRobField(  0, 1,   1, 1  );
//			addRobField( 15, 1,  16, 1  );
//			addRobField(  0, 2,   0, 3  );
//			addRobField( 16, 2,  16, 3  );
//
//			addRobField(  7, 1,   9,11  );
//			
//			addRobField(  0, 6,   3, 8  );
//			addRobField( 13, 6,  16, 8  );
//			
//			addRobField(  6,12,  10,12  );
//			addRobField(  4,13,  12,13  );
		}			

		public void tick() {
			ticks++;
			robots.forEach(rob->rob.tick());
		}
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("TICK "+ticks+"\n");
			Set<Pos> positions = getRobotPositions();
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					char c = '.';
					if (positions.contains(new Pos(x,y))) {
						c = 'X';
					}
					result.append(c);
				}
				result.append('\n');
			}
			return result.toString();
		}
		public int calcSafetyFactor() {
			int fTL = calcRobotsIn(0,0, maxX/2-1,maxY/2-1);
			int fTR = calcRobotsIn(maxX/2+1,0, maxX-1,maxY/2-1);
			int fBL = calcRobotsIn(0,maxY/2+1, maxX/2-1,maxY-1);
			int fBR = calcRobotsIn(maxX/2+1,maxY/2+1, maxX-1,maxY-1);
			return fTL*fTR*fBL*fBR;
		}
		private int calcRobotsIn(int xMin, int yMin, int xMax, int yMax) {
			return (int) robots.stream().filter(rob->rob.p.mod(maxX,maxY).isInArea(xMin, yMin, xMax, yMax)).count();
		}
		public void robMove(int dx, int dy) {
			robots.forEach(rob->rob.p = rob.p.add(dx,dy));
		}
		public void robNeg() {
			robots.forEach(rob->rob.v = rob.v.neg());
		}
		public void addRobField(int xMin, int yMin, int xMax, int yMax) {
			for (int x=xMin; x<=xMax; x++) {
				for (int y=yMin; y<=yMax; y++) {
//					Pos v = new Pos(rand.nextInt(3)+rand.nextInt(3)-2,rand.nextInt(3)+rand.nextInt(3)-2);
					int vx = (int) rand.nextInt(7)-3;
					int vy = (int) rand.nextInt(7)-3;
					if ((vx == 0) && (vy==0)) {
						vx = (int) Math.signum(rand.nextInt(2)-0.5);
						vy = (int) Math.signum(rand.nextInt(2)-0.5);
					}
					Pos v = new Pos(vx, vy);
					robots.add(new Robot(new Pos(x,y), v));
				}
			}
		}
		public String showInput() {
			final StringBuilder result = new StringBuilder();
			modRobs();
			robots.forEach(rob->result.append(rob.toString()).append('\n'));
			return result.toString();
		}
		public void modRobs() {
			robots.forEach(rob->rob.p = rob.p.mod(maxX, maxY));
		}
	}
	
	private static final String ROBOT_RX = "^p=([0-9]+),([0-9]+) v=([-0-9]+),([-0-9]+)$"; 
	
	public static void mainPart1(String inputfile, int maxX, int maxY) throws FileNotFoundException {
		World world = new World(maxX, maxY);
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				int px = Integer.parseInt(line.replaceFirst(ROBOT_RX, "$1"));
				int py = Integer.parseInt(line.replaceFirst(ROBOT_RX, "$2"));
				int vx = Integer.parseInt(line.replaceFirst(ROBOT_RX, "$3"));
				int vy = Integer.parseInt(line.replaceFirst(ROBOT_RX, "$4"));
				world.addRobot(px, py, vx, vy);
			}
		}
		System.out.println(world.toString());
		for (int i=0; i<100; i++) {
			world.tick();
			System.out.println(world.toString());
		}
		System.out.println("SafetyFactor: "+world.calcSafetyFactor());
	}


	public static void mainPart2(String inputfile, int maxX, int maxY) throws FileNotFoundException {
		output = new Y24GUIOutput14("2024 Day 14 Part 2", true);
		World world = new World(maxX, maxY);
//		try (Scanner scanner = new Scanner(new File(inputfile))) {
//			while (scanner.hasNext()) {
//				String line = scanner.nextLine().trim();
//				if (line.isBlank()) {
//					continue;
//				}
//				System.out.println(line);
//				int px = Integer.parseInt(line.replaceFirst(ROBOT_RX, "$1"));
//				int py = Integer.parseInt(line.replaceFirst(ROBOT_RX, "$2"));
//				int vx = Integer.parseInt(line.replaceFirst(ROBOT_RX, "$3"));
//				int vy = Integer.parseInt(line.replaceFirst(ROBOT_RX, "$4"));
//				world.addRobot(px, py, vx, vy);
//			}
//		}
		
//		world.addRobField(  0, 0,  12, 0  );
//		
//		world.addRobField(  0, 1,   1, 1  );
//		world.addRobField( 11, 1,  12, 1  );
//		world.addRobField(  0, 2,   0, 3  );
//		world.addRobField( 12, 2,  12, 3  );
//
//		world.addRobField(  5, 1,   7,11  );
//		
//		world.addRobField(  0, 6,   2, 8  );
//		world.addRobField( 10, 6,  12, 8  );
//		
//		world.addRobField(  4,12,   8,12  );
//		world.addRobField(  2,13,  10,13  );


		world.addTRobots(10,10);
		world.addTRobots(10,40);
		world.addTRobots(10,70);
		world.addTRobots(40,10);
		world.addTRobots(40,40);
		world.addTRobots(40,70);
		world.addTRobots(70,10);
		world.addTRobots(70,40);
		world.addTRobots(70,70);

//		output.addStep(world.toString());

		
		// world.robMove(37,36);
		world.robNeg();
		for (int i=0; i<30; i++) {
			world.tick();
		}
		world.robNeg();
		
		System.out.println(world.showInput());
		
		world.ticks=0;
		for (int i=0; i<500; i++) {
			output.addStep(world.toString());
			world.tick();
		}
	}




	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day14/feri/input-example.txt", 11, 7);
//		mainPart1("exchange/day14/feri/input-example-2.txt", 11, 7);
//		mainPart1("exchange/day14/feri/input.txt", 101, 103);     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day14/feri/input-example.txt", 11, 7);
		mainPart2("exchange/day14/feri/input-example-T3.txt", 101, 103);
//		mainPart2("exchange/day14/feri/input.txt", 101, 103); 
		                                                                
		System.out.println("---------------");
	}

	
}

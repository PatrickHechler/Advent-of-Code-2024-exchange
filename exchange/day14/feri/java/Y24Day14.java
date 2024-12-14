import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * see: https://adventofcode.com/2024/day/14
 *
 */
public class Y24Day14 {
	
	
	record Pos(int x, int y) {
		public Pos add(int dx, int dy) { return new Pos(x+dx,y+dy); }
		public Pos sub(int dx, int dy) { return add(-dx, -dy); }
		public Pos add(Pos v) { return add(v.x, v.y); }
		public Pos sub(Pos v) { return sub(v.x, v.y); }
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
		public void tick() {
			ticks++;
			robots.forEach(rob->rob.tick());
		}
		@Override
		public String toString() {
			Set<Pos> positions = getRobotPositions();
			StringBuilder result = new StringBuilder();
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
	}			



	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day14/feri/input-example.txt", 11, 7);
//		mainPart1("exchange/day14/feri/input-example-2.txt", 11, 7);
		mainPart1("exchange/day14/feri/input.txt", 101, 103);     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day14/feri/input-example.txt", 11, 7);
//		mainPart2("exchange/day14/feri/input.txt", 101, 103); 
		                                                                
		System.out.println("---------------");
	}

	
}

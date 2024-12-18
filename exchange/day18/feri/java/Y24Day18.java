import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2024/day/18
 *
 */
public class Y24Day18 {
	
	static Y24GUIOutput18 output;
	
	static final String DIR_CHARS = "^>v<";
	static final int[] DIR_X    = {  0,   1,   0,  -1};
	static final int[] DIR_Y    = { -1,   0,   1,   0};
	
	record Pos(int x, int y) {
		@Override public String toString() {
			return "("+x+","+y+")";
		}
		public Pos move(char dirChar) { return move(DIR_CHARS.indexOf(dirChar)); }
		public Pos move(int dir) { return new Pos(x+DIR_X[dir], y+DIR_Y[dir]); }
		public Pos up() { return new Pos(x,y-1); }
		public Pos right() { return new Pos(x+1,y); }
		public Pos down() { return new Pos(x,y+1); }
		public Pos left() { return new Pos(x-1,y); }
		public Pos[] neighbours() {
			return new Pos[] {up(), right(), down(), left()};
		}
		public boolean eq(int compX, int compY) {
			return (x == compX) && (y == compY);
		}
	}

	public static class World {
		private Set<Pos> corrupted;
		private Set<Pos> shortestPath;
		int sizeX;
		int sizeY;
		private Pos startPos;
		private Pos endPos;
		private Pos lastCorruptedPos;
		
		public World(int sizeX, int sizeY) {
			corrupted = new HashSet<>();
			shortestPath = new HashSet<>();
			startPos = new Pos(0,0);
			endPos = new Pos(sizeX-1, sizeY-1);
			lastCorruptedPos = new Pos(-1,-1);
			this.sizeX = sizeX;
			this.sizeY = sizeY;
		}
		private char get(int x, int y) {
			return get(new Pos(x,y));
		}
		private char get(Pos p) {
			if (corrupted.contains(p)) {
				return '#';
			}
			if ((p.x<0) || (p.x>=sizeX) || (p.y<0) || (p.y>=sizeY)) {
				return '#';
			}
			return '.';
		}
		public void addCorrupt(int x, int y) {
			lastCorruptedPos = new Pos(x,y); 
			corrupted.add(lastCorruptedPos);
		}
		@Override
		public String toString() {
			String lastColor = "b0";
			StringBuilder result = new StringBuilder();
			result.append("SHAPE ("+sizeX+","+sizeY+")\n");
			for (int y=0; y<sizeY; y++) {
				for (int x=0; x<sizeX; x++) {
					Pos p = new Pos(x,y);
					String color = "b0";
					if (shortestPath.contains(p)) {
						color = "b2";
					}
					if (p.equals(lastCorruptedPos)) {
						color = "b3";
					}
					char c = get(p);
					if (startPos.eq(x,y)) {
						c = 'S';
					}
					else if (endPos.eq(x,y)) {
						c = 'E';
					}
					if (color != lastColor) {
						lastColor = color;
						result.append(output.style(color));
					}
					result.append(c);
				}
				result.append('\n');
			}
			return result.toString();
		}
		record Entry(Pos pos, int dist) {}
		Map<Pos, Integer> distances = new HashMap<>();
		PriorityQueue<Entry> entries;
		public void addEntry(Pos pos, int distance) {
			if (get(pos) != '.') {
				return;
			}
			if (distances.containsKey(pos)) {
				if (distances.get(pos) <= distance) {
					return;
				}
			}
			distances.put(pos, distance);
			entries.add(new Entry(pos, distance));
		}
		public boolean hasChanged(Entry e) {
			return distances.get(e.pos) != e.dist;
		}
		public int calcShortestPath() {
			shortestPath.clear();
			distances = new HashMap<>();
			entries = new PriorityQueue<Entry>((e1,e2)->Integer.compare(e1.dist, e2.dist));
			addEntry(startPos, 0);
			while (true) {
				Entry e = entries.poll();
				if (e==null) {
					return -1;
				}
				if (hasChanged(e)) {
					continue;
				}
				if (e.pos.equals(endPos)) {
					fillShortestPath(endPos, e.dist);
					return e.dist;
				}
				addEntry(e.pos.up(), e.dist+1);
				addEntry(e.pos.right(), e.dist+1);
				addEntry(e.pos.down(), e.dist+1);
				addEntry(e.pos.left(), e.dist+1);
//				output.addStep(toString());
			}
		}
		private void fillShortestPath(Pos pos, int dist) {
			if (shortestPath.contains(pos)) {
				return;
			}
			if (!distances.containsKey(pos)) {
				return;
			}
			if (distances.get(pos) != dist) {
				return;
			}
			shortestPath.add(pos);
			fillShortestPath(pos.up(), dist-1);
			fillShortestPath(pos.right(), dist-1);
			fillShortestPath(pos.down(), dist-1);
			fillShortestPath(pos.left(), dist-1);
			
		}
	}

	
	static final String POS_RX = "^([0-9]+),([0-9]+)$";
	
	public static void mainPart1(String inputfile, int maxX, int maxY, int maxStep) throws FileNotFoundException {

		output = new Y24GUIOutput18("2024 Day 18 Part 1", true);
		World world = new World(maxX+1, maxY+1);
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			int step=0;
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				int x = Integer.parseInt(line.replaceFirst(POS_RX, "$1"));
				int y = Integer.parseInt(line.replaceFirst(POS_RX, "$2"));
				world.addCorrupt(x, y);
//				output.addStep(world.toString());
				step++;
				if (step == maxStep) {
					break;
				}
			}
		}
		System.out.println(world.calcShortestPath());
		output.addStep(world.toString());
	}



	public static void mainPart2(String inputfile, int maxX, int maxY) throws FileNotFoundException {
		

		output = new Y24GUIOutput18("2024 Day 18 Part 2", true);
		World world = new World(maxX+1, maxY+1);
		world.calcShortestPath();
		output.addStep(world.toString());
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			int step=0;
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				int x = Integer.parseInt(line.replaceFirst(POS_RX, "$1"));
				int y = Integer.parseInt(line.replaceFirst(POS_RX, "$2"));
				world.addCorrupt(x, y);
				step++;
				int dist = world.calcShortestPath();
				output.addStep(world.toString());
				System.out.println(step+": "+line+" "+dist);
				if (dist == -1) {
					break;
				}
			}
		}
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day18/feri/input-example.txt", 6,6, 12);
		mainPart1("exchange/day18/feri/input.txt", 70,70, 1024);
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day18/feri/input-example.txt", 6,6);
		mainPart2("exchange/day18/feri/input.txt", 70,70);    
		System.out.println("---------------");
	}

	
}

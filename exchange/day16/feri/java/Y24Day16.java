import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2024/day/16
 *
 */
public class Y24Day16 {
	
	static Y24GUIOutput16 output;
	
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

	static final int DIR_NS = 0;
	static final int DIR_EW = 1;
	
	record PosDir(Pos pos, int dir) {}
	record Entry(PosDir posDir, int dist) {
		boolean isEW() { return posDir.dir == DIR_EW; }
		boolean isNS() { return posDir.dir == DIR_NS; }
	}

	public static class World {
		private List<char[]> rows;
		int maxX;
		int maxY;
		private Pos startPos;
		private Pos endPos;
		
		public World() {
			rows = new ArrayList<>();
			startPos = null;
			endPos = null;
			maxX = 0;
			maxY = 0;
		}
		private char get(int x, int y) {
			if ((x<0) || (x>=maxX) || (y<0) || (y>=maxY)) {
				return '#';
			}
			return rows.get(y)[x];
		}
		public void addRow(String rowString) {
			int y = rows.size();
			char[] row = rowString.toCharArray();
			for (int x=0; x<row.length; x++) {
				if (row[x] == 'S') {
					startPos = new Pos(x,y);
					row[x] = '.';
				}
				if (row[x] == 'E') {
					endPos = new Pos(x,y);
					row[x] = '.';
				}
			}
			rows.add(row);
			maxX = row.length;
			maxY = rows.size();
		}
		@Override
		public String toString() {
			String lastColor = "b0";
			StringBuilder result = new StringBuilder();
			result.append("SHAPE ("+maxX+","+maxY+")\n");
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					String color = "b0";
					if (distances.containsKey(new PosDir(new Pos(x,y), DIR_EW)) || distances.containsKey(new PosDir(new Pos(x,y), DIR_NS))) {
						color = "b2";
					}
					char c = get(x,y);
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
		Map<PosDir, Integer> distances = new HashMap<>();
		PriorityQueue<Entry> entries;
		public void addEntry(PosDir posDir, int distance) {
			if (get(posDir.pos.x, posDir.pos.y) != '.') {
				return;
			}
			if (distances.containsKey(posDir)) {
				if (distances.get(posDir) <= distance) {
					return;
				}
			}
			distances.put(posDir, distance);
			entries.add(new Entry(posDir, distance));
		}
		public boolean hasChanged(Entry e) {
			return distances.get(e.posDir) != e.dist;
		}
		public int calcShortestPath() {
			distances = new HashMap<>();
			entries = new PriorityQueue<Entry>((e1,e2)->Integer.compare(e1.dist, e2.dist));
			addEntry(new PosDir(startPos, DIR_EW), 0);
			while (true) {
				Entry e = entries.poll();
				if (hasChanged(e)) {
					continue;
				}
				if (e.posDir.pos.equals(endPos)) {
					return e.dist;
				}
				if (e.isEW()) {
					addEntry(new PosDir(e.posDir.pos, DIR_NS), e.dist+1000);
					addEntry(new PosDir(e.posDir.pos.left(), DIR_EW), e.dist+1);
					addEntry(new PosDir(e.posDir.pos.right(), DIR_EW), e.dist+1);
				}
				else {
					addEntry(new PosDir(e.posDir.pos, DIR_EW), e.dist+1000);
					addEntry(new PosDir(e.posDir.pos.up(), DIR_NS), e.dist+1);
					addEntry(new PosDir(e.posDir.pos.down(), DIR_NS), e.dist+1);
				}
				output.addStep(toString());
			}
			
		}
	}

	public static void mainPart1(String inputfile) throws FileNotFoundException {

		output = new Y24GUIOutput16("2024 Day 16 Part 1", true);
		World world = new World();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					break;
				}
				world.addRow(line);
			}
		}
		output.addStep(world.toString());
		System.out.println(world.calcShortestPath());
	}



	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day16/feri/input-example.txt");
//		mainPart1("exchange/day16/feri/input-example-2.txt");
		mainPart1("exchange/day16/feri/input.txt");             // < 313196
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day16/feri/input-example-2.txt");
//		mainPart2("exchange/day16/feri/input.txt");
		System.out.println("---------------");
	}

	
}

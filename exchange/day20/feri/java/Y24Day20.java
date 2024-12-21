import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2024/day/20
 *
 */
public class Y24Day20 {
	
	static Y24GUIOutput20 output;
	
	static final String DIR_CHARS = "^>v<";
	static final int[] DIR_X    = {  0,   1,   0,  -1};
	static final int[] DIR_Y    = { -1,   0,   1,   0};
	
	record Pos(int x, int y) implements Comparable<Pos> {
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
		public Pos add(int dx, int dy) {
			return new Pos(x+dx,y+dy);
		}
		public List<Pos> calcPath(Pos targetPos) {
			List<Pos> result = new ArrayList<>();
			Pos current = this;
			while (true) {
				result.add(current);
				if (current.equals(targetPos)) {
					break;
				}
				current = current.moveOne(targetPos);
			}
			return result;
		}
		private Pos moveOne(Pos targetPos) {
			int dx = targetPos.x-x;
			int dy = targetPos.y-y;
			if (Math.abs(dx) > Math.abs(dy)) {
				return new Pos(x+(int)Math.signum(dx), y);
			}
			return new Pos(x,y+(int)Math.signum(dy));
		}
		@Override
		public int compareTo(Pos other) {
			int result = Integer.compare(y, other.y);
			if (result == 0) {
				result = Integer.compare(x, other.x);
			}
			return result;
		}
		public Pos min(Pos other) {
			if (compareTo(other)<=0) {
				return this;
			}
			return other;
		}
		public Pos max(Pos other) {
			if (compareTo(other)>=0) {
				return this;
			}
			return other;
		}
	}

	record Entry(Pos pos, int dist) {}

	public static class World {
		private List<char[]> rows;
		int maxX;
		int maxY;
		private Pos startPos;
		private Pos endPos;
		private Pos cStartPos;
		private Pos cEndPos;
		
		public World() {
			rows = new ArrayList<>();
			startPos = null;
			endPos = null;
			cStartPos = null;
			cEndPos = null;
			maxX = 0;
			maxY = 0;
		}
		private char get(int x, int y) {
			if ((x<0) || (x>=maxX) || (y<0) || (y>=maxY)) {
				return '#';
			}
			return rows.get(y)[x];
		}
		private boolean isWall(Pos p) {
			return get(p.x, p.y) == '#';
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
					if (distances.containsKey(new Pos(x,y)) || distances.containsKey(new Pos(x,y))) {
						color = "b2";
					}
					if (cStartPos!=null && cStartPos.eq(x, y)) {
						color = "b3";
					}
					if (cEndPos!=null && cEndPos.eq(x, y)) {
						color = "b3";
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
		Map<Pos, Integer> distances = new HashMap<>();
		PriorityQueue<Entry> entries;
		public void addEntry(Pos pos, int distance) {
			if (get(pos.x, pos.y) != '.') {
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
			distances = new HashMap<>();
			entries = new PriorityQueue<Entry>((e1,e2)->Integer.compare(e1.dist, e2.dist));
			addEntry(startPos, 0);
			while (true) {
				Entry e = entries.poll();
				if (hasChanged(e)) {
					continue;
				}
				if (e.pos.equals(endPos)) {
					return e.dist;
				}
				addEntry(e.pos.left(), e.dist+1);
				addEntry(e.pos.right(), e.dist+1);
				addEntry(e.pos.up(), e.dist+1);
				addEntry(e.pos.down(), e.dist+1);
//				output.addStep(toString());
			}
		}
		public int countCheats(int minSavings) {
			int result = 0;
			for (Pos pathPos:distances.keySet()) {
				int cheatStartDist = distances.get(pathPos);
				for (Pos neighbourPos:pathPos.neighbours()) {
					if (isWall(neighbourPos)) {
						for (Pos cheatEndPos:neighbourPos.neighbours()) {
							if (isWall(cheatEndPos)) {
								continue;
							}
							int cheatEndDist = distances.get(cheatEndPos);
							if (cheatEndDist-cheatStartDist-2>=minSavings) {
								result++;
								break;
							}
						}
					}
				}
			}
			return result;
		}
		
		record Cheat(Pos startPos, Pos endPos, int dist) implements Comparable<Cheat> {

			@Override
			public int compareTo(Cheat other) {
				Pos minPos = startPos.min(endPos);
				Pos maxPos = startPos.max(endPos);
				Pos otherMinPos = other.startPos.min(other.endPos);
				Pos otherMaxPos = other.startPos.max(other.endPos);
				int result = minPos.compareTo(otherMinPos);
				if (result == 0) {
					result = maxPos.compareTo(otherMaxPos);
					if (result == 0) {
						result = Integer.compare(dist, other.dist);
					}
				}
				return result;
			}
			
		}
		
		public int countLongCheats(int minSavings, int maxPicos) {
			Set<Cheat> cheats = new HashSet<>();
			for (Pos pathPos:distances.keySet()) {
				int cheatStartDist = distances.get(pathPos);
				for (int dx = -maxPicos; dx<=maxPicos; dx++) {
					for (int dy = -(maxPicos-Math.abs(dx)); dy<=maxPicos-Math.abs(dx); dy++) {
						Pos cheatEndPos = pathPos.add(dx, dy); 
						if (isWall(cheatEndPos)) {
							continue;
						}
						int steps = Math.abs(dx)+Math.abs(dy);
						int cheatEndDist = distances.get(cheatEndPos);
						int skipDist = cheatEndDist-cheatStartDist-steps;
						if (skipDist>=minSavings) {
							cheats.add(new Cheat(pathPos, cheatEndPos, skipDist));
						}
					}
				}
			}
			List<Cheat> sortedCheats = new ArrayList<>(cheats);
			Collections.sort(sortedCheats);
			Map<Pos, Cheat> bestCheats = new LinkedHashMap<>();
			for (Cheat cheat:sortedCheats) {
				Cheat bestCheat = bestCheats.get(cheat.startPos);
				if ((bestCheat == null) || bestCheat.dist < cheat.dist) {
					bestCheats.put(cheat.startPos, cheat);
				}
			}
			Map<Pos, Cheat> bestEndPosCheats = new LinkedHashMap<>();
			for (Cheat bestCheat:bestCheats.values()) {
				Cheat bestEndPosCheat = bestEndPosCheats.get(bestCheat.endPos);
				if ((bestEndPosCheat == null) || bestEndPosCheat.dist < bestCheat.dist) {
					bestEndPosCheats.put(bestCheat.endPos, bestCheat);
				}
			}
			List<Cheat> selectedCheats = new ArrayList<>(bestEndPosCheats.values());
			Collections.sort(selectedCheats);
			for (Cheat selectedCheat:selectedCheats) {
				showCheat(selectedCheat);
			}
			return cheats.size();
		}

		private void showCheat(Cheat cheat) {
			Pos cheatStartPos = cheat.startPos;
			Pos cheatEndPos = cheat.endPos;
			int cheatStartDist = distances.get(cheatStartPos);
			int cheatEndDist = distances.get(cheatEndPos);
			Set<Pos> cheatPath = new LinkedHashSet(cheatStartPos.calcPath(cheatEndPos));
			String lastColor = "b0";
			StringBuilder result = new StringBuilder();
			result.append("Cheat: "+cheat.dist+"\n");
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					Pos p = new Pos(x,y);
					String color = "b0";
					if (distances.containsKey(p)) {
						int dist = distances.get(p);
						if ((dist < cheatStartDist) || (dist > cheatEndDist)) {
							color = "b2";
						}
					}
					if (cheatPath.contains(p)) {
						color = "b3";
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
			output.addStep(result.toString());
		}
	}

	public static void mainPart1(String inputfile, int minSavings) throws FileNotFoundException {

//		output = new Y24GUIOutput20("2024 Day 20 Part 1", true);
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
		world.calcShortestPath();
		int countCheats = world.countCheats(minSavings);
		System.out.println("countCheats="+countCheats);
	}



	public static void mainPart2(String inputfile, int cheatLen, int minSavings) throws FileNotFoundException {

		output = new Y24GUIOutput20("2024 Day 20 Part 2", true);
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
		world.calcShortestPath();
		output.addStep(world.toString());
		int countLongCheats = world.countLongCheats(minSavings, cheatLen);
		System.out.println("countLongCheats="+countLongCheats);
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day20/feri/input-example.txt", 12);
		mainPart1("exchange/day20/feri/input.txt", 100);
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day20/feri/input-example.txt", 2, 12);
//		mainPart2("exchange/day20/feri/input-example.txt", 4, 10);
//		mainPart2("exchange/day20/feri/input.txt", 20,3000);    
//		mainPart2("exchange/day20/feri/input.txt", 2, 100);    
		mainPart2("exchange/day20/feri/input.txt", 20, 100);    
		System.out.println("---------------");
	}

	
}

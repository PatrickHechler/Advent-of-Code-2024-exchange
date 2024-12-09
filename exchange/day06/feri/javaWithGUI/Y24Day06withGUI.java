import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2024/day/6
 *
 */
public class Y24Day06withGUI {

	
	static Y24GUIOutput06 output;
	
	public static final int DIR_NORTH = 0;
	public static final int DIR_EAST = 1;
	public static final int DIR_SOUTH = 2;
	public static final int DIR_WEST = 3;

	public static final char[] DIR_CHARS = {'^', '>', 'v', '<'}; 
	public static final Pos[] DIR_VECTORS = {new Pos(0,-1), new Pos(1,0),new Pos(0,1), new Pos(-1,0)}; 

	public static int turnRight(int dir) { return (dir+1)%4; }
	public static int uTurn(int dir) { return (dir+2)%4; }
	public static int turnLeft(int dir) { return (dir+3)%4; }
	
	record Pos(int x, int y) {
		@Override public String toString() {
			return "("+x+","+y+")";
		}
		public Pos add(Pos other) {
			return add(other.x, other.y);
		}
		public Pos add(int dx, int dy) {
			return new Pos(x+dx, y+dy);
		}
		public Pos move(int dir) {
			return add(DIR_VECTORS[dir]);
		}
	}
	
	public static class Lab {
		private List<String> rows;
		int maxX;
		int maxY;
		
		public Pos guardPos;
		public int guardDir;
		
		public int ticks;
		
		public Map<Pos, Character> visitedPositions;
		
		public Lab() {
			rows = new ArrayList<>();
			maxX = 0;
			maxY = 0;
			guardPos = null;
			guardDir = -1;
			visitedPositions = null;
			ticks = 0;
		}
		public void addRow(String row) {
			rows.add(row);
			maxX = row.length();
			maxY = rows.size();
		}
		public char get(Pos pos) {
			return get(pos.x, pos.y);
		}
		public char get(int x, int y) {
			if ((x<0) || (x>=maxX) || (y<0) || (y>=maxY)) {
				return '+';
			}
			return rows.get(y).charAt(x);
		}
		public boolean isBlocked(Pos pos) {
			return get(pos) == '#';
		}
		public boolean isOutside(Pos pos) {
			return get(pos) == '+';
		}
		public void findGuardPos() {
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					if (get(x,y) == '^') {
						guardPos = new Pos(x,y);
						guardDir = DIR_NORTH;
						visitedPositions = new HashMap<>();
						visitedPositions.put(guardPos, '^');
						return;
					}
				}
			}
			throw new RuntimeException("start pos not found");
		}
		public boolean tick() {
			ticks++;
			Pos nextPos = guardPos.move(guardDir);
			while (isBlocked(nextPos)) {
				guardDir = turnRight(guardDir);
				nextPos = guardPos.move(guardDir);
			}
			guardPos = nextPos;
			if (isOutside(guardPos)) {
				return false;
			}
			visitedPositions.put(guardPos, DIR_CHARS[guardDir]);
			return true;
		}
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("SHAPE ("+maxX+","+maxY+"), TICKS "+ticks+", POS "+guardPos+", DIR "+DIR_CHARS[guardDir]+"\n");
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					Pos p = new Pos(x,y);
					char c = get(p);
					if (visitedPositions.containsKey(p)) {
						c = visitedPositions.get(p);
					}
					result.append(c);
				}
				result.append('\n');
			}
			return result.toString();
		}
		public void show() {
			StringBuilder text = new StringBuilder();
			text.append("SHAPE ("+maxX+","+maxY+"), TICKS "+ticks+", POS "+guardPos+", DIR "+DIR_CHARS[guardDir]+"\n");
			String currentColor = "b0";
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					Pos p = new Pos(x,y);
					char c = get(p);
					String color = "b0";
					if (visitedPositions.containsKey(p)) {
						c = visitedPositions.get(p);
						color = "b2";
						if (p.equals(guardPos)) {
							color = "b3";
						}
					}
					if (color != currentColor) {
						currentColor = color;
						text.append(output.style(color));
					}
					text.append(c);
				}
				text.append('\n');
			}
			output.addStep(text.toString());
		}
	}
	
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {
		
		output = new Y24GUIOutput06("2024 Day 06 Part 1", true);

		Lab lab = new Lab();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				lab.addRow(line);
			}
		}
		lab.findGuardPos();
		lab.show();
		while (lab.tick()) {
			lab.show();
		}
		lab.show();
		System.out.println(lab); 
		System.out.println(lab.visitedPositions.size()); 
	}

	

	public static void mainPart2(String inputfile) throws FileNotFoundException {
		
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day06/feri/input-example.txt");
		mainPart1("exchange/day06/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day06/feri/input-example.txt");
//		mainPart2("exchange/day06/feri/input.txt");
		System.out.println("---------------");
	}

	
}

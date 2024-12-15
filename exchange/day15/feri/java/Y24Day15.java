import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2024/day/15
 *
 */
public class Y24Day15 {
	
	static Y24GUIOutput15 output;
	
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
	}
	

	public static class World {
		private List<char[]> rows;
		int maxX;
		int maxY;
		private Pos robotPos;
		private Set<Pos> boxPositions;
		private String program;
		private int ticks;
		
		public World() {
			rows = new ArrayList<>();
			robotPos = null;
			boxPositions = new HashSet<>();
			maxX = 0;
			maxY = 0;
			program = "";
			ticks = 0;
		}
		public void addRow(String rowString) {
			int y = rows.size();
			char[] row = rowString.toCharArray();
			for (int x=0; x<row.length; x++) {
				if (row[x] == 'O') {
					boxPositions.add(new Pos(x,y));
					row[x] = '.';
				}
				if (row[x] == '@') {
					robotPos = new Pos(x,y);
					row[x] = '.';
				}
			}
			rows.add(row);
			maxX = row.length;
			maxY = rows.size();
		}
		public void addProgram(String prog) {
			program = program + prog;
		}
		public char get(Pos pos) {
			return get(pos.x, pos.y);
		}
		public char get(int x, int y) {
			if ((x<0) || (x>=maxX) || (y<0) || (y>=maxY)) {
				return '#';
			}
			return rows.get(y)[x];
		}
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("TICK "+ticks+", SHAPE ("+maxX+","+maxY+")\n");
			String prog = "   "+program+"          ";
			result.append(prog.substring(ticks, ticks+3))
				.append("[").append(prog.substring(ticks+3, ticks+4)).append("]")
				.append(prog.substring(ticks+4, ticks+13)).append("\n");
			
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					Pos p = new Pos(x,y);
					char c = get(p);
					if (p.equals(robotPos)) {
						c = '@';
					}
					else if (boxPositions.contains(p)) {
						c = 'O';
					}
					result.append(c);
				}
				result.append('\n');
			}
			return result.toString();
		}
		public boolean hasProgram() {
			return ticks < program.length();
		}
		public void tick() {
			char dirChar = program.charAt(ticks);
			ticks++;
			Pos nextRobotPos = robotPos.move(dirChar);
			if (isWall(nextRobotPos)) {
				return;
			}
			if (isBox(nextRobotPos)) {
				Pos boxToMove = nextRobotPos;
				Pos nextBoxToMove = boxToMove;
				while (isBox(nextBoxToMove)) {
					boxToMove = nextBoxToMove;
					nextBoxToMove = nextBoxToMove.move(dirChar);
				}
				if (isWall(nextBoxToMove)) {
					return;
				}
				boxPositions.remove(nextRobotPos);
				boxPositions.add(nextBoxToMove);
			}
			robotPos = nextRobotPos;
		}
		private boolean isBox(Pos p) {
			return boxPositions.contains(p);
		}
		private boolean isWall(Pos p) {
			return get(p) == '#';
		}
		public int calcSumGPS() {
			int result = 0;
			for (Pos boxPos:boxPositions) {
				result = result + 100*boxPos.y+boxPos.x;
			}
			return result;
		}
	}

	public static void mainPart1(String inputfile) throws FileNotFoundException {

		output = new Y24GUIOutput15("2024 Day 15 Part 1", true);
		World world = new World();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					break;
				}
				world.addRow(line);
			}
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				world.addProgram(line);
			}
		}
		output.addStep(world.toString());
		while (world.hasProgram()) {
			world.tick();
			output.addStep(world.toString());
		}
		System.out.println("Sum GPS: "+world.calcSumGPS());
	}


	public static class World2 {
		private List<char[]> rows;
		int maxX;
		int maxY;
		private Pos robotPos;
		private Set<Pos> boxPositions;
		private String program;
		private int ticks;
		
		public World2() {
			rows = new ArrayList<>();
			robotPos = null;
			boxPositions = new HashSet<>();
			maxX = 0;
			maxY = 0;
			program = "";
			ticks = 0;
		}
		public void addRow(String rowString) {
			char[] newRow = new char[rowString.length()*2];
			int y = rows.size();
			for (int x=0; x<rowString.length(); x++) {
				char c = rowString.charAt(x);
				switch (c) {
				case '#':
				case '.':
					newRow[2*x] = c;
					newRow[2*x+1] = c;
					break;
				case '@':
					newRow[2*x] = '.';
					newRow[2*x+1] = '.';
					robotPos = new Pos(2*x,y);
					break;
				case 'O':
					newRow[2*x] = '.';
					newRow[2*x+1] = '.';
					boxPositions.add(new Pos(2*x,y));
					break;
				default:
					throw new RuntimeException("unexpected char '"+c+"'");
				}
			}
			rows.add(newRow);
			maxX = newRow.length;
			maxY = rows.size();
		}
		public void addProgram(String prog) {
			program = program + prog;
		}
		public char get(Pos pos) {
			return get(pos.x, pos.y);
		}
		public char get(int x, int y) {
			if ((x<0) || (x>=maxX) || (y<0) || (y>=maxY)) {
				return '#';
			}
			return rows.get(y)[x];
		}
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("TICK "+ticks+", SHAPE ("+maxX+","+maxY+")\n");
			String prog = "   "+program+"          ";
			result.append(prog.substring(ticks, ticks+3))
				.append("[").append(prog.substring(ticks+3, ticks+4)).append("]")
				.append(prog.substring(ticks+4, ticks+13)).append("\n");
			
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					Pos p = new Pos(x,y);
					char c = get(p);
					if (p.equals(robotPos)) {
						c = '@';
					}
					else if (boxPositions.contains(p)) {
						c = '[';
					}
					else if (boxPositions.contains(p.left())) {
						c = ']';
					}
					result.append(c);
				}
				result.append('\n');
			}
			return result.toString();
		}
		public boolean hasProgram() {
			return ticks < program.length();
		}
		public void tick() {
			char dirChar = program.charAt(ticks);
			ticks++;
			Pos nextRobotPos = robotPos.move(dirChar);
			if (isWall(nextRobotPos)) {
				return;
			}
			if (isBox(nextRobotPos) || isBox(nextRobotPos.left())) {
				Pos box2Move = nextRobotPos;
				if (!isBox(nextRobotPos)) {
					box2Move = nextRobotPos.left();
				}
				Set<Pos> boxes2Move = new HashSet<>();
				if (!recursiveMoveBoxes(boxes2Move, box2Move, dirChar)) {
					return;
				}
				for (Pos box:boxes2Move) {
					boxPositions.remove(box);
					boxPositions.add(box.move(dirChar));
				}
			}
			robotPos = nextRobotPos;
		}
		private boolean recursiveMoveBoxes(Set<Pos> result, Pos box2Move, char dirChar) {
			result.add(box2Move);
			boolean ok = true;
			if ("^v".indexOf(dirChar) != -1) {
				Pos b1 = box2Move.move(dirChar);
				Pos b2 = box2Move.move(dirChar);
			}
			else {
				
			}
			return ok;
		}
		private boolean isBox(Pos p) {
			return boxPositions.contains(p);
		}
		private boolean isWall(Pos p) {
			return get(p) == '#';
		}
		public int calcSumGPS() {
			int result = 0;
			for (Pos boxPos:boxPositions) {
				result = result + 100*boxPos.y+boxPos.x;
			}
			return result;
		}
	}


	public static void mainPart2(String inputfile) throws FileNotFoundException {

		output = new Y24GUIOutput15("2024 Day 15 Part 2", true);
		World2 world = new World2();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					break;
				}
				world.addRow(line);
			}
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				world.addProgram(line);
			}
		}
		output.addStep(world.toString());
		while (world.hasProgram()) {
			world.tick();
			output.addStep(world.toString());
		}
		System.out.println("Sum GPS: "+world.calcSumGPS());
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day15/feri/input-example.txt");
//		mainPart1("exchange/day15/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day15/feri/input-example-2.txt");
//		mainPart2("exchange/day15/feri/input.txt");
		System.out.println("---------------");
	}

	
}

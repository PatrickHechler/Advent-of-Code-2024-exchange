import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2024/day/10
 *
 */
public class Y24Day10 {
	
	
	record Pos(int x, int y) {
		@Override public String toString() {
			return "("+x+","+y+")";
		}
		public Pos[] neighbours() {
			return new Pos[] {new Pos(x,y-1),new Pos(x+1,y),new Pos(x,y+1),new Pos(x-1,y)};
		}
	}
	
	
	public static class World {
		private List<String> rows;
		int maxX;
		int maxY;
		
		public World() {
			rows = new ArrayList<>();
			maxX = 0;
			maxY = 0;
		}
		public void addRow(String row) {
			rows.add(row);
			maxX = row.length();
			maxY = rows.size();
		}
		public int get(Pos pos) {
			return get(pos.x, pos.y);
		}
		public int get(int x, int y) {
			if ((x<0) || (x>=maxX) || (y<0) || (y>=maxY)) {
				return -1;
			}
			return rows.get(y).charAt(x)-'0';
		}
		public List<Pos> findTrailStarts() {
			List<Pos> result = new ArrayList<>();
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					if ( get(x,y) == 0) {
						result.add(new Pos(x,y));
					}
				}
			}
			return result;
		}
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("SHAPE ("+maxX+","+maxY+")\n");
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					Pos p = new Pos(x,y);
					int n = get(p);
					result.append('0'+n);
				}
				result.append('\n');
			}
			return result.toString();
		}
		public int countTrails(Pos trailStart) {

			Set<Pos> trailEnds = new HashSet<>();
			trailEnds.add(trailStart);
			for (int height=1; height<=9; height++) {
				Set<Pos> nextTrailEnds = new HashSet<>();
				for (Pos trailEnd:trailEnds) {
					for (Pos neighbour:trailEnd.neighbours()) {
						if (get(neighbour) == height) {
							nextTrailEnds.add(neighbour);
						}
					}
				}
				trailEnds = nextTrailEnds;
			}
			return trailEnds.size();
		}
	}

	public static void mainPart1(String inputfile) throws FileNotFoundException {
			
		World world = new World();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				world.addRow(line);
			}
		}
		List<Pos> trailStarts = world.findTrailStarts();
		int solution = 0;
		for (Pos trailStart:trailStarts) {
			int cnt = world.countTrails(trailStart);
			System.out.println("Number of trails from "+trailStart+": "+cnt+" trails");
			solution = solution + cnt; 
		}
		System.out.println("Solutions: "+solution);
	}


	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day10/feri/input-example.txt");
		mainPart1("exchange/day10/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day10/feri/input-example.txt");
//		mainPart2("exchange/day10/feri/input.txt");
		System.out.println("---------------");
	}

	
}

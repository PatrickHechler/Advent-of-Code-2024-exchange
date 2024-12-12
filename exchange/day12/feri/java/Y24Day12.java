import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2024/day/12
 *
 */
public class Y24Day12 {
	
	
	record Pos(int x, int y) {
		@Override public String toString() {
			return "("+x+","+y+")";
		}
		public Pos up() { return new Pos(x,y-1); }
		public Pos right() { return new Pos(x+1,y); }
		public Pos down() { return new Pos(x,y+1); }
		public Pos left() { return new Pos(x-1,y); }
		public Pos[] neighbours() {
			return new Pos[] {up(), right(), down(), left()};
		}
	}
	

	static class Area {
		char c;
		int size;
		int perimeter;
		Area parentArea;
		public Area(char c) {
			this.c = c;
			this.size = 0;
			this.perimeter = 0;
			this.parentArea = null;
		}
		public boolean isRoot() {
			return parentArea == null;
		}
		public Area getRoot() {
			if (parentArea == null) {
				return this;
			}
			return parentArea.getRoot();
		}
		public void setParentArea(Area parentArea) {
			this.parentArea = parentArea;
		}
		public boolean is(char otherC) {
			return c == otherC;
		}
		public void addField(int fences) {
			size++;
			perimeter += fences;
		}
		public int getSize() {
			return size;
		}
		public void join(Area masterArea) {
			masterArea.perimeter += this.perimeter;
			masterArea.size += this.size;
			this.parentArea = masterArea;
		}
		@Override
			public String toString() {
				return size+"x"+perimeter;
			}
	}
	
	public static class World {
		private List<String> rows;
		private Map<Pos, Area> pos2areasMap;
		private List<Area> areas;
		int maxX;
		int maxY;
		
		public World() {
			rows = new ArrayList<>();
			pos2areasMap = new HashMap<>();
			areas = new ArrayList<>();
			maxX = 0;
			maxY = 0;
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
				return '.';
			}
			return rows.get(y).charAt(x);
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
		public int calcFencingCosts() {
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					Pos p = new Pos(x,y);
					char c = get(p);
					Area areaLeft = pos2areasMap.get(p.left());
					Area areaUp = pos2areasMap.get(p.up());
					Area joinArea = null;
					if ((areaUp!=null) && areaUp.is(c)) {
						joinArea = areaUp.getRoot();
					}
					if ((areaLeft!=null) && areaLeft.is(c)) {
						if (joinArea != null) {
							Area childArea = areaLeft.getRoot();
							if (joinArea != childArea) {
								childArea.join(joinArea);
							}
						}
						else {
							joinArea = areaLeft.getRoot();
						}
					}
					if (joinArea == null) {
						joinArea = new Area(c);
						areas.add(joinArea);
					}
					int fences = countFences(p);
					pos2areasMap.put(p, joinArea);
					joinArea.addField(fences);
				}
			}
			System.out.println(areas.stream().filter(a -> a.isRoot()).toList());
			int result = areas.stream().filter(a -> a.isRoot()).mapToInt(a->a.getSize()*a.perimeter).sum();
			return result;
		}
		private int countFences(Pos p) {
			int result = 0;
			char c = get(p);
			for (Pos neighbour:p.neighbours()) {
				if (c != get(neighbour)) {
					result++;
				}
			}
			return result;
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
		
		System.out.println("fencing cost: "+world.calcFencingCosts());
	}


	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day12/feri/input-example.txt");
//		mainPart1("exchange/day12/feri/input-example-2.txt");
		mainPart1("exchange/day12/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day12/feri/input-example.txt");
//		mainPart2("exchange/day12/feri/input.txt");
		System.out.println("---------------");
	}

	
}

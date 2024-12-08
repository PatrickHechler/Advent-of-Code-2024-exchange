import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2024/day/8
 *
 */
public class Y24Day08 {
	
	
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
		public Pos sub(Pos other) {
			return new Pos(x-other.x, y-other.y);
		}
	}
	
	
	public static class World {
		private List<String> rows;
		int maxX;
		int maxY;
		Set<Pos> antinodes;
		
		public World() {
			rows = new ArrayList<>();
			antinodes = new HashSet<>();
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
				return '+';
			}
			return rows.get(y).charAt(x);
		}
		public Map<Character, List<Pos>> findOccurrences() {
			Map<Character, List<Pos>> result = new HashMap<>();
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					char c = get(x,y); 
					if ( c != '.') {
						result.computeIfAbsent(c, ch->new ArrayList<>()).add(new Pos(x,y));
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
					char c = get(p);
					result.append(c);
				}
				result.append('\n');
			}
			return result.toString();
		}
		public void addAntinodes(List<Pos> antennasPos) {
			for (int i=0; i<antennasPos.size(); i++) {
				for (int j=i+1; j<antennasPos.size(); j++) {
					addAntinodes(antennasPos.get(i), antennasPos.get(j));
				}
			}
		}
		private void addAntinodes(Pos antenna1, Pos antenna2) {
			Pos v = antenna2.sub(antenna1);
			addAntinode(antenna2.add(v));
			addAntinode(antenna1.sub(v));
		}
		public void addAntinodesLine(List<Pos> antennasPos) {
			for (int i=0; i<antennasPos.size(); i++) {
				for (int j=i+1; j<antennasPos.size(); j++) {
					addAntinodesLine(antennasPos.get(i), antennasPos.get(j));
				}
			}
		}
		private void addAntinodesLine(Pos antenna1, Pos antenna2) {
			Pos v = antenna2.sub(antenna1);
			Pos p = antenna2;
			while (addAntinode(p)) {
				p = p.add(v);
			}
			p = antenna1;
			while (addAntinode(p)) {
				p = p.sub(v);
			}
		}
		private boolean addAntinode(Pos p) {
			if ((p.x>=0) && (p.y>=0) && (p.x<maxX) && (p.y<maxY)) {
				antinodes.add(p);
				return true;
			}
			return false;
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
		System.out.println(world);
		Map<Character, List<Pos>> occurrences = world.findOccurrences();
		for (char c:occurrences.keySet()) {
			world.addAntinodes(occurrences.get(c));
		}
		System.out.println("Antinodes: "+world.antinodes.size());
	}


	public static void mainPart2(String inputfile) throws FileNotFoundException {
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
		System.out.println(world);
		Map<Character, List<Pos>> occurrences = world.findOccurrences();
		for (char c:occurrences.keySet()) {
			world.addAntinodesLine(occurrences.get(c));
		}
		System.out.println("Antinodes: "+world.antinodes.size());
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day08/feri/input-example.txt");
		mainPart1("exchange/day08/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day08/feri/input-example.txt");
		mainPart2("exchange/day08/feri/input.txt");
		System.out.println("---------------");
	}

	
}

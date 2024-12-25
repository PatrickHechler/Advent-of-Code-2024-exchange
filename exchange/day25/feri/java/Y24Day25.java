import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2024/day/25
 *
 */
public class Y24Day25 {
	
	static class Schema {
		int[] counts;
		boolean isLock;
		public Schema() {
		}
		public void addRow(String row) {
			if (counts == null) {
				counts = new int[]{-1, -1, -1, -1, -1};
				isLock = row.equals("#####"); 
			}
			for (int i=0; i<5; i++) {
				if (row.charAt(i) == '#') {
					counts[i]++;
				}
			}
		}
		@Override
		public String toString() {
			return "("+counts[0]+","+counts[1]+","+counts[2]+","+counts[3]+","+counts[4]+")";
		}
		public boolean matches(Schema other) {
			for (int i=0; i<5; i++) {
				if (counts[i] + other.counts[i] > 5) {
					return false;
				}
			}
			return true;
		}
		
	}
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {

		List<Schema> keys = new ArrayList<>();
		List<Schema> locks = new ArrayList<>();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				Schema schema = new Schema();
				schema.addRow(line);
				for (int i=0; i<6; i++) {
					line = scanner.nextLine().trim();
					schema.addRow(line);
				}
				if (schema.isLock) {
					locks.add(schema);
				}
				else {
					keys.add(schema);
				}
			}
			System.out.println("LOCKS: "+locks);
			System.out.println("KEYS: "+keys);
		}
		int countPossibleMatches = 0;
		for (Schema lock:locks) {
			for (Schema key:keys) {
				if (key.matches(lock)) {
					countPossibleMatches++;
				}
			}
		}
		System.out.println("Possible matches: "+countPossibleMatches);
	}



	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day25/feri/input-example.txt");
		mainPart1("exchange/day25/feri/input.txt");
		System.out.println("---------------");
		System.out.println();
	}

	
}

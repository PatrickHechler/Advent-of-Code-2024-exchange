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
public class Y24Day19 {

	
	public static class World {
		String[] towels;
		
		Map<String, Long> cache;
		
		public World(String[] towels) {
			this.towels = towels;
			this.cache = new HashMap<>();
		}

		public boolean checkPattern(String pattern) {
			if (pattern.isEmpty()) {
				return true;
			}
			for (String towel:towels) {
				if (pattern.startsWith(towel)) {
					if (checkPattern(pattern.substring(towel.length()))) {
						return true;
					}
				}
			}
			return false;
		}

		public long countPatterns(String pattern) {
			if (pattern.isEmpty()) {
				return 1;
			}
			if (cache.containsKey(pattern)) {
				return cache.get(pattern);
			}
			long result = 0;
			for (String towel:towels) {
				if (pattern.startsWith(towel)) {
					long cntPatterns = countPatterns(pattern.substring(towel.length()));
					result += cntPatterns;
				}
			}
			cache.put(pattern, result);
			return result;
		}
	}
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {

		int possiblePatterns = 0;
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			String line = scanner.nextLine().trim();
			String[] towels = line.split(", ");
			World world = new World(towels);
			while (scanner.hasNext()) {
				line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				if (world.checkPattern(line)) {
					System.out.println("GOOD: "+line);
					possiblePatterns++;
				}
			}
		}
		System.out.println("Possible patterns: "+possiblePatterns);
	}



	public static void mainPart2(String inputfile) throws FileNotFoundException {
		
		long numberPatterns = 0;
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			String line = scanner.nextLine().trim();
			String[] towels = line.split(", ");
			World world = new World(towels);
			while (scanner.hasNext()) {
				line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				long cntPatterns = world.countPatterns(line);
				numberPatterns = numberPatterns + cntPatterns; 
				System.out.println(cntPatterns+" for "+line);
			}
		}
		System.out.println("number of patterns: "+numberPatterns);
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day19/feri/input-example.txt");
//		mainPart1("exchange/day19/feri/input.txt");
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day19/feri/input-example.txt");
		mainPart2("exchange/day19/feri/input.txt");       // not 407858650
		System.out.println("---------------");
	}

	
}

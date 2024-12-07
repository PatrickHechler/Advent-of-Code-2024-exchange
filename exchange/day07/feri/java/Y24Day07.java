import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * see: https://adventofcode.com/2024/day/7
 *
 */
public class Y24Day07 {

	
	private static long concat(long num1, long num2) {
		return Long.parseLong(Long.toString(num1)+Long.toString(num2));
	}
	
	private static void addIfLE(Set<Long> nextAlternatives, long result, long possibleNextAlternative) {
		if (result<possibleNextAlternative) {
			return;
		}
		nextAlternatives.add(possibleNextAlternative);
	}
	
	private static boolean recursiveSolve(Set<Long> alternatives, long result, List<Long> arguments, boolean part2) {
		if (arguments.isEmpty()) {
			return alternatives.contains(result);
		}
		final long nextArg = arguments.removeFirst();
		Set<Long> nextAlternatives = new HashSet<>();
		for (long alternative:alternatives) {
			addIfLE(nextAlternatives, result, alternative+nextArg);
			addIfLE(nextAlternatives, result, alternative*nextArg);
			if (part2) {
				addIfLE(nextAlternatives, result, concat(alternative,nextArg));
			}
		}
		return recursiveSolve(nextAlternatives, result, arguments, part2);
	}

	private static boolean solvable(long result, List<Long> arguments, boolean part2) {
		Set<Long> alternatives = new HashSet<>();
		alternatives.add(arguments.removeFirst());
		return recursiveSolve(alternatives, result, arguments, part2);
	}

	public static void mainPart1(String inputfile) throws FileNotFoundException {
		
		long solution = 0;
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				long result = Long.parseLong(line.split(":")[0]);
				List<Long> arguments = Arrays.asList(line.split(":")[1].trim().split(" ")).stream().map(s->Long.parseLong(s)).collect(Collectors.toList());
				if (solvable(result, arguments, false)) {
					solution += result;
				}
			}
		}
		System.out.println("SOLUTION PART 1: "+solution);  
	}


	public static void mainPart2(String inputfile) throws FileNotFoundException {
		long solution = 0;
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				long result = Long.parseLong(line.split(":")[0]);
				List<Long> arguments = Arrays.asList(line.split(":")[1].trim().split(" ")).stream().map(s->Long.parseLong(s)).collect(Collectors.toList());
				if (solvable(result, arguments, true)) {
					solution += result;
				}
			}
		}
		System.out.println("SOLUTION PART 2: "+solution);  
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day07/feri/input-example.txt");
		mainPart1("exchange/day07/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day07/feri/input-example.txt");
		mainPart2("exchange/day07/feri/input.txt");
		System.out.println("---------------");
	}

	
}

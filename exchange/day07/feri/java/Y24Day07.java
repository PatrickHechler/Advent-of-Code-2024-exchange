import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2024/day/7
 *
 */
public class Y24Day07 {

	
	private static long concat(long num1, long num2) {
		return Long.parseLong(Long.toString(num1)+Long.toString(num2));
	}
	
	private static boolean recursiveSolve(long result, long[] arguments, int currentPos, long currentValue, boolean part2) {
		if (currentValue > result) {
			return false;
		}
		if (currentPos == arguments.length) {
			return currentValue == result;
		}
		long currentArgument = arguments[currentPos];
		boolean ok = recursiveSolve(result, arguments, currentPos+1, currentValue+currentArgument, part2);
		ok = ok || recursiveSolve(result, arguments, currentPos+1, currentValue*currentArgument, part2);
		if (part2) {
			ok = ok || recursiveSolve(result, arguments, currentPos+1, concat(currentValue,currentArgument), part2);
		}
		return ok;
	}

	private static boolean solvable(long result, long[] arguments, boolean part2) {
		return recursiveSolve(result, arguments, 1, arguments[0], part2);
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
				String[] strArguments = line.split(":")[1].trim().split(" ");
				long[] arguments = new long[strArguments.length];
				for (int i=0; i<strArguments.length; i++) {
					arguments[i] = Long.parseLong(strArguments[i]);
				}
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
				String[] strArguments = line.split(":")[1].trim().split(" ");
				long[] arguments = new long[strArguments.length];
				for (int i=0; i<strArguments.length; i++) {
					arguments[i] = Long.parseLong(strArguments[i]);
				}
				if (solvable(result, arguments, true)) {
					solution += result;
				}
			}
		}
		System.out.println("SOLUTION PART 2: "+solution);  
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
		StopWatch7.run("Day 7 Part 1", () -> {
//			mainPart1("exchange/day07/feri/input-example.txt");
			mainPart1("exchange/day07/feri/input.txt");     
		});
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		StopWatch7.run("Day 7 Part 2", () -> {
//			mainPart2("exchange/day07/feri/input-example.txt");
			mainPart2("exchange/day07/feri/input.txt");
		});	
		System.out.println("---------------");
	}

	
}

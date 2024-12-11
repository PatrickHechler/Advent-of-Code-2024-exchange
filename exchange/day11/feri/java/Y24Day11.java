import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2024/day/11
 *
 */
public class Y24Day11 {
	
	

	public static void mainPart1(String inputfile) throws FileNotFoundException {
			
		List<Long> numbers = new ArrayList<Long>();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String word = scanner.next().trim();
				numbers.add(Long.parseLong(word));
			}
		}
		System.out.println(numbers);
		for (int n=1; n<=25; n++) {
			numbers = blink(numbers);
			System.out.println(n+": "+numbers.size());
		}
		System.out.println("#numbers: "+numbers.size());
	}


	private static List<Long> blink(List<Long> numbers) {
		List<Long> result = new ArrayList<>();
		for (long number:numbers) {
			if (number == 0) {
				result.add(1L);
			}
			else {
				String numStr = Long.toString(number);
				if ((numStr.length()%2)==1) {
					result.add(number*2024L);
				}
				else {
					long num1 = Long.parseLong(numStr.substring(0, numStr.length()/2));
					long num2 = Long.parseLong(numStr.substring(numStr.length()/2));
					result.add(num1);
					result.add(num2);
				}
			}
		}
		return result;
	}


	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day11/feri/input-example.txt");
		mainPart1("exchange/day11/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day11/feri/input-example.txt");
//		mainPart2("exchange/day11/feri/input.txt");
		System.out.println("---------------");
	}

	
}

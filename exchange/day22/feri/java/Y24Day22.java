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
 * see: https://adventofcode.com/2024/day/21
 *
 */
public class Y24Day22 {
	
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {

		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					break;
				}
				int secret = Integer.parseInt(line);
				System.out.println("Secret: "+secret);
			}
		}
	}

	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
		mainPart1("exchange/day22/feri/input-example.txt");
//		mainPart1("exchange/day22/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day22/feri/input-example.txt");
//		mainPart2("exchange/day22/feri/input.txt");
		System.out.println("---------------");
	}

}

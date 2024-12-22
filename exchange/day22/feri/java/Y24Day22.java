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
			long sum2000 = 0;
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					break;
				}
				long secret = Long.parseLong(line);
				System.out.println("Secret: "+secret);
				System.out.println("  next: "+nextSecret(secret));
				for (int i=0; i<10; i++) {
					secret = nextSecret(secret);
					System.out.println("    "+(i+1)+":"+secret);
				}
				for (int i=0; i<2000-10; i++) {
					secret = nextSecret(secret);
				}
				sum2000 += secret;
				System.out.println("  2000: "+secret);
			}
			System.out.println("SUMME 2000: "+sum2000);
		}
	}

	private static long nextSecret(long secret) {
		long mult = secret*64;
		secret = secret ^ mult;
		secret = (secret % 16777216);
		long div = secret/32;
		secret = secret ^ div;
		secret = (secret % 16777216);
		long mult2 = secret*2048;
		secret = secret ^ mult2;
		secret = (secret % 16777216);
		return secret;
	}

	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day22/feri/input-example.txt");
//		mainPart1("exchange/day22/feri/input-example-2.txt");
		mainPart1("exchange/day22/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day22/feri/input-example.txt");
//		mainPart2("exchange/day22/feri/input.txt");
		System.out.println("---------------");
	}

}

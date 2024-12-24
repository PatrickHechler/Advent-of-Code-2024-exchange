import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
//				System.out.println("Secret: "+secret);
//				System.out.println("  next: "+nextSecret(secret));
				for (int i=0; i<10; i++) {
					secret = nextSecret(secret);
//					System.out.println("    "+(i+1)+":"+secret);
				}
				for (int i=0; i<2000-10; i++) {
					secret = nextSecret(secret);
				}
				sum2000 += secret;
//				System.out.println("  2000: "+secret);
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

	
	record Changes(int c4, int c3, int c2, int c1) {
		@Override
		public String toString() {
			return "("+c4+","+c3+","+c2+","+c1+")";
		}

	}
	
	record PriceChanges(long value, int c4, int c3, int c2, int c1) {
		public PriceChanges next(long newValue) {
			return new PriceChanges(newValue, c3, c2, c1, (int)(newValue-value)); 
		}
		public Changes getChanges() {
			return new Changes(c4, c3, c2, c1);
		}
		@Override
		public String toString() {
			return "("+value+"|"+c4+","+c3+","+c2+","+c1+")";
		}
	}
	
	public static void mainPart2(String inputfile) throws FileNotFoundException {
//		Changes watch = new Changes(2,-1,1,0);
		
		Map<Changes, Integer> sumFirstPrices = new HashMap<>();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					break;
				}
				long secret = Long.parseLong(line);
//				System.out.println("Secret: "+secret);
				PriceChanges changes = new PriceChanges(secret%10, 0, 0, 0, 0);
				long baseSecret = secret;
				for (int i=1; i<=3; i++) {
					secret = nextSecret(secret);
					changes = changes.next(secret%10);
//					System.out.println((i+1)+": "+secret+" "+changes);
				}
				Map<Changes, Integer> firstPrices = new HashMap<>();
				for (int i=4; i<=2000; i++) {
					secret = nextSecret(secret);
					changes = changes.next(secret%10);
					firstPrices.putIfAbsent(changes.getChanges(), (int)changes.value);
//					if (changes.getChanges().equals(watch)) {
//						System.out.println("     WATCH "+changes+" ("+secret+" = "+baseSecret+" x "+i+")");
////						showSequence(baseSecret, i-4, i);
//					}
////					System.out.println((i+1)+": "+secret+" "+changes);
				}
				for (Entry<Changes, Integer> entry:firstPrices.entrySet()) {
					addFirstPrice(sumFirstPrices, entry.getKey(), entry.getValue());
				}
//				Integer bp = firstPrices.get(watch);
//				if (bp != null) {
//					System.out.println("   FOUND "+bestPrices.get(watch)+"   "+watch+": + "+bestPrices.get(watch)+" = "+sumBestPrices.get(watch));
//				}
				
			}
		}
		int bestChangeValue = -1;
		Changes bestChange = null;
		for (Entry<Changes, Integer> entry:sumFirstPrices.entrySet()) {
			if (entry.getValue()>bestChangeValue) {
				bestChangeValue = entry.getValue();
				bestChange = entry.getKey();
			}
		}
		System.out.println("BEST CHANGE: "+bestChange+" value: "+bestChangeValue);
	}
	
	private static void showSequence(long startSecret, int from, int to) {
		System.out.println("       "+startSecret);
		long secret = startSecret;
		for (int i=1; i<=to; i++) {
			secret = nextSecret(secret);
			if (i>=from) {
				System.out.println("       x "+i+" = "+secret);
			}
		}
			
	}

	private static void addFirstPrice(Map<Changes, Integer> sumFirstPrices, Changes changes, int firstValue) {
		Integer sbp = sumFirstPrices.get(changes);
		if (sbp==null) {
			sbp = 0;
		}
		sumFirstPrices.put(changes, sbp+firstValue);
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day22/feri/input-example.txt");
//		mainPart1("exchange/day22/feri/input-example-2.txt");
		mainPart1("exchange/day22/feri/input.txt");     // = 14119253575
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		StopWatch22.run("Day 22 Part 2", () -> {
//			mainPart2("exchange/day22/feri/input-example.txt");
//			mainPart2("exchange/day22/feri/input-example-2.txt");
//			mainPart2("exchange/day22/feri/input-example-3.txt");
//			mainPart2("exchange/day22/feri/input-patrick.txt");     
			mainPart2("exchange/day22/feri/input.txt");     //
		});
		System.out.println("---------------");
	}

}

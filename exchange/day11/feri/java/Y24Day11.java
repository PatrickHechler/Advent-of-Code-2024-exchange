import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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


	public static class NUM {
		static Map<Long,NUM> nums = new HashMap<>(); 
		long value;
		NUM child1;
		NUM child2;
		long count;
		long nextCount;
		public NUM(long value) {
			this.value = value;
			this.count = 0;
			this.nextCount = 0;
			this.child1 = null;
			this.child2 = null;
		}
		public static NUM getNUM(long value) {
			return nums.computeIfAbsent(value, v -> new NUM(v));
		}
		public void blink() {
			if (child1 == null) {
				if (value == 0) {
					child1=getNUM(1);
				}
				else {
					String numStr = Long.toString(value);
					if ((numStr.length()%2)==1) {
						child1 = getNUM(value*2024L);
					}
					else {
						child1 = getNUM(Long.parseLong(numStr.substring(0, numStr.length()/2)));
						child2 = getNUM(Long.parseLong(numStr.substring(numStr.length()/2)));
					}
				}
			}
			child1.nextCount += count;
			if (child2 != null) {
				child2.nextCount += count;
			}
		}
		private void next() {
			this.count = nextCount;
			this.nextCount = 0;
		}
		public static void blinkAll() {
			List<NUM> numList = new ArrayList<>(nums.values());
			for (NUM num:numList) {
				num.blink();
			}
			for (NUM num:nums.values()) {
				num.next();
			}
		}
		public static long countAll() {
			long result = 0;
			for (NUM num:nums.values()) {
				result += num.count;
			}
			return result;
		}
		@Override
			public String toString() {
				return count+"x"+value;
			}
	}


	public static void mainPart2(String inputfile) throws FileNotFoundException {
		
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String word = scanner.next().trim();
				NUM num = NUM.getNUM(Long.parseLong(word));
				num.count++;
			}
		}
		System.out.println(NUM.nums);
		for (int n=1; n<=75; n++) {
			NUM.blinkAll();
			System.out.println(n+": "+NUM.countAll()+" ("+NUM.nums.size()+")");
		}
		System.out.println("#numbers: "+NUM.countAll());
		System.out.println("#distinct numbers: "+NUM.nums.size());
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day11/feri/input-example.txt");
		mainPart1("exchange/day11/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day11/feri/input-example.txt");
		mainPart2("exchange/day11/feri/input.txt");
		System.out.println("---------------");
	}

	
}

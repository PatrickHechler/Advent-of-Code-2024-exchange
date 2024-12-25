import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2024/day/24
 *
 */
public class Y24Day24 {
	
	static final String START_RX = "^([a-z0-9]+): (0|1)$";
	static final String OPERATION_RX = "^([a-z0-9]+) (AND|OR|XOR) ([a-z0-9]+) -> ([a-z0-9]+)$";
	
	record Rule (String reg1, String op, String reg2, String regTarget) {
		@Override
		public String toString() {
			return regTarget + " = "+reg1+" "+op+" "+reg2;
		}
	}
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {

		Map<String, Rule> rules = new LinkedHashMap<>();
		Map<String, Integer> regs = new LinkedHashMap<>();
		
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				if (line.matches(START_RX)) {
					String reg = line.replaceFirst(START_RX, "$1");
					int initValue = Integer.parseInt(line.replaceFirst(START_RX, "$2"));
					regs.put(reg, initValue);
				}
				else if (line.matches(OPERATION_RX)) {
					String reg1 = line.replaceFirst(OPERATION_RX, "$1");
					String op = line.replaceFirst(OPERATION_RX, "$2");
					String reg2 = line.replaceFirst(OPERATION_RX, "$3");
					String regTarget = line.replaceFirst(OPERATION_RX, "$4");
					Rule exists = rules.put(regTarget, new Rule(reg1, op, reg2, regTarget));
					if (exists != null) {
						throw new RuntimeException("duplicate rule for same target "+regTarget);
					}
				}
				else {
					throw new RuntimeException("unknown line");
				}
			}
			System.out.println(regs);
			System.out.println(rules);
			boolean changed = true;
			while (changed) {
				changed=false;
				for (Rule rule:rules.values()) {
					if (regs.containsKey(rule.regTarget)) {
						continue;
					}
					if (!regs.containsKey(rule.reg1)) {
						continue;
					}
					if (!regs.containsKey(rule.reg2)) {
						continue;
					}
					int v1 = regs.get(rule.reg1);
					int v2 = regs.get(rule.reg2);
					int vTarget; 
					switch (rule.op) {
					case "AND":
						vTarget = v1 & v2;
						break;
					case "OR":
						vTarget = v1 | v2;
						break;
					case "XOR":
						vTarget = v1 ^ v2;
						break;
					default:
						throw new RuntimeException("invalid operation "+rule.op);
					}
					regs.put(rule.regTarget, vTarget);
					changed = true;
				}
			}
			System.out.println(regs);
			String resultBits = "";
			int i = 0;
			while (regs.get(z(i)) != null) {
				resultBits = regs.get(z(i)) + resultBits;
				i++;
			}
			System.out.println("RESULT: "+resultBits);
			System.out.println("      = "+Long.parseLong(resultBits,2));
			
		}
	}

	private static String z(int i) {
		String result = Integer.toString(i);
		if (i<10) {
			result = "0"+result;
		}
		return "z"+result;
	}

	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day24/feri/input-example.txt");
		mainPart1("exchange/day24/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day24/feri/input-example.txt");
//		mainPart2("exchange/day24/feri/input.txt");     //
		System.out.println("---------------");
	}

}

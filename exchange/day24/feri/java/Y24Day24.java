import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
	
	public static class Circuit {
		Map<String, Rule> rules;
		Map<String, Integer> regs;
		public Circuit() {
			rules = new LinkedHashMap<>();
			regs = new LinkedHashMap<>();
		}
		public void setRegValue(String reg, int value) {
			regs.put(reg, value);
		}
		public void addRules(Rule rule) {
			if (rules.containsKey(rule.regTarget)) {
				throw new RuntimeException("duplicate rule target "+rule.regTarget);
			}
			rules.put(rule.regTarget, rule);			
		}
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			for (Rule rule:rules.values()) {
				result.append(rule.toString()).append("\n");
			}
			result.append("\n");
			List<String> sortedRegs = new ArrayList<>(regs.keySet());
			Collections.sort(sortedRegs);
			for (String reg:sortedRegs) {
				result.append(reg+": "+regs.get(reg));
			}
			return result.toString();
		}
		public void calcAll() {
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
		}
		public String getBinString(String prefix) {
			String result = "";
			int i=0;
			while (true) {
				String num = Integer.toString(i);
				if (i<10) {
					num = "0"+num;
				}
				String reg = prefix+num;
				if (!regs.containsKey(reg)) {
					break;
				}
				result = regs.get(reg) + result;
				i++;
			}
			return result;
		}
		public long getBinValue(String prefix) {
			return Long.parseLong(getBinString(prefix), 2);
		}
	}
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {

		Circuit circuit = new Circuit();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				if (line.matches(START_RX)) {
					String reg = line.replaceFirst(START_RX, "$1");
					int initValue = Integer.parseInt(line.replaceFirst(START_RX, "$2"));
					circuit.setRegValue(reg, initValue);
				}
				else if (line.matches(OPERATION_RX)) {
					String reg1 = line.replaceFirst(OPERATION_RX, "$1");
					String op = line.replaceFirst(OPERATION_RX, "$2");
					String reg2 = line.replaceFirst(OPERATION_RX, "$3");
					String regTarget = line.replaceFirst(OPERATION_RX, "$4");
					circuit.addRules(new Rule(reg1, op, reg2, regTarget));
				}
				else {
					throw new RuntimeException("unknown line");
				}
			}
			System.out.println(circuit);
			circuit.calcAll();
			System.out.println(circuit);
			System.out.println("RESULT: "+circuit.getBinString("z"));
			System.out.println("      = "+circuit.getBinValue("z"));
			
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

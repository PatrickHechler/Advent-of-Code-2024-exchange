import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2024/day/24
 *
 */
public class Y24Day24 {
	
	static final String START_RX = "^([a-z0-9]+): (0|1)$";
	static final String OPERATION_RX = "^([a-z0-9]+) (AND|OR|XOR) ([a-z0-9]+) -> ([a-z0-9]+)$";

	static final long XY_MASK = (1L<<44)-1L;
	static final long Z_MASK = (1L<<45)-1L;
	
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
		public Circuit swap(String ruleTarget1, String ruleTarget2) {
			Circuit result = new Circuit();
			Rule rule1 = rules.get(ruleTarget1);
			Rule rule2 = rules.get(ruleTarget2);
			Rule swap1 = new Rule(rule1.reg1, rule1.op, rule1.reg2, rule2.regTarget);
			Rule swap2 = new Rule(rule2.reg1, rule2.op, rule2.reg2, rule1.regTarget);
			for (Rule rule:rules.values()) {
				if (rule.regTarget.equals(ruleTarget1)) {
					result.addRules(swap1);
				}
				else if (rule.regTarget.equals(ruleTarget2)) {
					result.addRules(swap2);
				}
				else {
					result.addRules(rule);
				}
			}
			return result;
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
				result.append(reg+": "+regs.get(reg)).append("\n");
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
		public void clearRegs() {
			regs.clear();
		}
		public long getZ() {
			return getBinValue("z");
		}
		public void setX(long x) {
			setReg("x",x, 44);
		}
		public void setY(long y) {
			setReg("y",y, 44);
		}
		private void setReg(String prefix, long value, int maxBit) {
			long bit = 1L;
			for (int i=0; i<=maxBit; i++) {
				int b = 0;
				if ((value & bit) != 0L) {
					b = 1;
				}
				String num = Integer.toString(i);
				if (i<10) {
					num = "0"+num;
				}
				regs.put(prefix+num, b);
				bit = bit<<1;
			}
		}
		public List<String> getRuleTargets() {
			List<String> result = new ArrayList<>(rules.keySet());
			Collections.sort(result);
			return result;
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

	public static void mainPart2(String inputfile) throws FileNotFoundException {
		Circuit circuit = new Circuit();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				if (line.matches(START_RX)) {
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
			ErrStats es = findWeakBits(circuit, 10000);
			System.out.println("Error Stats: "+es);
			
			long x = es.errX;
			long y = es.errY;
			int bestCntErrBits = es.cntErrBits;
			String bestSwap1 = null;
			String bestSwap2 = null;
			List<String> ruleTargets = circuit.getRuleTargets();
			for (String target1:ruleTargets) {
				for (String target2:ruleTargets) {
					if (target1.equals(target2)) {
						continue;
					}
					Circuit swappedCircuit = circuit.swap(target1, target2);
					int cntErrBits = countErrBits(swappedCircuit, x, y);
					if (cntErrBits <= bestCntErrBits) {
						bestCntErrBits = cntErrBits;
						bestSwap1 = target1;
						bestSwap2 = target2;
						System.out.println("CntErrBits: "+cntErrBits+" "+target1+" <-> "+target2);
					}
							
				}
			}
			
		}
	}

	static Random rand = new Random(1);

	static record ErrStats(long errX, long errY, long errMaskZ, int cntErrBits, long weakBits, int cntWeakBits) {}
	
	private static ErrStats findWeakBits(Y24Day24.Circuit circuit, int iterations) {
		long maxErrBits = 0L;
		int maxCntErrBits = 0;
		long maxErrBitsX = 0L;
		long maxErrBitsY = 0L;
		long weakBits = 0L;
		long lastWeakBits = 0L;
		for (int i=0; i<iterations; i++) {
			long x = rand.nextLong() & XY_MASK;
			long y = rand.nextLong() & XY_MASK;
			long z = x + y;
			circuit.clearRegs();
			circuit.setX(x);
			circuit.setY(y);
			circuit.calcAll();
			long zComp = circuit.getZ();
			long errBits = zComp ^ z;
			int cntErrBits = Long.bitCount(errBits);
			if (cntErrBits>maxCntErrBits) {
				maxCntErrBits = cntErrBits;
				maxErrBits = errBits;
				maxErrBitsX = x;
				maxErrBitsY = y;
			}
			weakBits = weakBits | errBits;
			if (weakBits != lastWeakBits) {
				lastWeakBits = weakBits;
				System.out.println(i+": "+Long.toString(weakBits, 2));
			}
		}
		List<String> result = new ArrayList<>();
		for (int i=0; i<45; i++) {
			if (((1L<<i) & weakBits) != 0L) {
				String num = Integer.toString(i);
				if (i<10) {
					num = "0"+num;
				}
				result.add("z"+num);
			}
		}
		return new ErrStats(maxErrBitsX, maxErrBitsY, maxErrBits, maxCntErrBits, weakBits, Long.bitCount(weakBits));
	}

	private static int countErrBits(Circuit circuit, long x, long y) {
		long z = x + y;
		circuit.clearRegs();
		circuit.setX(x);
		circuit.setY(y);
		circuit.calcAll();
		long zComp = circuit.getZ();
		long errBits = zComp ^ z;
		int cntErrBits = Long.bitCount(errBits);
		return cntErrBits;
	}

	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day24/feri/input-example.txt");
//		mainPart1("exchange/day24/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day24/feri/input-example.txt");
		mainPart2("exchange/day24/feri/input.txt");     //
		System.out.println("---------------");
	}

}

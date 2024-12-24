package feri;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2024/day/24
 *
 */
public class Y24Day24Animation3D {
	
	static Y24GUIOutput3D23 output = null;

	
	
	static final long RAND_SEED = 4;
	static final double NET_DIST = 8.0;
	static final long NET_ITERATIONS = 50;
	static final double NET_SIZE_FACTOR = 0.25;

	static Random random = new Random(RAND_SEED);
	
	public static double rand(double from, double to) {
		return random.nextDouble()*(to-from)+from;
	}
	
    private static final DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
	public static String d(double d) {
		return df.format(d);
	}

	static record Pos3D(double x, double y, double z) {
		@Override public String toString() {
			return "("+d(x)+","+d(y)+","+d(z)+")";
		}
		public Pos3D add(Pos3D other) {
			return new Pos3D(x+other.x, y+other.y, z+other.z);  
		}
		public Pos3D subtract(Pos3D other) {
			return new Pos3D(x-other.x, y-other.y, z-other.z);  
		}
		public Pos3D multiply(double factor) {
			return new Pos3D(x*factor, y*factor, z*factor);  
		}
		public double magnitude() {
			return Math.sqrt(x*x+y*y+z*z);  
		}
		public Pos3D normalize() {
			double mag = magnitude();
			if (mag == 0) {
				return this;
			}
			return multiply(1/mag);  
		}
	}
	static Pos3D randomPos3D() {
		return new Pos3D(rand(-1000,1000), rand(-1000,1000), rand(-1000,1000));
	}

	static class Node3D {
		String name;
		Pos3D pos;
		Pos3D newPos;
		boolean fix;
		Set<Node3D> neighbours;
		public Node3D(String name, Pos3D pos) {
			this.name = name;
			this.pos = pos;
			neighbours = new LinkedHashSet<>();
			this.fix = false;
		}
		@Override public String toString() {
			return name+"["+pos+"#"+neighbours.size()+"]";
		}
		public void addConnection(Node3D otherNode) {
			neighbours.add(otherNode);
		}
		public void removeChild(Node3D child) {
			neighbours.remove(child);
		}
	}
	
	static class Node {
		String name;
		List<Node> children; 
		public Node(String name) {
			this.name= name;
			this.children = new ArrayList<>();
		}
		public void addNode(Node node) {
			children.add(node);
		}
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(name);
			String seperator = "[";
			for (Node child:children) {
				result.append(seperator).append(child.name);
				seperator = ",";
			}
			result.append("]");
			return result.toString();
		}
	}

	public static class World {
		Map<String, Node> nodes;
		Map<String, Node3D> nodes3D;
		public World() {
			this.nodes = new LinkedHashMap<>();
			this.nodes3D = new LinkedHashMap<>();
		}
		public void addNode(String nodeName, List<String> childNodeNames) {
			Node node = getOrCreateNode(nodeName);
			for (String childNodeName:childNodeNames) {
				node.addNode(getOrCreateNode(childNodeName));
			}
		}
		private Node getOrCreateNode(String nodeName) {
			return nodes.computeIfAbsent(nodeName, (k)->new Node(nodeName));
		}
		@Override
		public String toString() {
			return nodes.toString();
		}
		public void create3DTopology() {
			nodes3D = new HashMap<>();
			for (Node node:nodes.values()) {
				Node3D node3D;
				if (node.name.startsWith("z")) {
					int i=Integer.parseInt(node.name.substring(1));
					Pos3D pos = new Pos3D(2000.0*i/45-1000.0,0,1000);
					node3D = new Node3D(node.name, pos);
					node3D.fix = true;
				}
				else if (node.name.startsWith("x")) {
					int i=Integer.parseInt(node.name.substring(1));
					Pos3D pos = new Pos3D(2000.0*i/45-1000.0,-1000,-1000);
					node3D = new Node3D(node.name, pos);
					node3D.fix = true;
				}
				else if (node.name.startsWith("y")) {
					int i=Integer.parseInt(node.name.substring(1));
					Pos3D pos = new Pos3D(2000.0*i/45-1000.0,1000,-1000);
					node3D = new Node3D(node.name, pos);
					node3D.fix = true;
				}
				else {
					node3D = new Node3D(node.name, randomPos3D());
				}
				nodes3D.put(node3D.name, node3D);
			}
			for (Node node:nodes.values()) {
				for (Node child:node.children) {
					addNode3DConnection(node.name, child.name);
				}
			}
		}
		private void addNode3DConnection(String nodeName1, String nodeName2) {
			Node3D node1 = nodes3D.get(nodeName1);
			Node3D node2 = nodes3D.get(nodeName2);
			node1.addConnection(node2);
			node2.addConnection(node1);
		}
		public void move3DNodes() {
			for (Node3D node:nodes3D.values()) {
				if (node.fix) {
					node.newPos = node.pos;
					continue;
				}
				Pos3D sum = new Pos3D(0,0,0);
//				System.out.println("NODE "+node);
				int cntTargets = 0;
				for (Node3D neighbour:node.neighbours) {
//					System.out.println("  neighbout "+neighbour);
					Pos3D vect = neighbour.pos.subtract(node.pos);
					double dist = vect.magnitude();
//					System.out.println("  dist: "+dist);
					double move = dist-NET_DIST;
//					System.out.println("  move: "+move);
					Pos3D mVect = vect.normalize().multiply(move*0.5);
//					System.out.println("  mVect: "+mVect);
					Pos3D target = node.pos.add(mVect);
//					System.out.println("  target: "+target);
					sum = sum.add(target);
					cntTargets++;
//					System.out.println("  SUM: "+sum);
				}
				for (Node3D otherNode:nodes3D.values()) {
					if (otherNode == node) {
						continue;
					}
					Pos3D vect = otherNode.pos.subtract(node.pos);
					double dist = vect.magnitude();
					if (dist<NET_DIST/2) {
						double move = dist-NET_DIST/2;
						Pos3D mVect = vect.normalize().multiply(0.5*move);
						Pos3D target = node.pos.add(mVect);
						sum = sum.add(target);
						cntTargets++;
					}
				}
				Pos3D targetPos = sum.multiply(1.0/cntTargets);
//				System.out.println("  SUM/#: "+targetPos);
//				Pos3D halfWay = targetPos.subtract(node.pos)
				node.newPos = targetPos;
			}
//			System.out.println();
			for (Node3D node:nodes3D.values()) {
				node.pos = node.newPos;
			}
		}

		public void show3D(String info, Map<String, Integer> nodeColors) {
			List<Y24GUIOutput3D23.DDDObject> points = new ArrayList<>();
			for (Node3D node:nodes3D.values()) {
				int defaultType = 11;
				int defaultLineType = 3;
				if ((nodeColors != null) && nodeColors.containsKey(node.name)) {
					// defaultLineType = 0;
					defaultType = nodeColors.get(node.name);
				}
				int type = defaultType;
				double size = 2.0;
				double boxSize = size*NET_SIZE_FACTOR;
				Y24GUIOutput3D23.DDDObject point = new Y24GUIOutput3D23.DDDObject(node.name, node.pos.x, node.pos.y, node.pos.z, boxSize, type);
				points.add(point);
				for (Node3D neighbour:node.neighbours) {
					String lineName = node.name+"-"+neighbour.name;
					int lineType=defaultLineType;
					double lineSize = 0.5*NET_SIZE_FACTOR;
//					if (cluster.contains(node.name) && cluster.contains(neighbour.name)) {
//						lineType=0;
//						lineSize=2*lineSize;
//					}
					Y24GUIOutput3D23.DDDObject line = new Y24GUIOutput3D23.DDDLineObject(lineName, node.pos.x, node.pos.y, node.pos.z, neighbour.pos.x, neighbour.pos.y, neighbour.pos.z, lineSize, 30+lineType);
					points.add(line);
				}
			}
			if (output.scale == 1) {
				output.adjustScale(points);
			}
			output.addStep(info, points);
		}
		public void remove(String nodeName1, String nodeName2) {
			Node3D node1 = nodes3D.get(nodeName1);
			Node3D node2 = nodes3D.get(nodeName2);
			node1.removeChild(node2);
			node2.removeChild(node1);
		}
		public Set<String> collectCluster(String startNodeName) {
			Set<String> clusterNodes = new LinkedHashSet<>();
			Set<String> newNodes = new LinkedHashSet<>();
			newNodes.add(startNodeName);
			while (!newNodes.isEmpty()) {
				String nodeName = newNodes.iterator().next(); 
				Node3D node = nodes3D.get(nodeName);
				newNodes.remove(nodeName);
				clusterNodes.add(nodeName);
				for (Node3D child:node.neighbours) {
					if (clusterNodes.contains(child.name)) {
						continue;
					}
					newNodes.add(child.name);
				}
			}
			return clusterNodes;
		}
		public void remove(String lineName) {
			String[] nodeNames = lineName.split("-");
			remove(nodeNames[0], nodeNames[1]);
		}
		public String findLongestLine() {
			double maxDist = 0;
			String result = "?-?";
			for (Node3D node:nodes3D.values()) {
				for (Node3D child:node.neighbours) {
					double dist = child.pos.subtract(node.pos).magnitude();
					if (dist>maxDist) {
						maxDist = dist;
						result = node.name+"-"+child.name;
					}
				}
			}
			return result;
		}
		public void move3DNodes(long iterations, int show, Map<String, Integer> nodeColors) {
			for (int n=0; n<iterations; n++) {
				move3DNodes();
				if ((show>0) && (n%show)==show-1) {
					show3D("iteration "+n, nodeColors);
				}
			}
		}
	}
	
	
	
	static final String START_RX = "^([a-z0-9]+): (0|1)$";
	static final String OPERATION_RX = "^([a-z0-9]+) (AND|OR|XOR) ([a-z0-9]+) -> ([a-z0-9]+)$";

	static final long XY_MASK = (1L<<44)-1L;
	static final long Z_MASK = (1L<<45)-1L;
	
	public static String reg(String prefix, int i) {
		return i<10 ? prefix+"0"+i : prefix+i;
	}

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
			long result = 0;
		    long bit=1;
			int i=0;
			while (true) {
				String reg = i<10 ? "z0"+i : "z"+i;
				if (!regs.containsKey(reg)) {
					break;
				}
				if (regs.get(reg)==1) {
					result = result | bit;
				}
				bit = bit << 1;
				i++;
			}
			return result;
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
//			System.out.println(circuit);
			circuit.calcAll();
//			System.out.println(circuit);
//			System.out.println("RESULT: "+circuit.getBinString("z"));
			System.out.println("      = "+circuit.getBinValue("z"));
		}
	}
	
	record Swap(String target1, String target2, int err) {
		@Override public String toString() {
			return target1+"<->"+target2+"["+err+"]";
		}
	}

	public static void mainPart2(String inputfile) throws FileNotFoundException {
		
		output = new Y24GUIOutput3D23("2024 Day 23 Part II", true);

		World world = new World(); 
		
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
					world.addNode(regTarget, Arrays.asList(reg1, reg2));
				}
				else {
					throw new RuntimeException("unknown line");
				}
			}

			Map<String, Integer> nodeColors = new HashMap<>(); 
			for (int i=0; i<=45; i++) {
				nodeColors.put(reg("x",i), 12);
				nodeColors.put(reg("y",i), 12);
				nodeColors.put(reg("z",i), 10);
			}

			world.create3DTopology();
			world.show3D("INIT", nodeColors);
			world.move3DNodes(20, 1, nodeColors);
			world.move3DNodes(480, 20, nodeColors);

			System.out.println(circuit);
			
//			ErrStats es = findWeakBits(circuit, 10000, false);
//			long x = es.errX;
//			long y = es.errY;
//			List<Swap> bestSwaps1 = qoptimize(circuit, x, y);
//			for (Swap bestSwap1:bestSwaps1) {
//				System.out.println();
//				System.out.println("SWAP1 "+bestSwap1);
//				Circuit swapped1Circuit = circuit.swap(bestSwap1.target1, bestSwap1.target2); 
//				List<Swap> bestSwaps2 = qoptimize(swapped1Circuit, x, y);
//				bestSwaps2 = limit(bestSwaps2, 10);
//				for (Swap bestSwap2:bestSwaps2) {
//					System.out.println();
//					System.out.println("SWAP-2 "+bestSwap2+" SWAP-1 "+bestSwap1);
//					Circuit swapped2Circuit = swapped1Circuit.swap(bestSwap2.target1, bestSwap2.target2); 
//					List<Swap> bestSwaps3 = qoptimize(swapped2Circuit, x, y);
//					bestSwaps3 = limit(bestSwaps3, 10);
//					for (Swap bestSwap3:bestSwaps3) {
//						System.out.println();
//						System.out.println("SWAP~3 "+bestSwap3+" SWAP~2 "+bestSwap2+" SWAP~1 "+bestSwap1);
//						Circuit swapped3Circuit = swapped2Circuit.swap(bestSwap3.target1, bestSwap3.target2); 
//						List<Swap> bestSwaps4 = qoptimize(swapped3Circuit, x, y);
//						for (Swap bestSwap4:bestSwaps4) {
//							if (bestSwap4.err>0) {
//								break;
//							}
//							System.out.println();
//							System.out.println("SWAP:4 "+bestSwap4+" SWAP:3 "+bestSwap3+" SWAP:2 "+bestSwap2+" SWAP:1 "+bestSwap1);
//							Circuit swapped4Circuit = swapped3Circuit.swap(bestSwap4.target1, bestSwap4.target2);
//							es = findWeakBits(swapped4Circuit, 10000, true);
//							System.out.println(es);
//							if (es.cntWeakBits == 0) {
//								return;
//							}
//						}
//					}
//				}
//			}
		}
	}

	private static List<Swap> limit(List<Swap> fullList, int cnt) {
		List<Swap> result = new ArrayList<>();
		for (int i=0; i<cnt; i++) {
			result.add(fullList.get(i));
		}
		return result;
	}


	private static List<Swap> qoptimize(Circuit circuit, long x, long y) {
		int bestCntErrBits = Integer.MAX_VALUE;
		List<Swap> bestSwaps = new ArrayList<>();
		List<String> ruleTargets = circuit.getRuleTargets();
		for (int i=0; i<ruleTargets.size(); i++) {
			String target1=ruleTargets.get(i);
			for (int j=i+1; j<ruleTargets.size(); j++) {
				String target2=ruleTargets.get(j);
				Circuit swappedCircuit = circuit.swap(target1, target2);
				int cntErrBits;
				try {
					cntErrBits = countErrBits(swappedCircuit, x, y);
				}
				catch (NumberFormatException e) {
					// unreachable X   - SWAP1 jrp<->qmd[8] SWAP2 bpr<->ggm[6]
					continue;
				}
				bestSwaps.add(new Swap(target1, target2, cntErrBits));
				if (cntErrBits < bestCntErrBits) {
					bestCntErrBits = cntErrBits;
					if (cntErrBits == 0) {
						bestSwaps.clear();
						bestSwaps.add(new Swap(target1, target2, cntErrBits));
						System.out.println("SOLUTION "+target1+"<->"+target2);
						return bestSwaps;
					}
					System.out.println("CntErrBits: "+cntErrBits+" "+target1+" <-> "+target2);
				}
			}
		}
		Collections.sort(bestSwaps, (sw1,sw2)->Integer.compare(sw1.err, sw2.err));
		return bestSwaps;
	}

	static Random rand = new Random(1);

	static record ErrStats(long errX, long errY, long errMaskZ, int cntErrBits, long weakBits, int cntWeakBits) {}
	
	private static ErrStats findWeakBits(Circuit circuit, int iterations, boolean quitImmediately) {
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
			weakBits = weakBits | errBits;
			if (weakBits != lastWeakBits) {
				lastWeakBits = weakBits;
				System.out.println(i+": "+Long.toString(weakBits, 2));
			}
			if (cntErrBits>maxCntErrBits) {
				maxCntErrBits = cntErrBits;
				maxErrBits = errBits;
				maxErrBitsX = x;
				maxErrBitsY = y;
				if (quitImmediately) {
					return new ErrStats(maxErrBitsX, maxErrBitsY, maxErrBits, maxCntErrBits, weakBits, Long.bitCount(weakBits));
				}
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
//		mainPart2("exchange/day24/feri/input.txt");   
		mainPart2("input-day24.txt");     
		System.out.println("---------------");
	}

}

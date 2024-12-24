package feri;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;


/**
 * see: https://adventofcode.com/2024/day/23
 */
public class Y24Day23Animation3D {

	static Y24GUIOutput3D23 output;

	/*
	 * Example:
	 * 
	 * aa-bb
	 * bb-cc
	 * cc-dd
	 * dd-ee
	 * ee-ff
	 * ff-gg
	 * cc-aa
	 * ff-dd
	 * 
	 */

	private static final String INPUT_RX = "^([a-z]+): ([a-z ]+)$";
	
	public static record InputData(String nodeName, List<String> childNodeNames) {}
	
	public static class InputProcessor implements Iterable<InputData>, Iterator<InputData> {
		private Scanner scanner;
		public InputProcessor(String inputFile) {
			try {
				scanner = new Scanner(new File(inputFile));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		@Override public Iterator<InputData> iterator() { return this; }
		@Override public boolean hasNext() { return scanner.hasNext(); }
		@Override public InputData next() {
			String line = scanner.nextLine().trim();
			while (line.length() == 0) {
				line = scanner.nextLine();
			}
			if (line.matches(INPUT_RX)) {
				String nodeName = line.replaceFirst(INPUT_RX, "$1");
				List<String> childNodeNames = Arrays.asList(line.replaceFirst(INPUT_RX, "$2").trim().split(" +"));
				return new InputData(nodeName, childNodeNames);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}


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
		Set<Node3D> neighbours;
		public Node3D(String name, Pos3D pos) {
			this.name = name;
			this.pos = pos;
			neighbours = new LinkedHashSet<>();
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
				Node3D node3D = new Node3D(node.name, randomPos3D());
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

		public void show3D(String info, Set<String> cluster) {
			List<Y24GUIOutput3D23.DDDObject> points = new ArrayList<>();
			for (Node3D node:nodes3D.values()) {
				int defaultType = 11;
				int defaultLineType = 3;
				if ((cluster != null) && cluster.contains(node.name)) {
					// defaultLineType = 0;
					defaultType = 10;
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
					if (cluster.contains(node.name) && cluster.contains(neighbour.name)) {
						lineType=0;
						lineSize=2*lineSize;
					}
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
		public void move3DNodes(long iterations, int show, Set<String> cluster) {
			for (int n=0; n<iterations; n++) {
				move3DNodes();
				if ((show>0) && (n%show)==show-1) {
					show3D("iteration "+n, cluster);
				}
			}
		}
	}
	





	record ThreeNet(String c1, String c2, String c3) {
		public static ThreeNet createSorted(String ca, String cb, String cc) {
			String[] sortedC = new String[] {ca, cb, cc};
			Arrays.sort(sortedC);
			return new ThreeNet(sortedC[0], sortedC[1], sortedC[2]);
		}
		@Override
		public String toString() {
			return c1+","+c2+","+c3;
		}
	}
	
	public static void mainPart2(String inputfile) throws FileNotFoundException {

		output = new Y24GUIOutput3D23("2024 Day 23 Part II", true);

		Map<String, Set<String>> connections = new HashMap<>();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					break;
				}
				String[] computers = line.split("-");
				connections.computeIfAbsent(computers[0], (k)->new LinkedHashSet<>()).add(computers[1]);
				connections.computeIfAbsent(computers[1], (k)->new LinkedHashSet<>()).add(computers[0]);
			}
			World world = new World();

			Set<Set<String>> clusters = new LinkedHashSet<>();
			for (String computer:connections.keySet()) {
				Set<String> cluster = new LinkedHashSet<>();
				cluster.add(computer);
				clusters.add(cluster);
				world.addNode(computer, new ArrayList<>(connections.get(computer)));
			}
			
			for (int i=2; i<=connections.keySet().size(); i++) {
				Set<Set<String>> nextClusters = new LinkedHashSet<>();
				for (Set<String> cluster:clusters) {
					for (String newComputer:connections.keySet()) {
						if (cluster.contains(newComputer)) {
							continue;
						}
						Set<String> newComputerConnections = connections.get(newComputer);
						boolean fullyConnected = true;
						for (String clusterComputer:cluster) {
							if (!newComputerConnections.contains(clusterComputer)) {
								fullyConnected=false;
								break;
							}
						}
						if (fullyConnected) {
							Set<String> nextCluster = new LinkedHashSet<>(cluster);
							nextCluster.add(newComputer);
							nextClusters.add(nextCluster);
						}
					}
				}
				System.out.println("  Fully connected Networks of size "+i+": "+nextClusters.size());
				if (nextClusters.size() == 0) {
					System.out.println("FOUND "+clusters.size()+" cluster with max size "+(i-1));
					Set<String> maxCluster = clusters.iterator().next();
					List<String> sortedComputers = new ArrayList<>(maxCluster);
					Collections.sort(sortedComputers);
					System.out.println("MAX NETWORK CLUSTER: "+sortedComputers.toString().replace(" ", "").replace("[", "").replace("]", ""));
					world.create3DTopology();
					world.show3D("INIT", maxCluster);
					world.move3DNodes(20, 1, maxCluster);
					world.move3DNodes(480, 20, maxCluster);
					break;
				}
				clusters = nextClusters;
			}
		}
	}


	public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
		System.out.println("--- PART II ---");
//		mainPart2("input-example-2.txt");
//		mainPart2("input-example.txt");
		mainPart2("input.txt");
		System.out.println("---------------");    
	}
	
}

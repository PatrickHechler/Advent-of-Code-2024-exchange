import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2024/day/23
 *
 */
public class Y24Day23 {
	
	
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
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {

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
			Set<ThreeNet> t3Nets = new LinkedHashSet<>();
			for (String computer1:connections.keySet()) {
				if (computer1.startsWith("t")) {
					for (String computer2:connections.get(computer1)) {
						for (String computer3:connections.get(computer2)) {
							if (connections.get(computer3).contains(computer1)) {
								t3Nets.add(ThreeNet.createSorted(computer1, computer2, computer3));
							}
						}
					}
				}
			}
//			System.out.println(t3Nets);
			System.out.println(t3Nets.size());
		}
	}

	
	public static void mainPart2(String inputfile) throws FileNotFoundException {

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
			Set<Set<String>> clusters = new LinkedHashSet<>();
			for (String computer:connections.keySet()) {
				Set<String> cluster = new LinkedHashSet<>();
				cluster.add(computer);
				clusters.add(cluster);
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
					break;
				}
				clusters = nextClusters;
			}
//			System.out.println(t3Nets);
		}
	}
	


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day23/feri/input-example.txt");
		mainPart1("exchange/day23/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		StopWatch22.run("Day 23 Part 2", () -> {
//			mainPart2("exchange/day23/feri/input-example.txt");
			mainPart2("exchange/day23/feri/input.txt");
		});
		System.out.println("---------------");
	}

}

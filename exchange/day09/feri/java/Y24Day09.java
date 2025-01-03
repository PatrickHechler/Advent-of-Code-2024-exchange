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
 * see: https://adventofcode.com/2024/day/9
 *
 */
public class Y24Day09 {
	
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {
			
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				ArrayList<Integer> disk = new ArrayList<>();
				line = line + "0";
				for (int i=0; i<line.length()/2; i++) {
					int nFile = line.charAt(2*i)-'0'; 
					int nSpace = line.charAt(2*i+1)-'0';
					for (int n=0; n<nFile; n++) {
						disk.add(i);
					}
					for (int n=0; n<nSpace; n++) {
						disk.add(-1);
					}
				}
				System.out.println(disk);
				for (int i=0; i<disk.size(); i++) {
					while (disk.get(i)==-1) {
						int v = disk.remove(disk.size()-1);
						disk.set(i, v);
//						System.out.println(disk);
					}
				}
				long solution = 0;
				for (int i=0; i<disk.size(); i++) {
					solution += disk.get(i)*i;
				}
				System.out.println(solution);
			}
		}
	}


	public static void mainPart2(String inputfile) throws FileNotFoundException {
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					continue;
				}
				
				ArrayList<Integer> disk = new ArrayList<>();
				line = line + "0";
				for (int i=0; i<line.length()/2; i++) {
					int nFile = line.charAt(2*i)-'0'; 
					int nSpace = line.charAt(2*i+1)-'0';
					for (int n=0; n<nFile; n++) {
						disk.add(i);
					}
					for (int n=0; n<nSpace; n++) {
						disk.add(-1);
					}
				}
				int currentPos = disk.size()-1;
				while (currentPos>0) {
//					System.out.println(disk);
					while (disk.get(currentPos) == -1) {
						currentPos--;
					}
					int fileNr = disk.get(currentPos);
					int nLen = 1;
					while ((currentPos>0) && (disk.get(currentPos-1) == fileNr)) {
						currentPos--;
						nLen++;
					}
					int nGap = searchGap(disk, nLen);
					if ((nGap != -1) && (nGap < currentPos)) {
						for (int i=0; i<nLen; i++) {
							disk.set(currentPos+i, -1);
							disk.set(nGap+i, fileNr);
						}
					}
					currentPos--;
				}
				System.out.println(disk);
				long solution = 0;
				for (int i=0; i<disk.size(); i++) {
					int fNr = disk.get(i);
					if (fNr!=-1) {
						solution += fNr*i;
					}
				}
				System.out.println(solution);
			}
		}
	}


	
	private static int searchGap(ArrayList<Integer> disk, int nLen) {
		for (int i=0; i<disk.size()-nLen; i++) {
			boolean gap = true;
			for (int j=i; j<i+nLen; j++) {
				gap = gap && disk.get(j)==-1;
			}
			if (gap) {
				return i;
			}
		}
		return -1;
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day09/feri/input-example.txt");
		mainPart1("exchange/day09/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day09/feri/input-example.txt");
		mainPart2("exchange/day09/feri/input.txt");
		System.out.println("---------------");
	}

	
}

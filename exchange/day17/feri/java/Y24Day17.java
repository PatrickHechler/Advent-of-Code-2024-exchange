import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2024/day/17
 *
 */
public class Y24Day17 {

	static final String REGISTER_RX = "^Register (A|B|C): ([0-9]+)$";
	static final String PROGRAM_RX = "^Program: ([0-9,]+)$";
	
	
	public static enum OPCODE {
		ADV, BXL, BST, JNZ, BXC, OUT, BDV, CDV
	}

	
	public static class CPU {
		long A;
		long B;
		long C;
		int[] PROG;
		int ip;
		int ticks;
		StringBuilder OUT;
		public CPU(long a, long b, long c, int[] prog) {
			this.A = a;
			this.B = b;
			this.C = c;
			this.PROG = prog;
			this.ip = 0;
			this.ticks = 0;
			this.OUT = new StringBuilder();
		}
		public CPU createCopy(long newA) {
			return new CPU(newA, B, C, PROG);
		}
		public boolean exec(boolean debug) {
			if ((ip<0) || (ip>=PROG.length)) {
				return false;
			}
			ticks++;
			int code = PROG[ip];
			int arg = PROG[ip+1];
			ip += 2;
			OPCODE opc = OPCODE.values()[code];
			if (debug) {
				System.out.println("  EXEC "+opc+" "+arg);
			}
			switch (opc) {
			case ADV:
				A = A / (1 << combo(arg));
				break;
			case BXL:
				B = B ^ arg;
				break;
			case BST:
				B = combo(arg);
				break;
			case JNZ:
				if (A != 0) {
					ip = arg;
				}
				break;
			case BXC:
				B = B ^ C;
				break;
			case OUT:
				out(combo(arg), debug);
				if (OUT.length()>100) {
					return false;
				}
				break;
			case BDV:
				B = A / (1 << combo(arg));
				break;
			case CDV:
				C = A / (1 << combo(arg));
				break;
			default:
				throw new RuntimeException("unknow opcode "+opc+" for code "+code);
			}
			return true;
		}
		private void out(int value, boolean debug) {
			if (debug) {
				System.out.println("  OUT["+value+"]");
			}
			if (!OUT.isEmpty()) {
				OUT.append(",");
			}
			OUT.append(Integer.toString(value));
		}
		public String getOutput() {
			return OUT.toString();
		}
		private int combo(int arg) {
			int result = arg;
			switch (result) {
			case 0:
			case 1:
			case 2:
			case 3:
				break;
			case 4:
				result = (int)A & 0x07;
				break;
			case 5:
				result = (int)B & 0x07;
				break;
			case 6:
				result = (int)C & 0x07;
				break;
			case 7:
				throw new RuntimeException("reserver combo value 7");
			default:
				throw new RuntimeException("unknow combo value "+result);
			}
			return result;
		}
		@Override
		public String toString() {
			return "CPU["+ip+"|"+Long.toString(A, 16)+","+Long.toString(B, 16)+","+Long.toString(C, 16)+"|"+A+","+B+","+C+"|"+"]";
		}
	}
	
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {

		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				int a = Integer.parseInt(scanner.nextLine().trim().replaceFirst(REGISTER_RX, "$2"));
				int b = Integer.parseInt(scanner.nextLine().trim().replaceFirst(REGISTER_RX, "$2"));
				int c = Integer.parseInt(scanner.nextLine().trim().replaceFirst(REGISTER_RX, "$2"));
				scanner.nextLine();
				String[] strProg = scanner.nextLine().trim().replaceFirst(PROGRAM_RX, "$1").split(",");
				int[] prog = new int[strProg.length];
				for (int i=0; i<strProg.length; i++) {
					prog[i] = Integer.parseInt(strProg[i]);
				}
				System.out.println("---------------------");
				CPU cpu = new CPU(a,b,c, prog);
				System.out.println(cpu.toString());
				while(cpu.exec(true)) {
					System.out.println(cpu.toString());
				}
				System.out.println(cpu.getOutput());
				if (scanner.hasNext()) {
					scanner.nextLine();
				}
			}
		}
	}


	public static String run(CPU cpuTemplate, long A) {
		CPU cpu = cpuTemplate.createCopy(A);
		while (cpu.exec(false)) {}
		return cpu.getOutput();
	}

	public static List<Long> recursiveSearchA(CPU cpuTemplate, String searchString) {
		if (searchString.equals("")) {
			return Arrays.asList(0L);
		}
		List<Long> result = new ArrayList<>();
		List<Long> possibleAs = recursiveSearchA(cpuTemplate, searchString.substring(Math.min(searchString.length(), 2)));
		for (long possibleA:possibleAs) {
			for (long a=0; a<=7; a++) {
				if (run(cpuTemplate, (possibleA<<3)+a).equals(searchString)) {
					result.add((possibleA<<3)+a);
				}
			}
		}
		System.out.println("FOUND for "+searchString+": "+result);
		return result;
	}
	
	

	public static void mainPart2(String inputfile) throws FileNotFoundException {

		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				int a = Integer.parseInt(scanner.nextLine().trim().replaceFirst(REGISTER_RX, "$2"));
				int b = Integer.parseInt(scanner.nextLine().trim().replaceFirst(REGISTER_RX, "$2"));
				int c = Integer.parseInt(scanner.nextLine().trim().replaceFirst(REGISTER_RX, "$2"));
				scanner.nextLine();
				String program = scanner.nextLine().trim().replaceFirst(PROGRAM_RX, "$1");
				String[] strProg = program.split(",");
				int[] prog = new int[strProg.length];
				for (int i=0; i<strProg.length; i++) {
					prog[i] = Integer.parseInt(strProg[i]);
				}
				System.out.println("---------------------");
				CPU cpuTemplate = new CPU(0,b,c, prog);
				List<Long> results = recursiveSearchA(cpuTemplate, program);
				System.out.println(results.stream().reduce(Long.MAX_VALUE, Long::min));

			
			
			}
		}
	}

	/*

	public static String padl(String txt, char padChar, int len) {
		String result = txt;
		while (result.length()<len) {
			result = padChar + result;
		}
		return result;
	}
	
	public static String run(long A) {
		StringBuilder result = new StringBuilder(); 
		// long A=0b001011100;
		long B=0;
		long C=0;
		
		while (true) {
//			System.out.println("A: "+Integer.toString(A, 2));
			//  0: BST 4
			//  2: BXL 3
			B = (A^0x3) & 0x7;    // --> A: xxx(000) --> 011  / xxx011 --> 000
//			System.out.println("B: "+Integer.toString(B, 2));
					
			//  4: CDV 5
			C = A / (1 << B);
//			System.out.println("C: "+Integer.toString(C, 2));
			
			//  6: BXL 5
			// 10: BXC 3
			B = B ^ 0x5 ^ C;
			
			// 12: OUT 5
//			System.out.println("OUT["+(B&0x7)+"]");
			result.append(""+(B&0x7));
			if (result.length()>100) {
				break;
			}
			
			//  8: ADV 3
			A = A >> 3;
			
			B = 0;
			C = 0;
			
			// 14: JNZ 0
			if (A==0) {
				break;
			}
		}
		return result.toString(); 
	}

	*/
	
	/*
	A: 000000 -> 6
	A: 000001 -> 7
	A: 000010 -> 5
	A: 000011 -> 6
	A: 000100 -> 2
	A: 000101 -> 3
	A: 000110 -> 0
	A: 000111 -> 1
	A: 001000 -> 77
	A: 001001 -> 57
	A: 001010 -> 17
	A: 001011 -> 67
	A: 001100 -> 27
	A: 001101 -> 37
	A: 001110 -> 07
	A: 001111 -> 17
	A: 010000 -> 45
	A: 010001 -> 35
	A: 010010 -> 55
	A: 010011 -> 65
	A: 010100 -> 25
	A: 010101 -> 35
	A: 010110 -> 05
	A: 010111 -> 05
	A: 011000 -> 56
	A: 011001 -> 16
	A: 011010 -> 16
	A: 011011 -> 66
	A: 011100 -> 26
	A: 011101 -> 36
	A: 011110 -> 06
	A: 011111 -> 06
	A: 100000 -> 22
	A: 100001 -> 72
	A: 100010 -> 52
	A: 100011 -> 62
	A: 100100 -> 22
	A: 100101 -> 32
	A: 100110 -> 12
	A: 100111 -> 32
	A: 101000 -> 33
	A: 101001 -> 53
	A: 101010 -> 13
	A: 101011 -> 63
	A: 101100 -> 23
	A: 101101 -> 33
	A: 101110 -> 13
	A: 101111 -> 33
	A: 110000 -> 00
	A: 110001 -> 30
	A: 110010 -> 50
	A: 110011 -> 60
	A: 110100 -> 20
	A: 110101 -> 30
	A: 110110 -> 10
	A: 110111 -> 20
	A: 111000 -> 11
	A: 111001 -> 11
	A: 111010 -> 11
	A: 111011 -> 61
	A: 111100 -> 21
	A: 111101 -> 31
	A: 111110 -> 11
	A: 111111 -> 21
	
	2,4,1,3,7,5,1,5,0,3,4,3,5,5,3,0

	starting from end: 0
	
	only possible A:
	
	A: 000110 -> 0
	
	next is 3 starting A with 110
	
	A: 110001 -> 30
	A: 110101 -> 30
	
	next is 5 starting A with 110001 or 110101 
	
	*/

	/*
	
	// 2,4,1,3,7,5,1,5,0,3,4,3,5,5,3,0
	public static void test_manual() {
		int A=0b001011100;
		int B=0;
		int C=0;
		
		while (true) {
			System.out.println("A: "+Integer.toString(A, 2));
			//  0: BST 4
			//  2: BXL 3
			B = (A^0x3) & 0x7;    // --> A: xxx(000) --> 011  / xxx011 --> 000
			System.out.println("B: "+Integer.toString(B, 2));
					
			//  4: CDV 5
			C = A / (1 << B);
			System.out.println("C: "+Integer.toString(C, 2));
			
			//  6: BXL 5
			// 10: BXC 3
			B = B ^ 0x5 ^ C;
			
			// 12: OUT 5
			System.out.println("OUT["+(B&0x7)+"]");
			
			//  8: ADV 3
			A = A >> 3;
			
			B = 0;
			C = 0;
			
			// 14: JNZ 0
			if (A==0) {
				break;
			}
		}
	}

	public static void test_ORIG() {
		int A=47006051;
		int B=0;
		int C=0;
		
		while (true) {
			//  0: BST 4
			B = A & 0x7;
					
			//  2: BXL 3
			B = B ^ 0x3;
					
			//  4: CDV 5
			C = A / (1 << (B&0x7));
			
			//  6: BXL 5
			B = B ^ 0x5;
			
			//  8: ADV 3
			A = A / 8;
			
			// 10: BXC 3
			B = B ^ C;
			
			// 12: OUT 5
			System.out.println("OUT["+(B&0x7)+"]");
			
			// 14: JNZ 0
			if (A==0) {
				break;
			}
		}
	}
	
	*/

	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day17/feri/input-example.txt");
//		mainPart1("exchange/day17/feri/input-example-2.txt");
		mainPart1("exchange/day17/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day17/feri/input-example-p2.txt");
		mainPart2("exchange/day17/feri/input.txt");
		System.out.println("---------------");
	}

	
}

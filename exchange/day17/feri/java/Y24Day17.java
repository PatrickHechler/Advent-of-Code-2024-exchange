import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

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
		int A;
		int B;
		int C;
		int[] PROG;
		int ip;
		int ticks;
		StringBuilder OUT;
		public CPU(int a, int b, int c, int[] prog) {
			this.A = a;
			this.B = b;
			this.C = c;
			this.PROG = prog;
			this.ip = 0;
			this.ticks = 0;
			this.OUT = new StringBuilder();
		}
		public boolean exec() {
			if ((ip<0) || (ip>=PROG.length)) {
				return false;
			}
			ticks++;
			int code = PROG[ip];
			int arg = PROG[ip+1];
			ip += 2;
			OPCODE opc = OPCODE.values()[code];
			System.out.println("  EXEC "+opc+" "+arg);
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
				out(combo(arg));
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
		private void out(int combo) {
			System.out.println("OUT["+combo+"]");
			OUT.append(combo+",");
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
				result = A;
				break;
			case 5:
				result = B;
				break;
			case 6:
				result = C;
				break;
			case 7:
				throw new RuntimeException("reserver combo value 7");
			default:
				throw new RuntimeException("unknow combo value "+result);
			}
			return result & 0x07;
		}
		@Override
		public String toString() {
			return "CPU["+ip+"|"+Integer.toString(A, 16)+","+Integer.toString(B, 16)+","+Integer.toString(C, 16)+"|"+A+","+B+","+C+"|"+"]";
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
				while(cpu.exec()) {
					System.out.println(cpu.toString());
				}
				System.out.println(cpu.getOutput());
				if (scanner.hasNext()) {
					scanner.nextLine();
				}
			}
		}
	}



	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day17/feri/input-example.txt");
//		mainPart1("exchange/day17/feri/input-example-2.txt");
		mainPart1("exchange/day17/feri/input.txt");     // not 6,3,3,3,4,0,0,0,4
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day17/feri/input-example-2.txt");
		mainPart2("exchange/day17/feri/input.txt");
		System.out.println("---------------");
	}

	
}

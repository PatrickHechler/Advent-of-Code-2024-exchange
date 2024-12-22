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
 * see: https://adventofcode.com/2024/day/21
 *
 */
public class Y24Day21_BAK2 {
	
	static String[] NUMERIC_KEYPAD_KEYS = {
			"789",
			"456",
			"123",
			" 0A"
	};

	static String[] DIRECTIONAL_KEYPAD_KEYS = {
			" ^A",
			"<v>"
	};
	
	record Pos(int x, int y) {
		@Override
		public final String toString() {
			return "("+x+","+y+")";
		}
	}
	
	public static class KeyPad {
		Map<Character, Pos> keyPositions;
		Set<Pos> forbiddenPositions;
		Pos startPos;
		Pos currentPos;
		int maxY;
		public KeyPad(String[] keys) {
			keyPositions = new HashMap<>();
			forbiddenPositions = new HashSet<>();
			for (int y=0; y<keys.length; y++) {
				for (int x=0; x<keys[y].length(); x++) {
					Pos p = new Pos(x,y);
					char c = keys[y].charAt(x);
					keyPositions.put(c, p);
					if (c=='A') {
						startPos = p;
					}
					if (c==' ') {
						forbiddenPositions.add(p);
					}
				}
			}
			currentPos = startPos;
		}
		public List<String> type(String code) {
			List<String> result = new ArrayList<>();
			result.add("");
			for (char c:code.toCharArray()) {
				List<String> alternatives = navigateTo(c);
				List<String> newResult = new ArrayList<>();
				for (String oldCode:result) {
					for (String alternative:alternatives) {
						newResult.add(oldCode+alternative);
					}
				}
				result = newResult;
			}
			return result;
		}
		
		public List<String> navigateTo(char key) {
			List<String> result = new ArrayList<>();
			Pos targetPos = keyPositions.get(key);
			int x = currentPos.x;
			int y = currentPos.y;
			int tx = targetPos.x;
			int ty = targetPos.y;
			int dx = targetPos.x-x;
			int dy = targetPos.y-y;
			// two possible ways dx then dy, or dy then dx. Do not move over forbidden positions.
			char mx = (dx>0) ? '>' : '<';
			char my = (dy>0) ? 'v' : '^';
			int sx = (int)Math.signum(dx);
			int sy = (int)Math.signum(dy);
			StringBuilder way = new StringBuilder();
			boolean valid = true;
			for (int nx=x; nx!=tx; nx += sx) {
				way.append(mx);
				if (forbiddenPositions.contains(new Pos(nx,y))) {
					valid = false;
					break;
				}
			}
			for (int ny=y; ny!=ty; ny += sy) {
				way.append(my);
				if (forbiddenPositions.contains(new Pos(tx,ny))) {
					valid = false;
					break;
				}
			}
			if (valid) {
				way.append("A");
				result.add(way.toString());
			}
			valid = true;
			way.setLength(0);
			for (int ny=y; ny!=ty; ny += sy) {
				way.append(my);
				if (forbiddenPositions.contains(new Pos(x,ny))) {
					valid = false;
					break;
				}
			}
			for (int nx=x; nx!=tx; nx += sx) {
				way.append(mx);
				if (forbiddenPositions.contains(new Pos(nx,ty))) {
					valid = false;
					break;
				}
			}
			if (valid) {
				way.append("A");
				result.add(way.toString());
			}
			currentPos = targetPos;
			return result;
		}
	}

	public static class World {
		List<String> codes;
		KeyPad humanKeyPad;
		KeyPad rob3KeyPad;
		KeyPad rob2KeyPad;
		KeyPad rob1KeyPad;
		
		public World() {
			codes = new ArrayList<>();
			humanKeyPad = new KeyPad(DIRECTIONAL_KEYPAD_KEYS);
			rob3KeyPad = new KeyPad(DIRECTIONAL_KEYPAD_KEYS);
			rob2KeyPad = new KeyPad(DIRECTIONAL_KEYPAD_KEYS);
			rob1KeyPad = new KeyPad(NUMERIC_KEYPAD_KEYS);
		}
		public void addCode(String code) {
			codes.add(code);
		}
		public int solve() {
			int result = 0;
			for (String code:codes) {
				System.out.println("--- "+code+" ---");
				List<String>  dirCode = typeRob3Codes(code); 
				System.out.println(code+": "+dirCode+" "+dirCode.get(0).length()+" ");
				int codeNum = Integer.parseInt(code.replace("A", ""));
				result = result + dirCode.get(0).length()*codeNum;
			}
			return result;
		}

		public List<String> typeRob3Codes(String code) {
			List<String>  rob2code = typeRob2Codes(code);
			System.out.println("ROB2: "+rob2code);
			List<String> result = rob2KeyPad.type(rob2code.get(0));
			return result;
		}
		public List<String>  typeRob2Codes(String code) {
			String rob1code = typeRob1Codes(code);
			System.out.println("ROB1: "+rob1code);
			List<String>  result = rob2KeyPad.type(rob1code);
			return result;
		}
		public String typeRob1Codes(String code) {
			StringBuilder result = new StringBuilder();
			result.append(rob1KeyPad.type(code));
			return result.toString();
		}
		
		@Override
		public String toString() {
			return codes.toString();
		}
	}
	
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {

		World world = new World();
		try (Scanner scanner = new Scanner(new File(inputfile))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isBlank()) {
					break;
				}
				world.addCode(line);
			}
		}
		System.out.println(world.solve());
	}

	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day21/feri/input-example.txt");
//		mainPart1("exchange/day21/feri/input-example-1.txt");
		mainPart1("exchange/day21/feri/input-example-2.txt");
//		mainPart1("exchange/day21/feri/input.txt");     // < 244490   // < 241370
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day21/feri/input-example.txt");
//		mainPart2("exchange/day21/feri/input.txt");    
		System.out.println("---------------");
	}

/*
 
--- 2 ---
ROB1: <^A
ROB2: <<vA>^A>A
2: <<vAA>A>^AvA<^A>AvA^A 21 

--- 2 ---
ROB1: ^<A
ROB2: <Av<A^>>A
2: v<<A^>>Av<A<A^>>A<Av>AA^A 25 


*/	
	
}

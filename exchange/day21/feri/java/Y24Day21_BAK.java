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
public class Y24Day21_BAK {
	
	
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
		public String type(String code) {
			StringBuilder result = new StringBuilder();
			for (char c:code.toCharArray()) {
//				result.append(" ["+c+"]");
				result.append(navigateTo(c));
				result.append("A");
			}
			return result.toString();
		}
		public String navigateTo(char key) {
			Pos targetPos = keyPositions.get(key);
			int x = currentPos.x;
			int y = currentPos.y;
			int tx = targetPos.x;
			int ty = targetPos.y;
			int dx = targetPos.x-x;
			int dy = targetPos.y-y;
			// two possible was dx then dy, or dy then dx. Do not move over forbidden positions.
			char mx = (dx>0) ? '>' : '<';
			char my = (dy>0) ? 'v' : '^';
			int sx = (int)Math.signum(dx);
			int sy = (int)Math.signum(dy);
			StringBuilder result = new StringBuilder();
			boolean valid = true;
			for (int nx=x; nx!=tx; nx += sx) {
				result.append(mx);
				if (forbiddenPositions.contains(new Pos(nx,y))) {
					valid = false;
					break;
				}
			}
			for (int ny=y; ny!=ty; ny += sy) {
				result.append(my);
				if (forbiddenPositions.contains(new Pos(tx,ny))) {
					valid = false;
					break;
				}
			}
			if (valid) {
				currentPos = targetPos;
				return result.toString();
			}
			result.setLength(0);
			valid = true;
			for (int ny=y; ny!=ty; ny += sy) {
				result.append(my);
				if (forbiddenPositions.contains(new Pos(x,ny))) {
					valid = false;
					break;
				}
			}
			for (int nx=x; nx!=tx; nx += sx) {
				result.append(mx);
				if (forbiddenPositions.contains(new Pos(nx,ty))) {
					valid = false;
					break;
				}
			}
			if (valid) {
				currentPos = targetPos;
				return result.toString();
			}
			return null;
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
		public String typeRob3Codes() {
			String rob2code = typeRob2Codes();
			System.out.println("ROB2CODE: "+rob2code);
			String result = rob2KeyPad.type(rob2code);
			return result;
		}
		public String typeRob2Codes() {
			String rob1code = typeRob1Codes();
			System.out.println("ROB2CODE: "+rob1code);
			String result = rob2KeyPad.type(rob1code);
			return result;
		}
		public String typeRob1Codes() {
			StringBuilder result = new StringBuilder();
			String seperator = "";
			for (String code:codes) {
				result.append(rob1KeyPad.type(seperator));
				seperator="A";
				result.append(rob1KeyPad.type(code));
			}
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
		System.out.println(world.typeRob3Codes());
	}

	public static void mainPart2(String inputfile) throws FileNotFoundException {
	}


	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
		mainPart1("exchange/day21/feri/input-example-1.txt");
//		mainPart1("exchange/day21/feri/input-example.txt");
//		mainPart1("exchange/day21/feri/input.txt");
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exchange/day21/feri/input-example.txt");
//		mainPart2("exchange/day21/feri/input.txt");    
		System.out.println("---------------");
	}

	
}

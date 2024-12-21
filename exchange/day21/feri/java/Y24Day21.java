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
public class Y24Day21 {
	
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
	
	
	public static class AlternativeStringBuilder {
		private String value;
		private int len;
		List<AlternativeStringBuilder> alternativesList;
		boolean sequence;
		public AlternativeStringBuilder(String singleValue) {
			value = singleValue;
			alternativesList = null;
			sequence = false;
			len = value.length();
		}
		public AlternativeStringBuilder() {
			value = null;
			alternativesList = null;
			len = Integer.MAX_VALUE;
		}
		public boolean isValue() {
			return value != null;
		}
		public boolean isSequence() {
			return sequence;
		}
		public boolean isAlternative() {
			return !sequence;
		}
		public int length() {
			return len;
		}
		public void addMinimal(AlternativeStringBuilder alternative) {
			if (alternativesList == null) {
				alternativesList = new ArrayList<>();
				sequence = false;
				len = Integer.MAX_VALUE;
			}
			if (isSequence()) {
				throw new RuntimeException("AlternativeStringBuilder is a sequence");
			}
			if (alternative.length() == length()) {
				alternativesList.add(alternative);
			}
			else if (alternative.length() < length()) {
				alternativesList.clear();
				alternativesList.add(alternative);
				len = alternative.length();
			}
		}
		public void append(AlternativeStringBuilder alternatives) {
			if (alternativesList == null) {
				alternativesList = new ArrayList<>();
				sequence = true;
				len = 0;
			}
			if (!isSequence()) {
				throw new RuntimeException("AlternativeStringBuilder is not a sequence");
			}
			alternativesList.add(alternatives);
			len += alternatives.length();
		}
		@Override
		public String toString() {
			if (isValue()) {
				return value;
			}
			if (isAlternative()) {
				return "ALT["+alternativesList+"|"+len+"]";
			}
			if (isSequence()) {
				return "SEQ["+alternativesList+"|"+len+"]";
			}
			return "UNDEF";
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
		public AlternativeStringBuilder type(AlternativeStringBuilder codeAlternatives) {
			AlternativeStringBuilder result = new AlternativeStringBuilder();
			if (codeAlternatives.isValue()) {
				return type(codeAlternatives.value);
			}
			if (codeAlternatives.isAlternative()) {
				Pos startPos = currentPos;
				for (AlternativeStringBuilder codeSegment:codeAlternatives.alternativesList) {
					currentPos = startPos;
					AlternativeStringBuilder typedCodeSegment = type(codeSegment); 
					result.addMinimal(typedCodeSegment);
				}
			}
			if (codeAlternatives.isSequence()) {
				for (AlternativeStringBuilder codeSegment:codeAlternatives.alternativesList) {
					AlternativeStringBuilder typedCodeSegment = type(codeSegment); 
					result.append(typedCodeSegment);
				}
			}
			return result;
		}
		public AlternativeStringBuilder type(String code) {
			AlternativeStringBuilder result = new AlternativeStringBuilder();
			for (char c:code.toCharArray()) {
				result.append(navigateTo(c));
			}
			return result;
		}
		public AlternativeStringBuilder navigateTo(char key) {
			AlternativeStringBuilder result = null;
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
				result = new AlternativeStringBuilder(way.toString());
			}
			if ((dx==0) || (dy==0)) {
				currentPos = targetPos;
				return result;
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
				AlternativeStringBuilder result2 = new AlternativeStringBuilder(way.toString());
				if (result == null) { 
					result = result2;
				}
				else {
					AlternativeStringBuilder result1 = result;
					result = new AlternativeStringBuilder();
					result.addMinimal(result1);
					result.addMinimal(result2);
				}
			}
			currentPos = targetPos;
			return result;
		}
	}

	public static class World {
		List<String> codes;
		KeyPad humanKeyPad;
		KeyPad[] robKeyPads;
		int numRobs;
		
		public World(int numRobs) {
			codes = new ArrayList<>();
			humanKeyPad = new KeyPad(DIRECTIONAL_KEYPAD_KEYS);
			this.numRobs = numRobs;
			robKeyPads = new KeyPad[numRobs];
			for (int i=1; i<numRobs; i++) {
				robKeyPads[i] = new KeyPad(DIRECTIONAL_KEYPAD_KEYS);
			}
			robKeyPads[0] = new KeyPad(NUMERIC_KEYPAD_KEYS);
		}
		public void addCode(String code) {
			codes.add(code);
		}
		public int solve() {
			int result = 0;
			for (String code:codes) {
				System.out.println("--- "+code+" ---");
				AlternativeStringBuilder dirCodes = typeRobCodes(numRobs-1, new AlternativeStringBuilder(code));
				System.out.println(dirCodes);
				int len = dirCodes.length();
				System.out.println(code+": "+len+" ");
				int codeNum = Integer.parseInt(code.replace("A", ""));
				result = result + len*codeNum;
			}
			return result;
		}

		public AlternativeStringBuilder typeRobCodes(int robNr, AlternativeStringBuilder codeAlternatives) {
			if (robNr == 0) {
				return robKeyPads[0].type(codeAlternatives);
			}
			else {
				AlternativeStringBuilder childRobCodes = typeRobCodes(robNr-1, codeAlternatives);
//				System.out.println(childRobCodes);
				return robKeyPads[robNr].type(childRobCodes);
			}
		}
		@Override
		public String toString() {
			return codes.toString();
		}
	}
	
	
	public static void mainPart1(String inputfile) throws FileNotFoundException {

		World world = new World(3);
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

		World world = new World(25);
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
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exchange/day21/feri/input-example.txt");
//		mainPart1("exchange/day21/feri/input-example-1.txt");
//		mainPart1("exchange/day21/feri/input-example-2.txt");
//		mainPart1("exchange/day21/feri/input.txt");     // == 238078
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day21/feri/input-example.txt");
//		mainPart2("exchange/day21/feri/input-example-1.txt");
		mainPart2("exchange/day21/feri/input-example-2.txt");
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

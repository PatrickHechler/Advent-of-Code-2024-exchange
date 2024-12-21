import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
		List<Set<String>> alternativesList;
		public AlternativeStringBuilder() {
			alternativesList = new ArrayList<>();
		}
		public void appendMinimal(Collection<String> alternatives) {
			int minLength = Integer.MAX_VALUE;
			Set<String> minAlternatives = new LinkedHashSet<>();
			for (String alternative:alternatives) {
				if (alternative.length()==minLength) {
					minAlternatives.add(alternative);
				}
				else if (alternative.length()<minLength) {
					minAlternatives.clear();
					minAlternatives.add(alternative);
					minLength = alternative.length();
				}
			}
			alternativesList.add(minAlternatives);
		}
		public void appendMinimal(AlternativeStringBuilder alternatives) {
			appendMinimal(alternatives.getAlternatives());
		}
		public void append(String alternative) {
			alternativesList.add(new HashSet<>(Arrays.asList(alternative)));
		}
		public List<String> getAlternatives() {
			List<String> result = new ArrayList<>();
			result.add("");
			for (Set<String> alternatives:alternativesList) {
				List<String> nextResult = new ArrayList<>();
				for (String previousResultString:result) {
					for (String alternative:alternatives) {
						nextResult.add(previousResultString+alternative);
					}
				}
				result = nextResult;
			}
			return result;
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
		public AlternativeStringBuilder type(String code) {
			AlternativeStringBuilder asb = new AlternativeStringBuilder();
			for (char c:code.toCharArray()) {
//				result.append(" ["+c+"]");
				asb.appendMinimal(navigateTo(c));
				asb.append("A");
			}
			return asb;
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
				result.add(way.toString());
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
				List<String> dirCodes = typeRobCodes(numRobs-1, code);
				String dirCode = dirCodes.get(0);
				System.out.println(code+": "+dirCode+" "+dirCode.length()+" ");
				int codeNum = Integer.parseInt(code.replace("A", ""));
				result = result + dirCode.length()*codeNum;
			}
			return result;
		}

		public List<String> typeRobCodes(int robNr, String code) {
			if (robNr == 0) {
				return robKeyPads[0].type(code).getAlternatives();
			}
			else {
				Set<String> alternatives = new LinkedHashSet<>();
				List<String> childRobCodes = typeRobCodes(robNr-1, code);
				for (String childRobCode:childRobCodes) {
					alternatives.addAll(robKeyPads[robNr].type(childRobCode).getAlternatives());
				}
				AlternativeStringBuilder result = new AlternativeStringBuilder();
				result.appendMinimal(alternatives);
				return result.getAlternatives();
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
		mainPart1("exchange/day21/feri/input.txt");     // < 244490   // < 241370
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
//		mainPart2("exchange/day21/feri/input-example.txt");
		mainPart2("exchange/day21/feri/input.txt");    
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

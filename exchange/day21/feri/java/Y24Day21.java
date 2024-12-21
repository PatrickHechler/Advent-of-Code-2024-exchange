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
		public void addALL(AlternativeStringBuilder alternative) {
			if (alternativesList == null) {
				alternativesList = new ArrayList<>();
				sequence = false;
				len = Integer.MAX_VALUE;
			}
			if (isSequence()) {
				throw new RuntimeException("AlternativeStringBuilder is a sequence");
			}
			alternativesList.add(alternative);
			len = Math.min(len, alternative.length());
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
		public String showAlternatives() {
			if (isValue()) {
				return value;
			}
			if (isAlternative()) {
				return "("+join(alternativesList, "|")+")";
			}
			if (isSequence()) {
				return join(alternativesList, "");
			}
			return "UNDEF";
		}
		public void showAlternativeList() {
			for (String alt:getAlternatives()) {
				System.out.println(alt);
			}
		}
		public List<String> getAlternatives() {
			List<String> result = new ArrayList<>();
			if (isValue()) {
				result.add(value);
				return result;
			}
			if (isAlternative()) {
				for (AlternativeStringBuilder alternative:alternativesList) {
					result.addAll(alternative.getAlternatives());
				}
				return result;
			}
			if (isSequence()) {
				result.add("");
				for (AlternativeStringBuilder alternative:alternativesList) {
					List<String> nextAlts = alternative.getAlternatives();
					List<String> newResult = new ArrayList<>(); 
					for (String nextAlt:nextAlts) {
						for (String oldResult:result) {
							newResult.add(oldResult+nextAlt);
						}
					}
					result = newResult;
				}
				return result;
			}
			throw new RuntimeException("?");
		}

		private String join(List<AlternativeStringBuilder> list, String seperator) {
			StringBuilder result = new StringBuilder();
			for (AlternativeStringBuilder asb:list) {
				if (!result.isEmpty()) {
					result.append(seperator);
				}
				result.append(asb.showAlternatives());
			}
			return result.toString();
		}

	}
	
	public static class KeyPad {
		Map<String, Map<String, Long>> cache; 
		Map<Character, Pos> keyPositions;
		Set<Pos> forbiddenPositions;
		Pos startPos;
		Pos currentPos;
		int maxY;
		public KeyPad(String[] keys) {
			cache = new HashMap<>();
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
		public Map<String, Long> splitAGroup(String commands) {
			Map<String, Long> result = new HashMap<>();
			String text = commands;
			while (text.indexOf('A') != -1) {
				int pos = text.indexOf('A');
				String aGroup= text.substring(0,pos+1);
				if (result.containsKey(aGroup)) {
					result.put(aGroup, result.get(aGroup)+1);
				}
				else {
					result.put(aGroup, 1L);
				}
				text = text.substring(pos+1);
			}
			return result;
		}
		public Map<String, Long> translate(Map<String, Long> aGroups) {
			Map<String, Long> result = new HashMap<>();
			for (String aGroup:aGroups.keySet()) {
				long cnt = aGroups.get(aGroup);
				Map<String, Long> translatedAGroups = translateAGroup(aGroup);
				for (String translatedAGroup:translatedAGroups.keySet()) {
					Long cntInTranslate = translatedAGroups.get(translatedAGroup);
					if (result.containsKey(translatedAGroup)) {
						result.put(translatedAGroup, result.get(translatedAGroup)+cnt*cntInTranslate);
					}
					else {
						result.put(translatedAGroup, cnt*cntInTranslate);
					}
				}
			}
			return result;
		}
		public Map<String, Long> translateAGroup(String aGroup) {
			if (cache.containsKey(aGroup)) {
				return cache.get(aGroup);
			}
			AlternativeStringBuilder asb = type(aGroup);
			String resultString = asb.getAlternatives().get(0);
			Map<String, Long> result = splitAGroup(resultString);
			cache.put(aGroup, result);
			return result;
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
					result.addALL(typedCodeSegment);
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
				result.append(filter(navigateTo(c)));
			}
			return result;
		}
		private AlternativeStringBuilder filter(AlternativeStringBuilder alternatives) {
			if (alternatives.isValue()) {
				return alternatives;
			}
			AlternativeStringBuilder a1 = alternatives.alternativesList.get(0);
			AlternativeStringBuilder a2 = alternatives.alternativesList.get(1);
			String v1 = a1.value;
			String v2 = a2.value;
			
			if (v1.equals(v2)) {
				return a1;
			}
			
			if (v1.startsWith("<") && (v2.startsWith("^"))) {
				return a1; 
			}
			if (v1.startsWith("^") && (v2.startsWith("<"))) {
				return a2; 
			}
			
			if (v1.startsWith("v") && (v2.startsWith(">"))) {
				return a1; 
			}
			if (v1.startsWith(">") && (v2.startsWith("v"))) {
				return a2; 
			}

			
			if (v1.startsWith("<") && (v2.startsWith("v"))) {
				return a1; 
			}
			if (v1.startsWith("v") && (v2.startsWith("<"))) {
				return a2; 
			}
			
			if (v1.startsWith(">") && (v2.startsWith("^"))) {
				return a2; 
			}
			if (v1.startsWith("^") && (v2.startsWith(">"))) {
				return a1; 
			}
			
			throw new RuntimeException("invalid filter");
//			return a1;
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
					result.addALL(result1);
					result.addALL(result2);
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
		public long solve() {
			long result = 0;
			for (String code:codes) {
				System.out.println("--- "+code+" ---");
				String initial = robKeyPads[0].type(code).getAlternatives().get(0);
				Map<String, Long> aGroups = robKeyPads[0].splitAGroup(initial);
				for (int i=1; i<numRobs; i++) {
					System.out.println("--- NEW "+(i-1)+" ---");
					System.out.println(aGroups);
					aGroups = humanKeyPad.translate(aGroups);
				}
				long len = 0L;
				for (String aGroup:aGroups.keySet()) {
					Long cnt = aGroups.get(aGroup);
					len += cnt*aGroup.length();
				}
				System.out.println(aGroups+" \nlen="+len);
				Long codeNum = Long.parseLong(code.replace("A", ""));
				result = result + len*codeNum;
			}
			return result;
		}

		public int solveOLD() {
			int result = 0;
			for (String code:codes) {
				System.out.println("--- "+code+" ---");
				AlternativeStringBuilder dirCodes = typeRobCodes(numRobs-1, new AlternativeStringBuilder(code));
				System.out.println(dirCodes.showAlternatives());
				System.out.println("----------------------------------");
				dirCodes.showAlternativeList();
				System.out.println("-------------- OLD --------------------");
				System.out.println(humanKeyPad.splitAGroup(dirCodes.getAlternatives().get(0)));
				System.out.println("--------------  --------------------");
				int len = dirCodes.length();
				System.out.println(code+": "+len+" ");
				int codeNum = Integer.parseInt(code.replace("A", ""));
				result = result + len*codeNum;
			}
			return result;
		}

		private Map<String, Long> translateRobCodes(int robNr, String code) {
			if (robNr == 0) {
				String initial = robKeyPads[0].type(code).getAlternatives().get(0);
				return robKeyPads[0].splitAGroup(initial);
			}
			else {
				Map<String, Long> childAGroups = translateRobCodes(robNr-1, code);
				Map<String, Long> result = humanKeyPad.translate(childAGroups);
				return result;
			}
		}
		public AlternativeStringBuilder typeRobCodes(int robNr, AlternativeStringBuilder codeAlternatives) {
			if (robNr == 0) {
				return robKeyPads[0].type(codeAlternatives);
			}
			else {
				AlternativeStringBuilder childRobCodes = typeRobCodes(robNr-1, codeAlternatives);
				System.out.println("--------- ROB "+robNr+" ------------");
				System.out.println(childRobCodes.showAlternatives());
				System.out.println("-------------- OLD --------------------");
				System.out.println(humanKeyPad.splitAGroup(childRobCodes.getAlternatives().get(0)));
				System.out.println("----------------------------------");
				childRobCodes.showAlternativeList();
				System.out.println("----------------------------------");
				
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

	public static void mainPart2(String inputfile, int depth) throws FileNotFoundException {

		World world = new World(depth);
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
//		mainPart2("exchange/day21/feri/input-example.txt", 3);
//		mainPart2("exchange/day21/feri/input-example-1.txt", 3);
//		mainPart2("exchange/day21/feri/input-example-2.txt", 3);
//		mainPart2("exchange/day21/feri/input.txt", 25);      // > 134180557577040  < 335879439503508
		mainPart2("exchange/day21/feri/input.txt", 26);      // > 134180557577040  < 335879439503508
		//                                                                           475288764580252
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

-------------- OLD --------------------
{A=3, >^A=1, >A=2, <A=1, <vA=1, vA=1, v<<A=1, >>^A=1, <^A=1}

--- NEW 1 ---
{A=2, >^A=1, >A=2, <A=1, <vA=1, vA=1, v<<A=1, >>^A=1, <^A=1}



--- NEW 0 ---
{^A=1, <A=1, vvvA=1, >^^A=1}
-------------- OLD --------------------
{^A=1, <A=1, vvvA=1, >^^A=1}


--- NEW 1 ---
{A=2, >^A=1, >A=2, <A=1, <vA=1, vA=1, v<<A=1, >>^A=1, <^A=1}
-------------- OLD --------------------
{A=3, >^A=1, >A=2, <A=1, <vA=1, vA=1, v<<A=1, >>^A=1, <^A=1}


OLD
<A       ^A     >^^A     vvvA
v<<A>>^A <A>A   vA<^AA>A <vAAA>^A



*/	
	
}

"""Advent of Code 2024, Tag 17, Teil 1
Lösung von Matthias Baake
"""

from pathlib import Path

DIRECTORY = Path("2024")

PATH_1 = DIRECTORY / "data_17-example1.txt"
PATH_MB = DIRECTORY / "data_17-matthias_baake.txt"

PATH = PATH_MB


def read_file(path: Path) -> tuple[int, int, int, list[int]]:
    """Datei einlesen

    Args:
        path: Pfad der einzulesenden Datei

    Returns:
        [0]: Register A
        [1]: Register B
        [2]: Register C
        [3]: Programm als Liste von int
    """
    with open(path, encoding="utf-8") as file:
        reg_a = int(next(file).split(":")[1])
        reg_b = int(next(file).split(":")[1])
        reg_c = int(next(file).split(":")[1])
        next(file)
        program = next(file).split(":")[1]
        program_l = [int(x) for x in program.split(",")]

    return reg_a, reg_b, reg_c, program_l


def get_combo(oper: int, reg_a: int, reg_b: int, reg_c: int) -> int:
    """Combo-Parameter auswerten

    Args:
        oper: Operand
        reg_a: Inhalt von Register A
        reg_b: Inhalt von Register B
        reg_c: Inhalt von Register C

    Raises:
        ValueError: Bei ungültigem Operand

    Returns:
        Wert
    """
    if oper in (0, 1, 2, 3):
        return oper
    if oper == 4:
        return reg_a
    if oper == 5:
        return reg_b
    if oper == 6:
        return reg_c
    raise ValueError(f"Invalid combo operand {oper}")


def run_program(reg_a: int, reg_b: int, reg_c: int, program_l: list[int]) -> list[int]:
    """Programm ausführen

    Args:
        reg_a: Inhalt von Register A
        reg_b: Inhalt von Register B
        reg_c: Inhalt von Register C
        program_l: Programm als Liste von int

    Raises:
        ValueError: Bei ungültiger Anweisung

    Returns:
        Programm als Liste von int
    """
    out_l: list[int] = []
    pc = 0

    while pc < len(program_l):
        advance_pc = True
        opcode = program_l[pc]
        oper = program_l[pc + 1]
        if opcode == 0:  # adv; reg_a <- reg_a // 2^(combo oper)
            reg_a = reg_a // (2 ** get_combo(oper, reg_a, reg_b, reg_c))
        elif opcode == 1:  # bxl; reg_b <- reg_b XOR oper
            reg_b = reg_b ^ oper
        elif opcode == 2:  # bst; reg_b <- 7 & (combo oper)
            reg_b = 7 & get_combo(oper, reg_a, reg_b, reg_c)
        elif opcode == 3:  # jnz; pc <- oper if reg_a == 0 else pass
            if reg_a != 0:
                advance_pc = False
                pc = oper
        elif opcode == 4:  # bxc; reg_b <- reg_b ^ reg_c (oper ignored)
            reg_b = reg_b ^ reg_c
        elif opcode == 5:  # out; output combo oper & 7
            out_l.append(get_combo(oper, reg_a, reg_b, reg_c) & 7)
        elif opcode == 6:  # bdv; reg_b <- reg_a // 2^(combo oper)
            reg_b = reg_a // (2 ** get_combo(oper, reg_a, reg_b, reg_c))
        elif opcode == 7:  # cdv; reg_c <- reg_a // 2^(combo oper)
            reg_c = reg_a // (2 ** get_combo(oper, reg_a, reg_b, reg_c))
        else:
            raise ValueError(f"Unknown instruction {opcode} at {pc}")
        if advance_pc:
            pc += 2

    return out_l


def main() -> None:
    """Datei einlesen und Teilaufgabe 1 lösen"""
    reg_a, reg_b, reg_c, program_l = read_file(PATH)

    out_l = run_program(reg_a, reg_b, reg_c, program_l)

    # Teilaufgabe 1
    print(f"Ergebnis Teilaufgabe 1 für {PATH}: {','.join(map(str,out_l))}")
    # PATH_1: 4,6,3,5,6,3,5,2,1,0
    # PATH_MB: 1,2,3,1,3,2,5,3,1


if __name__ == "__main__":
    main()

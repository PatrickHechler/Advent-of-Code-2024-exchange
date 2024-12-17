"""Advent of Code 2024, Tag 17, Teil 2
Lösung von Matthias Baake
"""

from pathlib import Path

DIRECTORY = Path("2024")

PATH_1 = DIRECTORY / "data_17-example1.txt"
PATH_2 = DIRECTORY / "data_17-example2.txt"
PATH_MB = DIRECTORY / "data_17-matthias_baake.txt"


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

    # Programm in PATH_MB
    # 2 4  bst reg_a    reg_b <- 7 & reg_a
    # 1 5  bxl 5        reg_b <- reg_b ^ 5
    # 7 5  cdv reg_b    reg_c <- reg_a // (2**reg_b)    (reg_a >> reg_b)
    # 1 6  bxl 6        reg_b <- reg_b ^ 6
    # 0 3  adv 3        reg_a <- reg_a // 8             (reg_a >> 3)
    # 4 3  bxc          reg_b <- reg_b ^ reg_c
    # 5 5  out reg_b    out reg_b & 7
    # 3 0  jnz 0        jump to 0 if reg_a != 0
    #                   end
    #
    # Bei jedem Schleifendurchlauf werden die unteren 3 Bit von reg_a
    # in reg_b geschrieben und später aus reg_a entfernt.
    # Das Programm endet, wenn reg_a == 0 ist.
    #
    # Bei jedem Schleifendurchlauf werden abhängig von den letzten drei Bit
    # von Register A und drei weiteren Bits, deren Position von den letzten drei
    # abhängig ist, Register B und C belegt.
    #
    # Inhalt von reg_a vvv   B nach bxl 5  B nach bxl 6
    # ............ccc..000   5             3  (011)
    # .............ccc.001   4             2  (010)
    # ..........ccc....010   7             1  (001)
    # ...........ccc...011   6             0  (000)
    # ................c100   1             7  (111)
    # .................101   0             6  (110)
    # ..............ccc110   3             5  (101)
    # ...............cc111   2             4  (100)


def main() -> None:
    """Lösung für Teil finden"""
    _, _, _, program_l = read_file(PATH_MB)
    a_fixed = 0
    a_current = 0
    print(construct(a_fixed, a_current, 0, program_l))
    # 105706277661082


pos_c_l = [5, 4, 7, 6, 1, 0, 3, 2]  # Verschiebung von C in A


def construct(
    a_fixed: int, a_current: int, pos: int, program_l: list[int]
) -> int | None:
    """Rekursiv den gesuchten Anfangswert von Register A finden

    Args:
        a_fixed: Positionen in reg_a, deren Werte festgelegt sind
        a_current: Wert für reg_a, nur die Bits in a_fixed sind relevant
        pos: Position in program_l
        program_l: Das Programm und gleichzeitig die geforderten Ausgabe

    Returns:
        Den gefundenen Wert für Register A oder None, wenn ausgehend von
        den Parametern kein Wert existert
    """
    if pos >= len(program_l):
        # Alle Ausabebwerte wurden erzeugt, a_current ist ein möglicher
        # Anfangswert für Reigster A
        return a_current

    a_l: list[int] = []  # Liste der hier möglichen Anfangswerte für Register A

    for reg_a in range(8):
        shift_c = pos_c_l[reg_a]

        # Prüfung 1: passt der für reg_a gewählte Wert unter Berücksichtigung
        # der Bits an a_fixed zu a_current?

        pos_a = pos * 3
        # Abweichende Bits
        a_shifted = reg_a << pos_a
        bits_different = a_shifted ^ a_current
        # ... aber nur an den unteren drei Bits prüfen und nur, soweit diese
        # fixed sind
        if (bits_different & (0b111 << pos_a)) & a_fixed:
            # Kollision
            continue

        new_a_temp = a_current | a_shifted
        tmp_a_fixed = a_fixed | (0b111 << pos_a)

        # Den sich aus reg_a und dem geforderten Ausgabewert (program_l[pos])
        # ergebenden Wert für reg_c ermitteln
        reg_b2 = reg_a ^ 0b011
        reg_c = program_l[pos] ^ reg_b2

        # Prüfung 2: Passt der so ermittelte Wert für reg_c an die aus reg_b und
        # a_shift ermittelte Stelle (ohne Kollision)?

        # Abweichende Bits
        c_shifted = reg_c << (pos_a + shift_c)
        bits_different = c_shifted ^ new_a_temp
        # ... aber nur an den drei Bits, die sich aus pos_a und shift_c ergeben
        if (bits_different & (0b111 << (pos_a + shift_c))) & tmp_a_fixed:
            # Kollision
            continue

        # Bis hier keine Kollisionen, neue Werte für a_fixed und a_current
        # bestimmen
        new_a_current = c_shifted | (new_a_temp & (~(0b111 << (pos_a + shift_c))))
        new_a_fixed = a_fixed | (0b111 << pos_a) | (0b111 << (pos_a + shift_c))

        # Nächsten Wert prüfen
        result = construct(new_a_fixed, new_a_current, pos + 1, program_l)
        if result is not None:
            a_l.append(result)

    if not a_l:
        return None
    return min(a_l)


if __name__ == "__main__":
    main()

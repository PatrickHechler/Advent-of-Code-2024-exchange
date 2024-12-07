"""Advent of Code 2024, Tag 7
Lösung von Matthias Baake
"""

from typing import Callable, Generator, Sequence
from pathlib import Path

DIRECTORY = Path("2024")
PATH1 = DIRECTORY / Path("data_07-example.txt")
PATH2 = DIRECTORY / Path("data_07-matthias_baake.txt")

PATH = PATH2


def calc_val_1(num_seq: Sequence[int]) -> Generator[int, None, None]:
    """Werteliste für Teilaufgabe 1 erzeugen

    Args:
        num_seq: Folge der Zahlen, zwischen denen die Operatoren einzusetzen sind

    Yields:
        Die sich durch Einfügen von + und * ergebenden Werte
    """
    val_last = num_seq[-1]

    if len(num_seq) == 1:
        yield val_last
    else:
        for val_left in calc_val_1(num_seq[:-1]):
            yield val_left + val_last  # operator +
            yield val_left * val_last  # operator *


def calc_val_2(num_seq: Sequence[int]) -> Generator[int, None, None]:
    """Werteliste für Teilaufgabe 2 erzeugen

    Args:
        num_seq: Folge der Zahlen, zwischen denen die Operatoren einzusetzen sind

    Yields:
        Die sich durch Einfügen von +, * und || ergebenden Werte
    """
    val_last = num_seq[-1]

    if len(num_seq) == 1:
        yield val_last
    else:
        for val_left in calc_val_2(num_seq[:-1]):
            yield val_left + val_last  # operator +
            yield val_left * val_last  # operator *
            yield int(str(val_left) + str(val_last))  # operator ||


def check_val(
    gen_fct: Callable[[Sequence[int]], Generator[int, None, None]],
    testval: int,
    num_seq: Sequence[int],
) -> bool:
    """Prüfen, ob testval mit num_l erreicht werden kann

    Args:
        gen_fct: Generator-Funktion, die alle möglichen Werte erzeugt
        testval: Das gewünschte Ergebnis
        num_seq: Folge der Zahlen, zwischen denen die Operatoren einzusetzen sind

    Returns:
        True, wenn testval auf mindestens eine Art erzeugt werden kann,
        sonst False
    """
    for result in gen_fct(num_seq):
        if result == testval:
            return True
    return False


def main() -> None:
    """Datei einlesen und beide Teilaufgaben lösen"""
    sum_1 = 0
    sum_2 = 0
    with open(PATH, encoding="utf-8") as file:
        for line in file:
            testval_str, num_str = line.split(":")
            testval = int(testval_str)
            num_l = list(map(int, num_str.split()))
            if check_val(calc_val_1, testval, num_l):
                sum_1 += testval
            if check_val(calc_val_2, testval, num_l):
                sum_2 += testval

    print(f"Ergebnis Teilaufgabe 1 für {PATH.name}: {sum_1}")
    # fname1: 3749
    # fname2: 1611660863222

    print(f"Ergebnis Teilaufgabe 2 für {PATH.name}: {sum_2}")
    # fname1: 11387
    # fname2: 945341732469724


if __name__ == "__main__":
    main()

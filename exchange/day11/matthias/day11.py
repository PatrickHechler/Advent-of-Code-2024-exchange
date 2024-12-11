"""Advent of Code 2024, Tag 11
Lösung von Matthias Baake
"""

from collections import Counter, defaultdict
from pathlib import Path

DIRECTORY = Path("2024")
PATH1 = DIRECTORY / "data_11-example.txt"
PATH_MB = DIRECTORY / "data_11-matthias_baake.txt"

PATH = PATH_MB


def read_file(
    path: Path,
) -> list[int]:
    """Datei einlesen

    Args:
        path: Dateiname als Path

    Returns:
        Die eingelesenen "Steine" als Liste von int
    """
    with open(path, encoding="utf-8") as file:
        stone_l = list(map(int, next(file).split()))

    return stone_l


def blink(stone_l: list[int], num_blink: int) -> int:
    num_stones_d: dict[int, int] = Counter(stone_l)
    for _ in range(1, num_blink + 1):
        next_stones_d: dict[int, int] = defaultdict(lambda: 0)
        for stone, num in num_stones_d.items():
            if stone == 0:
                next_stones_d[1] += num
            else:
                stone_str = str(stone)
                if len(stone_str) % 2 == 0:
                    split_pos = int(len(stone_str) / 2)
                    stone1 = int(stone_str[:split_pos])
                    stone2 = int(stone_str[split_pos:])
                    next_stones_d[stone1] += num
                    next_stones_d[stone2] += num
                else:
                    next_stones_d[stone * 2024] += num
        num_stones_d = next_stones_d

    return sum(num_stones_d.values())


def main() -> None:
    """Datei einlesen und beide Teilaufgaben lösen"""
    stone_l = read_file(PATH)

    num_stones = blink(stone_l, 25)
    print(f"Ergebnis Teilaufgabe 1 für {PATH}: {num_stones}")
    # PATH1: 55312
    # PATH_MB: 235850

    num_stones = blink(stone_l, 75)
    print(f"Ergebnis Teilaufgabe 2 für {PATH}: {num_stones}")
    # PATH_MB: 279903140844645


if __name__ == "__main__":
    main()

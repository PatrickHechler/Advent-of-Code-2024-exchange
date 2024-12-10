"""Advent of Code 2024, Tag 10
Lösung von Matthias Baake
"""

from pathlib import Path

DIRECTORY = Path("2024")
PATH1 = DIRECTORY / "data_10-example1.txt"  # "larger example"
PATH2 = DIRECTORY / "data_10-example2.txt"  # part 2: 1 trailhead, rating 13
PATH3 = DIRECTORY / "data_10-example3.txt"  # part 2: 1 trailhead, rating 227
PATH_MB = DIRECTORY / "data_10-matthias_baake.txt"

PATH = PATH_MB


def read_file(
    path: Path,
) -> list[str]:
    """Datei einlesen

    Args:
        path: Dateiname als Path

    Returns:
        Die eingelesene Karte als Liste von str
    """
    with open(path, encoding="utf-8") as file:
        map_l = list(map(lambda line: line.rstrip(), file))

    return map_l


def part1(map_l: list[str]) -> int:
    """Teilaufgabe 1 lösen

    Args:
        map_l: Die eingelesene Karte

    Returns:
        Gesamt-Score
    """
    width = len(map_l[0])
    height = len(map_l)

    # Ziel-Karte vorbereiten, an allen 9 die eigene Position eintragen
    targets_d: dict[tuple[int, int], set[tuple[int, int]]] = {}
    for y in range(height):
        for x in range(width):
            if map_l[y][x] == "9":
                targets_d[(x, y)] = set(((x, y),))
            else:
                targets_d[(x, y)] = set()

    # Von 8 bis 0 absteigend Karte prüfen
    for val in range(8, -1, -1):
        char = str(val)
        prev_char = str(val + 1)

        for y in range(height):
            for x in range(width):
                if map_l[y][x] == char:
                    # Aktuellen Wert an (x, y) gefunden, alle benachbarten
                    # Felder prüfen, ob dort der nächsthöhere Wert steht.
                    # Wenn ja: dessen Ziel-Positionen zu der aktuellen Position
                    # hinzufügen
                    target_s = targets_d[(x, y)]
                    if y > 0 and map_l[y - 1][x] == prev_char:
                        target_s |= targets_d[(x, y - 1)]
                    if y < height - 1 and map_l[y + 1][x] == prev_char:
                        target_s |= targets_d[(x, y + 1)]
                    if x > 0 and map_l[y][x - 1] == prev_char:
                        target_s |= targets_d[(x - 1, y)]
                    if x < width - 1 and map_l[y][x + 1] == prev_char:
                        target_s |= targets_d[(x + 1, y)]

    # Für alle Kartenpositionen mit einer 0:
    # Anzahl der Ziele ermitteln und aufsummieren
    sum_score = 0
    for y in range(height):
        for x in range(width):
            if map_l[y][x] == "0":
                num_targets = len(targets_d[(x, y)])
                sum_score += num_targets

    return sum_score


def part2(map_l: list[str]) -> int:
    """Teilaufgabe 2 lösen

    Args:
        map_l: Die eingelesene Karte

    Returns:
        Gesamt-Rating
    """
    width = len(map_l[0])
    height = len(map_l)

    # Ziel-Karte vorbereiten, an allen 9 den Wert 1 eintragen, sonst 0
    rating_d: dict[tuple[int, int], int] = {}
    for y in range(height):
        for x in range(width):
            if map_l[y][x] == "9":
                rating_d[(x, y)] = 1
            else:
                rating_d[(x, y)] = 0

    # Von 8 bis 0 absteigend Karte prüfen
    for val in range(8, -1, -1):
        char = str(val)
        prev_char = str(val + 1)

        for y in range(height):
            for x in range(width):
                if map_l[y][x] == char:
                    # Aktuellen Wert an (x, y) gefunden, alle benachbarten
                    # Felder prüfen, ob dort der nächsthöhere Wert steht.
                    # Wenn ja: dort stehendes Rating zum eigenen Rating
                    # hinzuaddieren
                    rating = 0
                    if y > 0 and map_l[y - 1][x] == prev_char:
                        rating += rating_d[(x, y - 1)]
                    if y < height - 1 and map_l[y + 1][x] == prev_char:
                        rating += rating_d[(x, y + 1)]
                    if x > 0 and map_l[y][x - 1] == prev_char:
                        rating += rating_d[(x - 1, y)]
                    if x < width - 1 and map_l[y][x + 1] == prev_char:
                        rating += rating_d[(x + 1, y)]
                    rating_d[(x, y)] = rating

    # Für alle Kartenpositionen mit einer 0:
    # Rating aufsummieren
    sum_rating = 0
    for y in range(height):
        for x in range(width):
            if map_l[y][x] == "0":
                sum_rating += rating_d[(x, y)]

    return sum_rating


def main() -> None:
    """Datei einlesen und beide Teilaufgaben lösen"""
    map_l = read_file(PATH)

    sum_score = part1(map_l)
    print(f"Ergebnis Teilaufgabe 1 für {PATH}: {sum_score}")
    # PATH1: 36
    # PATH_MB: 796

    sum_rating = part2(map_l)
    print(f"Ergebnis Teilaufgabe 2 für {PATH}: {sum_rating}")
    # PATH2: 13
    # PATH3: 227
    # PATH1: 81
    # PATH_MB: 1942


if __name__ == "__main__":
    main()

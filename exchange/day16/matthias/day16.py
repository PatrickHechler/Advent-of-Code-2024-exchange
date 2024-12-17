"""Advent of Code 2024, Tag 16
Lösung von Matthias Baake
"""

import heapq
from functools import partial
from pathlib import Path
from typing import Callable, Iterable, TypeVar, Optional

DIRECTORY = Path("2024")

PATH_1 = DIRECTORY / "data_16-example1.txt"
PATH_2 = DIRECTORY / "data_16-example2.txt"
PATH_MB = DIRECTORY / "data_16-matthias_baake.txt"

PATH = PATH_MB


def read_file(path: Path) -> tuple[list[str], tuple[int, int], tuple[int, int], str]:
    """Datei einlesen

    Args:
        path: Pfad der einzulesenden Datei

    Returns:
        [0]: Liste mit den eingelesenen Zeilen
        [1]: Startposition ("S")
        [2]: Endposition ("E")
        [3]: Richtung, in die die Rentiere zu Beginn schauen
        tuple[list[str], tuple[int, int], tuple[int, int], str]: _description_
    """
    pos_start = None
    pos_end = None
    line_l = []
    direction = "E"
    with open(path, encoding="utf-8") as file:
        for row, line in enumerate(file):
            line_l.append(line)
            if "S" in line:
                col = line.index("S")
                assert pos_start is None
                pos_start = (row, col)
            elif "E" in line:
                col = line.index("E")
                assert pos_end is None
                pos_end = (row, col)
    assert pos_start is not None
    assert pos_end is not None
    return line_l, pos_start, pos_end, direction


T = TypeVar("T")
# 🩸 ... bleeding edge ... 🩸
#
# Seit 3.12 auch ohne TypeVar möglich, aber damit kommt mypy standardmäßig
# noch nicht klar, Meldung:
# PEP 695 generics are not yet supported. Use
# --enable-incomplete-feature=NewGenericSyntax for experimental support
#
# Schreibweise ist (ohne Verwendung von TypeVar):
# def dijkstra[T](...) -> ...:
#     ...
# vgl:
# https://docs.python.org/3.12/library/typing.html#generics
# https://mypy.readthedocs.io/en/stable/generics.html#generic-functions
#
# Darüber hinaus führt in manchen Fällen (so auch hier) black eine unschöne
# Formatierung durch, siehe https://github.com/psf/black/issues/4254 und
# https://github.com/psf/black/issues/4355
#
# 🩸 ... bleeding edge ... 🩸


def dijkstra(
    start_pos: T,
    end_pos_s: set[T],
    get_next: Callable[[T], Iterable[tuple[int, T]]],
) -> tuple[int, dict[T, tuple[int, set[T]]], set[T]]:
    """Sucht alle möglichen Wege ab start_pos mit dem Dijkstra-Algorithmus

    Der komplette Graph wird abgesucht und zu jedem Knoten wird eine Menge
    mit allen Vorgänger-Knoten bereitgestellt, über die ein kürzester Weg
    zu diesem Knoten ermittelt werden kann.

    Args:
        start_pos: Startposition
        end_pos_s: Menge von Endpositionen
        get_next: Funktion, die als Parameter einen Knoten erhält und ein
            Iterable mit allen von diesem Knoten direkt erreichbaren Knoten
            mit den jeweiligen Kosten zurückgibt

    Returns:
        [0]: Geringste Kosten von start_pos zu einem Knoten aus end_pos_s
        [1]: Dictionary, welches zu jedem Knoten die Kosten für den günstigsten
            Weg von start_pos zu diesem Knoten und eine Menge von Vorgänger-
            Knoten enthält, von denen aus der Knoten mit den geringstmöglichen
            Kosten erreicht werden kann
        [2]: Die Teilmenge von end_pos_s, die die mit den geringstmöglichen
            Kosten erreichbaren Zielknoten enthält
    """
    # Bereits endgültig geklärte Positionen
    visited_s: set[T] = set()

    # Aktuell zu prüfende Positionen (Score, Position)
    possible_l: list[tuple[int, T]] = [(0, start_pos)]
    heapq.heapify(possible_l)  # Sollte eigentlich unnötig sein, da nur ein Eintrag

    # Aktuell bester bekannter Score für T mit allen Vorgängern, von denen aus
    # man T mit diesem Score erreicht
    current_best_d: dict[T, tuple[int, set[T]]] = {start_pos: (0, set())}

    while possible_l:
        curr_score, pos = heapq.heappop(possible_l)
        if pos in visited_s:
            continue
        visited_s.add(pos)

        for delta_score, next_pos in get_next(pos):
            if next_pos in visited_s:
                # Bester Weg bereits ermittelt, ignorieren
                continue

            # Score, wenn man nach dem bisher besten bekannten Weg zu pos
            # (hat den score curr_score) weitergeht zu next_pos
            next_score = curr_score + delta_score

            if next_pos not in current_best_d:
                current_best_d[next_pos] = (9999999999, set())
            best_known, curr_prev_s = current_best_d[next_pos]

            if next_score < best_known:
                # Besseren (oder ersten) Weg gefunden, eintragen und zur
                # Prüfung vormerken
                current_best_d[next_pos] = (next_score, set((pos,)))
                heapq.heappush(possible_l, (next_score, next_pos))
            elif next_score == best_known:
                # Gleich guten Weg gefunden, zur Menge hinzufügen
                curr_prev_s.add(pos)
                current_best_d[next_pos] = (best_known, curr_prev_s)

    min_score: Optional[int] = None
    target_s: set[T] = set()
    for pos in end_pos_s:
        score, _ = current_best_d[pos]
        if min_score is None or score < min_score:
            min_score = score
            target_s = {pos}
        elif min_score is not None and score == min_score:
            target_s.add(pos)

    assert min_score is not None
    return min_score, current_best_d, target_s


def get_next_pos(
    line_l: list[str], pos: tuple[int, int, str]
) -> list[tuple[int, tuple[int, int, str]]]:
    """Gibt die nächsten Zustände zurück

    Ausgehend von Position pos wird eine Liste aller von dieser Position
    direkt erreichbaren Positionen mit den jeweiligen Kosten erzeugt.
    Basis dafür ist die Karte line_l.
    "Position" beinhaltet dabei Zeile, Spalte und Richtung.

    Diese Funktion ist zur Verwendung als Parameter get_next in der Funktion
    dijkstra() bestimmt.

    Args:
        line_l: Liste von Strings, die die Karte repräsentieren
        pos: Posititon, zu der die Folgepositionen zu ermitteln sind.

    Returns:
        Liste von Paaren mit [0]: Kosten von pos zur Position, [1]: nächste
        Position
    """
    delta_d = {"N": (-1, 0), "S": (1, 0), "W": (0, -1), "E": (0, 1)}
    next_pos_l: list[tuple[int, tuple[int, int, str]]] = []

    row, col, direction = pos
    d_row, d_col = delta_d[direction]

    # Bewegung von row, col in Richtung curr_dir ohne Drehung, wenn möglich
    if line_l[row + d_row][col + d_col] != "#":
        next_pos_l.append((1, (row + d_row, col + d_col, direction)))
    # Drehung an row, col um 90°
    if direction in ("N", "S"):
        for next_dir in ("W", "E"):
            next_pos_l.append((1000, (row, col, next_dir)))
    else:  # curr_dir in ("W", "E")
        for next_dir in ("N", "S"):
            next_pos_l.append((1000, (row, col, next_dir)))

    return next_pos_l


def find_shortest_path(
    line_l: list[str],
    pos_start: tuple[int, int],
    pos_end: tuple[int, int],
    direction: str,
) -> tuple[int, int]:
    """Sucht den "billigsten" Weg auf Karte line_l

    Args:
        line_l: Die Karte, auf der der Weg zu suchen ist
        pos_start: Startposition
        pos_end: Endposition
        direction: Richtung zu Beginn

    Returns:
        [0]: Kosten des billigsten Weges
        [1]: Anzahl von (row, col)-Positionen, die auf mindestens einem
            aller möglichen billigsten Wege liegen
    """
    assert line_l[pos_start[0]][pos_start[1]] == "S"
    assert line_l[pos_end[0]][pos_end[1]] == "E"

    score, best_d, target = dijkstra(
        (pos_start[0], pos_start[1], direction),
        set(
            (
                (pos_end[0], pos_end[1], "N"),
                (pos_end[0], pos_end[1], "S"),
                (pos_end[0], pos_end[1], "E"),
                (pos_end[0], pos_end[1], "W"),
            )
        ),
        partial(get_next_pos, line_l),
    )

    checked_s: set[tuple[int, int, str]] = set()
    to_check_s: set[tuple[int, int, str]] = target
    while to_check_s:
        pos = to_check_s.pop()
        if pos not in checked_s:
            checked_s.add(pos)
            _, prev_pos_s = best_d[pos]
            to_check_s |= prev_pos_s

    pos_s: set[tuple[int, int]] = {(x[0], x[1]) for x in checked_s}

    return score, len(pos_s)


def main() -> None:
    """Datei einlesen und beide Teilaufgaben lösen"""
    line_l, pos_start, pos_end, direction = read_file(PATH)

    score, num_pos = find_shortest_path(line_l, pos_start, pos_end, direction)

    # Teilaufgabe 1
    print(f"Ergebnis Teilaufgabe 1 für {PATH}: {score}")
    # PATH1: 7036
    # PATH2: 11048
    # PATH_MB: 135512

    # Teilaufgabe 2
    print(f"Ergebnis Teilaufgabe 2 für {PATH}: {num_pos}")
    # PATH1: 45
    # PATH2: 64
    # PATH_MB: 541


if __name__ == "__main__":
    main()

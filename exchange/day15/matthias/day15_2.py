"""Advent of Code 2024, Tag 15, Teil 2
Lösung von Matthias Baake
"""

from pathlib import Path
from typing import Optional

DIRECTORY = Path("2024")

PATH1 = DIRECTORY / "data_15-example1.txt"
PATH2 = DIRECTORY / "data_15-example2.txt"
PATH_MB = DIRECTORY / "data_15-matthias_baake.txt"

PATH = PATH_MB


def read_file(
    path: Path,
) -> tuple[
    tuple[int, int],
    set[tuple[int, int]],
    set[tuple[int, int]],
    str,
]:
    robot = None
    wall_s: set[tuple[int, int]] = set()
    box_s: set[tuple[int, int]] = set()
    move_l: list[str] = []
    after_map = False

    with open(path, encoding="utf-8") as file:
        for row, line in enumerate(file):
            if after_map:
                move_l.append(line.rstrip())
            else:
                if line.rstrip() == "":
                    after_map = True
                    continue
                for col, char in enumerate(line):
                    if char == "@":
                        robot = (row, col * 2)
                    elif char == "O":
                        box_s.add((row, col * 2))
                    elif char == "#":
                        wall_s.add((row, col * 2))
                        wall_s.add((row, col * 2 + 1))
    assert robot is not None
    return robot, wall_s, box_s, "".join(move_l)


def get_gps_sum(box_s: set[tuple[int, int]]) -> int:
    gps_sum = 0
    for row, col in box_s:
        gps_sum += row * 100 + col
    return gps_sum


def show_map(
    robot: tuple[int, int],
    wall_s: set[tuple[int, int]],
    box_s: set[tuple[int, int]],
) -> None:
    height = max(wall_s, key=lambda wall: wall[0])[0] + 1
    width = max(wall_s, key=lambda wall: wall[1])[1] + 1

    for row in range(height):
        line_l = []
        prev_box = False
        for col in range(width):
            if row == robot[0] and col == robot[1]:
                assert not prev_box
                line_l.append("@")
            elif (row, col) in wall_s:
                assert not prev_box
                line_l.append("#")
            elif (row, col) in box_s:
                assert not prev_box
                line_l.append("[]")
                prev_box = True
            else:
                if prev_box:
                    prev_box = False
                else:
                    line_l.append(".")
        print("".join(line_l))
    print()


delta_row_d = {"<": 0, ">": 0, "^": -1, "v": 1}
delta_col_d = {"<": -1, ">": 1, "^": 0, "v": 0}


def is_move_possible(
    curr_row: int,
    pos_left: int,
    pos_right: int,
    wall_s: set[tuple[int, int]],
    box_s: set[tuple[int, int]],
    move: str,
) -> tuple[bool, Optional[set[tuple[int, int]]]]:
    # Position nach Bewegung
    # Es kann pos_left == pos_right sein (direkt schiebender Roboter).
    # Andernfalls (schiebende Box) gilt pos_left + 1 == pos_right.
    next_row = curr_row + delta_row_d[move]
    n_col_left = pos_left + delta_col_d[move]
    n_col_right = pos_right + delta_col_d[move]

    # Mögliche Kollision mit Wänden prüfen
    if (next_row, n_col_left) in wall_s or (next_row, n_col_right) in wall_s:
        return False, None

    if move in ("^", "v"):
        if (next_row, n_col_left) in box_s:
            # n_col_left == n_col_right: Roboter befindet sich genau über/unter der
            # linken Seite einer Box
            # n_col_left == n_col_right - 1: schiebende Box befindet sich
            # genau über/unter einer anderen Box -> dieses Objekt kann direkt
            # nur eine Box schieben
            # Daher ist nur eine Prüfung auf eine Box an (next_row, n_col_left)
            # (mit rechter Hälfte an (next_row, n_col_left + 1)) erforderlich.
            move_possible, moved_boxes_s = is_move_possible(
                next_row, n_col_left, n_col_left + 1, wall_s, box_s, move
            )
            if not move_possible:
                return False, None
            assert moved_boxes_s is not None
            moved_boxes_s.add((next_row, n_col_left))
            return True, moved_boxes_s

        # Es könnte sein (bei n_col_left + 1 == n_col_right), dass zwei Boxen
        # geschoben werden müssten (eine mit der linken Hälfte an
        # n_col_left - 1, die andere mit der linken Hälfte an n_col_right).
        # Bei n_col_left == n_col_right käme nur eine Box mit ihrer linken
        # Hälfte an n_col_left - 1 in Frage, denn eine Box an n_col_left wäre
        # bereits oben geprüft worden.
        # Die entsprechende Prüfung unten stört in diesem Fall aber nicht.
        moved_boxes_s = set()
        if (next_row, n_col_left - 1) in box_s:
            # Die Box an next_row, n_col_left - 1 müsste geschoben werden.
            move_possible, moved_boxes_l_s = is_move_possible(
                next_row, n_col_left - 1, n_col_left, wall_s, box_s, move
            )
            if not move_possible:
                return False, None
            assert moved_boxes_l_s is not None
            moved_boxes_l_s.add((next_row, n_col_left - 1))
            moved_boxes_s |= moved_boxes_l_s
            # Hier kein return, der nächste Fall ist auch zu prüfen!

        if (next_row, n_col_right) in box_s:
            # Die Box an next_row, n_col_right müsste geschoben werden.
            move_possible, moved_boxes_r_s = is_move_possible(
                next_row, n_col_right, n_col_right + 1, wall_s, box_s, move
            )
            if not move_possible:
                return False, None
            assert moved_boxes_r_s is not None
            moved_boxes_r_s.add((next_row, n_col_right))
            moved_boxes_s |= moved_boxes_r_s

        return True, moved_boxes_s

    if move == "<":
        # Die neue linke Position kann nicht identisch mit einer Box-Position
        # sein, da andernfalls die aktuelle linke Position identisch mit der
        # rechten Hälfte dieser Box wäre
        assert (next_row, n_col_left) not in box_s

        if (next_row, n_col_left - 1) in box_s:
            # Links neben der linken Position ist eine Box; prüfen, ob
            # diese bewegt werden kann und welche diese bewegen würde
            move_possible, moved_boxes_s = is_move_possible(
                next_row, n_col_left - 1, n_col_left, wall_s, box_s, move
            )
            if not move_possible:
                return False, None
            assert moved_boxes_s is not None
            moved_boxes_s.add((next_row, n_col_left - 1))
            return True, moved_boxes_s
        # Keine Box links neben der Position (auch keine Wall, dies wurde
        # oben geprüft) --> Bewegung möglich, es wird keine Box geschoben
        return True, set()

    # move == ">"
    if (next_row, n_col_right) in box_s:
        # Rechts neben der rechten Position ist eine Box; prüfen, ob
        # diese bewegt werden kann und welche diese bewegen würde
        move_possible, moved_boxes_s = is_move_possible(
            next_row, n_col_right, n_col_right + 1, wall_s, box_s, move
        )
        if not move_possible:
            return False, None
        assert moved_boxes_s is not None
        moved_boxes_s.add((next_row, n_col_right))
        return True, moved_boxes_s

    # Keine Box rechts neben der Position (auch keine Wall, dies wurde
    # oben geprüft) --> Bewegung möglich, es wird keine Box geschoben
    return True, set()


def main() -> None:
    robot, wall_s, box_s, moves = read_file(PATH)

    for move in moves:
        move_possible, moved_boxes_s = is_move_possible(
            robot[0], robot[1], robot[1], wall_s, box_s, move
        )
        if move_possible:
            assert moved_boxes_s is not None

            d_row = delta_row_d[move]
            d_col = delta_col_d[move]

            # Roboter an neue Position verschieben
            robot = (robot[0] + d_row, robot[1] + d_col)

            # Verschobene Boxen an ihren bisherigen Positionen entfernen
            box_s -= moved_boxes_s

            # Verschobene Boxen mit ihren neuen Positionen eintragen
            for box in moved_boxes_s:
                box_s.add((box[0] + d_row, box[1] + d_col))

    result = get_gps_sum(box_s)
    # Teilaufgabe 2
    print(f"Ergebnis Teilaufgabe 2 für {PATH}: {result}")
    # PATH1: 9021
    # PATH_MB: 1381446


if __name__ == "__main__":
    main()

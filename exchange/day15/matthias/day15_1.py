"""Advent of Code 2024, Tag 15, Teil 1
Lösung von Matthias Baake
"""

from pathlib import Path

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
                        robot = (row, col)
                    elif char == "O":
                        box_s.add((row, col))
                    elif char == "#":
                        wall_s.add((row, col))
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
        for col in range(width):
            if row == robot[0] and col == robot[1]:
                line_l.append("@")
            elif (row, col) in wall_s:
                line_l.append("#")
            elif (row, col) in box_s:
                line_l.append("O")
            else:
                line_l.append(".")
        print("".join(line_l))
    print()


delta_row_d = {"<": 0, ">": 0, "^": -1, "v": 1}
delta_col_d = {"<": -1, ">": 1, "^": 0, "v": 0}


def make_move(
    robot: tuple[int, int],
    wall_s: set[tuple[int, int]],
    box_s: set[tuple[int, int]],
    move: str,
) -> tuple[int, int]:
    d_row = delta_row_d[move]
    d_col = delta_col_d[move]

    new_row = robot[0] + d_row
    new_col = robot[1] + d_col

    if (new_row, new_col) in wall_s:
        # Roboter steht direkt neben einer Wand, keine Bewegung möglich
        return robot

    if (new_row, new_col) in box_s:
        # Roboter steht direkt neben einer Box, prüfen
        first_box_row = new_row
        first_box_col = new_col
        while True:
            new_row += d_row
            new_col += d_col
            if (new_row, new_col) in wall_s:
                # Box-Reihe steht neben einer Wand, keine Bewegung möglich
                return robot
            if (new_row, new_col) in box_s:
                # Eine weitere Box, weiter prüfen
                pass
            else:
                # Platz ist frei -> Box-Reihe kann bewegt werden
                # Der Einfachheit halber die erste Box hierher verschieben
                box_s.add((new_row, new_col))
                box_s.remove((first_box_row, first_box_col))
                return (robot[0] + d_row, robot[1] + d_col)

    # Sonst: Platz neben Roboter ist frei, bewegen
    return (new_row, new_col)


def main() -> None:
    robot, wall_s, box_s, moves = read_file(PATH)
    # show_map(obj_s, robot)
    for next_move in moves:
        robot = make_move(robot, wall_s, box_s, next_move)
        # show_map(obj_s, robot)

    result = get_gps_sum(box_s)
    # Teilaufgabe 1
    print(f"Ergebnis Teilaufgabe 1 für {PATH}: {result}")
    # PATH1: 10092
    # PATH2: 2028
    # PATH_MB: 1360570


if __name__ == "__main__":
    main()

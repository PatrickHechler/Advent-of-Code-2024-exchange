"""Advent of Code 2024, Tag 14
Lösung von Matthias Baake
"""

from io import TextIOBase
import re
from collections import Counter
from pathlib import Path

DIRECTORY = Path("2024")

PATH1 = DIRECTORY / "data_14-example.txt"
SIZE1 = (11, 7)

PATH_MB = DIRECTORY / "data_14-matthias_baake.txt"
SIZE_MB = (101, 103)

PATH = PATH_MB
SIZE = SIZE_MB

OUTPUT = DIRECTORY / "output_day14.txt"


def read_file(path: Path) -> tuple[list[tuple[int, int]], list[tuple[int, int]]]:
    pat_line = re.compile(r"p=(\d+),(\d+) v=(-?\d+),(-?\d+)")

    pos_l: list[tuple[int, int]] = []
    v_l: list[tuple[int, int]] = []
    with open(path, encoding="utf-8") as file:
        for line in file:
            m = pat_line.match(line)
            assert m
            pos_l.append((int(m.group(1)), int(m.group(2))))
            v_l.append((int(m.group(3)), int(m.group(4))))
    return pos_l, v_l


def move(
    pos_l: list[tuple[int, int]],
    v_l: list[tuple[int, int]],
    num_move: int,
    width: int,
    height: int,
) -> list[tuple[int, int]]:
    new_pos_l = []
    for pos, v in zip(pos_l, v_l):
        new_pos_l.append(
            (
                (pos[0] + v[0] * num_move) % width,
                (pos[1] + v[1] * num_move) % height,
            )
        )
    return new_pos_l


def show(
    file: TextIOBase, sec: int, pos_l: list[tuple[int, int]], width: int, height: int
) -> None:
    num_d = Counter(pos_l)
    file.write(f"# Nach {sec} Sekunden\n")
    file.write("#" * (width + 4))
    file.write("\n")
    for row in range(height + 1):
        line_l: list[str] = []
        for col in range(width):
            num = num_d[(col, row)]
            if num == 0:
                line_l.append(" ")
            elif num < 10:
                line_l.append(str(num))
            else:
                line_l.append("X")
        file.write("# ")
        file.write("".join(line_l))
        file.write(" #")
        file.write("\n")
    file.write("#" * (width + 4))
    file.write("\n")
    file.write("\n\n\n")


def main() -> None:
    pos_l, v_l = read_file(PATH)
    width, height = SIZE

    # Teilaufgabe 1
    num_move = 100
    new_pos_l = move(pos_l, v_l, num_move, width, height)

    width2 = width // 2
    x2 = width2 + 1
    height2 = height // 2
    y2 = height2 + 1
    num_q1 = 0
    num_q2 = 0
    num_q3 = 0
    num_q4 = 0

    for x, y in new_pos_l:
        if x < width2:
            if y < height2:
                num_q1 += 1
            elif y >= y2:
                num_q2 += 1
        elif x >= x2:
            if y < height2:
                num_q3 += 1
            elif y >= y2:
                num_q4 += 1

    result = num_q1 * num_q2 * num_q3 * num_q4

    print(f"Ergebnis Teilaufgabe 1 für {PATH}: {result}")
    # PATH1: 12
    # PATH_MB: 223020000

    # Teilaufgabe 2
    second = 0
    num_seconds = 66
    pos_l = move(pos_l, v_l, num_seconds, width, height)
    second += num_seconds

    with open(OUTPUT, "w", encoding="utf-8") as file:
        for _ in range(1000):
            num_seconds = 101
            pos_l = move(pos_l, v_l, 101, width, height)
            second += num_seconds
            show(file, second, pos_l, width, height)


# Nach 7338 Sekunden
#########################################################################################################
#                                                                                                       #
#        1                                                                                              #
#                    1    1 1                                                                           #
#                                                                                  1                    #
#                                             1         1    1                                          #
#                                 1                                                                     #
#                   1                                                                                   #
#    1                                                                       1                          #
#                                                                1                                      #
#                   1                                   1            1                                  #
#  1  1                                                                                                 #
#                       1                                                              1                #
#                 1                  1       1                               1                          #
#                                                                             1                         #
#              1                     1                                                                  #
#                                 1                                                     1               #
#  1                  1      1                                     1                                    #
#                1                                                1                                     #
#                1    1                                                                                 #
#                                                                                                1      #
#                                                                       1                               #
#       1                                                                             1     1           #
#                          1                                                                            #
#       1                         1                                            1                        #
#           1                                               1                                           #
#                                                                                                       #
#                                                                                                       #
#                                         1       1               11             1                      #
#                                                                                                       #
#                                                                                                 1     #
# 1                                                                                         1           #
#       1                              1       1                                                        #
#                                                                                                       #
#                                                                                  1 1                  #
#                1111111111111111111111111111111                                                        #
#                1                             1               1                                        #
#                1                             1                                       1                #
#                1                             1                          1                             #
# 1              1                             1                                         1              #
#                1              1              1                                     1            1     #
#                1             111             1                                                        #
#  1             1            11111            1                       1                         1      #
#                1           1111111           1                                                11      #
#                1          111111111          1                                              1         #
#                1            11111            1          1                    1                     1  #
#              1 1           1111111           1                                                        #
#                1          111111111          1                                                        #
#                1         11111111111         1                                                        #
#                1        1111111111111        1             1             1       1                    #
#                1          111111111          1   1                                                    #
#                1         11111111111         1                                                        #
#       1        1        1111111111111        1                                         1              #
#                1       111111111111111       1              11                   1               1    #
#                1      11111111111111111      1                 1                                      #
#                1        1111111111111        1                                                        #
#                1       111111111111111       1          1                                             #
#                1      11111111111111111      1              1                                         #
#                1     1111111111111111111     1                                                        #
#                1    111111111111111111111    1  1                                                     #
#                1             111             1                       1                                #
#                1             111             1                                                        #
#              1 1             111             1      1                      1                          #
#          1     1                             1                                           1            #
#                1                             1                      1                      1          #
#                1                             1              1                                         #
#                1                             1                                                  1     #
#                1111111111111111111111111111111                                                        #
#                                                                      1        1                       #
#                                                                                                       #
#                                                                                                       #
#                                          1                                                            #
#                                                                                                       #
#                                                                              1                        #
#                            1                                                                          #
#                                                                                 1                     #
#     1                                                                                                 #
#                                 1                               1                                     #
#                                                                                                       #
#                    1              1     1                                                             #
#                                                               1                                       #
#                                 1                                                                     #
#                                                                   1                                   #
#                                                                                                       #
#                        1          1                    1                                              #
#                         1                              1                                              #
#                                          1                                                            #
#                                                                             1                         #
#                                                                    1                                  #
#     1                                                      1                                          #
#                                                            1                                   1      #
#                  1                               1     1                 1                            #
#                                                                                                       #
#                                                                                                       #
# 1   1                                      1                          1              1                #
#                                                                                                       #
#                                                                                                1      #
#                                                             1                                         #
#                                      1                                                                #
#                                                                                                    1  #
#                                                 1                                                     #
#            1                                                                                          #
#                                                                                                       #
#                  1             1     1                                                                #
#                                                                                                       #
#########################################################################################################

if __name__ == "__main__":
    main()

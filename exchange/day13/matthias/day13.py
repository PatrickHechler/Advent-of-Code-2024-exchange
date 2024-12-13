"""Advent of Code 2024, Tag 13
Lösung von Matthias Baake
"""

import logging
from fractions import Fraction
from pathlib import Path
from typing import Optional

logger = logging.getLogger(__name__)

DIRECTORY = Path("2024")
PATH1 = DIRECTORY / "data_13-example.txt"
PATH_MB = DIRECTORY / "data_13-matthias_baake.txt"

# PATH = PATH1
# logger.setLevel(logging.INFO)  # Optional

PATH = PATH_MB
logger.setLevel(logging.WARNING)


class Machine:
    def __init__(self, x_a: int, y_a: int, x_b: int, y_b: int, p_x: int, p_y: int):
        self.x_a = x_a
        self.y_a = y_a
        self.x_b = x_b
        self.y_b = y_b
        self.p_x = p_x
        self.p_y = p_y

    def show(self) -> None:
        logger.info("Button A: X+%d, Y+%d", self.x_a, self.y_a)
        logger.info("Button B: X+%d, Y+%d", self.x_b, self.y_b)
        logger.info("Prize: X=%d, Y=%d", self.p_x, self.p_y)

    def get_solution(self) -> Optional[tuple[Fraction, Fraction]]:
        # p_x = x_a * a + x_b * b [1]
        # p_y = y_a * a + y_b * b [2]
        #
        # [1] umstellen:
        # a = (p_x - x_b * b) / x_a [3]
        # [3] in [2] einsetzen:
        # p_y = y_a * (p_x - x_b * b) / x_a + y_b * b [4]
        # [4] umstellen:
        # p_y = y_a / x_a * p_x - y_a / x_a * x_b * b + y_b * b
        # p_y - y_a / x_a * p_x = (-y_a / x_a * x_b + y_b) * b
        # b = (p_y - y_a / x_a * p_x) / (y_b - y_a / x_a * x_b) [5]
        # Ergebnis in [3] einsetzen

        b = Fraction(
            (self.p_y - Fraction(self.y_a, self.x_a) * self.p_x),
            (self.y_b - Fraction(self.y_a, self.x_a) * self.x_b),
        )
        a = Fraction(self.p_x - self.x_b * b, self.x_a)

        if a.denominator != 1 or b.denominator != 1:
            return None
        return a, b


def read_file(path: Path) -> list[Machine]:
    machine_l: list[Machine] = []
    with open(path, encoding="utf-8") as file:
        while True:
            prefix, values = next(file).split(":")
            assert prefix == "Button A"
            str_x, str_y = values.split(",")
            a_x = int(str_x.split("+")[1])
            a_y = int(str_y.split("+")[1])

            prefix, values = next(file).split(":")
            assert prefix == "Button B"
            str_x, str_y = values.split(",")
            b_x = int(str_x.split("+")[1])
            b_y = int(str_y.split("+")[1])

            prefix, values = next(file).split(":")
            assert prefix == "Prize"
            str_x, str_y = values.split(",")
            p_x = int(str_x.split("=")[1])
            p_y = int(str_y.split("=")[1])

            machine_l.append(Machine(a_x, a_y, b_x, b_y, p_x, p_y))

            try:
                line_empty = next(file).rstrip()
                assert line_empty == ""
            except StopIteration:
                break

    return machine_l


def main() -> None:
    logging.basicConfig(level=logging.INFO)

    machine_l = read_file(PATH)

    # Teilaufgabe 1
    total_cost = Fraction(0)
    for machine in machine_l:
        machine.show()
        solution = machine.get_solution()
        if solution:
            a, b = solution
            logger.info("Solution: A=%s, B=%s", a, b)
            cost = 3 * a + 1 * b
            logger.info("Cost: %d", cost)
            total_cost += cost
        else:
            logger.info("No solution")
        logger.info("")

    print(f"Ergebnis Teilaufgabe 1 für {PATH}: {total_cost}")
    # PATH1: 480
    # PATH_MB: 40369

    # Teilaufgabe 2
    for machine in machine_l:
        machine.p_x += 10000000000000
        machine.p_y += 10000000000000

    total_cost = Fraction(0)
    for machine in machine_l:
        machine.show()
        solution = machine.get_solution()
        if solution:
            a, b = solution
            logger.info("Solution: A=%s, B=%s", a, b)
            cost = 3 * a + 1 * b
            logger.info("Cost: %d", cost)
            total_cost += cost
        else:
            logger.info("No solution")
        logger.info("")

    print(f"Ergebnis Teilaufgabe 2 für {PATH}: {total_cost}")
    # PATH1: 875318608908
    # PATH_MB: 72587986598368


if __name__ == "__main__":
    main()

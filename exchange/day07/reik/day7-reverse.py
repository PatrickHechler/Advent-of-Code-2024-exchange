#!/usr/bin/env python3

from time import perf_counter

with open('day7.txt') as f:
    equations = [[int(line.split()[0].rstrip(':')), list(map(int, line.split()[1:]))] for line in f]

def ends_with(value, end):
    value = str(value)
    end = str(end)
    return value.endswith(end)

def check_equation(target, numbers, part2=False):
    if not numbers:
        return target == 0
    n = numbers[-1]
    if target < n:
        return False
    if check_equation(target - n, numbers[:-1], part2):
        return True
    if target % n == 0 and check_equation(target // n, numbers[:-1], part2):
        return True
    if part2 and len(str(target)) > len(str(n)):
        if ends_with(target, n) and check_equation(int(str(target)[:-len(str(n))]), numbers[:-1], part2):
            return True
    return False

start = perf_counter()
total = sum(target for target, numbers in equations if check_equation(target, numbers))
print('Part 1', total, f'({round((perf_counter() - start) * 1000)} ms)')

start = perf_counter()
total = sum(target for target, numbers in equations if check_equation(target, numbers, True))
print('Part 2', total, f'({round((perf_counter() - start) * 1000)} ms)')
#!/usr/bin/env python3

from time import perf_counter

with open('day7.txt') as f:
    equations = [[int(line.split()[0].rstrip(':')), list(map(int, line.split()[1:]))] for line in f]

operations = [
    lambda a, b: a + b,
    lambda a, b: a * b,
    lambda a, b: int(str(a) + str(b))
]

def check_equation(target, numbers, operations):
    if len(numbers) > 1:
        a, b, *rest = numbers
        if a > target: 
            return
        for op in operations:
            numbers = [op(a, b)] + rest
            yield from check_equation(target, numbers, operations)
    elif numbers[0] == target:
        yield numbers[0]

def options(equations, operations):
    for target, numbers in equations:
        yield next(check_equation(target, numbers, operations), 0)


start = perf_counter()
total = sum(options(equations, operations[:2]))
print('Part 1:', total, f'({round((perf_counter() - start) * 1000)} ms)')

start = perf_counter()
total = sum(options(equations, operations));
print('Part 2:', total, f'({round((perf_counter() - start) * 1000)} ms)')
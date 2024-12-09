#!/usr/bin/env python3

with open('day8.txt') as f:
    lines = f.readlines()
    frequencies = {}
    max_x = len(lines[0].strip()) - 1
    max_y = len(lines) - 1
    for y, line in enumerate(lines):
        for x, char in enumerate(line.strip()):
            if char != '.':
                if char in frequencies:
                    frequencies[char].append((x, y))
                else:
                    frequencies[char] = [(x, y)]

def get_distance(p1, p2):
    return p2[1] - p1[1], p2[0] - p1[0]

def is_inbounds(p):
    return 0 <= p[0] <= max_x and 0 <= p[1] <= max_y

def find_antinodes(p1, p2, part2=False):
    dy, dx = get_distance(p1, p2)
    antinodes = set()
    def generate_antinodes(start, dy, dx, step):
        count = 1
        while True:
            new_point = (start[0] + count * step * dx, start[1] + count * step * dy)
            if is_inbounds(new_point):
                yield new_point
                count += 1
            else:
                break
    for p, dir in [(p1, -1), (p2, 1)]: 
        if part2:
            antinodes.update(generate_antinodes(p, dy, dx, dir))
            antinodes.add(p)
        else:
            antinode = next(generate_antinodes(p, dy, dx, dir), None)
            if antinode:
                antinodes.add(antinode)
    return antinodes

antinodes = {antinode for fq in frequencies.values() for i in range(len(fq)-1) for j in range(i+1, len(fq)) for antinode in find_antinodes(fq[i], fq[j])}
print('Part 1:', len(antinodes))
        
antinodes = {antinode for fq in frequencies.values() for i in range(len(fq)-1) for j in range(i+1, len(fq)) for antinode in find_antinodes(fq[i], fq[j], True)}
print('Part 2:', len(antinodes))
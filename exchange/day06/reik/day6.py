#!/usr/bin/env python3

with open('day6.txt') as f:
    lines = f.readlines()
    obstacles = set()
    max_x = len(lines[0].strip()) - 1
    max_y = len(lines) - 1
    for y, line in enumerate(lines):
        for x, position in enumerate(line.strip()):
            if position == '#':
                obstacles.add((x, y))
            elif position == '^':
                start = (x, y)


def get_visited(position, obstacles, direction = (0, -1)):
    visited = {position: {direction}}
    while True:
        new = position[0] + direction[0], position[1] + direction[1]
        if not (0 <= new[0] <= max_x and 0 <= new[1] <= max_y):
            return visited
        if new not in obstacles:
            if new in visited:
                if direction in visited[new]:
                    return {}
                visited[new].add(direction)
            else:
                visited[new] = {direction}
            position = new
        else:
            direction = -direction[1], direction[0]

visited = get_visited(start, obstacles)
print('Part 1:', len(visited))

loops = sum(1 for x, y in visited if not get_visited(start, obstacles | {(x, y)}) if (x, y) != start)
print('Part 2:', loops)

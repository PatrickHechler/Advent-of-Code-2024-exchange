#
# see: https://adventofcode.com/2024/day/10
# 
 
from time import time
from collections import deque

__startTime = time()

def loadInput(isTest = True):
    global __startTime

    if isTest:
        filename = f"exchange/day{DAY}/claudia/input-example.txt"
    else:
        filename = f"exchange/day{DAY}/claudia/input.txt"

    with open(filename) as f:
        content = [l[:-1] if l[-1] == "\n" else l for l in f.readlines()]

    return content


def writeSolutionFile(part, solution):
    filename = f"exchange/day{DAY}/claudia/solution-for-input.txt"
    parameter = "w" if part == 1 else "a"

    with open(filename, parameter) as f:
        f.write(f"Part {part}: {solution}\n")


def printTimeTaken():
    global __startTime
    __endTime = time()
    print("Time: {:.3f}s".format(__endTime-__startTime))

print()

#########################################################################################
# Day 10
#########################################################################################
DAY = "10"


def find_trails(grid, current_trail, trails):
    
    directions = [(-1,0), (1,0), (0,1), (0,-1)]
    
    last_position = current_trail[-1]
    
    for r, c in directions:
        new_row = last_position[0] + r
        new_col= last_position[1] + c
        
        # Check if new position is in grid
        if new_row < 0 or new_row >= len(grid) or new_col < 0 or new_col >= len(grid):
            continue
            
        # Check if new position has right value (old value + 1)
        if grid[last_position[0]][last_position[1]] != grid[new_row][new_col]-1:
            continue
        
        # Add position to current trail
        current_trail_copy = current_trail.copy()
        current_trail_copy.append((new_row, new_col))

        if len(current_trail_copy) == 10:
            trails.append(current_trail_copy)
        
        # Create new current_trail array for every direction
        find_trails(grid, current_trail_copy, trails)
        
    return trails

    
def doAllParts(part, isTest = True):
    data = loadInput(isTest)

    topoMap = [[int(data[r][c]) for c in range(len(data[0]))] for r in range(len(data))]
    
    if isTest:
        for r in range(len(topoMap)):
            print("".join([str(topoMap[r][c]) for c in range(len(data[r]))]))
        print()

    trails = []
    uniqueStartEnd = set()
    
    for r in range(len(topoMap)):
        for c in range(len(topoMap[r])):
            if topoMap[r][c] == 0:
                trails = find_trails(topoMap, [(r,c)], trails)
    
    for trail in trails:
        uniqueStartEnd.update([(trail[0], trail[-1])])
    
    if part == 1:
        result = len(uniqueStartEnd)
    else:
        result = len(trails)

    if not isTest:
        if part == 1:
            writeSolutionFile(1, result)
        else:
            writeSolutionFile(2, result)
            
    return result

            
#########################################################################################

print("--- PART 1 ---")
print(f"Solution Example: {doAllParts(1)}")
print(f"Solution Part 1:  {doAllParts(1, False)}")


print("\n==============\n")
print("--- PART 2 ---")
print(f"Solution Example: {doAllParts(2)}")
print(f"Solution Part 2:  {doAllParts(2, False)}")

#########################################################################################
print()
printTimeTaken()

#
# see: https://adventofcode.com/2024/day/8
# 
 
from time import time
from itertools import combinations
from operator import itemgetter


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
    parameter = "w" if part==1 else "a"

    with open(filename, parameter) as f:
        f.write(f"Part {part}: {solution}\n")


def printTimeTaken():
    global __startTime
    __endTime = time()
    print("Time: {:.3f}s".format(__endTime-__startTime))

print()

#########################################################################################
# Day 08
#########################################################################################
DAY="08"

def createGrid(part = 1, isTest = True):
    grid = loadInput(isTest)
    
    for r in range(len(grid)):
        grid[r] = [g for g in grid[r]]
    
    if isTest and part == 1:
        print("\nStart-GRID")
        showGrid(grid)
        print()

    for r in range(len(grid)):
        grid[r] = [grid[r][c] for c in range(len(grid[r]))]
    return grid


def showGrid(grid):
    print()
    for r in range(len(grid)):
        print("".join(grid[r]))
    print()


def doAllParts(part = 1, isTest = True):
    grid = createGrid(part, isTest)

    result = 0

    antinodes = set()
    antennas = {}
    listAntennas = []

    for r in range(len(grid)):
        for c in range(len(grid[0])):
            if grid[r][c] != ".":
                if grid[r][c] in antennas:
                    antennas[grid[r][c]] += [(r,c)]
                    listAntennas += [grid[r][c]]
                else:
                    antennas[grid[r][c]] = [(r,c)]
                    listAntennas = [grid[r][c]]

    for a in antennas:
        
        for a1,a2 in sorted(combinations(antennas[a], 2), key=itemgetter(0)):
            diffR=a2[0] - a1[0]
            diffC=a2[1] - a1[1]
            
            if part == 1:
                pos = a1
                pos = (pos[0]-diffR, pos[1]-diffC)
                if pos[0] in range(len(grid)) and pos[1] in range(len(grid[0])):
                    antinodes.add(pos)
                    if grid[pos[0]][pos[1]] == ".":
                        grid[pos[0]][pos[1]] = "#"

                pos=a2
                pos = (pos[0] + diffR, pos[1] + diffC)
                if pos[0] in range(len(grid)) and pos[1] in range(len(grid[0])):
                    antinodes.add(pos)
                    if grid[pos[0]][pos[1]] == ".":
                        grid[pos[0]][pos[1]] = "#"
                    antinodes.add(pos)
            
            else:
                pos=a1

                while True:
                    pos = (pos[0] - diffR, pos[1] - diffC)
                    if pos[0] in range(len(grid)) and pos[1] in range(len(grid[0])):
                        antinodes.add(pos)
                        if grid[pos[0]][pos[1]] == ".":
                            grid[pos[0]][pos[1]] = "#"
                    else:
                        break
                
                pos = a2

                while True:
                    pos = (pos[0] + diffR, pos[1] + diffC)
                    if pos[0] in range(len(grid)) and pos[1] in range(len(grid[0])):
                        antinodes.add(pos)
                        if grid[pos[0]][pos[1]] == ".":
                            grid[pos[0]][pos[1]] = "#"
                    else:
                        break

    if isTest:
        showGrid(grid)

    if part == 1:
        result = len(antinodes)
    if part == 2:
        result = len([grid[r][c] for r in range(len(grid)) for c in range(len(grid[0])) if grid[r][c] not in ["."]])

    if not isTest:
        writeSolutionFile(part, result)

    return result 


#########################################################################################

print("--- PART 1 ---")
print(f"Solution Example: {doAllParts()}")
print(f"Solution Part 1:  {doAllParts(1, False)}")


print("\n==============\n")
print("--- PART 2 ---")
print(f"Solution Example: {doAllParts(2)}")
print(f"Solution Part 2:  {doAllParts(2, False)}")

#########################################################################################
print()
printTimeTaken()

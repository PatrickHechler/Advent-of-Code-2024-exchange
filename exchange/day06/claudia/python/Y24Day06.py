#
# see: https://adventofcode.com/2024/day/6
# 
 
from time import time

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

####################################################################re#####################
# Day 06
#########################################################################################
DAY = "06"

def createGrid(isTest = True):
    grid = loadInput(isTest)
    
    for r in range(len(grid)):
        grid[r] = [g for g in grid[r]]
    
    if isTest:
        print("\nStart-GRID")
        showGrid(grid)
        print()

    for r in range(len(grid)):
        grid[r] = [grid[r][c] for c in range(len(grid[r]))]
    return grid


def doWalk(grid, startPos, d, isTest, part=1):
    pos = startPos
    visited = {(pos, d):1}
    countLoop = 0
    height = len(grid)
    width = len(grid[0])
    grid[startPos[0]][startPos[1]] = "."
    
    while True:
        # get next position
        newPos = (pos[0] + directions[d][0], pos[1] + directions[d][1])
        
        if newPos[0] in [-1, width] or newPos[1] in [-1, height]:
            # exit of the grid - no loop
            if isTest and part==1:
                print()
                grid[pos[0]][pos[1]] = directions[d][2]
                if isTest:
                    showGrid(grid)
                grid[pos[0]][pos[1]] = "."
            break

        elif grid[newPos[0]][newPos[1]] == "#" :
            # step not possible due to rock
            if isTest and part==1:
                print()
                grid[pos[0]][pos[1]] = directions[d][2]
                if isTest:
                    showGrid(grid)
                grid[pos[0]][pos[1]] = "."
                
            # change direction
            d = (d+1)%4
            
        else:
            pos = (pos[0] + directions[d][0], pos[1] + directions[d][1]) 
            
            if (pos, d) not in visited:
                # for Part 1 necessary
                if (pos, d) in visited:
                    visited[(pos, d)] += 1
                else:
                    visited[(pos, d)] = 1
            
            else:
                # for Part 2 necessary
                countLoop += 1
                break
    
    return visited, countLoop


def showVisitedGrid(grid, pointsOfWay):
    print()
    # mark visited positions with X
    for p in pointsOfWay:
        grid[p[0]][p[1]]="X"
    for r in range(len(grid)):
        print("".join(grid[r]))
    # undo changes in grid
    for p in pointsOfWay:
        grid[p[0]][p[1]]="."
    print()
    

def showGrid(grid):
    print()
    for r in range(len(grid)):
        print("".join(grid[r]))
    print()
    

def doPart1(grid, startPos, isTest = True):
    if isTest:
        print("Startposition:", startPos)
        print("Startdirection:", directions[0][2], startDirection)
    visited, _ = doWalk(grid, startPos, startDirection, isTest)
    visited = {p for p, _ in visited}
    
    if isTest:
        print("\n\nPositions visited by the guard before leaving the area - marked with an X:")
        showVisitedGrid(grid, visited)
        print()

    result = len(visited)    
    
    if not isTest:
        writeSolutionFile(1, result)
    
    return result

             
def doPart2(grid, startPos, isTest = True):
    if isTest:
        print("Startposition:", startPos)
        print("Startdirection:", directions[0][2])
    visited, _ = doWalk(grid, startPos, startDirection, isTest, 2)
    
    result=0
        
    if isTest:
        print()
        print("Additional rock for loop is marked with 0")
                
    for vpos in {p for p, _ in visited if p != startPos}:
        # mark additional rock in grid with #
        grid[vpos[0]][vpos[1]] = "#"
        tmp_result = doWalk(grid, startPos, startDirection,isTest, 2)[1]
        if tmp_result == 1:
            result +=1
            grid[startPos[0]][startPos[1]] = directions[startDirection][2]
            # after finding the loop, change additional rock in grid from # to 0
            grid[vpos[0]][vpos[1]] = "0"
            if isTest:
                showGrid(grid)
        # undo change for additional rock in grid
        grid[vpos[0]][vpos[1]] = "."

    if not isTest:
        writeSolutionFile(2, result)

    return result

#########################################################################################
directions = {
        0: (-1, 0, "^"),     # up
        1: (0, 1, ">"),      # right
        2: (1, 0, "v"),      # down
        3: (0, -1, "<")      # left
    }

# start direction is "up"
startDirection = 0

# grid for example
grid_t = createGrid(True) 

# Startposition
startPos_t = [(x,y) for x in range(len(grid_t)) for y in range(len(grid_t[0])) if grid_t[x][y] == "^"][0]
    
# grid for big file
grid = createGrid(False) 

# Startposition
startPos = [(x,y) for x in range(len(grid)) for y in range(len(grid[0])) if grid[x][y] == "^"][0]


print("--- PART 1 ---")
print(f"Solution Example: {doPart1(grid_t, startPos_t)}")
print(f"Solution Part 1:  {doPart1(grid, startPos, False)}")


print("\n==============\n")
print("--- PART 2 ---")
print(f"Solution Example: {doPart2(grid_t, startPos_t)}")
print(f"Solution Part 2:  {doPart2(grid, startPos, False)}")
#########################################################################################
print()
printTimeTaken()

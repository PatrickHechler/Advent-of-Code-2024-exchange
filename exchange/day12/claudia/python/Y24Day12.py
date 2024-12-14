#
# see: https://adventofcode.com/2024/day/12
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

#########################################################################################
# Day 12
#########################################################################################
DAY = "12"


directions = [(0,1), (1,0), (0,-1), (-1,0)]
    
def getRegion(point, grid):
    dimR=len(grid)
    dimC=len(grid[0])
    
    toCheck = {point}
    checked = set()
    
    while True:
        if len(toCheck) == 0:
            break
        
        rOld, cOld= toCheck.pop()
        
        newPoints = [ (rOld+d[0],cOld+d[1]) for d in directions \
                                            if (rOld+d[0],cOld+d[1]) not in checked \
                                            and rOld+d[0] not in [-1, dimR] \
                                            and cOld+d[1] not in [-1, dimC] \
                                            and grid[rOld+d[0]][cOld+d[1]] == grid[rOld][cOld] ]
        
        checked.add((rOld, cOld))
        [ toCheck.add((rNew, cNew)) for rNew, cNew in newPoints if (rNew, cNew) not in checked ]
                
    return checked


def getPerimeter(points):
    perimeters = set()

    for p in points:
        for d in directions:
            if (p[0] + d[0], p[1] + d[1]) not in points:
                    perimeters.add(((p[0] + d[0], p[1] + d[1]), d))

    return perimeters


def getSides(points):
    sides = {(-1,0): [], (1,0): [], (0,-1): [], (0,1): []}

    for p in points:
        for d in directions:
            newPoint = (p[0] + d[0], p[1] + d[1])
            if newPoint not in points:
                sides[d].append(newPoint)
    
    countSides = 0

    for side in sides:
        if side[0] in [-1,1]:
            # horizontal wall - from region up/down to wall
            vertical = 0
            level = {s[0]:[] for s in sides[side]}
        else:
            # vertical wall - from region left/right to wall
            vertical = 1
        
        level = {s[vertical]:[] for s in sides[side]}
            
        for s in sides[side]:
            level[s[vertical]].append(s[1-vertical])
            level[s[vertical]] = sorted(level[s[vertical]])
        
        for l in level:
            if len(level[l]) >= 1:
                countSides += 1
   
                for i in range(len(level[l])-1):
                    if level[l][i] != level[l][i+1] - 1:
                        # no connected sides even if the level is the same and therefore additional increase of counter
                        countSides += 1

    return countSides


def doAllParts(part, isTest = True):
    data = loadInput(isTest)
    grid = [ [data[r][c]  for c in range(len(data[r]))] for r in range(len(data)) ]

    if isTest and part == 1:
        
        for r in range(len(grid)):
            print("".join(grid[r]))
        print()

    region = {}
    seen = set()

    for r in range(len(grid)):
        for c in range(len(grid[r])):
            if (r,c) not in seen:
                region[(r,c)] = getRegion((r,c), grid)
                seen.update(region[(r,c)])               
            
    result = 0
    area = 0
    perimeter = 0

    for elem in region:
        area = len(region[elem])
        
        if part == 1:
            perimeter = getPerimeter(region[elem])
            result += area * len(perimeter)
            if isTest:
                print(f"A region of {grid[elem[0]][elem[1]]} plants with price {area} * {len(perimeter)} = {area * len(perimeter)}")
        
        else:
            cntSides = getSides(region[elem])
            result += area * cntSides 
            if isTest:
                print(f"A region of {grid[elem[0]][elem[1]]} plants with price {area} * {cntSides} = {area * cntSides}")
            
    if isTest:
        print()


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

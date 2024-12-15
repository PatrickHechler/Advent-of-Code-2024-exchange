#
# see: https://adventofcode.com/2024/day/14
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
# Day 14
#########################################################################################
DAY = "14"

HEIGHT = [ 7, 103]
WIDTH  = [11, 101]

def doStep(robot, isTest):
    if isTest:
        h = HEIGHT[0]
        w = WIDTH[0]
    else:
        h = HEIGHT[1]
        w = WIDTH[1]

    robot[0] += robot[2]
    robot[1] += robot[3]
    robot[0] = robot[0]%w
    robot[1] = robot[1]%h
    return robot


def doAllParts(part, isTest = True):
    data = loadInput(isTest)
    
    if isTest:
        h = HEIGHT[0]
        w = WIDTH[0]
    else:
        h = HEIGHT[1]
        w = WIDTH[1]

    robots = []

    for d in data:
        p, v = d.split(" ")
        px, py = [int(n) for n in p.split("=")[1].split(",")]
        vx, vy = [int(n) for n in v.split("=")[1].split(",")]
       
        robots.append([px, py, vx, vy])    

    if isTest:   
        print(robots)
    
    grid = [['.' for r in range(w)] for c in range(h)]
    
    if isTest:
        tmp_grid = grid.copy()
        tmp_robot = [2, 4, 2, -3]
        
        for i in range(6):
            if i == 0:
                print("Initial state:")
            else:
                tmp_str = "" if i == 1 else "s"
                print(f"After {i} second{tmp_str}:")
                tmp_robot = doStep(tmp_robot, isTest)

            print(tmp_robot, grid)
            print(tmp_grid[tmp_robot[1]])
            tmp_grid[tmp_robot[1]][tmp_robot[0]] = "1" 
            
            for r in range(h):
                print("".join(tmp_grid[r][:w]))
            
            print()
            tmp_grid[tmp_robot[1]][tmp_robot[0]] = "." 
            
        print()
                

    for robot in robots:
        grid[robot[1]][robot[0]] = "1" if grid[robot[1]][robot[0]] == "." else str(int(grid[robot[1]][robot[0]]) + 1)
        
    if isTest:
        print("Initial State of example:")
        for r in range(h):
            print("".join(grid[r][:w]))
        print()
        
    if part == 1:
        ROUNDS = 100
    else:
        ROUNDS = 20000
        stopProcess = False
    
    for roundNumber in range(ROUNDS):
        for i in range(len(robots)):
            # delete old position of robot in grid
            grid[robots[i][1]][robots[i][0]] = "." \
                    if grid[robots[i][1]][robots[i][0]] == "1" \
                    else str(int(grid[robots[i][1]][robots[i][0]]) - 1)

            robots[i] = doStep(robots[i], isTest)
            
            # add new position of robot in grid
            grid[robots[i][1]][robots[i][0]] = "1" \
                if grid[robots[i][1]][robots[i][0]] == "."\
                else str(int(grid[robots[i][1]][robots[i][0]]) + 1)
    
        if part == 2:
            for row in range(h):
                if "1111111111111111111111111111111" in "".join(grid[row]):

                    for r in range(h):
                        print("".join(grid[r]))    
                    print()

                    result = roundNumber + 1

                    stopProcess = True
                    break

            if stopProcess:
                break

    if part == 1: 
        if isTest:
            print("Afer 100 seconds:")
            for r in range(h):
                print("".join(grid[r][:w]))
            print()
        
        q1,q2,q3,q4 = 0,0,0,0
    
        if isTest:
            print("Quadrants:")
    
        for r in range(h//2):
            if isTest or part == 2:
                print("".join(grid[r][:w//2]), " ", "".join(grid[r][w//2+1:w]))
            q1 += sum([int(n) if n !="." else 0 for n in "".join(grid[r][:w//2])]) 
            q2 += sum([int(n) if n !="." else 0 for n in "".join(grid[r][w//2+1:w])])

        if isTest:
            print()

        for r in range(h//2+1, h):
            if isTest:
                print("".join(grid[r][:w//2]), " ", "".join(grid[r][w//2+1:w]))
            q3 += sum([int(n) if n != '.' else 0 for n in "".join(grid[r][:w//2])])
            q4 += sum([int(n) if n != '.' else 0 for n in "".join(grid[r][w//2+1:w])])
    
        result = q1 * q2 * q3 * q4

        if isTest:
            print(f"\nThe quadrants contain {q1}, {q2}, {q3}, and {q4} robot.")
            print(f"Multiplying these together gives a total safety factor of {result}.\n")
        
        
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
#print(f"Solution Example: {doAllParts(2)}")      # no part 2 for example 
print(f"Solution Part 2:  {doAllParts(2, False)}")

#########################################################################################
print()
printTimeTaken()

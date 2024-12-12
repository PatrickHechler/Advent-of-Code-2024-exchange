#
# see: https://adventofcode.com/2024/day/11
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
# Day 11
#########################################################################################
DAY = "11"


def getCountOfStones(stone, rounds, dirCountStones):
    if (stone,rounds) in dirCountStones:
        return dirCountStones[(stone,rounds)]
    
    width = len(str(stone))
   
    if rounds==0:
        countStones = 1
    elif stone==0:
        countStones = getCountOfStones(1, rounds-1, dirCountStones)
    elif width%2 == 0:
        sLeft = int(str(stone)[:width//2])
        sRight = int(str(stone)[width//2:])
                
        countStones = getCountOfStones(sLeft, rounds-1, dirCountStones) + getCountOfStones(sRight, rounds-1, dirCountStones)
    else:
        countStones = getCountOfStones(stone*2024, rounds-1, dirCountStones)

    dirCountStones[(stone, rounds)] = countStones

    return countStones


def doPart1(isTest = True):
    # code for first part => not optimized - only working for part 1 and not for part 2
    # => optimized code for part 2 can also be used for part 1
    data = loadInput(isTest)[0]

    blink=25

    newStones = []
    stones=[int(s) for s in data.split( )]
    if isTest:
        print(f"Initial arrangement: \n{' '.join(map(str, stones))}\n")
    
    for step in range(1, blink+1):
        newStones = []
        for s in stones:
            width = len(str(s))
            
            if s == 0:
                newStones.append(1)
            elif width % 2 == 0:
                newStones.append(int(str(s)[:int(width/2)]))
                newStones.append(int(str(s)[int(width/2):]))
            else:
                newStones.append(s*2024)
            
        stones = newStones.copy()
        
        if step <= 6 and isTest:
            if step == 1:
                strBlink="blink"
            else:
                strBlink="blinks"
            print(f"Afer {step} {strBlink}: \n{' '.join(map(str, stones))}\n")
    
    result = len(stones)

    if not isTest:
        writeSolutionFile(1, result)
    
    return result

            
def doPart2(isTest = True):
    data = loadInput(isTest)[0]

    dirCountStones={}
    rounds = 75
    result = 0
    
    for stone in data.split():
        result += getCountOfStones(int(stone), rounds, dirCountStones)
             
    if not isTest:
        writeSolutionFile(2, result)

    return result

#########################################################################################

print("--- PART 1 ---")
print(f"Solution Example: {doPart1()}")
print(f"Solution Part 1:  {doPart1(False)}")


print("\n==============\n")
print("--- PART 2 ---")
print(f"Solution Example: {doPart2()}")
print(f"Solution Part 2:  {doPart2(False)}")

#########################################################################################
print()
printTimeTaken()

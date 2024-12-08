#
# see: https://adventofcode.com/2024/day/7
# 
 
from time import time

__startTime = time()

def loadInput(isTest = True, part = ""):
    global __startTime

    if isTest:
        filename = f"exchange/day{DAY}/claudia/input-example{part}.txt"
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
# Day 07
#########################################################################################
DAY = "07"


def prepareData(inData):
    tasks = []
    for i in range(len(inData)):
        task = [x for x in inData[i].split(": ")]
        task[0] = int(task[0])
        task[1] = task[1].split()
        task[1] = [int(x) for x in task[1]]
        tasks.append(task)
    return tasks


def checkIsValid(entry, part):
    # no operator to insert
    if len(entry[1]) == 1:
        return entry[0] == entry[1][0]
    
    # replace first missing operator with +
    if checkIsValid([entry[0], [entry[1][0] + entry[1][1]] + entry[1][2:]], part):
        return True
        
    # replace first missing operator with *
    if checkIsValid([entry[0], [entry[1][0] * entry[1][1]] + entry[1][2:]], part):
        return True
    
    # for part 2: replace first missing operator with ||
    if part == 2 and checkIsValid([entry[0], [int(str(entry[1][0]) + str(entry[1][1]))] + entry[1][2:]], part):
        return True
    return False
    

def doAllParts(part, isTest = True):
    result=[0,0]
    
    data = loadInput(isTest)
    if isTest and part == 1:
        for i in range(len(data)):
            print(data[i])
        print()

    equatations = prepareData(data)
    
    for e in equatations:
        if checkIsValid(e, part):
            if part == 1:
                result[0] += e[0]
            else:
                result[1] += e[0]
    
    if not isTest:
        writeSolutionFile(2, result)

    return result
    
#########################################################################################

result_test = doAllParts(1)
result = doAllParts(1, False)

print("--- PART 1 ---")
print(f"Solution Example: {result_test[0]}")
print(f"Solution Part 1:  {result[0]}")

result_test = doAllParts(2)
result = doAllParts(2, False)

print("\n==============\n")
print("--- PART 2 ---")
print(f"Solution Example: {result_test[1]}")
print(f"Solution Part 2:  {result[1]}")

#########################################################################################
print()
printTimeTaken()

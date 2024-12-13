
#
# see: https://adventofcode.com/2024/day/13
# 
 
from time import time
import re
__startTime = time()

def loadInput(isTest = True):
    global __startTime

    if isTest:
        filename = f"C:/Users/User/OneDrive/Claudia/GIT/AoC2024/Advent-of-Code-2024-exchange/exchange/day{DAY}/claudia/input-example.txt"
    else:
        filename = f"C:/Users/User/OneDrive/Claudia/GIT/AoC2024/Advent-of-Code-2024-exchange/exchange/day{DAY}/claudia/input.txt"

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
# Day 13
#########################################################################################
DAY = "13"



def doAllParts(part, isTest = True):
    costA=3
    costB=1

    data = loadInput(isTest)
    
    clawMachines = ("\n".join(data)).split("\n\n")
    
    costs = 0

    for cM in clawMachines:
        A, B, P = cM.split("\n")
    
        if part == 2:
            toAdd=10000000000000
        else:
            toAdd=0
    
        A = [*map(lambda i: int(i[2:]), A.split(": ")[1].split(", "))]
        B = [*map(lambda i: int(i[2:]), B.split(": ")[1].split(", "))]
        P = [*map(lambda i: int(i[2:])+toAdd, P.split(": ")[1].split(", "))]
       
        # anzA * A0 + anzB * B0 = P0 <=> anzB = (P0 - anzA * A0) / B0
        #
        # anzA * A1 + anzB * B1 = P1 <=> anzA * A1 = P1 - anzB * B1  
        #                            <=> anzA * A1 = P1 - ( P0 - anzA * A0 ) / B0 ) / B1
        #                            <=> anzA * A1 * B0 = P1 * B0 - ( P0 - anzA * A0 ) * B1
        #                            <=> anzA * A1 * B0 = P1 * B0 - P0 * B1 + anzA * A0 * B1 
        #                            <=> anzA * (A1 * B0 - A0 * B1) = P1 * B0 - P0 * B1
        #                            <=> anzA = ( P1 * B0 - P0 * B1) / ( A1 * B0 - A0 * B1)
        
        anzA = (P[1] * B[0] - P[0] * B[1]) / (A[1] * B[0] - A[0] * B[1])
        anzB = (P[0] - anzA * A[0]) / B[0]

        if anzA.is_integer() and anzB.is_integer() \
            and ( (100 >= anzA >= 0 and 100 >= anzB >= 0 and part == 1) or part == 2 ):
                
            costs += int(anzA) * 3 + int(anzB)
            if isTest:
                print("claw machine:")
                print(cM)
                if part == 2:
                    print(f"Prize was changed for Part 1 (+{toAdd})")
                    print(f"=> Cheapest way to win the prize is by pushing the A button {anzA} times and the B button {anzB} times.")
                    print(f"   Doing this would cost a total of {int(anzA) * costA + int(anzB) * costB} tokens.\n")
        elif isTest:
            print("claw machine:")
            print(cM)
            print("=> There is no combination of A and B presses that will ever win a prize.\n")

    result = costs

    if not isTest:
        if part == 1:
            writeSolutionFile(1, result)
        else:
            writeSolutionFile(2, result)

    return result
             

#########################################################################################
print("--- PART 1 ---")
print(f"Solution Example: {doAllParts(1)}")
print(f"Solution Part 1:  {doAllParts(1,False)}")


print("\n==============\n")
print("--- PART 2 ---")
print(f"Solution Example: {doAllParts(2)}")
print(f"Solution Part 2:  {doAllParts(2,False)}")

#########################################################################################
print()
printTimeTaken()

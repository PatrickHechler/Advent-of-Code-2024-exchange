#!/usr/bin/env python3

with open('day5.txt') as f:
    rules, updates, is_rules = {}, [], True
    for line in f:
        if not line.strip():
            is_rules = False
            continue
        if is_rules:
            before, after = map(int, line.split('|'))
            rules.setdefault(before, [[], []])[0].append(after)
            rules.setdefault(after, [[], []])[1].append(before)
        else:
            updates.append(list(map(int, line.split(','))))

def check_rule(numberlist, rule):
    return next((number for number in numberlist if number in rule), None)

def check_update(update):
    for i, number in enumerate(update):
        if check_rule(update[i+1:], rules[number][1]) != None  or check_rule(update[:i], rules[number][0]) != None:
            return False
    return True

print('Part 1:', sum(updates[i][len(updates[i])//2] if check_update(u) else 0 for i, u in enumerate(updates)))

def sort_update(i):
    update = updates[i]
    while not check_update(update):
        for j, n in enumerate(update):
            wrong = check_rule(update[j+1:], rules[n][1]) or check_rule(update[:j], rules[n][0])
            if wrong:
                w = update.index(wrong)
                update[j], update[w] = update[w], update[j]
                break
    return update

print('Part 2:', sum(sort_update(i)[len(updates[i])//2] if not check_update(u) else 0 for i, u in enumerate(updates))) 
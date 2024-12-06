use std::fs::File;
use std::io::BufRead;
use std::path::Path;
use std::{env, io};
use std::collections::{HashMap, HashSet};

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day05 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let mut lines = reader.lines();

    // Load and parse input from file
    let mut ordering_rules: HashMap<i32, Vec<i32>> = HashMap::new();
    for l in &mut lines {
        let line = l.unwrap();
        if line.len() == 0 {
            break;
        }

        let rule = line.split('|').map(|s| s.parse::<i32>().unwrap()).collect::<Vec<i32>>();
        if !ordering_rules.contains_key(&rule[1]) {
            ordering_rules.insert(rule[1], Vec::new());
        }
        ordering_rules.get_mut(&rule[1]).unwrap().push(rule[0]);
    }
    let mut updates: Vec<Vec<i32>> = Vec::new();
    for l in lines {
        let line = l.unwrap();
        if line.len() == 0 {
            break;
        }
        updates.push(line.split(',').map(|s| s.parse::<i32>().unwrap()).collect::<Vec<i32>>());
    }

    // Task 1: Determine which updates are already in the correct order.
    // What do you get if you add up the middle page number from those correctly-ordered updates?
    let mut valid = 0;
    let mut sum = 0;
    for i in 0..updates.len() {
        if is_valid_update(&updates[i], &ordering_rules) {
            let j = updates[i].len() / 2;
            let middle_page = updates[i][j];
            valid += 1;
            sum += middle_page
        }
    }
    println!("Task 1: Valid updates={}, Sum of middle page numbers={}", valid, sum);

    // Task 2: Find the updates which are not in the correct order.
    // What do you get if you add up the middle page numbers after correctly ordering just those updates?
    let mut valid = 0;
    let mut sum = 0;
    for i in 0..updates.len() {
        if !is_valid_update(&updates[i], &ordering_rules) {
            let reordered_update = order(&updates[i], &ordering_rules);
            let j = reordered_update.len() / 2;
            let middle_page = reordered_update[j];
            valid += 1;
            sum += middle_page
        }
    }
    println!("Task 2: Corrected updates={}, Sum of middle page numbers={}", valid, sum);

    Ok(())
}

fn is_valid_update(update: &Vec<i32>, ordering_rules: &HashMap<i32, Vec<i32>>) -> bool {
    let mut printed: HashSet<i32> = HashSet::new();
    for i in 0..update.len() {
        let current_page = update[i];
        printed.insert(current_page);
        if !ordering_rules.contains_key(&current_page) {
            continue;
        }
        let rules = ordering_rules.get(&current_page).unwrap();
        for before_page in rules {
            if !update.contains(before_page) {
                continue;
            }
            if !printed.contains(before_page) {
                return false
            }
        }
    }
    true
}


fn order(update: &Vec<i32>, ordering_rules: &HashMap<i32, Vec<i32>>) -> Vec<i32> {
    let mut printed: HashSet<i32> = HashSet::new();
    let mut reordered: Vec<i32> = update.clone();

    let mut i = 0;
    'outer: while i < reordered.len() {
        let current_page = reordered[i];
        if !ordering_rules.contains_key(&current_page) {
            printed.insert(current_page);
            i += 1;
            continue 'outer;
        }
        let rules = ordering_rules.get(&current_page).unwrap();
        'inner: for before_page in rules {
            if !update.contains(before_page) {
                continue 'inner;
            }
            if !printed.contains(before_page) {
                for j in 0..reordered.len() {
                    let exchange_page = reordered[j];
                    if *before_page == exchange_page {
                        reordered[j] = reordered[i];
                        reordered[i] = exchange_page;
                        continue 'outer;
                    }
                }
                break 'inner;
            }
        }
        printed.insert(current_page);
        i += 1;
    }
    
    reordered
}

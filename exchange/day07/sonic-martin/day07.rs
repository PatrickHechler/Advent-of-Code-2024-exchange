use std::fs::File;
use std::io::BufRead;
use std::path::Path;
use std::{env, io};

const ADD: u32 = 0;
const MUL: u32 = 1;
const CAT: u32 = 2;

struct Equation {
    test_value: i64,
    numbers: Vec<i64>,
}

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day07 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let lines = reader.lines();

    // Load and parse input from file
    let mut equations: Vec<Equation> = Vec::new();
    for l in lines {
        let line = l.unwrap();
        let sides = line.split_once(": ").unwrap();
        let test_value: i64 = sides.0.parse().unwrap();
        let numbers: Vec<i64> = sides.1.split_whitespace().map(|x| x.parse().unwrap()).collect();
        equations.push(Equation {
            test_value,
            numbers,
        })
    }
    

    // Task 1: Determine which equations could possibly be true.
    // What is their total calibration result?
    // Only ADD and MUL operators.
    let mut calibration_result1: i64 = 0;
    for i in 0..equations.len() {
        if is_valid(&equations[i], 2) {
            calibration_result1 += equations[i].test_value;
        }
    }
    println!("Task 1: {}", calibration_result1);

    // Task 2: ADD, MUL and CAT operators.
    let mut calibration_result2: i64 = 0;
    for i in 0..equations.len() {
        if is_valid(&equations[i], 3) {
            calibration_result2 += equations[i].test_value;
        }
    }
    println!("Task 2: {}", calibration_result2);

    Ok(())
}

// is_valid checks if a given equation with the given operator alphabet is valid.
// The algorithm validates every possible combination.
// Thus, the run time is long (around 1 minute).
fn is_valid(eq: &Equation, alphabet: u32) -> bool {
    let num_of_numbers: u32 = eq.numbers.len() as u32;
    let num_of_operators: u32 = num_of_numbers - 1;
    let combinations = generate_combinations(num_of_operators, alphabet);
    
    for combination in combinations {
        let mut result: i64 = eq.numbers[0];
        for operator_index in 0..combination.len() {
            let operator = combination[operator_index];
            let number_index = operator_index + 1;
            let number = eq.numbers[number_index];
            match operator {
                ADD => result += number,
                MUL => result *= number,
                CAT => {
                    let left = result.to_string();
                    let right = number.to_string();
                    let concatenated = format!("{}{}", left, right);
                    result = concatenated.parse::<i64>().unwrap();
                },
                _  => {}
            }
        }
        if result == eq.test_value {
            return true;
        }
    }

    false
}

// generate_combinations generates via a recursive algorithm all possible combinations
// of n-digits with an alphabet of m-numbers.
fn generate_combinations(digits: u32, alphabet: u32) -> Vec<Vec<u32>> {
    let mut combinations: Vec<Vec<u32>> = Vec::new();
    
    // Base case
    if digits == 1 {
        for new_number in 0..alphabet {
            combinations.push(vec![new_number]);
        }
        return combinations;
    }

    // Recursive case
    let subsequent_combinations = generate_combinations(digits - 1, alphabet);
    let mut combinations: Vec<Vec<u32>> = Vec::new();
    for new_number in 0..alphabet {
        for i in 0..subsequent_combinations.len() {
            let mut new: Vec<u32> = subsequent_combinations[i].clone();
            new.push(new_number);
            combinations.push(new);
        }
    }

    combinations
}
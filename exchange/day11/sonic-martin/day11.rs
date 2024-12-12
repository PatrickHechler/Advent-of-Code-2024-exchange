// For that one I missed to find the optimal, yet simple, algorithm and took a look into the Reddit.
// Thus, this implementation is heavily inspired by this one:
// https://nickymeuleman.netlify.app/blog/aoc2024-day11/
use std::collections::HashMap;
use std::path::Path;
use std::{env, fs};

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day11 INPUT"));
    }

    // Load input file into string
    let input_path = Path::new(&args[1]);
    let input = fs::read_to_string(input_path).map_err(|e| e.to_string())?;

    // Parse input
    let mut stones: HashMap<String, u128> = HashMap::new();
    for stone in input.split_whitespace().map(String::from) {
        stones.entry(stone).or_insert(1);
    }

    // Task 1: How many stones will you have after blinking 25 times?
    for _ in 0..25 {
        stones = blink(&stones);
    }
    let task1_sum = stones.values().sum::<u128>();
    println!("Task 1: {} stones after 25 iterations", task1_sum);

    // Task 2: How many stones would you have after blinking a total of 75 times?
    for _ in 0..50 {
        stones = blink(&stones);
    }
    let task2_sum = stones.values().sum::<u128>();
    println!("Task 2: {} stones after 75 iterations", task2_sum);

    Ok(())
}

fn blink(stones: &HashMap<String, u128>) -> HashMap<String, u128> {
    let mut result: HashMap<String, u128> = HashMap::new();

    for (stone, amount) in stones {
        if stone == "0" {
            let new = String::from("1");
            *result.entry(new).or_default() += amount;
        } else if stone.len() % 2 == 0 {
            let middle = stone.len() / 2;
            let left: u128 = stone[..middle].parse().unwrap();
            let right: u128 = stone[middle..].parse().unwrap();
            let new_left = left.to_string();
            let new_right = right.to_string();
            *result.entry(new_left).or_default() += amount;
            *result.entry(new_right).or_default() += amount;
        } else {
            let original: u128 = stone.parse().unwrap();
            let new_num: u128 = original * 2024;
            let new = new_num.to_string();
            *result.entry(new).or_default() += amount;
        }
    }

    result
}

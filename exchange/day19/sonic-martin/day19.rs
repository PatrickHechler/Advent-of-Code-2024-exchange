// Initially I went for a recursive approach. I spotted that this is also about
// dynamic programming.
// But I spent quite some time, because I put the cache insert at the wrong place
// in the recursive approach, I tried a non-recursive approach without success.
// After getting a hint[1], I came back for the recursive approach.
//
// [1] https://nickymeuleman.netlify.app/blog/aoc2024-day19/

use std::fs::File;
use std::io::BufRead;
use std::path::Path;
use std::{env, io};
use std::collections::{HashMap};

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day19 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let mut lines = reader.lines();

    // Load and parse input from file
    let mut patterns = lines.next().unwrap().unwrap().split(", ").map(|s| s.to_string()).collect::<Vec<_>>();
    lines.next(); // skip empty line
    let designs= lines.collect::<Result<Vec<_>, _>>().unwrap();

    // Sort patterns according to their length, descending
    patterns.sort_by( |a, b| b.len().cmp(&a.len()));

    let mut count1 = 0; // Task 1: How many designs are possible?
    let mut count2 = 0; // Task 2: What do you get if you add up the number of different ways you could make each design?
    let mut cache = HashMap::new();
    for design in designs.iter() {
        println!("Design: {}", design);
        let n = valid_solutions(design.to_owned(), &patterns, &mut cache);
        if n > 0 {
            println!(" -> valid");
            count1 += 1;
            count2 += n;
        }

    }
    println!("Task 1: {}", count1);
    println!("Task 2: {}", count2);

    Ok(())
}

fn valid_solutions(design: String, patterns: &Vec<String>, cache: &mut HashMap<String, u64>) -> u64 {
    if design.is_empty() {
        return 1
    }

    if cache.contains_key(&design) {
        return *cache.get(&design).unwrap();
    }

    let mut n = 0;
    for pattern in patterns.iter() {
        if design.starts_with(pattern) {
            let subdesign = design[pattern.len()..].to_string();
            n += valid_solutions(subdesign, patterns, cache);
        }
    }
    
    cache.insert(design, n);

    n
}

use std::collections::{HashMap, HashSet};
use std::fs::File;
use std::io::BufRead;
use std::path::Path;
use std::{env, io};

#[derive(Clone, Debug, Eq, Hash, PartialEq)]
struct Point {
    x: i64,
    y: i64,
}

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day08 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let lines = reader.lines().collect::<Result<Vec<_>, _>>().unwrap();

    // Load and parse input from file
    let max_row_index = (lines.len() as i64) - 1;
    let max_column_index = (lines[0].len() as i64) - 1;
    let mut antennas: HashMap<char, Vec<Point>> = HashMap::new();
    for y in 0..lines.len() {
        let row = lines[y].chars().collect::<Vec<_>>();
        for x in 0..row.len() {
            let frequency = row[x];
            if frequency == '.' {
                continue;
            }
            antennas.entry(frequency).or_insert(vec![]).push(Point {
                x: x as i64,
                y: y as i64,
            });
        }
    }

    // Task 1: How many unique locations within the bounds of the map contain an antinode?
    let mut antinodes: HashSet<Point> = HashSet::new();
    for frequency in antennas.keys() {
        let ants = antennas.get(&frequency).unwrap();
        for i in 0..ants.len() {
            for j in 0..ants.len() {
                if i == j {
                    continue;
                }

                let ant1 = &ants[i];
                let ant2 = &ants[j];

                let dx = (ant1.x - ant2.x).abs();
                let dy = (ant1.y - ant2.y).abs();

                let mut anti1 = Point { x: 0, y: 0 };
                let mut anti2 = Point { x: 0, y: 0 };

                if ant1.x < ant2.x {
                    anti1.x = ant1.x - dx;
                    anti2.x = ant2.x + dx;
                } else {
                    anti1.x = ant1.x + dx;
                    anti2.x = ant2.x - dx;
                }
                if ant1.y < ant2.y {
                    anti1.y = ant1.y - dy;
                    anti2.y = ant2.y + dy;
                } else {
                    anti1.y = ant1.y + dy;
                    anti2.y = ant2.y - dy;
                }

                if anti1.x >= 0
                    && anti1.x <= max_column_index
                    && anti1.y >= 0
                    && anti1.y <= max_row_index
                {
                    antinodes.insert(anti1);
                }
                if anti2.x >= 0
                    && anti2.x <= max_column_index
                    && anti2.y >= 0
                    && anti2.y <= max_row_index
                {
                    antinodes.insert(anti2);
                }
            }
        }
    }
    println!("Task 1: {:?}", antinodes.len());    

    // Task 2: Calculate the impact of the signal using this updated model.
    // How many unique locations within the bounds of the map contain an antinode?
    let mut antinodes: HashSet<Point> = HashSet::new();
    for frequency in antennas.keys() {
        let ants = antennas.get(&frequency).unwrap();
        for i in 0..ants.len() {
            for j in 0..ants.len() {
                if i == j {
                    continue;
                }

                let ant1 = &ants[i];
                let ant2 = &ants[j];

                let dx = (ant1.x - ant2.x).abs();
                let dy = (ant1.y - ant2.y).abs();

                let mut anti = Point { x: ant1.x, y: ant1.y };
                loop {
                    if ant1.x < ant2.x {
                        anti.x -= dx;
                    } else {
                        anti.x += dx;
                    }
                    if ant1.y < ant2.y {
                        anti.y -= dy;
                    } else {
                        anti.y += dy;
                    }
                    if anti.x >= 0
                        && anti.x <= max_column_index
                        && anti.y >= 0
                        && anti.y <= max_row_index
                    {
                        antinodes.insert(anti.clone());
                    } else {
                        break;
                    }
                }

                let mut anti = Point { x: ant2.x, y: ant2.y };
                loop {
                    if ant1.x < ant2.x {
                        anti.x -= dx;
                    } else {
                        anti.x += dx;
                    }
                    if ant1.y < ant2.y {
                        anti.y -= dy;
                    } else {
                        anti.y += dy;
                    }
                    if anti.x >= 0
                        && anti.x <= max_column_index
                        && anti.y >= 0
                        && anti.y <= max_row_index
                    {
                        antinodes.insert(anti.clone());
                    } else {
                        break;
                    }
                }
                
            }
        }
    }
    println!("Task 2: {:?}", antinodes.len());

    Ok(())
}

use std::collections::{HashMap, HashSet};
use std::fs::File;
use std::io::BufRead;
use std::path::Path;
use std::{env, io};

const UP: char = '^';
const DOWN: char = 'v';
const LEFT: char = '<';
const RIGHT: char = '>';
const OBSTRUCTION: char = '#';
const VISITED: char = 'X';
//const EMPTY: char = '.';

type Point = (usize, usize);
const INVALID: Point = (usize::MAX, usize::MAX);

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day6 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let lines = reader.lines();

    // Load and parse input from file
    let mut map: Vec<Vec<char>> = Vec::new();
    for row in lines {
        let columns = row.unwrap().chars().collect::<Vec<char>>();
        map.push(columns);
    }
    let map_max_rows = map.len() - 1;
    let map_max_columns = map[0].len() - 1;

    // Find starting point
    let mut starting_pos: Point = INVALID;
    'find_starting_pos: for row in 0..map.len() {
        for col in 0..map[row].len() {
            if map[row][col] == DOWN
                || map[row][col] == UP
                || map[row][col] == RIGHT
                || map[row][col] == LEFT
            {
                starting_pos = (row, col);
                break 'find_starting_pos;
            }
        }
    }

    // Task 1: Predict the path of the guard.
    // How many distinct positions will the guard visit before leaving the mapped area?
    let visited = walk(starting_pos, &map).unwrap();
    println!("Task 1: {}", visited.len());

    // Task 2: You need to get the guard stuck in a loop by adding a single new obstruction.
    // How many different positions could you choose for this obstruction?
    //
    // Remark 1: This is a brute-force approach, it tries to walk every possible map.
    // To reduce the options, it only tries to place obstructions along the initially (Task 1)
    // walked way and adjacent fields of the walked way.
    // Still the execution takes a minute.
    // Remark 2: Thanks to Kevin for that algorithmic hint.
    let mut all_options: HashSet<Point> = HashSet::new();
    let mut valid_options: i32 = 0;
    for pos in visited {
        all_options.insert(pos);
        all_options.insert(next(pos, UP));
        all_options.insert(next(pos, RIGHT));
        all_options.insert(next(pos, DOWN));
        all_options.insert(next(pos, LEFT));
    }
    for pos in all_options {
        // The new obstruction can't be placed at the guard's starting position - the guard is there right now and would notice.
        if pos == starting_pos {
            continue;
        }
        // Out of bounds
        if pos.0 > map_max_rows || pos.1 > map_max_columns {
            continue;
        }
        // There is already an Obstruction
        if map[pos.0][pos.1] == OBSTRUCTION {
            continue;
        }

        let mut changed_map = map.clone();
        changed_map[pos.0][pos.1] = OBSTRUCTION;
        let v = walk(starting_pos, &changed_map); // Returns None for a loop.

        if v.is_none() {
            valid_options += 1;
        }
    }
    println!("Task 2: {}", valid_options);

    Ok(())
}

// walk follows the way of the guard on the map.
// For a non-infinite/non-loop walk it returns the walked way.
// For a loop, it aborts the walk after the same position was visited 5 times.
// Then walk returns None, indicating that this map has a loop.
fn walk(starting_pos: Point, starting_map: &Vec<Vec<char>>) -> Option<Vec<Point>> {
    let mut map = starting_map.clone();
    let map_max_rows = map.len() - 1;
    let map_max_columns = map[0].len() - 1;

    let mut pos = starting_pos;
    let mut visited: HashMap<Point, i32> = HashMap::new();

    'main: loop {
        let mut next_pos;

        'next: loop {
            next_pos = next(pos, map[pos.0][pos.1]);

            // Out of bounds
            if next_pos.0 > map_max_rows || next_pos.1 > map_max_columns {
                break 'main;
            }

            // No Obstruction
            if map[next_pos.0][next_pos.1] != OBSTRUCTION {
                break 'next;
            // Obstruction
            } else {
                map[pos.0][pos.1] = rotate(map[pos.0][pos.1]);
            }
        }

        *visited.entry(pos).or_insert(0) += 1;
        map[next_pos.0][next_pos.1] = map[pos.0][pos.1];
        map[pos.0][pos.1] = VISITED;

        // Loop detected.
        if *visited.entry(pos).or_default() == 5 {
            return None;
        }

        pos = next_pos;
    }
    *visited.entry(pos).or_insert(0) += 1;
    map[pos.0][pos.1] = VISITED;

    Some(visited.keys().cloned().collect::<Vec<Point>>())
}

fn next(current_pos: Point, direction: char) -> Point {
    let next = match direction {
        UP => (current_pos.0.checked_sub(1), Some(current_pos.1)),
        DOWN => (Some(current_pos.0 + 1), Some(current_pos.1)),
        LEFT => (Some(current_pos.0), current_pos.1.checked_sub(1)),
        RIGHT => (Some(current_pos.0), Some(current_pos.1 + 1)),
        _ => {
            panic!("Unknown direction")
        }
    };
    match next {
        (Some(x), Some(y)) => (x, y),
        (None, Some(..)) | (Some(..), None) => (usize::MAX, usize::MAX),
        (None, None) => (usize::MAX, usize::MAX),
    }
}

fn rotate(current: char) -> char {
    match current {
        UP => RIGHT,
        RIGHT => DOWN,
        DOWN => LEFT,
        LEFT => UP,
        _ => panic!("Unknown direction"),
    }
}

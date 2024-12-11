use std::collections::HashSet;
use std::fs::File;
use std::hash::Hash;
use std::io::BufRead;
use std::path::Path;
use std::{env, io};

#[derive(Clone, Debug, Hash, Eq, PartialEq)]
struct Point {
    x: usize,
    y: usize,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct Node {
    pos: Point,
    height: u32,
    neighbours: Vec<Point>,
}

impl Hash for Node {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        self.pos.hash(state);
    }
}

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day10 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let lines = reader.lines().collect::<Result<Vec<_>, _>>().unwrap();

    // Load and parse input from file
    let mut map: Vec<Vec<Node>> = Vec::new();
    let mut starting_points: Vec<Point> = Vec::new();
    for y in 0..lines.len() {
        let line = lines[y].chars().collect::<Vec<_>>();
        let mut row: Vec<Node> = Vec::new();
        for x in 0..line.len() {
            let height = line[x].to_digit(10).unwrap();
            let node = Node {
                pos: Point { x, y },
                height,
                neighbours: vec![],
            };
            if height == 0 {
                starting_points.push(Point { x, y });
            }
            row.push(node);
        }
        map.push(row);
    }

    // Build a graph
    // Already only connect nodes when height fits into the slope
    for y in 0..map.len() {
        for x in 0..map[y].len() {
            let mut options: Vec<Point> = Vec::new();
            // Up
            if y > 0 {
                options.push(Point { x, y: y - 1 });
            }
            // Down
            if y + 1 < map.len() {
                options.push(Point { x, y: y + 1 });
            }
            // Left
            if x > 0 {
                options.push(Point { x: x - 1, y });
            }
            // Right
            if x + 1 < map[y].len() {
                options.push(Point { x: x + 1, y });
            }
            // Only add neighbours if the height fits into the slope
            for n in options {
                let neighbour = &map[n.y][n.x];
                if (map[y][x].height + 1) == neighbour.height {
                    map[y][x].neighbours.push(n);
                }
            }
        }
    }

    // Find trails starting on height=0 and ending with height=9
    let mut trails: Vec<Vec<Point>> = Vec::new();
    for start_point in starting_points {
        let start_node = &map[start_point.y][start_point.x];
        let new_trails = walk(&map, &start_node);
        for trail in new_trails {
            let first = trail.first().unwrap();
            let last = trail.last().unwrap();
            if map[first.y][first.x].height == 0 && map[last.y][last.x].height == 9 {
                trails.push(trail);
            }
        }
    }

    // Task 1: The reindeer gleefully carries over a protractor and adds it to the pile.
    // What is the sum of the scores of all trailheads on your topographic map?
    let mut unique_trails: HashSet<(Point, Point)> = HashSet::new();
    for trail in &trails {
        let first = trail.first().unwrap();
        let last = trail.last().unwrap();
        unique_trails.insert((first.clone(), last.clone()));
    }
    println!("Task 1: {:?}", unique_trails.len());

    // Task 2: What is the sum of the ratings of all trailheads?
    println!("Task 2: {:?}", trails.len());

    Ok(())
}

// Walks from the start point every hiking trail.
// It is a simplified recursive depth-first-search.
// Result is a forest.
fn walk<'a>(map: &Vec<Vec<Node>>, start: &Node) -> Vec<Vec<Point>> {
    let mut trails: Vec<Vec<Point>> = Vec::new();

    if start.neighbours.is_empty() {
        return vec![vec![start.pos.clone()]];
    }

    for neighbour in start.neighbours.iter() {
        for mut new_trail in walk(&map, &map[neighbour.y][neighbour.x]) {
            let mut trail: Vec<Point> = vec![start.pos.clone()];
            trail.append(&mut new_trail);
            trails.push(trail)
        }
    }

    trails
}

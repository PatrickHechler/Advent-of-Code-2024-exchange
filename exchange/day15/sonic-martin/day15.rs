use std::cmp::Reverse;
use std::collections::{HashMap, VecDeque};
use std::fs::File;
use std::hash::Hash;
use std::io::BufRead;
use std::path::Path;
use std::{env, io};

const ROBOT: char = '@';
const BOX: char = 'O';
const BOX_LEFT: char = '[';
const BOX_RIGHT: char = ']';
const WALL: char = '#';
const EMPTY: char = '.';

const UP: char = '^';
const RIGHT: char = '>';
const DOWN: char = 'v';
const LEFT: char = '<';

type Number = i32;

#[derive(Copy, Clone, Debug, Eq, Hash, PartialEq)]
struct Point {
    x: Number,
    y: Number,
}

#[derive(Clone, Debug)]
struct Map {
    map: HashMap<Point, char>,
    cols: Number,
    rows: Number,
}

impl Map {
    fn new() -> Map {
        Map {
            map: HashMap::new(),
            cols: 0,
            rows: 0,
        }
    }

    fn insert(&mut self, p: Point, c: char) {
        self.map.insert(p, c);
        if p.x > self.cols {
            self.cols = p.x;
        }
        if p.y > self.rows {
            self.rows = p.y;
        }
    }

    fn get(&self, point: &Point) -> &char {
        self.map.get(point).unwrap()
    }

    fn get_neighbor(&self, point: &Point, direction: char) -> Point {
        let (dx, dy) = match direction {
            UP => (0, -1),
            RIGHT => (1, 0),
            DOWN => (0, 1),
            LEFT => (-1, 0),
            _ => {
                panic!("Unknown direction: {}", direction);
            }
        };
        Point {
            x: point.x + dx,
            y: point.y + dy,
        }
    }
}

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day15 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let lines = reader.lines().collect::<Result<Vec<_>, _>>().unwrap();

    // Load and parse input from file
    // Map
    let mut map: Map = Map::new();
    let mut map_enlarged = Map::new();
    let mut start_normal_map = Point {
        x: Number::MAX,
        y: Number::MAX,
    };
    let mut start_enlarged_map = Point {
        x: Number::MAX,
        y: Number::MAX,
    };
    for y in 0..lines.len() {
        if lines[y].is_empty() {
            break;
        }
        let line = lines[y].chars().collect::<Vec<_>>();
        for x in 0..line.len() {
            // Normal map
            let point = Point {
                x: x as Number,
                y: y as Number,
            };
            let cell_value = line[x];
            map.insert(point, cell_value);
            if cell_value == ROBOT {
                start_normal_map = point;
            }
            // Enlarged map
            let point1 = Point {
                x: point.x * 2,
                y: y as Number,
            };
            let point2 = Point {
                x: point1.x + 1,
                y: point.y,
            };
            map_enlarged.insert(point1, cell_value);
            map_enlarged.insert(point2, cell_value);
            if cell_value == ROBOT {
                map_enlarged.insert(point2, EMPTY);
                start_enlarged_map = point1;
            } else if cell_value == BOX {
                map_enlarged.insert(point1, BOX_LEFT);
                map_enlarged.insert(point2, BOX_RIGHT);
            }
        }
    }

    // Movements
    let mut movements: Vec<char> = Vec::new();
    // map #rows + 1 empty line + 1 for the first movement lines
    let movements_start = (map.rows + 1 + 1) as usize;
    for y in movements_start..lines.len() {
        let line = lines[y].chars().collect::<Vec<_>>();
        movements.extend(line);
    }

    // Task 1:
    // Predict the motion of the robot and boxes in the warehouse.
    // After the robot is finished moving, what is the sum of all boxes' GPS coordinates?
    let task1_map = do_movements1(map, start_normal_map, movements.to_owned());
    let mut sum: Number = 0;
    for (point, cell_value) in task1_map.map.iter() {
        if cell_value == &BOX {
            sum += 100 * point.y + point.x;
        }
    }
    println!("Task 1: {}", sum);

    // Task 2:
    // Predict the motion of the robot and boxes in the warehouse.
    // After the robot is finished moving, what is the sum of all boxes' GPS coordinates?
    let task2_map = do_movements2(map_enlarged, start_enlarged_map, movements.to_owned());
    let mut sum2: Number = 0;
    for (point, cell_value) in task2_map.map.iter() {
        if cell_value == &BOX_LEFT {
            sum2 += 100 * point.y + point.x;
        }
    }
    println!("Task 2: {}", sum2);

    Ok(())
}

fn print_map(map: &Map) {
    for y in 0..=map.rows {
        for x in 0..=map.cols {
            print!("{}", map.get(&Point { x, y }));
        }
        println!();
    }
}

fn do_movements1(init_map: Map, start: Point, movements: Vec<char>) -> Map {
    let mut map = init_map.clone();
    let mut current_position = start;

    for movement in movements.to_owned() {
        let next_position = map.get_neighbor(&current_position, movement);
        let next_cell = map.get(&next_position);
        if next_cell == &WALL {
            continue;
        } else if next_cell == &EMPTY {
            map.insert(current_position, EMPTY);
            map.insert(next_position, ROBOT);
            current_position = next_position;
        // BOX
        } else {
            // Lookahead and keep track of the last cell;
            let mut lookahead_position = map.get_neighbor(&next_position, movement);
            let mut lookahead_cell = map.get(&lookahead_position);
            loop {
                if lookahead_cell == &EMPTY || lookahead_cell == &WALL {
                    break;
                }
                lookahead_position = map.get_neighbor(&lookahead_position, movement);
                lookahead_cell = map.get(&lookahead_position);
            }
            // WALL: do nothing
            if lookahead_cell == &WALL {
                continue;
            // EMPTY
            } else {
                map.insert(current_position, EMPTY);
                map.insert(next_position, ROBOT); // Overwrites the first Box
                map.insert(lookahead_position, BOX); // and move all of them one ahead the Direction
                current_position = next_position;
            }
        }
    }

    map
}

fn do_movements2(init_map: Map, start: Point, movements: Vec<char>) -> Map {
    let mut map = init_map.clone();
    let mut current_position = start;

    for movement in movements.to_owned() {
        let next_position = map.get_neighbor(&current_position, movement);
        let next_cell = map.get(&next_position);
        if next_cell == &WALL {
            continue;
        } else if next_cell == &EMPTY {
            map.insert(current_position, EMPTY);
            map.insert(next_position, ROBOT);
            current_position = next_position;
        // BOX
        } else {
            // For horizontal movement, we can re-use movement behaviour from task 1.
            // Only slightly adapted for BOXes being [] not O
            if movement == LEFT || movement == RIGHT {
                let mut n: Number = 1;
                let mut lookahead_position = map.get_neighbor(&next_position, movement);
                let mut lookahead_cell = map.get(&lookahead_position).to_owned();
                loop {
                    if lookahead_cell == EMPTY || lookahead_cell == WALL {
                        break;
                    }
                    n += 1;
                    lookahead_position = map.get_neighbor(&lookahead_position, movement);
                    lookahead_cell = map.get(&lookahead_position).to_owned();
                }
                // WALL: do nothing
                if lookahead_cell == WALL {
                    continue;
                // EMPTY
                } else {
                    let lookahead_position = map.get_neighbor(&next_position, movement);
                    let lookahead_cell = map.get(&lookahead_position).to_owned();
                    let mut box_value = lookahead_cell;
                    for i in 0..n {
                        box_value = {
                            if box_value == BOX_LEFT {
                                BOX_RIGHT
                            } else {
                                BOX_LEFT
                            }
                        };
                        let left_right_factor = {
                            if movement == LEFT {
                                -1
                            } else {
                                1
                            }
                        };
                        let point = Point {
                            x: lookahead_position.x + i * left_right_factor,
                            y: lookahead_position.y,
                        };
                        map.insert(point, box_value);
                    }

                    map.insert(next_position, ROBOT); // Overwrites the first Box
                    map.insert(current_position, EMPTY);

                    current_position = next_position;
                }
            // For vertical movement we have to take care that boxes are now 2 point wide
            // That mainly means, that there can be pyramid-like figures.
            } else {
                // Lookahead and keep track if the move would be valid
                let mut valid_move = true;
                let mut lookahead_queue: VecDeque<Point> = VecDeque::new();
                let mut visited: HashMap<Point, char> = HashMap::new();
                lookahead_queue.push_back(next_position);
                while !lookahead_queue.is_empty() {
                    let lookahead_position = lookahead_queue.pop_front().unwrap().to_owned();
                    let lookahead_cell = map.get(&lookahead_position).to_owned();

                    // Check if it's a wall, early exit
                    if lookahead_cell == WALL {
                        valid_move = false;
                        break;
                    // When empty, then just go ahead in the loop
                    } else if lookahead_cell == EMPTY {
                        continue;
                    }

                    // From here on we assume it's a BOX

                    // Add the neighbour to the queue
                    let neighbour_position =
                        map.get_neighbor(&lookahead_position, movement).to_owned();
                    if !visited.contains_key(&neighbour_position) {
                        lookahead_queue.push_back(neighbour_position);
                    }

                    // Push the second part of this box into the queue,
                    // when not yet visited
                    let point2 = {
                        if lookahead_cell == BOX_LEFT {
                            map.get_neighbor(&lookahead_position, RIGHT)
                        } else {
                            map.get_neighbor(&lookahead_position, LEFT)
                        }
                    };
                    if !visited.contains_key(&point2) {
                        lookahead_queue.push_back(point2);
                    }

                    // Remembered where we were
                    visited.insert(lookahead_position, lookahead_cell);
                }
                // Do nothing if its an invalid move.
                // If it's a valid move, we need to move the boxes visited
                if valid_move {
                    // Sort the visited by y coordinates, so we dont overwrite each other
                    let mut sorted_visited: Vec<(&Point, &char)> = visited.iter().collect();
                    if movement == UP {
                        sorted_visited.sort_by_key(|(point, _)| point.y);
                    // else: DOWN
                    } else {
                        sorted_visited.sort_by_key(|(point, _)| Reverse(point.y));
                    }
                    for (point, cell_value) in sorted_visited.iter() {
                        let next_point = map.get_neighbor(point, movement);
                        map.insert(next_point, *cell_value.to_owned());
                        map.insert(**point, EMPTY);
                    }
                    map.insert(current_position, EMPTY);
                    map.insert(next_position, ROBOT);
                    current_position = next_position
                }
            }
        }
    }

    map
}

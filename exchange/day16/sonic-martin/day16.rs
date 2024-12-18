use std::collections::{BTreeMap, HashMap, HashSet};
use std::fs::File;
use std::io::BufRead;
use std::ops::Add;
use std::path::Path;
use std::{env, io};

const START: char = 'S';
const END: char = 'E';
const _NORMAL: char = '.';
const WALL: char = '#';

#[derive(Clone, Copy, Eq, Ord, PartialEq, PartialOrd, Hash, Debug)]
enum Direction {
    North,
    East,
    South,
    West,
}
impl Direction {
    const VALUES: [Self; 4] = [Self::North, Self::South, Self::East, Self::West];
}

// Source: https://github.com/TheAlgorithms/Rust/blob/master/src/graph/dijkstra.rs
type Graph<V, E> = BTreeMap<V, BTreeMap<V, E>>;

#[derive(Clone, Copy, Debug, Eq, Ord, PartialEq, PartialOrd, Hash)]
struct Node {
    x: usize,
    y: usize,
    direction: Direction,
}

// Source: https://github.com/TheAlgorithms/Rust/blob/master/src/graph/dijkstra.rs
fn add_edge<V: Ord + Copy, E: Ord>(graph: &mut Graph<V, E>, v1: V, v2: V, c: E) {
    graph.entry(v1).or_default().insert(v2, c);
    graph.entry(v2).or_default();
}

// Source: https://github.com/TheAlgorithms/Rust/blob/master/src/graph/dijkstra.rs
// performs Dijsktra's algorithm on the given graph from the given start
// the graph is a positively-weighted undirected graph
//
// returns a map that for each reachable vertex associates the distance and the predecessor
// since the start has no predecessor but is reachable, map[start] will be None
//
// Time: O(E * logV). For each vertex, we traverse each edge, resulting in O(E). For each edge, we
// insert a new shortest path for a vertex into the tree, resulting in O(E * logV).
// Space: O(V). The tree holds up to V vertices.
pub fn dijkstra<V: Ord + Copy + std::fmt::Debug, E: Ord + Copy + Add<Output = E>>(
    graph: &Graph<V, E>,
    start: V,
) -> BTreeMap<V, Option<(V, E)>> {
    let mut ans = BTreeMap::new();
    let mut prio = BTreeMap::new();

    // start is the special case that doesn't have a predecessor
    ans.insert(start, None);

    for (new, weight) in &graph[&start] {
        ans.insert(*new, Some((start, *weight)));
        prio.insert(*new, *weight);
    }

    while let Some((vertex, path_weight)) = prio.pop_first() {
        for (next, weight) in &graph[&vertex] {
            let new_weight = path_weight + *weight;
            match ans.get(next) {
                // if ans[next] is a lower dist than the alternative one, we do nothing
                Some(Some((_, dist_next))) if new_weight >= *dist_next => {}
                // if ans[next] is None then next is start and so the distance won't be changed, it won't be added again in prio
                Some(None) => {}
                // the new path is shorter, either new was not in ans or it was farther
                _ => {
                    ans.insert(*next, Some((vertex, new_weight)));
                    prio.insert(*next, new_weight);
                }
            }
        }
    }

    ans
}

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day16 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let lines = reader.lines().collect::<Result<Vec<_>, _>>().unwrap();

    // Cost mappings
    let cost_mapping = HashMap::from([
        ((Direction::North, Direction::North), 1),
        ((Direction::North, Direction::East), 1001),
        ((Direction::North, Direction::South), 2001),
        ((Direction::North, Direction::West), 1001),
        ((Direction::East, Direction::East), 1),
        ((Direction::East, Direction::South), 1001),
        ((Direction::East, Direction::West), 2001),
        ((Direction::East, Direction::North), 1001),
        ((Direction::South, Direction::South), 1),
        ((Direction::South, Direction::West), 1001),
        ((Direction::South, Direction::North), 2001),
        ((Direction::South, Direction::East), 1001),
        ((Direction::West, Direction::West), 1),
        ((Direction::West, Direction::North), 1001),
        ((Direction::West, Direction::East), 2001),
        ((Direction::West, Direction::South), 1001),
    ]);
    // Load and parse input from file into a map[y][x].
    let map: Vec<Vec<char>> = lines.iter().map(|l| l.chars().collect()).collect();

    // Create an undirected graph by that map
    let mut graph = BTreeMap::new();
    let mut start: (usize, usize) = (0, 0);
    let mut end: (usize, usize) = (0, 0);
    for y in 0..map.len() {
        for x in 0..map[y].len() {
            let cell = map[y][x];

            match cell {
                WALL => continue,
                START => start = (x, y),
                END => end = (x, y),
                _ => {}
            }

            let potential_neighbours = vec![
                (x, y - 1, Direction::North),
                (x + 1, y, Direction::East),
                (x, y + 1, Direction::South),
                (x - 1, y, Direction::West),
            ];

            for neighbour in potential_neighbours {
                let (neighbour_x, neighbour_y, neighbour_direction) = neighbour;

                if map[neighbour_y][neighbour_x] == WALL {
                    continue;
                }

                for direction in Direction::VALUES {
                    let current_node = Node { x, y, direction };
                    let neighbour_node = Node {
                        x: neighbour_x,
                        y: neighbour_y,
                        direction: neighbour_direction,
                    };

                    let cost = cost_mapping
                        .get(&(direction, neighbour_direction))
                        .unwrap()
                        .to_owned();
                    add_edge(&mut graph, current_node, neighbour_node, cost);
                }
            }
        }
    }

    // Run dijkstra
    let start_node = Node {
        x: start.0,
        y: start.1,
        direction: Direction::East,
    };
    let all_costs = dijkstra(&graph, start_node);

    // Task 1:
    // Analyze your map carefully.
    // What is the lowest score a Reindeer could possibly get?
    let mut minimal_cost = i32::MAX;
    let mut final_end_node: Node = Node {
        x: 0,
        y: 0,
        direction: Direction::North,
    };
    for direction in Direction::VALUES {
        let end_node = Node {
            x: end.0,
            y: end.1,
            direction,
        };
        let cost = all_costs.get(&end_node);
        match cost {
            Some(v) => {
                let w = v.unwrap();
                if w.1 < minimal_cost {
                    minimal_cost = w.1;
                    final_end_node = end_node;
                }
            }
            None => {}
        }
    }
    println!("Task 1: {}", minimal_cost);


    let mut visited: HashSet<(usize, usize)> = HashSet::new();
    let visited_best_path = walk(&all_costs, &final_end_node).unwrap();
    for node_coords in visited_best_path {
        let node = get_node_for_best_path_of_a_coord(&all_costs, node_coords);
        let new_visited = walk(&all_costs, &node);
        if new_visited.is_some() {
            visited.extend(new_visited.unwrap());
        }
    }
    println!("Task 2: {}", visited.len());

    Ok(())
}

fn walk(
    all_costs: &BTreeMap<Node, Option<(Node, i32)>>,
    start: &Node,
) -> Option<HashSet<(usize, usize)>> {
    if !all_costs.contains_key(start) {
        return None;
    }
    let mut visited: HashSet<(usize, usize)> = HashSet::new();

    visited.insert((start.x, start.y));

    let mut next_option = all_costs[start];
    let mut next = match next_option {
        Some(v) => v.0,
        None => return None,
    };
    loop {
        if next.x == start.x && next.y == start.y {
            return None;
        }
        visited.insert((next.x, next.y));
        next_option = all_costs[&next];
        next = match next_option {
            Some(v) => v.0,
            None => break,
        };
    }

    visited.insert((next.x, next.y));

    Some(visited)
}

fn get_node_for_best_path_of_a_coord(all_costs: &BTreeMap<Node, Option<(Node, i32)>>, start: (usize, usize)) -> Node {
    let mut minimal_cost = i32::MAX;
    let mut final_node: Node = Node {
        x: 0,
        y: 0,
        direction: Direction::North,
    };
    for direction in Direction::VALUES {
        let node = Node {
            x: start.0,
            y: start.1,
            direction,
        };
        let cost = all_costs.get(&node);
        match cost {
            Some(v) => {
                match v {
                    Some(x) => {
                        if x.1 < minimal_cost {
                            minimal_cost = x.1;
                            final_node = node;
                        }
                    },
                    None => continue
                }
            }
            None => continue
        }
    }

    final_node
}

use std::collections::{BTreeMap, HashMap};
use std::fs::File;
use std::io::BufRead;
use std::ops::Add;
use std::path::Path;
use std::{env, io};

const SAFE: char = '.';
const CORRUPTED: char = '#';

#[derive(Clone, Copy, Debug, Eq, Ord, PartialEq, PartialOrd, Hash)]
struct Node {
    x: i64,
    y: i64,
}

// Source: https://github.com/TheAlgorithms/Rust/blob/master/src/graph/dijkstra.rs
type Graph<V, E> = BTreeMap<V, BTreeMap<V, E>>;

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
        return Err(String::from(
            "Usage: cargo run --bin day18 INPUT [TEST_FLAG]",
        ));
    }

    // Real input (71x71 matrix) or test (7x7 matrix)
    let mut max_row = 71;
    let mut max_col = 71;
    if args.len() >= 3 {
        if &args[2] == "test" {
            max_row = 7;
            max_col = 7;
        }
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let lines = reader.lines().collect::<Result<Vec<_>, _>>().unwrap();

    // Load and parse input from file
    let bytes: Vec<Node> = lines
        .iter()
        .map(|line| {
            let mut coords = line.split(',').map(|s| s.parse::<i64>().unwrap());
            Node {
                x: coords.next().unwrap(),
                y: coords.next().unwrap(),
            }
        })
        .collect();

    // Build a map
    let mut map: HashMap<Node, char> = HashMap::new();
    for y in 0..max_row {
        for x in 0..max_col {
            map.insert(Node { x, y }, SAFE);
        }
    }

    // Task 1:
    // Simulate the first kilobyte (1024 bytes) falling onto your memory space.
    // Afterward, what is the minimum number of steps needed to reach the exit?
    let mut map1 = map.to_owned();
    for (i, b) in bytes.iter().enumerate() {
        if i >= 1024 {
            // Task 1
            break;
        }
        map1.insert(*b, CORRUPTED);
    }
    let all_costs = shortest_path(&map1, max_row, max_col);
    let end = Node {
        x: max_col - 1,
        y: max_row - 1,
    };
    let mut costs = 0;
    if let Some(node) = all_costs.get(&end) {
        if let Some(predecessor) = node {
            let pred_costs = predecessor.1;
            costs += pred_costs;
        }
    }
    println!("Task 1: {:?}", costs);

    // Task 2:
    // Simulate more of the bytes that are about to corrupt your memory space.
    // What are the coordinates of the first byte that will prevent the exit from being reachable
    // from your starting position?
    let mut map2 = map.to_owned();
    let end = Node {
        x: max_col - 1,
        y: max_row - 1,
    };
    let mut breaking_node: Node = Node { x: i64::MIN, y: i64::MIN };
    for b in bytes {
        map2.insert(b, CORRUPTED);
        let all_costs = shortest_path(&map2, max_row, max_col);
        if all_costs.get(&end).is_none() {
            breaking_node = b;
            break;
        }
    };
    println!("Task 2: {},{}", breaking_node.x, breaking_node.y);

    Ok(())
}

fn shortest_path(
    map: &HashMap<Node, char>,
    max_row: i64,
    max_col: i64,
) -> BTreeMap<Node, Option<(Node, i32)>> {
    // Create an undirected graph by that map
    let mut graph = BTreeMap::new();
    for y in 0..max_row {
        for x in 0..max_col {
            let node = Node { x, y };
            let cell = map.get(&node).unwrap();

            if *cell == CORRUPTED {
                continue;
            }

            let potential_neighbours = vec![
                Node { x, y: y - 1 }, // North
                Node { x: x + 1, y }, // East
                Node { x, y: y + 1 }, // South
                Node { x: x - 1, y }, // West
            ];

            for neighbour in potential_neighbours {
                if !map.contains_key(&neighbour) {
                    continue;
                }

                let neighbour_cell = map.get(&neighbour).unwrap();
                if *neighbour_cell == CORRUPTED {
                    continue;
                }

                add_edge(&mut graph, node, neighbour, 1);
            }
        }
    }

    // Find optimal ways from Start (0,0) to any point in the map
    let start = Node { x: 0, y: 0 };
    let all_costs = dijkstra(&graph, start);

    all_costs
}

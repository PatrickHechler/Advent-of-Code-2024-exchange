use std::collections::{HashMap, HashSet};
use std::fs::File;
use std::io::BufRead;
use std::path::Path;
use std::{env, io};

const COMPUTER_NAME_PREFIX: char = 't';

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day23 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);

    // Load and parse input from file
    let edges_string = reader
        .lines()
        .map(|x| {
            x.unwrap()
                .split_once("-")
                .map(|(x, y)| (x.to_string(), y.to_string()))
                .unwrap()
        })
        .collect::<HashSet<(String, String)>>();

    // Convert the edges from being (String, String) to (usize, usize) and have a mapping at hand.
    let mut edge_mapping = HashMap::new();
    let mut edge_mapping_back = HashMap::new();
    let mut edges = HashSet::new();
    for (u, v) in edges_string {
        if !edge_mapping.contains_key(&u) {
            edge_mapping.insert(u.to_owned(), edge_mapping.len());
        }
        if !edge_mapping.contains_key(&v) {
            edge_mapping.insert(v.to_owned(), edge_mapping.len());
        }

        let u_id = edge_mapping.get(&u).unwrap().to_owned();
        let v_id = edge_mapping.get(&v).unwrap().to_owned();
        edges.insert((u_id, v_id));

        edge_mapping_back.insert(u_id, u.to_owned());
        edge_mapping_back.insert(v_id, v.to_owned());
    }

    // Create an adjacency list from the edges
    let mut adj_list = HashMap::new();
    for (u, v) in edges {
        adj_list.entry(u).or_insert_with(HashSet::new).insert(v);
        adj_list.entry(v).or_insert_with(HashSet::new).insert(u);
    }

    // Task 1:
    // Find all the sets of three inter-connected computers.
    // How many contain at least one computer with a name that starts with t?
    let cliques3 = enumerate_cliques(&adj_list, 3); // Enumerate k-cliques of size k=3 using a naive algorithm
    let cliques_task1 = cliques3
        .iter()
        .map(|vs| {
            vs.iter()
                .map(|v| edge_mapping_back.get(&v).unwrap())
                .collect::<Vec<_>>()
        })
        .filter(|vs| vs.iter().any(|v| v.starts_with(COMPUTER_NAME_PREFIX)))
        .collect::<Vec<_>>();
    println!("Task 1: {}", cliques_task1.len());

    // Task 2:
    // [...]the LAN party will be the largest set of computers that are all connected to each other.
    // What is the password to get into the LAN party?
    let maximum_clique = maximum_clique(&adj_list);
    let mut cliques_task2 = maximum_clique
        .iter()
        .map(|v| edge_mapping_back.get(&v).unwrap().to_owned())
        .collect::<Vec<_>>();
    cliques_task2.sort();
    println!("Task 2: {}", cliques_task2.join(","));

    // // Naive approach
    // let mut max_clique = HashSet::new();
    // let mut k = 4;
    // loop {
    //     let new_cliques = enumerate_cliques(edges_string.to_owned(), k);
    //
    //     if new_cliques.is_empty() {
    //         break;
    //     }
    //
    //     max_clique = new_cliques[0].to_owned();
    //     k = k + 1;
    // }
    // let mut password = max_clique.iter().cloned().collect::<Vec<String>>();
    // password.sort();
    // println!("Task 2: {}", password.join(","));

    Ok(())
}


// maximum clique is a clique with the largest possible number of vertices.
// This implementation is a naive one and gets all maximal cliques in a graph.
// The maximal clique with the most edges is the maximum clique.
// See https://en.wikipedia.org/wiki/Clique_problem#Finding_maximum_cliques_in_arbitrary_graphs
fn maximum_clique(adj_list: &HashMap<usize, HashSet<usize>>) -> HashSet<usize> {
    let mut maximum_clique: HashSet<usize> = HashSet::new();

    let all_maximal_clique = bron_kerbosch(adj_list);
    for maximal_clique in all_maximal_clique {
        if maximal_clique.len() > maximum_clique.len() {
            maximum_clique = maximal_clique;
        }
    }

    maximum_clique
}

// Listing all maximal cliques via Bron-Kerbosch algorithm.
// See: https://en.wikipedia.org/wiki/Clique_problem#Listing_all_maximal_cliques
// Source: https://github.com/bertptrs/adventofcode/blob/master/2018/src/day23.rs#L15
fn bron_kerbosch(adj_list: &HashMap<usize, HashSet<usize>>) -> Vec<HashSet<usize>> {
    let mut cliques = Vec::new();

    let mut r = HashSet::new();
    let x = HashSet::new();
    let p: HashSet<usize> = (0..adj_list.len()).collect();

    bron_kerbosch1(adj_list, &mut cliques, &mut r, p, x);

    cliques
}
fn bron_kerbosch1(
    adj_list: &HashMap<usize, HashSet<usize>>,
    cliques: &mut Vec<HashSet<usize>>,
    r: &mut HashSet<usize>,
    p: HashSet<usize>,
    mut x: HashSet<usize>,
) {
    if p.is_empty() && x.is_empty() {
        if cliques.is_empty() {
            cliques.push(r.clone());
            return;
        }

        let cur = cliques.first().unwrap().len();
        if cur < r.len() {
            cliques.clear();
        }
        if cur <= r.len() {
            cliques.push(r.clone())
        }
        return;
    }

    let mut p_clone = p.clone();
    let pivot = *p.union(&x).max_by_key(|&&v| adj_list[&v].len()).unwrap();

    for &v in p.difference(&adj_list[&pivot]) {
        r.insert(v);
        let p1: HashSet<usize> = p_clone.intersection(&adj_list[&v]).cloned().collect();
        let x1: HashSet<usize> = x.intersection(&adj_list[&v]).cloned().collect();
        bron_kerbosch1(adj_list, cliques, r, p1, x1);
        r.remove(&v);

        p_clone.remove(&v);
        x.insert(v);
    }
}

// maximal_clique looks via greedy algorithm for ONE maximal clique in the given graph.
// Remark: For AoC2024 Day 23 part 2 this is not enough, see maximum_clique.
//
// Starting with an arbitrary clique (for instance, any single vertex or even the empty set),
// grow the current clique one vertex at a time by looping through the graph's remaining vertices.
// For each vertex v that this loop examines, add v to the clique if it is adjacent to every vertex
// that is already in the clique, and discard v otherwise
//
// Source: https://en.wikipedia.org/wiki/Clique_problem#Finding_a_single_maximal_clique
// fn maximal_clique(adj_list: &HashMap<usize, HashSet<usize>>) -> Vec<usize>{
//     if adj_list.is_empty() {
//         return Vec::new();
//     }
//     let mut vertices: VecDeque<usize> = adj_list.keys().into_iter().cloned().collect();
// 
//     let mut clique: Vec<usize> = vec![vertices.pop_front().unwrap()];
// 
//     'outer: while let Some(v) = vertices.pop_front() {
//         for u in &clique {
//             if !adj_list.get(u).unwrap().contains(&v) {
//                 continue 'outer;
//             }
//         }
//         clique.push(v);
//     }
//     clique
// }

// Function to enumerate k-cliques using a naive approach
//
// Based on: https://www.reddit.com/r/compsci/comments/p5m51s/comment/h97rxeg
fn enumerate_cliques(adj_list: &HashMap<usize, HashSet<usize>>, k: usize) -> Vec<Vec<usize>> {
    let vertices: Vec<usize> = adj_list.keys().into_iter().cloned().collect();

    // Generate all combinations of nodes of size k
    let combinations = comb(&vertices, k);

    // Filter valid cliques
    let mut cliques = Vec::new();
    'outer: for combination in combinations {
        for i in 0..combination.len() {
            for j in i + 1..combination.len() {
                if !adj_list
                    .get(&combination[i])
                    .unwrap()
                    .contains(&combination[j])
                {
                    continue 'outer;
                }
            }
        }
        cliques.push(combination);
    }

    cliques
}

// Function to generate combinations of a given length
// Sadly this consumes a lot of space.
// Source: https://rosettacode.org/wiki/Combinations#Rust
fn comb<T>(slice: &[T], k: usize) -> Vec<Vec<T>>
where
    T: Copy,
{
    // If k == 1, return a vector containing a vector for each element of the slice.
    if k == 1 {
        return slice.iter().map(|x| vec![*x]).collect::<Vec<Vec<T>>>();
    }
    // If k is exactly the slice length, return the slice inside a vector.
    if k == slice.len() {
        return vec![slice.to_vec()];
    }

    // Make a vector from the first element + all combinations of k - 1 elements of the rest of the slice.
    let mut result = comb(&slice[1..], k - 1)
        .into_iter()
        .map(|x| [&slice[..1], x.as_slice()].concat())
        .collect::<Vec<Vec<T>>>();

    // Extend this last vector with the all the combinations of k elements after from index 1 onward.
    result.extend(comb(&slice[1..], k));
    // Return final vector.
    result
}

use std::collections::{HashMap, HashSet};
use std::fs::File;
use std::hash::Hash;
use std::io::BufRead;
use std::path::Path;
use std::{env, io};

type Number = i32;

#[derive(Clone, Debug, Eq, Hash, PartialEq)]
enum Direction {
    UP,
    RIGHT,
    DOWN,
    LEFT,
}

#[derive(Copy, Clone, Debug, Eq, Hash, PartialEq)]
struct Point {
    x: Number,
    y: Number,
}

// Vec<Number> could with some adoption be simplified, so that always the sum is already present there
// instead of a Vec<Ys> or Vec<Xs>.
type PerimeterMap = HashMap<(Number, Direction), Vec<Number>>;

#[derive(Clone, Debug)]
struct Perimeters {
    horizontal: PerimeterMap, // Ys[Xs]
    vertical: PerimeterMap,   // Xs[Ys]
}

impl Perimeters {
    fn new() -> Perimeters {
        Perimeters {
            horizontal: HashMap::new(),
            vertical: HashMap::new(),
        }
    }

    fn add(&mut self, p: Point, direction: &Direction) {
        match direction {
            Direction::UP => self
                .horizontal
                .entry((p.y, Direction::UP))
                .or_insert_with(Vec::new)
                .push(p.x),
            Direction::DOWN => self
                .horizontal
                .entry((p.y + 1, Direction::DOWN))
                .or_insert_with(Vec::new)
                .push(p.x),
            Direction::LEFT => self
                .vertical
                .entry((p.x, Direction::LEFT))
                .or_insert_with(Vec::new)
                .push(p.y),
            Direction::RIGHT => self
                .vertical
                .entry((p.x + 1, Direction::RIGHT))
                .or_insert_with(Vec::new)
                .push(p.y),
        };
    }

    fn extend(&mut self, other: Perimeters) {
        extend_perimeter_map(&mut self.horizontal, other.horizontal);
        extend_perimeter_map(&mut self.vertical, other.vertical);
    }
}

#[derive(Debug)]
struct Region {
    plots: HashSet<Point>,
    perimeter: Number,
    perimeters: Perimeters, // Task 2
}

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day12 INPUT"));
    }

    // Prepare file for reading
    let input_path = Path::new(&args[1]);
    let file =
        File::open(input_path).map_err(|_| String::from("Failed to open the INPUT file."))?;
    let reader = io::BufReader::new(file);
    let lines = reader.lines().collect::<Result<Vec<_>, _>>().unwrap();

    // Load and parse input from file
    let mut map: HashMap<Point, char> = HashMap::new();
    let rows: Number = lines.len() as Number;
    let mut cols: Number = 0;
    for y in 0..lines.len() {
        let line = lines[y].chars().collect::<Vec<_>>();
        cols = line.len() as Number;
        for x in 0..line.len() {
            let point = Point {
                x: x as Number,
                y: y as Number,
            };
            let plot = line[x];
            map.insert(point, plot);
        }
    }

    // Heavy lifting
    let mut max_region_id: Number = 0;
    let mut plot_to_region: HashMap<Point, Number> = HashMap::new();
    let mut regions: HashMap<Number, Region> = HashMap::new();
    for y in 0..rows {
        for x in 0..cols {
            let point = Point { x, y };
            let plot_type = map
                .get(&Point {
                    x: x as Number,
                    y: y as Number,
                })
                .unwrap()
                .to_owned();
            let neighbour_options = [
                (Point { x, y: y - 1 }, Direction::UP),
                (Point { x: x + 1, y }, Direction::RIGHT),
                (Point { x, y: y + 1 }, Direction::DOWN),
                (Point { x: x - 1, y }, Direction::LEFT),
            ];
            let mut region_options: HashSet<Number> = HashSet::new();
            let mut perimeter: Number = 0;
            let mut perimeters = Perimeters::new();
            for (neighbour, direction) in neighbour_options.iter() {
                // Neighbour Point is out of bounds
                if !map.contains_key(neighbour) {
                    perimeter += 1;
                    perimeters.add(point, direction);
                    continue;
                }
                // Plot and Neighbour are different plot type
                let neighbour_type = map.get(neighbour).unwrap().to_owned();
                if plot_type != neighbour_type {
                    perimeter += 1;
                    perimeters.add(point, direction);
                    continue;
                }
                // Do not proceed, when the neighbour was not visited yet, and thus
                // does not have an assigned region.
                if !plot_to_region.contains_key(neighbour) {
                    continue;
                }
                // Potential region, which the current plant could be assigned to
                let neighbour_region = plot_to_region.get(neighbour).unwrap().to_owned();
                region_options.insert(neighbour_region);
            }
            // No plot with same type as neighbour, thus create a new region for the new plot
            if region_options.len() == 0 {
                let mut plots = HashSet::new();
                plots.insert(point.to_owned());
                regions.insert(
                    max_region_id.to_owned(),
                    Region {
                        perimeter,
                        plots,
                        perimeters,
                    },
                );
                plot_to_region.insert(point.to_owned(), max_region_id.to_owned());
                max_region_id += 1;
                continue;
            }
            // At least one neighbour is already in a region,
            // this will become the main region, and the new plot will be added to this region.
            let mut region_options_iter = region_options.iter();
            let main_region_id = region_options_iter.next().unwrap().to_owned();
            {
                let main_region = regions.get_mut(&main_region_id).unwrap();
                main_region.perimeter += perimeter;
                main_region.perimeters.extend(perimeters.to_owned());
                main_region.plots.insert(point.to_owned());
            }
            plot_to_region.insert(point.to_owned(), main_region_id.to_owned());
            // No further neighbours, skip further work.
            if region_options_iter.len() == 0 {
                continue;
            }
            // When there are further neighbours already being in a region, check if that
            // is the same or another region.
            // In later case it shall get merged with the main region.
            let mut add_perimeter = 0;
            let mut add_perimeters = Perimeters::new();
            let mut merged_regions: HashSet<Number> = HashSet::new();
            let mut merged_plots: HashSet<Point> = HashSet::new();
            for other_region_id in region_options_iter {
                // It is the same region as the main_region
                if main_region_id == *other_region_id {
                    continue;
                }
                let other_region = regions.get(&other_region_id).unwrap();
                add_perimeter += other_region.perimeter;
                add_perimeters.extend(other_region.perimeters.to_owned());
                merged_plots.extend(other_region.plots.iter().cloned());
                merged_regions.insert(other_region_id.to_owned());
            }
            let main_region = regions.get_mut(&main_region_id).unwrap();
            main_region.perimeter += add_perimeter;
            main_region.perimeters.extend(add_perimeters.to_owned());
            for p in merged_plots.iter() {
                main_region.plots.insert(*p);
                plot_to_region.insert(*p, main_region_id.to_owned());
            }
            for r in merged_regions {
                regions.remove(&r);
            }
        }
    }

    // Task 1: What is the total price of fencing all regions on your map?
    let mut total_price = 0;
    for region in regions.values() {
        total_price += region.plots.len() as Number * region.perimeter;
    }
    println!("Task 1: Total price: {}", total_price);

    // Task 2: What is the new total price of fencing all regions on your map with the bulk discount?
    let mut total_price2 = 0;
    for region in regions.values() {
        let perimeters = &region.perimeters;
        let horizontal = &perimeters.horizontal;
        let vertical = &perimeters.vertical;
        // Horizontal lines
        let mut sides_horizontal = 0;
        for xs in horizontal.values() {
            if xs.len() == 0 { // // Probably should not happen and we can assume it.
                continue;
            }
            // At this place we now have at least 1 side.
            sides_horizontal += 1;
            sides_horizontal += count_gaps(xs)
        }
        // Vertical lines
        let mut sides_vertical = 0;
        for xs in vertical.values() {
            if xs.len() == 0 { // // Probably should not happen and we can assume it.
                continue;
            }
            // At this place we now have at least 1 side.
            sides_vertical += 1;
            sides_vertical += count_gaps(xs)
        }

        let area = region.plots.len() as Number;
        let sides = sides_horizontal + sides_vertical;
        let price = area * sides;
        total_price2 += price;
    }
    println!("Task 2: Total price: {}", total_price2);

    Ok(())
}

// count_gaps sorts a vector of numbers and counts the gaps (x2 - x1 > 0).
fn count_gaps(numbers: &Vec<Number>) -> Number {
    if numbers.len() == 0 {
        return 0;
    }

    let mut sorted = numbers.to_owned();
    sorted.sort();

    let mut gaps = 0;
    let mut last = sorted[0];
    sorted.remove(0);
    for current in sorted {
        if current - last > 1 {
            // There is a gap
            gaps += 1;
        }
        last = current;
    }

    gaps
}

fn extend_perimeter_map(map1: &mut PerimeterMap, map2: PerimeterMap) {
    for (key, vec2) in map2 {
        if let Some(vec1) = map1.get_mut(&key) {
            vec1.extend(vec2);
        } else {
            map1.insert(key, vec2);
        }
    }
}

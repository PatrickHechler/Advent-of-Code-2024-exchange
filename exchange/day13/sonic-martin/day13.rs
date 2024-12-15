use regex::Regex;
use std::path::Path;
use std::{env, fs};

type Number = i128;

const CLAW_MACHINE_PATTERN: &str = r"(?m)Button A: X\+(?<a_dx>[0-9]+), Y\+(?<a_dy>[0-9]+)\s+Button B: X\+(?<b_dx>[0-9]+), Y\+(?<b_dy>[0-9]+)\s+Prize: X=(?<prize_x>[0-9]+), Y=(?<prize_y>[0-9]+)";

struct ClawMachine {
    a_dx: Number,
    a_dy: Number,
    b_dx: Number,
    b_dy: Number,
    prize_x: Number,
    prize_y: Number,
}

const A_TOKEN: Number = 3;
const B_TOKEN: Number = 1;
const TASK1_MAX_PRESSES: Number = 100;
const TASK2_CORRECTION: Number = 10000000000000;

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day13 INPUT"));
    }

    // Load file into string
    let input_path = Path::new(&args[1]);
    let input = fs::read_to_string(input_path).unwrap();

    // Parse instructions from input
    let mut machines = vec![];
    let re = Regex::new(CLAW_MACHINE_PATTERN).unwrap();
    for caps in re.captures_iter(&*input) {
        machines.push(ClawMachine {
            a_dx: caps.name("a_dx").unwrap().as_str().parse().unwrap(),
            a_dy: caps.name("a_dy").unwrap().as_str().parse().unwrap(),
            b_dx: caps.name("b_dx").unwrap().as_str().parse().unwrap(),
            b_dy: caps.name("b_dy").unwrap().as_str().parse().unwrap(),
            prize_x: caps.name("prize_x").unwrap().as_str().parse().unwrap(),
            prize_y: caps.name("prize_y").unwrap().as_str().parse().unwrap(),
        })
    }

    // Task 1:
    // You estimate that each button would need to be pressed no more than 100 times to win a prize.
    // How else would someone be expected to play?
    // Figure out how to win as many prizes as possible.
    // What is the fewest tokens you would have to spend to win all possible prizes?
    let mut total_token1: Number = 0;
    for m in &machines {
        let solution = solve_linear_2x2(m.a_dx,  m.b_dx, m.prize_x, m.a_dy, m.b_dy, m.prize_y);
        match solution {
            Some(pushs) => {
                let a = pushs.0;
                let b = pushs.1;
                // Extra rule: a <= 100 && b <= 100
                if a > TASK1_MAX_PRESSES || b > TASK1_MAX_PRESSES {
                    continue;
                }
                let a_token = a * A_TOKEN;
                let b_token = b * B_TOKEN;
                let token = a_token + b_token;
                total_token1 += token;
            }
            None => {}
        }
    }
    println!("Task 1: Total tokens: {}", total_token1);
    
    // Task 2:
    // Now, it is only possible to win a prize on the second and fourth claw machines.
    // Unfortunately, it will take many more than 100 presses to do so.
    // Using the corrected prize coordinates, figure out how to win as many prizes as possible.
    // What is the fewest tokens you would have to spend to win all possible prizes?
    let mut total_token2: Number = 0;
    for m in &machines {
        let correction_prize_x = m.prize_x + TASK2_CORRECTION;
        let correction_prize_y = m.prize_y + TASK2_CORRECTION;
        let solution = solve_linear_2x2(m.a_dx,  m.b_dx, correction_prize_x, m.a_dy, m.b_dy, correction_prize_y);
        match solution {
            Some(pushs) => {
                let a = pushs.0;
                let b = pushs.1;
                let a_token = a * A_TOKEN;
                let b_token = b * B_TOKEN;
                let token = a_token + b_token;
                total_token2 += token;
            }
            None => {}
        }
    }
    println!("Task 2: Total tokens: {}", total_token2);

    Ok(())
}

// Solves a linear system 2x2 with 2 unknowns with the cramer rule.
//
// See: https://www.geeksforgeeks.org/system-linear-equations-three-variables-using-cramers-rule/
// See: https://math.libretexts.org/Courses/Cosumnes_River_College/Math_420%3A_Differential_Equations_(Breitenbach)/11%3A_Appendices/03%3A_Determinants_and_Cramer's_Rule_for_2_X_2_Matrices
fn solve_linear_2x2(
    adx: Number,
    bdx: Number,
    c1: Number,
    ady: Number,
    bdy: Number,
    c2: Number,
) -> Option<(Number, Number)> {
    let det = adx * bdy - bdx * ady;
    if det == 0 {
        return None;
    }

    let det_x = c1 * bdy - c2 * bdx;
    let det_y = adx * c2 - ady * c1;
    let x = det_x / det;
    let y = det_y / det;

    // As we have integers, check if the solution would be ints.
    if det_x % det != 0 || det_y % det != 0 {
        return None;
    }

    Some((x, y))
}

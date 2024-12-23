// Task 2 heavily based on:
// https://nickymeuleman.netlify.app/blog/aoc2024-day17/

use regex::Regex;
use std::path::Path;
use std::{env, fs, process};

type Number = i128;

const PROGRAM_PATTERN: &str = r"(?m)Register A: (?<a>[0-9]+)\s+Register B: (?<b>[0-9]+)\s+Register C: (?<c>[0-9]+)\s+Program: (?<program>[0-9](,[0-9]+)*)";

const OPCODE_ADV: Number = 0;
const OPCODE_BXL: Number = 1;
const OPCODE_BST: Number = 2;
const OPCODE_JNZ: Number = 3;
const OPCODE_BXC: Number = 4;
const OPCODE_OUT: Number = 5;
const OPCODE_BDV: Number = 6;
const OPCODE_CDV: Number = 7;

fn main() -> Result<(), String> {
    // Arg parsing
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        return Err(String::from("Usage: cargo run --bin day17 INPUT"));
    }

    // Load file into string
    let input_path = Path::new(&args[1]);
    let input = fs::read_to_string(input_path).unwrap();

    // Parse instructions from input
    let re = Regex::new(PROGRAM_PATTERN).unwrap();
    let mut captures_iter = re.captures_iter(&input);
    let first_capture = match captures_iter.next() {
        Some(caps) => caps,
        None => return Err(String::from("No capture match found")),
    };
    if captures_iter.next().is_some() {
        return Err(String::from("More than one capture match found"));
    }
    let a: Number = first_capture.name("a").unwrap().as_str().parse().unwrap();
    let b: Number = first_capture.name("b").unwrap().as_str().parse().unwrap();
    let c: Number = first_capture.name("c").unwrap().as_str().parse().unwrap();
    let program: Vec<Number> = first_capture
        .name("program")
        .unwrap()
        .as_str()
        .split(",")
        .map(|x| x.parse().unwrap())
        .collect();

    // Task 1:
    // Using the information provided by the debugger, initialize the registers to the given values,
    // then run the program. Once it halts, what do you get if you use commas to join the values
    // it output into a single string?
    let output = run(a, b, c, &program);
    println!(
        "Task 1: {}",
        output
            .iter()
            .map(|x| x.to_string())
            .collect::<Vec<String>>()
            .join(",")
    );

    // // Task 2:
    // // Brute-force: Too slow
    // // What is the lowest positive initial value for register A that causes the program to
    // // output a copy of itself?
    // for check_a in 0..Number::MAX {
    //     let output = run(check_a, b, c, &program);
    //     if output == program {
    //         println!("Task 2: A={}", check_a);
    //         break;
    //     }
    // }

    // Task 2:
    // What is the lowest positive initial value for register A that causes the program to
    // output a copy of itself?
    // Solution heavily based on: https://nickymeuleman.netlify.app/blog/aoc2024-day17/
    let mut valid = vec![0];
    for wanted in program.iter().rev() {
        let mut cur_valid = Vec::new();
        for valid_next_a in &valid {
            for n in 0i128..8 {
                let check_a = (valid_next_a << 3) | n;
                let output = run(check_a, b, c, &program);
                if output.first().unwrap() == wanted {
                    cur_valid.push(check_a);
                }
            }
        }
        valid = cur_valid
    }
    // My adoption is a bit inaccurate, thus the options need to be verified
    valid.sort();
    for check_a in &valid {
        let output = run(*check_a, b, c, &program);
        if output == program {
            println!("Task 2: {}", check_a);
            break;
        }
    }

    Ok(())
}

fn run(mut a: Number, mut b: Number, mut c: Number, program: &Vec<Number>) -> Vec<Number> {
    // A number called the instruction pointer identifies the position in the program from
    // which the next opcode will be read; it starts at 0, pointing at the first 3-bit
    // number in the program.
    let mut pc: Number = 0;

    // Collection for the output
    let mut output = Vec::new();

    loop {
        // If the computer tries to read an opcode past the end of the program, it instead halts.
        if pc >= program.len() as Number {
            break;
        }

        // the computer knows eight instructions, each identified by a 3-bit number
        // (called the instruction's opcode)
        let pc_index = pc as usize;
        let opcode: Number = program[pc_index];

        // Each instruction also reads the 3-bit number after it as an input; this is called its operand.
        let operand: Number = program[pc_index + 1];

        // println!("pc: {}", pc);
        // println!("opcode: {}", opcode);
        // println!("operand: {}", operand);
        // println!("a: {}", a);
        // println!("b: {}", b);
        // println!("c: {}", c);

        // Execute the op
        match opcode {
            OPCODE_ADV => (pc, a, b, c, output) = adv(pc, operand, a, b, c, output),
            OPCODE_BXL => (pc, a, b, c, output) = bxl(pc, operand, a, b, c, output),
            OPCODE_BST => (pc, a, b, c, output) = bst(pc, operand, a, b, c, output),
            OPCODE_JNZ => (pc, a, b, c, output) = jnz(pc, operand, a, b, c, output),
            OPCODE_BXC => (pc, a, b, c, output) = bxc(pc, operand, a, b, c, output),
            OPCODE_OUT => (pc, a, b, c, output) = out(pc, operand, a, b, c, output),
            OPCODE_BDV => (pc, a, b, c, output) = bdv(pc, operand, a, b, c, output),
            OPCODE_CDV => (pc, a, b, c, output) = cdv(pc, operand, a, b, c, output),
            _ => {}
        }

        // Except for jump instructions, the instruction pointer increases by 2 after each
        // instruction is processed (to move past the instruction's opcode and its operand).
        // [...] if this instruction jumps, the instruction pointer is not increased by 2 after
        // this instruction.
        if opcode != OPCODE_JNZ {
            pc += 2;
        }
    }

    output
}

// The adv instruction (opcode 0) performs division.
// The numerator is the value in the A register.
// The denominator is found by raising 2 to the power of the instruction's combo operand.
// (So, an operand of 2 would divide A by 4 (2^2);
// an operand of 5 would divide A by 2^B.)
// The result of the division operation is truncated to an integer and then written to the A register.
fn adv(
    pc: Number,
    operand: Number,
    mut a: Number,
    b: Number,
    c: Number,
    out: Vec<Number>,
) -> (Number, Number, Number, Number, Vec<Number>) {
    let operand_value = combo_operand(operand, a, b, c);

    let numerator = a;
    let denominator = 2_i128.pow(operand_value as u32);

    let result = numerator / denominator;

    a = result;

    (pc, a, b, c, out)
}

// The bdv instruction (opcode 6) works exactly like the adv instruction except that the result
// is stored in the B register.
// (The numerator is still read from the A register.)
fn bdv(
    pc: Number,
    operand: Number,
    a: Number,
    mut b: Number,
    c: Number,
    out: Vec<Number>,
) -> (Number, Number, Number, Number, Vec<Number>) {
    let operand_value = combo_operand(operand, a, b, c);

    let numerator = a;
    let denominator = 2_i128.pow(operand_value as u32);

    let result = numerator / denominator;

    b = result;

    (pc, a, b, c, out)
}

// The cdv instruction (opcode 7) works exactly like the adv instruction except that the result
// is stored in the C register.
// (The numerator is still read from the A register.)
fn cdv(
    pc: Number,
    operand: Number,
    a: Number,
    b: Number,
    mut c: Number,
    out: Vec<Number>,
) -> (Number, Number, Number, Number, Vec<Number>) {
    let operand_value = combo_operand(operand, a, b, c);

    let numerator = a;
    let denominator = 2_i128.pow(operand_value as u32);

    let result = numerator / denominator;

    c = result;

    (pc, a, b, c, out)
}

// The bxl instruction (opcode 1) calculates the bitwise XOR of register B and the
// instruction's literal operand, then stores the result in register B.
fn bxl(
    pc: Number,
    operand: Number,
    a: Number,
    mut b: Number,
    c: Number,
    out: Vec<Number>,
) -> (Number, Number, Number, Number, Vec<Number>) {
    let result = operand ^ b;

    b = result;

    (pc, a, b, c, out)
}

// The bst instruction (opcode 2) calculates the value of its combo operand modulo 8
// (thereby keeping only its lowest 3 bits), then writes that value to the B register.
fn bst(
    pc: Number,
    operand: Number,
    a: Number,
    mut b: Number,
    c: Number,
    out: Vec<Number>,
) -> (Number, Number, Number, Number, Vec<Number>) {
    let operand_value = combo_operand(operand, a, b, c);

    let result = operand_value % 8;

    b = result;

    (pc, a, b, c, out)
}

// The jnz instruction (opcode 3) does nothing if the A register is 0.
// However, if the A register is not zero, it jumps by setting the instruction pointer to the
// value of its literal operand; if this instruction jumps, the instruction pointer is not
// increased by 2 after this instruction.
fn jnz(
    mut pc: Number,
    operand: Number,
    a: Number,
    b: Number,
    c: Number,
    out: Vec<Number>,
) -> (Number, Number, Number, Number, Vec<Number>) {
    // Dont jump
    if a == 0 {
        pc += 2;
    // Jump
    } else {
        pc = operand;
    }

    (pc, a, b, c, out)
}

// The bxc instruction (opcode 4) calculates the bitwise XOR of register B and register C,
// then stores the result in register B.
// (For legacy reasons, this instruction reads an operand but ignores it.)
fn bxc(
    pc: Number,
    _operand: Number,
    a: Number,
    mut b: Number,
    c: Number,
    out: Vec<Number>,
) -> (Number, Number, Number, Number, Vec<Number>) {
    let result = b ^ c;

    b = result;

    (pc, a, b, c, out)
}

// The out instruction (opcode 5) calculates the value of its combo operand modulo 8,
// then outputs that value.
// (If a program outputs multiple values, they are separated by commas.)
fn out(
    pc: Number,
    operand: Number,
    a: Number,
    b: Number,
    c: Number,
    mut out: Vec<Number>,
) -> (Number, Number, Number, Number, Vec<Number>) {
    let operand_value = combo_operand(operand, a, b, c);

    let result = operand_value % 8;

    out.push(result);

    (pc, a, b, c, out)
}

// Combo operands 0 through 3 represent literal values 0 through 3.
// Combo operand 4 represents the value of register A.
// Combo operand 5 represents the value of register B.
// Combo operand 6 represents the value of register C.
// Combo operand 7 is reserved and will not appear in valid programs.
fn combo_operand(operand: Number, a: Number, b: Number, c: Number) -> Number {
    match operand {
        0..=3 => operand,
        4 => a,
        5 => b,
        6 => c,
        _ => {
            eprintln!(
                "Invalid program: found an invalid combo operand: {}",
                operand
            );
            process::exit(1);
        }
    }
}

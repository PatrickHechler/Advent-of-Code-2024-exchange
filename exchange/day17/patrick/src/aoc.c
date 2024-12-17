/*
 * aoc.c
 *
 *  Created on: Dec 2, 2024
 *      Author: pat
 */
#include "interactive.h"
#include "aoc.h"
#include "hash.h"

#include "term.h"

#include <ctype.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <errno.h>
#include <search.h>
#include <string.h>

struct data* read_data(const char *path);

int day = 17;
int part = 2;
FILE *solution_out;
#ifdef INTERACTIVE
static int interactive = 0;
#endif

#define starts_with(str, start) !memcmp(str, start, sizeof(start) - 1)

typedef long num;
#define NUMF "%ld"
#define NUM(n) n##L
typedef double fpnum;
#define FPNUMF "%g"
#define FPNUM(n) n

struct pos {
	num x;
	num y;
};

struct data {
	num ip;
	num regs[3];
	char *instructions;
	size_t inst_size;
	char *out;
	size_t out_size;
	size_t max_out_size;
};

static int do_print = 1;

static void print(FILE *str, struct data *data, uint64_t result) {
	if (result) {
		fprintf(str, STEP_HEADER "result=%s\n" STEP_BODY, u64toa(result));
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print) {
		return;
	}
	fprintf(str, "Register A: "NUMF"\n"
	/*		   */"Register B: "NUMF"\n"
	/*		   */"Register C: "NUMF"\n"
	/*	 	   */"Instr Pntr: "NUMF"\n"
	/*		  */"Program:\n", data->regs[0], data->regs[1], data->regs[2],
			data->ip);
	print_opcodes = 1;
	for (char *i = data->instructions, *end = i + data->inst_size; i < end;
			++i) {
		num d = i - data->instructions;
		char *after = "";
		if (d == data->ip) {
			after = " <-- IP";
		}
		fprintf(str, " [0x%2o]: %d :", d, (int) *i);
		if (!(*i & 1) && i + 1 < end) {
			opcodes[*i](data, i[1]);
		}
	}
	print_opcodes = 0;
	if (data->out) {
		fprintf(str, STEP_FOOTER "Output: %.*s\n", data->out_size, data->out);
	}
	fputs(STEP_FINISHED, str);
}

static num get_combo_op(struct data *data, int operant) {
	if (operant <= 3) {
		if (print_opcodes) {
			printf("%d", operant);
		}
		return operant;
	}
	if (operant == 7) {
		fprintf(stderr, "invalid combo op: 7\n");
		exit(2);
	}
	if (print_opcodes) {
		fputc('A' + (operant & 3), stdout);
	}
	return data->regs[operant & 3];
}

static num op_0(struct data *data, int operant) {
	// adv
	if (print_opcodes) {
		fputs(" A = A / (1 << ", stdout);
		get_combo_op(data, operant);
		fputs(")\n", stdout);
	} else {
		num a = data->regs[0];
		num op = get_combo_op(data, operant);
		data->regs[0] = a / (UINT64_C(1) << op);
	}
	return data->ip += 2;
}
static num op_1(struct data *data, int operant) {
	// bxl
	if (print_opcodes) {
		fputs(" B = B ^ ", stdout);
		get_combo_op(data, operant);
		fputs("\n", stdout);
	} else {
		data->regs[1] ^= operant;
	}
	return data->ip += 2;
}
static num op_2(struct data *data, int operant) {
	// bst
	if (print_opcodes) {
		fputs(" B = ", stdout);
		get_combo_op(data, operant);
		fputs(" & 7\n", stdout);
	} else {
		num op = get_combo_op(data, operant);
		data->regs[1] = op & 7;
	}
	return data->ip += 2;
}
static num op_3(struct data *data, int operant) {
	// jnz
	if (print_opcodes) {
		printf(" JMP IF (A != 0) TO %d\n", operant);
	} else {
		num a = data->regs[0];
		if (a) {
			return data->ip = operant;
		}
	}
	return data->ip += 2;
}
static num op_4(struct data *data, int operant) {
	// bxc
	if (print_opcodes) {
		fputs(" B = B ^ C\n", stdout);
	} else {
		num c = data->regs[2];
		data->regs[1] ^= c;
	}
	return data->ip += 2;
}
static num op_5(struct data *data, int operant) {
	// out
	if (print_opcodes) {
		fputs(" OUT (", stdout);
		get_combo_op(data, operant);
		fputs(" & 7)\n", stdout);
	} else {
		num op = get_combo_op(data, operant) & 7;
		if (!data->max_out_size) {
			data->out = malloc(16);
			data->max_out_size = 16;
			data->out_size = 1;
			if (part == 1) {
				data->out[0] = op + '0';
			} else {
				data->out[0] = op;
			}
		} else {
			data->out_size += part == 1 ? 2 : 1;
			if (data->out_size > data->max_out_size) {
				data->out = realloc(data->out, data->max_out_size <<= 1);
				if (!data->out) {
					perror("realloc");
					exit(2);
				}
			}
			if (part == 1) {
				data->out[data->out_size - 2] = ',';
				data->out[data->out_size - 1] = op + '0';
			} else {
				data->out[data->out_size - 1] = op;
			}
		}
	}
	return data->ip += 2;
}
static num op_6(struct data *data, int operant) {
	// bdv
	if (print_opcodes) {
		fputs(" B = A / (1 << ", stdout);
		get_combo_op(data, operant);
		fputs(")\n", stdout);
	} else {
		num a = data->regs[0];
		num op = get_combo_op(data, operant);
		data->regs[1] = a / (UINT64_C(1) << op);
	}
	return data->ip += 2;
}
static num op_7(struct data *data, int operant) {
	// cdv
	if (print_opcodes) {
		fputs(" C = A / (1 << ", stdout);
		get_combo_op(data, operant);
		fputs(")\n", stdout);
	} else {
		num a = data->regs[0];
		num op = get_combo_op(data, operant);
		data->regs[2] = a / (UINT64_C(1) << op);
	}
	return data->ip += 2;
}

typedef num (*opcode)(struct data*, int);

static opcode opcodes[] = { op_0, op_1, op_2, op_3, op_4, op_5, op_6, op_7 };

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	num ip = 0;
	int olen;
	/* there is no usually need for simulation */
#ifdef INTERACTIVE
	if (interactive) {
		while (ip >= 0 && ip + 1 < data->inst_size) {
			print(solution_out, data, result);
			char op = data->instructions[ip], arg = data->instructions[ip + 1];
			ip = opcodes[op](data, arg);
		}
		print(solution_out, data, result);
		olen = data->out_size;
	} else
#endif
	olen = outlen(data->regs[0]);
	static char *res = 0;
	if (!res) {
		res = malloc(olen + 1);
	} else {
		res = realloc(res, olen + 1);
	}
#ifdef INTERACTIVE
	if (interactive) {
		memcpy(res, data->out, olen);
	} else
#endif
	for (int i = 0; i < olen; ++i) {
		res[i] = find(data->regs[0], i);
	}
	res[data->out_size] = 0;
	/* remember to free in order to avoid a memory leak in interactive mode */
	free(data->instructions);
	if (data->out) {
		free(data->out);
	}
	free(data);
	return res;
}

static struct data* parse_line(struct data *data, char *line) {
	for (; *line && isspace(*line); ++line)
		;
	if (!*line) {
		return data;
	}
	if (!data) {
		data = calloc(1, sizeof(struct data));
	}
	if (starts_with(line, "Register ")) {
		char *c = line + sizeof("Register ") - 1;
		int idx = *c - 'A';
		if (idx < 0 || idx >= 3) {
			fprintf(stderr, "invalid register: %c\n", *c);
			exit(1);
		}
		if (c[1] != ':') {
			fprintf(stderr, "no ':' after 'Register %c' %c\n", *c, c[1]);
			exit(1);
		}
		data->regs[idx] = strtol(c + 2, &c, 10);
		if (errno) {
			fprintf(stderr, "strtol failed: %s\n", strerror(errno));
			exit(1);
		}
	} else if (starts_with(line, "Program: ")) {
		char *c = line + sizeof("Program: ") - 1;
		char *inst = malloc(16);
		size_t inst_max_size = 16;
		size_t inst_size = 0;
		while (102) {
			if (*c < '0' || *c > '7') {
				fprintf(stderr, "invalid opcode: %c\n", *c);
				exit(1);
			}
			if (++inst_size > inst_max_size) {
				inst = realloc(inst, inst_max_size <<= 1);
			}
			inst[inst_size - 1] = *c - '0';
			if (c[1] != ',') {
				if (!c[1] || isspace(c[1])) {
					c++;
					break;
				}
				fprintf(stderr, "opcode not followed by comma: %c\n", c[1]);
				exit(1);
			}
			c += 2;
		}
		while (*c && isspace(*c))
			++c;
		if (*c) {
			fprintf(stderr, "garbage at end of line: %s", c);
			exit(1);
		}
		data->instructions = inst;
		data->inst_size = inst_size;
	} else {
		fprintf(stderr, "invalid line: %s", line);
		exit(1);
	}
	return data;
}

// common stuff

#ifndef __unix__
ssize_t getline(char **line_buf, size_t *line_len, FILE *file) {
	ssize_t result = 0;
	while (21) {
		if (*line_len == result) {
			size_t len = result ? result * 2 : 64;
			void *ptr = realloc(*line_buf, len);
			if (!ptr) {
				fseek(file, -result, SEEK_CUR);
				return -1;
			}
			*line_len = len;
			*line_buf = ptr;
		}
		ssize_t len = fread(*line_buf + result, 1, *line_len - result, file);
		if (!len) {
			if (!result) {
				return -1;
			}
			if (result == *line_len) {
				void *ptr = realloc(*line_buf, result + 1);
				if (!ptr) {
					fseek(file, -result, SEEK_CUR);
					return -1;
				}
				*line_len = result + 1;
				*line_buf = ptr;
			}
			(*line_buf)[result] = 0;
			return result;
		}
		char *c = memchr(*line_buf + result, '\n', len);
		if (c) {
			ssize_t result2 = c - *line_buf + 1;
			if (result2 == *line_len) {
				void *ptr = realloc(*line_buf, result2 + 1);
				if (!ptr) {
					fseek(file, -*line_len - len, SEEK_CUR);
					return -1;
				}
				*line_len = result2 + 1;
				*line_buf = ptr;
			}
			fseek(file, result2 - result - len, SEEK_CUR);
			(*line_buf)[result2] = 0;
			return result2;
		}
		result += len;
	}
}
char* strchrnul(char *str, char c) {
	char *end = strchr(str, c);
	return end ? end : (str + strlen(str));
}
void* reallocarray(void *ptr, size_t nmemb, size_t size) {
	size_t s = nmemb * size;
	if (s / size != nmemb) {
		errno = EOVERFLOW;
		return 0;
	}
	return realloc(ptr, s);
}
#endif

char* u64toa(uint64_t value) {
	static char result[23];
	if (sprintf(result, "%llu", (unsigned long long) value) <= 0) {
		return 0;
	}
	return result;
}

char* d64toa(int64_t value) {
	static char result[23];
	if (sprintf(result, "%lld", (unsigned long long) value) <= 0) {
		return 0;
	}
	return result;
}

struct data* read_data(const char *path) {
	char *line_buf = 0;
	size_t line_len = 0;
	struct data *result = 0;
	FILE *file = fopen(path, "rb");
	if (!file) {
		perror("fopen");
		abort();
	}
	while (144) {
		ssize_t s = getline(&line_buf, &line_len, file);
		if (s < 0) {
			if (feof(file)) {
				free(line_buf);
				fclose(file);
				return result;
			}
			perror("getline failed");
			fflush(0);
			abort();
		}
		if (strlen(line_buf) != s) {
			fprintf(stderr, "\\0 character in line!");
			abort();
		}
		result = parse_line(result, line_buf);
	}
}

int main(int argc, char **argv) {
	solution_out = stdout;
	char *me = argv[0];
	char *f = 0;
	if (argc > 1) {
#ifdef INTERACTIVE
		if (argc > 4)
#else
		if (argc > 3)
#endif
				{
			print_help: ;
			fprintf(stderr, "usage: %s"
#ifdef INTERACTIVE
							" [interactive]"
#endif
							" [p1|p2] [DATA]\n", me);
			return 1;
		}
		int idx = 1;
		if (!strcmp("help", argv[idx])) {
			goto print_help;
		}
#ifdef INTERACTIVE
		if (!strcmp("interactive", argv[idx])) {
			idx++;
			interactive = 1;
		}
		if (idx < argc)
#endif
		{
			if (!strcmp("p1", argv[idx])) {
				part = 1;
				idx++;
			} else if (!strcmp("p2", argv[idx])) {
				part = 2;
				idx++;
			}
			if (!f && argv[idx]) {
				f = argv[idx++];
			}
			if (f && argv[idx]) {
				goto print_help;
			}
		}
	}
	if (!f) {
		f = "rsrc/data.txt";
	} else if (!strchr(f, '/')) {
		char *f2 = malloc(64);
		if (snprintf(f2, 64, "rsrc/test%s.txt", f) <= 0) {
			perror("snprintf");
			abort();
		}
		f = f2;
	}
#ifdef INTERACTIVE
	if (interactive) {
		printf("execute now day %d part %d on file %s in interactive mode\n",
				day, part, f);
		interact(f);
		return EXIT_SUCCESS;
	}
#endif
	printf("execute now day %d part %d on file %s\n", day, part, f);
	clock_t start = clock();
	const char *result = solve(f);
	clock_t end = clock();
	if (result) {
		uint64_t diff = end - start;
		printf("the result is %s\n"
				"  I needed %lu.%.6lu seconds\n", result, diff / CLOCKS_PER_SEC,
				((diff % CLOCKS_PER_SEC) * 1000000LU) / CLOCKS_PER_SEC);
	} else {
		puts("there is no result");
	}
	return EXIT_SUCCESS;
}

/*
 * aoc.c
 *
 *  Created on: Dec 2, 2024
 *      Author: pat
 */
#include "interactive.h"
#include "aoc.h"
#include "hash.h"

#include <ctype.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <errno.h>
#include <search.h>
#include <string.h>

#ifdef INTERACTIVE
#define INTERACT(...) __VA_ARGS__
#else
#define INTERACT(...)
#endif

struct data* read_data(const char *path);

int day = 23;
int part = 2;
FILE *solution_out;
#ifdef INTERACTIVE
int interactive = 0;
#else
#define interactive 0
#endif

#define starts_with(str, start) !memcmp(str, start, sizeof(start) - 1)

typedef int64_t num;

struct pos {
	num x;
	num y;
};

struct wire {
	char *name;
	unsigned value :1;
	unsigned has_value :1;
};

enum operation {
	op_and, op_or, op_xor
};

struct gate {
	struct wire *in0;
	struct wire *in1;
	struct wire *out;
	enum operation op;
};

struct data {
	struct gate *gates;
	uint64_t valueless_zwire_count;
	uint64_t gates_count;
	uint64_t max_gates_count;
	uint64_t init_result;
	struct hashset wires;
};

static int w_eq(const void *a, const void *b) {
	const struct wire *w0 = a, *w1 = b;
	return !strcmp(w0->name, w1->name);
}
static uint64_t w_h(const void *a) {
	const struct wire *w0 = a;
	uint64_t result = 1;
	for (const char *c = w0->name; *c; ++c)
		result = result * 31 + *c;
	return result;
}
static void w_free(void *a) {
	free(((struct wire*) a)->name);
	free(a);
}

static int do_print = 1;

static void print(FILE *str, struct data *data, uint64_t result) {
	if (result) {
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print && !interactive) {
		return;
	}
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = data->init_result;
	for (int iter = 0; data->valueless_zwire_count; ++iter) {
		print(solution_out, data, result);
		printf("start iteration %d\n", iter);
		for (uint64_t i = 0; i < data->gates_count; ++i) {
			if (data->gates[i].in0->has_value
					&& data->gates[i].in1->has_value) {
				unsigned a = data->gates[i].in0->value;
				unsigned b = data->gates[i].in1->value;
				unsigned dst;
				switch (data->gates[i].op) {
				case op_and:
					dst = a && b;
					break;
				case op_or:
					dst = a || b;
					break;
				case op_xor:
					dst = a ^ b;
					break;
				default:
					exit(4);
				}
				if (data->gates[i].out->has_value) {
					data->gates[i].out->value = dst;
				} else {
					data->gates[i].out->value = dst;
					data->gates[i].out->has_value = 1;
					if (data->gates[i].out->name[0] == 'z') {
						char *end;
						unsigned long bit = strtoul(
								data->gates[i].out->name + 1, &end, 10);
						if (*end || errno) {
							fprintf(stderr,
									"zwire has non numbers after the number\n");
							exit(1);
						}
						if (bit >= 64) {
							fprintf(stderr, "bit out of 64-bit range\n");
							exit(1);
						}
						result &= ~(UINT64_C(1) << bit);
						result |= ((uint64_t) dst) << bit;
						if (!--data->valueless_zwire_count) {
							break;
						}
					}
				}
			}
		}
	}
	print(solution_out, data, result);
	free(data);
	return u64toa(result);
}

static void* comp_wire(void *param, void *element) {
	struct wire *result = malloc(sizeof(struct wire));
	result->name = strdup(((struct wire*) element)->name);
	result->value = 0;
	result->has_value = 0;
	if (result->name[0] == 'z') {
		++((struct data*) param)->valueless_zwire_count;
	}
	return result;
}

static struct data* parse_line(struct data *data, char *line) {
	for (; *line && isspace(*line); ++line)
		;
	if (!*line) {
		return data;
	}
	if (!data) {
		data = malloc(sizeof(struct data));
		data->wires = (struct hashset ) { .hash = w_h, .equal = w_eq, .free =
						w_free };
		data->valueless_zwire_count = 0;
		data->gates = 0;
		data->gates_count = 0;
		data->max_gates_count = 0;
	}
	if (!data->max_gates_count) {
		char *col = strchr(line, ':');
		if (col) {
			*col = 0;
			struct wire *w = malloc(sizeof(struct wire));
			w->name = strdup(line);
			char *end;
			unsigned long l = strtoul(col + 1, &end, 2);
			if (errno || l > 1) {
				fprintf(stderr, "invalid line: %s:%s", line, col);
				exit(1);
			}
			w->value = l;
			w->has_value = 1;
			while (*end && isspace(*end))
				++end;
			if (*end) {
				fprintf(stderr, "invalid line: %s:%s", line, col);
				exit(1);
			}
			if (hs_set(&data->wires, w)) {
				fprintf(stderr, "wire %s initialized multiple times!\n",
						w->name);
				exit(1);
			}
			if (w->name[0] == 'z') {
				unsigned long bit = strtoul(w->name + 1, &end, 10);
				if (*end || errno) {
					fprintf(stderr, "zwire has non numbers after the number\n");
					exit(1);
				}
				if (bit >= 64) {
					fprintf(stderr, "bit out of 64-bit range\n");
					exit(1);
				}
				if (w->value) {
					data->init_result |= UINT64_C(1) << bit;
				}
			}
			return data;
		}
		data->gates = malloc(sizeof(struct gate) * 16);
		data->gates_count = 1;
		data->max_gates_count = 16;
	} else if (++data->gates_count >= data->max_gates_count) {
		data->gates = reallocarray(data->gates, data->max_gates_count <<= 1,
				sizeof(struct gate));
	}
	char *sep0 = strchr(line, ' ');
	if (!sep0) {
		fprintf(stderr, "invalid line: %s", line);
		exit(1);
	}
	*(sep0++) = 0;
	char *sep1 = strchr(sep0, ' ');
	if (!sep1) {
		fprintf(stderr, "invalid line: %s %s", line, sep0);
		exit(1);
	}
	*(sep1++) = 0;
	char *sep2 = strstr(sep1, " -> ");
	if (!sep2) {
		fprintf(stderr, "invalid line: %s %s %s", line, sep0, sep1);
		exit(1);
	}
	*sep2 = 0;
	sep2 += 4;
	char *end = sep2 + strlen(sep2);
	while (end > sep2 && isspace(end[-1]))
		--end;
	if (end <= sep2) {
		fprintf(stderr, "invalid line: %s %s %s %s", line, sep0, sep1, sep2);
		exit(1);
	}
	*end = 0;
	struct wire *w0 = hs_compute_absent(&data->wires, &line, data, comp_wire);
	struct wire *w1 = hs_compute_absent(&data->wires, &sep1, data, comp_wire);
	struct wire *w2 = hs_compute_absent(&data->wires, &sep2, data, comp_wire);
	data->gates[data->gates_count - 1].in0 = w0;
	data->gates[data->gates_count - 1].in1 = w1;
	data->gates[data->gates_count - 1].out = w2;
	if (!strcmp(sep0, "OR")) {
		data->gates[data->gates_count - 1].op = op_or;
	} else if (!strcmp(sep0, "XOR")) {
		data->gates[data->gates_count - 1].op = op_xor;
	} else if (!strcmp(sep0, "AND")) {
		data->gates[data->gates_count - 1].op = op_and;
	} else {
		fprintf(stderr, "invalid operation '%s' in line: %s %s %s %s", sep0,
				line, sep0, sep1, sep2);
		exit(1);
	}
	return data;
}

// common stuff

#if !(AOC_COMPAT & AC_POSIX)
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
#endif // AC_POSIX
#if !(AOC_COMPAT & AC_STRCN)
char* strchrnul(char *str, int c) {
	char *end = strchr(str, c);
	return end ? end : (str + strlen(str));
}
#endif // AC_STRCN
#if !(AOC_COMPAT & AC_REARR)
void* reallocarray(void *ptr, size_t nmemb, size_t size) {
	size_t s = nmemb * size;
	if (s / size != nmemb) {
		errno = ENOMEM;
		return 0;
	}
	return realloc(ptr, s);
}
#endif // AC_REARR

char* u64toa(uint64_t value) {
	static char result[21];
	if (sprintf(result, "%"I64"u", value) <= 0) {
		return 0;
	}
	return result;
}

char* d64toa(int64_t value) {
	static char result[21];
	if (sprintf(result, "%"I64"d", value) <= 0) {
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
	}
	interact(f, interactive);
#endif
	printf("execute now day %d part %d on file %s\n", day, part, f);
	clock_t start = clock();
	const char *result = solve(f);
	clock_t end = clock();
	if (result) {
		uint64_t diff = end - start;
		printf("the result is %s\n"
				"  I needed %"I64"u.%.6"I64"u seconds\n", result,
				diff / CLOCKS_PER_SEC,
				((diff % CLOCKS_PER_SEC) * UINT64_C(1000000)) / CLOCKS_PER_SEC);
	} else {
		puts("there is no result");
	}
	return EXIT_SUCCESS;
}

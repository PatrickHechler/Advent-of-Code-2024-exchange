/*
 * aoc.c
 *
 *  Created on: Dec 2, 2024
 *      Author: pat
 */

#include "aoc.h"

#include <ctype.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <errno.h>
#include <search.h>

#define DAY 07
int part = 2;

#define NUMF "%ld"
typedef long num;

typedef struct equation {
	num result;
	num rvalue_count;
	num values[];
} *equation;

struct data {
	equation *equations;
	size_t equations_count;
	size_t max_equations_count;
};

static num calc(num target, num first, num count, num *nums) {
	if (!count) {
		return first;
	}
	num second = *nums;
	num mul = first * second;
	num res = calc(target, mul, count - 1, nums + 1);
	if (res == target) {
		return res;
	}
	num add = first + second;
	res = calc(target, add, count - 1, nums + 1);
	if (part == 1 || res == target) {
		return res;
	}
	num first_mul = 1;
	for (num second_second = second; second_second;
			second_second /= 10, first_mul *= 10)
		;
	if (first_mul == 1) {
		first_mul = 10;
	}
	first *= first_mul;
	first += second;
	return calc(target, first, count - 1, nums + 1);
}

char* solve(char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	for (int ei = 0; ei < data->equations_count; ++ei) {
		equation eq = data->equations[ei];
		printf(NUMF ":", eq->result);
		for (int i = 0; i < eq->rvalue_count; ++i) {
			printf(" " NUMF, eq->values[i]);
		}
		num res = calc(eq->result, eq->values[0], eq->rvalue_count - 1,
				eq->values + 1);
		if (res == eq->result) {
			puts(" -- true");
			result += res;
		} else {
			printf(" -- " NUMF "\n", res);
		}
	}
	return u64toa(result);
}

static struct data* parse_line(struct data *data, char *line) {
	for (; *line && isspace(*line); ++line)
		;
	if (!*line) {
		return data;
	}
	if (!data) {
		data = malloc(sizeof(struct data));
		data->equations = malloc(sizeof(equation) * 16);
		data->equations_count = 0;
		data->max_equations_count = 16;
	}
	if (++data->equations_count >= data->max_equations_count) {
		data->equations = reallocarray(data->equations,
				data->max_equations_count <<= 1, sizeof(equation));
	}
	equation eq = malloc(sizeof(struct equation) + sizeof(num) * 16);
	int max_eq_size = 16;
	char *ptr;
	eq->result = strtol(line, &ptr, 10);
	if (*ptr != ':') {
		fprintf(stderr, "invalid line: %s", line);
		abort();
	}
	ptr++;
	eq->rvalue_count = 0;
	while (58) {
		for (; *ptr && isspace(*ptr); ++ptr)
			;
		if (!*ptr) {
			if (!eq->rvalue_count) {
				fprintf(stderr, "no rvalues: %s", line);
				abort();
			}
			eq = realloc(eq,
					sizeof(struct equation) + sizeof(num) * eq->rvalue_count);
			data->equations[data->equations_count - 1] = eq;
			return data;
		}
		if (++eq->rvalue_count >= max_eq_size) {
			eq = realloc(eq,
					sizeof(struct equation)
							+ sizeof(num) * (max_eq_size <<= 1));
		}
		eq->values[eq->rvalue_count - 1] = strtol(ptr, &ptr, 10);
		if (errno) {
			perror("strtol");
			fprintf(stderr, "error while parsing line %s", line);
			abort();
		}
	}
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
#endif

char* u64toa(uint64_t value) {
	char *result = malloc(21);
	if (sprintf(result, "%llu", (unsigned long long) value) <= 0) {
		free(result);
		return 0;
	}
	return result;
}

char* d64toa(int64_t value) {
	char *result = malloc(21);
	if (sprintf(result, "%lld", (unsigned long long) value) <= 0) {
		free(result);
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
	char *me = argv[0];
	char *f = 0;
	if (argc > 1) {
		if (argc > 4) {
			fprintf(stderr, "usage: %s [p1|p2] [DATA]\n", me);
			return 1;
		}
		int idx = 1;
		if (!strcmp("help", argv[idx])) {
			fprintf(stderr, "usage: %s [p1|p2] [DATA]\n", me);
			return 1;
		} else if (!strcmp("p1", argv[idx])) {
			part = 1;
			f = argv[idx + 1] ? argv[idx + 1] : 0;
		} else if (!strcmp("p2", argv[idx])) {
			part = 2;
			f = argv[idx + 1] ? argv[idx + 1] : 0;
		} else if (argv[idx + 1]) {
			fprintf(stderr, "usage: %s [p1|p2] [DATA]\n", me);
			return 1;
		} else {
			f = argv[idx];
		}
	}
	parse_end: if (!f) {
		f = "rsrc/data.txt";
	} else if (!strchr(f, '/')) {
		char *f2 = malloc(64);
		if (snprintf(f2, 64, "rsrc/test%s.txt", f) <= 0) {
			perror("snprintf");
			abort();
		}
		f = f2;
	}
	printf("execute now day %d part %d on file %s\n", DAY, part, f);
	clock_t start = clock();
	char *result = solve(f);
	clock_t end = clock();
	if (result) {
		uint64_t diff = end - start;
		printf("the result is %s\n"
				"  I needed %lu.%.6lu seconds\n", result, diff / CLOCKS_PER_SEC,
				((diff % CLOCKS_PER_SEC) * 1000000LU) / CLOCKS_PER_SEC);
	} else {
		puts("there is no result");
	}
	return 0;
}

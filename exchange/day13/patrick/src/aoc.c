/*
 * aoc.c
 *
 *  Created on: Dec 2, 2024
 *      Author: pat
 */

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

int day = 12;
int part = 2;

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

struct mashine {
	struct pos a;
	struct pos b;
	struct pos prize;
};

struct data {
	struct mashine *mashines;
	size_t mashines_count;
	size_t max_mashines_count;
};

void print_mashine(FILE *str, struct mashine *m) {
	fprintf(str,
			/*	  */"Button A: X+%ld, Y+%ld\n" //
					"Button B: X+%ld, Y+%ld\n"//
					"Prize: X=%ld, Y=%ld\n", m->a.x, m->a.y, m->b.x, m->b.y,
			m->prize.x, m->prize.y);
}

static void print(FILE *str, struct data *data, uint64_t result) {
	fprintf(str, "result=%s\n", u64toa(result));
	for (off_t l = 0; l < data->mashines_count; ++l) {
		print_mashine(str, data->mashines + l);
	}
}

static uint64_t add(uint64_t a, uint64_t b) {
	uint64_t result = a + b;
	if (a > INT64_MAX && result <= INT64_MAX) {
		fprintf(stderr, "OVERFLOW!");
		exit(5);
	}
	return result;
}

static char* solve(char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	for (off_t i = 0; i < data->mashines_count; ++i) {
		struct mashine *m = data->mashines + i;
		// p = a * 3 + b
		// t.x = a * a.x + b * b.x
		// t.y = a * a.y + b * b.y
		if (part == 2) {
			m->prize.x += NUM(10000000000000);
			m->prize.y += NUM(10000000000000);
		}
//		print_mashine(stdout, m);
		// calculate a
		// t.y = a * a.y + b * b.y	 | - (b * b.y)
		// t.y - b * b.y = a * a.y	 | / a.y
		// (t.y - b * b.y) / a.y = a

		// calculate b
		// t.x = a * a.x + b * b.x									 | replace a
		// t.x = ((t.y - b * b.y) / a.y) * a.x + b * b.x			 | / a.x
		// t.x / a.x = (t.y - b * b.y) / a.y + b * b.x / a.x		 | * a.y
		// t.x / a.x * a.y = t.y - b * b.y + b * b.x / a.x * a.y	 | - t.y
		// t.x / a.x * a.y - t.y = - b * b.y + b * b.x / a.x * a.y	 | ()
		// t.x / a.x * a.y - t.y = b * (b.x / a.x * a.y - b.y)		 | / (b.x / a.x * a.y - b.y)
		fpnum b = (fpnum) m->prize.x / m->a.x * m->a.y - m->prize.y;
		// (t.x / a.x * a.y - t.y) / (b.x / a.x * a.y - b.y) = b
		b /= (fpnum) m->b.x / m->a.x * m->a.y - m->b.y;
		// (t.y - b * b.y) / a.y = a
		fpnum a = ((fpnum) m->prize.y - b * m->b.y) / m->a.y;
		num bn = b, an = a;
		if (b - bn > 0.5) {
			bn++;
		}
		if (a - an > 0.5) {
			an++;
		}
		if (m->prize.x != m->a.x * an + m->b.x * bn
				|| m->prize.y != m->a.y * an + m->b.y * bn) {
			printf(
					"  %ld is bad ("NUMF":"FPNUMF" = "NUMF" * "FPNUMF" + "NUMF" * "FPNUMF")\n",
					i + 1, m->prize.x, m->a.x * a + m->b.x * b, m->a.x, a,
					m->b.x, b);
			continue;
		}
		if (an < 0 || bn < 0) {
			printf("  %ld is negative\n", i + 1);
			continue;
		}
		uint64_t add_result = add(add(add(an, an), an), bn);
		printf(
				"  %ld is good ("NUMF" = "NUMF" * "NUMF" + "NUMF" * "NUMF") (prize=%s)\n",
				i + 1, m->prize.x, m->a.x, an, m->b.x, bn, u64toa(add_result));
		if (part == 1 && (an > 100 || bn > 100)) {
			continue;
		}
		result = add(result, add_result);
		continue;
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
		data->mashines = malloc(sizeof(struct mashine) * 16);
		data->mashines_count = 0;
		data->max_mashines_count = 16;
	}
	struct pos *pos;
	char assign;
	if (!memcmp(line, "Button A: X+", sizeof("Button A: X+") - 1)) {
		if (++data->mashines_count >= data->max_mashines_count) {
			data->mashines = reallocarray(data->mashines,
					data->max_mashines_count <<= 1, sizeof(struct mashine));
		}
		pos = &data->mashines[data->mashines_count - 1].a;
		assign = '+';
	} else if (!data->mashines_count) {
		abort();
	} else if (!memcmp(line, "Button B: X+", sizeof("Button B: X+") - 1)) {
		pos = &data->mashines[data->mashines_count - 1].b;
		assign = '+';
	} else if (!memcmp(line, "Prize: X=", sizeof("Prize: X=") - 1)) {
		pos = &data->mashines[data->mashines_count - 1].prize;
		assign = '=';
	} else {
		abort();
	}
	char *ptr = strchr(line, assign) + 1;
	pos->x = strtol(ptr, &ptr, 10);
	if (errno) {
		perror("strtol");
		abort();
	}
	if (memcmp(ptr, ", Y", sizeof(", Y") - 1)
			|| ptr[sizeof(", Y") - 1] != assign) {
		abort();
	}
	ptr += sizeof(", Y");
	pos->y = strtol(ptr, &ptr, 10);
	if (errno) {
		perror("strtol");
		abort();
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
	char *me = argv[0];
	char *f = 0;
	if (argc > 1) {
		if (argc > 3) {
			print_help: ;
			fprintf(stderr, "usage: %s"
					" [p1|p2] [DATA]\n", me);
			return 1;
		}
		int idx = 1;
		if (!strcmp("help", argv[idx])) {
			goto print_help;
		}
		if (!strcmp("p1", argv[idx])) {
			part = 1;
			idx++;
		} else if (!strcmp("p2", argv[idx])) {
			part = 2;
			idx++;
		}
		if (f) {
			if (argv[idx]) {
				goto print_help;
			}
		} else if (argv[idx] && argv[idx + 1]) {
			goto print_help;
		} else if (argv[idx]) {
			f = argv[idx];
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
	printf("execute now day %d part %d on file %s\n", day, part, f);
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
	return EXIT_SUCCESS;
}

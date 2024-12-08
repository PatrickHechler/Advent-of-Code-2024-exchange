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

#define DAY 8
int part = 2;

#define NUMF "%d"
typedef int num;

struct pos {
	num x;
	num y;
};

static int compare_pos(const void *a, const void *b) {
	const struct pos *fa = a, *fb = b;
	if (fa->x > fb->x) {
		return 1;
	}
	if (fa->x < fb->x) {
		return -1;
	}
	if (fa->y > fb->y) {
		return 1;
	}
	if (fa->y < fb->y) {
		return -1;
	}
	return 0;
}

typedef struct pos antenna;

typedef struct frequency {
	void *antennas;
	char value;
} freq;

static int compare_freq(const void *a, const void *b) {
	const struct frequency *fa = a, *fb = b;
//	printf("compare freq=%p(%c : %p) with freq=%p(%c : %p)\n", fa, fa->value,
//			fa->antennas, fb, fb->value, fb->antennas);
	return fa->value - fb->value;
}

struct data {
	char **lines;
	size_t line_count;
	size_t max_line_count;
	size_t line_length;
	void *freqs;
};

static void print(FILE *str, struct data *data, uint64_t result) {
	fprintf(str, "result=%s\n", u64toa(result));
	for (int l = 0; l < data->line_count; ++l) {
		fprintf(str, "%s\n", data->lines[l]);
	}
}

static struct data *data;
static const freq *cur_f;

static void calc_antenna_antinods(const void *nodep, VISIT value, int level) {
	const antenna *a = *(antenna**) nodep;
	if (value == leaf || value == endorder) {
		static const antenna *cur_a = 0;
		if (cur_a) {
			if (compare_pos(a, cur_a) <= 0) {
				return;
			}
			num x_diff = a->x - cur_a->x;
			num y_diff = a->y - cur_a->y;
			if (part == 1) {
				num x0 = a->x + x_diff;
				num y0 = a->y + y_diff;
				num x1 = cur_a->x - x_diff;
				num y1 = cur_a->y - y_diff;
				if (x0 >= 0 && y0 >= 0 && x0 < data->line_length
						&& y0 < data->line_count) {
					data->lines[y0][x0] = '#';
				} else {
					printf(
							"the antinode 0 (" NUMF "|" NUMF ") is out of map (%ld|%ld)\n",
							x0, y0, data->line_length, data->line_count);
				}
				if (x1 >= 0 && y1 >= 0 && x1 < data->line_length
						&& y1 < data->line_count) {
					data->lines[y1][x1] = '#';
				} else {
					printf(
							"the antinode 1 (" NUMF "|" NUMF ") is out of map (%ld|%ld)\n",
							x1, y1, data->line_length, data->line_count);
				}
			} else {
				// should x_diff and y_diff be shorted?
				int redo = 0;
				do {
					num x = a->x;
					num y = a->y;
					do {
						data->lines[y][x] = '#';
						x += x_diff;
						y += y_diff;
					} while (x >= 0 && y >= 0 && x < data->line_length
							&& y < data->line_count);
					redo = !redo;
					x_diff = -x_diff;
					y_diff = -y_diff;
				} while (redo);
			}
		} else {
			cur_a = a;
			twalk(cur_f->antennas, calc_antenna_antinods);
			cur_a = 0;
		}
	}
}
static void calc_freq_antinods(const void *nodep, VISIT value, int level) {
	const freq *f = *(freq**) nodep;
	if (value == leaf || value == endorder) {
		cur_f = f;
		twalk(f->antennas, calc_antenna_antinods);
		cur_f = 0;
	}
}

static void print_antenna(const void *nodep, VISIT value, int level) {
	const antenna *a = *(antenna**) nodep;
	if (value == leaf || value == endorder) {
		printf("  antenna(" NUMF "|" NUMF ")\n", a->x, a->y);
	}
}
static void print_freq(const void *nodep, VISIT value, int level) {
	const freq *f = *(freq**) nodep;
	if (value == leaf || value == endorder) {
		printf(" freq=%p(%c : %p)\n", f, f->value, f->antennas);
		twalk(f->antennas, print_antenna);
	}
}

char* solve(char *path) {
	data = read_data(path);
	uint64_t result = 0;
	print(stdout, data, result);
	for (int l = 0; l < data->line_count; ++l) {
		for (char *c = data->lines[l]; *c; ++c) {
			if (*c == '.') {
				continue;
			}
			if (!isalnum(*c)) {
				fprintf(stderr, "");
			}
			freq fs = { .value = *c }, *f;
			freq **f2 = tfind(&fs, &data->freqs, compare_freq);
			if (!f2) {
				f = malloc(sizeof(freq));
				f->antennas = 0;
				f->value = *c;
//				printf("create new freq=%p(%c : %p)\n", f, f->value,
//						f->antennas);
				tsearch(f, &data->freqs, compare_freq);
			} else {
				f = *f2;
			}
			antenna *a = malloc(sizeof(antenna));
			a->y = l;
			a->x = c - data->lines[l];
//			printf("before: freq=%p(%c : %p)\n", f, f->value, f->antennas);
//			twalk(f->antennas, print_antenna);
//			twalk(data->freqs, print_freq);
			void *a2 = tsearch(a, &f->antennas, compare_pos);
//			printf("after: freq=%p(%c : %p)\n", f, f->value, f->antennas);
//			twalk(f->antennas, print_antenna);
//			twalk(data->freqs, print_freq);
			if (*(void**) a2 != a) {
				abort();
			}
		}
	}
//	twalk(data->freqs, print_freq);
	twalk(data->freqs, calc_freq_antinods);
	for (int l = 0; l < data->line_count; ++l) {
		for (char *c = strchr(data->lines[l], '#'); c; c = strchr(c + 1, '#')) {
			result++;
		}
	}
	print(stdout, data, result);
	return u64toa(result);
}

static struct data* parse_line(struct data *data, char *line) {
	for (; *line && isspace(*line); ++line)
		;
	if (!*line) {
		return data;
	}
	char *end = line + strlen(line);
	while (isspace(*--end))
		;
	end++;
	if (!data) {
		data = malloc(sizeof(struct data));
		data->lines = malloc(sizeof(char*) * 16);
		data->line_count = 0;
		data->max_line_count = 16;
		data->line_length = end - line;
	}
	if (data->line_length != end - line) {
		fprintf(stderr, "input is no rectangle!");
		abort();
	}
	if (++data->line_count >= data->max_line_count) {
		data->lines = reallocarray(data->lines, data->max_line_count <<= 1,
				sizeof(char*));
	}
	*end = '\0';
	data->lines[data->line_count - 1] = strdup(line);
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

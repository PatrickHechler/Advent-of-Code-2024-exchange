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

int day = 12;
int part = 2;

typedef int num;
#define NUMF "%d"

struct pos {
	num x;
	num y;
};

struct data {
	char **lines;
	size_t line_count;
	size_t max_line_count;
	size_t line_length;
};

static void print(FILE *str, struct data *data, uint64_t result) {
	fprintf(str, "result=%s\n", u64toa(result));
	for (int l = 0; l < data->line_count; ++l) {
		fprintf(str, "%s\n", data->lines[l]);
	}
}

#define REGION_WORK_CHAR (part == 2 ? (char) 4 : '\377')

static int is_border(struct data *data, char c, char nc, num x, num y) {
	if (x < 0 || y < 0 || x >= data->line_length || y >= data->line_count) {
		return 1;
	}
	return data->lines[y][x] != c && data->lines[y][x] != nc;
}

static uint64_t calc_region_dir_border(struct data *data, num x, num y,
		int call);

static uint64_t calc_region_dir_border_if_match(struct data *data, char c,
		num x, num y, int call) {
	if (x < 0 || y < 0 || x >= data->line_length || y >= data->line_count) {
		return 0;
	}
	if (data->lines[y][x] != c) {
		return 0;
	}
	return calc_region_dir_border(data, x, y, call);
}

static uint64_t calc_region_side(struct data *data, char c, char nc, num x,
		num y, num xadd, num yadd, int call) {
	data->lines[y][x] = nc;
	num nx = x + xadd;
	num ny = y + yadd;
	if (!is_border(data, c, nc, nx, ny)) {
		uint64_t result = 0;
		result += calc_region_dir_border_if_match(data, c, x + 1, y, call);
		result += calc_region_dir_border_if_match(data, c, x - 1, y, call);
		result += calc_region_dir_border_if_match(data, c, x, y + 1, call);
		result += calc_region_dir_border_if_match(data, c, x, y - 1, call);
		return result;
	}
	num dirxadd2 = yadd;
	num diryadd2 = xadd;
	int search_run = 1;
	uint64_t result = 1;
	while (78) {
		for (num xadd2 = dirxadd2 * search_run, yadd2 = diryadd2 * search_run;
				y + yadd2 < data->line_count && x + xadd2 < data->line_length;
				xadd2 += dirxadd2, yadd2 += diryadd2) {
			if (data->lines[y + yadd2][x + xadd2] != c
					&& data->lines[y + yadd2][x + xadd2] != nc) {
				break;
			}
			if (!search_run) {
				nx = x + xadd2;
				ny = y + yadd2;
				result += calc_region_dir_border_if_match(data, c, nx + 1, ny,
						call);
				result += calc_region_dir_border_if_match(data, c, nx - 1, ny,
						call);
				result += calc_region_dir_border_if_match(data, c, nx, ny + 1,
						call);
				result += calc_region_dir_border_if_match(data, c, nx, ny - 1,
						call);
			} else {
				nx = x + xadd + xadd2;
				ny = y + yadd + yadd2;
				if (!is_border(data, c, nc, nx, ny)) {
					break;
				}
				data->lines[y + yadd2][x + xadd2] = nc;
			}
		}
		if (xadd != diryadd2 || yadd != dirxadd2) {
			if (!search_run) {
				break;
			}
			search_run = 0;
		}
		dirxadd2 = -dirxadd2;
		diryadd2 = -diryadd2;
	}
	return result;
}

static uint64_t calc_region_dir_border(struct data *data, num x, num y,
		int call) {
	char c = data->lines[y][x];
	char nc = call;
	uint64_t result = 0;
	if (call == 1) {
		result += calc_region_side(data, c, nc, x, y, 0, 1, call);
	} else if (call == 2) {
		result += calc_region_side(data, c, nc, x, y, 0, -1, call);
	} else if (call == 3) {
		result += calc_region_side(data, c, nc, x, y, 1, 0, call);
	} else if (call == 4) {
		result += calc_region_side(data, c, nc, x, y, -1, 0, call);
	} else {
		abort();
	}
	return result;
}

static uint64_t calc_region_border_p1(struct data *data, num x, num y) {
	char c = data->lines[y][x];
	data->lines[y][x] = REGION_WORK_CHAR;
	uint64_t result = 0;
	if (x && data->lines[y][x - 1] == c) {
		result += calc_region_border_p1(data, x - 1, y);
	} else if (!x || data->lines[y][x - 1] != REGION_WORK_CHAR) {
		result++;
	}
	if (y && data->lines[y - 1][x] == c) {
		result += calc_region_border_p1(data, x, y - 1);
	} else if (!y || data->lines[y - 1][x] != REGION_WORK_CHAR) {
		result++;
	}
	if (x + 1 < data->line_length && data->lines[y][x + 1] == c) {
		result += calc_region_border_p1(data, x + 1, y);
	} else if (x + 1
			>= data->line_length|| data->lines[y][x + 1] != REGION_WORK_CHAR) {
		result++;
	}
	if (y + 1 < data->line_count && data->lines[y + 1][x] == c) {
		result += calc_region_border_p1(data, x, y + 1);
	} else if (y + 1
			>= data->line_count|| data->lines[y + 1][x] != REGION_WORK_CHAR) {
		result++;
	}
	return result;
}

static uint64_t calc_region_border(struct data *data, num x, num y) {
	if (part == 2) {
		uint64_t l_result = calc_region_dir_border(data, x, y, 1);
		uint64_t r_result = calc_region_dir_border(data, x, y, 2);
		uint64_t u_result = calc_region_dir_border(data, x, y, 3);
		uint64_t d_result = calc_region_dir_border(data, x, y, 4);
		return l_result + r_result + u_result + d_result;
	}
	return calc_region_border_p1(data, x, y);
}

static uint64_t calc_region_area(struct data *data, num x, num y) {
	if (x < 0 || y < 0 || x >= data->line_length || y >= data->line_count) {
		return 0;
	}
	if (data->lines[y][x] != REGION_WORK_CHAR) {
		return 0;
	}
	data->lines[y][x] = '\0';
	uint64_t result = 1;
	result += calc_region_area(data, x - 1, y);
	result += calc_region_area(data, x + 1, y);
	result += calc_region_area(data, x, y - 1);
	result += calc_region_area(data, x, y + 1);
	return result;
}

static uint64_t calc_region(struct data *data, num x, num y) {
	char c = data->lines[y][x];
	uint64_t border = calc_region_border(data, x, y);
	uint64_t area = calc_region_area(data, x, y);
	uint64_t result = border * area;
	char *b = strdup(u64toa(border));
	char *a = strdup(u64toa(area));
	printf(
			"the region (" NUMF "|" NUMF "):%c has a border of %s and area %s ==> result is %s\n",
			x, y, c, b, a, u64toa(result));
	free(b);
	free(a);
	return result;
}

static char* solve(char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	print(stdout, data, result);
	for (num y = 0; y < data->line_count; ++y) {
		for (num x = 0; x < data->line_length; ++x) {
			if (!data->lines[y][x]) {
				continue;
			}
			result += calc_region(data, x, y);
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

// interactive stuff
#ifdef INTERACTIVE
enum cache_policy keep = keep_all;

struct data* copy_data(struct data *data) {
	struct data *result = malloc(sizeof(struct data));
	result->lines = malloc(sizeof(char*) * data->line_count);
	result->line_count = data->line_count;
	result->max_line_count = data->line_count;
	result->line_length = data->line_length;
	for (off_t i = 0; i < data->line_count; ++i) {
		result->lines[i] = malloc(data->line_length + 1);
		memcpy(result->lines[i], data->lines[i], data->line_length + 1);
	}
	return result;
}

void free_data(struct data *data) {
	for (off_t i = 0; i < data->line_count; ++i) {
		free(data->lines[i]);
	}
	free(data);
}

int next_data(struct data *data) {
	return 0;
}

void world_sizes(struct data *data, struct coordinate *min,
		struct coordinate *max) {
	min->x = 0;
	min->y = 0;
	max->x = data->line_count - 1;
	max->y = data->line_length - 1;
}

size_t get(struct data *data, off_t x, off_t y, size_t text_size, char *buf,
		size_t buf_len) {
	if (y < 0 || x + text_size < 0 || y >= data->line_count
			|| (x > 0 && x >= data->line_length)) {
		return snprintf(buf, buf_len, FRMT_CURSOR_FORWARD, (int) text_size);
	}
	size_t result = 0;
#define maxsub(a,b) a = (a <= b ? 0 : a - b)
	if (x < 0) {
		int s = -x;
		size_t len = skip_columns(buf, buf_len, s);
		buf += len;
		result += len;
		text_size += x; // x is negative
		maxsub(buf_len, len);
		x = 0;
	}
	int last = 256;
	char *line = data->lines[y];
	for (off_t i = 0; i < data->line_length && text_size;
			++i, --text_size, ++result) {
		char c = line[i];
		if (c != last) {
			last = c;
			int r = 128, g = 128, b = 128;
			if (c >= 'A' && c <= 'Z') {
				if (c % 3 == 0) {
					r = (c - '@') * 91;
					g = (c - 'D') * 31;
					b = (c - 'Z') * 7;
				} else if (c % 3 == 1) {
					g = (c - '@') * 71;
					b = (c - 'D') * 31;
					r = (c - 'Z') * 7;
				} else {
					b = (c - '@') * 71;
					r = (c - 'D') * 31;
					g = (c - 'Z') * 7;
				}
				r = 64 + (r & 127);
				g = 64 + (g & 127);
				b = 64 + (b & 127);
			}
			size_t len = snprintf(buf, buf_len, FRMT_FC_RGB, r, g, b);
			buf += len;
			result += len;
			maxsub(buf_len, len);
		}
		if (buf_len) {
			*(buf++) = c;
			--buf_len;
		}
	}
	if (text_size) {
		result += snprintf(buf, buf_len, FRMT_CURSOR_FORWARD, (int) text_size);
	}
	return result;
}

#endif

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
void* reallocarray(void*ptr, size_t nmemb, size_t size) {
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
#ifdef INTERACTIVE
	int interactive = 0;
#endif
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
			if (f) {
				if (argv[idx]) {
					goto print_help;
				}
			} else if (!argv[idx] || argv[idx + 1]) {
				goto print_help;
			} else {
				f = argv[idx];
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

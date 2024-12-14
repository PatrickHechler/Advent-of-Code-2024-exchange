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

int day = 14;
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

struct robot {
	struct pos pos;
	struct pos vec;
};

struct data {
	struct robot *robots;
	size_t robots_count;
	size_t max_robots_count;
	struct robot *orig_robots;
};

static size_t x_count, y_count;

void print_robot(FILE *str, struct robot *r) {
	fprintf(str, "p="NUMF","NUMF"  \tv="NUMF","NUMF"\n", r->pos.x, r->pos.y,
			r->vec.x, r->vec.y);
}

static void fill_buf(struct data *data, char *buf) {
	memset(buf, '.', (x_count + 1) * y_count);
	for (off_t y = 1; y <= y_count; ++y) {
		buf[(x_count + 1) * y - 1] = '\n';
	}
	for (off_t l = 0; l < data->robots_count; ++l) {
		struct robot *r = data->robots + l;
		if (++buf[r->pos.x + (x_count + 1) * r->pos.y] == '.' + 1) {
			buf[r->pos.x + (x_count + 1) * r->pos.y] = '1';
		} else if (buf[r->pos.x + (x_count + 1) * r->pos.y] > '9') {
			buf[r->pos.x + (x_count + 1) * r->pos.y] = '9' + 1;
		}
	}
}

static void print(FILE *str, struct data *data, uint64_t result) {
	fprintf(str, "result=%s\n", u64toa(result));
	static char *buf = 0;
	if (!buf) {
		buf = malloc((x_count + 1) * y_count + 1);
		buf[(x_count + 1) * y_count] = '\0';
	}
	fill_buf(data, buf);
	fputs(buf, str);
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
	if (part == 2) {
		int main(int argc, char **argv);
		char *argv[4] = { "progname", "interactive", path, NULL };
		int e = main(3, argv);
		exit(e);
	}
	struct data *data = read_data(path);
	uint64_t result = 0;
	for (int tick = 0; tick < 100; ++tick) {
		print(stdout, data, result);
		for (off_t i = 0; i < data->robots_count; ++i) {
			struct robot *r = data->robots + i;
			r->pos.x = (r->pos.x + r->vec.x + x_count) % x_count;
			r->pos.y = (r->pos.y + r->vec.y + y_count) % y_count;
		}
	}
	uint64_t top_left = 0, top_right = 0, bottom_left = 0, bottom_right = 0;
	for (off_t i = 0; i < data->robots_count; ++i) {
		struct robot *r = data->robots + i;
		if (r->pos.x > x_count / 2) { // right
			if (r->pos.y > y_count / 2) { // top
				top_right++;
			} else if (r->pos.y < y_count / 2) { // bottom
				bottom_right++;
			}
		} else if (r->pos.x < x_count / 2) { // left
			if (r->pos.y > y_count / 2) { // top
				top_left++;
			} else if (r->pos.y < y_count / 2) { // bottom
				bottom_left++;
			}
		}
	}
	result = top_left * top_right * bottom_left * bottom_right;
	print(stdout, data, result);
	printf("tl=%lu tr=%lu bl=%lu br=%lu\n", top_left, top_right, bottom_left,
			bottom_right);
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
		data->robots = malloc(sizeof(struct robot) * 16);
		data->robots_count = 0;
		data->max_robots_count = 16;
		data->orig_robots = 0;
	}
	char assign;
	if (line[0] != 'p' || line[1] != '=') {
		inval: fprintf(stderr, "invalid line: %s", line);
		abort();
	}
	char *ptr;
	if (++data->robots_count > data->max_robots_count) {
		data->robots = reallocarray(data->robots, data->max_robots_count <<= 1,
				sizeof(struct robot));
	}
	struct robot *r = data->robots + data->robots_count - 1;
	r->pos.x = strtol(line + 2, &ptr, 10);
	if (errno) {
		perror("strtol");
		goto inval;
	}
	if (*ptr != ',') {
		goto inval;
	}
	r->pos.y = strtol(ptr + 1, &ptr, 10);
	if (errno) {
		perror("strtol");
		goto inval;
	}
	if (ptr[0] != ' ' || ptr[1] != 'v' || ptr[2] != '=') {
		goto inval;
	}
	r->vec.x = strtol(ptr + 3, &ptr, 10);
	if (errno) {
		perror("strtol");
		goto inval;
	}
	if (*ptr != ',') {
		goto inval;
	}
	r->vec.y = strtol(ptr + 1, &ptr, 10);
	if (errno) {
		perror("strtol");
		goto inval;
	}
	while (*ptr && isspace(*ptr))
		ptr++;
	if (*ptr) {
		goto inval;
	}
	return data;
}

#ifdef INTERACTIVE
enum cache_policy keep = keep_last;

struct data* copy_data(struct data *data) {
	struct data *result = malloc(sizeof(struct data));
	result->max_robots_count = data->robots_count;
	result->robots_count = data->robots_count;
	result->robots = malloc(sizeof(struct robot) * data->robots_count);
	if (!result->robots) {
		perror("malloc");
		exit(8);
	}
	result->orig_robots = data->orig_robots;
	memcpy(result->robots, data->robots,
			sizeof(struct robot) * data->robots_count);
	return result;
}

void free_data(struct data *data) {
	free(data->robots);
	free(data);
}

static void empty_area(char *buf, off_t x, off_t y) {
	if (x < 0 || y < 0 || x >= x_count || y >= x_count) {
		return;
	}
	if (buf[x + (x_count + 1) * y] == '.') {
		return;
	}//10403
	buf[x + (x_count + 1) * y] = '.';
	for (int xa = 1; xa < 1; ++xa) {
		for (int ya = 1; ya < 1; ++ya) {
			empty_area(buf, x - xa, y);
			empty_area(buf, x + xa, y);
			empty_area(buf, x, y - ya);
			empty_area(buf, x, y + ya);
			empty_area(buf, x - xa, y - ya);
			empty_area(buf, x - xa, y + ya);
			empty_area(buf, x + xa, y - ya);
			empty_area(buf, x + xa, y + ya);
		}
	}
}

int next_data(struct data *data) {
	if (!data->orig_robots) {
		data->orig_robots = malloc(sizeof(struct robot) * data->robots_count);
		memcpy(data->orig_robots, data->robots,
				sizeof(struct robot) * data->robots_count);
	} else if (!memcmp(data->orig_robots, data->robots,
			sizeof(struct robot) * data->robots_count)) {
		return 0;
	}
	static char *buf = 0;
	static size_t lxc = 0;
	static size_t lyc = 0;
	if (lxc != x_count || lyc != y_count) {
		if (buf) {
			free(buf);
		}
		lxc = x_count;
		lyc = y_count;
		buf = malloc((x_count + 1) * y_count + 1);
		buf[(x_count + 1) * y_count] = '\0';
	}
	fill_buf(data, buf);
	int first = 238;
	int second = 239;
	printf("buffer:\n%s\n", buf);
	for (off_t y = 0; y < y_count; ++y) {
		for (off_t x = 0; x < x_count; ++x) {
			char c = buf[x + (x_count + 1) * y];
			if (c == '.') {
				continue;
			}
			if (!first) {
				second = 0;
				break;
			}
			first = 0;
			empty_area(buf, x, y);
		}
	}
	if (second) {
		return 0;
	}
	for (off_t i = 0; i < data->robots_count; ++i) {
		struct robot *r = data->robots + i;
		r->pos.x = (r->pos.x + r->vec.x + x_count) % x_count;
		r->pos.y = (r->pos.y + r->vec.y + y_count) % y_count;
	}
	return 1;
}

void world_sizes(struct data *data, struct coordinate *min,
		struct coordinate *max) {
	min->x = 0;
	min->y = 0;
	max->x = x_count - 1;
	max->y = y_count - 1;
}

size_t get(struct data *data, off_t x, off_t y, size_t text_size, char *buf0,
		size_t buf_len) {
	static char buf2[101];
	size_t result = 0;
	memset(buf2, 0, 101);
	for (off_t i = 0; i < data->robots_count; ++i) {
		struct robot *r = data->robots + i;
		if (r->pos.y == y) {
			buf2[r->pos.x]++;
		}
	}
#define add(buf_add, txt_add) \
	if (buf_add > buf_len) { \
		buf_len = 0; \
	} else { \
		buf_len -= buf_add; \
	} \
	if (text_size >= txt_add) { \
		text_size -= txt_add; \
	} else { \
		abort(); \
	} \
	result += buf_add; \
	buf0 += buf_add;

	if (x < 0) {
		int len = -x > text_size ? text_size : -x;
		size_t add = skip_columns(buf0, buf_len, len);
		add(add, len);
		x = 0;
		if (!text_size) {
			return result;
		}
	}
	int add = snprintf(buf0, buf_len, FC_GRAY);
	add(add, 0);
	int is_back = 251;
	for (; x < x_count && text_size; ++x) {
		if (buf2[x]) {
			int c = buf2[x] > 9 ? '9' + 1 : '0' + buf2[x];
			if (is_back) {
				int add = snprintf(buf0, buf_len, FC_GREEN "%c", c);
				add(add, 1);
			} else {
				if (buf_len) {
					*buf0 = c;
				}
				add(1, 1);
			}
			is_back = 0;
		} else {
			if (is_back) {
				if (buf_len) {
					*buf0 = '.';
				}
				add(1, 1);
			} else {
				int add = snprintf(buf0, buf_len, FC_GRAY ".");
				add(add, 1);
			}
			is_back = 265;
		}
	}
//	size_t add_len = skip_columns(buf0, buf_len, text_size);
//	add(add, text_size);
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
	if (strcmp(path, "rsrc/data.txt")) {
		x_count = 11;
		y_count = 7;
	} else {
		x_count = 101;
		y_count = 103;
	}
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

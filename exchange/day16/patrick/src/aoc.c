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

int day = 16;
int part = 2;

typedef long num;
#define NUMF "%ld"
#define NUM(n) n##L
typedef double fpnum;
#define FPNUMF "%g"
#define FPNUM(n) n

enum dir {
	x_inc, y_inc, x_dec, y_dec,
};

struct pos {
	num x;
	num y;
};

struct reindeer {
	num x;
	num y;
	enum dir dir;
	num score;
};

struct data {
	char **lines;
	num line_length;
	num line_count;
	size_t max_line_count;
};

static void print(FILE *str, struct data *data, struct hashset *all,
		struct hashset *new, uint64_t result) {
	if (result != UINT64_MAX) {
		fprintf(str, STEP_HEADER "current best result=%s\n" STEP_BODY,
				u64toa(result));
	} else {
		fputs(STEP_BODY, str);
	}
	for (num y = 0; y < data->line_count; ++y) {
		struct reindeer r = { .y = y };
		for (num x = 0; x < data->line_length; ++x) {
			r.x = x;
			r.dir = x_inc;
			int in_new = !!hs_get(new, &r);
			r.dir = y_inc;
			in_new = in_new || !!hs_get(new, &r);
			r.dir = x_dec;
			in_new = in_new || !!hs_get(new, &r);
			r.dir = y_dec;
			in_new = in_new || !!hs_get(new, &r);
			r.dir = x_inc;
			int in_all = !!hs_get(all, &r);
			r.dir = y_inc;
			in_all = in_all || !!hs_get(all, &r);
			r.dir = x_dec;
			in_all = in_all || !!hs_get(all, &r);
			r.dir = y_dec;
			in_all = in_all || !!hs_get(all, &r);
			if (in_all && in_new) {
				fputc('+', str);
			} else if (in_all) {
				fputc('A', str);
			} else if (in_new) {
				fputc('N', str);
			} else {
				fputc(data->lines[y][x], str);
			}
		}
		fputc('\n', str);
	}
	fputs(STEP_FINISHED, str);
}

static uint64_t r_h(const void *a) {
	const struct reindeer *p = a;
	return (p->x << 8) ^ (p->y << 16) ^ p->dir;
}

static int r_eq(const void *a0, const void *b0) {
	const struct reindeer *a = a0, *b = b0;
	return a->x == b->x && a->y == b->y && a->dir == b->dir;
}

static void* compute(void *param, void *old_val, void *new_val) {
	if (!old_val) {
		if (param) {
			goto check_other;
		}
		return new_val;
	}
	struct reindeer *or = old_val, *nr = new_val;
	if (or->score <= nr->score) {
		return or;
	}
	if (!param) {
		return nr;
	}
	check_other: or = hs_get(param, nr);
	if (!or || or->score > nr->score) {
		struct reindeer *result = malloc(sizeof(struct reindeer));
		*result = *nr;
		return result;
	}
	return 0;
}

static int f_add(void *param, void *element) {
	if (hs_compute(param, element, 0, compute) != element) {
		free(element);
	}
	return 0;
}

struct sa {
	struct data *data;
	struct hashset *all;
	struct hashset *fill;
	uint64_t result;
};
/*
 #################
 #...#...#...#..E#
 #.#.#.#.#.#.#.#^#
 #.#.#.#...#...#^#
 #.#.#.#.###.#.#^#
 #>>v#.#.#.....#^#
 #^#v#.#.#.#####^#
 #^#v..#.#.#>>>>^#
 #^#v#####.#^###.#
 #^#v#..>>>>^#...#
 #^#v###^#####.###
 #^#v#>>^#.....#.#
 #^#v#^#####.###.#
 #^#v#^........#.#
 #^#v#^#########.#
 #S#>>^..........#
 #################
 */
static int v_step(void *param, void *element) {
	struct sa *arg = param;
	struct reindeer *r = element;
	num xadd, yadd;
	switch (r->dir) {
	case x_inc:
		xadd = 1;
		yadd = 0;
		break;
	case x_dec:
		xadd = -1;
		yadd = 0;
		break;
	case y_inc:
		xadd = 0;
		yadd = 1;
		break;
	case y_dec:
		xadd = 0;
		yadd = -1;
		break;
	}
	num x = r->x, y = r->y;
	uint64_t score = r->score;
	if (xadd) {
		if (arg->data->lines[y - 1][x] != '#') {
			struct reindeer rd = { .x = x, .y = y, .dir = y_dec, .score = score
					+ 1000 };
			if (rd.score <= arg->result) {
				hs_compute(arg->fill, &rd, arg->all, compute);
			}
		}
		if (arg->data->lines[y + 1][x] != '#') {
			struct reindeer rd = { .x = x, .y = y, .dir = y_inc, .score = score
					+ 1000 };
			if (rd.score <= arg->result) {
				hs_compute(arg->fill, &rd, arg->all, compute);
			}
		}
	} else {
		if (arg->data->lines[y][x - 1] != '#') {
			struct reindeer rd = { .x = x, .y = y, .dir = x_dec, .score = score
					+ 1000 };
			if (rd.score <= arg->result) {
				hs_compute(arg->fill, &rd, arg->all, compute);
			}
		}
		if (arg->data->lines[y][x + 1] != '#') {
			struct reindeer rd = { .x = x, .y = y, .dir = x_inc, .score = score
					+ 1000 };
			if (rd.score <= arg->result) {
				hs_compute(arg->fill, &rd, arg->all, compute);
			}
		}
	}
	while (score < arg->result) {
		x += xadd;
		y += yadd;
		score++;
		char c = arg->data->lines[y][x];
		if (c == '.' || c == 'S') {
			struct reindeer rd =
					{ .x = x, .y = y, .dir = r->dir, .score = score };
			hs_compute(arg->fill, &rd, arg->all, compute);
			continue;
		}
		if (c == '#')
			break;
		if (c == 'E') {
			arg->result = score;
			return 0;
		}
		fprintf(stderr, "invalid char %c\n", c);
		exit(1);
	}
	return 0;
}

static uint64_t do_step(struct data *data, struct hashset *all,
		struct hashset *new, struct hashset *fill, uint64_t result) {
	struct sa arg = { .data = data, .all = all, .fill = fill, .result = result };
	hs_for_each(new, &arg, v_step);
	return arg.result;
}

static char* solve(char *path) {
	struct data *data = read_data(path);
	uint64_t result = UINT64_MAX;
	struct hashset new = { .equal = r_eq, .hash = r_h, };
	struct hashset fill = { .equal = r_eq, .hash = r_h, };
	struct hashset all = { .equal = r_eq, .hash = r_h, .free = free, };
	struct pos s;
	for (num y = 0; y < data->line_count; ++y) {
		char *start = strchr(data->lines[y], 'S');
		if (start) {
			s.y = y;
			s.x = start - data->lines[y];
			break;
		}
	}
	struct reindeer *r = malloc(sizeof(struct reindeer));
	r->score = 0;
	r->dir = x_inc;
	r->x = s.x;
	r->y = s.y;
	hs_add(&new, r);
	long l = 0;
	while (new.entry_count) {
		print(stdout, data, &all, &new, result);
		result = do_step(data, &all, &new, &fill, result);
		hs_filter(&new, &all, f_add);
		hs_filter(&fill, &new, f_add);
	}
	print(stdout, data, &all, &new, result);
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
	*end = '\0';
	if (!data) {
		data = malloc(sizeof(struct data));
		data->lines = malloc(sizeof(char*) * 16);
		data->line_count = 0;
		data->max_line_count = 16;
		data->line_length = end - line;
	}
	if (data->line_length != end - line) {
		fprintf(stderr, "world is no recrangle!\n");
		exit(2);
	}
	if (*line != '#' || end[-1] != '#') {
		fprintf(stderr, "world is no invalid: %s\n", line);
		exit(2);
	}
	if (++data->line_count > data->max_line_count) {
		data->lines = reallocarray(data->lines, data->max_line_count <<= 1,
				sizeof(char*));
	}
	data->lines[data->line_count - 1] = strdup(line);
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

int next_data(struct data *data) {
	if (!data->orig_robots) {
		data->orig_robots = malloc(sizeof(struct robot) * data->robots_count);
		memcpy(data->orig_robots, data->robots,
				sizeof(struct robot) * data->robots_count);
	} else if (!memcmp(data->orig_robots, data->robots,
			sizeof(struct robot) * data->robots_count)) {
		return 0;
	}
	if (!has_next_world(data)) {
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

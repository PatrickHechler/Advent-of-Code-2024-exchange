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

int day = 15;
int part = 2;

typedef long num;
#define NUMF "%ld"
#define NUM(n) n##L
typedef double fpnum;
#define FPNUMF "%g"
#define FPNUM(n) n

typedef struct pos {
	num x;
	num y;
} pos;

typedef pos robot;

struct data {
	char **lines;
	num line_length;
	num line_count;
	size_t max_line_count;
	char *movements;
	size_t movements_count;
	size_t max_movements_count;
};

static void print(FILE *str, struct data *data, uint64_t result) {
	fprintf(str, "result=%s\n", u64toa(result));
	for (int l = 0; l < data->line_count; ++l) {
		fprintf(str, "%s\n", data->lines[l]);
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

int can_step_robot2(struct data *data, num x, num y, num addx, num addy) {
	num tx = x + addx, ty = y + addy;
	char c = data->lines[ty][tx];
	if (addy) {
		if (c == '[') {
			return can_step_robot2(data, tx, ty, addx, addy)
					&& can_step_robot2(data, tx + 1, ty, addx, addy);
		}
		if (c == ']') {
			return can_step_robot2(data, tx, ty, addx, addy)
					&& can_step_robot2(data, tx - 1, ty, addx, addy);
		}
	} else if (c == '[' || c == ']') {
		return can_step_robot2(data, tx, ty, addx, addy);
	}
	if (c == '#') {
		return 0;
	}
	if (c == '.') {
		return 1;
	}
	fprintf(stderr, "ERROR");
	exit(12);
}

void do_step_robot2(struct data *data, num x, num y, num addx, num addy) {
	num tx = x + addx, ty = y + addy;
	char c = data->lines[ty][tx];
	if (addy) {
		if (c == '[') {
			do_step_robot2(data, tx, ty, addx, addy);
			do_step_robot2(data, tx + 1, ty, addx, addy);
			data->lines[ty][tx + 1] = '.';
			data->lines[ty + addy][tx + 1] = ']';
		} else if (c == ']') {
			do_step_robot2(data, tx, ty, addx, addy);
			do_step_robot2(data, tx - 1, ty, addx, addy);
			data->lines[ty][tx - 1] = '.';
			data->lines[ty + addy][tx - 1] = '[';
		}
	} else if (c == '[' || c == ']') {
		do_step_robot2(data, tx, ty, addx, addy);
	}
	data->lines[ty][tx] = data->lines[y][x];
}

void step_robot(struct data *data, robot *bot, char m) {
	num addx = 0, addy = 0;
	switch (m) {
	case '^':
		addy = -1;
		break;
	case 'v':
		addy = 1;
		break;
	case '<':
		addx = -1;
		break;
	case '>':
		addx = 1;
		break;
	default:
		fprintf(stderr, "invalid movement %c\n", m);
		exit(1);
	}
	if (part == 2) {
		if (!can_step_robot2(data, bot->x, bot->y, addx, addy)) {
			return;
		}
		do_step_robot2(data, bot->x, bot->y, addx, addy);
		data->lines[bot->y][bot->x] = '.';
		bot->x += addx;
		bot->y += addy;
		return;
	}
	while (100) {
		bot->x += addx;
		bot->y += addy;
		char t = data->lines[bot->y][bot->x];
		if (t == '.') {
			while (105) {
				char move = data->lines[bot->y - addy][bot->x - addx];
				data->lines[bot->y][bot->x] = move;
				if (move == '@') {
					data->lines[bot->y - addy][bot->x - addx] = '.';
					break;
				}
				bot->x -= addx;
				bot->y -= addy;
			}
			break;
		} else if (t == '#') {
			while (data->lines[bot->y][bot->x] != '@') {
				bot->x -= addx;
				bot->y -= addy;
			}
			break;
		} else if (t != 'O') {
			fprintf(stderr, "invalid character in map %c\n", t);
			exit(1);
		}
	}
}

static char* solve(char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	robot bot = { 0, 0 };
	for (num y = 0; y < data->line_count; ++y) {
		char *c = strchr(data->lines[y], '@');
		if (!c) {
			continue;
		}
		bot.x = c - data->lines[y];
		bot.y = y;
		break;
	}
	if (!bot.x) {
		fprintf(stderr, "bot not found!\n");
		exit(1);
	}
	for (char *m = data->movements; *m; ++m) {
//		print(stdout, data, result);
		step_robot(data, &bot, *m);
		printf("move %c\n", *m);
	}
	print(stdout, data, result);
	char box = part == 2 ? '[' : 'O';
	for (num y = 0; y < data->line_count; ++y) {
		for (char *c = strchr(data->lines[y], box); c; c = strchr(c + 1, box)) {
			result += 100 * y + (c - data->lines[y]);
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
	*end = '\0';
	if (!data) {
		data = malloc(sizeof(struct data));
		data->lines = malloc(sizeof(char*) * 16);
		data->line_count = 0;
		data->max_line_count = 16;
		data->line_length = end - line;
		data->movements = 0;
		data->movements_count = 0;
		data->max_movements_count = 0;
	}
	if (*line != '#' || data->line_length != end - line
			|| data->max_movements_count) {
		size_t old_mc = data->movements_count;
		if (!data->max_movements_count) {
			data->movements = malloc(sizeof(char*) * 128);
			data->movements_count = 0;
			data->max_movements_count = 16;
		}
		data->movements_count += end - line;
		while (data->movements_count >= data->max_movements_count) {
			data->movements = reallocarray(data->movements,
					data->max_movements_count <<= 1, sizeof(char*));
		}
		memcpy(data->movements + old_mc, line, end - line);
		data->movements[data->movements_count] = 0;
	} else {
		if (++data->line_count > data->max_line_count) {
			data->lines = reallocarray(data->lines, data->max_line_count <<= 1,
					sizeof(char*));
		}
		if (part == 2) {
			data->lines[data->line_count - 1] = malloc((end - line) * 2 + 1);
			char *dst = data->lines[data->line_count - 1];
			char *src = line;
			while (*src) {
				switch (*src) {
				case '#':
					dst[0] = '#';
					dst[1] = '#';
					break;
				case '.':
					dst[0] = '.';
					dst[1] = '.';
					break;
				case 'O':
					dst[0] = '[';
					dst[1] = ']';
					break;
				case '@':
					dst[0] = '@';
					dst[1] = '.';
					break;
				default:
					fprintf(stderr, "invalid map char: %c\n", *src);
					exit(1);
				}
				src++;
				dst += 2;
			}
			*dst = '\0';
		} else {
			data->lines[data->line_count - 1] = strdup(line);
		}
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
				f = argv[idx];
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

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

static const char dtc[] = { '>', 'v', '<', '^' };

struct pos {
	num x;
	num y;
};

struct reindeer {
	struct pos pos;
	enum dir dir;
	num score;
	struct hashset hist;
};

struct data {
	char **lines;
	num line_length;
	num line_count;
	size_t max_line_count;
};

struct pr_arg {
	const char *name;
	FILE *str;
};

static int v_print_p(void *param, void *element) {
	struct pos *p = element;
	fprintf(param, " ("NUMF"|"NUMF")", p->x, p->y);
	return 0;
}

static int v_print_r(void *param, void *element) {
	struct pr_arg *arg = param;
	struct reindeer *r = element;
	fprintf(arg->str, "%s: score=%s, cur=("NUMF"|"NUMF":%c)", arg->name,
			u64toa(r->score), r->pos.x, r->pos.y, dtc[r->dir]);
	hs_for_each(&r->hist, arg->str, v_print_p);
	fputc('\n', arg->str);
	return 0;
}

static int do_print = 0;

static void print(FILE *str, struct data *data, struct hashset *all,
		struct hashset *new, uint64_t result) {
	if (result != UINT64_MAX) {
		fprintf(str, STEP_HEADER "current best result=%s\n" STEP_BODY,
				u64toa(result));
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print) {
		return;
	}
	for (num y = 0; y < data->line_count; ++y) {
		struct reindeer r = { .pos.y = y };
		for (num x = 0; x < data->line_length; ++x) {
			r.pos.x = x;
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
	fputs(STEP_FOOTER, str);
	struct pr_arg arg = { .name = "all", .str = str };
	hs_for_each(all, &arg, v_print_r);
	arg.name = "new";
	hs_for_each(new, &arg, v_print_r);
	fputs(STEP_FINISHED, str);
}

static uint64_t r_h(const void *a) {
	const struct reindeer *p = a;
	uint64_t result = 1;
	result = 13 * result + p->dir;
	result = 31 * result + p->pos.x;
	result = 71 * result + p->pos.y;
	return result;
}

static int r_eq(const void *a0, const void *b0) {
	const struct reindeer *a = a0, *b = b0;
	return a->pos.x == b->pos.x && a->pos.y == b->pos.y && a->dir == b->dir;
}

static uint64_t p_h(const void *a) {
	const struct pos *p = a;
	uint64_t result = 1;
	result = 31 * result + p->x;
	result = 71 * result + p->y;
	return result;
}

static int p_eq(const void *a0, const void *b0) {
	const struct pos *a = a0, *b = b0;
	return a->x == b->x && a->y == b->y;
}

static void* new_pos(void *param, void *element) {
	struct pos *result = malloc(sizeof(struct pos));
	*result = *(struct pos*) element;
	return result;
}

static int vf_pos_add(void *param, void *element) {
	hs_compute_absent(param, element, 0, new_pos);
	return 0;
}

static void* compute(void *param, void *old_val, void *new_val) {
	if (!old_val) {
		if (param) {
			goto check_other;
		}
		if (part == 2) {
			goto return_new;
		}
		return new_val;
	}
	struct reindeer *or = old_val, *nr = new_val;
	if (or->score <= nr->score) {
		if (part == 2 && or->score == nr->score) {
			hs_for_each(&nr->hist, &or->hist, vf_pos_add);
		}
		return or;
	}
	if (!param) {
		goto return_new;
	}
	check_other: or = hs_get(param, nr);
	if (!or || or->score > nr->score) {
		return_new: ;
		struct reindeer *result = malloc(sizeof(struct reindeer));
		struct hashset hs = { .equal = p_eq, .hash = p_h, .free = free };
		hs_for_each(&nr->hist, &hs, vf_pos_add);
		*result = *nr;
		result->hist = hs;
		return result;
	}
	return 0;
}

static int vf_add(void *param, void *element) {
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
	struct hashset *best;
};

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
	struct pos p = { .x = r->pos.x, .y = r->pos.y };
	uint64_t score = r->score;
	if (xadd) {
		if (arg->data->lines[p.y - 1][p.x] != '#') {
			struct reindeer rd = { .pos = p, .dir = y_dec,
					.score = score + 1000, .hist = r->hist };
			if (rd.score <= arg->result) {
				hs_compute(arg->fill, &rd, arg->all, compute);
			}
		}
		if (arg->data->lines[p.y + 1][p.x] != '#') {
			struct reindeer rd = { .pos = p, .dir = y_inc,
					.score = score + 1000, .hist = r->hist };
			if (rd.score <= arg->result) {
				hs_compute(arg->fill, &rd, arg->all, compute);
			}
		}
	} else {
		if (arg->data->lines[p.y][p.x - 1] != '#') {
			struct reindeer rd = { .pos = p, .dir = x_dec,
					.score = score + 1000, .hist = r->hist };
			if (rd.score <= arg->result) {
				hs_compute(arg->fill, &rd, arg->all, compute);
			}
		}
		if (arg->data->lines[p.y][p.x + 1] != '#') {
			struct reindeer rd = { .pos = p, .dir = x_inc,
					.score = score + 1000, .hist = r->hist };
			if (rd.score <= arg->result) {
				hs_compute(arg->fill, &rd, arg->all, compute);
			}
		}
	}
	struct hashset hs = { .equal = p_eq, .hash = p_h, .free = free };
	hs_for_each(&r->hist, &hs, vf_pos_add);
	while (score < arg->result) {
		p.x += xadd;
		p.y += yadd;
		score++;
		hs_set(&hs, new_pos(0, &p));
		static long l = 0;
		printf("l=%ld\n", ++l);
		char c = arg->data->lines[p.y][p.x];
		if (c == '.') {
			struct reindeer rd = { .pos = p, .dir = r->dir, .score = score,
					.hist = hs };
			hs_compute(arg->fill, &rd, arg->all, compute);
			//TODO add again continue;
			break;
		}
		if (c == '#')
			break;
		if (c == 'E') {
			if (arg->result != score) {
				hs_clear(arg->best);
			}
			hs_for_each(&hs, arg->best, vf_pos_add);
			arg->result = score;
			break;
		}
		fprintf(stderr, "invalid char %c\n", c);
		exit(1);
	}
	hs_clear(&hs);
	return 0;
}

static uint64_t do_step(struct data *data, struct hashset *all,
		struct hashset *new, struct hashset *fill, uint64_t result,
		struct hashset *best) {
	struct sa arg = { .data = data, .all = all, .fill = fill, .result = result,
			.best = best };
	hs_for_each(new, &arg, v_step);
	return arg.result;
}

static int clear_pos(void *param, void *element) {
	struct reindeer *r = element;
	hs_clear(&r->hist);
	return 0;
}

static char* solve(char *path) {
	struct data *data = read_data(path);
	uint64_t result = UINT64_MAX;
	struct hashset new = { .equal = r_eq, .hash = r_h, };
	struct hashset fill = { .equal = r_eq, .hash = r_h, };
	struct hashset all = { .equal = r_eq, .hash = r_h, .free = free, };
	struct hashset best = { .equal = p_eq, .hash = p_h, .free = free, };
	struct pos s;
	for (num y = 0; y < data->line_count; ++y) {
		char *start = strchr(data->lines[y], 'S');
		if (start) {
			s.y = y;
			s.x = start - data->lines[y];
			*start = '.';
			break;
		}
	}
	struct reindeer *r = calloc(1, sizeof(struct reindeer));
	r->dir = x_inc;
	r->pos = s;
	r->hist.equal = p_eq;
	r->hist.hash = p_h;
	r->hist.free = free;
	hs_add(&r->hist, new_pos(0, &s));
	hs_add(&new, r);
	while (new.entry_count) {
		print(stdout, data, &all, &new, result);
		result = do_step(data, &all, &new, &fill, result, &best);
		hs_filter(&new, &all, vf_add);
		hs_filter(&fill, &new, vf_add);
	}
	print(stdout, data, &all, &new, result);
	if (part == 2) {
		result = best.entry_count;
	}
	hs_for_each(&all, 0, clear_pos);
	hs_clear(&all);
	hs_clear(&new);
	hs_clear(&fill);
	hs_clear(&best);
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

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

struct computer {
	char *name;
	struct hashset connected;
};

struct three_comps {
	struct computer *a;
	struct computer *b;
	struct computer *c;
};
struct data {
	struct hashset comps;
};

static int do_print = 1;

static int v_print_neighbor(void *param, void *element) {
	FILE *str = param;
	struct computer *comp = element;
	fprintf(str, ":%s", comp->name);
	return 0;
}

static int v_print(void *param, void *element) {
	FILE *str = param;
	struct computer *comp = element;
	fprintf(str, "    %s", comp->name);
	hs_for_each(&comp->connected, str, v_print_neighbor);
	fputc('\n', str);
	return 0;
}

static int v_print2(void *param, void *element) {
	FILE *str = param;
	struct three_comps *tc = element;
	fprintf(str, " %s:%s:%s", tc->a->name, tc->b->name, tc->c->name);
	return 0;
}

static void print(FILE *str, struct data *data, struct hashset *tcs,
		uint64_t result) {
	if (result) {
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print && !interactive) {
		return;
	}
	fprintf(str, "  compinations:");
	hs_for_each(tcs, str, v_print2);
	fputs("\n  computers:\n", str);
	hs_for_each(&data->comps, str, v_print);
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

static uint64_t c_h(const void *a) {
	_Static_assert(offsetof(struct computer, name) == 0, "Error!");
	uint64_t result = 1;
	for (const char *c = ((const struct computer*) a)->name; *c; ++c) {
		result = result * 31 + *c;
	}
	return result;
}
static int c_eq(const void *a, const void *b) {
	const struct computer *c0 = a, *c1 = b;
	return !strcmp(c0->name, c1->name);
}
static void c_free(void *a) {
	hs_clear(&((struct computer*) a)->connected);
	free(a);
}

static struct three_comps* tc_new(struct computer *a, struct computer *b,
		struct computer *c) {
	struct three_comps *result = malloc(sizeof(struct three_comps));
	if (a > b) {
		struct computer *t = b;
		b = a;
		a = t;
	}
	if (a > c) {
		struct computer *t = c;
		c = a;
		a = t;
	}
	if (b > c) {
		struct computer *t = c;
		c = b;
		b = t;
	}
	result->a = a;
	result->b = b;
	result->c = c;
	return result;
}
static uint64_t tc_h(const void *a) {
	const struct three_comps *tc = a;
	uint64_t result = 1;
	result = result * 31 + (intptr_t) tc->a;
	result = result * 31 + (intptr_t) tc->b;
	result = result * 31 + (intptr_t) tc->c;
	return result;
}
static int tc_eq(const void *a, const void *b) {
	const struct three_comps *tc0 = a, *tc1 = b;
	return tc0->a == tc1->a && tc0->b == tc1->b && tc0->c == tc1->c;
}

/* n! / ( k! * (n - k)! )
 * assumes that n >= k, k > 0, n > 0 */
//static uint64_t over(uint64_t n, uint64_t k) {
//	uint64_t result = 1;
//	uint64_t max = k;
//	uint64_t min = n - k;
//	if (max < min) {
//		max = min;
//		min = k;
//	}
//	for (uint64_t i = max + 1; i <= n; ++i)
//		result *= i;
//	/* result = n! / max(k,n-k)! */
//	uint64_t div = 1;
//	for (uint64_t i = 1; i <= min; ++i)
//		div *= i;
//	return result / div;
//}
struct cnt_arg {
	struct computer *need;
	struct computer *has;
	struct hashset *tcs;
};
static int v_count(void *param, void *element) {
	struct cnt_arg *arg = param;
	struct computer *com = element;
	if (hs_get(&com->connected, arg->need)) {
		printf("    %s\n", com->name);
		hs_set(arg->tcs, tc_new(arg->need, arg->has, com));
	}
	return 0;
}

struct c_arg {
	struct hashset *tcs;
	struct computer *comp;
};
static int v_calc(void *param, void *element) {
	struct c_arg *arg = param;
	struct computer *com = element;
	struct cnt_arg carg = { arg->comp, com, arg->tcs };
	printf("  search for connections to %s:%s\n", arg->comp->name, com->name);
	hs_for_each(&com->connected, &carg, v_count);
	return 0;
}

static int f_remove(void *param, void *element) {
	struct computer *rm = param;
	struct computer *com = element;
	hs_remove(&com->connected, rm);
	return 0;
}

struct cr_arg {
	struct hashset tcs;
	struct data *data;
};
static int v_calc_result(void *param, void *element) {
	struct cr_arg *arg = param;
	struct computer *com = element;
	size_t start_entry_count = arg->tcs.entry_count;
	if (com->name[0] != 't' || com->connected.entry_count < 2) {
		return 0;
	}
	struct c_arg ccarg = { &arg->tcs, com };
	hs_for_each(&com->connected, &ccarg, v_calc);
	printf("found %"I64"u new combinations for %s\n",
			arg->tcs.entry_count - start_entry_count, com->name);
	hs_filter(&com->connected, com, f_remove);
	print(solution_out, arg->data, &arg->tcs, arg->tcs.entry_count);
	return 0;
}

static int v_h(void *param, void *element) {
	uint64_t *arg = param;
	struct computer *com = element;
	*arg += c_h(com);
	return 0;
}

static uint64_t h_h(const void *a) {
	struct hashset *hs = (void*) a;
	uint64_t result = 1;
	hs_for_each(hs, &result, v_h);
	return result;
}

static int v_eq(void *param, void *element) {
	struct hashset *ohs = param;
	if (!hs_get(ohs, element)) {
		return 1;
	}
	return 0;
}
static int h_eq(const void *a, const void *b) {
	struct hashset *hs0 = (void*) a, *hs1 = (void*) b;
	if (hs0->entry_count != hs1->entry_count) {
		return 0;
	}
	if (hs_for_each(hs0, hs1, v_eq)) {
		return 0;
	}
	return 1;
}
static void h_free(void *element) {
	hs_clear(element);
	free(element);
}
static void* h_compute(void *param, void *old_element, void *new_element) {
	if (old_element) {
		hs_clear(new_element);
		return old_element;
	}
	struct hashset *result = malloc(sizeof(struct hashset));
	*result = *(struct hashset*) new_element;
	return result;
}

static int v_init_all_groups(void *param, void *element) {
	struct hashset *arg = param;
	struct computer *com = element;
	struct hashset hs = { .equal = c_eq, .hash = c_h };
	hs_set(&hs, com);
	hs_compute(arg, &hs, 0, h_compute);
	return 0;
}

static int v_hs_set(void *param, void *element) {
	struct hashset *arg = param;
	hs_set(arg, element);
	return 0;
}

static int v_check_contains_all(void *param, void *element) {
	struct hashset *arg = param;
	struct computer *com = element;
	if (!hs_get(arg, com)) {
		return 1;
	}
	return 0;
}
struct caag_arg {
	struct hashset *all;
	struct hashset *add;
	struct hashset *cur;
	struct computer *com;
};
static int v_check_and_add_groups(void *param, void *element) {
	struct caag_arg *arg = param;
	struct computer *com = element;
	if (hs_get(arg->cur, com)) {
		return 0;
	}
	if (hs_for_each(arg->cur, &com->connected, v_check_contains_all)) {
		return 0;
	}
	struct hashset new = { .equal = c_eq, .hash = c_h };
	hs_for_each(arg->cur, &new, v_hs_set);
	hs_add(&new, com);
	if (hs_get(arg->all, &new)) {
		hs_clear(&new);
		return 0;
	}
	hs_compute(arg->add, &new, 0, h_compute);
	return 0;
}

struct fag_arg {
	struct hashset *all;
	struct hashset *add;
	struct hashset *cur;
};
static int v_find_add_groups(void *param, void *element) {
	struct fag_arg *arg = param;
	struct computer *com = element;
	struct caag_arg caarg = { arg->all, arg->add, arg->cur, com };
	hs_for_each(&com->connected, &caarg, v_check_and_add_groups);
	return 0;
}

struct cag_arg {
	struct hashset *all;
	struct hashset *add;
};
static int v_calc_add_groups(void *param, void *element) {
	struct cag_arg *arg = param;
	struct hashset *group = element;
	struct fag_arg farg = { arg->all, arg->add, group };
	hs_for_each(group, &farg, v_find_add_groups);
	return 0;
}

static int v_find_max_group(void *param, void *element) {
	struct hashset **max = param;
	struct hashset *cur = element;
	if ((*max)->entry_count < cur->entry_count) {
		*max = cur;
	}
	return 0;
}

struct g_arg {
	struct computer **list;
	off_t off;
};
static int v_gather(void *param, void *element) {
	struct g_arg *arg = param;
	arg->list[arg->off++] = element;
	return 0;
}

static int c_com(const void *a, const void *b) {
	const struct computer *const*c0 = a, *const*c1 = b;
	return strcmp((*c0)->name, (*c1)->name);
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	if (part == 1) {
		struct cr_arg arg = { { .equal = tc_eq, .hash = tc_h, .free = free },
				data };
		print(solution_out, data, &arg.tcs, result);
		hs_for_each(&data->comps, &arg, v_calc_result);
		result = arg.tcs.entry_count;
		print(solution_out, data, &arg.tcs, result);
		hs_clear(&arg.tcs);
	} else {
		struct hashset all_groups =
				{ .equal = h_eq, .hash = h_h, .free = h_free };
		hs_for_each(&data->comps, &all_groups, v_init_all_groups);
		size_t prev_all_groups = 0;
		while (all_groups.entry_count != prev_all_groups) {
			prev_all_groups = all_groups.entry_count;
			printf("there are %zu groups\n", all_groups.entry_count);
			struct hashset add_groups = { .equal = h_eq, .hash = h_h };
			struct cag_arg arg = { &all_groups, &add_groups };
			hs_for_each(&all_groups, &arg, v_calc_add_groups);
			hs_for_each(&add_groups, &all_groups, v_hs_set);
			hs_clear(&add_groups);
		}
		struct hashset max = { .entry_count = 0 };
		struct hashset *maxp = &max;
		hs_for_each(&all_groups, &maxp, v_find_max_group);
		struct g_arg arg = { malloc(
				sizeof(struct computer*) * maxp->entry_count) };
		hs_for_each(maxp, &arg, v_gather);
		qsort(arg.list, maxp->entry_count, sizeof(struct computer*), c_com);
		fputs(STEP_BODY, solution_out);
		for (off_t i = 0; i < maxp->entry_count; ++i) {
			if (i) {
				fprintf(solution_out, ",%s", arg.list[i]->name);
			} else {
				fputs(arg.list[i]->name, solution_out);
			}
		}
		fputs("\n" STEP_FINISHED, solution_out);
	}
	hs_clear(&data->comps);
	free(data);
	return u64toa(result);
}

static void* compute_comp(void *param, void *element) {
	struct computer *result = malloc(sizeof(struct computer));
	*result = *(struct computer*) element;
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
		data->comps = (struct hashset ) { .hash = c_h, .equal = c_eq, .free =
						c_free };
	}
	char *mid = strchr(line, '-');
	if (!mid) {
		fprintf(stderr, "invalid line: %s", line);
		exit(1);
	}
	*mid = 0;
	struct computer c = { .name = strdup(line), .connected.equal = c_eq,
			.connected.hash = c_h };
	struct computer *a = hs_compute_absent(&data->comps, &c, 0, compute_comp);
	++mid;
	char *end = mid + strlen(mid);
	while (end > mid && isspace(end[-1]))
		--end;
	if (end <= mid) {
		mid[-1] = '-';
		fprintf(stderr, "invalid line: %s", line);
	}
	*end = 0;
	c.name = strdup(mid);
	struct computer *b = hs_compute_absent(&data->comps, &c, 0, compute_comp);
	if (hs_add(&a->connected, b)) {
		fprintf(stderr, "duplicated entry: '%s-%s'\n", a->name, b->name);
		exit(1);
	}
	if (hs_add(&b->connected, a)) {
		fprintf(stderr, "ERROR on: '%s-%s'\n", a->name, b->name);
		exit(2);
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

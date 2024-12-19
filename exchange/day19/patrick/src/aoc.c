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

int day = 19;
int part = 2;
FILE *solution_out;
#ifdef INTERACTIVE
int interactive = 0;
#endif

#define starts_with(str, start) !memcmp(str, start, sizeof(start) - 1)

typedef long num;
#define NUMF "l"

struct pos {
	num x;
	num y;
};

struct data {
	char **towels;
	size_t towels_size;
	char **designs;
	size_t designs_size;
	size_t max_designs_size;
};

static int do_print = 1;

static void print(FILE *str, struct data *data, uint64_t result) {
	if (result) {
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print) {
		fputs(STEP_FINISHED, str);
		return;
	}
	fputs(STEP_FINISHED, str);
}

struct result {
	char *value;
	uint64_t result;
};

static uint64_t r_h(const void *a) {
	_Static_assert(offsetof(struct result, value) == 0, "Error!");
	uint64_t result = 1;
	for (const char *c = ((const struct result*) a)->value; *c; ++c) {
		result = result * 31 + *c;
	}
	return result;
}

static int r_e(const void *a, const void *b) {
	return !strcmp(((const struct result*) a)->value,
			((const struct result*) b)->value);
}

struct hs_list {
	char *value;
	uint64_t count;
	struct hs_list *next;
};

static uint64_t t_h(const void *a) {
	_Static_assert(offsetof(struct hs_list, value) == 0, "Error!");
	return (((const struct hs_list*) a)->value[1] << 8)
			| ((const struct hs_list*) a)->value[0];
}

static int t_e(const void *a, const void *b) {
	const struct hs_list *al = (const struct hs_list*) a;
	const struct hs_list *bl = (const struct hs_list*) b;
	return al->value[0] == bl->value[0] && al->value[1] == bl->value[1];
}

static void* compute_hsl(void *param, void *old_element, void *new_element) {
	char *val = ((struct hs_list*) new_element)->value;
	for (struct hs_list *old = old_element; old; old = old->next) {
		if (strcmp(old->value, val))
			continue;
		old->count++;
		return old_element;
	}
	struct hs_list *result = malloc(sizeof(struct hs_list));
	result->value = strdup(val);
	result->count = 1;
	result->next = old_element;
	return result;
}

static int f_free_hsl(void *param, void *element) {
	struct hs_list *list = element;
	while (list) {
		free(list->value);
		struct hs_list *old = list;
		list = list->next;
		free(old);
	}
	return 0;
}

static int v_count_hsl(void *param, void *element) {
	long result = 0;
	for (struct hs_list *list = element; list; list = list->next)
		++result;
	*(long*) param += result;
	return 0;
}

static uint64_t is_possible(char *design, struct hashset *hs,
		struct hashset *hs2, size_t hs2ml, struct hashset *hs_r);

static struct result* calc_possible(char *design, struct hashset *hs,
		struct hashset *hs2, size_t hs2ml, struct hashset *hs_r) {
	size_t remain = strlen(design);
	uint64_t result = 0;
	struct hashset *uhs = remain > hs2ml ? hs2 : hs;
	for (struct hs_list *l = hs_get(uhs, &design); l; l = l->next) {
		const char *t = l->value;
		size_t tlen = strlen(t);
		if (tlen > remain || memcmp(design, t, tlen)) {
			continue;
		}
		if (tlen == remain) {
			if (part == 1) {
				result = 1;
				break;
			}
			result += l->count;
			continue;
		}
		uint64_t add = is_possible(design + tlen, hs, hs2, hs2ml, hs_r);
		if (part == 1 && add) {
			result = 1;
			break;
		}
		result += add * l->count;
	}
	if (remain > 1) {
		char c = design[1];
		design[1] = 0;
		struct hs_list *l = hs_get(uhs, &design);
		design[1] = c;
		if (l) {
			if (1 == remain) {
				if (part == 1) {
					result = 1;
				} else {
					result += l->count;
				}
			} else {
				result += is_possible(design + 1, hs, hs2, hs2ml, hs_r)
						* l->count;
			}
		}
	}
	if (do_print && remain > 24) {
		fprintf(solution_out, "found %15"I64"u possibilities for %60s\n", result,
				design);
	}
	struct result *res = malloc(sizeof(struct result));
	res->value = design;
	res->result = result;
	return res;
}

static uint64_t is_possible(char *design, struct hashset *hs,
		struct hashset *hs2, size_t hs2ml, struct hashset *hs_r) {
	struct result *r = hs_get(hs_r, &design);
	if (!r) {
		r = calc_possible(design, hs, hs2, hs2ml, hs_r);
		hs_set(hs_r, r);
	}
	return r->result;
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	struct hashset hs = { .hash = t_h, .equal = t_e };
	struct hashset hs2 = { .hash = t_h, .equal = t_e };
	for (int i = 0; i < data->towels_size; ++i) {
		hs_compute(&hs, &data->towels[i], 0, compute_hsl);
	}
	size_t hs2_ml = 0;
	for (size_t i0 = 0; i0 < data->towels_size; ++i0) {
		const char *t0 = data->towels[i0];
		size_t len0 = strlen(t0);
		long cnt = 0;
		hs_for_each(&hs2, &cnt, v_count_hsl);
		printf("%zu remain [%zu : %zu/%zu : %zu]\n",
				(data->towels_size - i0) * data->towels_size * data->towels_size
//								* data->towels_size
						, data->towels_size - i0, hs2.entry_count,
				hs2.data_size, cnt);
		for (int i1 = 0; i1 < data->towels_size; ++i1) {
			const char *t1 = data->towels[i1];
			size_t len1 = strlen(t1);
//			for (int i2 = 0; i2 < data->towels_size; ++i2) {
//				const char *t2 = data->towels[i2];
//				size_t len2 = strlen(t2);
			if (len0 + len1
//						+ len2
					> hs2_ml) {
				hs2_ml = len0 + len1
//							+ len2
						;
			}
			char t[len0 + len1
//					   	   + len2
					+ 1];
			memcpy(t, t0, len0);
			memcpy(t + len0, t1, len1);
//				memcpy(t + len0 + len1, t1, len2);
			t[len0 + len1
//					  + len2
			] = 0;
			char *b = t;
			hs_compute(&hs2, &b, 0, compute_hsl);
//			}
		}
	}
	uint64_t cnt = 0;
	hs_for_each(&hs2, &cnt, v_count_hsl);
	printf("calculated set [%zu : %"I64"u]\n", hs2.entry_count, cnt);
	struct hashset hs_r = { .hash = r_h, .equal = r_e, .free = free };
	for (int i = 0; i < data->designs_size; ++i) {
		fprintf(solution_out, "%scheck design %s\n%s",
		/*		*/STEP_HEADER, data->designs[i], STEP_BODY);
		uint64_t add = is_possible(data->designs[i], &hs, &hs2, hs2_ml, &hs_r);
		if (add) {
			result += add;
		}
		fprintf(solution_out, "the design has %"I64"u possibilities\n", add);
		fprintf(solution_out, "new result: %"I64"u\n%s", result,
		/*		*/STEP_FINISHED);
	}
	print(solution_out, data, result);
	hs_filter(&hs, 0, f_free_hsl);
	hs_clear(&hs);
	hs_clear(&hs2);
	for (num i = 0; i < data->designs_size; ++i) {
		free(data->designs[i]);
	}
	free(data->designs);
	for (num i = 0; i < data->towels_size; ++i) {
		free(data->towels[i]);
	}
	free(data->towels);
	free(data);
	return u64toa(result);
}

static struct data* parse_line(struct data *data, char *line) {
	while (*line && isspace(*line))
		++line;
	if (!*line) {
		return data;
	}
	if (!data) {
		data = malloc(sizeof(struct data));
		size_t max_towels_size = 16;
		data->towels = malloc(sizeof(char*) * 16);
		data->towels_size = 0;
		data->max_designs_size = 16;
		data->designs_size = 0;
		data->designs = malloc(sizeof(char*) * 16);
		char *end = strchr(line, ',');
		if (!end) {
			fprintf(stderr, "the first line must contain the towels! %s", line);
			exit(1);
		}
		while (109) {
			if (++data->towels_size > max_towels_size) {
				data->towels = reallocarray(data->towels, max_towels_size <<= 1,
						sizeof(char*));
			}
			int e = *end;
			if (!e) {
				while (isspace(end[-1]))
					--end;
			}
			*end = '\0';
			data->towels[data->towels_size - 1] = strdup(line);
			if (!e) {
				return data;
			}
			if (end[1] != ' ') {
				fprintf(stderr, "invalid line %s", line);
				exit(1);
			}
			line = end + 2;
			end = strchrnul(line, ',');
		}
	}
	if (++data->designs_size > data->max_designs_size) {
		data->designs = reallocarray(data->designs, data->max_designs_size <<=
				1, sizeof(char*));
	}
	char *end = line + strlen(line);
	while (isspace(end[-1]))
		--end;
	*end = '\0';
	data->designs[data->designs_size - 1] = strdup(line);
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

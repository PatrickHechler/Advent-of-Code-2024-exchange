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

#define DAY 11
int part = 2;

typedef long num;
#define NUMF "%ld"

typedef num stone;

struct data {
	stone *stones;
	size_t stones_count;
};

static void print(FILE *str, struct data *data, uint64_t result) {
	fprintf(str, "result=%s\n", u64toa(result));
	for (int l = 0; l < data->stones_count; ++l) {
		if (l) {
			fprintf(str, " " NUMF, data->stones[l]);
		} else {
			fprintf(str, NUMF, data->stones[l]);
		}
	}
	fputc('\n', str);
}

static void blink(struct data *data) {
	stone *new_stones = calloc(data->stones_count, sizeof(stone) * 2);
	if (!new_stones) {
		perror("calloc");
		abort();
	}
	size_t new_stones_count = 0;
	for (size_t off = 0; off < data->stones_count; ++off) {
		stone s = data->stones[off];
		if (s == 0) {
			new_stones[new_stones_count++] = 1;
			continue;
		}
		size_t mul = 1;
		int even = 1;
		for (stone s2 = s; s2; s2 /= 10, even ^= 1) {
			if (even) {
				mul *= 10;
			}
		}
		if (even) {
			new_stones[new_stones_count++] = s / mul;
			new_stones[new_stones_count++] = s % mul;
		} else {
			new_stones[new_stones_count++] = s * 2024;
		}
	}
	free(data->stones);
	data->stones = new_stones;
	data->stones_count = new_stones_count;
}

struct result {
	uint64_t result;
	stone stone;
	int remain_blinks;
};

static uint64_t res_h(const void *a) {
	const struct result *ra = (const struct result*) a;
	return ra->stone * 31 + ra->remain_blinks;
}

static int res_eq(const void *a, const void *b) {
	const struct result *ra = (const struct result*) a, *rb =
			(const struct result*) b;
	return ra->stone == rb->stone && ra->remain_blinks == rb->remain_blinks;
}

static uint64_t blinkN(struct hashset *hs, stone s, int count) {
	struct result arg = { .stone = s, .remain_blinks = count };
	struct result *res = hs_get(hs, &arg);
	if (res) {
		return res->result;
	}
	uint64_t result = 1;
	while (count-- > 0) {
		if (s == 0) {
			s = 1;
			continue;
		}
		size_t mul = 1;
		int even = 1;
		for (stone s2 = s; s2; s2 /= 10, even ^= 1) {
			if (even) {
				mul *= 10;
			}
		}
		if (even) {
			result += blinkN(hs, s / mul, count);
			s %= mul;
		} else {
			s *= 2024;
		}
	}
	res = malloc(sizeof(struct result));
	res->result = result;
	res->stone = arg.stone;
	res->remain_blinks = arg.remain_blinks;
	if (hs_set(hs, res)) {
		fprintf(stderr,
				"the stone " NUMF ":%d has been calculated multiple times\n",
				arg.stone, arg.remain_blinks);
		abort();
	}
	return result;
}

static char* solve(char *path) {
	struct data *data = read_data(path);
	print(stdout, data, data->stones_count);
	uint64_t result = 0;
	struct hashset hs = { .equal = res_eq, .hash = res_h };
	for (size_t i = 0; i < data->stones_count; ++i) {
		printf("stone %lu of %lu (result=%s)\n", i + 1, data->stones_count,
				u64toa(result));
		result += blinkN(&hs, data->stones[i], part == 1 ? 25 : 75);
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
	if (!data) {
		data = malloc(sizeof(struct data));
		data->stones = malloc(sizeof(stone*) * 16);
		data->stones_count = 0;
	} else {
		fprintf(stderr, "multiple lines contain data!\n");
		abort();
	}
	size_t stones_max_count = 16;
	char *ptr = line;
	while (62) {
		stone s = strtol(ptr, &ptr, 10);
		if (errno) {
			perror("strtol");
			abort();
		}
		if (++data->stones_count >= stones_max_count) {
			data->stones = reallocarray(data->stones, stones_max_count <<= 1,
					sizeof(stone));
		}
		data->stones[data->stones_count - 1] = s;
		for (; *ptr && isspace(*ptr); ++ptr)
			;
		if (!*ptr) {
			return data;
		}
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

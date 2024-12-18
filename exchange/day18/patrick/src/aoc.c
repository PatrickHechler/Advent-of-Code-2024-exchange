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

int day = 18;
int part = 2;
FILE *solution_out;
#ifdef INTERACTIVE
int interactive = 0;
#endif

#define starts_with(str, start) !memcmp(str, start, sizeof(start) - 1)

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

struct data {
	struct pos *bytes;
	size_t bytes_size;
	size_t max_bytes_size;
	struct pos max_pos;
	char *memory;
};

static int do_print = 1;

static void print(FILE *str, struct data *data, uint64_t result) {
	if (result) {
		fprintf(str, STEP_HEADER "result=%s\n" STEP_BODY, u64toa(result));
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print) {
		return;
	}
	const num xcount = data->max_pos.x + 1, ycount = data->max_pos.y + 1;
	for (num y = 0; y < ycount; ++y) {
		fprintf(str, "%.*s\n", xcount, data->memory + (y * xcount));
	}
	fputs(STEP_FINISHED, str);
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	size_t count = 1024;
	if (data->bytes_size < count) {
		count = 12; // sample data
		data->max_pos.x = 6;
		data->max_pos.y = 6;
	}
	const num xcount = data->max_pos.x + 1, ycount = data->max_pos.y + 1;
	data->memory = malloc(xcount * ycount);
	memset(data->memory, '.', xcount * ycount);
	for (num i = 0; i < count; ++i) {
		struct pos *p = data->bytes + i;
		data->memory[p->x + (xcount * p->y)] = '#';
	}
	print(solution_out, data, result);
	data->memory[0] = 'O';
	while (data->memory[xcount * ycount - 1] != 'O') {
		result++;
		for (num y = 0; y < ycount; ++y) {
			for (num x = 0; x < xcount; ++x) {
				char c = data->memory[x + (xcount * y)];
				if (c != 'O')
					continue;
				if (x && data->memory[x - 1 + (xcount * y)] == '.') {
					data->memory[x - 1 + (xcount * y)] = 'o';
				}
				if (x + 1 < xcount
						&& data->memory[x + 1 + (xcount * y)] == '.') {
					data->memory[x + 1 + (xcount * y)] = 'o';
				}
				if (y && data->memory[x + (xcount * (y - 1))] == '.') {
					data->memory[x + (xcount * (y - 1))] = 'o';
				}
				if (y + 1 < ycount
						&& data->memory[x + (xcount * (y + 1))] == '.') {
					data->memory[x + (xcount * (y + 1))] = 'o';
				}
			}
		}
		print(solution_out, data, result);
		for (num i = 0; i < xcount * ycount; ++i) {
			if (data->memory[i] == 'o') {
				data->memory[i] = 'O';
			}
		}
	}
	print(solution_out, data, result);
	/* remember to free in order to avoid a memory leak in interactive mode */
	free(data->bytes);
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
		data->bytes = malloc(sizeof(struct pos) * 16);
		data->bytes_size = 0;
		data->max_bytes_size = 16;
		data->max_pos.x = 70;
		data->max_pos.y = 70;
	}
	if (++data->bytes_size >= data->max_bytes_size) {
		data->bytes = reallocarray(data->bytes, data->max_bytes_size <<= 1,
				sizeof(struct pos));
	}
	char *end;
	data->bytes[data->bytes_size - 1].x = strtol(line, &end, 10);
	if (errno || *end != ',') {
		fprintf(stderr, "invalid line: %s", line);
		exit(1);
	}
	data->bytes[data->bytes_size - 1].y = strtol(end + 1, &end, 10);
	while (*end && isspace(*end))
		++end;
	if (!*end) {
		return data;
	}
	fprintf(stderr, "invalid line: %s", line);
	exit(1);
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
				"  I needed %lu.%.6lu seconds\n", result, diff / CLOCKS_PER_SEC,
				((diff % CLOCKS_PER_SEC) * 1000000LU) / CLOCKS_PER_SEC);
	} else {
		puts("there is no result");
	}
	return EXIT_SUCCESS;
}

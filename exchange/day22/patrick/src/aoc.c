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

int day = 22;
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

struct data {
	num *codes;
	num codes_count;
	size_t max_codes_count;
};

static int do_print = 1;

static void print(FILE *str, struct data *data, uint64_t result) {
	if (result) {
		fprintf(str, "%sresult=%"I64"u\n%s", STEP_HEADER, result, STEP_BODY);
	} else {
		fputs(STEP_BODY, str);
	}
	if (!do_print && !interactive) {
		return;
	}
	for (num y = 0; y < data->codes_count; ++y) {
		fprintf(str, "%"I64"u\n", data->codes[y]);
	}
	fputs(interactive ? STEP_FINISHED : RESET, str);
}

static num next_secret(num s) {
	s ^= s * 64;
	s %= 16777216;
	s ^= s / 32;
	s %= 16777216;
	s ^= s * 2048;
	s %= 16777216;
	return s;
}

static uint64_t solvep2(struct data *data) {
	typedef int_fast8_t change_t;
	typedef int_fast8_t prize_t;
	prize_t **changes = malloc(sizeof(int_fast32_t*) * data->codes_count);
	prize_t **prizes = malloc(sizeof(prize_t*) * data->codes_count);
	for (num i = 0; i < data->codes_count; ++i) {
		num s = data->codes[i];
		changes[i] = malloc(sizeof(change_t) * 2000);
		prizes[i] = malloc(sizeof(prize_t) * 2000);
		change_t *ch = changes[i];
		prize_t *pr = prizes[i];
		num last_prize = 0;
		for (num ii = 0; ii < 2000; ++ii) {
			s = next_secret(s);
			num prize = s % 10;
			pr[ii] = prize;
			ch[ii] = prize - last_prize;
			last_prize = prize;
		}
	}
	change_t wait_changes[4] = { -9, -9, -9, -9 };
	num best_bananas = 0;
	while (83) {
		num bananas = 0;
		for (num i = 0; i < data->codes_count; ++i) {
			for (num off = 0; 108;) {
				void *hit = memmem(changes[i] + off,
						(2000 - off) * sizeof(change_t), wait_changes,
						sizeof(change_t) * 4);
				if (!hit) {
					break;
				}
				if (((uintptr_t) hit) % _Alignof(change_t)) {
					off = ((char*) hit - (char*) changes[i]) / sizeof(change_t)
							+ 1;
					continue;
				}
				change_t *p = hit;
				ptrdiff_t diff = p - changes[i];
				bananas += prizes[i][diff + 3];
				break;
			}
		}
		if (bananas > best_bananas) {
			best_bananas = bananas;
			printf("new best: %"I64"u\n", bananas);
		}
		while (84) {
			if (++wait_changes[0] > 9) {
				wait_changes[0] = -9;
				if (++wait_changes[1] > 9) {
					wait_changes[1] = -9;
					if (++wait_changes[2] > 9) {
						wait_changes[2] = -9;
						if (++wait_changes[3] > 9) {
							return best_bananas;
						}
					}
					printf("wc={-,-,%d,%d}\n", (int) wait_changes[2],
							(int) wait_changes[3]);
				}
			}
			num n = wait_changes[0] + wait_changes[1] + wait_changes[2]
					+ wait_changes[3];
			if (n <= -9 || n > 9) {
				continue;
			}
			n = wait_changes[0] + wait_changes[1] + wait_changes[2];
			if (n < -9 || n > 9) {
				continue;
			}
			n = wait_changes[1] + wait_changes[2] + wait_changes[3];
			if (n <= -9 || n > 9) {
				continue;
			}
			n = wait_changes[0] + wait_changes[1];
			if (n < -9 || n > 9) {
				continue;
			}
			n = wait_changes[1] + wait_changes[2];
			if (n < -9 || n > 9) {
				continue;
			}
			n = wait_changes[2] + wait_changes[3];
			if (n <= -9 || n > 9) {
				continue;
			}
			n = wait_changes[3];
			if (n == -9) {
				continue;
			}
			break;
		}
	}
}

const char* solve(const char *path) {
	struct data *data = read_data(path);
	uint64_t result = 0;
	print(solution_out, data, result);
	if (part == 1) {
		for (num i = 0; i < data->codes_count; ++i) {
			num s = data->codes[i];
			for (num i = 0; i < 2000; ++i) {
				s = next_secret(s);
			}
			data->codes[i] = s;
			result += s;
		}
	} else {
		result = solvep2(data);
	}
	print(solution_out, data, result);
	free(data->codes);
	free(data);
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
		data->codes = malloc(sizeof(char*) * 16);
		data->codes_count = 0;
		data->max_codes_count = 16;
	}
	if (++data->codes_count > data->max_codes_count) {
		data->codes = reallocarray(data->codes, data->max_codes_count <<= 1,
				sizeof(char*));
	}
	char *end;
	data->codes[data->codes_count - 1] = strtol(line, &end, 10);
	while (*end && isspace(*end))
		++end;
	if (*end) {
		fprintf(stderr, "invalid line: %s", line);
		exit(1);
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

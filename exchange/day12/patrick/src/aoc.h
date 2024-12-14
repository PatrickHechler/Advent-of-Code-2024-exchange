/*
 * aoc.h
 *
 *  Created on: Dec 1, 2024
 *      Author: pat
 */

#ifndef SRC_AOC_H_
#define SRC_AOC_H_

#include <stdint.h>

#ifndef __unix__
#include <stdio.h>
#include <stddef.h>

ssize_t getline(char **line_buf, size_t *line_len, FILE *file);
char* strchrnul(char *str, char c);
void* reallocarray(void*ptr, size_t nmemb, size_t size);
#endif

extern int day;
extern int part;

char* u64toa(uint64_t);
char* d64toa(int64_t);

struct data;

struct data* read_data(const char *path);

#ifdef INTERACTIVE
#include <stddef.h>
#include <stdio.h>

extern enum cache_policy keep;

struct data* copy_data(struct data*);
void free_data(struct data*);
int next_data(struct data*);
void world_sizes(struct data*, struct coordinate*min, struct coordinate*max);
size_t get(struct data*, off_t x, off_t y, size_t text_size, char*, size_t buf_len);
#endif

#endif /* SRC_AOC_H_ */

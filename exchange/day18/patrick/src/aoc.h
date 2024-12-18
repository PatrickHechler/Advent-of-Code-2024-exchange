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

#ifdef INTERACTIVE
#include <stdio.h>

extern int day;
extern int part;
extern FILE *solution_out;
extern int interactive;
#endif

char* u64toa(uint64_t);
char* d64toa(int64_t);

struct data;

const char* solve(const char*file);

#endif /* SRC_AOC_H_ */

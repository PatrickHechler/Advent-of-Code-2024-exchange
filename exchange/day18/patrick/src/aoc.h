/*
 * aoc.h
 *
 *  Created on: Dec 1, 2024
 *      Author: pat
 */

#ifndef SRC_AOC_H_
#define SRC_AOC_H_

/* only set AOC_POSIX if not set by the CFLAGS */
#ifndef AOC_POSIX
#	if defined __unix__ \
		|| (defined __FreeBSD__ || defined __NetBSD__ || defined __OpenBSD__ \
				|| defined __bsdi__ || defined __DragonFly__) \
		|| defined __CYGWIN__ \
		|| defined __MINGW32__ \
		|| (defined __APPLE__ && defined __MACH__)
#		define AOC_POSIX 1
#	else
#		define AOC_POSIX 0
#	endif
#endif // AOC_POSIX

#include <stdint.h>

#if !AOC_POSIX
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

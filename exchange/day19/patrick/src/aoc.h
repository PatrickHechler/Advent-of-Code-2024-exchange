/*
 * aoc.h
 *
 *  Created on: Dec 1, 2024
 *      Author: pat
 */

#ifndef SRC_AOC_H_
#define SRC_AOC_H_

#define AC_POSIX 1
#define AC_REARR 2
#define AC_STRCN 4
/* only set AOC_COMPAT if not set by the CFLAGS */
#ifndef AOC_COMPAT
#	if defined __FreeBSD__ || (defined __gnu_linux__ || defined __GNU__ || defined __GLIBC__)
#		define AOC_COMPAT (AC_POSIX | AC_REARR | AC_STRCN)
#	elif defined defined __OpenBSD__
#		define AOC_COMPAT (AC_POSIX | AC_REARR)
#	elif defined defined __NetBSD__
#		define AOC_COMPAT (AC_POSIX | AC_STRCN)
#	elif defined __unix__ \
		|| (defined __bsdi__ || defined __DragonFly__) \
		|| defined __CYGWIN__ \
		|| defined __MINGW32__ \
		|| (defined __APPLE__ && defined __MACH__)
#		define AOC_COMPAT (AC_POSIX)
#	else
#		define AOC_COMPAT (0)
#	endif
#endif // AOC_COMPAT

#include <stdint.h>
#include <limits.h>

#if UINT64_MAX == UCHAR_MAX
#	define I64 "hh"
#elif UINT64_MAX == USHRT_MAX
#	define I64 "h"
#elif UINT64_MAX == UINT_MAX
#	define I64 ""
#elif UINT64_MAX == ULONG_MAX
#	define I64 "l"
#elif UINT64_MAX == ULLONG_MAX
#	define I64 "ll"
#else
#	error "i don't know what the I64 type looks like! 37"
#endif

#if !(AOC_COMPAT & AC_POSIX)
#include <stdio.h>
#include <stddef.h>

ssize_t getline(char **line_buf, size_t *line_len, FILE *file);
char* strchrnul(char *str, char c);
#endif
#if !(AOC_COMPAT & AC_REARR)
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

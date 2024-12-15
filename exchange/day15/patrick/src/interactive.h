// SPDX-License-Identifier: AGPL-3.0-or-later

/*
 * interactive.h
 *
 *  Created on: 12 Dec 2024
 *      Author: pat
 */

#ifndef SRC_INTERACTIVE_H_
#define SRC_INTERACTIVE_H_

#ifndef INTERACTIVE
#include "color.h"
#else
#include "term.h"

#include <stddef.h>
#include <stdio.h>

void interact(const char *path);

size_t skip_columns(char *buf, size_t buf_len, int s);

struct coordinate {
	off_t x;
	off_t y;
};

enum cache_policy {
	/* special values */
	keep_none = -2,
	keep_last = -1,
	/* just all (1 << value) times */
	keep_all = 0,
	keep_all2 = 1,
	keep_all8 = 3,
	keep_all16 = 4,
	keep_all64 = 6,
	keep_all128 = 7,
	keep_all256 = 8,
	keep_all512 = 9,
	keep_all1024 = 10,
	keep_all2048 = 11,
	keep_all4096 = 12,
	keep_all8192 = 13,
	keep_all16384 = 14,
};

#endif // INTERACTIVE

#endif /* SRC_INTERACTIVE_H_ */

// SPDX-License-Identifier: AGPL-3.0-or-later

/*
 * interactive.c
 *
 *  Created on: 12 Dec 2024
 *      Author: pat
 */

#include "interactive.h"
#include "aoc.h"
#include "hash.h"

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <ctype.h>
#include <time.h>

#ifdef __unix__
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
static int in;
static int out;
#else // __unix__
static FILE *in;
static FILE *out;
#define read(in, buf, count) fread(in, 1, buf, count)
#define write(out, buf, count) fwrite(out, 1, buf, count); fflush(out)
#define dprintf(out, ...) fprintf(out, __VA_ARGS__); fflush(out)
#endif // __unix__
#define writestr(out, str) write(out, str, sizeof(str) - 1)
#define addstr(str) ensure_buf(sizeof(str) - 1); \
	memcpy(buf + buf_end_pos, str, sizeof(str) - 1); \
	buf_end_pos += sizeof(str) - 1

typedef struct coordinate pos;

static pos cur = { 0, 0 };
static const char *file;
static struct data *data;
static off_t world = 0;
static struct data *last = 0;
struct world {
	off_t index;
	struct data *data;
};
static int w_eq(const void *a, const void *b) {
	_Static_assert(offsetof(struct world, index) == 0, "Error!");
	return ((const struct world*) a)->index == ((const struct world*) b)->index;
}
static uint64_t w_h(const void *a) {
	return ((const struct world*) a)->index;
}
static struct hashset worlds = { .equal = w_eq, .hash = w_h };

static pos display_sizes = { 0, 0 };

static pos max_pos = { 0, 0 };
static pos min_pos = { 0, 0 };

static size_t buf_capacity;
static off_t buf_end_pos;
static char *buf;

static size_t rbuf_capacity;
static off_t rbuf_pos;
static off_t rbuf_end_pos;
static char *rbuf;

static void update_display_size();
static void write_buf();
static int ensure_buf(size_t free_space);

const size_t header_display_lines = 4;
const size_t footer_display_lines = 2;
const size_t side_columns = 2;

static void show() {
	int add = snprintf(buf + buf_end_pos, buf_capacity - buf_end_pos,
			RESET CURSOR_UP_ONE FRMT_CURSOR_FORWARD ERASE_START_OF_DISPLAY
			CURSOR_START_OF_DISPLAY
			"day %d part %d on file %s (world %ld)\n"
			"world: min: (%ld, %ld), max: (%ld, %ld)\n" //
			"shown: min: (%ld, %ld), max: (%ld, %ld)\n", display_sizes.x - 1, //
			day, part, file, world, /* line 1 */min_pos.y, min_pos.x, max_pos.y,
			max_pos.x, /* line 2 */
			cur.y, cur.x,
			cur.y + display_sizes.y - 1 - header_display_lines
					- footer_display_lines,
			cur.x + display_sizes.x - 1 - side_columns/* line 3 */);
	if (ensure_buf(add)) {
		show();
		return;
	}
	buf_end_pos += add;
	addstr("\u250C");
	for (off_t i = 1; i < display_sizes.x - 1; ++i) {
		addstr("\u2500");
	}
	addstr("\u2510");
	for (off_t l = 0;
			l < display_sizes.y - header_display_lines - footer_display_lines;
			++l) {
		addstr("\u2502");
		if (side_columns != 2) {
			abort();
		}
		retry: ;
		while (82) {
			size_t need = get(data, cur.x, cur.y + l,
					display_sizes.x - side_columns, buf + buf_end_pos,
					buf_capacity - buf_end_pos);
			if (ensure_buf(need)) {
				continue;
			}
			buf_end_pos += need;
			break;
		}
		while (109) {
			size_t need = snprintf(buf + buf_end_pos,
					buf_capacity - buf_end_pos,
					RESET FRMT_CURSOR_SET_COLUMN "\u2502",
					(int) display_sizes.x);
			if (ensure_buf(need)) {
				continue;
			}
			buf_end_pos += need;
			break;
		}
	}
	addstr("\u2514");
	for (off_t i = 1; i < display_sizes.x - 1; ++i) {
		addstr("\u2500");
	}
	addstr("\u2518");
	write_buf();
}

static void* compute_world(void *param, void *val) {
	struct world *result = malloc(sizeof(struct world));
	result->index = world;
	result->data = copy_data(data);
	return result;
}

static void next_world() {
	switch (keep) {
	case keep_none:
		last = 0;
		if (next_data(data)) {
			world++;
		}
		break;
	case keep_last:
		if (last) {
			free_data(last);
		}
		last = data;
		data = copy_data(data);
		if (next_data(data)) {
			world++;
		}
		break;
	case keep_all10000:
		if (last) {
			free_data(last);
		}
		last = data;
		data = copy_data(data);
		if (next_data(data)) {
			world++;
			if (!(world % 10000)) {
				hs_compute_absent(&worlds, &world, 0, compute_world);
			}
		}
		break;
	case keep_all1000:
		if (last) {
			free_data(last);
		}
		last = data;
		data = copy_data(data);
		if (next_data(data)) {
			world++;
			if (!(world % 1000)) {
				hs_compute_absent(&worlds, &world, 0, compute_world);
			}
		}
		break;
	case keep_all100:
		if (last) {
			free_data(last);
		}
		last = data;
		data = copy_data(data);
		if (next_data(data)) {
			world++;
			if (!(world % 100)) {
				hs_compute_absent(&worlds, &world, 0, compute_world);
			}
		}
		break;
	case keep_all10:
		if (last) {
			free_data(last);
		}
		last = data;
		data = copy_data(data);
		if (next_data(data)) {
			world++;
			if (!(world % 10)) {
				hs_compute_absent(&worlds, &world, 0, compute_world);
			}
		}
		break;
	case keep_all:
		if (last) {
			free_data(last);
		}
		last = data;
		data = copy_data(data);
		if (next_data(data)) {
			world++;
			hs_compute_absent(&worlds, &world, 0, compute_world);
		}
		break;
	}
}

void prev_world() {
	if (!world) {
		return;
	}
	world--;
	free_data(data);
	if (last) {
		data = last;
		last = 0;
		return;
	}
	off_t w;
	switch (keep) {
	case keep_none:
	case keep_last:
		read_d: w = 0;
		data = read_data(file);
		break;
	case keep_all10000: {
		w = world - (world % 10000);
		data = hs_get(&worlds, &w);
		if (!data)
			goto read_d;

		data = copy_data(data);
		break;
	}
	case keep_all1000: {
		w = world - (world % 1000);
		data = hs_get(&worlds, &w);
		if (!data)
			goto read_d;

		data = copy_data(data);
		break;
	}
	case keep_all100: {
		w = world - (world % 100);
		data = hs_get(&worlds, &w);
		if (!data)
			goto read_d;

		data = copy_data(data);
		break;
	}
	case keep_all10: {
		w = world - (world % 10);
		data = hs_get(&worlds, &w);
		if (!data)
			goto read_d;

		data = copy_data(data);
		break;
	}
	case keep_all:
		w = world;
		data = hs_get(&worlds, &w);
		if (!data)
			goto read_d;

		data = copy_data(data);
		break;
	}
	for (; w < world; ++w) {
		dprintf(out, "\033[Gworld %ld\033[K", w);
		if (!next_data(data)) {
			world = w;
			break;
		}
	}
}

static void read_command() {
	off_t pos;
	for (pos = rbuf_pos; pos < rbuf_end_pos; ++pos) {
		switch (rbuf[pos]) {
		case ESC_C:
			if (rbuf[pos + 1] == '[') {
				switch (rbuf[pos + 2]) {
				case 'A':
					pos += 2;
					goto y_dec;
				case 'D':
					pos += 2;
					goto x_dec;
				case 'B':
					pos += 2;
					goto y_inc;
				case 'C':
					pos += 2;
					goto x_inc;
				case '6':
					if (rbuf[pos + 3] == '~') {
						next_world();
						break;
					}
					goto print_unknown;
				case '5':
					if (rbuf[pos + 3] == '~') {
						prev_world();
						break;
					}
					goto print_unknown;
				default:
					break;
				}
			}
			goto print_unknown;
		case 'C' - '@':
			addstr(ERASE_COMPLETE_DISPLAY)
			;
			write_buf();
			break;
		case 'D' - '@':
			exit(EXIT_SUCCESS);
		case 'w':
			y_dec: if (min_pos.y < cur.y)
				cur.y--;
			break;
		case 'a':
			x_dec: if (min_pos.x < cur.x)
				cur.x--;
			break;
		case 's':
			y_inc: if (max_pos.y - display_sizes.y + header_display_lines
					+ footer_display_lines >= cur.y)
				cur.y++;
			break;
		case 'd':
			x_inc: if (max_pos.x - display_sizes.x + side_columns >= cur.x)
				cur.x++;
			break;
		case ':':
		default:
			print_unknown: if (rbuf[pos] < ' ') {
				ensure_buf(2);
				buf[buf_end_pos] = '^';
				buf[buf_end_pos + 1] = rbuf[pos] + '@';
				buf_end_pos += 2;
			} else {
				ensure_buf(1);
				buf[buf_end_pos++] = rbuf[pos];
			}
		}
	}
}

static struct termios orig_term;
static void restore_term() {
	writestr(out, SHOW_CURSOR RESET "\ngoodbye\n");
	close(out);
	tcsetattr(in, TCSAFLUSH, &orig_term);
	close(in);
}

void interact(const char *path) {
	file = path;
	data = read_data(path);
	world_sizes(data, &min_pos, &max_pos);
	printf("read_data(%s)=%p\n", path, data);
	if (!data) {
		fprintf(stderr, "failed to read the needed\n", path, data);
		exit(EXIT_FAILURE);
	}
#ifndef __unix__
	fprintf(stderr, "non POSIX systems are not completely supported\n");
	in = stdin;
	out = stderr;
#else // __unix__
	fflush(stderr);
	in = STDIN_FILENO;
	char *tty = getenv("TERM");
	if (tty) {
		in = open(tty, O_RDONLY);
		if (in < 0) {
			fprintf(stderr, "open(%s, RDONLY) %s", tty, strerror(errno));
			exit(EXIT_FAILURE);
		}
	}
	struct termios term;
	if (tcgetattr(in, &term)) {
		perror("tcgatattr(stdin)");
		exit(EXIT_FAILURE);
	}
	orig_term = term;
	if (atexit(restore_term)) {
		perror("atexit(restore_term)");
		exit(EXIT_FAILURE);
	}
	term.c_iflag &= ~(IGNBRK | INPCK | ISTRIP);
	term.c_iflag |= IGNPAR | ICRNL;
#ifdef __linux__
	term.c_iflag |= IUTF8;
#endif // __linux__
	term.c_oflag |= ONLRET;
#if defined __unix__ && defined ONLCR
	term.c_oflag |= ONLCR;
#endif
	term.c_cflag &= ~(PARENB);
	term.c_lflag &= ~(ICANON | ISIG | ECHO);
	term.c_cc[VMIN] = 0;
	if (tcsetattr(in, TCSAFLUSH, &term)) {
		perror("tcsatattr(stdin)");
		exit(EXIT_FAILURE);
	}
	char *nam = ttyname(in);
	if (!nam) {
		perror("ttyname");
		exit(EXIT_FAILURE);
	}
	out = open(nam, O_WRONLY);
	if (out < 0) {
		fprintf(stderr, "failed to open(%s, WRONLY): %s\n", nam,
				strerror(errno));
		exit(EXIT_FAILURE);
	}
	dprintf(out, "initilized terminal\n");
#endif // __unix__
	buf_capacity = 1024;
	buf = malloc(buf_capacity);
	if (!buf) {
		perror("malloc");
		exit(EXIT_FAILURE);
	}
	rbuf_capacity = 64;
	rbuf = malloc(buf_capacity);
	if (!rbuf) {
		perror("malloc");
		exit(EXIT_FAILURE);
	}
	dprintf(out,
	HIDE_CURSOR RESET TITLE(Advent of Code 2024 day %d part %d (%s)), day, part,
			file);
	update_display_size();
	if (rbuf_pos != rbuf_end_pos) {
		read_command();
	}
	int cont = 448;
	while (cont) {
		show();
#if __unix__
//		struct timespec wait = { .tv_nsec = 10000000 /*10ms*/};
//		nanosleep(&wait, 0);
#endif
//		for (int i = 10; i; --i) {
			off_t w = world;
			next_world();
			if (w == world) {
				cont = 0;
				break;
			}
//		}
	}
	while (69) {
		show();
		time_t start = time(0);
		if (buf_end_pos) {
			abort();
		}
		while (113) {
			rbuf_pos = rbuf_end_pos = 0;
			ssize_t r = read(in, rbuf, rbuf_capacity);
			if (r < 0) {
				if (errno == EINTR || errno == EAGAIN || errno == EWOULDBLOCK) {
					errno = 0;
					goto no_data;
					continue;
				}
				perror("read");
				exit(EXIT_FAILURE);
			}
			if (r == 0) {
				no_data: ;
				if (difftime(start, time(0)) >= 1.0) {
					break;
#ifdef __unix__
				} else {
					struct timespec wait = { .tv_nsec = 5000000 /* 5 ms */};
					nanosleep(&wait, 0);
#endif
				}
				continue;
			}
			rbuf_end_pos = r;
			read_command();
			break;
		}
	}
}

size_t skip_columns(char *buf, size_t buf_len, int s) {
	if (s < sizeof(CURSOR_FORWARD(9))) {
		memset(buf, ' ', s > buf_len ? buf_len : s);
		return s;
	} else {
		return snprintf(buf, buf_len, FRMT_CURSOR_FORWARD, s);
	}
}

static void update_display_size() {
	writestr(out, CURSOR_SET(9999, 9999) CURSOR_GET);
	time_t start = time(0);
	while (113) {
		ssize_t r = read(in, rbuf, rbuf_capacity);
		if (r < 0) {
			if (errno == EINTR || errno == EAGAIN || errno == EWOULDBLOCK) {
				errno = 0;
				goto no_data;
				continue;
			}
			perror("read");
			exit(EXIT_FAILURE);
		}
		if (r == 0) {
			no_data: ;
			if (difftime(time(0), start) >= 2.0) {
				writestr(out, "\nfailed to update the terminal size!\n");
				exit(EXIT_FAILURE);
#ifdef __unix__
			} else {
				struct timespec wait = { .tv_nsec = 1000000 /* 1 ms */};
				nanosleep(&wait, 0);
#endif
			}
			continue;
		}
		for (off_t o = 0; o < r; o++) {
			off_t start_o = o;
			if (rbuf[rbuf_end_pos + o] != ESC_C) {
				continue;
			}
			if (rbuf[rbuf_end_pos + ++o] != '[') {
				continue;
			}
			size_t nl = 0, nc = 0;
			for (o++; o < r && isdigit(rbuf[rbuf_end_pos + o]); o++) {
				size_t nnl = nl * 10;
				nnl += rbuf[rbuf_end_pos + o] - '0';
				if (nnl / 10 != nl) {
					break;
				}
				nl = nnl;
			}
			if (rbuf[rbuf_end_pos + o] != ';' || !nl) {
				continue;
			}
			for (o++; o < r && isdigit(rbuf[rbuf_end_pos + o]); o++) {
				size_t nnc = nc * 10;
				nnc += rbuf[rbuf_end_pos + o] - '0';
				if (nnc / 10 != nc) {
					break;
				}
				nc = nnc;
			}
			if (rbuf[rbuf_end_pos + o] != 'R' || !nc) {
				continue;
			}
			if (display_sizes.y > nl) {
				dprintf(out, FRMT_CURSOR_UP_START ERASE_END_OF_DISPLAY,
						display_sizes.y - nl);
			} else if (display_sizes.y < nl) {
				size_t diff = nl - display_sizes.y;
				size_t cd = diff > 128 ? 128 : diff;
				char rbuf2[cd];
				memset(rbuf2, '\n', cd);
				while (diff) {
					size_t cpy = diff;
					if (cpy > cd) {
						cpy = cd;
					}
					write(out, rbuf2, cpy);
					diff -= cpy;
				}
			}
			display_sizes.y = nl;
			display_sizes.x = nc;
			if (r > ++o) {
				memmove(rbuf + rbuf_end_pos + start_o, rbuf + rbuf_end_pos + o,
						r - o);
			}
			rbuf_end_pos += start_o;
			return;
		}
		rbuf_end_pos += r;
	}
}
static int ensure_buf(size_t free_space) {
	int need_refill = 0;
	while (308) {
		if (buf_capacity - buf_end_pos >= free_space) {
			return need_refill;
		}
		if (buf_capacity >= free_space) {
			write_buf();
			return 1;
		}
		buf = reallocarray(buf, buf_capacity, 2);
		if (!buf) {
			perror("realloc");
			exit(EXIT_FAILURE);
		}
		buf_capacity *= 2;
		need_refill = 1;
	}
}
static void write_buf() {
	for (off_t buf_pos = 0; buf_pos != buf_end_pos;) {
		ssize_t w = write(out, buf + buf_pos, buf_end_pos - buf_pos);
		if (w < 0) {
			if (errno == EAGAIN || errno == EWOULDBLOCK || errno == EINTR) {
				errno = 0;
				continue;
			}
			perror("write");
			exit(EXIT_FAILURE);
		}
		buf_pos += w;
	}
	buf_end_pos = 0;
}

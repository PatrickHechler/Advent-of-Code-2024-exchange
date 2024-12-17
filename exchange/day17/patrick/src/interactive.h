// SPDX-License-Identifier: AGPL-3.0-or-later

/*
 * interactive.h
 *
 *  Created on: 12 Dec 2024
 *      Author: pat
 */

#ifndef SRC_INTERACTIVE_H_
#define SRC_INTERACTIVE_H_

#include "color.h"
#include "control.h"
#include <stdio.h>

struct coordinate {
	size_t x;
	size_t y;
};

#ifndef INTERACTIVE
#define STEP_HEADER       ""
#define STEP_BODY         ""
#define STEP_FOOTER       ""
#define STEP_FIN_TEXT     ""
#define STEP_FINISHED     ""
#define STEP_ALL_FINISHED ""
#else

/* start of the current steps (additional/optional) header */
#define STEP_HEADER       SOH
/* start of the current steps (non-optional) body */
#define STEP_BODY         STX
/* start of the current steps (additional/optional) footer */
#define STEP_FOOTER       ETX
/* finish transferring text, now comes some optional data or STEP_FINISHED
 * 1: struct coordinate world_min_coordinate (defaults to (0|0))
 * if  */
#define STEP_FIN_TEXT     ETX
#define STEP_FINISHED     FF
#define STEP_ALL_FINISHED EM

void interact(char *path);

#endif // INTERACTIVE

#endif /* SRC_INTERACTIVE_H_ */

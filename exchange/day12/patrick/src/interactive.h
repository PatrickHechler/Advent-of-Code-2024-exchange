// SPDX-License-Identifier: AGPL-3.0-or-later

/*
 * interactive.h
 *
 *  Created on: 12 Dec 2024
 *      Author: pat
 */

#ifndef SRC_INTERACTIVE_H_
#define SRC_INTERACTIVE_H_

/* basic stuff like coloring */

#define ESC_C '\033'
#define ESC "\033"

#define CSI ESC "["
#define C_SEP ";"
#define C_END "m"

#define C_RESET             "0"
#define C_BOLD              "1"
#define C_LESS_INTENSE      "2"
#define C_ITALIC            "3"
#define C_UNDERLINE         "4"
#define C_REVERSE_FB        "7"
#define C_STRIKE_THROUGH    "9"
#define C_DEF_INTENSE       "22"
#define C_NO_ITALIC         "23"
#define C_NO_UNDERLINE      "24"
#define C_NO_REVERSE_FB     "27"
#define C_NO_STRIKE_THROUGH "29"
#define C_FC_DEF            "39"
#define C_BC_DEF            "49"

#define C_FC_PREFIX "3"
#define C_BC_PREFIX "4"

#define C_BLACK   "0"
#define C_RED     "1"
#define C_GREEN   "2"
#define C_YELLOW  "3"
#define C_BLUE    "4"
#define C_MAGENTA "5"
#define C_CYAN    "6"
#define C_WHITE   "7"

#define C_RGB "8" C_SEP "2" C_SEP

#define C_RGB_GRAY       C_RGB "128" C_SEP "128" C_SEP "128"
#define C_RGB_LIGHT_GRAY C_RGB "192" C_SEP "192" C_SEP "192"
#define C_RGB_DARK_GRAY  C_RGB  "64" C_SEP  "64" C_SEP  "64"

#define RESET             CSI C_RESET C_END
#define BOLD              CSI C_BOLD C_END
#define LESS_INTENSE      CSI C_LESS_INTENSE C_END
#define ITALIC            CSI C_ITALIC C_END
#define UNDERLINE         CSI C_UNDERLINE C_END
#define REVERSE_FB        CSI C_REVERSE_FB C_END
#define STRIKE_THROUGH    CSI C_STRIKE_THROUGH C_END
#define DEF_INTENSE       CSI C_DEF_INTENSE C_END
#define NO_ITALIC         CSI C_NO_ITALIC C_END
#define NO_UNDERLINE      CSI C_NO_UNDERLINE C_END
#define NO_REVERSE_FB     CSI C_NO_REVERSE_FB C_END
#define NO_STRIKE_THROUGH CSI C_NO_STRIKE_THROUGH C_END
#define FC_DEF            CSI C_FC_DEF C_END
#define BC_DEF            CSI C_BC_DEF C_END
#define FC_BLACK          CSI C_FC_PREFIX C_BLACK C_END
#define FC_RED            CSI C_FC_PREFIX C_RED C_END
#define FC_GREEN          CSI C_FC_PREFIX C_GREEN C_END
#define FC_YELLOW         CSI C_FC_PREFIX C_YELLOW C_END
#define FC_BLUE           CSI C_FC_PREFIX C_BLUE C_END
#define FC_MAGENTA        CSI C_FC_PREFIX C_MAGENTA C_END
#define FC_CYAN           CSI C_FC_PREFIX C_CYAN C_END
#define FC_WHITE          CSI C_FC_PREFIX C_WHITE C_END
#define FC_GRAY           CSI C_FC_PREFIX C_RGB_GRAY C_END
#define FC_LIGHT_GRAY     CSI C_FC_PREFIX C_RGB_LIGHT_GRAY C_END
#define FC_DARK_GRAY      CSI C_FC_PREFIX C_RGB_DARK_GRAY C_END
#define BC_BLACK          CSI C_BC_PREFIX C_BLACK C_END
#define BC_RED            CSI C_BC_PREFIX C_RED C_END
#define BC_GREEN          CSI C_BC_PREFIX C_GREEN C_END
#define BC_YELLOW         CSI C_BC_PREFIX C_YELLOW C_END
#define BC_BLUE           CSI C_BC_PREFIX C_BLUE C_END
#define BC_MAGENTA        CSI C_BC_PREFIX C_MAGENTA C_END
#define BC_CYAN           CSI C_BC_PREFIX C_CYAN C_END
#define BC_WHITE          CSI C_BC_PREFIX C_WHITE C_END
#define BC_GRAY           CSI C_BC_PREFIX C_RGB_GRAY C_END
#define BC_LIGHT_GRAY     CSI C_BC_PREFIX C_RGB_LIGHT_GRAY C_END
#define BC_DARK_GRAY      CSI C_BC_PREFIX C_RGB_DARK_GRAY C_END

#define BC_RGB(r,g,b) CSI C_BC_PREFIX C_RGB #r C_SEP #g C_SEP #b C_END
#define FC_RGB(r,g,b) CSI C_FC_PREFIX C_RGB #r C_SEP #g C_SEP #b C_END

#define FRMT_BC_RGB BC_RGB(%u, %u, %u)
#define FRMT_FC_RGB FC_RGB(%u, %u, %u)

/* more advanced stuff */
#ifdef INTERACTIVE

#define BELL "\b"

#define C_CURSOR_UP        "A"
#define C_CURSOR_DOWN      "B"
#define C_CURSOR_FORWARD   "C"
#define C_CURSOR_BACK      "D"
#define C_CURSOR_NEXT_LINE "E"
#define C_CURSOR_PREV_LINE "F"
#define C_CURSOR_SET_COLUM "G"
#define C_CURSOR_SET       "H"

#define C_ERASE_IN_DISPLAY "J"
#define C_ERASE_IN_LINE    "K"

#define CURSOR_UP_ONE           CSI C_CURSOR_UP
#define CURSOR_DOWN_ONE         CSI C_CURSOR_DOWN
#define CURSOR_FORWARD_ONE      CSI C_CURSOR_FORWARD
#define CURSOR_BACK_ONE         CSI C_CURSOR_BACK
#define CURSOR_NEXT_LINE        CSI C_CURSOR_NEXT_LINE
#define CURSOR_PREV_LINE        CSI C_CURSOR_PREV_LINE
#define CURSOR_START_OF_LINE    CSI C_CURSOR_SET_COLUM
#define CURSOR_START_OF_DISPLAY CSI C_CURSOR_SET

#define ERASE_END_OF_DISPLAY   CSI C_ERASE_IN_DISPLAY
#define ERASE_START_OF_DISPLAY CSI "1" C_ERASE_IN_DISPLAY
#define ERASE_COMPLETE_DISPLAY CSI "2" C_ERASE_IN_DISPLAY

#define ERASE_END_OF_LINE   CSI C_ERASE_IN_LINE
#define ERASE_START_OF_LINE CSI "1" C_ERASE_IN_LINE
#define ERASE_COMPLETE_LINE CSI "2" C_ERASE_IN_LINE

#define CURSOR_GET CSI "6n"

#define SHOW_CURSOR CSI "?25h"
#define HIDE_CURSOR CSI "?25l"

#define C_SCROLL_PAGE_UP "S"
#define C_SCROLL_PAGE_DOWN "S"

#define SCROLL_PAGE_UP CSI "S"
#define SCROLL_PAGE_DOWN CSI "S"

#define C_END_OF_STRING ESC "\\"

#define TITLE_START ESC "]0"
#define TITLE_END   C_END_OF_STRING

/* moves n steps in the given direction */
#define CURSOR_FORWARD(count) CSI #count C_CURSOR_FORWARD
#define CURSOR_BACK(count)    CSI #count C_CURSOR_BACK
#define CURSOR_UP(count)      CSI #count C_CURSOR_UP
#define CURSOR_DOWN(count)    CSI #count C_CURSOR_DOWN

#define FRMT_CURSOR_FORWARD CURSOR_FORWARD(%u)
#define FRMT_CURSOR_BACK    CURSOR_BACK(%u)
#define FRMT_CURSOR_UP      CURSOR_UP(%u)
#define FRMT_CURSOR_DOWN    CURSOR_DOWN(%u)

/* line cursor up/down, but also set the column to 1 */
#define CURSOR_UP_START(count)    CSI #count C_CURSOR_PREV_LINE
#define CURSOR_DOWN_START(count)  CSI #count C_CURSOR_NEXT_LINE

#define FRMT_CURSOR_DOWN_START  CURSOR_DOWN_START(%u)
#define FRMT_CURSOR_UP_START    CURSOR_UP_START(%u)

/* set the cursors position */
#define CURSOR_SET(lin, col)   CSI #lin C_SEP #col C_CURSOR_SET
#define FRMT_CURSOR_SET        CURSOR_SET(%u,%u)

/* set the cursor to the start of the given line */
#define CURSOR_SET_LINE(lin)   CSI #lin C_CURSOR_SET
#define FRMT_CURSOR_SET_LINE   CURSOR_SET_LINE(%u)

/* set the cursor to the given column */
#define CURSOR_SET_COLUMN(count) CSI #count C_CURSOR_SET_COLUM
#define FRMT_CURSOR_SET_COLUMN CURSOR_SET_COLUMN(%u)

/* set the window title */
#define SCROLL_PAGES_UP(count) CSI #count C_SCROLL_PAGE_UP
#define SCROLL_PAGES_DOWN(count) CSI #count C_SCROLL_PAGE_DOWN

#define FRMT_SCROLL_PAGE_UP SCROLL_PAGE_UP(%u)
#define FRMT_SCROLL_PAGE_DOWN SCROLL_PAGE_DOWN(%u)

/* set the window title */
#define TITLE(title) TITLE_START #title TITLE_END
#define FRMT_TITLE TITLE(%s)

void interact(const char *path);

#endif // INTERACTIVE

#endif /* SRC_INTERACTIVE_H_ */

/*
 * @(#)util.h	1.26 03/12/19
 * 
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
#ifndef UTIL_H
#define UTIL_H

/*
 *  General defines
 *
 *  We now get JAVAWS_NAME from generated header file.  Include it.
 */
#include "jawsversion.h"

/*
 *  Util. methods
 */
int   useEncodingDecl (char *buffer, char *encoding, int size, char *enc);
int   ReadFileToBuffer(char* file, char **buffer);
int   SaveBufferToFile(char* file, char* s, int size);
int   isUTF8 (char *buffer, int size);

#define isWhitespace(c) \
               (c == ' ' || c == '\t' || c == '\n' || c == '\r')

/* 
 * Debug support 
 */
#define assert(s, msg)      \
    if (!(s)) { Abort(msg); }

/*
 *  Error message API
 */
void  Abort                       (char *s);
void  Message                     (char *s);

#endif /* UTIL_H */

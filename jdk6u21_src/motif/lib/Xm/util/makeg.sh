#! /bin/sh
# makeg - run "make" with options necessary to make a debuggable executable
# $XConsortium: makeg,v 1.3 95/10/16 19:17:42 gildea Exp $

# set GDB=1 in your environment if using gdb on Solaris 2.

make="${MAKE-make}"
flags="CDEBUGFLAGS=-g"

# gdb on Solaris needs the stabs included in the executable
test "${GDB+yes}" = yes && flags="$flags -xs"

exec "$make" "$flags" LDSTRIPFLAGS= ${1+"$@"}

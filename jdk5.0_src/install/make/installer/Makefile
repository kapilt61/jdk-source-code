#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#
#
# @(#)Makefile	1.6 03/12/19
#
#
# Makefile for building and packaging all of the JDK and the JRE. See
# also included files.
#
#
# Most frequently used targets:
#
#    all            -- builds all the directories here.
#    clobber        -- clobber the files generated by this Makefile 

INSTALL_BUILDDIR=..
include $(INSTALL_BUILDDIR)/common/Defs.gmk

all:: 
	@$(ECHO) $(ARCH) "Installer Build started:  " $(FULL_VERSION)

SUBDIRS  = binaries bundles

all optimized debug::
	@for i in $(SUBDIRS) ; do \
	    $(ECHO) ">>>Recursively making "$$i" "$@" @ `$(DATE)` ..."; \
	    $(CD) $$i; $(MAKE) \
            || exit 1; $(CD) ..; \
	    $(ECHO) "<<<Finished Recursively making "$$i" "$@" @ `$(DATE)`." ; \
	done

clean: clobber


clobber::
	@for i in $(SUBDIRS) ; do \
	    $(ECHO) ">>>Recursively making "$$i" "clean" @ `$(DATE)` ..."; \
	    $(CD) $$i; $(MAKE) clean RELEASE="$(RELEASE)" FULL_VERSION="$(FULL_VERSION)" \
            || exit 1; $(CD) ..; \
	    $(ECHO) "<<<Finished Recursively making "$$i" all @ `$(DATE)`." ; \
	done


# this should be the last rule in this file:
all::
	@$(ECHO) $(ARCH) "Installer Build finished:  " $(FULL_VERSION)
#
# Phonies to avoid accidents.
#
.PHONY: all clobber

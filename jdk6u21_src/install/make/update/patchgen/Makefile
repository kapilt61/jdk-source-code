#
# Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
# ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#
#
# @(#)Makefile	1.8 05/11/17
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

INSTALL_BUILDDIR=../..
include $(INSTALL_BUILDDIR)/common/Defs.gmk

all:: 
	@$(ECHO) $(ARCH) "Patching Generation started:  " $(FULL_VERSION)

SUBDIRS  = 

all build::
	@for i in `$(CD) $(ALT_BASE_IMAGE_DIR) ; $(LS) -d jre*` ; do \
	    $(ECHO) ">>>Recursively making "$$i" Patchgen for JRE "$@" @ `$(DATE)` ..."; \
	    $(CD) jre; SMALLER_PATCH=`$(ECHO) $$i` $(MAKE) \
            || exit 1; $(CD) ..; \
	    $(ECHO) "<<<Finished Recursively making Patchgen for JRE "$$i" "$@" @ `$(DATE)`." ; \
	done
	$(ECHO) ">>>Recursively making "$$i" "$@" @ `$(DATE)` ..."; \

clean: clobber

clobber::
	@for i in jre ; do \
	    $(ECHO) ">>>Recursively making "$$i" "clean" @ `$(DATE)` ..."; \
	    $(CD) $$i; $(MAKE) clean RELEASE="$(RELEASE)" FULL_VERSION="$(FULL_VERSION)" \
            || exit 1; $(CD) ..; \
	    $(ECHO) "<<<Finished Recursively making "$$i" all @ `$(DATE)`." ; \
	done


# this should be the last rule in this file:
all::
	@$(ECHO) $(ARCH) "Patching Generation finished:  " $(FULL_VERSION)
#
# Phonies to avoid accidents.
#
.PHONY: all clobber

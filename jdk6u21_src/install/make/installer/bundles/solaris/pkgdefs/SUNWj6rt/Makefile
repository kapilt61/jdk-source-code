#
# @(#)Makefile	1.38 10/03/23
#
# Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
# ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

include	../Makefile.com

DATAFILES += $(PACKAGE_TMP_DIR)/depend

PKGFILES =  jre COPYRIGHT

# Packages to list in the depend file (see ../Makefile.targ).
PKG_DEPENDENCIES = SUNWmfrun SUNWlibC SUNWxwrtl SUNWxwice SUNWxwplt

POSTCOMMON = ../common/postcommon

all:	$(FILES) $(PACKAGE_TMP_DIR)/action

install: all generate-prototype make-package

#Search through the j2sdk-image and get all the jre directories 
#and put them in the prototype file with 755 permissions.  Then
#find all the jre files and give them 755 permissions if they are 
#executable, 644 otherwise.  For each file in jre/bin* also get
#the same file in bin/*.  Finally, find all of the symlinks,
#get the values of each, and place them correctly in the
#prototype file with 555 permissions.
generate-prototype: $(PKG_ARCHIVE) FRC
	$(CAT) $(POSTCOMMON) postinstall > $(PACKAGE_TMP_DIR)/postinstall
	$(CAT) $(POSTCOMMON) preremove > $(PACKAGE_TMP_DIR)/preremove
	$(CAT) $(POSTCOMMON) postremove > $(PACKAGE_TMP_DIR)/postremove
	$(CP) space $(PACKAGE_TMP_DIR);
	$(ECHO) d none $(REGISTRY_DIR) 755 root bin >> $(PACKAGE_TMP_DIR)/proto.$(ARCH).tmp;
	$(ECHO) d none $(REGISTRY_DIR)$(INSTANCE_DIR) 755 root bin >> $(PACKAGE_TMP_DIR)/proto.$(ARCH).tmp;
	$(ECHO) d none $(PKGDEST) 755 root bin >> $(PACKAGE_TMP_DIR)/proto.$(ARCH).tmp;
	$(ECHO) d none $(PKGDEST)/bin 755 root bin >> $(PACKAGE_TMP_DIR)/proto.$(ARCH).tmp;
	for i in `$(CD) $(JDK_IMAGE_DIR) ; $(FIND) $(PKGFILES) -type d` ; do \
	      $(ECHO) d none $(PKGDEST)$$i 755 root bin >> $(PACKAGE_TMP_DIR)/proto.$(ARCH).tmp; \
	done;
	for i in `$(CD) $(JDK_IMAGE_DIR) ; $(FIND) $(PKGFILES) -type f` ; do \
	    p=`$(FIND) $(JDK_IMAGE_DIR)/$$i \( -perm -0100 -o -perm -0010 -o -perm -0001 \) -print `; \
	    if [ -n "$$p" ] ; then \
	       $(ECHO) f none $(PKGDEST)$$i=$(JDK_IMAGE_DIR)/$$i 755 root bin >> $(PACKAGE_TMP_DIR)/proto.$(ARCH).tmp; \
	       b=`$(BASENAME) $$p`; \
	       if [ -x $(JDK_IMAGE_DIR)/bin/$$b ] ; then \
		 $(ECHO) f none $(PKGDEST)bin/$$b=$(JDK_IMAGE_DIR)/bin/$$b 755 root bin >> $(PACKAGE_TMP_DIR)/proto.$(ARCH).tmp; \
	       fi; \
	    else \
	       $(ECHO) f none $(PKGDEST)$$i=$(JDK_IMAGE_DIR)/$$i 644 root bin >> $(PACKAGE_TMP_DIR)/proto.$(ARCH).tmp; \
	    fi; \
	done;
	for i in `$(CD) $(JDK_IMAGE_DIR) ; $(FIND) $(PKGFILES) bin -type l` ; do \
	    t=`$(LS) -l $(JDK_IMAGE_DIR)/$$i | $(SED) -e 's=.*-> =='`; \
	    $(ECHO) s none $(PKGDEST)$$i=$$t 555 root bin >> $(PACKAGE_TMP_DIR)/proto.$(ARCH).tmp; \
	done;

include	../Makefile.targ

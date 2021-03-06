/* $XConsortium: ContainerT.h /main/6 1996/02/09 15:05:04 drk $ */
/*
 * @OSF_COPYRIGHT@
 * COPYRIGHT NOTICE
 * Copyright (c) 1990, 1991, 1992, 1993 Open Software Foundation, Inc.
 * ALL RIGHTS RESERVED (MOTIF). See the file named COPYRIGHT.MOTIF for
 * the full copyright text.
 */
/*
 * HISTORY
 */

#ifndef _XmContainerT_H
#define _XmContainerT_H

#ifdef __cplusplus
extern "C" {
#endif

#include <Xm/Xm.h>

externalref XrmQuark XmQTcontainer;

/* Trait structures and typedefs, place typedefs first */

/* this one can be expanded in the future */
typedef struct _XmContainerDataRec {
    Mask valueMask ;

    Cardinal * detail_order ;
    Cardinal detail_order_count ;
    XmTabList detail_tablist ;
    Dimension first_column_width ;
    XtEnum  selection_mode ;    /* XmNORMAL_MODE,	XmADD_MODE */
    Pixel   select_color ;

} XmContainerDataRec, *XmContainerData;

#define ContAllValid             (0xFFFF)
#define ContDetailOrder	         (1L<<0)
#define ContDetailTabList        (1L<<1)
#define ContFirstColumnWidth     (1L<<2)
#define ContSelectionMode        (1L<<3)
#define ContSelectColor          (1L<<4)


typedef void (*XmContainerGetValuesProc)(Widget w, 
					XmContainerData contData);

/* Version 0: initial release. */

typedef struct _XmContainerTraitRec {
  int			   version;	/* 0 */
  XmContainerGetValuesProc getValues;
} XmContainerTraitRec, *XmContainerTrait;


#ifdef __cplusplus
}  /* Close scope of 'extern "C"' declaration which encloses file. */
#endif

#endif /* _XmContainerT_H */

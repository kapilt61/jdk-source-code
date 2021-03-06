/*
 * @(#)MarkToMarkPosnSubtables.h	1.3 05/05/11
 *
 * (C) Copyright IBM Corp. 1998-2004 - All Rights Reserved
 *
 */

#ifndef __MARKTOMARKPOSITIONINGSUBTABLES_H
#define __MARKTOMARKPOSITIONINGSUBTABLES_H

#include "LETypes.h"
#include "LEFontInstance.h"
#include "OpenTypeTables.h"
#include "GlyphPositioningTables.h"
#include "AttachmentPosnSubtables.h"
#include "GlyphIterator.h"

struct MarkToMarkPositioningSubtable : AttachmentPositioningSubtable
{
    le_int32   process(GlyphIterator *glyphIterator, const LEFontInstance *fontInstance) const;
    LEGlyphID  findMark2Glyph(GlyphIterator *glyphIterator) const;
};

struct Mark2Record
{
    Offset mark2AnchorTableOffsetArray[ANY_NUMBER];
};

struct Mark2Array
{
    le_uint16 mark2RecordCount;
    Mark2Record mark2RecordArray[ANY_NUMBER];
};

#endif


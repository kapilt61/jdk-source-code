/*
 * @(#)GDEFMarkFilter.h	1.10 05/05/11
 *
 * (C) Copyright IBM Corp. 1998-2004 - All Rights Reserved
 *
 */

#ifndef __GDEFMARKFILTER__H
#define __GDEFMARKFILTER__H

#include "LETypes.h"
#include "LEGlyphFilter.h"
#include "GlyphDefinitionTables.h"

class GDEFMarkFilter : public LEGlyphFilter
{
private:
    const GlyphClassDefinitionTable *classDefTable;

    GDEFMarkFilter(const GDEFMarkFilter &other); // forbid copying of this class
    GDEFMarkFilter &operator=(const GDEFMarkFilter &other); // forbid copying of this class

public:
    GDEFMarkFilter(const GlyphDefinitionTableHeader *gdefTable);
    virtual ~GDEFMarkFilter();

    virtual le_bool accept(LEGlyphID glyph) const;
};


#endif

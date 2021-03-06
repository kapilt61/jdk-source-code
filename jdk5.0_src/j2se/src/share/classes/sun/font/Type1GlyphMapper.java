/* @(#)Type1GlyphMapper.java	1.2 12/19/03
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.font;

/* 
 * This isn't a critical performance case, so don't do any
 * char->glyph map caching for Type1 fonts. The ones that are used
 * in composites will be cached there.
 */

public final class Type1GlyphMapper extends CharToGlyphMapper {

    Type1Font font;

    public Type1GlyphMapper(Type1Font font) {
	this.font = font;
	initMapper();
    }

    private void initMapper() {
	if (font.pScaler == 0L) {
	    font.getScaler();
	}
	missingGlyph = font.getMissingGlyphCode(font.pScaler);
    }
    
    public int getNumGlyphs() {
	return font.getNumGlyphs(font.pScaler);
    }

    public int getMissingGlyphCode() {
	return missingGlyph;
    }

    public boolean canDisplay(char ch) {
	return font.getGlyphCode(font.pScaler, ch) != missingGlyph;
    }

    public int charToGlyph(char ch) {
	return font.getGlyphCode(font.pScaler, ch);
    }

    public int charToGlyph(int ch) {
	if (ch < 0 || ch > 0xffff) {
	    return missingGlyph;
	} else {
	    return font.getGlyphCode(font.pScaler, (char)ch);
	}
    }
 
    public void charsToGlyphs(int count, char[] unicodes, int[] glyphs) {
	/* The conversion into surrogates is misleading.
	 * The Type1 glyph mapper only accepts 16 bit unsigned shorts.
	 * If its > not in the range it can use assign the missing glyph. 
	 */
	for (int i=0; i<count; i++) {
	    int code = unicodes[i]; // char is unsigned.
	    
	    if (code >= HI_SURROGATE_START && 
		code <= HI_SURROGATE_END && i < count - 1) {
		char low = unicodes[i + 1];
		
		if (low >= LO_SURROGATE_START &&
		    low <= LO_SURROGATE_END) {
		    code = (code - HI_SURROGATE_START) * 
			0x400 + low - LO_SURROGATE_START + 0x10000;
		    glyphs[i + 1] = 0xFFFF; // invisible glyph
		}
	    }
	    if (code < 0 || code > 0xffff) {
		glyphs[i] = missingGlyph;
	    } else {
		glyphs[i] = font.getGlyphCode(font.pScaler, (char)code);
	    }
	    if (code >= 0x10000) {
		i += 1; // Empty glyph slot after surrogate
	    }
	}
    }

    public void charsToGlyphs(int count, int[] unicodes, int[] glyphs) {
	/* I believe this code path is never exercised. Its there mainly
	 * for surrogates and/or the opentype engine which aren't likely
	 * to be an issue for Type1 fonts. So no need to optimise it.
	 */
	for (int i=0; i<count; i++) {
	    if (unicodes[i] < 0 || unicodes[i] > 0xffff) {
		glyphs[i] = missingGlyph;
	    } else {
		glyphs[i] =
		    font.getGlyphCode(font.pScaler, (char)unicodes[i]);
	    }
	}
    }


    /* This variant checks if shaping is needed and immediately
     * returns true if it does. A caller of this method should be expecting
     * to check the return type because it needs to know how to handle
     * the character data for display.
     */
    public boolean charsToGlyphsNS(int count, char[] unicodes, int[] glyphs) {

	for (int i=0; i<count; i++) {
	    int code = unicodes[i]; // char is unsigned.
	    
	    if (code >= HI_SURROGATE_START && 
		code <= HI_SURROGATE_END && i < count - 1) {
		char low = unicodes[i + 1];
		
		if (low >= LO_SURROGATE_START &&
		    low <= LO_SURROGATE_END) {
		    code = (code - HI_SURROGATE_START) * 
			0x400 + low - LO_SURROGATE_START + 0x10000;
		    glyphs[i + 1] = INVISIBLE_GLYPH_ID;
		}
	    }

	    if (code < 0 || code > 0xffff) {
		glyphs[i] = missingGlyph;
	    } else {
		glyphs[i] = font.getGlyphCode(font.pScaler, (char)code);
	    }

	    if (code < 0x0590) {
		continue;
	    }
	    else if (code <= 0x05ff) { // Hebrew 0x0590->0x05ff
		return true;
	    }
	    else if (code >= 0x0600 && code <= 0x06ff) { // Arabic
		return true;
	    }
	    else if (code >= 0x0900 && code <= 0x0d7f) {
		// if Indic, assume shaping for conjuncts, reordering:
		// 0900 - 097F Devanagari
		// 0980 - 09FF Bengali
		// 0A00 - 0A7F Gurmukhi
		// 0A80 - 0AFF Gujarati
		// 0B00 - 0B7F Oriya
		// 0B80 - 0BFF Tamil
		// 0C00 - 0C7F Telugu
		// 0C80 - 0CFF Kannada
		// 0D00 - 0D7F Malayalam
		return true;
	    }
	    else if (code >= 0x0e00 && code <= 0x0e7f) {
		// if Thai, assume shaping for vowel, tone marks
		return true;
	    }
	    else if (code >= 0x200c && code <= 0x200d) { //  zwj or zwnj
		return true;
	    }
	    else if (code >= 0x202a && code <= 0x202e) { // directional control
		return true;
	    }
	    else if (code >= 0x206a && code <= 0x206f) { // directional control
		return true;
	    }
	    else if (code >= 0x10000) {
		i += 1; // Empty glyph slot after surrogate
		continue;
	    }
	}

	return false;
    }
}

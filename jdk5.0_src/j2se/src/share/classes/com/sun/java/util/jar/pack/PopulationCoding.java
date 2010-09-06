/*
 * @(#)PopulationCoding.java	1.7 04/01/06
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.java.util.jar.pack;

import java.util.*;
import java.io.*;

/**
 * Population-based coding.
 * See the section "Encodings of Uncorrelated Values" in the Pack200 spec.
 * @author John Rose
 * @version 1.7, 01/06/04
 */
// This tactic alone reduces the final zipped rt.jar by about a percent.
class PopulationCoding implements Constants, CodingMethod {
    Histogram vHist;   // histogram of all values
    int[]     fValues; // list of favored values
    int       fVlen;   // inclusive max index
    long[]    symtab;  // int map of favored value -> token [1..#fValues]

    CodingMethod favoredCoding;
    CodingMethod tokenCoding;
    CodingMethod unfavoredCoding;

    int L = -1;  //preferred L value for tokenCoding

    public void setFavoredValues(int[] fValues, int fVlen) {
	// Note:  {f} is allFavoredValues[1..fvlen], not [0..fvlen-1].
	// This is because zero is an exceptional favored value index.
	assert(fValues[0] == 0);  // must be empty
	assert(this.fValues == null);  // do not do this twice
	this.fValues = fValues;
	this.fVlen   = fVlen;
	if (L >= 0) {
	    setL(L);  // reassert
	}
    }
    public void setFavoredValues(int[] fValues) {
	int fVlen = fValues.length-1;
	setFavoredValues(fValues, fVlen);
    }
    public void setHistogram(Histogram vHist) {
	this.vHist = vHist;
    }
    public void setL(int L) {
	this.L = L;
	if (L >= 0 && fValues != null && tokenCoding == null) {
	    tokenCoding = fitTokenCoding(fVlen, L);
	    assert(tokenCoding != null);
	}
    }

    public static Coding fitTokenCoding(int fVlen, int L) {
	// Find the smallest B s.t. (B,H,0) covers fVlen.
	if (fVlen < 256)
	    // H/L do not matter when B==1
	    return BandStructure.BYTE1;
	Coding longest = BandStructure.UNSIGNED5.setL(L);
	if (!longest.canRepresent(fVlen))
	    return null;  // failure; L is too sharp and fVlen too large
	Coding tc = longest;
	for (Coding shorter = longest; ; ) {
	    shorter = shorter.setB(shorter.B()-1);
	    if (shorter.umax() < fVlen)
		break;
	    tc = shorter;  // shorten it by reducing B
	}
	return tc;
    }

    public void setFavoredCoding(CodingMethod favoredCoding) {
	this.favoredCoding = favoredCoding;
    }
    public void setTokenCoding(CodingMethod tokenCoding) {
        this.tokenCoding = tokenCoding;
	this.L = -1;
	if (tokenCoding instanceof Coding && fValues != null) {
	    Coding tc = (Coding) tokenCoding;
	    if (tc == fitTokenCoding(fVlen, tc.L()))
		this.L = tc.L();;
	    // Otherwise, it's a non-default coding.
	}
    }
    public void setUnfavoredCoding(CodingMethod unfavoredCoding) {
	this.unfavoredCoding = unfavoredCoding;
    }

    public int favoredValueMaxLength() {
	if (L == 0)
	    return Integer.MAX_VALUE;
	else
	    return BandStructure.UNSIGNED5.setL(L).umax();
    }

    public void resortFavoredValues() {
	Coding tc = (Coding) tokenCoding;
	// Make a local copy before reordering.
	fValues = BandStructure.realloc(fValues, 1+fVlen);
	// Resort favoredValues within each byte-size cadre.
	int fillp = 1;  // skip initial zero
	for (int n = 1; n <= tc.B(); n++) {
	    int nmax = tc.byteMax(n);
	    if (nmax > fVlen)
		nmax = fVlen;
	    if (nmax < tc.byteMin(n))
		break;
	    int low = fillp;
	    int high = nmax+1;
	    if (high == low)  continue;
	    assert(high > low)
		: high+"!>"+low;
	    assert(tc.getLength(low) == n)
		: n+" != len("+(low)+") == "+
		  tc.getLength(low);
	    assert(tc.getLength(high-1) == n)
		: n+" != len("+(high-1)+") == "+
		  tc.getLength(high-1);
	    int midTarget = low + (high-low)/2;
	    int mid = low;
	    // Divide the values into cadres, and sort within each.
	    int prevCount = -1;
	    int prevLimit = low;
	    for (int i = low; i < high; i++) {
		int val = fValues[i];
		int count = vHist.getFrequency(val);
		if (prevCount != count) {
		    if (n == 1) {
			// For the single-byte encoding, keep strict order
			// among frequency groups.
			Arrays.sort(fValues, prevLimit, i);
		    } else if (Math.abs(mid - midTarget) >
			       Math.abs(i   - midTarget)) {
			// Find a single inflection point
			// close to the middle of the byte-size cadre.
			mid = i;
		    }
		    prevCount = count;
		    prevLimit = i;
		}
	    }
	    if (n == 1) {
		Arrays.sort(fValues, prevLimit, high);
	    } else {
		// Sort up to the midpoint, if any.
		Arrays.sort(fValues, low, mid);
		Arrays.sort(fValues, mid, high);
	    }
	    assert(tc.getLength(low) == tc.getLength(mid));
	    assert(tc.getLength(low) == tc.getLength(high-1));
	    fillp = nmax+1;
	}
	assert(fillp == fValues.length);

	// Reset symtab.
	symtab = null;
    }

    public int getToken(int value) {
	if (symtab == null)
	    symtab = makeSymtab();
	int pos = Arrays.binarySearch(symtab, (long)value << 32);
	if (pos < 0)  pos = -pos-1;
	if (pos < symtab.length && value == (int)(symtab[pos] >>> 32))
	    return (int)symtab[pos];
	else
	    return 0;
    }

    public int[][] encodeValues(int[] values, int start, int end) {
	// Compute token sequence.
	int[] tokens = new int[end-start];
	int nuv = 0;
	for (int i = 0; i < tokens.length; i++) {
	    int val = values[start+i];
	    int tok = getToken(val);
	    if (tok != 0)
		tokens[i] = tok;
	    else
		nuv += 1;
	}
	// Compute unfavored value sequence.
	int[] unfavoredValues = new int[nuv];
	nuv = 0;  // reset
	for (int i = 0; i < tokens.length; i++) {
	    if (tokens[i] != 0)  continue;  // already covered
	    int val = values[start+i];
	    unfavoredValues[nuv++] = val;
	}
	assert(nuv == unfavoredValues.length);
	return new int[][]{ tokens, unfavoredValues };
    }

    private long[] makeSymtab() {
	long[] symtab = new long[fVlen];
	for (int token = 1; token <= fVlen; token++) {
	    symtab[token-1] = ((long)fValues[token] << 32) | token;
	}
	// Index by value:
	Arrays.sort(symtab);
	return symtab;
    }

    private Coding getTailCoding(CodingMethod c) {
	while (c instanceof AdaptiveCoding)
	    c = ((AdaptiveCoding)c).tailCoding;
	return (Coding) c;
    }

    // CodingMethod methods.
    public void writeArrayTo(OutputStream out, int[] a, int start, int end) throws IOException {
	int[][] vals = encodeValues(a, start, end);
	writeSequencesTo(out, vals[0], vals[1]);
    }
    void writeSequencesTo(OutputStream out, int[] tokens, int[] uValues) throws IOException {
	favoredCoding.writeArrayTo(out, fValues, 1, 1+fVlen);
	getTailCoding(favoredCoding).writeTo(out, computeSentinelValue());
	tokenCoding.writeArrayTo(out, tokens, 0, tokens.length);
	if (uValues.length > 0)
	    unfavoredCoding.writeArrayTo(out, uValues, 0, uValues.length);
    }

   int computeSentinelValue() {
	Coding fc = getTailCoding(favoredCoding);
	if (fc.isDelta()) {
	    // repeat the last favored value, using delta=0
	    return 0;
	} else {
	    // else repeat the shorter of the min or last value
	    int min = fValues[1];
	    int last = min;
	    // (remember that fVlen is an inclusive limit in fValues)
	    for (int i = 2; i <= fVlen; i++) {
		last = fValues[i];
		min = moreCentral(min, last);
	    }
	    int endVal;
	    if (fc.getLength(min) <= fc.getLength(last))
		return min;
	    else
		return last;
	}
   }

    public void readArrayFrom(InputStream in, int[] a, int start, int end) throws IOException {
	// Parameters are fCode, L, uCode.
	setFavoredValues(readFavoredValuesFrom(in, end-start));
	// Read the tokens.  Read them into the final array, for the moment.
	tokenCoding.readArrayFrom(in, a, start, end);
	// Decode the favored tokens.
	int headp = 0, tailp = -1;
	int uVlen = 0;
	for (int i = start; i < end; i++) {
	    int tok = a[i];
	    if (tok == 0) {
		// Make a linked list, and decode in a second pass.
		if (tailp < 0) {
		    headp = i;
		} else {
		    a[tailp] = i;
		}
		tailp = i;
		uVlen += 1;
	    } else {
		a[i] = fValues[tok];
	    }
	}
	// Walk the linked list of "zero" locations, decoding unfavored vals.
	int[] uValues = new int[uVlen];
	if (uVlen > 0)
	    unfavoredCoding.readArrayFrom(in, uValues, 0, uVlen);
	for (int i = 0; i < uVlen; i++) {
	    int nextp = a[headp];
	    a[headp] = uValues[i];
	    headp = nextp;
	}
    }

    int[] readFavoredValuesFrom(InputStream in, int maxForDebug) throws IOException {
	int[] fValues = new int[1000];  // realloc as needed
	// The set uniqueValuesForDebug records all favored values.
	// As each new value is added, we assert that the value
	// was not already in the set.
	HashSet uniqueValuesForDebug = null;
	assert((uniqueValuesForDebug = new HashSet()) != null);
	int fillp = 1;
	int min = Integer.MIN_VALUE;  // farthest from the center
	int last = 0;
	CodingMethod fcm = favoredCoding;
	while (fcm instanceof AdaptiveCoding) {
	    AdaptiveCoding ac = (AdaptiveCoding) fcm;
	    int len = ac.headLength;
	    while (fillp + len > fValues.length)
		fValues = BandStructure.realloc(fValues);
	    int newFillp = fillp + len;
	    ac.headCoding.readArrayFrom(in, fValues, fillp, newFillp);
	    while (fillp < newFillp) {
		int val = fValues[fillp++];
		assert(uniqueValuesForDebug.add(new Integer(val)));
		assert(fillp <= maxForDebug);
		last = val;
		min = moreCentral(min, val);
	    }
	    fcm = ac.tailCoding;
	}
	Coding fc = (Coding) fcm;
	if (fc.isDelta()) {
	    for (int state = 0;;) {
		// Read a new value:
		state += fc.readFrom(in);
		state = fc.reduceToUnsignedRange(state);
		int val = state;
		if (fillp > 1 && (val == last || val == min))
		    break;
		if (fillp == fValues.length)
		    fValues = BandStructure.realloc(fValues);
		fValues[fillp++] = val;
		assert(uniqueValuesForDebug.add(new Integer(val)));
		assert(fillp <= maxForDebug);
		last = val;
		min = moreCentral(min, val);
	    }
	} else {
	    for (;;) {
		int val = fc.readFrom(in);
		if (fillp > 1 && (val == last || val == min))
		    break;
		if (fillp == fValues.length)
		    fValues = BandStructure.realloc(fValues);
		fValues[fillp++] = val;
		assert(uniqueValuesForDebug.add(new Integer(val)));
		assert(fillp <= maxForDebug);
		last = val;
		min = moreCentral(min, val);
	    }
	}
	return BandStructure.realloc(fValues, fillp);
    }

    private static int moreCentral(int x, int y) {
	int kx = (x >> 31) ^ (x << 1);
	int ky = (y >> 31) ^ (y << 1);
	// bias kx/ky to get an unsigned comparison:
	kx -= Integer.MIN_VALUE;
	ky -= Integer.MIN_VALUE;
	int xy = (kx < ky? x: y);
	// assert that this ALU-ish version is the same:
	assert(xy == moreCentralSlow(x, y));
	return xy;
    }
    private static int moreCentralSlow(int x, int y) {
	int ax = x;
	if (ax < 0)  ax = -ax;
	if (ax < 0)  return y;  //x is MIN_VALUE
	int ay = y;
	if (ay < 0)  ay = -ay;
	if (ay < 0)  return x;  //y is MIN_VALUE
	if (ax < ay)  return x;
	if (ax > ay)  return y;
	// At this point the absolute values agree, and the positive wins.
	return x > y ? x : y;
    }

    static final int[] LValuesCoded
	= { -1, 4, 8, 16, 32, 64, 128, 192, 224, 240, 248, 252 };

    public byte[] getMetaCoding(Coding dflt) {
	int K = fVlen;
	int LCoded = 0;
	if (tokenCoding instanceof Coding) {
	    Coding tc = (Coding) tokenCoding;
	    if (tc.B() == 1) {
		LCoded = 1;
	    } else if (L >= 0) {
		assert(L == tc.L());
		for (int i = 1; i < LValuesCoded.length; i++) {
		    if (LValuesCoded[i] == L) { LCoded = i; break; }
		}
	    }
	}
	CodingMethod tokenDflt = null;
	if (LCoded != 0 && tokenCoding == fitTokenCoding(fVlen, L)) {
	    // A simple L value is enough to recover the tokenCoding.
	    tokenDflt = tokenCoding;
	}
	int FDef = (favoredCoding == dflt)?1:0;
	int UDef = (unfavoredCoding == dflt || unfavoredCoding == null)?1:0;
	int TDef = (tokenCoding == tokenDflt)?1:0;
	int TDefL = (TDef == 1) ? LCoded : 0;
	assert(TDef == ((TDefL>0)?1:0));
	ByteArrayOutputStream bytes = new ByteArrayOutputStream(10);
	bytes.write(_meta_pop + FDef + 2*UDef + 4*TDefL);
	try {
	    if (FDef == 0)  bytes.write(favoredCoding.getMetaCoding(dflt));
	    if (TDef == 0)  bytes.write(tokenCoding.getMetaCoding(dflt));
	    if (UDef == 0)  bytes.write(unfavoredCoding.getMetaCoding(dflt));
	} catch (IOException ee) {
	    throw new RuntimeException(ee);
	}
	return bytes.toByteArray();
    }
    public static int parseMetaCoding(byte[] bytes, int pos, Coding dflt, CodingMethod res[]) {
	int op = bytes[pos++] & 0xFF;
	if (op < _meta_pop)  return pos-1; // backup
	op -= _meta_pop;
	int FDef = op % 2;
	int UDef = (op / 2) % 2;
	int TDefL = (op / 4);
	if (TDefL >= LValuesCoded.length)  return pos-1;  // backup
	int TDef = (TDefL > 0)?1:0;
	int L = LValuesCoded[TDefL];
	CodingMethod[] FCode = {dflt}, TCode = {null}, UCode = {dflt};
	if (FDef == 0)
	    pos = BandStructure.parseMetaCoding(bytes, pos, dflt, FCode);
	if (TDef == 0)
	    pos = BandStructure.parseMetaCoding(bytes, pos, dflt, TCode);
	if (UDef == 0)
	    pos = BandStructure.parseMetaCoding(bytes, pos, dflt, UCode);
	PopulationCoding pop = new PopulationCoding();
	pop.L = L;  // might be -1
	pop.favoredCoding   = FCode[0];
	pop.tokenCoding     = TCode[0];  // might be null!
	pop.unfavoredCoding = UCode[0];
	res[0] = pop;
	return pos;
    }

    private String keyString(CodingMethod m) {
	if (m instanceof Coding)
	    return ((Coding)m).keyString();
	if (m == null)
	    return "none";
	return m.toString();
    }
    public String toString() {
	return
	    "pop("+
	    "fVlen="+fVlen+
	    " fc="+keyString(favoredCoding)+
	    " tc="+keyString(tokenCoding)+
	    " uc="+keyString(unfavoredCoding)+
	    ")";
    }
}
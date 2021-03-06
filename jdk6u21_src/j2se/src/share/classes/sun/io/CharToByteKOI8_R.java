/*
 * @(#)CharToByteKOI8_R.java	1.15 10/03/23
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.io;

import sun.nio.cs.KOI8_R;

/**
 * Tables and data to convert Unicode to KOI8_R
 *
 * @author  ConverterGenerator tool
 * @version >= JDK1.1.6
 */

public class CharToByteKOI8_R extends CharToByteSingleByte {

    private final static KOI8_R nioCoder = new KOI8_R();

    public String getCharacterEncoding() {
        return "KOI8_R";
    }

    public CharToByteKOI8_R() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}

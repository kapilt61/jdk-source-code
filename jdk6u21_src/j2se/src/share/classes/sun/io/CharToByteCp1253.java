/*
 * @(#)CharToByteCp1253.java	1.17 10/03/23
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.io;

import sun.nio.cs.MS1253;

/**
 * Tables and data to convert Unicode to Cp1253
 *
 * @author  ConverterGenerator tool
 * @version >= JDK1.1.6
 */

public class CharToByteCp1253 extends CharToByteSingleByte {

    private final static MS1253 nioCoder = new MS1253();

    public String getCharacterEncoding() {
        return "Cp1253";
    }

    public CharToByteCp1253() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}

/*
 * @(#)ByteToCharEUC_JP_Solaris.java	1.6	10/03/23
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.io;

import sun.nio.cs.ext.JIS_X_0208_Solaris_Decoder;
import sun.nio.cs.ext.JIS_X_0212_Solaris_Decoder;
/**
 * @version 1.6, 10/03/23
 *
 * @author Limin Shi
 * @author Ian Little
 * 
 * EUC_JP variant converter for Solaris with vendor defined chars
 * added (4765370)
 */


public class ByteToCharEUC_JP_Solaris extends ByteToCharEUC_JP {
    private byte savedSecond = 0;

    ByteToCharJIS0201 bcJIS0201 = new ByteToCharJIS0201();
    ByteToCharJIS0212_Solaris bcJIS0212 = new ByteToCharJIS0212_Solaris();

    short[] j0208Index1 = JIS_X_0208_Solaris_Decoder.getIndex1();
    String[] j0208Index2 = JIS_X_0208_Solaris_Decoder.getIndex2();
    ByteToCharJIS0212_Solaris j0212Decoder = new ByteToCharJIS0212_Solaris();

    public ByteToCharEUC_JP_Solaris() {
        super();
        start = 0xA1;
        end = 0xFE;
        savedSecond = 0;
    }

    public int flush(char[] output, int outStart, int outEnd)
        throws MalformedInputException
    {
        if (savedSecond != 0) {
            reset();
            throw new MalformedInputException();
        }
        reset();
        return 0;
    }

    /**
     * Resets the converter.
     * Call this method to reset the converter to its initial state
     */
    public void reset() {
        super.reset();
        savedSecond = 0;
    }

    public String getCharacterEncoding() {
        return "eucJP-open";
    }

    protected char convSingleByte(int b) {
        if (b < 0 || b > 0x7F)
            return REPLACE_CHAR;
        return bcJIS0201.getUnicode(b);
    }

    protected char getUnicode(int byte1, int byte2) {
        if (byte1 == 0x8E) {
            return bcJIS0201.getUnicode(byte2 - 256);
        }
        // Fix for bug 4121358 - similar fix for bug 4117820 put
        // into ByteToCharDoubleByte.getUnicode()
        if (((byte1 < 0) || (byte1 > j0208Index1.length))
            || ((byte2 < start) || (byte2 > end)))
            return REPLACE_CHAR;

	char result = super.getUnicode(byte1, byte2);
	if (result != '\uFFFD') {
	    return result;
	} else {
	    int n = (j0208Index1[byte1 - 0x80] & 0xf) * (end - start + 1)
                + (byte2 - start);
        return j0208Index2[j0208Index1[byte1 - 0x80] >> 4].charAt(n);
	}
    }

    protected char decode0212(int byte1, int byte2) {
	return j0212Decoder.getUnicode(byte1, byte2);
    }

    /**
     * Converts sequences of bytes to characters.
     * Conversions that result in Exceptions can be restarted by calling
     * convert again, with appropriately modified parameters.
     * @return the characters written to output.
     * @param input byte array containing text in Double/single Byte
     * @param inStart offset in input array
     * @param inEnd offset of last byte to be converted
     * @param output character array to receive conversion result
     * @param outStart starting offset
     * @param outEnd offset of last byte to be written to
     * @throw UnsupportedCharacterException for any bytes
     * that cannot be converted to the external character set.
     */
    public int convert(byte[] input, int inOff, int inEnd,
                       char[] output, int outOff, int outEnd)
        throws UnknownCharacterException,
               ConversionBufferFullException
    {
        char    outputChar = REPLACE_CHAR;
        int     inputSize = 0;          // Size of input

        // Record beginning offsets
        charOff = outOff;
        byteOff = inOff;

        // Loop until we hit the end of the input
        while (byteOff < inEnd) {
            int byte1, byte2;

            if (savedByte == 0) {
                byte1 = input[byteOff];
                inputSize = 1;
            } else {
                byte1 = savedByte;
                savedByte = 0;
                inputSize = 0;
            }

            outputChar = convSingleByte(byte1);

            if (outputChar == REPLACE_CHAR) {   // Multibyte char
                if ((byte1 & 0xff) == 0x8F) {   // JIS0212
                    if (byteOff + inputSize + 1 >= inEnd) {
                        // split in the middle of a character
                        // save the first 2 bytes for next time around
                        savedByte = (byte) byte1;
                        byteOff += inputSize;
                        if (byteOff < inEnd) {
                            savedSecond = input[byteOff];
                            byteOff++;
                        }
                        break;
                    }
                    if (savedSecond != 0) {
                        byte1 = savedSecond & 0xff;
                        savedSecond = 0;
                    } else {
                        byte1 = input[byteOff + inputSize] & 0xff;
                        inputSize++;
                    }
                    byte2 = input[byteOff + inputSize] & 0xff;
                    inputSize++;
                    outputChar = bcJIS0212.getUnicode(byte1-0x80, byte2-0x80);
                } else { // JIS0208
                    if (byteOff + inputSize >= inEnd) {
                        // split in the middle of a character
                        // save the first byte for next time around
                        savedByte = (byte) byte1;
                        byteOff += inputSize;
                        break;
                    }
                    byte1 &= 0xff;
                    byte2 = input[byteOff + inputSize] & 0xff;
                    inputSize++;
                    outputChar = getUnicode(byte1, byte2);
                }
            }

            if (outputChar == REPLACE_CHAR) {
                if (subMode)
                    outputChar = subChars[0];
                else {
                    badInputLength = inputSize;
                    throw new UnknownCharacterException();
                }
            }

            if (charOff >= outEnd)
                throw new ConversionBufferFullException();

            output[charOff++] = outputChar;
            byteOff += inputSize;
        }

        return charOff - outOff;
    }

}


/*
 * @(#)ByteToCharEUC_JP_LINUX.java	1.6 10/03/23
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package	sun.io;

/**
 * Class for converting bytes to characters for the EUC-JP encoding in
 * linux. This converter supports the JIS0201 and the JIS0208 encoding and
 * omits support for the JIS212 encoding.
 * 
 * @author Naveen Sanjeeva
 */

public class ByteToCharEUC_JP_LINUX extends  ByteToCharJIS0208 {
    private byte savedSecond = 0;
    ByteToCharJIS0201 bcJIS0201 = new ByteToCharJIS0201();

    public ByteToCharEUC_JP_LINUX() {
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
        return "EUC_JP_LINUX";
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
	if (((byte1 < 0) || (byte1 > index1.length))
	    || ((byte2 < start) || (byte2 > end)))
	    return REPLACE_CHAR;

	int n = (index1[byte1 - 0x80] & 0xf) * (end - start + 1)
		+ (byte2 - start);
	return index2[index1[byte1 - 0x80] >> 4].charAt(n);
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
	char	outputChar = REPLACE_CHAR;
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

	    if (outputChar == REPLACE_CHAR) {	// Multibyte char
		if ((byte1 & 0xff) != 0x8F) {   // JIS0208
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
		} else if ((byte1 & 0xff) == 0x8F) {   // JIS0212
		    // Handling of 3-byte sequences for this converter involves
		    // just ignoring the relevant bytes and returning a
		    // suitable substitute char if one exists.
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

		    // Skip over the saved bytes if any 
		    if (savedSecond != 0) {
			savedSecond = 0;
		    } else {
		    	inputSize++;
		    }
		    inputSize++;
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

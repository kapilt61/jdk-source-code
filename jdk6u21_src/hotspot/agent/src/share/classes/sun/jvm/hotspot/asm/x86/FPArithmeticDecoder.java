/*
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.jvm.hotspot.asm.x86;

import sun.jvm.hotspot.asm.*;

public class FPArithmeticDecoder extends FPInstructionDecoder {
   private int rtlOperation;

   public FPArithmeticDecoder(String name, int addrMode1, int operandType1, int rtlOperation) {
      super(name, addrMode1, operandType1);
      this.rtlOperation = rtlOperation;
   }
   public FPArithmeticDecoder(String name, int addrMode1, int operandType1, int addrMode2, int operandType2, int rtlOperation) {
      super(name, addrMode1, operandType1, addrMode2, operandType2);
      this.rtlOperation = rtlOperation;
   }

   protected Instruction decodeInstruction(byte[] bytesArray, boolean operandSize, boolean addrSize, X86InstructionFactory factory) {
      Operand op1 = getOperand1(bytesArray, operandSize, addrSize);
      Operand op2 = getOperand2(bytesArray, operandSize, addrSize);
      int size = byteIndex - instrStartIndex;
      return factory.newFPArithmeticInstruction(name, rtlOperation, op1, op2, size, prefixes);
   }
}

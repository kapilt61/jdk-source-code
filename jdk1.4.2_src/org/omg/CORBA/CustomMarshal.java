/*
 * @(#)CustomMarshal.java	1.13 03/01/28
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA;

import org.omg.CORBA.DataOutputStream;
import org.omg.CORBA.DataInputStream;

/**
 * An abstract value type that is meant to 
 * be used by the ORB, not the user. Semantically it is treated 
 * as a custom valuetype's implicit base class, although the custom 
 * valutype does not actually inherit it in IDL. The implementer 
 * of a custom value type shall provide an implementation of the 
 * <tt>CustomMarshal</tt> operations. The manner in which this is done is 
 * specified in the IDL to Java langague mapping. Each custom 
 * marshaled value type shall have its own implementation.
 * @see DataOutputStream
 * @see DataInputStream
 */
public interface CustomMarshal {
    /**
     * Marshal method has to be implemented by the Customized Marshal class
     * This is the method invoked for Marshalling.
     * 
     * @param os a DataOutputStream
     */ 
    void marshal(DataOutputStream os);
    /**
     * Unmarshal method has to be implemented by the Customized Marshal class
     * This is the method invoked for Unmarshalling.
     * 
     * @param is a DataInputStream
     */ 
    void unmarshal(DataInputStream is);
}

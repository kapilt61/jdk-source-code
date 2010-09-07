/*
 * @(#)InvalidAccessException.java	1.6 10/03/24
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.plugin.dom.exception;

import org.w3c.dom.DOMException;

/**
 *  DOM operations only raise exceptions in "exceptional" circumstances, 
 * i.e., when an operation is impossible to perform (either for logical 
 * reasons, because data is lost, or  because the implementation has become 
 * unstable). In general, DOM methods return specific error values in ordinary
 *  processing situations, such as out-of-bound errors when using 
 * <code>NodeList</code> .  
 * <p> Implementations may raise other exceptions under other circumstances. 
 * For example, implementations may raise an implementation-dependent  
 * exception if a <code>null</code> argument is passed. 
 * <p> Some languages and object systems do not support the concept of 
 * exceptions. For such systems, error conditions may be indicated using 
 * native error reporting mechanisms. For some bindings, for example, methods 
 * may return error codes similar to those listed in the corresponding method 
 * descriptions.
 * <p>See also the <a href='http://www.w3.org/TR/2000/CR-DOM-Level-2-20000510'>Document Object Model (DOM) Level 2 Specification</a>.
 */
public class InvalidAccessException extends DOMException 
{
    public InvalidAccessException(String message) 
    {
       super(INVALID_ACCESS_ERR, message);
    }
}


/*
 * @(#)GetPropertyAction.java	1.9 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.security.action;

/**
 * A convenience class for retrieving the string value of a system
 * property as a privileged action.
 *
 * <p>An instance of this class can be used as the argument of
 * <code>AccessController.doPrivileged</code>.
 *
 * <p>The following code retrieves the value of the system
 * property named <code>"prop"</code> as a privileged action: <p>
 *
 * <pre>
 * String s = (String) java.security.AccessController.doPrivileged(
 *                         new GetPropertyAction("prop"));
 * </pre>
 *
 * @author Roland Schemers
 * @version 1.9, 12/19/03
 * @see java.security.PrivilegedAction
 * @see java.security.AccessController
 * @since JDK1.2
 */

public class GetPropertyAction implements java.security.PrivilegedAction {
    private String theProp;
    private String defaultVal;

    /**
     * Constructor that takes the name of the system property whose
     * string value needs to be determined.
     *
     * @param theProp the name of the system property.
     */
    public GetPropertyAction(String theProp) {
	this.theProp = theProp;
    }

    /**
     * Constructor that takes the name of the system property and the default
     * value of that property.
     *
     * @param theProp the name of the system property.
     * @param defaulVal the default value.
     */
    public GetPropertyAction(String theProp, String defaultVal) {
	this.theProp = theProp;
	this.defaultVal = defaultVal;
    }

    /**
     * Determines the string value of the system property whose
     * name was specified in the constructor.
     *
     * @return the string value of the system property,
     *         or the default value if there is no property with that key.
     */
    public Object run() {
	String value = System.getProperty(theProp);
	return (value == null) ? defaultVal : value;
    }
}

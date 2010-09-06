/*
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.plugin.dom.html;

import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import sun.plugin.dom.DOMObject;
import sun.plugin.dom.DOMObjectHelper;
import sun.plugin.dom.DOMObjectFactory;

/**
 *  Client-side image map. See the  MAP element definition in HTML 4.0.
 * <p>See also the <a href='http://www.w3.org/TR/2000/CR-DOM-Level-2-20000510'>Document Object Model (DOM) Level 2 Specification</a>.
 */
public final class HTMLMapElement extends HTMLElement 
				  implements org.w3c.dom.html.HTMLMapElement {
    public HTMLMapElement(DOMObject obj, 
			  org.w3c.dom.html.HTMLDocument doc) {
	super(obj, doc);
    }

    /**
     *  The list of areas defined for the image map. 
     */
    public org.w3c.dom.html.HTMLCollection getAreas() {
	Object result = obj.getMember(HTMLConstants.MEMBER_AREAS);
	if(result != null && result instanceof DOMObject) {
		Object ret = DOMObjectFactory.createHTMLObject((DOMObject)result,
					    (org.w3c.dom.html.HTMLDocument)getOwnerDocument());
		if(ret instanceof org.w3c.dom.html.HTMLCollection) {
		    return (org.w3c.dom.html.HTMLCollection)ret;
		}
	}
	return null;	
    }

    /**
     *  Names the map (for use with <code>usemap</code> ). See the  name 
     * attribute definition in HTML 4.0.
     */
    public String getName() {
	return getAttribute(HTMLConstants.ATTR_NAME);
    }

    public void setName(String name) {
	setAttribute(HTMLConstants.ATTR_NAME, name);
    }
}


/*
 * @(#)ThemeReader.java	1.10 10/03/23
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.windows;

import java.awt.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.beans.*;
import javax.swing.SwingUtilities;

/* !!!! WARNING !!!!
 * This class has to be in sync with
 * src/solaris/classes/sun/awt/windows/ThemeReader.java
 * while we continue to build WinL&F on solaris
 */


/**
 * Implements Theme Support for Windows XP.
 *
 * @version 1.10 03/23/10
 * @author Sergey Salishev
 * @author Bino George
 * @author Igor Kushnirskiy
 */
public class ThemeReader {
    private static final HashMap<String, Long> widgetToTheme = 
        new HashMap<String, Long>();
    
    // lock for the cache
    // reading should be done with readLock 
    // writing with writeLock
    private static final ReadWriteLock readWriteLock = 
        new ReentrantReadWriteLock();
    private static final Lock readLock = readWriteLock.readLock();
    private static final Lock writeLock = readWriteLock.writeLock();

    static void flush() {
        writeLock.lock();
        try {
            // Close old themes.
            for (Long value : widgetToTheme.values()) {
                closeTheme(value.longValue());     
            }        
            widgetToTheme.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public native static boolean isThemed();

    // this should be called only with writeLock held
    private static Long getThemeImpl(String widget) {
        Long theme = widgetToTheme.get(widget);
        if (theme == null) {
	    int i = widget.indexOf("::");
	    if (i > 0) {
                // We're using the syntax "subAppName::controlName" here, as used by msstyles.
		// See documentation for SetWindowTheme on MSDN.
		setWindowTheme(widget.substring(0, i));
		theme = openTheme(widget.substring(i+2));
		setWindowTheme(null);
	    } else {
		theme = openTheme(widget);
	    }
            widgetToTheme.put(widget, theme);
        }
        return theme;
    }

    // returns theme value 
    // this method should be invoked with readLock locked
    private static Long getTheme(String widget) {
        // mostly copied from the javadoc for ReentrantReadWriteLock
        Long theme = widgetToTheme.get(widget);
        if (theme == null) {
            readLock.unlock();
            writeLock.lock();
            try {
                theme = getThemeImpl(widget);
            } finally {
                readLock.lock();
                writeLock.unlock();// Unlock write, still hold read
            }
        }
        return theme;
    }
 
    public native static void paintBackground(int[] buffer, long theme, int part, int state,
			     int x, int y, int w, int h, int stride); 

    public static void paintBackground(int[] buffer, String widget,
           int part, int state, int x, int y, int w, int h, int stride) {
        readLock.lock();
        try {
            paintBackground(buffer, getTheme(widget), part, state, x, y, w, h, stride);
        } finally {
            readLock.unlock();
        }
    }
    
    public native static Insets getThemeMargins(long theme, int part, int state,
 int marginType);

    public static Insets getThemeMargins(String widget, int part, int state, int marginType) {
        readLock.lock();
        try {
            return getThemeMargins(getTheme(widget), part, state, marginType); 
        } finally {
            readLock.unlock();
        }
    }

    private native static boolean isThemePartDefined(long theme, int part, int state);

    public static boolean isThemePartDefined(String widget, int part, int state) {
        readLock.lock();
        try {
            return isThemePartDefined(getTheme(widget), part, state);
        } finally {
            readLock.unlock();
        }
    }

    public native static Color getColor(long theme, int part, int state, 
                                                               int property);

    public static Color getColor(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            return getColor(getTheme(widget), part, state, property);
        } finally {
            readLock.unlock();
        }
    }
    
    public native static int getInt(long theme, int part, int state, 
                                                                int property);

    public static int getInt(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            return getInt(getTheme(widget), part, state, property);
        } finally {
            readLock.unlock();
        }
    }
    
    public native static int getEnum(long theme, int part, int state, 
                                                                int property);

    public static int getEnum(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            return getEnum(getTheme(widget), part, state, property); 
        } finally {
            readLock.unlock();
        }
    }
    
    public native static boolean getBoolean(long theme, int part, int state, 
                                                                int property);

    public static boolean getBoolean(String widget, int part, int state, 
                                     int property) {
        readLock.lock();
        try {
            return getBoolean(getTheme(widget), part, state, property);
        } finally {
            readLock.unlock();
        }
    }

    public native static boolean getSysBoolean(long theme, int property);

    public static boolean getSysBoolean(String widget, int property) {
        readLock.lock();
        try {
            return getSysBoolean(getTheme(widget), property);
        } finally {
            readLock.unlock();
        }
    }

    public native static Point getPoint(long theme, int part, int state, 
                                                                int property);

    public static Point getPoint(String widget, int part, int state, int property) {
        readLock.lock();
        try {
            return getPoint(getTheme(widget), part, state, property);
        } finally {
            readLock.unlock();
        }
    }
    
    public native static Dimension getPosition(long theme, int part, int state,
                                                                 int property);

    public static Dimension getPosition(String widget, int part, int state, 
                                        int property) {
        readLock.lock();
        try {
            return getPosition(getTheme(widget), part,state,property);
        } finally {
            readLock.unlock();
        }
    }

    public native static Dimension getPartSize(long theme, int part, int state);

    public static Dimension getPartSize(String widget, int part, int state) {
        readLock.lock();
        try {
            return getPartSize(getTheme(widget), part, state);
        } finally {
            readLock.unlock();
        }
    }

    public native static long openTheme(String widget);

    public native static void closeTheme(long theme);

    public native static void setWindowTheme(String subAppName);

    private native static long getThemeTransitionDuration(long theme, int part,
                                        int stateFrom, int stateTo, int propId);
    public static long getThemeTransitionDuration(String widget, int part, 
                                       int stateFrom, int stateTo, int propId) {
        readLock.lock();
        try {
            return getThemeTransitionDuration(getTheme(widget), 
                                              part, stateFrom, stateTo, propId);
        } finally {
            readLock.unlock();
        }
    }
    public native static boolean isGetThemeTransitionDurationDefined();

    private native static Insets getThemeBackgroundContentMargins(long theme, 
                     int part, int state, int boundingWidth, int boundingHeight);
    public static Insets getThemeBackgroundContentMargins(String widget,
                    int part, int state, int boundingWidth, int boundingHeight) {
        readLock.lock();
        try {
            return getThemeBackgroundContentMargins(getTheme(widget), 
                                    part, state, boundingWidth, boundingHeight);
        } finally {
            readLock.unlock();
        }
    }
}

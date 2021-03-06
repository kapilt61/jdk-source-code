/*
 * @(#)ManagementFactory.java	1.27 04/04/18
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.management;

import java.lang.management.*;
import java.util.logging.LogManager;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.RuntimeOperationsException;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import sun.security.action.LoadLibraryAction;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.ListIterator;
import com.sun.management.OSMBeanFactory;

import static java.lang.management.ManagementFactory.*;

/**
 * ManagementFactory provides static factory methods to create
 * instances of the management interface.
 */
public class ManagementFactory {
    private ManagementFactory() {};

    private static VMManagement jvm;

    private static boolean mbeansCreated = false;
    private static ClassLoadingImpl    classMBean = null;
    private static MemoryImpl          memoryMBean = null;
    private static ThreadImpl          threadMBean = null;
    private static RuntimeImpl         runtimeMBean = null;
    private static CompilationImpl     compileMBean = null;
    private static OperatingSystemImpl osMBean = null;

    public static synchronized ClassLoadingMXBean getClassLoadingMXBean() {
        if (classMBean == null) {
            classMBean = new ClassLoadingImpl(jvm);
        }
        return classMBean;
    }

    public static synchronized MemoryMXBean getMemoryMXBean() {
        if (memoryMBean == null) {
            memoryMBean = new MemoryImpl(jvm);
        }
        return memoryMBean;
    }

    public static synchronized ThreadMXBean getThreadMXBean() {
        if (threadMBean == null) {
            threadMBean = new ThreadImpl(jvm);
        }
        return threadMBean;
    }

    public static synchronized RuntimeMXBean getRuntimeMXBean() {
        if (runtimeMBean == null) {
            runtimeMBean = new RuntimeImpl(jvm);
        }
        return runtimeMBean;
    }

    public static synchronized CompilationMXBean getCompilationMXBean() {
        if (compileMBean == null && jvm.getCompilerName() != null) {
            compileMBean = new CompilationImpl(jvm);
        }
        return compileMBean;
    }

    public static synchronized OperatingSystemMXBean getOperatingSystemMXBean() {
        if (osMBean == null) {
            osMBean = (OperatingSystemImpl)
                          OSMBeanFactory.getOperatingSystemMXBean(jvm);
        }
        return osMBean;
    }

    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        MemoryPoolMXBean[] pools = MemoryImpl.getMemoryPools();
        List list = new ArrayList(pools.length);
        for (int i = 0; i < pools.length; i++) {
            MemoryPoolMXBean p = pools[i];
            list.add(p);
        }
        return list;
    }

    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        MemoryManagerMXBean[]  mgrs = MemoryImpl.getMemoryManagers();
        List result = new ArrayList(mgrs.length);
        for (int i = 0; i < mgrs.length; i++) {
            MemoryManagerMXBean m = mgrs[i];
            result.add(m);
        }
        return result;
    }

    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        MemoryManagerMXBean[]  mgrs = MemoryImpl.getMemoryManagers();
        List result = new ArrayList(mgrs.length);
        for (int i = 0; i < mgrs.length; i++) {
            if (mgrs[i] instanceof GarbageCollectorMXBean) {
                GarbageCollectorMXBean gc = (GarbageCollectorMXBean) mgrs[i];
                result.add(gc);
            }
        }
        return result;
    }

    private static HotspotRuntime hsRuntimeMBean = null;
    private static HotspotClassLoading hsClassMBean = null;
    private static HotspotThread hsThreadMBean = null;
    private static HotspotCompilation hsCompileMBean = null;
    private static HotspotMemory hsMemoryMBean = null;

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotRuntimeMBean getHotspotRuntimeMBean() {
        if (hsRuntimeMBean == null) {
            hsRuntimeMBean = new HotspotRuntime(jvm);
        }
        return hsRuntimeMBean;
    }

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotClassLoadingMBean getHotspotClassLoadingMBean() {
        if (hsClassMBean == null) {
            hsClassMBean = new HotspotClassLoading(jvm);
        }
        return hsClassMBean;
    }

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotThreadMBean getHotspotThreadMBean() {
        if (hsThreadMBean == null) {
            hsThreadMBean = new HotspotThread(jvm);
        }
        return hsThreadMBean;
    }

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotMemoryMBean getHotspotMemoryMBean() {
        if (hsMemoryMBean == null) {
            hsMemoryMBean = new HotspotMemory(jvm);
        }
        return hsMemoryMBean;
    }

    /**
     * This method is for testing only.
     */
    public static synchronized HotspotCompilationMBean getHotspotCompilationMBean() {
        if (hsCompileMBean == null) {
            hsCompileMBean = new HotspotCompilation(jvm);
        }
        return hsCompileMBean;
    }

    private static Permission monitorPermission = 
        new ManagementPermission("monitor");
    private static Permission controlPermission = 
        new ManagementPermission("control");

    /**
     * Check that the current context is trusted to perform monitoring 
     * or management.  
     * <p>
     * If the check fails we throw a SecurityException, otherwise
     * we return normally.
     *
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have ManagementPermission("control").
     */
    static void checkAccess(Permission p) 
         throws SecurityException {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(p);
	}
    }

    static void checkMonitorAccess() throws SecurityException {
        checkAccess(monitorPermission);
    }
    static void checkControlAccess() throws SecurityException {
        checkAccess(controlPermission);
    }

    // Register a MBean and throws exception if the instance with
    // the same name exists.
    private static void addMBean(MBeanServer mbs, Object mbean, 
                                 String mbeanName) {
        addMBean(mbs, mbean, mbeanName, false);
    }

    private static void addMBean(MBeanServer mbs, Object mbean, 
                                 String mbeanName, boolean ignoreConflicts) {
        try {
            final ObjectName objName = new ObjectName(mbeanName);

            // inner class requires these fields to be final
            final MBeanServer mbs0 = mbs;
            final Object mbean0 = mbean;
            final boolean ignore = ignoreConflicts;
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws InstanceAlreadyExistsException,
                                           MBeanRegistrationException,
                                           NotCompliantMBeanException {
                    try {
                        ObjectInstance o = mbs0.registerMBean(mbean0,
                                                              objName);
                        return null;
                    } catch (InstanceAlreadyExistsException e) {
                        // if an instance with the object name exists in
                        // the MBeanServer ignore the exception
                        // if ignoreConflicts is true;
                        // otherwise, throws exception.
                        if (!ignore) {
                             throw e;
                        }
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw Util.newException(e.getException());
        } catch (MalformedObjectNameException e) {
            // should not reach here
            throw Util.newException(e);
        }
    }

    public static MBeanServer createPlatformMBeanServer() {
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        // Register all the platform MBeans to this MBeanServer
        addMBean(mbs, getClassLoadingMXBean(), CLASS_LOADING_MXBEAN_NAME);
        addMBean(mbs, getMemoryMXBean(), MEMORY_MXBEAN_NAME);
        addMBean(mbs, getOperatingSystemMXBean(), OPERATING_SYSTEM_MXBEAN_NAME);
        addMBean(mbs, getRuntimeMXBean(), RUNTIME_MXBEAN_NAME);
        addMBean(mbs, getThreadMXBean(), THREAD_MXBEAN_NAME);

        // CompilationMBean may not exist
        if (getCompilationMXBean() != null) {
            addMBean(mbs, getCompilationMXBean(), COMPILATION_MXBEAN_NAME);
        }
    
        // Register MBeans for memory pools and memory managers
        addMemoryManagers(mbs);
        addMemoryPools(mbs);

        // Register platform extension 
        addMBean(mbs, LogManager.getLoggingMXBean(),
                 LogManager.LOGGING_MXBEAN_NAME);

        return mbs;
    }

    private final static String HOTSPOT_CLASS_LOADING_MBEAN_NAME =
        "sun.management:type=HotspotClassLoading";

    private final static String HOTSPOT_COMPILATION_MBEAN_NAME =
        "sun.management:type=HotspotCompilation";

    private final static String HOTSPOT_MEMORY_MBEAN_NAME =
        "sun.management:type=HotspotMemory";

    private static final String HOTSPOT_RUNTIME_MBEAN_NAME =
        "sun.management:type=HotspotRuntime";
 
    private final static String HOTSPOT_THREAD_MBEAN_NAME =
        "sun.management:type=HotspotThreading";

    private final static String HOTSPOT_INTERNAL_MBEAN_NAME =
        "sun.management:type=HotspotInternal";

    private static ObjectName hsInternalObjName = null;
    static synchronized ObjectName getHotspotInternalObjectName() {
        if (hsInternalObjName == null) {
            try {
                hsInternalObjName = new ObjectName(HOTSPOT_INTERNAL_MBEAN_NAME);
            } catch (MalformedObjectNameException e) {
                // should not reach here
                throw Util.newException(e);
            }
        }
        return hsInternalObjName;
    } 

    static void registerInternalMBeans(MBeanServer mbs) {
        // register all internal MBeans if not registered
        // No exception is thrown if a MBean with that object name 
        // already registered (i.e. ignore if name conflicts).
        addMBean(mbs, getHotspotClassLoadingMBean(), 
            HOTSPOT_CLASS_LOADING_MBEAN_NAME, true);
        addMBean(mbs, getHotspotMemoryMBean(), 
            HOTSPOT_MEMORY_MBEAN_NAME, true);
        addMBean(mbs, getHotspotRuntimeMBean(),
            HOTSPOT_RUNTIME_MBEAN_NAME, true);
        addMBean(mbs, getHotspotThreadMBean(),
            HOTSPOT_THREAD_MBEAN_NAME, true);

        // CompilationMBean may not exist
        if (getCompilationMXBean() != null) {
            addMBean(mbs, getHotspotCompilationMBean(), 
                HOTSPOT_COMPILATION_MBEAN_NAME, true);
        }
    }
    private static void unregisterMBean(MBeanServer mbs, String mbeanName) {
        try {
            final ObjectName objName = new ObjectName(mbeanName);

            // inner class requires these fields to be final
            final MBeanServer mbs0 = mbs;
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws MBeanRegistrationException,
                                           RuntimeOperationsException  {
                    try {
                        mbs0.unregisterMBean(objName);
                    } catch (InstanceNotFoundException e) {
                        // ignore exception if not found
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw Util.newException(e.getException());
        } catch (MalformedObjectNameException e) {
            // should not reach here
            throw Util.newException(e);
        }
    }

    static void unregisterInternalMBeans(MBeanServer mbs) {
        // unregister all internal MBeans 
        unregisterMBean(mbs, HOTSPOT_CLASS_LOADING_MBEAN_NAME);
        unregisterMBean(mbs, HOTSPOT_MEMORY_MBEAN_NAME);
        unregisterMBean(mbs, HOTSPOT_RUNTIME_MBEAN_NAME);
        unregisterMBean(mbs, HOTSPOT_THREAD_MBEAN_NAME);

        // CompilationMBean may not exist
        if (getCompilationMXBean() != null) {
            unregisterMBean(mbs, HOTSPOT_COMPILATION_MBEAN_NAME);
        }
    }

    private static synchronized void addMemoryPools(MBeanServer mbs) {

        // Get a list of memory pools
        MemoryPoolMXBean[] newPools = MemoryImpl.getMemoryPools();

        for (int i = 0; i < newPools.length; i++) {
            String poolObjNameString = Util.getMBeanObjectName(newPools[i]);
            addMBean(mbs, newPools[i], poolObjNameString);
        }
    }

    // Register all memory managers with the MBeanServer;
    private static synchronized void addMemoryManagers(MBeanServer mbs) {

        // Get a list of memory managers
        MemoryManagerMXBean[] newMgrs = MemoryImpl.getMemoryManagers();
        
        for (int i = 0; i < newMgrs.length; i++) {
            String mgrObjNameString = Util.getMBeanObjectName(newMgrs[i]);
            addMBean(mbs, newMgrs[i], mgrObjNameString);
        }
    }

    // Invoked by the VM
    private static MemoryPoolMXBean createMemoryPool
        (String name, boolean isHeap, long uThreshold, long gcThreshold) {
        return new MemoryPoolImpl(name, isHeap, uThreshold, gcThreshold); 
    }

    private static MemoryManagerMXBean createMemoryManager(String name) { 
        return new MemoryManagerImpl(name);
    }

    private static GarbageCollectorMXBean 
        createGarbageCollector(String name, String type) { 

        // ignore type parameter which is for future extension
        return new GarbageCollectorImpl(name);
    }

    static {
        AccessController.doPrivileged(new LoadLibraryAction("management"));
        jvm = new VMManagementImpl();
    }

    public static boolean isThreadSuspended(int state) {
        return ((state & JMM_THREAD_STATE_FLAG_SUSPENDED) != 0);
    }

    public static boolean isThreadRunningNative(int state) {
        return ((state & JMM_THREAD_STATE_FLAG_NATIVE) != 0);
    }

    public static Thread.State toThreadState(int state) {
        // suspended and native bits may be set in state
        int threadStatus = state & ~JMM_THREAD_STATE_FLAG_MASK;
        return sun.misc.VM.toThreadState(threadStatus);
    }

    // These values are defined in jmm.h
    private static final int JMM_THREAD_STATE_FLAG_MASK = 0xFFF00000;
    private static final int JMM_THREAD_STATE_FLAG_SUSPENDED = 0x00100000;
    private static final int JMM_THREAD_STATE_FLAG_NATIVE = 0x00400000;

}

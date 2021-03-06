/*
 * @(#)VMManagementImpl.c	1.14 10/03/23
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

#include <jni.h>
#include "jvm.h"
#include "management.h"
#include "sun_management_VMManagementImpl.h"

#define MAX_VERSION_LEN   20

JNIEXPORT jstring JNICALL 
Java_sun_management_VMManagementImpl_getVersion0
  (JNIEnv *env, jclass dummy) 
{
    char buf[MAX_VERSION_LEN];
    jstring version_string = NULL;

    unsigned int major = ((unsigned int) jmm_version & 0x0FFF0000) >> 16;
    unsigned int minor = ((unsigned int) jmm_version & 0xFF00) >> 8;

    // for internal use
    unsigned int micro = (unsigned int) jmm_version & 0xFF;

    sprintf(buf, "%d.%d", major, minor);
    version_string = (*env)->NewStringUTF(env, buf);
    return version_string;
}

static void setStaticBooleanField
   (JNIEnv* env, jclass cls, const char* name, jboolean value) 
{
    jfieldID fid;
    fid = (*env)->GetStaticFieldID(env, cls, name, "Z");
    if (fid != 0) {
        (*env)->SetStaticBooleanField(env, cls, fid, value);
    }
} 

JNIEXPORT void JNICALL 
Java_sun_management_VMManagementImpl_initOptionalSupportFields
  (JNIEnv *env, jclass cls)
{
    jmmOptionalSupport mos;
    jint ret = jmm_interface->GetOptionalSupport(env, &mos);

    jboolean value;

    value = mos.isCompilationTimeMonitoringSupported;
    setStaticBooleanField(env, cls, "compTimeMonitoringSupport", value);

    value = mos.isThreadContentionMonitoringSupported;
    setStaticBooleanField(env, cls, "threadContentionMonitoringSupport", value);

    value = mos.isCurrentThreadCpuTimeSupported;
    setStaticBooleanField(env, cls, "currentThreadCpuTimeSupport", value);

    value = mos.isOtherThreadCpuTimeSupported;
    setStaticBooleanField(env, cls, "otherThreadCpuTimeSupport", value);

    value = mos.isBootClassPathSupported;
    setStaticBooleanField(env, cls, "bootClassPathSupport", value);

    if (jmm_version >= JMM_VERSION_1_1) {
        value = mos.isObjectMonitorUsageSupported;
        setStaticBooleanField(env, cls, "objectMonitorUsageSupport", value);

        value = mos.isSynchronizerUsageSupported;
        setStaticBooleanField(env, cls, "synchronizerUsageSupport", value);
    } else {
        setStaticBooleanField(env, cls, "objectMonitorUsageSupport", JNI_FALSE);
        setStaticBooleanField(env, cls, "synchronizerUsageSupport", JNI_FALSE);
    }
}

JNIEXPORT jstring JNICALL 
Java_sun_management_VMManagementImpl_getVmArguments0
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetInputArguments(env);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getTotalClassCount
  (JNIEnv *env, jobject dummy)
{
    /* JMM_CLASS_LOADED_COUNT is the total number of classes loaded */
    jlong count = jmm_interface->GetLongAttribute(env, NULL,
                                                  JMM_CLASS_LOADED_COUNT);
    return count;
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getUnloadedClassCount
  (JNIEnv *env, jobject dummy)
{
    /* JMM_CLASS_UNLOADED_COUNT is the total number of classes unloaded */
    jlong count = jmm_interface->GetLongAttribute(env, NULL, 
                                                  JMM_CLASS_UNLOADED_COUNT);
    return count;
}

JNIEXPORT jboolean JNICALL
Java_sun_management_VMManagementImpl_getVerboseGC
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetBoolAttribute(env, JMM_VERBOSE_GC);
}

JNIEXPORT jboolean JNICALL
Java_sun_management_VMManagementImpl_getVerboseClass
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetBoolAttribute(env, JMM_VERBOSE_CLASS);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getTotalThreadCount
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_THREAD_TOTAL_COUNT);
}

JNIEXPORT jint JNICALL
Java_sun_management_VMManagementImpl_getLiveThreadCount
  (JNIEnv *env, jobject dummy)
{
    jlong count = jmm_interface->GetLongAttribute(env, NULL,
                                                  JMM_THREAD_LIVE_COUNT);
    return (jint) count;
}

JNIEXPORT jint JNICALL
Java_sun_management_VMManagementImpl_getPeakThreadCount
  (JNIEnv *env, jobject dummy)
{
    jlong count = jmm_interface->GetLongAttribute(env, NULL,
                                                  JMM_THREAD_PEAK_COUNT);
    return (jint) count;
}

JNIEXPORT jint JNICALL
Java_sun_management_VMManagementImpl_getDaemonThreadCount
  (JNIEnv *env, jobject dummy)
{
    jlong count = jmm_interface->GetLongAttribute(env, NULL,
                                                  JMM_THREAD_DAEMON_COUNT);
    return (jint) count;
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getTotalCompileTime
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_COMPILE_TOTAL_TIME_MS);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getStartupTime
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_JVM_INIT_DONE_TIME_MS);
}

JNIEXPORT jboolean JNICALL
Java_sun_management_VMManagementImpl_isThreadContentionMonitoringEnabled
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetBoolAttribute(env,
                                           JMM_THREAD_CONTENTION_MONITORING);
}

JNIEXPORT jboolean JNICALL
Java_sun_management_VMManagementImpl_isThreadCpuTimeEnabled
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetBoolAttribute(env, JMM_THREAD_CPU_TIME);
}

JNIEXPORT jint JNICALL
Java_sun_management_VMManagementImpl_getProcessId
  (JNIEnv *env, jobject dummy)
{
    jlong pid = jmm_interface->GetLongAttribute(env, NULL,
                                                JMM_OS_PROCESS_ID);
    return (jint) pid;
}

JNIEXPORT jint JNICALL
Java_sun_management_VMManagementImpl_getAvailableProcessors
  (JNIEnv *env, jobject dummy)
{
    return JVM_ActiveProcessorCount();
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getSafepointCount
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_SAFEPOINT_COUNT);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getTotalSafepointTime
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_TOTAL_STOPPED_TIME_MS);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getSafepointSyncTime
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_TOTAL_SAFEPOINTSYNC_TIME_MS);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getTotalApplicationNonStoppedTime
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_TOTAL_APP_TIME_MS);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getLoadedClassSize
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_CLASS_LOADED_BYTES);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getUnloadedClassSize
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_CLASS_UNLOADED_BYTES);
}
JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getClassLoadingTime
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_TOTAL_CLASSLOAD_TIME_MS);
}


JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getMethodDataSize
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_METHOD_DATA_SIZE_BYTES);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getInitializedClassCount
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_CLASS_INIT_TOTAL_COUNT);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getClassInitializationTime
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_CLASS_INIT_TOTAL_TIME_MS);
}

JNIEXPORT jlong JNICALL
Java_sun_management_VMManagementImpl_getClassVerificationTime
  (JNIEnv *env, jobject dummy)
{
    return jmm_interface->GetLongAttribute(env, NULL,
                                           JMM_CLASS_VERIFY_TOTAL_TIME_MS);
}

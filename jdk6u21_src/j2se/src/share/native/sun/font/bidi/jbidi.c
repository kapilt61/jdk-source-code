/*
 *  @(#)jbidi.c	1.10 05/11/17
 *
 * (C) Copyright IBM Corp. 2000 - 2003 - All Rights Reserved
 *
 * Portions Copyright 2006 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by IBM. These materials are provided
 * under terms of a License Agreement between IBM and Sun.
 * This technology is protected by multiple US and International
 * patents. This notice and attribution to IBM may not be removed.
 */

// jni interface to native bidi from java

#include <stdlib.h>
#include "jbidi.h"

#define U_COMMON_IMPLEMENTATION
#include "ubidi.h"
#include "ubidiimp.h"
#include "uchardir.h"

static jclass g_bidi_class = 0;
static jmethodID g_bidi_reset = 0;

static void resetBidi(JNIEnv *env, jclass cls, jobject bidi, jint dir, jint level, jint len, jintArray runs, jintArray cws) {
    if (!g_bidi_class) {
	  g_bidi_class = (*env)->NewGlobalRef(env, cls);
	  g_bidi_reset = (*env)->GetMethodID(env, g_bidi_class, "reset", "(III[I[I)V");
    }

	(*env)->CallVoidMethod(env, bidi, g_bidi_reset, dir, level, len, runs, cws);
}

JNIEXPORT jint JNICALL Java_java_text_Bidi_nativeGetDirectionCode
  (JNIEnv *env, jclass cls, jint cp) 
{
    return (jint)u_getDirection((uint32_t)cp);
}

JNIEXPORT void JNICALL Java_java_text_Bidi_nativeBidiChars
  (JNIEnv *env, jclass cls, jobject jbidi, jcharArray text, jint tStart, jbyteArray embs, jint eStart, jint length, jint dir)
{
    UErrorCode err = U_ZERO_ERROR;
    UBiDi* bidi = ubidi_openSized(length, length, &err);
    if (!U_FAILURE(err)) {
        jchar *cText = (jchar*)(*env)->GetPrimitiveArrayCritical(env, text, NULL);
        if (cText) {
            UBiDiLevel baseLevel = (UBiDiLevel)dir;
            jbyte *cEmbs = 0;
	    uint8_t *cEmbsAdj = 0;
            if (embs != NULL) {
                cEmbs = (jbyte*)(*env)->GetPrimitiveArrayCritical(env, embs, NULL);
		if (cEmbs) {
		    cEmbsAdj = (uint8_t*)(cEmbs + eStart);
		}
            }
            ubidi_setPara(bidi, cText + tStart, length, baseLevel, cEmbsAdj, &err);
	    if (cEmbs) {
		(*env)->ReleasePrimitiveArrayCritical(env, embs, cEmbs, JNI_ABORT);
	    }

            (*env)->ReleasePrimitiveArrayCritical(env, text, cText, JNI_ABORT);

            if (!U_FAILURE(err)) {
                jint resDir = (jint)ubidi_getDirection(bidi);
                jint resLevel = (jint)ubidi_getParaLevel(bidi);
                jint resRunCount = 0;
                jintArray resRuns = 0;
                jintArray resCWS = 0;
                if (resDir == UBIDI_MIXED) {
                    resRunCount = (jint)ubidi_countRuns(bidi, &err);
                    if (!U_FAILURE(err)) {
                        if (resRunCount) {
                            jint* cResRuns = (jint*)calloc(resRunCount * 2, sizeof(jint));
                                  if (cResRuns) {
                                    int32_t limit = 0;
                                    UBiDiLevel level;
                                    jint *p = cResRuns;
                                    while (limit < length) {
                                        ubidi_getLogicalRun(bidi, limit, &limit, &level);
                                        *p++ = (jint)limit;
                                        *p++ = (jint)level;
                                    }

                                    {
                                        const DirProp *dp = bidi->dirProps;
                                        jint ccws = 0;
                                        jint n = 0;
                                        p = cResRuns;
                                        do {
                                            if ((*(p+1) ^ resLevel) & 0x1) {
                                                while (n < *p) {
                                                    if (dp[n++] == WS) {
                                                        ++ccws;
                                                    }
                                                }
                                            } else {
                                                n = *p;
                                            }
                                            p += 2;
                                        } while (n < length);

                                        resCWS = (*env)->NewIntArray(env, ccws);
                                        if (resCWS) {
                                            jint* cResCWS = (jint*)(*env)->GetPrimitiveArrayCritical(env, resCWS, NULL);
                                            if (cResCWS) {
                                                jint ccws = 0;
                                                jint n = 0;
                                                p = cResRuns;
                                                do {
                                                    if ((*(p+1) ^ resLevel) & 0x1) {
                                                        while (n < *p) {
                                                            if (dp[n] == WS) {
                                                                cResCWS[ccws++] = n;
                                                            }
                                                            ++n;
                                                        }
                                                    } else {
                                                        n = *p;
                                                    }
                                                    p += 2;
                                                } while (n < length);
                                                (*env)->ReleasePrimitiveArrayCritical(env, resCWS, cResCWS, 0);
                                            }
                                        }
                                    }

                                    resRuns = (*env)->NewIntArray(env, resRunCount * 2);
				    if (resRuns) {
					(*env)->SetIntArrayRegion(env, resRuns, 0, resRunCount * 2, cResRuns);
				    }
                                    free(cResRuns);
                                }
                            }
                        }
                    }

                resetBidi(env, cls, jbidi, resDir, resLevel, length, resRuns, resCWS);
            }
        }
        ubidi_close(bidi);
    }
}



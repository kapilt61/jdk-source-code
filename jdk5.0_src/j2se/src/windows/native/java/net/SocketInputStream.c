/*
 * @(#)SocketInputStream.c	1.36 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

#include <windows.h>
#include <winsock2.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <sys/types.h>

#include "java_net_SocketInputStream.h"

#include "net_util.h"
#include "jni_util.h"

/*************************************************************************
 * SocketInputStream
 */

static jfieldID IO_fd_fdID;

/*
 * Class:     java_net_SocketInputStream
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL 
Java_java_net_SocketInputStream_init(JNIEnv *env, jclass cls) {
    IO_fd_fdID = NET_GetFileDescriptorID(env);
}

/*
 * Class:     java_net_SocketInputStream
 * Method:    socketRead
 * Signature: (Ljava/io/FileDescriptor;[BIII)I
 */
JNIEXPORT jint JNICALL
Java_java_net_SocketInputStream_socketRead0(JNIEnv *env, jobject this, 
					    jobject fdObj, jbyteArray data, 
					    jint off, jint len, jint timeout) 
{
    char *bufP;
    char BUF[MAX_BUFFER_LEN];
    jint fd, newfd;
    jint nread;

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "socket closed");
	return -1;
    } 
    fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID); 
    if (fd == -1) {
	NET_ThrowSocketException(env, "Socket closed");
	return -1;
    }
    
    /*
     * If the caller buffer is large than our stack buffer then we allocate
     * from the heap (up to a limit). If memory is exhausted we always use
     * the stack buffer.
     */
    if (len <= MAX_BUFFER_LEN) {
	bufP = BUF;
    } else {
	if (len > MAX_HEAP_BUFFER_LEN) {
	    len = MAX_HEAP_BUFFER_LEN;
	}
	bufP = (char *)malloc((size_t)len);
	if (bufP == NULL) {
	    /* allocation failed so use stack buffer */
	    bufP = BUF;
	    len = MAX_BUFFER_LEN;
	}
    }


    if (timeout) {
	if (timeout <= 5000 || !isRcvTimeoutSupported) {
	    int ret = NET_Timeout (fd, timeout);
	    
	    if (ret <= 0) {
		if (ret == 0) {
 		    JNU_ThrowByName(env, JNU_JAVANETPKG "SocketTimeoutException",
				    "Read timed out");
		} else if (ret == JVM_IO_ERR) {
		    JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "socket closed");
		} else if (ret == JVM_IO_INTR) {
		    JNU_ThrowByName(env, JNU_JAVAIOPKG "InterruptedIOException",
				    "Operation interrupted");
		}
		if (bufP != BUF) {
		    free(bufP);
		}
		return -1;
	    }

	    /*check if the socket has been closed while we were in timeout*/ 
	    newfd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);
	    if (newfd == -1) {
		NET_ThrowSocketException(env, "Socket Closed");
		return -1;
	    }  
	}
    }

    nread = recv(fd, bufP, len, 0);
    if (nread > 0) {
	(*env)->SetByteArrayRegion(env, data, off, nread, (jbyte *)bufP);
    } else {
	if (nread < 0) {
	    /*
	     * Recv failed.
	     */
	    switch (WSAGetLastError()) {
	        case WSAEINTR:
		    JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", 
			"socket closed");
		    break;
	
		case WSAECONNRESET:
		case WSAESHUTDOWN:
		    /*
		     * Connection has been reset - Windows sometimes reports
		     * the reset as a shutdown error.
		     */
		    JNU_ThrowByName(env, "sun/net/ConnectionResetException",
			"");
		    break;

	        case WSAETIMEDOUT :
                    JNU_ThrowByName(env, JNU_JAVANETPKG "SocketTimeoutException",
                                   "Read timed out");
		    break;

		default:
		    NET_ThrowCurrent(env, "recv failed");
	    }
	}
    }
    if (bufP != BUF) {
	free(bufP);
    }
    return nread;
}

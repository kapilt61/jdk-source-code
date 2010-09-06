/*
 * @(#)GssKrb5Client.java	1.18 04/02/03
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.security.sasl.gsskerb; 

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.security.sasl.*;

// JAAS
import javax.security.auth.callback.CallbackHandler;

// JGSS
import org.ietf.jgss.*;

/**
  * Implements the GSSAPI SASL client mechanism for Kerberos V5. 
  * (<A HREF="ftp://ftp.isi.edu/in-notes/rfc2222.txt">RFC 2222</A>,
  * <a HREF="http://www.ietf.org/internet-drafts/draft-ietf-cat-sasl-gssapi-04.txt">draft-ietf-cat-sasl-gssapi-04.txt</a>).
  * It uses the Java Bindings for GSSAPI
  * (<A HREF="ftp://ftp.isi.edu/in-notes/rfc2853.txt">RFC 2853</A>)
  * for getting GSSAPI/Kerberos V5 support.
  *
  * The client/server interactions are:
  * C0: bind (GSSAPI, initial response)
  * S0: sasl-bind-in-progress, challenge 1 (output of accept_sec_context or [])
  * C1: bind (GSSAPI, response 1 (output of init_sec_context or []))
  * S1: sasl-bind-in-progress challenge 2 (security layer, server max recv size)
  * C2: bind (GSSAPI, response 2 (security layer, client max recv size, authzid))
  * S2: bind success response
  *
  * Expects thread's Subject to contain client's Kerberos credentials
  * - If not, underlying KRB5 mech will attempt to acquire Kerberos creds
  *   by logging into Kerberos (via default TextCallbackHandler).
  * - These creds will be used for exchange with server.
  * 
  * Required callbacks: none.
  *
  * Environment properties that affect behavior of implementation:
  *
  * javax.security.sasl.qop
  * - quality of protection; list of auth, auth-int, auth-conf; default is "auth"
  * javax.security.sasl.maxbuf
  * - max receive buffer size; default is 65536
  * javax.security.sasl.sendmaxbuffer
  * - max send buffer size; default is 65536; (min with server max recv size)
  *
  * javax.security.sasl.server.authentication
  * - "true" means require mutual authentication; default is "false"
  * 
  *
  * @author Rosanna Lee
  */

final class GssKrb5Client extends GssKrb5Base implements SaslClient {
    // ---------------- Constants -----------------
    private static final String MY_CLASS_NAME = GssKrb5Client.class.getName();

    private boolean finalHandshake = false; 
    private boolean mutual = false;       // default false
    private byte[] authzID;

    /**
     * Creates a SASL mechanism with client credentials that it needs 
     * to participate in GSS-API/Kerberos v5 authentication exchange 
     * with the server.
     */
    GssKrb5Client(String authzID, String protocol, String serverName,
	Map props, CallbackHandler cbh) throws SaslException {

	super(props, MY_CLASS_NAME);

	String service = protocol + "@" + serverName;
	logger.log(Level.FINE, "KRB5CLNT01:Requesting service name: {0}", 
	    service);

	try {
	    GSSManager mgr = GSSManager.getInstance();

	    // Create the name for the requested service entity for Krb5 mech
	    GSSName acceptorName = mgr.createName(service,
		GSSName.NT_HOSTBASED_SERVICE, KRB5_OID);

	    // Create a context using default credentials for Krb5 mech
	    secCtx = mgr.createContext(acceptorName,
		KRB5_OID,   /* mechanism */
		null,       /* default credentials */
		GSSContext.INDEFINITE_LIFETIME);

	    // Parse properties  to set desired context options
	    if (props != null) {
		// Mutual authentication
		String prop = (String)props.get(Sasl.SERVER_AUTH);
		if (prop != null) {
		    mutual = "true".equalsIgnoreCase(prop);
		}
	    }
	    secCtx.requestMutualAuth(mutual);

	    // Always specify potential need for integrity and confidentiality
	    // Decision will be made during final handshake
	    secCtx.requestConf(true);
	    secCtx.requestInteg(true);
		
	} catch (GSSException e) {
	    throw new SaslException("Failure to initialize security context", e);
	}

	if (authzID != null && authzID.length() > 0) {
	    try {
		this.authzID = authzID.getBytes("UTF8");
	    } catch (IOException e) {
		throw new SaslException("Cannot encode authorization ID", e);
	    }
	}
    }

    public boolean hasInitialResponse() {
	return true;
    }

    /**
     * Processes the challenge data.
     * 
     * The server sends a challenge data using which the client must
     * process using GSS_Init_sec_context.
     * As per RFC 2222, when GSS_S_COMPLETE is returned, we do
     * an extra handshake to determine the negotiated security protection
     * and buffer sizes.
     *
     * @param challengeData A non-null byte array containing the
     * challenge data from the server.
     * @return A non-null byte array containing the response to be
     * sent to the server.
     */
    public byte[] evaluateChallenge(byte[] challengeData) throws SaslException {
	if (completed) {
	    throw new IllegalStateException(
		"GSSAPI authentication already complete");
	}

	if (finalHandshake) {
	    return doFinalHandshake(challengeData);
	} else {

	    // Security context not established yet; continue with init

	    try {
		byte[] gssOutToken = secCtx.initSecContext(challengeData,
		    0, challengeData.length);
		if (logger.isLoggable(Level.FINER)) {
		    traceOutput(MY_CLASS_NAME, "evaluteChallenge",
			"KRB5CLNT02:Challenge: [raw]", challengeData);
		    traceOutput(MY_CLASS_NAME, "evaluateChallenge",
			"KRB5CLNT03:Response: [after initSecCtx]", gssOutToken);
		}

		if (secCtx.isEstablished()) {
		    finalHandshake = true;
		    if (gssOutToken == null) {
			// RFC 2222 7.2.1:  Client responds with no data
			return EMPTY;
		    }
		}

		return gssOutToken;
	    } catch (GSSException e) {
		throw new SaslException("GSS initiate failed", e);
	    }
	}
    }

    private byte[] doFinalHandshake(byte[] challengeData) throws SaslException {
	try {
	    // Security context already established. challengeData
	    // should contain security layers and server's maximum buffer size

	    if (logger.isLoggable(Level.FINER)) {
		traceOutput(MY_CLASS_NAME, "doFinalHandshake",
		    "KRB5CLNT04:Challenge [raw]:", challengeData);
	    }

	    if (challengeData.length == 0) {
		// Received S0, should return []
		return EMPTY;
	    }

	    // Received S1 (security layer, server max recv size)
	    
	    byte[] gssOutToken = secCtx.unwrap(challengeData, 0,
		challengeData.length, new MessageProp(0, false));

	    // First octet is a bit-mask specifying the protections
	    // supported by the server
	    if (logger.isLoggable(Level.FINE)) {
		if (logger.isLoggable(Level.FINER)) {
		    traceOutput(MY_CLASS_NAME, "doFinalHandshake",
			"KRB5CLNT05:Challenge [unwrapped]:", gssOutToken);
		}
		logger.log(Level.FINE, "KRB5CLNT06:Server protections: {0}",
		    new Byte(gssOutToken[0]));
	    }

	    // Client selects preferred protection
	    // qop is ordered list of qop values
	    byte selectedQop = findPreferredMask(gssOutToken[0], qop);
	    if (selectedQop == 0) {
		throw new SaslException(
		    "No common protection layer between client and server");
	    }

	    if ((selectedQop&PRIVACY_PROTECTION) != 0) {
		privacy = true;
		integrity = true;
	    } else if ((selectedQop&INTEGRITY_ONLY_PROTECTION) != 0) {
		integrity = true;
	    } 

	    // 2nd-4th octets specifies maximum buffer size expected by
	    // server (in network byte order)
	    int srvMaxBufSize = networkByteOrderToInt(gssOutToken, 1, 3);

	    // Determine the max send buffer size based on what the
	    // server is able to receive and our specified max
	    sendMaxBufSize = (sendMaxBufSize == 0) ? srvMaxBufSize :
		Math.min(sendMaxBufSize, srvMaxBufSize);

	    // Update context to limit size of returned buffer
	    rawSendSize = secCtx.getWrapSizeLimit(JGSS_QOP, privacy,
		sendMaxBufSize);

	    if (logger.isLoggable(Level.FINE)) {
		logger.log(Level.FINE, 
"KRB5CLNT07:Client max recv size: {0}; server max recv size: {1}; rawSendSize: {2}",
		    new Object[] {new Integer(recvMaxBufSize),
				  new Integer(srvMaxBufSize),
				  new Integer(rawSendSize)}); 
	    }

	    // Construct negotiated security layers and client's max
	    // receive buffer size and authzID
	    int len = 4;
	    if (authzID != null) {
		len += authzID.length;
	    }

	    byte[] gssInToken = new byte[len];
	    gssInToken[0] = selectedQop;

	    if (logger.isLoggable(Level.FINE)) {
		logger.log(Level.FINE,
	    "KRB5CLNT08:Selected protection: {0}; privacy: {1}; integrity: {2}",
		    new Object[]{new Byte(selectedQop),
				 Boolean.valueOf(privacy), 
				 Boolean.valueOf(integrity)});
	    }

	    intToNetworkByteOrder(recvMaxBufSize, gssInToken, 1, 3);
	    if (authzID != null) {
		// copy authorization id
		System.arraycopy(authzID, 0, gssInToken, 4, authzID.length);
		logger.log(Level.FINE, "KRB5CLNT09:Authzid: {0}", authzID);
	    }

	    if (logger.isLoggable(Level.FINER)) {
		traceOutput(MY_CLASS_NAME, "doFinalHandshake",
		    "KRB5CLNT10:Response [raw]", gssInToken);
	    }

	    gssOutToken = secCtx.wrap(gssInToken,
		0, gssInToken.length,
		new MessageProp(0 /* qop */, false /* privacy */));

	    if (logger.isLoggable(Level.FINER)) {
		traceOutput(MY_CLASS_NAME, "doFinalHandshake",
		    "KRB5CLNT11:Response [after wrap]", gssOutToken);
	    }
	    
	    completed = true;  // server authenticated
	    msgProp = new MessageProp(JGSS_QOP, privacy);

	    return gssOutToken;
	} catch (GSSException e) {
	    throw new SaslException("Final handshake failed", e);
	}
    }
}
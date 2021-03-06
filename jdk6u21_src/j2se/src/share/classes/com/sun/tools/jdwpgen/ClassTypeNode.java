/*
 * @(#)ClassTypeNode.java	1.12 10/03/23
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.jdwpgen;

import java.util.*;

class ClassTypeNode extends ReferenceTypeNode {

    String docType() {
        return "classID";
    }

    String javaType() {
        return "ClassTypeImpl";
    }

    String javaRead() {
        return "vm.classType(ps.readClassRef())";
    }
}

@echo off
REM
REM Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
REM ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM
REM  
REM

call saenv.bat

%SA_JAVA_CMD% sun.jvm.hotspot.HSDB %1 %2


 /* @(#)w_lgamma.c	1.10 10/03/23           */
/*
 * @(#)w_lgamma.c	1.10 10/03/23
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/* double lgamma(double x)
 * Return the logarithm of the Gamma function of x.
 *
 * Method: call __ieee754_lgamma_r
 */

#include "fdlibm.h"

extern int signgam;

#ifdef __STDC__
	double lgamma(double x)
#else
	double lgamma(x)
	double x;
#endif
{
#ifdef _IEEE_LIBM
	return __ieee754_lgamma_r(x,&signgam);
#else
        double y;
        y = __ieee754_lgamma_r(x,&signgam);
        if(_LIB_VERSION == _IEEE_) return y;
        if(!finite(y)&&finite(x)) {
            if(floor(x)==x&&x<=0.0)
                return __kernel_standard(x,x,15); /* lgamma pole */
            else
                return __kernel_standard(x,x,14); /* lgamma overflow */
        } else
            return y;
#endif
}

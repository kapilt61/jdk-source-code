#ifdef USE_PRAGMA_IDENT_HDR
#pragma ident "@(#)c1_globals_sparc.hpp	1.56 04/05/05 16:15:14 JVM"
#endif
/*
 * Copyright 2004 Sun Microsystems, Inc.  All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.  Use is subject to license terms.
 */

//
// Sets the default values for platform dependent flags used by the client compiler.
// (see c1_globals.hpp)
//

define_pd_global(bool, PrintNotLoaded,               false);
define_pd_global(bool, ComputeLoops,                 true );
define_pd_global(bool, OptimizeLibraryCalls,         true );
define_pd_global(bool, UseTableRanges,               false);
define_pd_global(bool, UseOnStackReplacement,        true );
define_pd_global(bool, InlineIntrinsics,             true );
define_pd_global(bool, CICompileOSR,                 true );
define_pd_global(bool, BackgroundCompilation,        false);
define_pd_global(bool, UseTLAB,                      true );
define_pd_global(bool, ResizeTLAB,                   true );
define_pd_global(bool, UseTypeProfile,               false);
define_pd_global(bool, PreferInterpreterNativeStubs, false);
define_pd_global(bool, RoundFloatsWithStore,         false);
define_pd_global(bool, ProfileInterpreter,           false);
define_pd_global(bool, ProfileTraps,                 false);
define_pd_global(bool, TieredCompilation,            false);

define_pd_global(intx, CompileThreshold,             1000 ); // Design center runs on 1.3.1
define_pd_global(intx, Tier2CompileThreshold,        1500 );
define_pd_global(intx, CompileThresholdMin,          500  );
define_pd_global(intx, CompileThresholdMax,          50000);
define_pd_global(intx, OnStackReplacePercentage,     1400 );
define_pd_global(intx, FreqInlineSize,               325  );
define_pd_global(intx, NewSizeThreadIncrease,        16*K );
define_pd_global(intx, NewRatio,                     8    ); // Design center runs on 1.3.1  

define_pd_global(intx, InitialCodeCacheSize,         160*K);
define_pd_global(intx, ReservedCodeCacheSize,        32*M );
define_pd_global(intx, CodeCacheExpansionSize,       32*K );
define_pd_global(uintx,CodeCacheMinBlockLength,      1);
define_pd_global(bool, CacheCallFreeLoopsOnly,       false);
define_pd_global(bool, CacheFloats,                  true);
define_pd_global(bool, CacheDoubleWord,              false);
define_pd_global(bool, LIROptimizeDeleteOps,         true);
define_pd_global(bool, LIROptimizeStack,             false);
define_pd_global(bool, LIROptimizeFloats,            true);
define_pd_global(bool, LIRFillDelaySlots,            true);
define_pd_global(bool, EliminateLoadsAcrossCalls,    false);
define_pd_global(bool, EliminateFieldAccess,         false);
define_pd_global(bool, EliminateArrayAccess,         false);
define_pd_global(bool, OptimizeSinglePrecision,      false);
define_pd_global(bool, CSEArrayLength,               true);

define_pd_global(uintx, PermSize,                     8*M );
define_pd_global(uintx, MaxPermSize,                 64*M );

define_pd_global(bool, NeverActAsServerClassMachine, true);
define_pd_global(intx, SafepointPollOffset, 0);

/*
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

# include "incls/_precompiled.incl"
# include "incls/_vmPSOperations.cpp.incl"

// The following methods are used by the parallel scavenge collector
VM_ParallelGCFailedAllocation::VM_ParallelGCFailedAllocation(size_t size,
  bool is_tlab, unsigned int gc_count) :
  VM_GC_Operation(gc_count),
  _size(size),
  _is_tlab(is_tlab),
  _result(NULL)
{
}

void VM_ParallelGCFailedAllocation::doit() {
  JvmtiGCForAllocationMarker jgcm;
  notify_gc_begin(false);

  ParallelScavengeHeap* heap = (ParallelScavengeHeap*)Universe::heap();
  assert(heap->kind() == CollectedHeap::ParallelScavengeHeap, "must be a ParallelScavengeHeap");

  GCCauseSetter gccs(heap, _gc_cause);
  _result = heap->failed_mem_allocate(_size, _is_tlab);

  if (_result == NULL && GC_locker::is_active_and_needs_gc()) {
    set_gc_locked();
  }

  notify_gc_end();
}

VM_ParallelGCFailedPermanentAllocation::VM_ParallelGCFailedPermanentAllocation(size_t size,
  unsigned int gc_count, unsigned int full_gc_count) :
  VM_GC_Operation(gc_count, full_gc_count, true /* full */),
  _size(size),
  _result(NULL)
{
}

void VM_ParallelGCFailedPermanentAllocation::doit() {
  JvmtiGCFullMarker jgcm;
  notify_gc_begin(true);

  ParallelScavengeHeap* heap = (ParallelScavengeHeap*)Universe::heap();
  assert(heap->kind() == CollectedHeap::ParallelScavengeHeap, "must be a ParallelScavengeHeap");

  GCCauseSetter gccs(heap, _gc_cause);
  _result = heap->failed_permanent_mem_allocate(_size);
  if (_result == NULL && GC_locker::is_active_and_needs_gc()) {
    set_gc_locked();
  }
  notify_gc_end();
}

// Only used for System.gc() calls
VM_ParallelGCSystemGC::VM_ParallelGCSystemGC(unsigned int gc_count,
                                             unsigned int full_gc_count,
                                             GCCause::Cause gc_cause) :
  VM_GC_Operation(gc_count, full_gc_count, true /* full */)
{
  _gc_cause = gc_cause;
}

void VM_ParallelGCSystemGC::doit() {
  JvmtiGCFullMarker jgcm;
  notify_gc_begin(true);

  ParallelScavengeHeap* heap = (ParallelScavengeHeap*)Universe::heap();
  assert(heap->kind() == CollectedHeap::ParallelScavengeHeap,
    "must be a ParallelScavengeHeap");

  GCCauseSetter gccs(heap, _gc_cause);
  if (_gc_cause == GCCause::_gc_locker
      DEBUG_ONLY(|| _gc_cause == GCCause::_scavenge_alot)) {
    // If (and only if) the scavenge fails, this will invoke a full gc.
    heap->invoke_scavenge();
  } else {
    heap->invoke_full_gc(false);
  }
  notify_gc_end();
}

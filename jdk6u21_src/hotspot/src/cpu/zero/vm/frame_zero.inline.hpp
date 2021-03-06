/*
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
 * Copyright 2007, 2008, 2009 Red Hat, Inc.
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

// Constructors

inline frame::frame() {
  _sp = NULL;
  _fp = NULL;
  _pc = NULL;
  _cb = NULL;
  _deopt_state = unknown;
}

inline frame::frame(intptr_t* sp, intptr_t* fp) {
  _sp = sp;
  _fp = fp;
  switch (zeroframe()->type()) {
  case ZeroFrame::ENTRY_FRAME:
    _pc = StubRoutines::call_stub_return_pc();
    _cb = NULL;
    break;

  case ZeroFrame::INTERPRETER_FRAME:
    _pc = NULL;
    _cb = NULL;
    break;

  case ZeroFrame::SHARK_FRAME:
    _pc = zero_sharkframe()->pc();
    _cb = CodeCache::find_blob_unsafe(pc());
    break;

  case ZeroFrame::FAKE_STUB_FRAME:
    _pc = NULL;
    _cb = NULL;
    break;

  default:
    ShouldNotReachHere();
  }
  _deopt_state = not_deoptimized;
}

// Accessors

inline intptr_t* frame::sender_sp() const {
  return (intptr_t *) zeroframe()->next();
}

inline intptr_t* frame::link() const {
  ShouldNotCallThis();
}

#ifdef CC_INTERP
inline interpreterState frame::get_interpreterState() const {
  return zero_interpreterframe()->interpreter_state();
}

inline intptr_t** frame::interpreter_frame_locals_addr() const {
  return &(get_interpreterState()->_locals);
}

inline intptr_t* frame::interpreter_frame_bcx_addr() const {
  return (intptr_t*) &(get_interpreterState()->_bcp);
}

inline constantPoolCacheOop* frame::interpreter_frame_cache_addr() const {
  return &(get_interpreterState()->_constants);
}

inline methodOop* frame::interpreter_frame_method_addr() const {
  return &(get_interpreterState()->_method);
}

inline intptr_t* frame::interpreter_frame_mdx_addr() const {
  return (intptr_t*) &(get_interpreterState()->_mdx);
}

inline intptr_t* frame::interpreter_frame_tos_address() const {
  return get_interpreterState()->_stack + 1;
}
#endif // CC_INTERP

inline int frame::interpreter_frame_monitor_size() {
  return BasicObjectLock::size();
}

inline intptr_t* frame::interpreter_frame_expression_stack() const {
  intptr_t* monitor_end = (intptr_t*) interpreter_frame_monitor_end();
  return monitor_end - 1;
}

inline jint frame::interpreter_frame_expression_stack_direction() {
  return -1;
}

// Return a unique id for this frame. The id must have a value where
// we can distinguish identity and younger/older relationship. NULL
// represents an invalid (incomparable) frame.
inline intptr_t* frame::id() const {
  return sp();
}

inline JavaCallWrapper* frame::entry_frame_call_wrapper() const {
  return zero_entryframe()->call_wrapper();
}

inline void frame::set_saved_oop_result(RegisterMap* map, oop obj) {
  ShouldNotCallThis();
}

inline oop frame::saved_oop_result(RegisterMap* map) const {
  ShouldNotCallThis();
}

inline bool frame::is_older(intptr_t* id) const {
  ShouldNotCallThis();
}

inline intptr_t* frame::entry_frame_argument_at(int offset) const {
  ShouldNotCallThis();
}

inline intptr_t* frame::unextended_sp() const {
  if (zeroframe()->is_shark_frame())
    return zero_sharkframe()->unextended_sp();
  else
    return (intptr_t *) -1;
}

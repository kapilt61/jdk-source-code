/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
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

class StubAssembler;

// The Runtime1 holds all assembly stubs and VM
// runtime routines needed by code code generated
// by the Compiler1.

#define RUNTIME1_STUBS(stub, last_entry) \
  stub(dtrace_object_alloc)          \
  stub(unwind_exception)             \
  stub(forward_exception)            \
  stub(throw_range_check_failed)       /* throws ArrayIndexOutOfBoundsException */ \
  stub(throw_index_exception)          /* throws IndexOutOfBoundsException */ \
  stub(throw_div0_exception)         \
  stub(throw_null_pointer_exception) \
  stub(register_finalizer)           \
  stub(new_instance)                 \
  stub(fast_new_instance)            \
  stub(fast_new_instance_init_check) \
  stub(new_type_array)               \
  stub(new_object_array)             \
  stub(new_multi_array)              \
  stub(handle_exception_nofpu)         /* optimized version that does not preserve fpu registers */ \
  stub(handle_exception)             \
  stub(throw_array_store_exception)  \
  stub(throw_class_cast_exception)   \
  stub(throw_incompatible_class_change_error)   \
  stub(slow_subtype_check)           \
  stub(monitorenter)                 \
  stub(monitorenter_nofpu)             /* optimized version that does not preserve fpu registers */ \
  stub(monitorexit)                  \
  stub(monitorexit_nofpu)              /* optimized version that does not preserve fpu registers */ \
  stub(access_field_patching)        \
  stub(load_klass_patching)          \
  stub(jvmti_exception_throw)        \
  stub(g1_pre_barrier_slow)          \
  stub(g1_post_barrier_slow)         \
  stub(fpu2long_stub)                \
  stub(counter_overflow)             \
  last_entry(number_of_ids)

#define DECLARE_STUB_ID(x)       x ## _id ,
#define DECLARE_LAST_STUB_ID(x)  x
#define STUB_NAME(x)             #x " Runtime1 stub",
#define LAST_STUB_NAME(x)        #x " Runtime1 stub"

class Runtime1: public AllStatic {
  friend class VMStructs;
  friend class ArrayCopyStub;
 private:
  static int desired_max_code_buffer_size() {
    return (int) NMethodSizeLimit;  // default 256K or 512K
  }
  static int desired_max_constant_size() {
    return (int) NMethodSizeLimit / 10;  // about 25K
  }

  // Note: This buffers is allocated once at startup since allocation
  // for each compilation seems to be too expensive (at least on Intel
  // win32).
  static BufferBlob* _buffer_blob;

 public:
  enum StubID {
    RUNTIME1_STUBS(DECLARE_STUB_ID, DECLARE_LAST_STUB_ID)
  };

  // statistics
#ifndef PRODUCT
  static int _resolve_invoke_cnt;
  static int _handle_wrong_method_cnt;
  static int _ic_miss_cnt;
  static int _generic_arraycopy_cnt;
  static int _primitive_arraycopy_cnt;
  static int _oop_arraycopy_cnt;
  static int _arraycopy_slowcase_cnt;
  static int _new_type_array_slowcase_cnt;
  static int _new_object_array_slowcase_cnt;
  static int _new_instance_slowcase_cnt;
  static int _new_multi_array_slowcase_cnt;
  static int _monitorenter_slowcase_cnt;
  static int _monitorexit_slowcase_cnt;
  static int _patch_code_slowcase_cnt;
  static int _throw_range_check_exception_count;
  static int _throw_index_exception_count;
  static int _throw_div0_exception_count;
  static int _throw_null_pointer_exception_count;
  static int _throw_class_cast_exception_count;
  static int _throw_incompatible_class_change_error_count;
  static int _throw_array_store_exception_count;
  static int _throw_count;
#endif

 private:
  static bool      _is_initialized;
  static CodeBlob* _blobs[number_of_ids];
  static const char* _blob_names[];

  // stub generation
  static void generate_blob_for(StubID id);
  static OopMapSet* generate_code_for(StubID id, StubAssembler* masm);
  static OopMapSet* generate_exception_throw(StubAssembler* sasm, address target, bool has_argument);
  static void generate_handle_exception(StubAssembler *sasm, OopMapSet* oop_maps, OopMap* oop_map, bool ignore_fpu_registers = false);
  static void generate_unwind_exception(StubAssembler *sasm);
  static OopMapSet* generate_patching(StubAssembler* sasm, address target);

  static OopMapSet* generate_stub_call(StubAssembler* sasm, Register result, address entry,
                                       Register arg1 = noreg, Register arg2 = noreg, Register arg3 = noreg);

  // runtime entry points
  static void new_instance    (JavaThread* thread, klassOopDesc* klass);
  static void new_type_array  (JavaThread* thread, klassOopDesc* klass, jint length);
  static void new_object_array(JavaThread* thread, klassOopDesc* klass, jint length);
  static void new_multi_array (JavaThread* thread, klassOopDesc* klass, int rank, jint* dims);

#ifdef TIERED
  static void counter_overflow(JavaThread* thread, int bci);
#endif // TIERED

  static void unimplemented_entry   (JavaThread* thread, StubID id);

  static address exception_handler_for_pc(JavaThread* thread);
  static void post_jvmti_exception_throw(JavaThread* thread);

  static void throw_range_check_exception(JavaThread* thread, int index);
  static void throw_index_exception(JavaThread* thread, int index);
  static void throw_div0_exception(JavaThread* thread);
  static void throw_null_pointer_exception(JavaThread* thread);
  static void throw_class_cast_exception(JavaThread* thread, oopDesc* obect);
  static void throw_incompatible_class_change_error(JavaThread* thread);
  static void throw_array_store_exception(JavaThread* thread);

  static void monitorenter(JavaThread* thread, oopDesc* obj, BasicObjectLock* lock);
  static void monitorexit (JavaThread* thread, BasicObjectLock* lock);

  static int access_field_patching(JavaThread* thread);
  static int move_klass_patching(JavaThread* thread);

  static void patch_code(JavaThread* thread, StubID stub_id);

 public:
  static BufferBlob* get_buffer_blob();
  static void setup_code_buffer(CodeBuffer* cb, int call_stub_estimate);

  // initialization
  static bool is_initialized()                   { return _is_initialized; }
  static void initialize();
  static void initialize_pd();

  // stubs
  static CodeBlob* blob_for (StubID id);
  static address   entry_for(StubID id)          { return blob_for(id)->instructions_begin(); }
  static const char* name_for (StubID id);
  static const char* name_for_address(address entry);

  // method tracing
  static void trace_block_entry(jint block_id);

#ifndef PRODUCT
  static address throw_count_address()       { return (address)&_throw_count;       }
#endif

  // directly accessible leaf routine
  static int  arraycopy(oopDesc* src, int src_pos, oopDesc* dst, int dst_pos, int length);
  static void primitive_arraycopy(HeapWord* src, HeapWord* dst, int length);
  static void oop_arraycopy(HeapWord* src, HeapWord* dst, int length);

  static void print_statistics()                 PRODUCT_RETURN;
};

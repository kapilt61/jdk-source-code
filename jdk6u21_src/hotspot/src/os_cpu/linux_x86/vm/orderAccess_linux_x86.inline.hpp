/*
 * Copyright (c) 2003, 2009, Oracle and/or its affiliates. All rights reserved.
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

// Implementation of class OrderAccess.

inline void OrderAccess::loadload()   { acquire(); }
inline void OrderAccess::storestore() { release(); }
inline void OrderAccess::loadstore()  { acquire(); }
inline void OrderAccess::storeload()  { fence(); }

inline void OrderAccess::acquire() {
  volatile intptr_t dummy;
#ifdef AMD64
  __asm__ volatile ("movq 0(%%rsp), %0" : "=r" (dummy) : : "memory");
#else
  __asm__ volatile ("movl 0(%%esp),%0" : "=r" (dummy) : : "memory");
#endif // AMD64
}

inline void OrderAccess::release() {
  dummy = 0;
}

inline void OrderAccess::fence() {
  if (os::is_MP()) {
    // always use locked addl since mfence is sometimes expensive
#ifdef AMD64
    __asm__ volatile ("lock; addl $0,0(%%rsp)" : : : "cc", "memory");
#else
    __asm__ volatile ("lock; addl $0,0(%%esp)" : : : "cc", "memory");
#endif
  }
}

inline jbyte    OrderAccess::load_acquire(volatile jbyte*   p) { return *p; }
inline jshort   OrderAccess::load_acquire(volatile jshort*  p) { return *p; }
inline jint     OrderAccess::load_acquire(volatile jint*    p) { return *p; }
inline jlong    OrderAccess::load_acquire(volatile jlong*   p) { return *p; }
inline jubyte   OrderAccess::load_acquire(volatile jubyte*  p) { return *p; }
inline jushort  OrderAccess::load_acquire(volatile jushort* p) { return *p; }
inline juint    OrderAccess::load_acquire(volatile juint*   p) { return *p; }
inline julong   OrderAccess::load_acquire(volatile julong*  p) { return *p; }
inline jfloat   OrderAccess::load_acquire(volatile jfloat*  p) { return *p; }
inline jdouble  OrderAccess::load_acquire(volatile jdouble* p) { return *p; }

inline intptr_t OrderAccess::load_ptr_acquire(volatile intptr_t*   p) { return *p; }
inline void*    OrderAccess::load_ptr_acquire(volatile void*       p) { return *(void* volatile *)p; }
inline void*    OrderAccess::load_ptr_acquire(const volatile void* p) { return *(void* const volatile *)p; }

inline void     OrderAccess::release_store(volatile jbyte*   p, jbyte   v) { *p = v; }
inline void     OrderAccess::release_store(volatile jshort*  p, jshort  v) { *p = v; }
inline void     OrderAccess::release_store(volatile jint*    p, jint    v) { *p = v; }
inline void     OrderAccess::release_store(volatile jlong*   p, jlong   v) { *p = v; }
inline void     OrderAccess::release_store(volatile jubyte*  p, jubyte  v) { *p = v; }
inline void     OrderAccess::release_store(volatile jushort* p, jushort v) { *p = v; }
inline void     OrderAccess::release_store(volatile juint*   p, juint   v) { *p = v; }
inline void     OrderAccess::release_store(volatile julong*  p, julong  v) { *p = v; }
inline void     OrderAccess::release_store(volatile jfloat*  p, jfloat  v) { *p = v; }
inline void     OrderAccess::release_store(volatile jdouble* p, jdouble v) { *p = v; }

inline void     OrderAccess::release_store_ptr(volatile intptr_t* p, intptr_t v) { *p = v; }
inline void     OrderAccess::release_store_ptr(volatile void*     p, void*    v) { *(void* volatile *)p = v; }

inline void     OrderAccess::store_fence(jbyte*  p, jbyte  v) {
  __asm__ volatile (  "xchgb (%2),%0"
                    : "=r" (v)
                    : "0" (v), "r" (p)
                    : "memory");
}
inline void     OrderAccess::store_fence(jshort* p, jshort v) {
  __asm__ volatile (  "xchgw (%2),%0"
                    : "=r" (v)
                    : "0" (v), "r" (p)
                    : "memory");
}
inline void     OrderAccess::store_fence(jint*   p, jint   v) {
  __asm__ volatile (  "xchgl (%2),%0"
                    : "=r" (v)
                    : "0" (v), "r" (p)
                    : "memory");
}

inline void     OrderAccess::store_fence(jlong*   p, jlong   v) {
#ifdef AMD64
  __asm__ __volatile__ ("xchgq (%2), %0"
                        : "=r" (v)
                        : "0" (v), "r" (p)
                        : "memory");
#else
  *p = v; fence();
#endif // AMD64
}

// AMD64 copied the bodies for the the signed version. 32bit did this. As long as the
// compiler does the inlining this is simpler.
inline void     OrderAccess::store_fence(jubyte*  p, jubyte  v) { store_fence((jbyte*)p,  (jbyte)v);  }
inline void     OrderAccess::store_fence(jushort* p, jushort v) { store_fence((jshort*)p, (jshort)v); }
inline void     OrderAccess::store_fence(juint*   p, juint   v) { store_fence((jint*)p,   (jint)v);   }
inline void     OrderAccess::store_fence(julong*  p, julong  v) { store_fence((jlong*)p,  (jlong)v);  }
inline void     OrderAccess::store_fence(jfloat*  p, jfloat  v) { *p = v; fence(); }
inline void     OrderAccess::store_fence(jdouble* p, jdouble v) { *p = v; fence(); }

inline void     OrderAccess::store_ptr_fence(intptr_t* p, intptr_t v) {
#ifdef AMD64
  __asm__ __volatile__ ("xchgq (%2), %0"
                        : "=r" (v)
                        : "0" (v), "r" (p)
                        : "memory");
#else
  store_fence((jint*)p, (jint)v);
#endif // AMD64
}

inline void     OrderAccess::store_ptr_fence(void**    p, void*    v) {
#ifdef AMD64
  __asm__ __volatile__ ("xchgq (%2), %0"
                        : "=r" (v)
                        : "0" (v), "r" (p)
                        : "memory");
#else
  store_fence((jint*)p, (jint)v);
#endif // AMD64
}

// Must duplicate definitions instead of calling store_fence because we don't want to cast away volatile.
inline void     OrderAccess::release_store_fence(volatile jbyte*  p, jbyte  v) {
  __asm__ volatile (  "xchgb (%2),%0"
                    : "=r" (v)
                    : "0" (v), "r" (p)
                    : "memory");
}
inline void     OrderAccess::release_store_fence(volatile jshort* p, jshort v) {
  __asm__ volatile (  "xchgw (%2),%0"
                    : "=r" (v)
                    : "0" (v), "r" (p)
                    : "memory");
}
inline void     OrderAccess::release_store_fence(volatile jint*   p, jint   v) {
  __asm__ volatile (  "xchgl (%2),%0"
                    : "=r" (v)
                    : "0" (v), "r" (p)
                    : "memory");
}

inline void     OrderAccess::release_store_fence(volatile jlong*   p, jlong   v) {
#ifdef AMD64
  __asm__ __volatile__ (  "xchgq (%2), %0"
                          : "=r" (v)
                          : "0" (v), "r" (p)
                          : "memory");
#else
  *p = v; fence();
#endif // AMD64
}

inline void     OrderAccess::release_store_fence(volatile jubyte*  p, jubyte  v) { release_store_fence((volatile jbyte*)p,  (jbyte)v);  }
inline void     OrderAccess::release_store_fence(volatile jushort* p, jushort v) { release_store_fence((volatile jshort*)p, (jshort)v); }
inline void     OrderAccess::release_store_fence(volatile juint*   p, juint   v) { release_store_fence((volatile jint*)p,   (jint)v);   }
inline void     OrderAccess::release_store_fence(volatile julong*  p, julong  v) { release_store_fence((volatile jlong*)p,  (jlong)v);  }

inline void     OrderAccess::release_store_fence(volatile jfloat*  p, jfloat  v) { *p = v; fence(); }
inline void     OrderAccess::release_store_fence(volatile jdouble* p, jdouble v) { *p = v; fence(); }

inline void     OrderAccess::release_store_ptr_fence(volatile intptr_t* p, intptr_t v) {
#ifdef AMD64
  __asm__ __volatile__ (  "xchgq (%2), %0"
                          : "=r" (v)
                          : "0" (v), "r" (p)
                          : "memory");
#else
  release_store_fence((volatile jint*)p, (jint)v);
#endif // AMD64
}
inline void     OrderAccess::release_store_ptr_fence(volatile void*     p, void*    v) {
#ifdef AMD64
  __asm__ __volatile__ (  "xchgq (%2), %0"
                          : "=r" (v)
                          : "0" (v), "r" (p)
                          : "memory");
#else
  release_store_fence((volatile jint*)p, (jint)v);
#endif // AMD64
}

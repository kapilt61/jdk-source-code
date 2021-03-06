/*
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
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

// Should this header be preserved during GC?
inline bool markOopDesc::must_be_preserved_with_bias(oop obj_containing_mark) const {
  assert(UseBiasedLocking, "unexpected");
  if (has_bias_pattern()) {
    // Will reset bias at end of collection
    // Mark words of biased and currently locked objects are preserved separately
    return false;
  }
  markOop prototype_header = prototype_for_object(obj_containing_mark);
  if (prototype_header->has_bias_pattern()) {
    // Individual instance which has its bias revoked; must return
    // true for correctness
    return true;
  }
  return (!is_unlocked() || !has_no_hash());
}

inline bool markOopDesc::must_be_preserved(oop obj_containing_mark) const {
  if (!UseBiasedLocking)
    return (!is_unlocked() || !has_no_hash());
  return must_be_preserved_with_bias(obj_containing_mark);
}

// Should this header (including its age bits) be preserved in the
// case of a promotion failure during scavenge?
inline bool markOopDesc::must_be_preserved_with_bias_for_promotion_failure(oop obj_containing_mark) const {
  assert(UseBiasedLocking, "unexpected");
  // We don't explicitly save off the mark words of biased and
  // currently-locked objects during scavenges, so if during a
  // promotion failure we encounter either a biased mark word or a
  // klass which still has a biasable prototype header, we have to
  // preserve the mark word. This results in oversaving, but promotion
  // failures are rare, and this avoids adding more complex logic to
  // the scavengers to call new variants of
  // BiasedLocking::preserve_marks() / restore_marks() in the middle
  // of a scavenge when a promotion failure has first been detected.
  if (has_bias_pattern() ||
      prototype_for_object(obj_containing_mark)->has_bias_pattern()) {
    return true;
  }
  return (this != prototype());
}

inline bool markOopDesc::must_be_preserved_for_promotion_failure(oop obj_containing_mark) const {
  if (!UseBiasedLocking)
    return (this != prototype());
  return must_be_preserved_with_bias_for_promotion_failure(obj_containing_mark);
}


// Should this header (including its age bits) be preserved in the
// case of a scavenge in which CMS is the old generation?
inline bool markOopDesc::must_be_preserved_with_bias_for_cms_scavenge(klassOop klass_of_obj_containing_mark) const {
  assert(UseBiasedLocking, "unexpected");
  // CMS scavenges preserve mark words in similar fashion to promotion failures; see above
  if (has_bias_pattern() ||
      klass_of_obj_containing_mark->klass_part()->prototype_header()->has_bias_pattern()) {
    return true;
  }
  return (this != prototype());
}
inline bool markOopDesc::must_be_preserved_for_cms_scavenge(klassOop klass_of_obj_containing_mark) const {
  if (!UseBiasedLocking)
    return (this != prototype());
  return must_be_preserved_with_bias_for_cms_scavenge(klass_of_obj_containing_mark);
}

inline markOop markOopDesc::prototype_for_object(oop obj) {
#ifdef ASSERT
  markOop prototype_header = obj->klass()->klass_part()->prototype_header();
  assert(prototype_header == prototype() || prototype_header->has_bias_pattern(), "corrupt prototype header");
#endif
  return obj->klass()->klass_part()->prototype_header();
}

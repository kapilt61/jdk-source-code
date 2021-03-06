/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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

class Matcher;
class ProjNode;

//------------------------------MultiNode--------------------------------------
// This class defines a MultiNode, a Node which produces many values.  The
// values are wrapped up in a tuple Type, i.e. a TypeTuple.
class MultiNode : public Node {
public:
  MultiNode( uint required ) : Node(required) {
    init_class_id(Class_Multi);
  }
  virtual int Opcode() const;
  virtual const Type *bottom_type() const = 0;
  virtual bool       is_CFG() const { return true; }
  virtual uint hash() const { return NO_HASH; }  // CFG nodes do not hash
  virtual bool depends_only_on_test() const { return false; }
  virtual const RegMask &out_RegMask() const;
  virtual Node *match( const ProjNode *proj, const Matcher *m );
  virtual uint ideal_reg() const { return NotAMachineReg; }
  ProjNode* proj_out(uint which_proj) const; // Get a named projection

};

//------------------------------ProjNode---------------------------------------
// This class defines a Projection node.  Projections project a single element
// out of a tuple (or Signature) type.  Only MultiNodes produce TypeTuple
// results.
class ProjNode : public Node {
protected:
  virtual uint hash() const;
  virtual uint cmp( const Node &n ) const;
  virtual uint size_of() const;
  void check_con() const;       // Called from constructor.

public:
  ProjNode( Node *src, uint con, bool io_use = false )
    : Node( src ), _con(con), _is_io_use(io_use)
  {
    init_class_id(Class_Proj);
    // Optimistic setting. Need additional checks in Node::is_dead_loop_safe().
    if (con != TypeFunc::Memory || src->is_Start())
      init_flags(Flag_is_dead_loop_safe);
    debug_only(check_con());
  }
  const uint _con;              // The field in the tuple we are projecting
  const bool _is_io_use;        // Used to distinguish between the projections
                                // used on the control and io paths from a macro node
  virtual int Opcode() const;
  virtual bool      is_CFG() const;
  virtual bool depends_only_on_test() const { return false; }
  virtual const Type *bottom_type() const;
  virtual const TypePtr *adr_type() const;
  virtual bool pinned() const;
  virtual const Type *Value( PhaseTransform *phase ) const;
  virtual uint ideal_reg() const;
  virtual const RegMask &out_RegMask() const;
#ifndef PRODUCT
  virtual void dump_spec(outputStream *st) const;
#endif
};

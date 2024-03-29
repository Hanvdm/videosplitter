/**
 * 
 */
package com.googlecode.videosplitter.mpeg.atom;

/**
 * The user data atom.
 */
public class UdtaAtom extends LeafAtom {

  /**
   * Constructor for a user-data atom.
   */
  public UdtaAtom() {
    super(new byte[]{'u','d','t','a'});
  }
  
  /**
   * Copy constructor.  Perform deep copy.
   * @param old the version to copy
   */
  public UdtaAtom(UdtaAtom old) {
    super(old);
  }
  
  /**
   * Cut the udta atom.  Nothing changes for this atom so just create a copy.
   * @return a copy of the udta atom
   */
  public UdtaAtom cut() {
    return new UdtaAtom(this);
  }

  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
}
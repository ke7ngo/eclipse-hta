// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Pairing Elements
// 
// Synopsis:
//  Huppaal 
// /////////////////////////////////////////////////////////////
// @FILE:    ElementElementPair.java
// @PLACE:   Uppsala; host:eniac
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Fri Feb 16 14:07:34 2001
// @VERSION: Fri Feb 16 14:10:10 2001
// /////////////////////////////////////////////////////////////
// 


import java.lang.*;

import org.w3c.dom.Element;

//**** from other packages 

//****************************************

/**
 * Wrapper Class for Pair of Elements
 *
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=ElementElementPair.java%20(Mon%20Nov%2020%2011:40:18%202000)">M. Oliver M&ouml;ller</A>
 * @version Fri Feb 16 14:10:10 2001
 */
public class ElementElementPair  {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * First of pair
   */
  public Element first;

  /**
   * Second of pair
   */
  public Element second;
  
  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * The only constructor.
   */
  public ElementElementPair(Element theFirst, Element theSecond){
    first = theFirst;
    second = theSecond;
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////

}
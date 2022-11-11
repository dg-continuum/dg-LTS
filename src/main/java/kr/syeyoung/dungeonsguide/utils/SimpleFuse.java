package kr.syeyoung.dungeonsguide.utils;

/**
 * This class represents a simple boolean fuse
 * equivalent to
 * <pre>{@code
 *  boolean fuse;
 *  public onClick(){
 *      if(!fuse){
 *          fuse = true;
 *          // do work that should only be done once
 *      }
 *  }
 * }</pre>
 * this is a utility class for code clarity <br/>
 * @author Eryk Ruta
 */
public class SimpleFuse {
    boolean state = false;

    public boolean isBlown(){
        return state;
    }

    public void blow(){
        state = true;
    }

}

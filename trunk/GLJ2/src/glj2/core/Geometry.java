package glj2.core;

import java.io.Serializable;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public abstract interface Geometry extends Serializable {
	
	public abstract void render();
	
	public abstract void destroy();
	
}
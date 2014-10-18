package glj2.core;

import java.io.Serializable;

import javax.vecmath.Matrix4f;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public abstract interface Geometry extends Serializable {
	
	public abstract Matrix4f getPosition();
	
	public abstract void render();
	
	public abstract void destroy();
	
}

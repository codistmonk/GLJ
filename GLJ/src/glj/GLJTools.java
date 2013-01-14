package glj;

import glj.Scene.Camera;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2013-01-12)
 */
public final class GLJTools {
	
	private GLJTools() {
		throw new IllegalInstantiationException();
	}
	
	public static final Point3f ORIGIN = new Point3f();
	
	public static final Vector3f ZERO = new Vector3f();
	
	public static final Vector3f UNIT_X = new Vector3f(+1.0F, +0.0F, +0.0F);
	
	public static final Vector3f UNIT_Y = new Vector3f(+0.0F, +1.0F, +0.0F);
	
	public static final Vector3f UNIT_Z = new Vector3f(+0.0F, +0.0F, +1.0F);
	
	/**
	 * {@value}.
	 */
	public static final int FLOAT_BYTE_COUNT = Float.SIZE / 8;
	
	/**
	 * {@value}.
	 */
	public static final int INT_BYTE_COUNT = Integer.SIZE / 8;
	
	/**
	 * @param camera
	 * <br>Not null
	 * @return
	 * <br>Maybe null
	 * <br>New
	 */
	public static Vector3f getCameraDirection(final Camera camera) {
		final Vector3f result = new Vector3f(getCameraLocation(camera));
		
		result.normalize();
		
		return result;
	}
	
	/**
	 * @param camera
	 * <br>Not null
	 * @return
	 * <br>Maybe null
	 * <br>New
	 */
	public static Point3f getCameraLocation(final Camera camera) {
		final Point4f p = new Point4f(+0.0F, +0.0F, +0.0F, +1.0F);
		final Matrix4f m = new Matrix4f();
		
		m.set(camera.getPosition());
		m.invert();
		m.transform(p);
		
		if (p.w == 0.0F) {
			return null;
		}
		
		return new Point3f(p.x / p.w, p.y / p.w, p.z / p.w);
	}
	
	/**
	 * @param matrix
	 * <br>Not null
	 * <br>Input-output
	 * @param left
	 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
	 * @param right
	 * <br>Range: <code>]left .. Float.MAX_VALUE]</code>
	 * @param bottom
	 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
	 * @param top
	 * <br>Range: <code>]bottom .. Float.MAX_VALUE]</code>
	 * @param zNear
	 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
	 * @param zFar
	 * <br>Range: <code>]zNear .. Float.MAX_VALUE]</code>
	 */
	public static final void setOrthographicProjectionMatrix(final Matrix4f matrix, final float left, final float right, final float bottom, final float top, final float zNear, final float zFar) {
		matrix.setIdentity();
		
		matrix.m00 = +2.0F / (right - left);
		matrix.m11 = +2.0F / (top - bottom);
		matrix.m22 = -2.0F / (zFar - zNear);
		
		matrix.m03 = - (right + left) / (right - left);
		matrix.m13 = - (top + bottom) / (top - bottom);
		matrix.m23 = - (zFar + zNear) / (zFar - zNear);
	}
	
	/**
	 * @param matrix
	 * <br>Not null
	 * <br>Input-output
	 * @param verticalFieldOfVisionInRadians
	 * <br>Range: <code>]+0.0F .. +Math.PI[</code> 
	 * @param viewportAspectRatio
	 * <br>Range: <code>]+0.0F .. +Float.MAX_VALUE]</code> 
	 * @param zNear
	 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
	 * @param zFar
	 * <br>Range: <code>]zNear .. Float.MAX_VALUE]</code>
	 */
	public static final void setPerspectiveProjectionMatrix(final Matrix4f matrix, final float verticalFieldOfVisionInRadians, final float viewportAspectRatio, final float zNear, final float zFar) {
		final float range = (float) (Math.tan(verticalFieldOfVisionInRadians / 2.0F) * zNear);	
		final float left = -range * viewportAspectRatio;
		final float right = +range * viewportAspectRatio;
		final float bottom = -range;
		final float top = +range;
		
		matrix.setZero();
		
		matrix.m00 = +2.0F * zNear / (right - left);
		matrix.m11 = +2.0F * zNear / (top - bottom);
		matrix.m22 = - (zFar + zNear) / (zFar - zNear);
		
		matrix.m32 = -1.0F;
		matrix.m23 = -2.0F * zFar * zNear / (zFar - zNear);
	}
	
	/**
	 * @param matrix
	 * <br>Not null
	 * <br>Input-output
	 * @param eye
	 * <br>Not null
	 * @param center
	 * <br>Not null
	 * @param up
	 * <br>Not null
	 */
	public static final void setLookAtMatrix(final Matrix4f matrix, final Tuple3f eye, final Tuple3f center, final Vector3f up) {
		final Vector3f f = new Vector3f();
		final Vector3f u = new Vector3f();
		final Vector3f s = new Vector3f();
		
		f.sub(center, eye);
		f.normalize();
		
		u.normalize(up);
		
		s.cross(f, u);
		s.normalize();
		
		u.cross(s, f);
		
		matrix.setIdentity();
		
		matrix.m00 = s.x;
		matrix.m01 = s.y;
		matrix.m02 = s.z;
		
		matrix.m10 = u.x;
		matrix.m11 = u.y;
		matrix.m12 = u.z;
		
		matrix.m20 = -f.x;
		matrix.m21 = -f.y;
		matrix.m22 = -f.z;
		
		f.negate(eye);
		
		localTranslate(matrix, f);
	}
	
	/**
	 * @param matrix
	 * <br>Not null
	 * <br>Input-output
	 * @param vector
	 * <br>Not null
	 */
	public static final void localTranslate(final Matrix4f matrix, final Vector3f vector) {
		final Matrix4f localTransform = new Matrix4f();
		
		localTransform.setIdentity();
		localTransform.setTranslation(vector);
		
		matrix.mul(localTransform);
	}
	
}

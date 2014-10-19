package glj2.core;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.vecmath.Point3f;

/**
 * @author codistmonk (creation 2014-10-17)
 */
public final class Orbiter extends MouseHandler {
	
	private final Scene scene;
	
	private Point mouse;
	
	private final Point3f target;
	
	private float distance;
	
	private double horizontalRadians;
	
	private double verticalRadians;
	
	private float clippingDepth;
	
	public Orbiter(final Scene scene) {
		super(scene.getUpdatedNeeded());
		this.scene = scene;
		this.target = new Point3f();
		this.distance = 4F;
		this.clippingDepth = 1F;
	}
	
	public final float getDistance() {
		return this.distance;
	}
	
	public final void setDistance(final float distance) {
		this.distance = Math.max(this.clippingDepth * 0.5001F, distance);
	}
	
	public final double getHorizontalRadians() {
		return this.horizontalRadians;
	}
	
	public final void setHorizontalRadians(final double horizontalRadians) {
		this.horizontalRadians = horizontalRadians;
	}
	
	public final double getVerticalRadians() {
		return this.verticalRadians;
	}
	
	public final void setVerticalRadians(final double verticalRadians) {
		this.verticalRadians = verticalRadians;
	}
	
	public final Point3f getTarget() {
		return this.target;
	}
	
	public final float getFieldDepth() {
		return this.clippingDepth;
	}
	
	public final void setClippingDepth(final float clippingDepth) {
		this.clippingDepth = clippingDepth;
	}
	
	@Override
	public final void mouseDragged(final MouseEvent event) {
		if (this.mouse != null) {
			final int x = event.getX();
			final int y = event.getY();
			final double delta = PI / 64.0;
			this.horizontalRadians -= (x - this.mouse.x) * delta;
			this.verticalRadians += (y - this.mouse.y) * delta;
			this.verticalRadians = min(max(-PI / 2.0 + delta, this.verticalRadians), PI / 2.0 - delta);
			
			this.mouse.setLocation(x, y);
			
			this.updateSceneCamera();
		} else {
			this.mouse = event.getPoint();
		}
	}
	
	@Override
	public final void mouseReleased(final MouseEvent event) {
		this.mouse = null;
	}
	
	@Override
	public final void mouseWheelMoved(final MouseWheelEvent event) {
		if (event.getWheelRotation() < 0) {
			this.setDistance(this.getDistance() * 0.8F);
		} else {
			this.setDistance(this.getDistance() * 1.2F);
		}
		
		this.updateSceneCamera();
	}
	
	public final void updateSceneCamera() {
		final Camera camera = this.scene.getCamera();
		final float distance = this.getDistance();
		
		{
			final float[] clipping = camera.getClipping();
			final float oldNear = clipping[4];
			final float near = distance - this.clippingDepth / 2F;
			
			for (int i = 0; i <= 4; ++i) {
				clipping[i] *= near / oldNear;
			}
			
			clipping[5] = near + this.clippingDepth;
			
			camera.setProjection();
		}
		
		camera.setView(new Point3f(
				this.target.x + (float) (distance * cos(this.verticalRadians) * sin(this.horizontalRadians)),
				this.target.y + (float) (distance * sin(this.verticalRadians)),
				this.target.z + (float) (distance * cos(this.verticalRadians) * cos(this.horizontalRadians))
				), this.target, GLJTools.UNIT_Y);
		
		this.getUpdateNeeded().set(true);
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 8588231683123271508L;
	
}
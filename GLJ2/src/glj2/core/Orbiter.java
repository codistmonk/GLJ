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
	
	private final Point3f target = new Point3f();
	
	private double distance = 3F;
	
	private double horizontalRadians;
	
	private double verticalRadians;
	
	public Orbiter(final Scene scene) {
		super(null);
		this.scene = scene;
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
			this.distance *= 0.8;
		} else {
			this.distance *= 1.2;
		}
		
		this.updateSceneCamera();
	}
	
	public final void updateSceneCamera() {
		this.scene.getCamera().setView(new Point3f(
				this.target.x + (float) (this.distance * cos(this.verticalRadians) * sin(this.horizontalRadians)),
				this.target.y + (float) (this.distance * sin(this.verticalRadians)),
				this.target.z + (float) (this.distance * cos(this.verticalRadians) * cos(this.horizontalRadians))
				), this.target, GLJTools.UNIT_Y);
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 8588231683123271508L;
	
}
package glj2.std;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.Predicate;

import javax.vecmath.Point3f;

import glj2.core.Camera;
import glj2.core.GLJTools;
import glj2.core.Scene;
import multij.swing.MouseHandler;
import multij.tools.Tools;

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
	
	private float clippingNear;
	
	private float clippingFar;
	
	private Predicate<MouseEvent> eventFilter;
	
	public Orbiter(final Scene scene) {
		super(scene.getUpdatedNeeded());
		this.scene = scene;
		this.target = new Point3f();
		this.distance = 4F;
		this.clippingNear = -0.5F;
		this.clippingFar = 0.5F;
		
		this.setEventFilter(e -> true);
	}
	
	public final float getDistance() {
		return this.distance;
	}
	
	public final void setDistance(final float distance) {
		this.distance = Math.max(0F, distance);
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
	
	public final float getClippingNear() {
		return this.clippingNear;
	}
	
	public final void setClippingNear(final float clippingNear) {
		this.clippingNear = clippingNear;
	}
	
	public final float getClippingFar() {
		return this.clippingFar;
	}
	
	public final void setClippingFar(final float clippingFar) {
		this.clippingFar = clippingFar;
	}
	
	public final float getClippingDepth() {
		return this.getClippingFar() - this.getClippingNear();
	}
	
	public final void setClippingDepth(final float clippingDepth) {
		this.setClippingNear(-clippingDepth / 2F);
		this.setClippingFar(clippingDepth / 2F);
		Tools.debugPrint(clippingDepth);
	}
	
	public final Predicate<MouseEvent> getEventFilter() {
		return this.eventFilter;
	}
	
	public final void setEventFilter(final Predicate<MouseEvent> eventFilter) {
		this.eventFilter = eventFilter;
	}
	
	@Override
	public final void mouseDragged(final MouseEvent event) {
		if (!this.getEventFilter().test(event)) {
			return;
		}
		
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
		if (!this.getEventFilter().test(event)) {
			return;
		}
		
		if (!event.isControlDown() && !event.isShiftDown() && !event.isAltDown() && !event.isAltGraphDown() && !event.isMetaDown()) {
			if (event.getWheelRotation() < 0) {
				this.setDistance(this.getDistance() * 0.8F);
			} else {
				this.setDistance(this.getDistance() * 1.2F);
			}
		}
		
		this.updateSceneCamera();
	}
	
	public final void updateSceneCamera() {
		final Camera camera = this.scene.getCamera();
		final float distance = this.getDistance();
		final float[] clipping = camera.getClipping();
		
		clipping[4] = Math.max(0.01F, distance + this.getClippingNear());
		clipping[5] = clipping[4] + this.getClippingDepth();
		
		{
			final float oldTop = clipping[3];
			final double viewVerticalRadians = PI / 6.0; // TODO allow angle customization
			final float newTop = (float) (clipping[4] * tan(viewVerticalRadians));
			
			for (int i = 0; i <= 3; ++i) {
				clipping[i] *= newTop / oldTop;
			}
		}
		
		camera.setProjection();
		
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
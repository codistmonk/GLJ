package glj2.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.jogamp.opengl.util.Animator;

/**
 * @author codistmonk (creation 2014-08-18)
 */
public abstract class Scene implements GLEventListener, Serializable {
	
	private Camera camera;
	
	private final Map<String, ExtendedShaderProgram> shaderPrograms = new LinkedHashMap<>();
	
	private final Map<String, Geometry> geometries = new LinkedHashMap<>();
	
	private final Map<ExtendedShaderProgram, List<Geometry>> renderingPool = new LinkedHashMap<>();
	
	private GL gl;
	
	public final Camera getCamera() {
		return this.camera;
	}
	
	public final ExtendedShaderProgram getShaderProgram(final String name) {
		return this.getShaderPrograms().get(name);
	}
	
	public final Geometry getGeometry(final String name) {
		return this.getGeometries().get(name);
	}
	
	public final Map<String, ExtendedShaderProgram> getShaderPrograms() {
		return this.shaderPrograms;
	}
	
	public final ExtendedShaderProgram add(final String name, final ExtendedShaderProgram shaderProgram) {
		this.getShaderPrograms().put(name, shaderProgram);
		
		return shaderProgram;
	}
	
	public final Map<String, Geometry> getGeometries() {
		return this.geometries;
	}
	
	public final Geometry add(final String name, final Geometry geometry) {
		this.getGeometries().put(name, geometry);
		
		return geometry;
	}
	
	public final Map<ExtendedShaderProgram, List<Geometry>> getRenderingPool() {
		return this.renderingPool;
	}
	
	public final Scene add(final ExtendedShaderProgram shaderProgram, final Geometry... geometries) {
		return this.add(shaderProgram, Arrays.asList(geometries));
	}
	
	public final Scene add(final ExtendedShaderProgram shaderProgram, final Collection<Geometry> geometries) {
		if (shaderProgram == null) {
			throw new NullPointerException();
		}
		
		this.getRenderingPool().compute(shaderProgram,
				(k, v) -> v != null ? v : new ArrayList<>()).addAll(geometries);
		
		return this;
	}
	
	public final Scene remove(final ExtendedShaderProgram shaderProgram, final Geometry... geometries) {
		return this.remove(shaderProgram, Arrays.asList(geometries));
	}
	
	public final Scene remove(final ExtendedShaderProgram shaderProgram, final Collection<Geometry> geometries) {
		if (shaderProgram == null) {
			throw new NullPointerException();
		}
		
		final List<Geometry> list = this.getRenderingPool().get(shaderProgram);
		
		if (list != null) {
			list.removeAll(Arrays.asList(geometries));
		}
		
		return this;
	}
	
	public final <T extends GL> GL getGL() {
		return this.gl;
	}
	
	@Override
	public final void init(final GLAutoDrawable drawable) {
		this.gl = drawable.getGL();
		this.camera = new Camera(this.getGL());
		
		this.initialize(drawable);
		
		drawable.setAnimator(new Animator(drawable));
		drawable.getAnimator().start();
	}
	
	@Override
	public final void dispose(final GLAutoDrawable drawable) {
		drawable.getAnimator().stop();
		
		for (final ExtendedShaderProgram shaderProgram : this.getShaderPrograms().values()) {
			shaderProgram.destroy();
		}
		
		this.getShaderPrograms().clear();
		
		for (final Geometry geometry : this.getGeometries().values()) {
			geometry.destroy();
		}
		
		this.getGeometries().clear();
	}
	
	@Override
	public final void reshape(final GLAutoDrawable drawable, final int x, final int y,
			final int width, final int height) {
		this.getCamera().setCanvasSize(width, height).updateGL();
		
		this.reshaped();
	}
	
	@Override
	public final void display(final GLAutoDrawable drawable) {
		this.beforeRender();
		this.render();
		this.afterRender();
	}
	
	protected void beforeRender() {
		this.getGL().glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	}
	
	protected void afterRender() {
		// NOP
	}
	
	protected void reshaped() {
		// NOP
	}
	
	protected void initialize(final GLAutoDrawable drawable) {
		// NOP
	}
	
	protected void render() {
		for (final Map.Entry<ExtendedShaderProgram, List<Geometry>> entry : this.getRenderingPool().entrySet()) {
			entry.getKey().useProgram(true);
			
			for (final Geometry geometry : entry.getValue()) {
				geometry.render();
			}
		}
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8853467479921949191L;
	
}
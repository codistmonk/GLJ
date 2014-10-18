package glj2.core;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.jogamp.opengl.util.Animator;

/**
 * @author codistmonk (creation 2014-08-18)
 */
public abstract class Scene implements GLEventListener, Serializable {
	
	private Camera camera;
	
	private final MatrixConverter projectionView = new MatrixConverter();
	
	private final Map<String, ExtendedShaderProgram> shaderPrograms = new LinkedHashMap<>();
	
	private final Map<String, Geometry> geometries = new LinkedHashMap<>();
	
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
	
	@SuppressWarnings("unchecked")
	public final <T extends GL> T getGL() {
		return (T) this.gl;
	}
	
	@Override
	public final void init(final GLAutoDrawable drawable) {
		drawable.setGL(new DebugGL4((GL4) drawable.getGL()));
		
		this.gl = drawable.getGL();
		this.camera = new Camera(this.getGL());
		
		{
			this.gl.glEnable(GL.GL_DEPTH_TEST);
			this.gl.glDepthFunc(GL.GL_LESS);
		}
		
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
	
	public final MatrixConverter getProjectionView() {
		return this.projectionView;
	}
	
	protected void initialize(final GLAutoDrawable drawable) {
		// NOP
	}
	
	protected void reshaped() {
		// NOP
	}
	
	protected void beforeRender() {
		this.getGL().glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		this.getCamera().getProjectionView(this.getProjectionView().getMatrix());
		
		this.getProjectionView().updateBuffer();
	}
	
	protected void render() {
		this.shaderPrograms.values().forEach(ExtendedShaderProgram::run);
	}
	
	protected void afterRender() {
		// NOP
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8853467479921949191L;
	
}
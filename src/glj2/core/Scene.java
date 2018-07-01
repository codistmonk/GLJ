package glj2.core;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;

import java.awt.Dimension;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import multij.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-18)
 */
public abstract class Scene implements GLEventListener, Serializable {
	
	private Camera camera;
	
	private final AtomicBoolean updatedNeeded = new AtomicBoolean();
	
	private final MatrixConverter projectionView = new MatrixConverter();
	
	private final Map<String, ExtendedShaderProgram> shaderPrograms = new LinkedHashMap<>();
	
	private final Map<String, ExtendedShaderProgram> selectionShaderPrograms = new LinkedHashMap<>();
	
	private final Map<String, Geometry> geometries = new LinkedHashMap<>();
	
	private final Map<String, Geometry> selectionGeometries = new LinkedHashMap<>();
	
	private GL gl;
	
	private int renderBuffersOutdated;
	
	private final IntBuffer selectionFrameBuffer = Buffers.newDirectIntBuffer(1);
	
	private final IntBuffer selectionRenderBuffer = Buffers.newDirectIntBuffer(1);
	
	private final IntBuffer selectionDepthBuffer = Buffers.newDirectIntBuffer(1);
	
	private ByteBuffer selectionData;
	
	private final int[] selectionId = { 1 };
	
	private final TreeMap<Integer, Object> selectableObjects = new TreeMap<>();
	
	public final int[] getSelectionId() {
		return this.selectionId;
	}
	
	public final TreeMap<Integer, Object> getSelectableObjects() {
		return this.selectableObjects;
	}
	
	public final AtomicBoolean getUpdatedNeeded() {
		return this.updatedNeeded;
	}
	
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
	
	public final Map<String, ExtendedShaderProgram> getSelectionShaderPrograms() {
		return this.selectionShaderPrograms;
	}
	
	public final ExtendedShaderProgram add(final String name, final ExtendedShaderProgram shaderProgram) {
		this.getShaderPrograms().put(name, shaderProgram);
		
		return shaderProgram;
	}
	
	public final ExtendedShaderProgram addSelectionShader(final String name, final ExtendedShaderProgram shaderProgram) {
		this.getSelectionShaderPrograms().put(name, shaderProgram);
		
		return shaderProgram;
	}
	
	public final Map<String, Geometry> getGeometries() {
		return this.geometries;
	}
	
	public final Map<String, Geometry> getSelectionGeometries() {
		return this.selectionGeometries;
	}
	
	public final Geometry add(final String name, final Geometry geometry) {
		this.getGeometries().put(name, geometry);
		
		return geometry;
	}
	
	public final Geometry addSelectionGeometry(final String name, final Geometry geometry) {
		this.getSelectionGeometries().put(name, geometry);
		
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
		
		{
			drawable.getGL().glGenFramebuffers(1, this.selectionFrameBuffer);
			drawable.getGL().glGenRenderbuffers(1, this.selectionRenderBuffer);
			drawable.getGL().glGenRenderbuffers(1, this.selectionDepthBuffer);
		}
		
		this.initialize(drawable);
		
		drawable.setAnimator(new Animator(drawable));
		drawable.getAnimator().start();
	}
	
	@Override
	public final void dispose(final GLAutoDrawable drawable) {
		final GLAnimatorControl animator = drawable.getAnimator();
		
		if (animator != null) {
			animator.stop();
		}
		
		drawable.getGL().glDeleteFramebuffers(1, this.selectionFrameBuffer);
		drawable.getGL().glDeleteRenderbuffers(1, this.selectionRenderBuffer);
		drawable.getGL().glDeleteRenderbuffers(1, this.selectionDepthBuffer);
		
		this.getShaderPrograms().clear();
		this.getSelectionShaderPrograms().clear();
		this.getGeometries().clear();
		
		Tools.gc(100L);
	}
	
	public final ByteBuffer getSelectionData() {
		return this.selectionData;
	}
	
	@Override
	public final void reshape(final GLAutoDrawable drawable, final int x, final int y,
			final int width, final int height) {
		this.getCamera().setCanvasSize(width, height).updateGL();
		
		this.reshapeSelectionBuffers(width, height);
		
		this.reshaped();
	}
	
	private final void reshapeSelectionBuffers(final int width, final int height) {
		this.getGL().glBindFramebuffer(GL.GL_FRAMEBUFFER, this.selectionFrameBuffer.get(0));
		
		this.getGL().glBindRenderbuffer(GL.GL_RENDERBUFFER, this.selectionRenderBuffer.get(0));
		this.getGL().glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_RGBA8, width, height);
		this.getGL().glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_RENDERBUFFER, this.selectionRenderBuffer.get(0));
		
		this.getGL().glBindRenderbuffer(GL.GL_RENDERBUFFER, this.selectionDepthBuffer.get(0));
		this.getGL().glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_DEPTH_COMPONENT16, width, height);
		this.getGL().glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, this.selectionDepthBuffer.get(0));
		
		this.getGL().glEnable(GL.GL_DEPTH_TEST);
		this.getGL().glDepthFunc(GL.GL_LESS);
		
		this.getGL().glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
		
		this.selectionData = Buffers.newDirectByteBuffer(width * height * 4);
	}
	
	@Override
	public final void display(final GLAutoDrawable drawable) {
		if (this.getUpdatedNeeded().getAndSet(false)) {
			this.renderBuffersOutdated = 2;
		}
		
		if (0 < this.renderBuffersOutdated) {
			--this.renderBuffersOutdated;
			this.beforeRender();
			this.render();
			this.afterRender();
		} else {
			try {
				Thread.sleep(20L);
			} catch (final InterruptedException exception) {
				throw Tools.unchecked(exception);
			}
		}
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
		this.getShaderPrograms().values().forEach(ExtendedShaderProgram::run);
		
		this.selectionRender();
	}
	
	private final void selectionRender() {
		if (!this.getSelectionShaderPrograms().isEmpty()) {
			final Dimension canvasSize = this.getCamera().getCanvasSize();
			
			this.getGL().glBindFramebuffer(GL.GL_FRAMEBUFFER, this.selectionFrameBuffer.get(0));
			this.getGL().glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			this.getSelectionShaderPrograms().values().forEach(ExtendedShaderProgram::run);
			this.getGL().glReadPixels(0, 0, canvasSize.width, canvasSize.height, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, this.selectionData);
			this.getGL().glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
		}
	}
	
	protected void afterRender() {
		// NOP
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8853467479921949191L;
	
}
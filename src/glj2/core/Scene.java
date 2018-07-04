package glj2.core;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;

import static glj2.core.GLJTools.checkFrameBufferStatus;
import static multij.tools.MathTools.lelt;
import static multij.tools.Tools.ints;

import java.awt.Dimension;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
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
	
	private final Map<String, Geometry> geometries = new LinkedHashMap<>();
	
	private GL gl;
	
	private int renderBuffersOutdated;
	
	private final Picking picking = this.new Picking();
	
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
	
	public final Picking getPicking() {
		return this.picking;
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
		
		this.getPicking().initialize();
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
		
		this.dispose();
		
		this.getPicking().dispose();
		this.getShaderPrograms().clear();
		this.getGeometries().clear();
		
		Tools.gc(100L);
	}
	
	protected void dispose() {
		// NOP
	}
	
	@Override
	public final void reshape(final GLAutoDrawable drawable, final int x, final int y,
			final int width, final int height) {
		this.getCamera().setCanvasSize(width, height).updateGL();
		
		this.getPicking().reshape(width, height);
		
		this.reshaped();
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
		
		this.getPicking().render();
	}
	
	protected void afterRender() {
		// NOP
	}
	
	/**
	 * @author codistmonk (creation 2018-07-01)
	 */
	public final class Picking implements Serializable {
		
		private final Map<String, ExtendedShaderProgram> idShaderPrograms = new LinkedHashMap<>();
		
		private final Map<String, Geometry> geometries = new LinkedHashMap<>();
		
		private final IntBuffer frameBuffer = Buffers.newDirectIntBuffer(1);
		
		private final IntBuffer renderBuffer = Buffers.newDirectIntBuffer(1);
		
		private final IntBuffer depthBuffer = Buffers.newDirectIntBuffer(1);
		
		private final IntBuffer uvBuffer = Buffers.newDirectIntBuffer(1);
		
		private ByteBuffer idData;
		
		private ByteBuffer uvData;
		
		private final int[] id = { 1 };
		
		private final TreeMap<Integer, Object> objects = new TreeMap<>();
		
		private final Pick pick = this.new Pick();
		
		public final Map<String, ExtendedShaderProgram> getShaderPrograms() {
			return this.idShaderPrograms;
		}
		
		public final int[] getId() {
			return this.id;
		}
		
		public final TreeMap<Integer, Object> getObjects() {
			return this.objects;
		}
		
		public final Object add(final Object object) {
			this.getObjects().put(this.getId()[0], object);
			
			return object;
		}
		
		public final void initialize() {
			final GL gl = getGL();
			
			gl.glGenFramebuffers(1, this.frameBuffer);
			gl.glGenRenderbuffers(1, this.renderBuffer);
			gl.glGenRenderbuffers(1, this.depthBuffer);
			gl.glGenRenderbuffers(1, this.uvBuffer);
		}
		
		public final void dispose() {
			final GL gl = getGL();
			
			gl.glDeleteFramebuffers(1, this.frameBuffer);
			gl.glDeleteRenderbuffers(1, this.renderBuffer);
			gl.glDeleteRenderbuffers(1, this.depthBuffer);
			gl.glDeleteRenderbuffers(1, this.uvBuffer);
			
			this.getShaderPrograms().clear();
			this.getGeometries().clear();
		}
		
		public final void reshape(final int width, final int height) {
			final GL gl = getGL();
			
			gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, this.frameBuffer.get(0));
			
			gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, this.renderBuffer.get(0));
			gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_RGBA8, width, height);
			gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_RENDERBUFFER, this.renderBuffer.get(0));
			
			gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, this.depthBuffer.get(0));
			gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_DEPTH_COMPONENT16, width, height);
			gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, this.depthBuffer.get(0));
			
			gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, this.uvBuffer.get(0));
			gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_RGBA8, width, height);
			gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0 + 1, GL.GL_RENDERBUFFER, this.uvBuffer.get(0));
			
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glDepthFunc(GL.GL_LESS);
			
			gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
			
			this.idData = Buffers.newDirectByteBuffer(width * height * Integer.BYTES);
			this.uvData = Buffers.newDirectByteBuffer(width * height * Float.BYTES * 2);
		}
		
		public final void render() {
			if (!this.getShaderPrograms().isEmpty()) {
				final Dimension canvasSize = getCamera().getCanvasSize();
				final GL2 gl = getGL();
				
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, this.frameBuffer.get(0));
				gl.glDrawBuffers(2, ints(GL.GL_COLOR_ATTACHMENT0, GL.GL_COLOR_ATTACHMENT0 + 1), 0);
				
				checkFrameBufferStatus(gl);
				
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				getShaderPrograms().values().forEach(ExtendedShaderProgram::run);
				gl.glReadBuffer(GL.GL_COLOR_ATTACHMENT0);
				gl.glReadPixels(0, 0, canvasSize.width, canvasSize.height, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, this.idData);
				gl.glReadBuffer(GL.GL_COLOR_ATTACHMENT0 + 1);
				gl.glReadPixels(0, 0, canvasSize.width, canvasSize.height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, this.uvData);
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
			}
		}
		
		public final ByteBuffer getIdData() {
			return this.idData;
		}
		
		public final ByteBuffer getUVData() {
			return this.uvData;
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
		
		public final Pick pick(final int x, final int y) {
			return this.pick(x, y, this.pick);
		}
		
		public final Pick pick(final int x, final int y, final Pick result) {
			if (getPicking().getIdData() == null) {
				return null;
			}
			
			{
				final Dimension wh = getCamera().getCanvasSize();
				final int w = wh.width;
				final int h = wh.height;
				
				if (!lelt(0, x, w) || !lelt(0, y, h)) {
					return null;
				}
			}
			
			final int i = this.computeIndex(x, y);
			final int uv = this.getUV(i);
			final float u = unpackU(uv);
			final float v = unpackV(uv);
			final int id = getPicking().getIdData().getInt(i * Integer.BYTES) & 0x00FFFFFF;
			final Entry<Integer, Object> entry = getPicking().getObjects().floorEntry(id);
			
			return result.set(id, entry, u, v);
		}
		
		public int getUV(final int index) {
			return this.getUVData().getInt(index * Integer.BYTES);
		}
		
		public final int computeIndex(final int x, final int y) {
			final Dimension wh = getCamera().getCanvasSize();
			final int w = wh.width;
			final int h = wh.height;
			
			return x + (h - 1 - y) * w;
		}
		
		/**
		 * @author codistmonk (creation 2018-07-02)
		 */
		public final class Pick implements Serializable {
			
			private int id;
			
			private Map.Entry<Integer, Object> entry;
			
			private float u;
			
			private float v;
			
			{
				this.set(null);
			}
			
			public final Pick set(final int id, final Map.Entry<Integer, Object> entry, final float u, final float v) {
				this.id = id;
				this.entry = entry;
				this.u = u;
				this.v = v;
				
				return this;
			}
			
			public final Pick set(final Pick pick) {
				if (pick == null) {
					return this.set(-1, null, Float.NaN, Float.NaN);
				}
				
				return this.set(pick.getId(), pick.getEntry(), pick.getU(), pick.getV());
			}
			
			public final int getId() {
				return this.id;
			}
			
			public final Map.Entry<Integer, Object> getEntry() {
				return this.entry;
			}
			
			@SuppressWarnings("unchecked")
			public final <T> T getObject() {
				return this.getEntry() == null ? null : (T) this.getEntry().getValue();
			}
			
			public final int getIndexInObject() {
				return this.getEntry() == null ? -1 : this.getId() - this.getEntry().getKey();
			}
			
			public final float getU() {
				return this.u;
			}
			
			public final float getV() {
				return this.v;
			}
			
			private static final long serialVersionUID = 2038320657797923347L;
			
		}
		
		private static final long serialVersionUID = 8979091140094393524L;
		
	}
	
	private static final long serialVersionUID = -8853467479921949191L;
	
	public static final float unpackU(final int uv) {
		return (uv & 0x0000FFFF) / 65536F;
	}
	
	public static final float unpackV(final int uv) {
		return ((uv >> 16) & 0x0000FFFF) / 65536F;
	}
	
}
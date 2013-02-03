package glj;

import static glj.GLJTools.FLOAT_BYTE_COUNT;
import static glj.GLJTools.INT_BYTE_COUNT;
import static glj.GLJTools.UNIT_Y;
import static glj.GLJTools.UNIT_Z;
import static glj.GLJTools.ZERO;
import static glj.GLJTools.setLookAtMatrix;
import static glj.GLJTools.setOrthographicProjectionMatrix;
import static glj.GLJTools.setPerspectiveProjectionMatrix;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_BUFFER_SIZE;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import static net.sourceforge.aprog.tools.Tools.ignore;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import net.sourceforge.aprog.tools.Tools;

import com.jogamp.opengl.util.packrect.Rect;

/**
 * @author codistmonk (creation 2012-01-12)
 */
public abstract class Scene implements GLEventListener {
	
	private GLAutoDrawable drawable;
	
	private GL4 gl;
	
	private GLSL glsl;
	
	private final Camera defaultCamera;
	
	private boolean debugGLEnabled;
	
	public Scene() {
		this.defaultCamera = this.new Camera();
		this.debugGLEnabled = true;
	}
	
	public final Camera getDefaultCamera() {
		return this.defaultCamera;
	}
	
	public final GLAutoDrawable getDrawable() {
		return this.drawable;
	}
	
	public final GL4 getGL() {
		return this.gl;
	}
	
	public final boolean isDebugGLEnabled() {
		return this.debugGLEnabled;
	}
	
	public final void setDebugGLEnabled(final boolean debugGLEnabled) {
		this.debugGLEnabled = debugGLEnabled;
	}
	
	public final GLSL getGLSL() {
		return this.glsl;
	}
	
	@Override
	public final void init(final GLAutoDrawable drawable) {
		this.updateDrawable(drawable);
		
		this.initialize();
	}
	
	@Override
	public final void dispose(final GLAutoDrawable drawable) {
		debugPrint();
		this.updateDrawable(drawable);
		
		this.dispose();
	}
	
	@Override
	public final void display(final GLAutoDrawable drawable) {
		this.updateDrawable(drawable);
		
		this.display();
	}
	
	@Override
	public final void reshape(final GLAutoDrawable drawable, final int x, final int y,
			final int width, final int height) {
		this.updateDrawable(drawable);
		
		this.getDefaultCamera().setViewport(x, y, width, height);
		
		this.reshape();
	}
	
	/**
	 * @return
	 * <br>Range: any boolean
	 */
	public final boolean debugGL() {
		if (this.isDebugGLEnabled()) {
			int errorCode = this.getGL().glGetError();
			
			final boolean result = errorCode != GL.GL_NO_ERROR;
			
			while (errorCode != GL.GL_NO_ERROR) {
				System.err.println(Tools.debug(Tools.DEBUG_STACK_OFFSET + 1, "OpenGL error 0x" + Integer.toHexString(errorCode), ":", new GLU().gluErrorString(errorCode)));
				
				errorCode = this.getGL().glGetError();
			}
			
			if (result) {
				throw new RuntimeException();
			}
			
			return result;
		}
		
		return false;
	}
	
	/**
	 * @return
	 * <br>Range: <code>[100 .. 990]</code>
	 */
	public final int computeGLSLVersion() {
		final String shadingLanguageVersion = this.getGL().glGetString(GL2ES2.GL_SHADING_LANGUAGE_VERSION); this.debugGL();
		
		try {
			return (int) (100.0 * new Scanner(shadingLanguageVersion).nextDouble());
		} catch (final Exception exception) {
			ignore(exception);
		}
		
		return 120;
	}
	
	protected void initialize() {
		this.getGL().glEnable(GL_DEPTH_TEST); this.debugGL();
		this.getGL().glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
	}
	
	protected void dispose() {
		// NOP
	}
	
	protected void display() {
		this.getGL().glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
	}
	
	protected void reshape() {
		// NOP
	}
	
	private final void updateDrawable(final GLAutoDrawable drawable) {
		if (this.drawable != drawable) {
			this.drawable = drawable;
			this.gl = (GL4) drawable.getGL();
			this.glsl = this.new GLSL();
		}
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public final class ShaderProgram {
		
		private final int id;
		
		public ShaderProgram() {
			this.id = Scene.this.getGL().glCreateProgram(); Scene.this.debugGL();
		}
		
		public final int getId() {
			return this.id;
		}
		
		public final ShaderProgram attach(final Shader shader) {
			Scene.this.getGL().glAttachShader(this.getId(), shader.getId()); Scene.this.debugGL();
			
			return this;
		}
		
		public final ShaderProgram link() {
			Scene.this.getGL().glLinkProgram(this.getId()); Scene.this.debugGL();
			
			return this;
		}
		
		public final ShaderProgram use() {
			Scene.this.getGL().glUseProgram(this.getId()); Scene.this.debugGL();
			
			return this;
		}
		
		public final ShaderProgram unuse() {
			Scene.this.getGL().glUseProgram(0); Scene.this.debugGL();
			
			return this;
		}
		
		public final ShaderProgram bindAttribute(final String name, final VBO vbo) {
			this.use();
			
			final int attributeId = this.getAttributeId(name);
			
			vbo.bind();
			Scene.this.getGL().glVertexAttribPointer(attributeId,
					vbo.getValuesPerComponent(), vbo.getValueType(), false, 0, 0L); Scene.this.debugGL();
			Scene.this.getGL().glEnableVertexAttribArray(attributeId); Scene.this.debugGL();
			vbo.unbind();
			
			return this.unuse();
		}
		
		public final int getAttributeId(final String name) {
			try {
				return Scene.this.getGL().glGetAttribLocation(this.getId(), name);
			} finally {
				Scene.this.debugGL();
			}
		}
		
		public final int getUniformId(final String name) {
			try {
				return Scene.this.getGL().glGetUniformLocation(this.getId(), name);
			} finally {
				Scene.this.debugGL();
			}
		}
		
		public final ShaderProgram setUniform(final String name, final Matrix4f matrix) {
			this.use();
			
			final int uniformId = this.getUniformId(name);
			
			Scene.this.getGL().glUniformMatrix4fv(uniformId, 1, true, new float[] {
					matrix.m00, matrix.m01, matrix.m02, matrix.m03,
					matrix.m10, matrix.m11, matrix.m12, matrix.m13,
					matrix.m20, matrix.m21, matrix.m22, matrix.m23,
					matrix.m30, matrix.m31, matrix.m32, matrix.m33,
			}, 0); Scene.this.debugGL();
			
			return this.unuse();
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public final class Shader {
		
		private final int id;
		
		private final int type;
		
		private StringBuilder sourceBuilder;
		
		public Shader(final int type) {
			this.id = Scene.this.getGL().glCreateShader(type); Scene.this.debugGL();
			this.type = type;
			this.sourceBuilder = new StringBuilder();
		}
		
		public final int getId() {
			return this.id;
		}
		
		public final int getType() {
			return this.type;
		}
		
		public final Shader compile() {
			Scene.this.getGL().glShaderSource(this.getId(), 1, 
					array(this.sourceBuilder.toString()), null, 0); Scene.this.debugGL();
			Scene.this.getGL().glCompileShader(this.getId()); Scene.this.debugGL();
			
			this.sourceBuilder = null;
			
			this.printInfoLog();
			
			return this;
		}
		
		public final Shader appendLine(final String line) {
			this.sourceBuilder.append(line
					.replaceAll("\\" + VERSION, "" + Scene.this.getGLSL().getVersion())
					.replaceAll("\\" + ATTRIBUTE, Scene.this.getGLSL().getAttribute())
					.replaceAll("\\" + IN, "" + Scene.this.getGLSL().getVaryingIn())
					.replaceAll("\\" + OUT, "" + Scene.this.getGLSL().getVaryingOut())
					.replaceAll("\\" + DECLARE_FRAGMENT_OUTPUT_COLOR, "" + Scene.this.getGLSL().getDeclareFragmentOutputColor())
					.replaceAll("\\" + FRAGMENT_OUTPUT_COLOR, "" + Scene.this.getGLSL().getFragmentOutputColor())
				).append('\n');
			
			return this;
		}
		
		public final void printInfoLog() {
			final int[] infoLogLength = { 0 };
			
			Scene.this.getGL().glGetShaderiv(this.getId(), GL2ES2.GL_INFO_LOG_LENGTH, infoLogLength, 0); Scene.this.debugGL();
			
			if (infoLogLength[0] > 0) {
				final byte[] infoLog = new byte[infoLogLength[0]];
				final int[] bytesWritten = { 0 };
				
				Scene.this.getGL().glGetShaderInfoLog(this.getId(), infoLogLength[0], bytesWritten, 0, infoLog, 0); Scene.this.debugGL();
				
				if (bytesWritten[0] > 0)
				{
					debugPrint("Shader", this.getId() + ":", new String(infoLog));
				}
			}
		}
		
		/**
		 * {@value}.
		 */
		public static final String FRAGMENT_OUTPUT_COLOR = "$FRAGMENT_OUTPUT_COLOR";
		
		/**
		 * {@value}.
		 */
		public static final String DECLARE_FRAGMENT_OUTPUT_COLOR = "$DECLARE_FRAGMENT_OUTPUT_COLOR";
		
		/**
		 * {@value}.
		 */
		public static final String OUT = "$OUT";
		
		/**
		 * {@value}.
		 */
		public static final String IN = "$IN";
		
		/**
		 * {@value}.
		 */
		public static final String ATTRIBUTE = "$ATTRIBUTE";
		
		/**
		 * {@value}.
		 */
		public static final String VERSION = "$VERSION";
		
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public final class GLSL {
		
		private final int version;
		
		private final String attribute;
		
		private final String varyingOut;
		
		private final String varyingIn;
		
		private final String declareFragmentOutputColor;
		
		private final String fragmentOutputColor;
		
		public GLSL() {
			this.version = Scene.this.computeGLSLVersion();
			
			if (this.getVersion() < 130) {
				this.attribute = "attribute";
				this.varyingOut = "varying";
				this.varyingIn = "varying";
				this.declareFragmentOutputColor = "";
				this.fragmentOutputColor = "gl_FragData[0]";
			} else {
				this.attribute = "in";
				this.varyingOut = "out";
				this.varyingIn = "in";
				this.declareFragmentOutputColor = "out vec4 fragmentOutputColor;";
				this.fragmentOutputColor = "fragmentOutputColor";
			}
		}
		
		public final int getVersion() {
			return this.version;
		}
		
		public final String getAttribute() {
			return this.attribute;
		}
		
		public final String getVaryingOut() {
			return this.varyingOut;
		}
		
		public final String getVaryingIn() {
			return this.varyingIn;
		}
		
		public final String getDeclareFragmentOutputColor() {
			return this.declareFragmentOutputColor;
		}
		
		public final String getFragmentOutputColor() {
			return this.fragmentOutputColor;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public final class VAO {
		
		private final int[] id;
		
		public VAO() {
			this.id = new int[1];
			
			Scene.this.getGL().glGenVertexArrays(1, this.id, 0); Scene.this.debugGL();
		}
		
		public final int getId() {
			return this.id[0];
		}
		
		public final VAO bind() {
			Scene.this.getGL().glBindVertexArray(this.getId()); Scene.this.debugGL();
			
			return this;
		}
		
		public final VAO unbind() {
			Scene.this.getGL().glBindVertexArray(0); Scene.this.debugGL();
			
			return this;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public final class VBO {
		
		private final int[] id;
		
		private final int bufferType;
		
		private final int valueType;
		
		private final int valuesPerComponent;
		
		private int valueCount;
		
		public VBO(final int valueType) {
			this(GL_ELEMENT_ARRAY_BUFFER, valueType, 1);
		}
		
		public VBO(final int valueType, final int valuesPerComponent) {
			this(GL_ARRAY_BUFFER, valueType, valuesPerComponent);
		}
		
		public VBO(final int bufferType, final int valueType, final int valuesPerComponent) {
			this.id = new int[1];
			this.bufferType = bufferType;
			this.valueType = valueType;
			this.valuesPerComponent = valuesPerComponent;
			
			Scene.this.getGL().glGenBuffers(1, this.id, 0); Scene.this.debugGL();
		}
		
		public final int getId() {
			return this.id[0];
		}
		
		public final int getBufferType() {
			return this.bufferType;
		}
		
		public final int getValueType() {
			return this.valueType;
		}
		
		public final int getValuesPerComponent() {
			return this.valuesPerComponent;
		}
		
		public final int getValueCount() {
			return this.valueCount;
		}
		
		public final int getComponentCount() {
			return this.getValueCount() / this.getValuesPerComponent();
		}
		
		public final VBO bind() {
			Scene.this.getGL().glBindBuffer(this.getBufferType(), this.getId()); Scene.this.debugGL();
			
			return this;
		}
		
		public final VBO unbind() {
			Scene.this.getGL().glBindBuffer(this.getBufferType(), 0); Scene.this.debugGL();
			
			return this;
		}
		
		public final VBO update(final int offset, final float... data) {
			return this.update(offset, data.length, FLOAT_BYTE_COUNT, FloatBuffer.wrap(data));
		}
		
		public final VBO update(final int offset, final int... data) {
			return this.update(offset, data.length, INT_BYTE_COUNT, IntBuffer.wrap(data));
		}
		
		private final VBO update(final int offset, final int dataLength, final int datumByteCount, final Buffer data) {
			this.bind();
			
			final int[] bufferSize = { 0 };
			
			Scene.this.getGL().glGetBufferParameteriv(this.getBufferType(), GL_BUFFER_SIZE, bufferSize, 0); Scene.this.debugGL();
			final long byteOffset = offset * datumByteCount;
			final long dataByteCount = dataLength * datumByteCount;
			
			if (bufferSize[0] < byteOffset + dataByteCount) {
				Scene.this.getGL().glBufferData(this.getBufferType(), byteOffset + dataByteCount, null, GL.GL_DYNAMIC_DRAW); Scene.this.debugGL();
			}
			
			Scene.this.getGL().glBufferSubData(this.getBufferType(), byteOffset, dataByteCount, data); Scene.this.debugGL();
			
			this.valueCount = Math.max(this.valueCount, offset + dataLength);
			
			return this.unbind();
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2012-11-13)
	 */
	public final class Camera {
		
		private final Rect viewport;
		
		private float zNear;
		
		private float zFar;
		
		private final Matrix4f projection;
		
		private final Matrix4f position;
		
		private final Vector4f scissorRegion;
		
		private boolean orthographic;
		
		private float orthographicScale;
		
		public Camera() {
			this.viewport = new Rect();
			this.projection = new Matrix4f();
			this.position = new Matrix4f();
			this.scissorRegion = new Vector4f();
			
			this.setZClipping(DEFAULT_Z_NEAR, DEFAULT_Z_FAR);
			this.setViewport(DEFAULT_VIEWPORT_X, DEFAULT_VIEWPORT_Y, DEFAULT_VIEWPORT_WIDTH, DEFAULT_VIEWPORT_HEIGHT);
			this.setPosition(UNIT_Z, ZERO, UNIT_Y);
			this.setOrthographic(false);
		}
		
		/**
		 * @param cameraLocation
		 * <br>Not null
		 * @param targetLocation
		 * <br>Not null
		 * @param upDirection
		 * <br>Not null
		 */
		public final void setPosition(final Tuple3f cameraLocation, final Tuple3f targetLocation, final Vector3f upDirection) {
			setLookAtMatrix(this.getPosition(), cameraLocation, targetLocation, upDirection);
		}
		
		/**
		 * @param x
		 * <br>Range: any int
		 * @param y
		 * <br>Range: any int
		 * @param width
		 * <br>Range: <code>[1 .. Integer.MAX_VALUE]</code>
		 * @param height
		 * <br>Range: <code>[1 .. Integer.MAX_VALUE]</code>
		 */
		public final void setViewport(final int x, final int y, final int width, final int height) {
			this.getViewport().setPosition(x, y);
			this.getViewport().setSize(width, height);
			this.setScissorRegion(x, y, width, height);
		}
		
		/**
		 * @param x
		 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
		 * @param y
		 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
		 * @param width
		 * <br>Range: <code>]+0.0F .. Float.MAX_VALUE]</code>
		 * @param height
		 * <br>Range: <code>]+0.0F .. Float.MAX_VALUE]</code>
		 */
		public final void setScissorRegion(final float x, final float y, final float width, final float height) {
			this.scissorRegion.set(x, y, width, height);
			this.updateProjectionMatrix();
		}
		
		/**
		 * @return
		 * <br>Range: any boolean
		 */
		public final boolean isOrthographic() {
			return this.orthographic;
		}
		
		/**
		 * @param orthographic
		 * <br>Range: any boolean
		 */
		public final void setOrthographic(final boolean orthographic) {
			this.orthographic = orthographic;
			
			this.updateProjectionMatrix();
		}
		
		/**
		 * @return
		 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
		 */
		public final float getOrthographicScale() {
			return this.orthographicScale;
		}
		
		/**
		 * @param orthographicScale
		 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
		 */
		public final void setOrthographicScale(final float orthographicScale) {
			this.orthographicScale = orthographicScale;
			
			if (this.isOrthographic()) {
				this.updateProjectionMatrix();
			}
		}
		
		/**
		 * @return
		 * <br>Not null
		 * <br>Reference
		 */
		public final Rect getViewport() {
			return this.viewport;
		}
		
		/**
		 * @return
		 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE] - {0.0F}</code>
		 */
		public final float getZNear() {
			return this.zNear;
		}
		
		/**
		 * @return
		 * <br>Range: <code>]this.getZNear() .. Float.MAX_VALUE] - {0.0F}</code>
		 */
		public final float getZFar() {
			return this.zFar;
		}
		
		/**
		 * @param zNear
		 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE] - {0.0F}</code>
		 * @param zFar
		 * <br>Range: <code>]zNear .. Float.MAX_VALUE] - {0.0F}</code>
		 */
		public final void setZClipping(final float zNear, final float zFar) {
			this.zNear = zNear;
			this.zFar = zFar;
			
			this.updateProjectionMatrix();
		}
		
		/**
		 * @return
		 * <br>Not null
		 * <br>Reference
		 */
		public final Matrix4f getProjection() {
			return this.projection;
		}
		
		/**
		 * @return
		 * <br>Not null
		 * <br>Reference
		 */
		public final Matrix4f getPosition() {
			return this.position;
		}

		public final void updateViewport() {
			Scene.this.getGL().glViewport(this.getViewport().x(), this.getViewport().y(), this.getViewport().w(), this.getViewport().h()); Scene.this.debugGL();
			// TODO find a way to reduce the viewport to the scissor region (and maybe glScissor will become useless)
//					gl.glViewport((int) this.scissorRegion.x, (int) this.scissorRegion.y, (int) this.scissorRegion.z, (int) this.scissorRegion.w); Scene.this.debugGL();
			Scene.this.getGL().glScissor((int) this.scissorRegion.x, (int) this.scissorRegion.y, (int) this.scissorRegion.z, (int) this.scissorRegion.w); Scene.this.debugGL();
			
			Scene.this.getGL().glEnable(GL.GL_SCISSOR_TEST); Scene.this.debugGL();
		}
		
		private final void updateProjectionMatrix() {
			final float viewportAspectRatio = (float) this.getViewport().w() / this.getViewport().h();
			
			this.getProjection().setIdentity();
			
			final Matrix4f localTransform = new Matrix4f();
			
			if (this.isOrthographic()) {
				setOrthographicProjectionMatrix(localTransform,
						-this.getOrthographicScale() * viewportAspectRatio, +this.getOrthographicScale() * viewportAspectRatio,
		                -this.getOrthographicScale(), +this.getOrthographicScale(),
		                -this.getZFar() / (this.getViewport().w() / this.getOrthographicScale()), +this.getZFar());
			} else {
				setPerspectiveProjectionMatrix(localTransform,
						FIELD_OF_VISION_VERTICAL_ANGLE, viewportAspectRatio, this.getZNear(), this.getZFar());
			}
			
			this.getProjection().mul(localTransform);
		}

		/**
		 * {@value}.
		 */
		public static final int DEFAULT_VIEWPORT_X = +0;
		
		/**
		 * {@value}.
		 */
		public static final int DEFAULT_VIEWPORT_Y = +0;
		
		/**
		 * {@value}.
		 */
		public static final int DEFAULT_VIEWPORT_WIDTH = +800;
		
		/**
		 * {@value}.
		 */
		public static final int DEFAULT_VIEWPORT_HEIGHT = +600;
		
		/**
		 * {@value}.
		 */
		public static final float DEFAULT_Z_NEAR = +0.1F;
		
		/**
		 * {@value}.
		 */
		public static final float DEFAULT_Z_FAR = +1000.0F;
		
		/**
		 * {@value}.
		 */
		public static final float FIELD_OF_VISION_VERTICAL_ANGLE = (float) (+Math.PI / 3.0F);
		
	}
	
}

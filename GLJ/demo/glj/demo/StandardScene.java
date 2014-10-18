package glj.demo;

import static glj.Scene.Shader.ATTRIBUTE;
import static glj.Scene.Shader.DECLARE_FRAGMENT_OUTPUT_COLOR;
import static glj.Scene.Shader.FRAGMENT_OUTPUT_COLOR;
import static glj.Scene.Shader.IN;
import static glj.Scene.Shader.OUT;
import static glj.Scene.Shader.VERSION;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_LINES;
import static javax.media.opengl.GL.GL_POINTS;
import static net.sourceforge.aprog.tools.Tools.debugPrint;

import glj.Scene;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLDebugListener;
import javax.media.opengl.GLDebugMessage;
import javax.vecmath.Matrix4f;

import net.sourceforge.aprog.context.Context;

/**
 * @author codistmonk (creation 2012-01-12)
 */
public final class StandardScene extends Scene {
	
	private final Context context;
	
	private final Map<String, Renderer> renderers;
	
	public StandardScene(final Context context) {
		this.context = context;
		this.renderers = new LinkedHashMap<String, Renderer>();
		
		context.set("RENDERERS", this.renderers);
	}
	
	@Override
	protected final void initialize() {
		super.initialize();
		
		this.getDrawable().getContext().enableGLDebugMessage(true);
		this.getDrawable().getContext().addGLDebugListener(new GLDebugListener() {
			
			@Override
			public final void messageSent(final GLDebugMessage event) {
				debugPrint(event);
			}
			
		});
		
		final Shader vertexShader = this.new Shader(GL2ES2.GL_VERTEX_SHADER)
		.appendLine("#version " + VERSION)
		.appendLine("")
		.appendLine("uniform mat4 projection;")
		.appendLine("uniform mat4 view;")
		.appendLine("uniform mat4 model;")
		.appendLine("")
		.appendLine(ATTRIBUTE + " vec3 vertexLocation;")
		.appendLine(ATTRIBUTE + " vec4 vertexColor;")
		.appendLine("")
		.appendLine(OUT +" vec4 fragmentInputColor;")
		.appendLine("")
		.appendLine("void main()")
		.appendLine("{")
		.appendLine("	gl_Position = projection * view * model * vec4(vertexLocation, 1.0);")
		.appendLine("	fragmentInputColor = vertexColor;")
		.appendLine("}")
		;
		
		final Shader fragmentShader = this.new Shader(GL2ES2.GL_FRAGMENT_SHADER)
		.appendLine("#version " + VERSION)
		.appendLine("")
		.appendLine(IN + " vec4 fragmentInputColor;")
		.appendLine("")
		.appendLine(DECLARE_FRAGMENT_OUTPUT_COLOR)
		.appendLine("")
		.appendLine("void main()")
		.appendLine("{")
		.appendLine("	" + FRAGMENT_OUTPUT_COLOR + " = fragmentInputColor;")
		.appendLine("}")
		;
		
		final ShaderProgram shaderProgram = this.new ShaderProgram()
		.attach(vertexShader.compile())
		.attach(fragmentShader.compile())
		.link()
		;
		
		this.context.set("SHADER_PROGRAM", shaderProgram);
		
		final Matrix4f position = new Matrix4f();
		
		position.setIdentity();
		
		final float negative = -128.0F;
		final float positive = +127.0F;
		final float size = positive - negative;
		int segmentIndex = 0;
		
		for (final float[] ij : new float[][] { { negative, negative }, { negative, positive }, { positive, positive }, { positive, negative } }) {
			final float i = ij[0];
			final float j = ij[1];
			final float ci = (i - negative) / size;
			final float cj = (j - negative) / size;
			
			this.renderers.put("boxSegment" + (segmentIndex++), this.new PrimitiveRenderer(
					GL_LINES,
					shaderProgram,
					this.new VBO(GL_FLOAT, 3).update(0,
							negative, i, j,
							positive, i, j
					),
					this.new VBO(GL_FLOAT, 4).update(0,
							+0.0F, ci, cj, +1.0F,
							+1.0F, ci, cj, +1.0F
					),
					position
			));
			
			this.renderers.put("boxSegment" + (segmentIndex++), this.new PrimitiveRenderer(
					GL_LINES,
					shaderProgram,
					this.new VBO(GL_FLOAT, 3).update(0,
							j, negative, i,
							j, positive, i
							),
					this.new VBO(GL_FLOAT, 4).update(0,
							cj, +0.0F, ci, +1.0F,
							cj, +1.0F, ci, +1.0F
							),
					position
			));
			
			this.renderers.put("boxSegment" + (segmentIndex++), this.new PrimitiveRenderer(
					GL_LINES,
					shaderProgram,
					this.new VBO(GL_FLOAT, 3).update(0,
							i, j, negative,
							i, j, positive
							),
					this.new VBO(GL_FLOAT, 4).update(0,
							ci, cj, +0.0F, +1.0F,
							ci, cj, +1.0F, +1.0F
							),
					position
			));
		}
		
		this.renderers.put("dataPoints", this.new PrimitiveRenderer(
				GL_POINTS,
				shaderProgram,
				this.new VBO(GL_FLOAT, 3).update(0, +0.0F, +0.0F, +0.0F),
				this.new VBO(GL_FLOAT, 4).update(0, +1.0F, +1.0F, +1.0F, +1.0F),
				position
		));
	}
	
	@Override
	protected final void display() {
		super.display();
		
		for (final Renderer renderer : this.renderers.values()) {
			renderer.render();
		}
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public final class PrimitiveRenderer implements Renderer {
		
		private final ShaderProgram shaderProgram;
		
		private final VBO locations;
		
		private final VBO colors;
		
		private final Matrix4f position;
		
		private final int primitiveType;
		
		private final float primitiveSize;
		
		public PrimitiveRenderer(final int primitiveType, final float primitiveSize, final ShaderProgram shaderProgram, final VBO locations,
				final VBO colors, final Matrix4f position) {
			this.primitiveType = primitiveType;
			this.primitiveSize = primitiveSize;
			this.shaderProgram = shaderProgram;
			this.locations = locations;
			this.colors = colors;
			this.position = position;
		}
		
		public PrimitiveRenderer(final int primitiveType, final ShaderProgram shaderProgram, final VBO locations,
				final VBO colors, final Matrix4f position) {
			this(primitiveType, 1.0F, shaderProgram, locations, colors, position);
		}
		
		public final ShaderProgram getShaderProgram() {
			return this.shaderProgram;
		}
		
		public final VBO getLocations() {
			return this.locations;
		}
		
		public final VBO getColors() {
			return this.colors;
		}
		
		public final Matrix4f getPosition() {
			return this.position;
		}
		
		public final int getPrimitiveType() {
			return this.primitiveType;
		}
		
		public final float getPrimitiveSize() {
			return this.primitiveSize;
		}
		
		@Override
		public final void render() {
			final GL4 gl = StandardScene.this.getGL();
			
			switch (this.getPrimitiveType()) {
			case GL_POINTS:
				gl.glPointSize(this.getPrimitiveSize()); StandardScene.this.debugGL();
				break;
			case GL_LINES:
				gl.glLineWidth(this.getPrimitiveSize()); StandardScene.this.debugGL();
				break;
			default:
				break;
			}
			
			this.getShaderProgram().setUniform("projection", StandardScene.this.getDefaultCamera().getProjection());
			this.getShaderProgram().setUniform("view", StandardScene.this.getDefaultCamera().getPosition());
			this.getShaderProgram().setUniform("model", this.getPosition());
			this.getShaderProgram().bindAttribute("vertexLocation", this.getLocations());
			this.getShaderProgram().bindAttribute("vertexColor", this.getColors());
			
			this.getShaderProgram().use();
			
			gl.glDrawArrays(this.getPrimitiveType(), 0, this.getLocations().getComponentCount()); StandardScene.this.debugGL();
			
			this.getShaderProgram().unuse();
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public static abstract interface Renderer {
		
		public abstract void render();
		
	}
	
}

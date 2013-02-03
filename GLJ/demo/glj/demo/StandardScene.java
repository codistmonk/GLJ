package glj.demo;

import static glj.Scene.Shader.*;
import static javax.media.opengl.GL.GL_FLOAT;
import static net.sourceforge.aprog.tools.Tools.debugPrint;

import glj.Scene;

import java.util.ArrayList;
import java.util.Collection;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLDebugListener;
import javax.media.opengl.GLDebugMessage;
import javax.vecmath.Matrix4f;

import net.sourceforge.aprog.context.Context;

/**
 * @author codistmonk (creation 2012-01-12)
 */
public final class StandardScene extends Scene {
	
	private final Context context;
	
	private final Collection<Renderer> renderers;
	
	public StandardScene(final Context context) {
		this.context = context;
		this.renderers = new ArrayList<Renderer>();
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
		
		final Matrix4f position = new Matrix4f();
		
		position.setIdentity();
		
		final float negative = -128.0F;
		final float positive = +127.0F;
		final float size = positive - negative;
		
		for (final float[] ij : new float[][] { { negative, negative }, { negative, positive }, { positive, positive }, { positive, negative } }) {
			final float i = ij[0];
			final float j = ij[1];
			final float ci = (i - negative) / size;
			final float cj = (j - negative) / size;
			
			this.renderers.add(new Segment(
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
			
			this.renderers.add(new Segment(
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
			
			this.renderers.add(new Segment(
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
	}
	
	@Override
	protected final void display() {
		super.display();
		
		for (final Renderer renderer : this.renderers) {
			renderer.render();
		}
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public static abstract interface Renderer {
		
		public abstract void render();
		
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public final class Points implements Renderer {
		
		private final ShaderProgram shaderProgram;
		
		private final VBO locations;
		
		private final VBO colors;
		
		private final Matrix4f position;
		
		public Points(final ShaderProgram shaderProgram, final VBO locations,
				final VBO colors, final Matrix4f position) {
			this.shaderProgram = shaderProgram;
			this.locations = locations;
			this.colors = colors;
			this.position = position;
		}
		
		@Override
		public final void render() {
			this.shaderProgram.setUniform("projection", StandardScene.this.getDefaultCamera().getProjection());
			this.shaderProgram.setUniform("view", StandardScene.this.getDefaultCamera().getPosition());
			this.shaderProgram.setUniform("model", this.position);
			this.shaderProgram.bindAttribute("vertexLocation", this.locations);
			this.shaderProgram.bindAttribute("vertexColor", this.colors);
			this.shaderProgram.use();
			StandardScene.this.getGL().glDrawArrays(GL.GL_POINTS, 0, this.locations.getComponentCount()); StandardScene.this.debugGL();
			this.shaderProgram.unuse();
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public final class Segment implements Renderer {
		
		private final ShaderProgram shaderProgram;
		
		private final VBO locations;
		
		private final VBO colors;
		
		private final Matrix4f position;
		
		public Segment(final ShaderProgram shaderProgram, final VBO locations,
				final VBO colors, final Matrix4f position) {
			this.shaderProgram = shaderProgram;
			this.locations = locations;
			this.colors = colors;
			this.position = position;
		}
		
		@Override
		public final void render() {
			this.shaderProgram.setUniform("projection", StandardScene.this.getDefaultCamera().getProjection());
			this.shaderProgram.setUniform("view", StandardScene.this.getDefaultCamera().getPosition());
			this.shaderProgram.setUniform("model", this.position);
			this.shaderProgram.bindAttribute("vertexColor", this.colors);
			this.shaderProgram.bindAttribute("vertexLocation", this.locations);
			this.shaderProgram.use();
			StandardScene.this.getGL().glDrawArrays(GL.GL_LINES, 0, this.locations.getComponentCount()); StandardScene.this.debugGL();
			this.shaderProgram.unuse();
		}
		
	}
	
}

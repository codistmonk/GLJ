package glj2.demos;

import static glj2.core.ExtendedShaderProgram.fragmentShader;
import static glj2.core.ExtendedShaderProgram.vertexShader;
import static net.sourceforge.aprog.tools.Tools.debugPrint;

import com.jogamp.opengl.util.glsl.ShaderCode;

import glj2.core.ExtendedShaderProgram;
import glj2.core.GLSwingContext;
import glj2.core.MatrixConverter;
import glj2.core.Orbiter;
import glj2.core.Scene;
import glj2.core.Shaders;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;

import net.sourceforge.aprog.swing.SwingTools;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-16)
 */
public final class Demo1 {
	
	private Demo1() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		SwingTools.useSystemLookAndFeel();
		
		final GLSwingContext context = new GLSwingContext();
		final ShaderCode vertexShaderCode1 = vertexShader(Shaders.VERTEX_SHADER_1);
		final ShaderCode vertexShaderCode2 = vertexShader(Shaders.VERTEX_SHADER_2);
		final ShaderCode vertexShaderCode3 = vertexShader(Shaders.VERTEX_SHADER_3);
		final ShaderCode fragmentShaderCode1 = fragmentShader(Shaders.FRAGMENT_SHADER_1);
		final ShaderCode fragmentShaderCode2 = fragmentShader(Shaders.FRAGMENT_SHADER_2);
		final ShaderCode fragmentShaderCode3 = fragmentShader(Shaders.FRAGMENT_SHADER_3);
		
		final Scene scene = new Scene() {
			
			private final FrameRate frameRate = new FrameRate(1_000_000_000L);
			
			private final MatrixConverter transform = new MatrixConverter();
			
			@Override
			protected final void initialize(final GLAutoDrawable drawable) {
				drawable.setGL(new DebugGL4((GL4) drawable.getGL()));
				
				final GL4 gl = (GL4) drawable.getGL();
				final String renderer = gl.glGetString(GL.GL_RENDERER);
				final String version = gl.glGetString(GL.GL_VERSION);
				
				debugPrint("GL:", gl);
				debugPrint("Renderer:", renderer);
				debugPrint("Version:", version);
				
				{
					gl.glEnable(GL.GL_DEPTH_TEST);
					gl.glDepthFunc(GL.GL_LESS);
				}
				
				this.add("tr 1", new Triangle(gl)
						.addVertex(0F, 0F, -2F, 1F, 0F, 0F, 1F)
						.addVertex(1F, 0F, -2F, 0F, 1F, 0F, 1F)
						.addVertex(0F, 1F, -2F, 0F, 0F, 1F, 1F));
				this.add("tr 2", new Triangle(gl)
						.addVertex(0F, 0F, -3F, 0F, 1F, 1F, 1F)
						.addVertex(1F, 0F, -3F, 1F, 0F, 1F, 1F)
						.addVertex(0F, 1F, -3F, 1F, 1F, 0F, 1F));
				
				this.add("sp 1 1", new ExtendedShaderProgram(gl).build(vertexShaderCode1, fragmentShaderCode1));
				this.add("sp 1 2", new ExtendedShaderProgram(gl).build(vertexShaderCode1, fragmentShaderCode2));
				this.add("sp 2 3", new ExtendedShaderProgram(gl)
						.attribute("vertexLocation", 0)
						.attribute("vertexColor", 1)
						.build(vertexShaderCode2, fragmentShaderCode3));
				this.add("sp 3 3", new ExtendedShaderProgram(gl)
						.attribute("vertexLocation", 0)
						.attribute("vertexColor", 1)
						.build(vertexShaderCode3, fragmentShaderCode3));
				
				this.add(this.getShaderProgram("sp 3 3"), this.getGeometries().values());
			}
			
			@Override
			protected final void reshaped() {
				this.getCamera().setPerspective(-1F, 1F, -1F, 1F, 0.5F, 40F);
			}
			
			@Override
			protected final void beforeRender() {
				super.beforeRender();
				
				this.getCamera().getProjectionView(this.transform.getMatrix());
				
				this.getShaderProgram("sp 3 3").useProgram(true).setUniformMatrix4fv(
						"transform", 1, true, this.transform.updateBuffer());
			}
			
			@Override
			protected final void afterRender() {
				super.afterRender();
				
				if (this.frameRate.ping()) {
					context.getFrame().setTitle("" + this.frameRate.get());
				}
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 2457973938713347874L;
			
		};
		
		context.getCanvas().addGLEventListener(scene);
		new Orbiter(scene).addTo(context.getCanvas());
		context.show();
	}
	
}

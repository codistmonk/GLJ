package glj2.core;

import static glj2.core.ExtendedShaderProgram.fragmentShader;
import static glj2.core.ExtendedShaderProgram.vertexShader;

import com.jogamp.opengl.util.glsl.ShaderCode;

import javax.media.opengl.GL2ES2;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-18)
 */
public final class Shaders {
	
	private Shaders() {
		throw new IllegalInstantiationException();
	}
	
	public static final ShaderCode VERTEX_SHADER_1 = vertexShader(
			"#version 400\n" +
			"in vec3 vertexLocation;\n" +
			"void main() {\n" +
			"	gl_Position = vec4(vertexLocation, 1.0);\n" +
			"}\n");
	
	public static final ShaderCode VERTEX_SHADER_2 = vertexShader(
			"#version 400\n" +
			"in vec3 vertexLocation;\n" +
			"in vec4 vertexColor;\n" +
			"out vec4 interpolatedColor;\n" +
			"void main() {\n" +
			"	interpolatedColor = vertexColor;\n" +
			"	gl_Position = vec4(vertexLocation, 1.0);\n" +
			"}\n");
	
	public static final ShaderCode VERTEX_SHADER_3 = vertexShader(
			"#version 400\n" +
			"uniform mat4 transform;\n" +
			"in vec3 vertexLocation;\n" +
			"in vec4 vertexColor;\n" +
			"out vec4 interpolatedColor;\n" +
			"void main() {\n" +
			"	interpolatedColor = vertexColor;\n" +
			"	gl_Position = transform * vec4(vertexLocation, 1.0);\n" +
			"}\n");
	
	public static final ShaderCode FRAGMENT_SHADER_1 = fragmentShader(
			"#version 400\n" +
			"out vec4 fragmentColor;\n" +
			"void main() {\n" +
			"	fragmentColor = vec4(0.5, 0.0, 0.5, 1.0);\n" +
			"}\n");
	
	public static final ShaderCode FRAGMENT_SHADER_2 = fragmentShader(
			"#version 400\n" +
			"uniform vec4 objectColor;\n" +
			"out vec4 fragmentColor;\n" +
			"void main() {\n" +
			"	fragmentColor = objectColor;\n" +
			"}\n");
	
	public static final ShaderCode FRAGMENT_SHADER_3 = fragmentShader(
			"#version 400\n" +
			"in vec4 interpolatedColor;\n" +
			"out vec4 fragmentColor;\n" +
			"void main() {\n" +
			"	fragmentColor = interpolatedColor;\n" +
			"}\n");
	
	public static final ExtendedShaderProgram newProgramV1F1(final GL2ES2 gl) {
		return new ExtendedShaderProgram(gl).build(VERTEX_SHADER_1, FRAGMENT_SHADER_1);
	}
	
	public static final ExtendedShaderProgram newProgramV1F2(final GL2ES2 gl) {
		return new ExtendedShaderProgram(gl).build(VERTEX_SHADER_1, FRAGMENT_SHADER_2);
	}
	
	public static final ExtendedShaderProgram newProgramV2F3(final GL2ES2 gl) {
		return new ExtendedShaderProgram(gl)
			.attribute("vertexLocation", 0)
			.attribute("vertexColor", 1)
			.build(VERTEX_SHADER_2, FRAGMENT_SHADER_3);
	}
	
	public static final ExtendedShaderProgram newProgramV3F3(final GL2ES2 gl) {
		return new ExtendedShaderProgram(gl)
			.attribute("vertexLocation", 0)
			.attribute("vertexColor", 1)
			.build(VERTEX_SHADER_3, FRAGMENT_SHADER_3);
	}
	
}

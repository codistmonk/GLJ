package glj2.core;

import static glj2.core.ExtendedShaderProgram.fragmentShader;
import static glj2.core.ExtendedShaderProgram.vertexShader;

import com.jogamp.opengl.util.glsl.ShaderCode;

import javax.media.opengl.GL2ES2;

import multij.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-18)
 */
public final class Shaders {
	
	private Shaders() {
		throw new IllegalInstantiationException();
	}
	
	public static final ShaderCode VERTEX_SHADER_IN_LOCATION = vertexShader(
			"#version 330\n" +
			"in vec3 vertexLocation;\n" +
			"void main() {\n" +
			"	gl_Position = vec4(vertexLocation, 1.0);\n" +
			"}\n");
	
	public static final ShaderCode VERTEX_SHADER_IN_LOCATION_COLOR = vertexShader(
			"#version 330\n" +
			"in vec3 vertexLocation;\n" +
			"in vec4 vertexColor;\n" +
			"out vec4 interpolatedColor;\n" +
			"void main() {\n" +
			"	interpolatedColor = vertexColor;\n" +
			"	gl_Position = vec4(vertexLocation, 1.0);\n" +
			"}\n");
	
	public static final ShaderCode VERTEX_SHADER_UNIFORM_TRANSFORM_IN_LOCATION_COLOR = vertexShader(
			"#version 330\n" +
			"uniform mat4 transform;\n" +
			"in vec3 vertexLocation;\n" +
			"in vec4 vertexColor;\n" +
			"out vec4 interpolatedColor;\n" +
			"void main() {\n" +
			"	interpolatedColor = vertexColor;\n" +
			"	gl_Position = transform * vec4(vertexLocation, 1.0);\n" +
			"}\n");
	
	public static final ShaderCode FRAGMENT_SHADER_CONSTANT_COLOR = fragmentShader(
			"#version 330\n" +
			"out vec4 fragmentColor;\n" +
			"void main() {\n" +
			"	fragmentColor = vec4(0.5, 0.0, 0.5, 1.0);\n" +
			"}\n");
	
	public static final ShaderCode FRAGMENT_SHADER_UNIFORM_COLOR = fragmentShader(
			"#version 330\n" +
			"uniform vec4 objectColor;\n" +
			"out vec4 fragmentColor;\n" +
			"void main() {\n" +
			"	fragmentColor = objectColor;\n" +
			"}\n");
	
	public static final ShaderCode FRAGMENT_SHADER_IN_COLOR = fragmentShader(
			"#version 330\n" +
			"in vec4 interpolatedColor;\n" +
			"out vec4 fragmentColor;\n" +
			"void main() {\n" +
			"	fragmentColor = interpolatedColor;\n" +
			"}\n");
	
	public static final ExtendedShaderProgram newProgramV1F1(final GL2ES2 gl) {
		return new ExtendedShaderProgram(gl).build(VERTEX_SHADER_IN_LOCATION, FRAGMENT_SHADER_CONSTANT_COLOR);
	}
	
	public static final ExtendedShaderProgram newProgramV1F2(final GL2ES2 gl) {
		return new ExtendedShaderProgram(gl).build(VERTEX_SHADER_IN_LOCATION, FRAGMENT_SHADER_UNIFORM_COLOR);
	}
	
	public static final ExtendedShaderProgram newProgramV2F3(final GL2ES2 gl) {
		return new ExtendedShaderProgram(gl)
			.attribute("vertexLocation", 0)
			.attribute("vertexColor", 1)
			.build(VERTEX_SHADER_IN_LOCATION_COLOR, FRAGMENT_SHADER_IN_COLOR);
	}
	
	public static final ExtendedShaderProgram newProgramV3F3(final GL2ES2 gl) {
		return new ExtendedShaderProgram(gl)
			.attribute("vertexLocation", 0)
			.attribute("vertexColor", 1)
			.build(VERTEX_SHADER_UNIFORM_TRANSFORM_IN_LOCATION_COLOR, FRAGMENT_SHADER_IN_COLOR);
	}
	
}

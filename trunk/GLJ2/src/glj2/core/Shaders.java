package glj2.core;

import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-18)
 */
public final class Shaders {
	
	private Shaders() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * {@value}.
	 */
	public static final String VERTEX_SHADER_1 =
		"#version 400\n" +
		"in vec3 vertexLocation;\n" +
		"void main() {\n" +
		"	gl_Position = vec4(vertexLocation, 1.0);\n" +
		"}\n"
	;
	
	/**
	 * {@value}.
	 */
	public static final String VERTEX_SHADER_2 =
		"#version 400\n" +
		"in vec3 vertexLocation;\n" +
		"in vec4 vertexColor;\n" +
		"out vec4 interpolatedColor;\n" +
		"void main() {\n" +
		"	interpolatedColor = vertexColor;\n" +
		"	gl_Position = vec4(vertexLocation, 1.0);\n" +
		"}\n"
	;
	
	/**
	 * {@value}.
	 */
	public static final String VERTEX_SHADER_3 =
		"#version 400\n" +
		"uniform mat4 transform;\n" +
		"in vec3 vertexLocation;\n" +
		"in vec4 vertexColor;\n" +
		"out vec4 interpolatedColor;\n" +
		"void main() {\n" +
		"	interpolatedColor = vertexColor;\n" +
		"	gl_Position = transform * vec4(vertexLocation, 1.0);\n" +
		"}\n"
	;
	
	/**
	 * {@value}.
	 */
	public static final String FRAGMENT_SHADER_1 =
		"#version 400\n" +
		"out vec4 fragmentColor;\n" +
		"void main() {\n" +
		"	fragmentColor = vec4(0.5, 0.0, 0.5, 1.0);\n" +
		"}\n"
	;
	
	/**
	 * {@value}.
	 */
	public static final String FRAGMENT_SHADER_2 =
		"#version 400\n" +
		"uniform vec4 objectColor;\n" +
		"out vec4 fragmentColor;\n" +
		"void main() {\n" +
		"	fragmentColor = objectColor;\n" +
		"}\n"
	;
	
	/**
	 * {@value}.
	 */
	public static final String FRAGMENT_SHADER_3 =
		"#version 400\n" +
		"in vec4 interpolatedColor;\n" +
		"out vec4 fragmentColor;\n" +
		"void main() {\n" +
		"	fragmentColor = interpolatedColor;\n" +
		"}\n"
	;

}

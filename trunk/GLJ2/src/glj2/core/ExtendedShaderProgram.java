package glj2.core;

import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

import java.io.PrintStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLUniformData;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public final class ExtendedShaderProgram extends ShaderProgram {
	
	private final GL2ES2 gl;
	
	private final Map<String, Integer> locations;
	
	public ExtendedShaderProgram(final GL2ES2 gl) {
		this.gl = gl;
		this.locations = new HashMap<>();
	}
	
	public final ExtendedShaderProgram attribute(final String name, final int location) {
		if (this.locations.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate attribute: " + name);
		}
		
		this.locations.put(name, location);
		
		return this;
	}
	
	public final synchronized void destroy() {
		super.destroy(this.gl);
	}
	
	public final synchronized void release() {
		super.release(this.gl);
	}
	
	public final synchronized void release(final boolean destroyShaderCode) {
		super.release(this.gl, destroyShaderCode);
	}
	
	public final synchronized boolean validateProgram(final PrintStream verboseOut) {
		return super.validateProgram(this.gl, verboseOut);
	}
	
	public final synchronized ExtendedShaderProgram useProgram(final boolean on) {
		super.useProgram(this.gl, on);
		
		return this;
	}
	
	public final int glGetUniformLocation(final String name) {
		return this.locations.compute(name, (k, v) -> v != null ?
				v : this.gl.glGetUniformLocation(this.program(), name));
	}
	
	public final void setUniform1f(final String name, final float x) {
		this.gl.glUniform1f(this.glGetUniformLocation(name), x);
	}
	
	public final void setUniform1fv(final String name, final int count, final FloatBuffer value) {
		this.gl.glUniform1fv(this.glGetUniformLocation(name), count, value);
	}
	
	public final void setUniform1fv(final String name, final int count, final float[] value,
			int valueOffset) {
		this.gl.glUniform1fv(this.glGetUniformLocation(name), count, value, valueOffset);
	}
	
	public final void setUniform1i(final String name, final int x) {
		this.gl.glUniform1i(this.glGetUniformLocation(name), x);
	}
	
	public final void setUniform1iv(final String name, final int count, final IntBuffer value) {
		this.gl.glUniform1iv(this.glGetUniformLocation(name), count, value);
	}
	
	public final void setUniform1iv(final String name, final int count, final int[] value, final int valueOffset) {
		this.gl.glUniform1iv(this.glGetUniformLocation(name), count, value, valueOffset);
	}
	
	public final void setUniform2f(final String name, final float x, final float y) {
		this.gl.glUniform2f(this.glGetUniformLocation(name), x, y);
	}
	
	public final void setUniform2fv(final String name, final int count, final FloatBuffer value) {
		this.gl.glUniform2fv(this.glGetUniformLocation(name), count, value);
	}
	
	public final void setUniform2fv(final String name, final int count, final float[] value,
			int valueOffset) {
		this.gl.glUniform2fv(this.glGetUniformLocation(name), count, value, valueOffset);
	}
	
	public final void setUniform2i(final String name, final int x, final int y) {
		this.gl.glUniform2i(this.glGetUniformLocation(name), x, y);
	}
	
	public final void setUniform2iv(final String name, final int count, final IntBuffer value) {
		this.gl.glUniform2iv(this.glGetUniformLocation(name), count, value);
	}
	
	public final void setUniform2iv(final String name, final int count, final int[] value, final int valueOffset) {
		this.gl.glUniform2iv(this.glGetUniformLocation(name), count, value, valueOffset);
	}
	
	public final void setUniform3f(final String name, final float x, final float y, final float z) {
		this.gl.glUniform3f(this.glGetUniformLocation(name), x, y, z);
	}
	
	public final void setUniform3fv(final String name, final int count, final FloatBuffer value) {
		this.gl.glUniform3fv(this.glGetUniformLocation(name), count, value);
	}
	
	public final void setUniform3fv(final String name, final int count, final float[] value,
			int valueOffset) {
		this.checkInUse();
		this.gl.glUniform3fv(this.glGetUniformLocation(name), count, value, valueOffset);
	}
	
	public final void setUniform3i(final String name, final int x, final int y, final int z) {
		this.checkInUse();
		this.gl.glUniform3i(this.glGetUniformLocation(name), x, y, z);
	}
	
	public final void setUniform3iv(final String name, final int count, final IntBuffer value) {
		this.checkInUse();
		this.gl.glUniform3iv(this.glGetUniformLocation(name), count, value);
	}
	
	public final void setUniform3iv(final String name, final int count, final int[] value, final int valueOffset) {
		this.checkInUse();
		this.gl.glUniform3iv(this.glGetUniformLocation(name), count, value, valueOffset);
	}
	
	public final void setUniform4f(final String name, final float x, final float y, final float z, final float w) {
		this.checkInUse();
		this.gl.glUniform4f(this.glGetUniformLocation(name), x, y, z, w);
	}
	
	public final void setUniform4fv(final String name, final int count, final FloatBuffer value) {
		this.checkInUse();
		this.gl.glUniform4fv(this.glGetUniformLocation(name), count, value);
	}
	
	public final void setUniform4fv(final String name, final int count, final float[] value,
			int valueOffset) {
		this.checkInUse();
		this.gl.glUniform4fv(this.glGetUniformLocation(name), count, value, valueOffset);
	}
	
	public final void setUniform4i(final String name, final int x, final int y, final int z, final int w) {
		this.checkInUse();
		this.gl.glUniform4i(this.glGetUniformLocation(name), x, y, z, w);
	}
	
	public final void setUniform4iv(final String name, final int count, final IntBuffer value) {
		this.checkInUse();
		this.gl.glUniform4iv(this.glGetUniformLocation(name), count, value);
	}
	
	public final void setUniform4iv(final String name, final int count, final int[] value, final int valueOffset) {
		this.checkInUse();
		this.gl.glUniform4iv(this.glGetUniformLocation(name), count, value, valueOffset);
	}
	
	public final void setUniformMatrix2fv(final String name, final int count,
			final boolean transpose, final FloatBuffer value) {
		this.checkInUse();
		this.gl.glUniformMatrix2fv(this.glGetUniformLocation(name), count, transpose, value);
	}
	
	public final void setUniformMatrix2fv(final String name, final int count,
			final boolean transpose, final float[] value, final int valueOffset) {
		this.checkInUse();
		this.gl.glUniformMatrix2fv(this.glGetUniformLocation(name), count, transpose, value,
				valueOffset);
	}
	
	public final void setUniformMatrix3fv(final String name, final int count,
			final boolean transpose, final FloatBuffer value) {
		this.checkInUse();
		this.gl.glUniformMatrix3fv(this.glGetUniformLocation(name), count, transpose, value);
	}
	
	public final void setUniformMatrix3fv(final String name, final int count,
			final boolean transpose, final float[] value, final int valueOffset) {
		this.checkInUse();
		this.gl.glUniformMatrix3fv(this.glGetUniformLocation(name), count, transpose, value,
				valueOffset);
	}
	
	public final void setUniformMatrix4fv(final String name, final int count,
			final boolean transpose, final FloatBuffer value) {
		this.checkInUse();
		this.gl.glUniformMatrix4fv(this.glGetUniformLocation(name), count, transpose, value);
	}
	
	public final void setUniformMatrix4fv(final String name, final int count,
			final boolean transpose, final float[] value, final int valueOffset) {
		this.checkInUse();
		this.gl.glUniformMatrix4fv(this.glGetUniformLocation(name), count, transpose, value,
				valueOffset);
	}
	
	public final void setUniform(final GLUniformData data) {
		this.checkInUse();
		this.gl.glUniform(data);
	}
	
	public final ExtendedShaderProgram build(final ShaderCode... shaderCodes) {
		this.init(this.gl);
		
		for (final ShaderCode shaderCode : shaderCodes) {
			shaderCode.compile(this.gl, System.err);
			this.add(shaderCode);
		}
		
		for (final Map.Entry<String, Integer> entry : this.locations.entrySet()) {
			this.gl.glBindAttribLocation(this.program(), entry.getValue(), entry.getKey());
		}
		
		this.link(this.gl, System.err);
		
		return this;
	}
	
	public final void checkInUse() {
		if (!this.inUse()) {
			throw new IllegalStateException();
		}
	}
	
	public static final ShaderCode vertexShader(final String code) {
		return new ShaderCode(GL2ES2.GL_VERTEX_SHADER, 1, new String[][] { { code } });
	}
	
	public static final ShaderCode fragmentShader(final String code) {
		return new ShaderCode(GL2ES2.GL_FRAGMENT_SHADER, 1, new String[][] { { code } });
	}
	
}

package glj2.core;

import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

import java.io.PrintStream;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLUniformData;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public final class ExtendedShaderProgram extends ShaderProgram {
	
	private final GL2ES2 gl;
	
	private final Map<String, Integer> locations;
	
	private final List<UniformSetter> uniformSetters;
	
	private final List<Geometry> geometries;
	
	public ExtendedShaderProgram(final GL2ES2 gl) {
		this.gl = gl;
		this.locations = new HashMap<>();
		this.uniformSetters = new ArrayList<>();
		this.geometries = new ArrayList<>();
	}
	
	public final synchronized ExtendedShaderProgram addUniformSetters(
			final Collection<? extends UniformSetter> uniformSetters) {
		this.uniformSetters.addAll(uniformSetters);
		
		return this;
	}
	
	public final synchronized ExtendedShaderProgram addUniformSetters(final UniformSetter... uniformSetters) {
		return this.addUniformSetters(Arrays.asList(uniformSetters));
	}
	
	public final synchronized ExtendedShaderProgram addGeometries(final Collection<Geometry> geometries) {
		this.geometries.addAll(geometries);
		
		return this;
	}
	
	public final synchronized void clearGeometries() {
		this.geometries.clear();
	}
	
	public final synchronized ExtendedShaderProgram addGeometries(final Geometry... geometries) {
		return this.addGeometries(Arrays.asList(geometries));
	}
	
	public final synchronized ExtendedShaderProgram attribute(final String name, final int location) {
		if (this.locations.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate attribute: " + name);
		}
		
		this.locations.put(name, location);
		
		return this;
	}
	
	public final synchronized ExtendedShaderProgram attribute(final String name) {
		return this.attribute(name, this.locations.size());
	}
	
	public final synchronized void run() {
		this.useProgram(true);
		this.geometries.forEach(geometry -> {
			this.uniformSetters.forEach(uniformSetter -> uniformSetter.applyTo(this, geometry));
			geometry.render();
		});
		this.useProgram(false);
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
	
	@Override
	protected final void finalize() throws Throwable {
		try {
			super.destroy(this.gl);
		} finally {
			super.finalize();
		}
	}
	
	public static final ShaderCode vertexShader(final String code) {
		return new ShaderCode(GL2ES2.GL_VERTEX_SHADER, 1, new String[][] { { code } });
	}
	
	public static final ShaderCode fragmentShader(final String code) {
		return new ShaderCode(GL2ES2.GL_FRAGMENT_SHADER, 1, new String[][] { { code } });
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static abstract interface UniformSetter extends Serializable {
		
		public abstract void applyTo(ExtendedShaderProgram program, Geometry geometry);
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static abstract class AbstractUniformSetter implements UniformSetter {
		
		private final String uniformName;
		
		public AbstractUniformSetter(final String uniformName) {
			this.uniformName = uniformName;
		}
		
		public final String getUniformName() {
			return this.uniformName;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -3523752734533063594L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform1Float extends AbstractUniformSetter {
		
		private final float x;
		
		public Uniform1Float(final String uniformName, final float x) {
			super(uniformName);
			this.x = x;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform1f(this.getUniformName(), this.x);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4531602689503391339L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform1Floats extends AbstractUniformSetter {
		
		private final int count;
		
		private final float[] value;
		
		private final int offset;
		
		public Uniform1Floats(final String uniformName, final int count, final float[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform1fv(this.getUniformName(), this.count, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -7383188964928061148L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform1FloatBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final FloatBuffer value;
		
		public Uniform1FloatBuffer(final String uniformName, final int count, final FloatBuffer value) {
			super(uniformName);
			this.count = count;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform1fv(this.getUniformName(), this.count, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 5769376835584423083L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform1Int extends AbstractUniformSetter {
		
		private final int x;
		
		public Uniform1Int(final String uniformName, final int x) {
			super(uniformName);
			this.x = x;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform1i(this.getUniformName(), this.x);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1894578739584432142L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform1Ints extends AbstractUniformSetter {
		
		private final int count;
		
		private final int[] value;
		
		private final int offset;
		
		public Uniform1Ints(final String uniformName, final int count, final int[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform1iv(this.getUniformName(), this.count, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 7159651052804554691L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform1IntBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final IntBuffer value;
		
		public Uniform1IntBuffer(final String uniformName, final int count, final IntBuffer value) {
			super(uniformName);
			this.count = count;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform1iv(this.getUniformName(), this.count, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8627081221981714669L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform2Float extends AbstractUniformSetter {
		
		private final float x, y;
		
		public Uniform2Float(final String uniformName, final float x, final float y) {
			super(uniformName);
			this.x = x;
			this.y = y;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform2f(this.getUniformName(), this.x, this.y);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8081317250901047804L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform2Floats extends AbstractUniformSetter {
		
		private final int count;
		
		private final float[] value;
		
		private final int offset;
		
		public Uniform2Floats(final String uniformName, final int count, final float[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform2fv(this.getUniformName(), this.count, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -4644764517179636862L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform2FloatBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final FloatBuffer value;
		
		public Uniform2FloatBuffer(final String uniformName, final int count, final FloatBuffer value) {
			super(uniformName);
			this.count = count;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform2fv(this.getUniformName(), this.count, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -3479858774491261904L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform2Int extends AbstractUniformSetter {
		
		private final int x, y;
		
		public Uniform2Int(final String uniformName, final int x, final int y) {
			super(uniformName);
			this.x = x;
			this.y = y;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform2i(this.getUniformName(), this.x, this.y);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1786569512995794913L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform2Ints extends AbstractUniformSetter {
		
		private final int count;
		
		private final int[] value;
		
		private final int offset;
		
		public Uniform2Ints(final String uniformName, final int count, final int[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform2iv(this.getUniformName(), this.count, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1725070986458573688L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform2IntBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final IntBuffer value;
		
		public Uniform2IntBuffer(final String uniformName, final int count, final IntBuffer value) {
			super(uniformName);
			this.count = count;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform2iv(this.getUniformName(), this.count, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 8150477924407325111L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform3Float extends AbstractUniformSetter {
		
		private final float x, y, z;
		
		public Uniform3Float(final String uniformName, final float x, final float y, final float z) {
			super(uniformName);
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform3f(this.getUniformName(), this.x, this.y, this.z);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -371046934157626378L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform3Floats extends AbstractUniformSetter {
		
		private final int count;
		
		private final float[] value;
		
		private final int offset;
		
		public Uniform3Floats(final String uniformName, final int count, final float[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform3fv(this.getUniformName(), this.count, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 3445321867109676115L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform3FloatBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final FloatBuffer value;
		
		public Uniform3FloatBuffer(final String uniformName, final int count, final FloatBuffer value) {
			super(uniformName);
			this.count = count;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform3fv(this.getUniformName(), this.count, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 977825581255689865L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform3Int extends AbstractUniformSetter {
		
		private final int x, y, z;
		
		public Uniform3Int(final String uniformName, final int x, final int y, final int z) {
			super(uniformName);
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform3i(this.getUniformName(), this.x, this.y, this.z);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8749090025064315970L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform3Ints extends AbstractUniformSetter {
		
		private final int count;
		
		private final int[] value;
		
		private final int offset;
		
		public Uniform3Ints(final String uniformName, final int count, final int[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform3iv(this.getUniformName(), this.count, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 6415204820796124482L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform3IntBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final IntBuffer value;
		
		public Uniform3IntBuffer(final String uniformName, final int count, final IntBuffer value) {
			super(uniformName);
			this.count = count;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform3iv(this.getUniformName(), this.count, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -6640028372332411513L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform4Float extends AbstractUniformSetter {
		
		private final float x, y, z, w;
		
		public Uniform4Float(final String uniformName, final float x, final float y, final float z, final float w) {
			super(uniformName);
			this.x = x;
			this.y = y;
			this.z = z;
			this.w = w;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform4f(this.getUniformName(), this.x, this.y, this.z, this.w);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 8045266516490346123L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform4Floats extends AbstractUniformSetter {
		
		private final int count;
		
		private final float[] value;
		
		private final int offset;
		
		public Uniform4Floats(final String uniformName, final int count, final float[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform4fv(this.getUniformName(), this.count, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -5752369756832613174L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform4FloatBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final FloatBuffer value;
		
		public Uniform4FloatBuffer(final String uniformName, final int count, final FloatBuffer value) {
			super(uniformName);
			this.count = count;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform4fv(this.getUniformName(), this.count, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4483557406840533930L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform4Int extends AbstractUniformSetter {
		
		private final int x, y, z, w;
		
		public Uniform4Int(final String uniformName, final int x, final int y, final int z, final int w) {
			super(uniformName);
			this.x = x;
			this.y = y;
			this.z = z;
			this.w = w;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform4i(this.getUniformName(), this.x, this.y, this.z, this.w);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1908095885260636561L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform4Ints extends AbstractUniformSetter {
		
		private final int count;
		
		private final int[] value;
		
		private final int offset;
		
		public Uniform4Ints(final String uniformName, final int count, final int[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform4iv(this.getUniformName(), this.count, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 5879765211533868810L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class Uniform4IntBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final IntBuffer value;
		
		public Uniform4IntBuffer(final String uniformName, final int count, final IntBuffer value) {
			super(uniformName);
			this.count = count;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniform4iv(this.getUniformName(), this.count, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 5127106728028567954L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class UniformMatrix2Floats extends AbstractUniformSetter {
		
		private final int count;
		
		private final boolean transpose;
		
		private final float[] value;
		
		private final int offset;
		
		public UniformMatrix2Floats(final String uniformName, final int count, final boolean transpose, final float[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.transpose = transpose;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniformMatrix2fv(this.getUniformName(), this.count, this.transpose, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4279952097992358253L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class UniformMatrix2FloatBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final boolean transpose;
		
		private final FloatBuffer value;
		
		public UniformMatrix2FloatBuffer(final String uniformName, final int count, final boolean transpose, final FloatBuffer value) {
			super(uniformName);
			this.count = count;
			this.transpose = transpose;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniformMatrix2fv(this.getUniformName(), this.count, this.transpose, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 3094940695916683101L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class UniformMatrix3Floats extends AbstractUniformSetter {
		
		private final int count;
		
		private final boolean transpose;
		
		private final float[] value;
		
		private final int offset;
		
		public UniformMatrix3Floats(final String uniformName, final int count, final boolean transpose, final float[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.transpose = transpose;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniformMatrix3fv(this.getUniformName(), this.count, this.transpose, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 7472343005778252075L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class UniformMatrix3FloatBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final boolean transpose;
		
		private final FloatBuffer value;
		
		public UniformMatrix3FloatBuffer(final String uniformName, final int count, final boolean transpose, final FloatBuffer value) {
			super(uniformName);
			this.count = count;
			this.transpose = transpose;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniformMatrix3fv(this.getUniformName(), this.count, this.transpose, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 592415301437347710L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class UniformMatrix4Floats extends AbstractUniformSetter {
		
		private final int count;
		
		private final boolean transpose;
		
		private final float[] value;
		
		private final int offset;
		
		public UniformMatrix4Floats(final String uniformName, final int count, final boolean transpose, final float[] value, final int offset) {
			super(uniformName);
			this.count = count;
			this.transpose = transpose;
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniformMatrix4fv(this.getUniformName(), this.count, this.transpose, this.value, this.offset);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4039654756718444118L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-10-18)
	 */
	public static final class UniformMatrix4FloatBuffer extends AbstractUniformSetter {
		
		private final int count;
		
		private final boolean transpose;
		
		private final FloatBuffer value;
		
		public UniformMatrix4FloatBuffer(final String uniformName, final int count, final boolean transpose, final FloatBuffer value) {
			super(uniformName);
			this.count = count;
			this.transpose = transpose;
			this.value = value;
		}
		
		@Override
		public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
			program.setUniformMatrix4fv(this.getUniformName(), this.count, this.transpose, this.value);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 6275872226419231487L;
		
	}
	
}

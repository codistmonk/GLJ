package glj2.core;

import static net.sourceforge.aprog.tools.Tools.unchecked;

import java.io.Serializable;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;

import com.jogamp.common.nio.Buffers;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public final class VBO implements Serializable {
	
	private final GL2ES2 gl;
	
	private final IntBuffer vbo;
	
	private final int target;
	
	public VBO(final GL2ES2 gl, final Buffer data, final int usage) {
		this(gl, GL.GL_ARRAY_BUFFER, data, usage);
	}
	
	public VBO(final GL2ES2 gl, final int target, final Buffer data, final int usage) {
		this.gl = gl;
		this.vbo = Buffers.newDirectIntBuffer(1);
		this.target = target;
		
		gl.glGenBuffers(1, this.vbo);
		this.bind();
		gl.glBufferData(target, data.capacity() * getDatumSize(data), data, usage);
	}
	
	public final int getTarget() {
		return this.target;
	}
	
	public final VBO bind() {
		this.gl.glBindBuffer(this.getTarget(), this.vbo.get(0));
		
		return this;
	}
	
	public final void destroy() {
		this.gl.glDeleteBuffers(1, this.vbo);
	}
	
	public static final int getDatumSize(final Buffer buffer) {
		final String bufferClassName = buffer.getClass().getName();
		final Matcher matcher = PATTERN.matcher(bufferClassName);
		
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid buffer type: " + bufferClassName);
		}
		
		String datumClassName = matcher.group(2);
		
		if ("Int".equals(datumClassName)) {
			datumClassName = "Integer";
		}
		
		try {
			return Class.forName("java.lang." + datumClassName).getField("BYTES").getInt(null);
		} catch (final Exception exception) {
			throw unchecked(exception);
		}
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -4781678108773022509L;
	
	private static final Pattern PATTERN = Pattern.compile("(.*Direct)?(.+)Buffer.*");
	
}
package glj2.core;

import com.jogamp.common.util.cache.TempJarCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import jogamp.common.os.PlatformPropsImpl;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2013-01-12)
 */
public final class GLJTools {
	
	private GLJTools() {
		throw new IllegalInstantiationException();
	}
	
	public static final Point3f ORIGIN = new Point3f();
	
	public static final Vector3f ZERO = new Vector3f();
	
	public static final Vector3f UNIT_X = new Vector3f(+1.0F, +0.0F, +0.0F);
	
	public static final Vector3f UNIT_Y = new Vector3f(+0.0F, +1.0F, +0.0F);
	
	public static final Vector3f UNIT_Z = new Vector3f(+0.0F, +0.0F, +1.0F);
	
	/**
	 * {@value}.
	 */
	public static final int FLOAT_BYTE_COUNT = Float.SIZE / 8;
	
	/**
	 * {@value}.
	 */
	public static final int INT_BYTE_COUNT = Integer.SIZE / 8;
	
	public static final void unzip(final ZipInputStream input, final String outputRoot) {
		try {
			ZipEntry entry = input.getNextEntry();
			
			while (entry != null) {
				Tools.debugPrint(entry.getName());
				
				final File file = new File(outputRoot, entry.getName());
				
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					try (final OutputStream output = new FileOutputStream(file)) {
						Tools.write(input, output);
					}
				}
				
				entry = input.getNextEntry();
			}
		} catch (final IOException exception) {
			throw Tools.unchecked(exception);
		}
	}
	
	public static final GLCanvas newGLCanvas() {
		loadNativeLibraries();
		
		return new GLCanvas(new GLCapabilities(GLProfile.getMaxProgrammableCore(true)));
	}
	
	public static final void loadNativeLibraries() {
		if (!TempJarCache.isInitialized()) {
			TempJarCache.initSingleton();
		}
		
		final File librariesDirectory = TempJarCache.getTempFileCache().getTempDir();
		final File applicationFile = Tools.getApplicationFile();
		
		Tools.debugPrint(librariesDirectory);
		Tools.debugPrint(applicationFile);
		
		if (applicationFile.isFile()) {
			final String nativeSuffix = "-natives-" + PlatformPropsImpl.os_and_arch + ".jar";
			
			Tools.debugPrint("Native suffix:", nativeSuffix);
			
			try (final JarFile jar = new JarFile(applicationFile)) {
				for (final JarEntry entry : Tools.iterable(jar.entries())) {
					final String entryName = new File(entry.getName()).getName();
					
					if (entryName.endsWith(nativeSuffix)) {
						final File outputFile = new File(librariesDirectory, entryName);
						
						if (!outputFile.exists()) {
							Tools.debugPrint("Unpacking:", entryName);
							
							try (final InputStream input = jar.getInputStream(entry);
									final OutputStream output = new FileOutputStream(outputFile)) {
								Tools.write(input, output);
							}
							
							try {
								final URI jarURI = new URI("jar:" + outputFile.toURI() + "!/");
								Tools.debugPrint(jarURI);
								Tools.debugPrint(TempJarCache.addNativeLibs(GLProfile.class, jarURI, null));
							} catch (final Exception exception) {
								exception.printStackTrace();
							}
						}
					}
				}
			} catch (final IOException exception) {
				exception.printStackTrace();
			}
		}
	}
	
	/**
	 * @param camera
	 * <br>Not null
	 * @return
	 * <br>Maybe null
	 * <br>New
	 */
	public static Vector3f getCameraDirection(final Camera camera) {
		final Vector3f result = new Vector3f(getCameraLocation(camera));
		
		result.normalize();
		
		return result;
	}
	
	/**
	 * @param camera
	 * <br>Not null
	 * @return
	 * <br>Maybe null
	 * <br>New
	 */
	public static Point3f getCameraLocation(final Camera camera) {
		final Point4f p = new Point4f(+0.0F, +0.0F, +0.0F, +1.0F);
		final Matrix4f m = new Matrix4f();
		
		m.set(camera.getView());
		m.invert();
		m.transform(p);
		
		if (p.w == 0.0F) {
			return null;
		}
		
		return new Point3f(p.x / p.w, p.y / p.w, p.z / p.w);
	}
	
	/**
	 * @param matrix
	 * <br>Not null
	 * <br>Input-output
	 * @param left
	 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
	 * @param right
	 * <br>Range: <code>]left .. Float.MAX_VALUE]</code>
	 * @param bottom
	 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
	 * @param top
	 * <br>Range: <code>]bottom .. Float.MAX_VALUE]</code>
	 * @param zNear
	 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
	 * @param zFar
	 * <br>Range: <code>]zNear .. Float.MAX_VALUE]</code>
	 */
	public static final void setOrthographicProjectionMatrix(final Matrix4f matrix, final float left, final float right, final float bottom, final float top, final float zNear, final float zFar) {
		matrix.setIdentity();
		
		matrix.m00 = +2.0F / (right - left);
		matrix.m11 = +2.0F / (top - bottom);
		matrix.m22 = -2.0F / (zFar - zNear);
		
		matrix.m03 = - (right + left) / (right - left);
		matrix.m13 = - (top + bottom) / (top - bottom);
		matrix.m23 = - (zFar + zNear) / (zFar - zNear);
	}
	
	/**
	 * @param matrix
	 * <br>Not null
	 * <br>Input-output
	 * @param verticalFieldOfVisionInRadians
	 * <br>Range: <code>]+0.0F .. +Math.PI[</code> 
	 * @param viewportAspectRatio
	 * <br>Range: <code>]+0.0F .. +Float.MAX_VALUE]</code> 
	 * @param zNear
	 * <br>Range: <code>[Float.MIN_VALUE .. Float.MAX_VALUE]</code>
	 * @param zFar
	 * <br>Range: <code>]zNear .. Float.MAX_VALUE]</code>
	 */
	public static final void setPerspectiveProjectionMatrix(final Matrix4f matrix, final float verticalFieldOfVisionInRadians, final float viewportAspectRatio, final float zNear, final float zFar) {
		final float range = (float) (Math.tan(verticalFieldOfVisionInRadians / 2.0F) * zNear);	
		final float left = -range * viewportAspectRatio;
		final float right = +range * viewportAspectRatio;
		final float bottom = -range;
		final float top = +range;
		
		matrix.setZero();
		
		matrix.m00 = +2.0F * zNear / (right - left);
		matrix.m11 = +2.0F * zNear / (top - bottom);
		matrix.m22 = - (zFar + zNear) / (zFar - zNear);
		
		matrix.m32 = -1.0F;
		matrix.m23 = -2.0F * zFar * zNear / (zFar - zNear);
	}
	
	/**
	 * @param matrix
	 * <br>Not null
	 * <br>Input-output
	 * @param eye
	 * <br>Not null
	 * @param center
	 * <br>Not null
	 * @param up
	 * <br>Not null
	 */
	public static final void setLookAtMatrix(final Matrix4f matrix, final Tuple3f eye, final Tuple3f center, final Vector3f up) {
		final Vector3f f = new Vector3f();
		final Vector3f u = new Vector3f();
		final Vector3f s = new Vector3f();
		
		f.sub(center, eye);
		f.normalize();
		
		u.normalize(up);
		
		s.cross(f, u);
		s.normalize();
		
		u.cross(s, f);
		
		matrix.setIdentity();
		
		matrix.m00 = s.x;
		matrix.m01 = s.y;
		matrix.m02 = s.z;
		
		matrix.m10 = u.x;
		matrix.m11 = u.y;
		matrix.m12 = u.z;
		
		matrix.m20 = -f.x;
		matrix.m21 = -f.y;
		matrix.m22 = -f.z;
		
		f.negate(eye);
		
		localTranslate(matrix, f);
	}
	
	/**
	 * @param matrix
	 * <br>Not null
	 * <br>Input-output
	 * @param vector
	 * <br>Not null
	 */
	public static final void localTranslate(final Matrix4f matrix, final Vector3f vector) {
		final Matrix4f localTransform = new Matrix4f();
		
		localTransform.setIdentity();
		localTransform.setTranslation(vector);
		
		matrix.mul(localTransform);
	}
	
}

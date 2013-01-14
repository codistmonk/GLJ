package glj;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author codistmonk (creation 2012-02-07)
 */
public final class LauncherTools {
	
	private LauncherTools() {
		// NOP
	}
	
	/**
	 * @param vmArguments
	 * <br>Maybe null
	 * @param mainClass
	 * <br>Not null
	 * @param arguments
	 * <br>Not null
	 * <br>Length range: <code>[0 .. Integer.MAX_VALUE]</code>
	 */
	public static final void launch(final String[] vmArguments, final Class<?> mainClass, final String[] arguments) {
		try {
			if (vmArguments != null) {
				throw new RuntimeException("New VM needed for arguments " + Arrays.toString(vmArguments));
			}
			
			mainClass.getMethod("main", new Class<?>[] { String[].class }).invoke(null, (Object) arguments);
		} catch (final Throwable problem1) {
			problem1.printStackTrace();
			
			final File mainClassRoot = getClassRoot(mainClass);
			
			try {
				launch(vmArguments, createClassPath(mainClassRoot), mainClass.getName(), arguments);
			} catch (final Throwable problem2) {
				problem2.printStackTrace();
				
				try {
					launch(vmArguments, createClassPath(getClassRoot(Class.forName("com.jogamp.opengl.JoglVersion")))
							+ File.pathSeparator + mainClassRoot, mainClass.getName(), arguments);
				} catch (final Throwable problem3) {
					throw new RuntimeException(problem3);
				}
			}
		}
	}
	
	/**
	 * @param vmArguments
	 * <br>Maybe null
	 * <br>Length range: <code>[0 .. Integer.MAX_VALUE]</code>
	 * @param classPath
	 * <br>Not null
	 * @param mainClassName
	 * <br>Not null
	 * @param arguments
	 * <br>Not null
	 * <br>Length range: <code>[0 .. Integer.MAX_VALUE]</code>
	 */
	public static final void launch(final String[] vmArguments, final String classPath, final String mainClassName, final String[] arguments) {
		try {
			final String[] command = append(new String[] { "java" }, append(append(
					vmArguments != null ? vmArguments : new String[0]
					, "-cp", classPath, mainClassName), arguments));
			
			System.out.println(Arrays.toString(command));
			
			final Process process = Runtime.getRuntime().exec(command);
			
			redirectOutputsToConsole(process);
		} catch (final IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}
	
	/**
	 * @param applicationFile
	 * <br>Not null
	 * @return
	 * <br>Not null
	 * <br>New
	 */
	public static final String createClassPath(final File applicationFile) {
		try {
			final StringBuilder classPathBuilder = new StringBuilder().append(".");
			
			System.out.println(applicationFile);
			
			final JarFile jarFile = new JarFile(applicationFile);
			final File temporaryDirectory = newTemporaryDirectory();
			
			for (final Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				final JarEntry entry = entries.nextElement();
				
				if (entry.getName().toLowerCase().startsWith("lib/") && entry.getName().toLowerCase().endsWith(".jar")) {
					System.out.println("jar:file://" + applicationFile + "!/" + entry.getName());
					
					final File temporaryJar = newTemporary(temporaryDirectory, getName(entry.getName()), jarFile.getInputStream(entry));
					
					System.out.println(temporaryJar);
					
					if (!temporaryJar.getPath().contains("natives")) {
						classPathBuilder.append(File.pathSeparator).append(temporaryJar.getPath());
					}
				}
			}
			
			classPathBuilder.append(File.pathSeparator).append(applicationFile.getPath());
			
			return classPathBuilder.toString();
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	/**
	 * @param pathName
	 * <br>Not null
	 * @return
	 * <br>Not null
	 * <br>New
	 */
	public static final String getName(final String pathName) {
		return pathName.substring(pathName.lastIndexOf("/") + 1);
	}
	
	/**
	 * @param parentDirectory
	 * <br>Not null
	 * @param name
	 * <br>Not null
	 * @param data
	 * <br>Not null
	 * <br>Input-output
	 * @return
	 * <br>Not null
	 * <br>New
	 * @throws IOException If an error occurs
	 */
	public static final File newTemporary(final File parentDirectory, final String name, final InputStream data) throws IOException {
		final File result = new File(parentDirectory, name);
		
		result.deleteOnExit();
		
		final byte[] buffer = new byte[1024];
		final OutputStream output = new FileOutputStream(result);
		int available;
		
		do {
			output.write(buffer, 0, available = Math.max(0, data.read(buffer)));
		} while (0 < available);
		
		return result;
	}
	
	/**
	 * @return
	 * <br>Not null
	 * <br>New
	 * @throws IOException If an error occurs
	 */
	public static final File newTemporaryDirectory() throws IOException {
		final File result = File.createTempFile("tmp", "");
		
		result.delete();
		result.mkdir();
		result.deleteOnExit();
		
		return result;
	}
    
    /**
     * @param cls
     * <br>Not null
     * @return
     * <br>Not null
     */
    public static final File getClassRoot(final Class<?> cls) {
        try {
            return new File(getClassRootURL(cls).toURI());
        } catch (final URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    /**
     * @param cls
     * <br>Not null
     * @return
     * <br>Not null
     */
    public static final URL getClassRootURL(final Class<?> cls) {
        return cls.getProtectionDomain().getCodeSource().getLocation();
    }

    /**
     *
     * @param process
     * <br>Not null
     */
    public static final void redirectOutputsToConsole(final Process process) {
        pipe(process.getErrorStream(), System.err);
        pipe(process.getInputStream(), System.out);
    }

    /**
     * Creates and starts a new thread that scans {@code input} (with a {@link Scanner}) and writes each line to {@code output}.
     * 
     * @param input
     * <br>Not null
     * <br>Input-output
     * @param output
     * <br>Not null
     * <br>Input-output
     * @return 
     * <br>Not null
     */
    public static final Thread pipe(final InputStream input, final PrintStream output) {
        final Thread result = new Thread() {

            @Override
            public final void run() {
                final Scanner errorScanner = new Scanner(input);

                try {
                    while (errorScanner.hasNext()) {
                        output.println(errorScanner.nextLine());
                    }
                } catch (final Exception exception) {
                    // NOP
                }
            }

        };
        
        result.start();
        
        return result;
    }
    
    /**
     * @param <T> The common type of the elements
     * @param array
     * <br>Not null
     * @param moreElements
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    public static final <T> T[] append(final T[] array, final T... moreElements) {
        @SuppressWarnings("unchecked")
        final T[] result = (T[]) Array.newInstance(
                array.getClass().getComponentType(), array.length + moreElements.length);

        System.arraycopy(array, 0, result, 0, array.length);
        System.arraycopy(moreElements, 0, result, array.length, moreElements.length);

        return result;
    }
	
}

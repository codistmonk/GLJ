package glj2.core;

import static glj2.core.GLJTools.newGLCanvas;

import java.awt.Dimension;
import java.io.Serializable;

import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import multij.swing.SwingTools;

/**
 * @author codistmonk (creation 2014-10-17)
 */
public final class GLSwingContext implements Serializable {
	
	private final GLCanvas canvas;
	
	private final JFrame frame;
	
	public GLSwingContext() {
		this.canvas = newGLCanvas();
		this.frame = new JFrame();
		
		this.canvas.setPreferredSize(new Dimension(640, 480));
		
		this.frame.add(this.canvas);
		this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	public final GLCanvas getCanvas() {
		return this.canvas;
	}
	
	public final JFrame getFrame() {
		return this.frame;
	}
	
	public final GLSwingContext show() {
		SwingUtilities.invokeLater(() -> { SwingTools.packAndCenter(this.getFrame()).setVisible(true); });
		
		return this;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -7736071389274273376L;
	
}
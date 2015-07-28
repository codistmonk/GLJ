package glj2.demos;

import java.io.Serializable;

import multij.tools.NanoTicToc;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public final class FrameRate implements Serializable {
	
	private final NanoTicToc nanoTimer;
	
	private final long refreshPeriod;
	
	private long frameRate;
	
	private long frameCount;
	
	public FrameRate(final long refreshPeriodInNanoseconds) {
		this.nanoTimer = new NanoTicToc();
		this.refreshPeriod = refreshPeriodInNanoseconds;
	}
	
	public final boolean ping() {
		final long interval = this.nanoTimer.toc();
		
		++this.frameCount;
		
		if (this.refreshPeriod <= interval) {
			this.frameRate = 1_000_000_000L * this.frameCount / this.refreshPeriod;
			this.frameCount = 0L;
			this.nanoTimer.tic();
			
			return true;
		}
		
		return false;
	}
	
	public final long get() {
		return this.frameRate;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 7014982921331613925L;
	
}
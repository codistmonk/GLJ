package glj2.demos;

import static glj2.core.ExtendedShaderProgram.fragmentShader;
import static glj2.core.ExtendedShaderProgram.vertexShader;
import static glj2.core.Shaders.*;
import static glj2.std.Solid.DRAWING_MODE;
import static glj2.std.Solid.STRIDE;
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;
import static multij.tools.Manifold.opposite;
import static multij.tools.Manifold.Traversor.EDGE;
import static multij.tools.Manifold.Traversor.FACE;
import static multij.tools.MathTools.lelt;
import static multij.tools.MathTools.square;
import static multij.tools.Tools.check;
import static multij.tools.Tools.debugError;
import static multij.tools.Tools.debugPrint;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.glsl.ShaderCode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;

import glj2.std.Mesh;
import glj2.std.Solid;
import glj2.std.UniformMPV;
import glj2.core.ExtendedShaderProgram;
import glj2.core.ExtendedShaderProgram.Uniform1Int;
import glj2.core.Scene.Picking.Pick;
import glj2.demos.Demo1.DefaultScene;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLRunnable;
import javax.media.opengl.awt.GLCanvas;
import javax.vecmath.Vector3d;

import multij.primitivelists.IntList;
import multij.swing.MouseHandler;
import multij.swing.SwingTools;
import multij.tools.IllegalInstantiationException;
import multij.tools.Manifold;

/**
 * @author codistmonk (creation 2018-06-28)
 */
public final class Demo5 {
	
	private Demo5() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String... commandLineArguments) {
		SwingTools.useSystemLookAndFeel();
		
		new DefaultScene() {
			
			{
				this.getOrbiter().setEventFilter(e -> e instanceof MouseWheelEvent || isRightMouseButton(e));
				
				final GLCanvas canvas = this.getContext().getCanvas();
				
				new MouseHandler() {
					
					private Solid objectUnderMouse;
					
					private int faceIndexUnderMouse;
					
					private final Point mouse = new Point();
					
					private final Pick pick = getPicking().new Pick();
					
					@Override
					public final void mousePressed(final MouseEvent e) {
						if (isLeftMouseButton(e)) {
							this.mouse.setLocation(e.getPoint());
							this.pick.set(getPicking().pick(e.getX(), e.getY()));
						}
					}
					
					@Override
					public final void mouseReleased(final MouseEvent e) {
						if (isLeftMouseButton(e)) {
							this.mouse.setLocation(-1, -1);
							this.pick.set(0, null, 0F, 0F);
						}
					}
					
					@Override
					public final void mouseDragged(final MouseEvent e) {
						if (!isLeftMouseButton(e)) {
							return;
						}
						
						final Pick pick = getPicking().pick(e.getX(), e.getY());
						final Solid object = this.pick.getObject();
						
						if (pick != null && pick.getEntry() != null && object == pick.getEntry().getValue()) {
							final Mesh mesh = object.getAttribute(Solid.FACES_GEOMETRY);
							final BufferedImage image = mesh.getImage();
							final int w = image.getWidth();
							final int h = image.getHeight();
							final float u1 = this.pick.getU();
							final float v1 = this.pick.getV();
							final float u2 = pick.getU();
							final float v2 = pick.getV();
							
							final AffineTransform t = computeTransition(u1, v1, u2, v2);
							final Rectangle clip13 = new Rectangle((int) (u1 * 3F) * w / 3, (int) (v1 * 2F) * h / 2, w / 3, h / 2);
							final Rectangle clip24 = new Rectangle((int) (u2 * 3F) * w / 3, (int) (v2 * 2F) * h / 2, w / 3, h / 2);
							
							final Point2D.Float uv1 = new Point2D.Float(u1, v1);
							final Point2D.Float uv2 = new Point2D.Float(u2, v2);
							final Point2D.Float uv3 = (Point2D.Float) uv2.clone();
							final Point2D.Float uv4 = (Point2D.Float) uv1.clone();
							
							{
								t.transform(uv2, uv3);
								
								try {
									t.inverseTransform(uv1, uv4);
								} catch (final NoninvertibleTransformException exception) {
									exception.printStackTrace();
								}
							}
							
							final float u3 = uv3.x;
							final float v3 = uv3.y;
							final float u4 = uv4.x;
							final float v4 = uv4.y;
							
							final int x1 = (int) (u1 * w);
							final int y1 = (int) (v1 * h);
							final int x2 = (int) (u2 * w);
							final int y2 = (int) (v2 * h);
							final int x3 = (int) (u3 * w);
							final int y3 = (int) (v3 * h);
							final int x4 = (int) (u4 * w);
							final int y4 = (int) (v4 * h);
							
							{
								final Graphics2D gr = image.createGraphics();
								
								gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
								gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
								gr.setStroke(new BasicStroke(9F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
								gr.setColor(Color.BLACK);
								
								gr.setClip(clip13);
								gr.drawLine(x1, y1, x3, y3);
								gr.setClip(clip24);
								gr.drawLine(x2, y2, x4, y4);
								
								gr.dispose();
							}
							
							this.updateImage(mesh);
						}
						
						this.pick.set(pick);
						this.mouse.setLocation(e.getPoint());
					}
					
					private final AffineTransform computeTransition(final float u1, final float v1, final float u2, final float v2) {
						final int i1 = (int) (u1 * 3F);
						final int j1 = (int) (v1 * 2F);
						final int i2 = (int) (u2 * 3F);
						final int j2 = (int) (v2 * 2F);
						
						check(lelt(0, i1, 3) && lelt(0, j1, 2) && lelt(0, i2, 3) && lelt(0, j2, 2));
						
						final AffineTransform result = new AffineTransform();
						
						result.scale(1.0 / 3.0, 1.0 / 2.0);
						
						if (i1 == 0) {
							if (j1 == 0) {
								if (i2 == 0) {
									if (j2 == 0) {
										// (0 0) -> (0 0)
									} else {
										// (0 0) -> (0 1)
										result.rotate(PI, 1.0 / 2.0, 1.0 + 1.0 / 2.0);
									}
								} else if (i2 == 1) {
									if (j2 == 0) {
										// (0 0) -> (1 0)
									} else {
										// (0 0) -> (1 1)
										result.translate(-2.0, -1.0);
										result.rotate(-PI / 2.0, 1.0 + 1.0 / 2.0, 1.0 + 1.0 / 2.0);
									}
								} else {
									if (j2 == 0) {
										// (0 0) -> (2 0)
										check(false);
									} else {
										// (0 0) -> (2 1)
										result.translate(-2.0, -2.0);
									}
								}
							} else {
								if (i2 == 0) {
									if (j2 == 0) {
										// (0 1) -> (0 0)
										result.translate(0.0, 2.0);
										result.rotate(PI, 1.0 / 2.0, 1.0 / 2.0);
									} else {
										// (0 1) -> (0 1)
									}
								} else if (i2 == 1) {
									if (j2 == 0) {
										// (0 1) -> (1 0)
										result.translate(-2.0, 1.0);
										result.rotate(-PI / 2.0, 1.0 + 1.0 / 2.0, 1.0 / 2.0);
									} else {
										// (0 1) -> (1 1)
									}
								} else {
									if (j2 == 0) {
										// (0 1) -> (2 0)
										result.translate(-2.0, 0.0);
									} else {
										// (0 1) -> (2 1)
										check(false);
									}
								}
							}
						} else if (i1 == 1) {
							if (j1 == 0) {
								if (i2 == 0) {
									if (j2 == 0) {
										// (1 0) -> (0 0)
									} else {
										// (1 0) -> (0 1)
										result.translate(1.0, 0.0);
										result.rotate(PI / 2.0, 1.0 / 2.0, 1.0 + 1.0 / 2.0);
									}
								} else if (i2 == 1) {
									if (j2 == 0) {
										// (1 0) -> (1 0)
									} else {
										// (1 0) -> (1 1)
										check(false);
									}
								} else {
									if (j2 == 0) {
										// (1 0) -> (2 0)
									} else {
										// (1 0) -> (2 1)
										result.translate(-1.0, -2.0);
										result.rotate(PI / 2.0, 2.0 + 1.0 / 2.0, 1.0 + 1.0 / 2.0);
									}
								}
							} else {
								if (i2 == 0) {
									if (j2 == 0) {
										// (1 1) -> (0 0)
										result.translate(1.0, 2.0);
										result.rotate(PI / 2.0, 1.0 / 2.0, 1.0 / 2.0);
									} else {
										// (1 1) -> (0 1)
									}
								} else if (i2 == 1) {
									if (j2 == 0) {
										// (1 1) -> (1 0)
										check(false);
									} else {
										// (1 1) -> (1 1)
									}
								} else {
									if (j2 == 0) {
										// (1 1) -> (2 0)
										result.translate(-1.0, 0.0);
										result.rotate(PI / 2.0, 2.0 + 1.0 / 2.0, 1.0 / 2.0);
									} else {
										// (1 1) -> (2 1)
									}
								}
							}
						} else {
							if (j1 == 0) {
								if (i2 == 0) {
									if (j2 == 0) {
										// (2 0) -> (0 0)
										check(false);
									} else {
										// (2 0) -> (0 1)
										result.translate(2.0, 0.0);
									}
								} else if (i2 == 1) {
									if (j2 == 0) {
										// (2 0) -> (1 0)
									} else {
										// (2 0) -> (1 1)
										result.translate(2.0, -1.0);
										result.rotate(-PI / 2.0, 1.0 + 1.0 / 2.0, 1.0 + 1.0 / 2.0);
									}
								} else {
									if (j2 == 0) {
										// (2 0) -> (2 0)
									} else {
										// (2 0) -> (2 1)
										result.translate(0.0, -2.0);
										result.rotate(PI, 2.0 + 1.0 / 2.0, 1.0 + 1.0 / 2.0);
									}
								}
							} else {
								if (i2 == 0) {
									if (j2 == 0) {
										// (2 1) -> (0 0)
										result.translate(2.0, 2.0);
									} else {
										// (2 1) -> (0 1)
										check(false);
									}
								} else if (i2 == 1) {
									if (j2 == 0) {
										// (2 1) -> (1 0)
										result.translate(2.0, 1.0);
										result.rotate(-PI / 2.0, 1.0 + 1.0 / 2.0, 1.0 / 2.0);
									} else {
										// (2 1) -> (1 1)
									}
								} else {
									if (j2 == 0) {
										// (2 1) -> (2 0)
										result.rotate(PI, 2.0 + 1.0 / 2.0, 1.0 / 2.0);
									} else {
										// (2 1) -> (2 1)
									}
								}
							}
						}
						
						result.scale(3.0, 2.0);
						
						return result;
					}
					
					public void updateImage(final Mesh mesh) {
						canvas.invoke(false, new GLRunnable() {
							
							@Override
							public final boolean run(final GLAutoDrawable drawable) {
								mesh.updateImage();
								getUpdatedNeeded().set(true);
								
								return false;
							}
							
						});
					}
					
					@Override
					public final void mouseMoved(final MouseEvent e) {
						final Pick pick = getPicking().pick(e.getX(), e.getY());
						
						if (pick != null && pick.getEntry() != null) {
							final int idUnderMouse = pick.getId();
							final Entry<Integer, Object> entry = pick.getEntry();
							final Solid structureUnderMouse = (Solid) entry.getValue();
							final int faceIndex = idUnderMouse - entry.getKey();
							
							this.underMouse(structureUnderMouse, faceIndex);
						} else {
							this.underMouse(null, -1);
						}
					}
					
					private final void underMouse(final Solid object, final int faceIndex) {
						if (object != null && object.getAttribute("Texture") != null) {
							return;
						}
						
						if (this.objectUnderMouse != null && this.faceIndexUnderMouse != faceIndex) {
							final Mesh p = (Mesh) this.objectUnderMouse.getAttributes().get(Solid.FACES_GEOMETRY);
							
							FaceProcessor.forEachFaceIn(this.objectUnderMouse.getTopology(), (f, i, start, end) -> {
								if (i == this.faceIndexUnderMouse) {
									this.restoreColors(canvas, p, start, end);
								}
							});
						}
						
						this.objectUnderMouse = object;
						this.faceIndexUnderMouse = faceIndex;
						
						if (object != null) {
							final Mesh p = (Mesh) object.getAttributes().get(Solid.FACES_GEOMETRY);
							final int stride = (int) object.getAttribute(STRIDE);
							final FloatBuffer data = Buffers.newDirectFloatBuffer(4 * stride);
							
							for (int i = 0; i < stride; ++i) {
								data.put(1F).put(1F).put(0F).put(1F);
							}
							
							FaceProcessor.forEachFaceIn(object.getTopology(), (f, i, start, end) -> {
								if (i == faceIndex) {
									this.updateColors(canvas, p, start, end, data);
								}
							});
						}
					}
					
					private final void updateColors(final GLCanvas canvas, final Mesh p, final int start, final int end,
							final FloatBuffer data) {
						canvas.invoke(false, new GLRunnable() {
							
							@Override
							public final boolean run(final GLAutoDrawable drawable) {
								p.updateColors(start, end, data.position(0));
								getUpdatedNeeded().set(true);
								
								return false;
							}
							
						});
					}
					
					private final void restoreColors(final GLCanvas canvas, final Mesh p, final int start, final int end) {
						canvas.invoke(false, new GLRunnable() {
							
							@Override
							public final boolean run(final GLAutoDrawable drawable) {
								p.updateColors(start, end);
								getUpdatedNeeded().set(true);
								
								return false;
							}
							
						});
					}
					
					private static final long serialVersionUID = 7979661269418664373L;
					
				}.addTo(canvas);
			}
			
			@Override
			protected final void initialize(final GLAutoDrawable drawable) {
				super.initialize(drawable);
				
				final GL4 gl = this.getGL();
				
				this.add("coloring", newProgramV3F3(gl))
					.addUniformSetters(new UniformMPV(this.getProjectionView().getMatrix()));
				this.add("texturing", newProgramV3F4(gl))
					.addUniformSetters(new UniformMPV(this.getProjectionView().getMatrix()))
					.addUniformSetters(new Uniform1Int("tex", 0));
				this.getPicking().add("picking", newPickingProgram(gl))
					.addUniformSetters(new UniformMPV(this.getProjectionView().getMatrix()));
				
				{
					final Solid s = Quads.newCube(new Solid());
					final int k = 4;
					
					for (int i = 0; i < k; ++i) {
						Quads.subdivide(s, true);
					}
					
					if (true) {
						final int w = 1024;
						final int h = w;
						final BufferedImage image = s.getTexture(w);
						
						{
							final Graphics2D gr = (Graphics2D) image.getGraphics();
							
							gr.setColor(Color.RED);
							gr.fillRect(0, 0, w, h);
							gr.setColor(Color.CYAN);
							gr.fillRect(0, 0, w / 2, h / 2);
							gr.setColor(Color.GREEN);
							gr.fillRect(w, 0, w, h);
							gr.setColor(Color.MAGENTA);
							gr.fillRect(w, 0, w / 2, h / 2);
							gr.setColor(Color.BLUE);
							gr.fillRect(2 * w, 0, w, h);
							gr.setColor(Color.YELLOW);
							gr.fillRect(2 * w, 0, w / 2, h / 2);
							gr.setColor(Color.CYAN);
							gr.fillRect(0, h, w, h);
							gr.setColor(Color.RED);
							gr.fillRect(0, h, w / 2, h / 2);
							gr.setColor(Color.MAGENTA);
							gr.fillRect(w, h, w, h);
							gr.setColor(Color.GREEN);
							gr.fillRect(w, h, w / 2, h / 2);
							gr.setColor(Color.YELLOW);
							gr.fillRect(2 * w, h, w, h);
							gr.setColor(Color.BLUE);
							gr.fillRect(2 * w, h, w / 2, h / 2);
							
							gr.setColor(Color.BLACK);
							gr.setFont(gr.getFont().deriveFont(100F));
							gr.drawString("A", 0, h / 2);
							gr.drawString("B", w, h / 2);
							gr.drawString("C", 2 * w, h / 2);
							gr.drawString("D", 0, h + h / 2);
							gr.drawString("E", w, h + h / 2);
							gr.drawString("F", 2 * w, h + h / 2);
							
							gr.dispose();
						}
						
						s.setAttribute("Texture", image);
					}
					
					this.add("geom", s.createFacesGeometry(gl));
					
					if (s.getAttribute("Texture") != null) {
						this.getShaderProgram("texturing").addGeometries(this.getGeometry("geom"));
					} else {
						this.getShaderProgram("coloring").addGeometries(this.getGeometry("geom"));
					}
					
					this.getPicking().add(s);
					this.getPicking().add("picking_geom", s.createFacePickingGeometry(gl, this.getPicking().getId()));
					this.getPicking().getShaderPrograms().get("picking").addGeometries(this.getPicking().getGeometries().values());
				}
			}
			
			private static final long serialVersionUID = -6836065598820066109L;
			
		}.show();
	}
	
	public static final Vector3d mid(final Vector3d v1, final Vector3d v2) {
		final Vector3d result = new Vector3d(v1);
		
		result.add(v2);
		result.scale(0.5);
		
		return result;
	}
	
	public static final Vector3d v() {
		return v(0.0, 0.0, 0.0);
	}
	
	public static final Vector3d v(final double x, final double y, final double z) {
		return new Vector3d(x, y, z);
	}
	
	public static final void printFaces(final Manifold topo) {
		debugPrint();
		
		topo.forEach(FACE, (f, i) -> {
			final IntList tmp = new IntList();
			topo.forEachDartIn(FACE, f, (d, j) -> {
				tmp.add(d);
			});
			debugPrint(tmp);
		});
	}
	
	public static final double distance(final Vector3d v1, final Vector3d v2) {
		return sqrt(square(v1.x - v2.x) + square(v1.y - v2.y) + square(v1.z - v2.z));
	}
	
	public static final double sqd(final Vector3d v1, final Vector3d v2) {
		return square(v1.x - v2.x) + square(v1.y - v2.y) + square(v1.z - v2.z);
	}
	
	public static final Vector3d mid(final Vector3d v1, final Vector3d v2, final boolean normalize) {
		final Vector3d m = mid(v1, v2);
		
		return normalize ? normalize(m) : m;
	}
	
	public static final Vector3d normalize(final Vector3d v) {
		v.normalize();
		
		return v;
	}
	
	public static final ShaderCode VERTEX_SHADER_UNIFORM_TRANSFORM_IN_LOCATION_COLOR_UV = vertexShader(
			"#version 330\n" +
			"uniform mat4 transform;\n" +
			"in vec3 vertexLocation;\n" +
			"in vec4 vertexColor;\n" +
			"in vec2 vertexUV;\n" +
			"out vec4 interpolatedColor;\n" +
			"out vec2 interpolatedUV;\n" +
			"void main() {\n" +
			"	interpolatedColor = vertexColor;\n" +
			"	interpolatedUV = vertexUV;\n" +
			"	gl_Position = transform * vec4(vertexLocation, 1.0);\n" +
			"}\n");
	
	public static final ShaderCode FRAGMENT_SHADER_IN_COLOR_UV = fragmentShader(
			"#version 330\n" +
			"in vec4 interpolatedColor;\n" +
			"in vec2 interpolatedUV;\n" +
			"out vec4 fragmentColor;\n" +
			"out vec4 fragmentUV;\n" +
			"void main() {\n" +
			"	fragmentColor = interpolatedColor;\n" +
			"	int u = int(interpolatedUV.x * 65536);\n" +
			"	int v = int(interpolatedUV.y * 65536);\n" +
			"	int u0 = u & 0x00FF;\n" +
			"	int u1 = (u >> 8) & 0x00FF;\n" +
			"	int v0 = v & 0x00FF;\n" +
			"	int v1 = (v >> 8) & 0x00FF;\n" +
			"	fragmentUV = vec4(u0 / 255.0, u1 / 255.0, v0 / 255.0, v1 / 255.0);\n" +
			"}\n");
	
	public static final ExtendedShaderProgram newPickingProgram(final GL2ES2 gl) {
		return new ExtendedShaderProgram(gl)
			.attribute("vertexLocation", 0)
			.attribute("vertexColor", 1)
			.attribute("vertexUV", 2)
			.build(VERTEX_SHADER_UNIFORM_TRANSFORM_IN_LOCATION_COLOR_UV, FRAGMENT_SHADER_IN_COLOR_UV);
	}
	
	/**
	 * @author codistmonk (creation 2018-07-01)
	 */
	public static final class Quads {
		
		private Quads() {
			throw new IllegalInstantiationException();
		}
		
		public static final Solid newCube(final Solid s) {
			final Manifold topo = s.getTopology();
			final int ab = topo.newEdge();
			final int ba = opposite(ab);
			final int bc = topo.newEdge();
			final int cb = opposite(bc);
			final int cd = topo.newEdge();
			final int dc = opposite(cd);
			final int da = topo.newEdge();
			final int ad = opposite(da);
			final int ae = topo.newEdge();
			final int ea = opposite(ae);
			final int bf = topo.newEdge();
			final int fb = opposite(bf);
			final int cg = topo.newEdge();
			final int gc = opposite(cg);
			final int dh = topo.newEdge();
			final int hd = opposite(dh);
			final int ef = topo.newEdge();
			final int fe = opposite(ef);
			final int fg = topo.newEdge();
			final int gf = opposite(fg);
			final int gh = topo.newEdge();
			final int hg = opposite(gh);
			final int he = topo.newEdge();
			final int eh = opposite(he);
			
			topo.initializeCycle(ab, bc, cd, da);
			topo.initializeCycle(ad, dh, he, ea);
			topo.initializeCycle(ae, ef, fb, ba);
			topo.initializeCycle(bf, fg, gc, cb);
			topo.initializeCycle(cg, gh, hd, dc);
			topo.initializeCycle(eh, hg, gf, fe);
			
			check(topo.isValid());
			
			final int a = ab;
			final int b = bc;
			final int c = cd;
			final int d = da;
			final int e = ef;
			final int f = fg;
			final int g = gh;
			final int h = he;
			
			s.setVertex(a, normalize(v(-0.5F, -0.5F, -0.5F)));
			s.setVertex(b, normalize(v(+0.5F, -0.5F, -0.5F)));
			s.setVertex(c, normalize(v(+0.5F, +0.5F, -0.5F)));
			s.setVertex(d, normalize(v(-0.5F, +0.5F, -0.5F)));
			s.setVertex(e, normalize(v(-0.5F, -0.5F, +0.5F)));
			s.setVertex(f, normalize(v(+0.5F, -0.5F, +0.5F)));
			s.setVertex(g, normalize(v(+0.5F, +0.5F, +0.5F)));
			s.setVertex(h, normalize(v(-0.5F, +0.5F, +0.5F)));
			
			s.setAttribute(DRAWING_MODE, GL.GL_TRIANGLE_FAN);
			s.setAttribute(STRIDE, 4);
			
			return s;
		}
		
		public static final void subdivide(final Solid s, final boolean normalize) {
			final Collection<Vector3d> initialVertices = new HashSet<>(s.getVertices());
			
			Triangles.cutAllEdges(s);
			
			final Manifold topo = s.getTopology();
			
			topo.forEach(FACE, (f, i) -> {
				int a = f;
				
				while (!initialVertices.contains(s.getVertex(a))) {
					a = topo.getNext(a);
				}
				
				final int a1 = topo.getNext(a);
				final int b = topo.getNext(a1);
				final int b1 = topo.getNext(b);
				final int c = topo.getNext(b1);
				final int c1 = topo.getNext(c);
				final int d = topo.getNext(c1);
				final int d1 = topo.getNext(d);
				
				check(a == topo.getNext(d1));
				
				final Vector3d va = s.getVertex(a);
				final Vector3d vb = s.getVertex(b);
				final Vector3d vc = s.getVertex(c);
				final Vector3d vd = s.getVertex(d);
				final Vector3d va1 = mid(va, vb, normalize);
				final Vector3d vb1 = mid(vb, vc, normalize);
				final Vector3d vc1 = mid(vc, vd, normalize);
				final Vector3d vd1 = mid(vd, va, normalize);
				
				s.setVertex(a1, va1);
				s.setVertex(b1, vb1);
				s.setVertex(c1, vc1);
				s.setVertex(d1, vd1);
				
				final int na1 = topo.newEdge();
				final int nb1 = topo.newEdge();
				final int nc1 = topo.newEdge();
				final int nd1 = topo.newEdge();
				
				topo.setCycle(na1, a1, b, opposite(nb1));
				topo.setCycle(nb1, b1, c, opposite(nc1));
				topo.setCycle(nc1, c1, d, opposite(nd1));
				topo.setCycle(nd1, d1, a, opposite(na1));
				
				s.setVertex(na1, mid(va1, vc1, normalize));
				
				check(topo.isValid());
				
				s.refreshVertices();
				
				for (int k = 0; k < topo.getDartCount(); ++k) {
					if (null == s.getVertex(k)) {
						debugError(k);
						check(false);
					}
				}
			});
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2018-07-01)
	 */
	public static final class Triangles {
		
		private Triangles() {
			throw new IllegalInstantiationException();
		}
		
		public static final Solid newRegularTetrahedron(final Solid s) {
			final Manifold topo = s.getTopology();
			final int ab = topo.newEdge();
			final int ba = opposite(ab);
			final int ac = topo.newEdge();
			final int ca = opposite(ac);
			final int ad = topo.newEdge();
			final int da = opposite(ad);
			final int bc = topo.newEdge();
			final int cb = opposite(bc);
			final int cd = topo.newEdge();
			final int dc = opposite(cd);
			final int db = topo.newEdge();
			final int bd = opposite(db);
			final int a = ab;
			final int b = bc;
			final int c = cd;
			final int d = db;
			
			{
				topo.initializeCycle(ab, bc, ca);
				topo.initializeCycle(ac, cd, da);
				topo.initializeCycle(ad, db, ba);
				topo.initializeCycle(bd, dc, cb);
				
				check(topo.isValid());
			}
			
			{
				// https://en.wikipedia.org/wiki/Tetrahedron#Formulas_for_a_regular_tetrahedron
				final double sqrt89 = sqrt(8.0 / 9.0);
				final double sqrt29 = sqrt(2.0 / 9.0);
				final double sqrt23 = sqrt(2.0 / 3.0);
				
				s.setVertex(a, v(0.0, 0.0, 1.0));
				s.setVertex(b, v(sqrt89, 0.0, -1.0 / 3.0));
				s.setVertex(c, v(-sqrt29, sqrt23, -1.0 / 3.0));
				s.setVertex(d, v(-sqrt29, -sqrt23, -1.0 / 3.0));
				
				check(s.getVertices().size() == topo.getDartCount());
			}
			
			s.setAttribute(DRAWING_MODE, GL.GL_TRIANGLES);
			s.setAttribute(STRIDE, 3);
			
			return s;
		}
		
		public static final void subdivideLongestEdge(final Solid s) {
			final Manifold topo = s.getTopology();
			final double[] length = { 0.0 };
			final int[] edge = { 0 };
			
			topo.forEach(EDGE, (e, i) -> {
				final double l = distance(s.getVertex(e), s.getVertex(opposite(e)));
				
				if (length[0] < l) {
					length[0] = l;
					edge[0] = e;
				}
			});
			
			final Vector3d xyz = mid(s.getVertex(edge[0]), s.getVertex(opposite(edge[0])), true);
			
			final int newDart = topo.cutEdge(edge[0]);
			
			topo.cutFace(edge[0], topo.getPrevious(edge[0], 2));
			topo.cutFace(topo.getPrevious(opposite(edge[0])), topo.getNext(opposite(edge[0])));
			
			check(topo.isValid());
			
			s.setVertex(newDart, xyz);
			s.refreshVertices();
		}
		
		public static final void subdivideAllEdges(final Solid s) {
			final Collection<Vector3d> initialVertices = new HashSet<>(s.getVertices());
			final Manifold topo = s.getTopology();
			
			cutAllEdges(s);
			
			topo.forEach(FACE, (f, i) -> {
				int a = f;
				
				while (!initialVertices.contains(s.getVertex(a))) {
					a = topo.getNext(a);
				}
				
				final int a1 = topo.getNext(a);
				final int b = topo.getNext(a1);
				final int b1 = topo.getNext(b);
				final int c = topo.getNext(b1);
				final int c1 = topo.getNext(c);
				
				check(a == topo.getNext(c1));
				
				final Vector3d va = s.getVertex(a);
				final Vector3d vb = s.getVertex(b);
				final Vector3d vc = s.getVertex(c);
				
				final int a1b1 = topo.cutFace(a, b);
				final int b1c1 = topo.cutFace(a1b1, c);
				final int c1a1 = topo.cutFace(b1c1, a);
				
				s.setVertex(a1b1, mid(va, vb, true));
				s.setVertex(b1c1, mid(vb, vc, true));
				s.setVertex(c1a1, mid(vc, va, true));
			});
			
			check(topo.isValid());
			check(topo.getDartCount() == s.getVertices().size());
		}
		
		public static final void cutAllEdges(final Solid s) {
			final Manifold topo = s.getTopology();
			
			topo.forEach(EDGE, (e, i) -> {
				final int f = topo.getNext(e);
				final Vector3d ve = s.getVertex(e);
				final Vector3d vf = s.getVertex(f);
				
				final int g = topo.cutEdge(e);
				
				s.setVertex(e, ve);
				s.setVertex(f, vf);
				s.setVertex(g, mid(ve, vf, true));
			});
			
			check(topo.isValid());
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2018-07-03)
	 */
	public static abstract interface FaceProcessor extends Serializable {
		
		public abstract void process(int f, int i, int start, int end);
		
		public static void forEachFaceIn(final Manifold topology, final FaceProcessor processor) {
			final int tmp[] = { 0 };
			
			topology.forEach(FACE, (f, i) -> {
				final int n = FACE.countDarts(topology, f);
				final int start = tmp[0];
				final int end = start + n;
				
				processor.process(f, i, start, end);
				
				tmp[0] += n;
			});
		}
		
	}
	
}

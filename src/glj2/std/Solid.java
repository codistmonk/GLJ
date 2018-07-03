package glj2.std;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;
import static multij.tools.Manifold.Traversor.EDGE;
import static multij.tools.Manifold.Traversor.FACE;
import static multij.tools.Manifold.Traversor.VERTEX;
import static multij.tools.MathTools.square;
import static multij.tools.Tools.check;
import static multij.tools.Tools.debugPrint;
import static multij.tools.Tools.doubles;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import javax.vecmath.Vector3d;

import com.jogamp.common.nio.Buffers;

import glj2.core.VBO;
import multij.tools.Manifold;

/**
 * @author codistmonk (creation 2018-06-28)
 */
public final class Solid implements Serializable {
	
	private final Manifold topology = new Manifold();
	
	private final List<Vector3d> vertices = new ArrayList<>();
	
	private final Map<String, Object> attributes = new TreeMap<>();
	
	public final Manifold getTopology() {
		return this.topology;
	}
	
	public final List<Vector3d> getVertices() {
		return this.vertices;
	}
	
	public final Map<String, Object> getAttributes() {
		return this.attributes;
	}
	
	public final Solid setAttribute(final String key, final Object value) {
		this.getAttributes().put(key, value);
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public final <T> T getAttribute(final String key) {
		return (T) this.getAttributes().get(key);
	}
	
	public final Vector3d getVertex(final int dart) {
		return this.getVertices().get(dart);
	}
	
	public final void setVertex(final int dart, final Vector3d xyz) {
		this.getTopology().forEachDartIn(VERTEX, dart, (d, j) -> {
			while (this.getVertices().size() <= d) {
				this.getVertices().add(null);
			}
			
			this.getVertices().set(d, xyz);
		});
	}
	
	public final void refreshVertices() {
		for (int i = 0; i < this.getVertices().size(); ++i) {
			final Vector3d v = this.getVertices().get(i);
			
			if (v != null) {
				this.setVertex(i, v);
			}
		}
		
		check(this.getTopology().getDartCount() == this.getVertices().size());
		check(this.getVertices().stream().allMatch(v -> v != null));
	}
	
	public final VBO getLocationsVBO(final GL2ES2 gl) {
		return (VBO) this.getAttributes().computeIfAbsent("LocationsVBO", __ -> Mesh.newVBO(gl, this.getLocations().position(0), Mesh.LOCATIONS));
	}
	
	public final FloatBuffer getLocations() {
		final Manifold topo = this.getTopology();
		
		return (FloatBuffer) this.getAttributes().computeIfAbsent("Locations", __ -> {
			final FloatBuffer result = Buffers.newDirectFloatBuffer(topo.getDartCount() * Mesh.LOCATION_COMPONENTS);
			
			switch ((int) this.getAttribute(DRAWING_MODE)) {
			case GL.GL_LINES:
			{
				debugPrint("edges:", EDGE.count(topo));
				
				topo.forEach(EDGE, (f, i) -> {
					topo.forEachDartIn(EDGE, f, (dt, j) -> {
						addVertexTo(result, this.getVertex(dt));
					});
				});
				
				break;
			}
			case GL.GL_TRIANGLES:
			case GL.GL_TRIANGLE_FAN:
			case GL.GL_TRIANGLE_STRIP:
			{
				debugPrint("faces:", FACE.count(topo));
				
				topo.forEach(FACE, (f, i) -> {
					topo.forEachDartIn(FACE, f, (dt, j) -> {
						addVertexTo(result, this.getVertex(dt));
					});
				});
				
				break;
			}
			default:
				check(false);
			}
			
			check(!result.hasRemaining());
			
			return result;
		});
	}
	
	public final VBO getUVsVBO(final GL2ES2 gl) {
		return (VBO) this.getAttributes().computeIfAbsent("UVsVBO", __ -> Mesh.newVBO(gl, this.getUVs().position(0), Mesh.UVS));
	}
	
	public final FloatBuffer getUVs() {
		final Manifold topo = this.getTopology();
		
		return (FloatBuffer) this.getAttributes().computeIfAbsent("UVs", __ -> {
			final FloatBuffer result = Buffers.newDirectFloatBuffer(topo.getDartCount() * Mesh.UV_COMPONENTS);
			
			topo.forEach(FACE, (f, i) -> {
				final List<Vector3d> vs = new ArrayList<>();
				
				topo.forEachDartIn(FACE, f, (dt, j) -> {
					vs.add(this.getVertex(dt));
				});
				
				final Predicate<? super double[]> uvValidator = uv -> abs(uv[0]) <= 1.0 && abs(uv[1]) <= 1.0;
				final double[] s = { 0.0 };
				final List<double[]> zuv = vs.stream().map(v -> { s[0] += v.z; return doubles(v.x / v.z, v.y / v.z); }).collect(toList());
				final List<double[]> uvs;
				double uOffset;
				final double x0 = 0.0;
				final double x1 = 1.0 / 3.0;
				final double x2 = 2.0 / 3.0;
				final double y0 = 0.0;
				final double y1 = 1.0 / 2.0;
				
				if (zuv.stream().allMatch(uvValidator)) {
					uvs = zuv;
					uOffset = x0;
				} else {
					s[0] = 0.0;
					final List<double[]> yuv = vs.stream().map(v -> { s[0] += v.y; return doubles(v.z / v.y, v.x / v.y); }).collect(toList());
					
					if (yuv.stream().allMatch(uvValidator)) {
						uvs = yuv;
						uOffset = x1;
					} else {
						s[0] = 0.0;
						final List<double[]> xuv = vs.stream().map(v -> { s[0] += v.x; return doubles(v.y / v.x, v.z / v.x); }).collect(toList());
						
						if (xuv.stream().allMatch(uvValidator)) {
							uvs = xuv;
							uOffset = x2;
						} else {
							throw new IllegalStateException();
						}
					}
				}
				
				double vOffset = s[0] < 0.0 ? y1 : y0;
				double au = 1.0;
				double av = 0.0;
				double a = 0.0;
				double bu = 0.0;
				double bv = 1.0;
				double b = 0.0;
				
				
				// swap/rotate subregions to simplify connexity (2 strips of 3 subregions)
				{
					if (uOffset == x2 && vOffset == y0) {
						uOffset = x0;
						vOffset = y1;
					} else if (uOffset == x0 && vOffset == y1) {
						uOffset = x2;
						vOffset = y0;
					}
					
					if (uOffset == x0 && vOffset == y1) {
						au = -1.0;
						a = 1.0;
						bv = -1.0;
						b = 1.0;
					} else if (uOffset == x1 && vOffset == y1) {
						au = 0.0;
						av = 1.0;
						bu = 1.0;
						bv = 0.0;
					} else if (uOffset == x2 && vOffset == y1) {
						au = -1.0;
						a = 1.0;
					} else if (uOffset == x0 && vOffset == y0) {
						au = 0.0;
						av = 1.0;
						bu = -1.0;
						bv = 0.0;
						b = 1.0;
					} else if (uOffset == x1 && vOffset == y0) {
						au = -1.0;
						a = 1.0;
						bv = -1.0;
						b = 1.0;
					} else if (uOffset == x2 && vOffset == y0) {
						au = 0.0;
						av = 1.0;
						bu = 1.0;
						bv = 0.0;
					}
				}
				
				for (int j = 0; j < uvs.size(); ++j) {
					final double[] uv = uvs.get(j);
					final double u = (uv[0] + 1.0) / 2.0;
					final double v = (uv[1] + 1.0) / 2.0;
					
					result.put((float) (uOffset + (au * u + av * v + a) / 3.0));
					result.put(1F - (float) (vOffset + (bu * u + bv * v + b) / 2.0));
				}
			});
			
			check(!result.hasRemaining());
			
			return result;
		});
	}
	
	public final VBO getColorsVBO(final GL2ES2 gl) {
		return (VBO) this.getAttributes().computeIfAbsent("ColorsVBO", __ -> Mesh.newVBO(gl, this.getColors().position(0), Mesh.COLORS));
	}
	
	public final FloatBuffer getColors() {
		final Manifold topo = this.getTopology();
		
		return (FloatBuffer) this.getAttributes().computeIfAbsent("Colors", __ -> {
			final FloatBuffer result = Buffers.newDirectFloatBuffer(topo.getDartCount() * Mesh.COLOR_COMPONENTS);
			
			topo.forEach(FACE, (f, i) -> {
				topo.forEachDartIn(FACE, f, (d, j) -> {
					final Vector3d v = this.getVertex(d);
					result.put((float) square(v.x));
					result.put((float) square(v.y));
					result.put((float) square(v.z));
					result.put(1F);
				});
			});
			
			check(!result.hasRemaining());
			
			return result;
		});
	}
	
	public final VBO getIdsVBO(final GL2ES2 gl, final int[] idOffset) {
		return (VBO) this.getAttributes().computeIfAbsent("IdsVBO", __ -> Mesh.newVBO(gl, this.getIds(idOffset).position(0), Mesh.COLORS));
	}
	
	public final FloatBuffer getIds(final int[] idOffset) {
		final Manifold topo = this.getTopology();
		
		return (FloatBuffer) this.getAttributes().computeIfAbsent("Ids", __ -> {
			final FloatBuffer result = Buffers.newDirectFloatBuffer(topo.getDartCount() * Mesh.COLOR_COMPONENTS);
			
			switch ((int) this.getAttribute(DRAWING_MODE)) {
			case GL.GL_LINES:
			{
				topo.forEach(EDGE, (f, i) -> {
					final Color id = new Color(idOffset[0]++);
					
					topo.forEachDartIn(EDGE, f, (dt, j) -> {
						addVertexTo(result, id);
					});
				});
				
				break;
			}
			case GL.GL_TRIANGLES:
			case GL.GL_TRIANGLE_FAN:
			case GL.GL_TRIANGLE_STRIP:
			{
				topo.forEach(FACE, (f, i) -> {
					final Color id = new Color(idOffset[0]++);
					
					topo.forEachDartIn(FACE, f, (dt, j) -> {
						addVertexTo(result, id);
					});
				});
				
				break;
			}
			}
			
			check(!result.hasRemaining());
			
			return result;
		});
	}
	
	public final Mesh createFacesGeometry(final GL4 gl) {
		final Manifold topo = this.getTopology();
		final Mesh result = new Mesh(gl, topo.getDartCount())
				.setDrawingMode(this.getAttribute(DRAWING_MODE))
				.setStride(this.getAttribute(STRIDE))
				.setLocations(this.getLocations(), this.getLocationsVBO(gl));
		
		final BufferedImage image = this.getAttribute("Texture");
		
		if (image != null) {
			result.setTexture(image);
			result.setUVs(this.getUVs(), this.getUVsVBO(gl));
		} else {
			result.setColors(this.getColors(), this.getColorsVBO(gl));
		}
		
		this.getAttributes().put(FACES_GEOMETRY, result.setupVAO());
		
		return result;
	}
	
	public final Mesh createFacePickingGeometry(final GL4 gl, final int[] idOffset) {
		final Manifold topo = this.getTopology();
		final Mesh result = new Mesh(gl, topo.getDartCount())
				.setDrawingMode(this.getAttribute(DRAWING_MODE))
				.setStride(this.getAttribute(STRIDE))
				.setLocations(this.getLocations(), this.getLocationsVBO(gl))
				.setColors(this.getIds(idOffset), this.getIdsVBO(gl, idOffset))
				.setUVs(this.getUVs(), this.getUVsVBO(gl));
		
		this.getAttributes().put(PICKING_GEOMETRY, result.setupVAO());
		
		return result;
	}
	
	private static final long serialVersionUID = 2214457862031400618L;
	
	public static final String GEOMETRY = "Geometry";
	
	public static final String FACES = "Faces";
	
	public static final String PICKING = "Picking";
	
	public static final String FACES_GEOMETRY = FACES + GEOMETRY;
	
	public static final String PICKING_GEOMETRY = PICKING + GEOMETRY;
	
	public static final String STRIDE = "Stride";

	public static final String DRAWING_MODE = "DrawingMode";
	
	public static final void addVertexTo(final FloatBuffer data, final Vector3d xyz) {
		data.put((float) xyz.x);
		data.put((float) xyz.y);
		data.put((float) xyz.z);
	}
	
	public static final void addVertexTo(final FloatBuffer data, final Color c) {
		data.put(c.getRed() / 255F);
		data.put(c.getGreen() / 255F);
		data.put(c.getBlue() / 255F);
		data.put(c.getAlpha() / 255F);
	}
	
	public static final void addVertexTo(final Mesh p, final Vector3d xyz) {
		addVertexTo(p, (float) xyz.x, (float) xyz.y, (float) xyz.z); 
	}
	
	public static final void addVertexTo(final Mesh p, final float x, final float y, final float z) {
		p.addVertex(x, y, z, square(x), square(y), square(z), 1F);
	}
	
	public static final void addVertexTo(final Mesh p, final Vector3d xyz, final float u, final float v) {
		p.addVertex((float) xyz.x, (float) xyz.y, (float) xyz.z, u, v);
	}
	
	public static final void addVertexTo(final Mesh p, final Vector3d xyz, final Color color) {
		p.addVertex((float) xyz.x, (float) xyz.y, (float) xyz.z,
				color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
	}
	
}
package glj.demo;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author codistmonk (creation 2013-01-21)
 */
public final class TabularDoubleData {
	
	private final List<double[]> data;
	
	private final List<Column> columns;
	
	private final Set<Column> grouping;
	
	private final List<Column> sortingKeys;
	
	private Object view;
	
	private Column countColumn;
	
	private int columnCount = -1;
	
	public TabularDoubleData() {
		this.data = new ArrayList<double[]>();
		this.columns = new ArrayList<Column>();
		this.grouping = new HashSet<Column>();
		this.sortingKeys = new ArrayList<Column>();
	}
	
	public final void addDataRow(final double... dataRow) {
		this.data.add(dataRow);
		
		if (this.columnCount < 0) {
			this.columnCount = dataRow.length;
			
			for (int i = 0; i < this.columnCount; ++i) {
				this.new Column();
			}
		} else if (this.columnCount != dataRow.length) {
			throw new IllegalArgumentException();
		}
	}
	
	public final int getRowCount() {
		return this.data.size();
	}
	
	public final int getColumnCount() {
		return this.columns.size();
	}
	
	public final Column getCountColumn() {
		return this.countColumn;
	}
	
	public final void setCountColumn(final Column countColumn) {
		if (this.getCountColumn() != countColumn) {
			this.countColumn = countColumn;
		}
	}
	
	public final Set<Column> getGrouping() {
		return this.grouping;
	}
	
	public final List<Column> getSortingKeys() {
		return this.sortingKeys;
	}
	
	public final <V> V getView() {
		return (V) this.view;
	}
	
	public final void updateView() {
		if (!this.getGrouping().containsAll(this.getSortingKeys())) {
			throw new IllegalStateException();
		}
		
		if (this.getGrouping().isEmpty()) {
			final List<ViewRow> list = new ArrayList<ViewRow>();
			this.view = list;
			
			for (final double[] dataRow : this.data) {
				list.add(this.new ViewRow(dataRow));
			}
			
			if (!this.getSortingKeys().isEmpty()) {
				Collections.sort(list);
			}
		} else {
			final Map<ViewRow, ViewRow> map;
			
			if (this.getSortingKeys().isEmpty()) {
				map = new LinkedHashMap<ViewRow, ViewRow>();
			} else {
				map = new TreeMap<ViewRow, ViewRow>();
			}
			
			this.view = map;
			
			for (final double[] dataRow : this.data) {
				final ViewRow newViewRow = this.new ViewRow(dataRow);
				final ViewRow existingViewRow = map.get(newViewRow);
				
				if (existingViewRow != null) {
					existingViewRow.incrementCount(newViewRow.getCount());
				} else {
					map.put(newViewRow, newViewRow);
				}
			}
		}
		
	}
	
	final void addColumn(final Column column) {
		this.columns.add(column);
	}
	
	final Iterable<Column> getColumns() {
		return this.columns;
	}
	
	/**
	 * @author codistmonk (creation 2013-01-21)
	 */
	public final class ViewRow implements Comparable<ViewRow> {
		
		private final double[] dataRow;
		
		private long count;
		
		private final int hashCode;
		
		public ViewRow(final double[] dataRow) {
			this.dataRow = dataRow;
			final Column countColumn = TabularDoubleData.this.getCountColumn();
			this.count = countColumn != null ? (long) this.getDataRow()[countColumn.getIndex()] : 1;
			this.hashCode = this.computeHashCode();
		}
		
		public final void incrementCount(final long increment) {
			++this.count;
		}
		
		public final double[] getDataRow() {
			return this.dataRow;
		}
		
		public final long getCount() {
			return this.count;
		}
		
		@Override
		public final int hashCode() {
			return this.hashCode;
		}
		
		public final boolean equals(final Object object) {
			final ViewRow that = cast(this.getClass(), object);
			
			if (that == null) {
				return false;
			}
			
			for (final Column column : TabularDoubleData.this.getColumns()) {
				final int columnIndex = column.getIndex();
				
				if (this.getDataRow()[columnIndex] != that.getDataRow()[columnIndex]) {
					return false;
				}
			}
			
			return true;
		}
		
		@Override
		public final int compareTo(final ViewRow that) {
			final List<Column> sortingKeys = TabularDoubleData.this.getSortingKeys();
			
			if (sortingKeys.isEmpty()) {
				return this.equals(that) ? 0 : 1;
			}
			
			final double[] thisDataRow = this.getDataRow();
			final double[] thatDataRow = that.getDataRow();
			
			for (final Column column : sortingKeys) {
				final int columnIndex = column.getIndex();
				final int maybeResult = Double.compare(thisDataRow[columnIndex], thatDataRow[columnIndex]);
				
				if (maybeResult != 0) {
					return maybeResult;
				}
			}
			
			return 0;
		}
		
		private final int computeHashCode() {
			int result = 0;
			
			for (final Column column : TabularDoubleData.this.getColumns()) {
				result += this.getDataRow()[column.getIndex()];
			}
			
			return result;
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2013-01-21)
	 */
	public final class Column {
		
		private final int index;
		
		private String name;
		
		private double rounding;
		
		Column() {
			this.index = TabularDoubleData.this.getColumnCount();
			this.name = "$" + this.index;
			TabularDoubleData.this.addColumn(this);
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final void setName(final String name) {
			this.name = name;
		}
		
		public final int getIndex() {
			return this.index;
		}
		
		public final double getRounding() {
			return this.rounding;
		}
		
		public final void setRounding(double rounding) {
			this.rounding = rounding;
		}
		
		public final double getDatum(final double[] dataRow) {
			return round(dataRow[this.getIndex()], this.getRounding());
		}
		
	}
	
	public static final double round(final double value, final double rounding) {
		// TODO
		return value;
	}
	
}

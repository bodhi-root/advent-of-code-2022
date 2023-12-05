package common.geometry;

public class XY<T> {

	public T x;
	public T y;
	
	public XY(T x, T y) {
		this.x = x;
		this.y = y;
	}
		
	public int hashCode() {
		return x.hashCode() + (17 * y.hashCode());
	}
	
	@SuppressWarnings("rawtypes")
	public boolean equals(Object o) {
		if (!(o instanceof XY))
			return false;

		XY that = (XY)o;
		return this.x.equals(that.x) && this.y.equals(that.y);
	}

	public String toString() {
		return x.toString() + ", " + y.toString();
	}	
	
}

package common.geometry;

public class XYZ<T> {

	public T x;
	public T y;
	public T z;
	
	public XYZ(T x, T y, T z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
		
	public int hashCode() {
		return x.hashCode() + (17 * y.hashCode()) + (34 * z.hashCode());
	}
	
	@SuppressWarnings("rawtypes")
	public boolean equals(Object o) {
		if (!(o instanceof XYZ))
			return false;

		XYZ that = (XYZ)o;
		return this.x.equals(that.x) && 
			   this.y.equals(that.y) &&
			   this.z.equals(that.z);
	}

	public String toString() {
		return x.toString() + ", " + y.toString() + ", " + z.toString();
	}	
	
}

package de.hechler.patrick.utils.objects;

public class TwoVals<A, B> {
	
	public final A a;
	public final B b;
	
	public TwoVals(A a, B b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public int hashCode() {
		final int prime  = 31;
		int       result = 1;
		result = prime * result + ((this.a == null) ? 0 : this.a.hashCode());
		result = prime * result + ((this.b == null) ? 0 : this.b.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TwoVals<?, ?> other = (TwoVals<?, ?>) obj;
		if (this.a == null) {
			if (other.a != null) return false;
		} else if (!this.a.equals(other.a)) return false;
		if (this.b == null) {
			if (other.b != null) return false;
		} else if (!this.b.equals(other.b)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "TwoVals [a=" + this.a + ", b=" + this.b + "]";
	}
	
}

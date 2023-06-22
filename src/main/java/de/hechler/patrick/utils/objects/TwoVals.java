// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
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

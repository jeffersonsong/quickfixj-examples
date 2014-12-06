package quickfix.examples.utility;

public class Tuple2<S, T> {
	private S first;
	private T second;
	private transient int hashCode;

	public Tuple2(S first, T second) {
		this.first = first;
		this.second = second;
	}

	public S getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	public boolean equals(Object other) {
		return this == other || other != null && (other instanceof Tuple2)
				&& equals((Tuple2<S, T>) other);
	}

	public boolean equals(Tuple2<S, T> other) {
		return (first == null && other.first == null || first != null
				&& other.first != null && first.equals(other.first))
				&& (second == null && other.second == null || second != null
						&& other.second != null && second.equals(other.second));
	}

	public int hashCode() {
		if (hashCode == 0) {
			hashCode += (first != null ? first.hashCode() : 0);
			hashCode += hashCode * 37
					+ (second != null ? second.hashCode() : 0);
		}
		return hashCode;
	}

	public String toString() {
		return new StringBuilder().append(first.toString()).append('=')
				.append(second.toString()).toString();
	}
}

package se.uu.ub.cora.javaclient.token.internal;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class ListSupplier<T> implements Supplier<T> {
	private Iterator<T> iterator;

	static <E> Supplier<E> of(E... es) {
		List<E> of = List.of(es);
		return new ListSupplier<>(of);
	}

	public ListSupplier(List<T> list) {
		iterator = list.iterator();
	}

	@Override
	public T get() {
		return iterator.next();
	}
}

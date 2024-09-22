/*
 * Copyright 2019-2022 The OSHI Project Contributors
 * SPDX-License-Identifier: MIT
 */
package oshi.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

/**
 * A memoized function stores the output corresponding to some set of specific inputs. Subsequent calls with remembered
 * inputs return the remembered result rather than recalculating it.
 */
// https://github.com/oshi/oshi/blob/master/oshi-core/src/main/java/oshi/util/Memoizer.java
public final class MemoizedSupplier {

	private MemoizedSupplier() {
	}

	/**
	 * Store a supplier in a delegate function to be computed once, and only again after time to live (ttl) has expired.
	 *
	 * @param <T>
	 *            The type of object supplied
	 * @param original
	 *            The {@link java.util.function.Supplier} to memoize
	 * @param ttlNanos
	 *            Time in nanoseconds to retain calculation. If negative, retain indefinitely.
	 * @return A memoized version of the supplier
	 */
	public static <T> Supplier<T> memoize(Supplier<T> original, Duration ttl) {
		long ttlNanos = ttl.toNanos();

		// Adapted from Guava's ExpiringMemoizingSupplier
		return new Supplier<T>() {
			private final Supplier<T> delegate = original;
			private volatile T value; // NOSONAR squid:S3077
			private volatile long expirationNanos;

			@Override
			public T get() {
				long nanos = expirationNanos;
				long now = System.nanoTime();
				if (nanos == 0 || (ttlNanos >= 0 && now - nanos >= 0)) {
					synchronized (this) {
						if (nanos == expirationNanos) { // recheck for lost race
							T t = delegate.get();
							value = t;
							nanos = now + ttlNanos;
							expirationNanos = (nanos == 0) ? 1 : nanos;
							return t;
						}
					}
				}
				return value;
			}
		};
	}

	/**
	 * Store a supplier in a delegate function to be computed only once.
	 *
	 * @param <T>
	 *            The type of object supplied
	 * @param original
	 *            The {@link java.util.function.Supplier} to memoize
	 * @return A memoized version of the supplier
	 */
	public static <T> Supplier<T> memoize(Supplier<T> original) {
		return memoize(original, Duration.of(1, ChronoUnit.CENTURIES));
	}
}
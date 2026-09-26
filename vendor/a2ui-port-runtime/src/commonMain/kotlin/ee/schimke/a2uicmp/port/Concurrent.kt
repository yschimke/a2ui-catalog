// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

/**
 * `java.util.concurrent.ConcurrentHashMap`, reduced to what upstream calls on it: the `MutableMap`
 * surface plus the atomic `computeIfAbsent` / `compute`. On the JVM the delegate IS a
 * `java.util.concurrent.ConcurrentHashMap` and both calls are its own atomic ones; single-threaded
 * targets (wasmJs) use a plain map.
 */
public class ConcurrentHashMap<K : Any, V : Any>
private constructor(private val delegate: MutableMap<K, V>) : MutableMap<K, V> by delegate {
    public constructor() : this(platformConcurrentMap())

    public fun computeIfAbsent(key: K, mappingFunction: (K) -> V): V =
        delegate.platformComputeIfAbsent(key, mappingFunction)

    public fun compute(key: K, remappingFunction: (K, V?) -> V?): V? =
        delegate.platformCompute(key, remappingFunction)

    override fun equals(other: Any?): Boolean = delegate == other

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = delegate.toString()
}

internal expect fun <K : Any, V : Any> platformConcurrentMap(): MutableMap<K, V>

internal expect fun <K : Any, V : Any> MutableMap<K, V>.platformComputeIfAbsent(
    key: K,
    mappingFunction: (K) -> V,
): V

internal expect fun <K : Any, V : Any> MutableMap<K, V>.platformCompute(
    key: K,
    remappingFunction: (K, V?) -> V?,
): V?

/** `java.util.concurrent.atomic.AtomicInteger`, reduced to the members upstream calls. */
public expect class AtomicInteger(initialValue: Int) {
    public fun get(): Int

    public fun incrementAndGet(): Int

    public fun decrementAndGet(): Int
}

/** `java.util.concurrent.locks.ReentrantLock`, reduced to the members `withLock` needs. */
public expect class ReentrantLock(fair: Boolean) {
    public fun lock()

    public fun unlock()
}

/** `kotlin.concurrent.withLock`, which is JVM-only, over the port's [ReentrantLock]. */
public inline fun <T> ReentrantLock.withLock(action: () -> T): T {
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}

/**
 * `java.util.Map.putIfAbsent`, a JDK default method with no common counterpart. On the JVM the
 * member wins over this extension, so the call site keeps the JDK's behaviour there.
 */
public fun <K, V> MutableMap<K, V>.putIfAbsent(key: K, value: V): V? {
    val existing = get(key)
    if (existing == null) put(key, value)
    return existing
}

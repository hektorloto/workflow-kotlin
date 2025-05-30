package com.squareup.workflow1.ui

/**
 * Normally returns true if [me] and [you] are instances of the same class.
 * If that common class implements [Compatible], both instances must also
 * have the same [Compatible.compatibilityKey].
 *
 * A convenient way to take control over the matching behavior of objects that
 * don't implement [Compatible] is to wrap them with [NamedScreen].
 */
public fun compatible(
  me: Any,
  you: Any
): Boolean {
  return when {
    me::class != you::class -> false
    me !is Compatible -> true
    else -> me.compatibilityKey == (you as Compatible).compatibilityKey
  }
}

/**
 * Implemented by objects whose [compatibility][compatible] requires more nuance
 * than just being of the same type.
 *
 * Renderings that don't implement this interface directly can be distinguished
 * by wrapping them with [NamedScreen].
 */
public interface Compatible {
  /**
   * Instances of the same type are [compatible] iff they have the same [compatibilityKey].
   */
  public val compatibilityKey: String

  public companion object {
    /**
     * Calculates a suitable [Compatible.compatibilityKey] for a given [value], incorporating
     * [name] if that is not blank. Includes the [compatibilityKey] for [value] if it
     * implements [Compatible], to support recursion from wrapping.
     *
     * Style note: [name] is given more prominence than the key generate
     */
    public fun keyFor(
      value: Any,
      name: String = ""
    ): String {
      val key = (value as? Compatible)?.compatibilityKey ?: value::class.java.name

      return name.takeIf { it.isNotEmpty() }?.let { "$name($key)" } ?: key
    }
  }
}

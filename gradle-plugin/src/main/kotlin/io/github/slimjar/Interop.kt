package io.github.slimjar // ktlint-disable filename

import groovy.lang.Closure

public inline fun <T : Any, I> Any.asGroovyClosure(
    default: I,
    crossinline func: (arg: I) -> T
): Closure<T> = object : Closure<T>(this), (I) -> T, () -> T {
    fun doCall(arg: I) = func(arg)
    fun doCall() = doCall(default)
    override fun invoke(p1: I): T = doCall(p1)
    override fun invoke(): T = invoke(default)
}

package com.franosch.type

fun interface Transfer<N, S> {
    fun apply(node: N, input: S): S
}

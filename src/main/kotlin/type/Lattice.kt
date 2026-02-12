package com.franosch.type

interface Lattice<S> {
    fun bottom(): S
    fun join(a: S, b: S): S
    fun equals(a: S, b: S): Boolean
    fun widen(prev: S, next: S): S = next
}

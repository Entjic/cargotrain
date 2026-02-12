package com.franosch.type


interface DiGraph<N> {
    fun successors(n: N): Set<N>
    fun predecessors(n: N): Set<N>
    fun nodes(): Set<N>
}

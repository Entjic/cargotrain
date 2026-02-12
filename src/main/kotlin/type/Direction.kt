package com.franosch.type

interface Direction<N> {
    fun inputs(g: DiGraph<N>, n: N): Set<N>
    fun outputs(g: DiGraph<N>, n: N): Set<N>
}

class Forward<N> : Direction<N> {
    override fun inputs(g: DiGraph<N>, n: N) = g.predecessors(n)
    override fun outputs(g: DiGraph<N>, n: N) = g.successors(n)
}

class Backward<N> : Direction<N> {
    override fun inputs(g: DiGraph<N>, n: N) = g.successors(n)
    override fun outputs(g: DiGraph<N>, n: N) = g.predecessors(n)
}

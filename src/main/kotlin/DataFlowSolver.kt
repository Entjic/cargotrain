package com.franosch

import com.franosch.type.*

class DataflowSolver<N, S>(
    private val graph: DiGraph<N>,
    private val lattice: Lattice<S>,
    private val transfer: Transfer<N, S>,
    private val direction: Direction<N>
) {

    data class NodeState<S>(var `in`: S, var out: S)

    fun solve(initial: Map<N, S>): Map<N, NodeState<S>> {
        val state = mutableMapOf<N, NodeState<S>>()

        // initialize in/out sets
        for (n in graph.nodes()) {
            state[n] = NodeState(
                `in` = initial[n] ?: lattice.bottom(), out = lattice.bottom()
            )
        }

        val worklist = ArrayDeque<N>()
        worklist.addAll(graph.nodes())

        while (worklist.isNotEmpty()) {
            val node = worklist.removeFirst()
            val nodeState = state[node]!!

            // compute in[n] = join of predecessors' out
            val input = direction.inputs(graph, node).map {
                state[it]!!.out
            }.fold(lattice.bottom(), lattice::join)

            val oldIn = nodeState.`in`
            nodeState.`in` = input

            // compute out[n] = transfer(n, in[n])
            val oldOut = nodeState.out
            val apply = transfer.apply(node, nodeState.`in`)
            nodeState.out = lattice.widen(oldOut, apply)

            // enqueue successors if out[n] changed
            if (!lattice.equals(oldOut, nodeState.out) || !lattice.equals(oldIn, input)) {
                direction.outputs(graph, node).forEach { worklist.addLast(it) }
            }
        }

        return state
    }
}

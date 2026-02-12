package com.franosch

import com.franosch.type.Forward
import com.franosch.type.Lattice
import com.franosch.type.Transfer
import java.io.File

fun main(args: Array<String>) {

    val file = File("input.txt").takeIf { it.exists() }
        ?: if (args.isNotEmpty()) File(args[0])
        else throw IllegalArgumentException("No input file found and no CLI argument provided.")

    if (!file.exists()) {
        throw IllegalArgumentException("Input file does not exist: ${file.absolutePath}")
    }

    val graph = FileParser.parse(file)

    val result = findCargoInflow(graph)

    result.forEach { (station, node) ->
        print("IN[${station.id}] = ${node.`in`}, ")
    }
    println()
    result.forEach { (station, node) ->
        print("OUT[${station.id}] = ${node.out}, ")
    }

}

public fun findCargoInflow(graph: Graph): Map<Station, DataflowSolver.NodeState<Set<Int>>> {
    class CargoLattice : Lattice<Set<Int>> {
        override fun bottom(): Set<Int> {
            return setOf()
        }

        override fun equals(a: Set<Int>, b: Set<Int>): Boolean {
            return a == b
        }

        override fun join(a: Set<Int>, b: Set<Int>): Set<Int> {
            return a.union(b)
        }

    }

    class CargoTransfer : Transfer<Station, Set<Int>> {
        override fun apply(node: Station, input: Set<Int>): Set<Int> {
            val copy = input.toMutableSet()
            copy.remove(node.unloadKind)
            copy.add(node.loadKind)
            return copy
        }
    }


    val reachable = mutableMapOf<Station, MutableSet<Int>>()

    for (node in graph.nodes()) {
        reachable[node] = mutableSetOf()
    }

    return DataflowSolver(graph, CargoLattice(), CargoTransfer(), Forward()).solve(reachable)
}


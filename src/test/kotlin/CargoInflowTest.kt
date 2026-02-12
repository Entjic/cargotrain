package com.franosch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CargoInflowTest {


    @Test
    fun testLinearGraph() {
        // 1 -> 2 -> 3
        val s1 = Station(1, unloadKind = 0, loadKind = 1)
        val s2 = Station(2, unloadKind = 1, loadKind = 2)
        val s3 = Station(3, unloadKind = 2, loadKind = 3)
        val edges = mapOf(
            s1 to setOf(s2),
            s2 to setOf(s3),
            s3 to emptySet()
        )
        val graph = Graph(s1, edges)

        val result = findCargoInflow(graph)
        assertEquals(setOf<Int>(), result[s1]?.`in`)
        assertEquals(setOf(1), result[s1]?.out)
        assertEquals(setOf(1), result[s2]?.`in`)
        assertEquals(setOf(2), result[s2]?.out)
        assertEquals(setOf(2), result[s3]?.`in`)
        assertEquals(setOf(3), result[s3]?.out)
    }

    @Test
    fun testBranchingGraph() {
        //   1
        //  / \
        // 2   3
        //  \ /
        //   4
        val s1 = Station(1, 0, 1)
        val s2 = Station(2, 1, 2)
        val s3 = Station(3, 1, 3)
        val s4 = Station(4, 2, 4)
        val edges = mapOf(
            s1 to setOf(s2, s3),
            s2 to setOf(s4),
            s3 to setOf(s4),
            s4 to emptySet()
        )
        val graph = Graph(s1, edges)

        val result = findCargoInflow(graph)

        // arrival cargo (in) at s4 = union of departure cargo from s2 and s3
        val expectedInS4 = result[s2]!!.out.union(result[s3]!!.out)

        assertEquals(expectedInS4, result[s4]?.`in`)
    }

    @Test
    fun testCycleGraph() {
        // 1 -> 2 -> 3 -> 1
        val s1 = Station(1, 0, 1)
        val s2 = Station(2, 1, 2)
        val s3 = Station(3, 2, 3)
        val edges = mapOf(
            s1 to setOf(s2),
            s2 to setOf(s3),
            s3 to setOf(s1)
        )
        val graph = Graph(s1, edges)

        val result = findCargoInflow(graph)

        // Check that the fixpoint stabilizes and in/out sets are finite
        result.forEach { (_, node) ->
            assertTrue(node.`in`.size <= 3)
            assertTrue(node.out.size <= 3)
        }
    }

    @Test
    fun testUnloadLoadInteractions() {
        // 1 -> 2
        val s1 = Station(1, 0, 1)
        val s2 = Station(2, 1, 0)
        val edges = mapOf(
            s1 to setOf(s2),
            s2 to emptySet()
        )
        val graph = Graph(s1, edges)

        val result = findCargoInflow(graph)

        // s2 arrival cargo should contain 1 (from s1)
        assertEquals(setOf(1), result[s2]?.`in`)

        // s2 departure cargo should contain 0 (load 0, unload 1)
        assertEquals(setOf(0), result[s2]?.out)
    }

    @Test
    fun testDisconnectedGraph() {
        // 1 -> 2, 3 isolated
        val s1 = Station(1, 0, 1)
        val s2 = Station(2, 1, 2)
        val s3 = Station(3, 0, 3)
        val edges = mapOf(
            s1 to setOf(s2),
            s2 to emptySet(),
            s3 to emptySet()
        )
        val graph = Graph(s1, edges)

        val result = findCargoInflow(graph)

        // isolated station s3 has no cargo arrival
        assertEquals(emptySet<Int>(), result[s3]?.`in`)
        assertEquals(setOf(3), result[s3]?.out) // only its load
    }

    @Test
    fun testComplexGraph() {
        // Stations: 1-7
        val s1 = Station(1, unloadKind = 0, loadKind = 1)
        val s2 = Station(2, unloadKind = 1, loadKind = 2)
        val s3 = Station(3, unloadKind = 2, loadKind = 3)
        val s4 = Station(4, unloadKind = 0, loadKind = 2)
        val s5 = Station(5, unloadKind = 3, loadKind = 0)
        val s6 = Station(6, unloadKind = 2, loadKind = 3)
        val s7 = Station(7, unloadKind = 1, loadKind = 0)

        val edges = mapOf(
            s1 to setOf(s2, s4),
            s2 to setOf(s3, s5),
            s3 to setOf(s6),
            s4 to setOf(s5),
            s5 to setOf(s7),
            s6 to setOf(s7),
            s7 to emptySet()
        )

        val graph = Graph(s1, edges)

        val result = findCargoInflow(graph)

        // Print arrival (IN) cargo
        println("Arrival cargo:")
        result.forEach { (station, node) ->
            println("IN[${station.id}] = ${node.`in`}")
        }

        // Print departure (OUT) cargo
        println("\nDeparture cargo:")
        result.forEach { (station, node) ->
            println("OUT[${station.id}] = ${node.out}")
        }

        // Example checks (spot check)
        assertEquals(setOf<Int>(), result[s1]?.`in`)
        assertTrue(result[s7]?.`in`?.contains(0) == true)
        assertTrue(result[s7]?.`in`?.contains(3) == true)
    }

}

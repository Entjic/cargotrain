package com.franosch

import com.franosch.type.DiGraph
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.StringTokenizer

// --------------------
// Domain models
// --------------------

data class Station(
    val id: Int,
    val unloadKind: Int,
    val loadKind: Int
)

class Graph(
    val start: Station,
    private val edges: Map<Station, Set<Station>>
) : DiGraph<Station> {

    override fun successors(n: Station): Set<Station> {
        return edges[n]?.toSet() ?: emptySet()
    }

    override fun predecessors(n: Station): Set<Station> {
        // compute reverse edges
        return edges.filter { it.value.contains(n) }.keys.toSet()
    }

    override fun nodes(): Set<Station> {
        return edges.keys
    }
}

// --------------------
// Parser
// --------------------

object FileParser {

    fun parse(file: File): Graph {
        BufferedReader(FileReader(file)).use { reader ->

            val header = nextInts(reader)
            require(header.size == 2) { "First line must contain S T" }

            val stationCount = header[0]
            val trackCount = header[1]

            val stations = mutableMapOf<Int, Station>()

            repeat(stationCount) {
                val parts = nextInts(reader)
                require(parts.size == 3) {
                    "Station line must contain: s c_unload c_load"
                }

                val station = Station(
                    id = parts[0],
                    unloadKind = parts[1],
                    loadKind = parts[2]
                )
                stations[station.id] = station
            }

            val outgoing = mutableMapOf<Station, MutableSet<Station>>()

            // initialize empty adjacency lists
            for (s in stations.keys) {
                outgoing[stations[s]!!] = mutableSetOf()
            }

            repeat(trackCount) {
                val parts = nextInts(reader)
                require(parts.size == 2) {
                    "Track line must contain: s_from s_to"
                }

                val from = parts[0]
                val to = parts[1]

                require(stations.containsKey(from)) {
                    "Unknown station id in track: $from"
                }
                require(stations.containsKey(to)) {
                    "Unknown station id in track: $to"
                }

                outgoing.getValue(stations[from]!!).add(stations[to]!!)
            }

            val startLine = nextInts(reader)
            require(startLine.size == 1) {
                "Last line must contain starting station"
            }
            val startStation = startLine[0]

            require(stations.containsKey(startStation)) {
                "Start station does not exist: $startStation"
            }

            println(outgoing)

            return Graph(
                start = stations[startStation]!!,
                edges = outgoing,
            )
        }
    }

    private fun nextInts(reader: BufferedReader): List<Int> {
        val line = reader.readLine()
            ?: throw IllegalArgumentException("Unexpected end of file")

        val st = StringTokenizer(line)
        val result = mutableListOf<Int>()
        while (st.hasMoreTokens()) {
            result.add(st.nextToken().toInt())
        }
        return result
    }
}

package com.growingspaghetti.eiji_sub_ngram

import android.util.Log
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import kotlin.streams.toList

const val BLOCK_SIZE = 12

fun loadThenFilter(input: String, nums: List<Pair<Int, Int>>, textFile: String): List<String> {
    val lines = load(nums, textFile)
    return lines.parallelStream().filter { it.contains(input) }.sorted().toList()
}

private fun load(nums: List<Pair<Int, Int>>, textFile: String): List<String> {
    Log.d("NGRAM", String.format("%s given:%s", nums, nums.size))
    if (nums.isEmpty()) {
        return emptyList()
    }
    val txtf = RandomAccessFile(textFile, "r")
    txtf.use {
        // must not be parallel
        return nums.stream().sorted { o1, o2 -> o1.first - o2.first }.distinct().map { pair ->
            txtf.seek(pair.first.toLong())
            val line = ByteArray(pair.second)
            txtf.read(line)
            String(line)
        }.toList()
    }
}

fun ngramSearch(keyword: String, ngram: String, index: String): List<Pair<Int, Int>> {
    if (keyword.isEmpty()) {
        return emptyList()
    }
    val searchBlock = ByteArray(BLOCK_SIZE)
    for (c in keyword.toByteArray().iterator().withIndex()) {
        if (c.index > searchBlock.size - 1) {
            break
        }
        searchBlock[c.index] = c.value
    }
    val (begin, end) = determineNgramRange(searchBlock, ngram)
    return determineOccurrenceLinePositions(begin, end, index)
}

private fun determineNgramRange(searchBlock: ByteArray, ngram: String): Pair<Long, Long> {
    val ngramf = RandomAccessFile(ngram, "r")
    ngramf.use {
        val begin = leftLimit(ngramf, searchBlock) / BLOCK_SIZE * 8
        val end = rightLimit(ngramf, searchBlock) / BLOCK_SIZE * 8
        return Pair(begin, end)
    }
}

private fun determineOccurrenceLinePositions(begin: Long, end: Long, index: String): List<Pair<Int, Int>> {
    val nums: MutableList<Pair<Int, Int>> = mutableListOf()
    val indexf = RandomAccessFile(index, "r")
    indexf.use {
        for (p in begin until end step 8) {
            val offset = ByteArray(4)
            val len = ByteArray(4)
            indexf.seek(p)
            indexf.read(offset)
            indexf.seek(p + 4)
            indexf.read(len)
            nums.add(Pair(toInt(offset), toInt(len)))
        }
    }
    return nums
}

private fun toInt(b: ByteArray): Int {
    return ByteBuffer.wrap(b).int
}

private fun leftLimit(index: RandomAccessFile, head: ByteArray): Long {
    val word = ByteArray(BLOCK_SIZE)
    val next = ByteArray(BLOCK_SIZE)
    val blocks = index.length() / BLOCK_SIZE
    var fr = 0L
    var to = blocks * BLOCK_SIZE
    var cursor = (blocks / 2 - 1) * BLOCK_SIZE
    while (true) {
        index.seek(cursor)
        index.read(word)
        index.seek(cursor + BLOCK_SIZE)
        index.read(next)
        for (i in head.indices.reversed()) {
            if (head[i] != 0x00.toByte()) {
                break
            }
            word[i] = 0
            next[i] = 0
        }

        Log.d("NGRAM", String(word))
        if (unsignedArrayCompare(word, head) < 0 && unsignedArrayCompare(head, next) <= 0) {
            return cursor + BLOCK_SIZE
        }
        if (unsignedArrayCompare(head, word) <= 0) {
            to = cursor
            val update = (cursor - fr) / BLOCK_SIZE / 2 * BLOCK_SIZE
            if (update == 0L) {
                return cursor
            }
            cursor -= update
        } else if (unsignedArrayCompare(word, head) < 0) {
            fr = cursor
            val update = (to - cursor) / BLOCK_SIZE / 2 * BLOCK_SIZE
            if (update == 0L) {
                return cursor
            }
            cursor += update
        }
    }
}

private fun rightLimit(index: RandomAccessFile, head: ByteArray): Long {
    val word = ByteArray(BLOCK_SIZE)
    val next = ByteArray(BLOCK_SIZE)
    val blocks = index.length() / BLOCK_SIZE
    var fr = 0L
    var to = blocks * BLOCK_SIZE
    var cursor = (blocks / 2 - 1) * BLOCK_SIZE
    while (true) {
        index.seek(cursor)
        index.read(word)
        index.seek(cursor + BLOCK_SIZE)
        index.read(next)
        for (i in head.indices.reversed()) {
            if (head[i] != 0x00.toByte()) {
                break
            }
            word[i] = 0
            next[i] = 0
        }

        Log.d("NGRAM", String(word))
        if (unsignedArrayCompare(word, head) <= 0 && unsignedArrayCompare(head, next) < 0) {
            return cursor + BLOCK_SIZE
        }
        if (unsignedArrayCompare(head, word) < 0) {
            to = cursor
            val update = (cursor - fr) / BLOCK_SIZE / 2 * BLOCK_SIZE
            if (update == 0L) {
                return cursor
            }
            cursor -= update
        } else if (unsignedArrayCompare(word, head) <= 0) {
            fr = cursor
            val update = (to - cursor) / BLOCK_SIZE / 2 * BLOCK_SIZE
            if (update == 0L) {
                return cursor
            }
            cursor += update
        }
    }
}

// in Java, 同型=[-27 -112 -116 -27 -98 -117] having negative values
private fun unsignedArrayCompare(a: ByteArray, b: ByteArray): Int {
    for (i in 0 until BLOCK_SIZE) {
        val ua = a[i].toUByte()
        val ub = b[i].toUByte()
        if (ua < ub) {
            return -1
        }
        if (ua > ub) {
            return +1
        }
    }
    return 0
}
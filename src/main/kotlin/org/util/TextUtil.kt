package org.util

import java.util.*

/**
 * Created by Stan van der Bend for Empyrean at 04/06/2018.
 *
 * @author Stan van der Bend
 * @author Graham
 */
object TextUtil {

    /**
     * This is an implementation of the levenstein algorithm.
     * In essence, this can be used to numerically represent the similarity in two given strings.
     *
     * @param a first input
     * @param b second input
     *
     * @return the numerical similarity of two given inputs.
     */
    fun calculateLevensteinDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) {
            for (j in 0..b.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> dp[i][j] = min(
                        dp[i - 1][j - 1] + costOfSubstitution(a[i - 1], b[j - 1]),
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1
                    )
                }
            }
        }

        return dp[a.length][b.length]
    }

    private fun costOfSubstitution(a: Char, b: Char): Int {
        return if (a == b) 0 else 1
    }

    private fun min(vararg numbers: Int): Int {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE)
    }


}

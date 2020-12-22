package net.imglib2.i2k2020.intro.tasks

import bdv.util.BdvFunctions
import bdv.util.BdvOptions
import net.imglib2.Localizable
import net.imglib2.RealLocalizable
import net.imglib2.position.FunctionRandomAccessible
import net.imglib2.position.FunctionRealRandomAccessible
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.type.numeric.integer.UnsignedLongType
import net.imglib2.util.Intervals

object KT12_Functions {
    /**
     * Define the Julia fractal, which can return a value for any real
     * coordinate and display with BigDataViewer
     */
    fun fractal() {
        val a = 0.2
        val b = 0.8
        val n = 1000
        val julia = FunctionRealRandomAccessible(
            2,
            { x: RealLocalizable, y: UnsignedLongType ->
                var i: Long = 0
                var v = 0.0
                var c = x.getDoublePosition(0)
                var d = x.getDoublePosition(1)
                while (i < n && v < 4096) {
                    val br = c * c - d * d
                    d = 2 * c * d
                    c = br + a
                    d += b
                    v = Math.sqrt(c * c + d * d)
                    ++i
                }
                y.set(i)
            }) { UnsignedLongType() }
        BdvFunctions.show(julia, Intervals.createMinMax(-1, -1, 1, 1), "", BdvOptions.options().is2D)
            .setDisplayRange(0.0, 32.0)
    }

    /**
     * Define a function on integer coordinates that makes a funny pattern and
     * display with BigDataViewer
     */
    fun funnyCheckerBoard() {
        val checkerboard = FunctionRandomAccessible(
            2,
            { location: Localizable, value: UnsignedByteType ->
                value.integer = Math.abs(location.getIntPosition(0)) % 3 +
                        -Math.abs(location.getIntPosition(1)) % 3
            }) { UnsignedByteType() }
        BdvFunctions.show(checkerboard, Intervals.createMinMax(-10, -10, 10, 10), "", BdvOptions.options().is2D)
            .setDisplayRange(0.0, 1.0)
    }

    @JvmStatic
    fun main(args: Array<String>) {

        // function
        funnyCheckerBoard()

        // real function
        fractal()
    }
}
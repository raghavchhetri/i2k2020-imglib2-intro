package net.imglib2.i2k2020.intro.tasks

import net.imglib2.IterableInterval
import net.imglib2.RandomAccessibleInterval
import net.imglib2.type.Type
import net.imglib2.util.Pair
import net.imglib2.util.Util
import net.imglib2.util.ValuePair

object KT3_GenericAccess {
    /**
     * Using an Iterable and generic types to compute the maximal value in an
     * image
     *
     * @param iterable
     * @return
     */
    fun <T> max(iterable: Iterable<T>): T where T : Comparable<T>?, T : Type<T>? {

        // will hold the maximal value, init with first value, important to make
        // a copy because of NativeTypes
        val max = iterable.iterator().next()!!.copy()

        // iterate all pixels
        for (pixelValue in iterable) if (pixelValue!!.compareTo(max) > 0) max!!.set(pixelValue)
        return max
    }

    /**
     * Using an IterableInterval and generic types to compute the maximal value
     * in an image
     *
     * @param iterable
     * @return
     */
    fun <T> maxWithLocation(iterable: IterableInterval<T>): Pair<T, LongArray> where T : Comparable<T>?, T : Type<T>? {

        // iterates the IterableInterval
        val cursor = iterable.localizingCursor()

        // will hold the maximal value, init with first value, important to make
        // a copy because of NativeTypes
        val max = cursor.next()!!.copy()

        // the location of the max value, init with first value
        val position = LongArray(iterable.numDimensions())
        cursor.localize(position)
        while (cursor.hasNext()) {
            val pixelValue = cursor.next()
            if (pixelValue!!.compareTo(max) > 0) {
                max!!.set(pixelValue)
                cursor.localize(position)
            }
        }
        return ValuePair(max, position)
    }

    /**
     * Using a RandomAccessibleInterval and generic types to return the value in
     * the center of an image
     *
     * @param rai
     * @return
     */
    fun <T> centerValue(rai: RandomAccessibleInterval<T>): T {

        // create a RandomAccess
        val ra = rai.randomAccess()

        // set it to the center pixel in all dimensions
        for (d in 0 until rai.numDimensions()) ra.setPosition((rai.max(d) - rai.min(d)) / 2 + rai.min(d), d)
        return ra.get()
    }

    @JvmStatic
    fun main(args: Array<String>) {

        // Img implements RandomAccessibleInterval and IterableInterval
        val imgF = KT1_CreateImg.createNewImg()

        // compute the maximal value
        println(
            """
                
                Max value: ${
                max(
                    imgF
                )
            }
                """.trimIndent()
        )

        // compute the maximal value and it's location
        val max = maxWithLocation(imgF)
        println(
            """
Max value: ${max.a} @ ${Util.printCoordinates(max.b)}"""
        )

        // output the value in the center of the image
        println(
            """
                
                Center value: ${
                centerValue(
                    imgF
                )
            }
                """.trimIndent()
        )
    }
}
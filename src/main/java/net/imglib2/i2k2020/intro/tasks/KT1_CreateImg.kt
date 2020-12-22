package net.imglib2.i2k2020.intro.tasks

import net.imglib2.img.Img
import net.imglib2.img.array.ArrayImgs
import net.imglib2.type.numeric.real.FloatType
import net.imglib2.util.Util
import java.util.*
import kotlin.jvm.JvmStatic

/**
 * Illustrates how to use an array in ImgLib2, how to use iteration, random
 * access
 */
object KT1_CreateImg {
    /**
     * Use ArrayImg to create a 5x5 float image and fill with random numbers,
     * then print out its values and locations. Finally set the center pixel to
     * 1000 and print out again.
     *
     * @return
     */
    @JvmStatic
    fun createNewImg(): Img<FloatType> {

        // create a 5x5 pixel image of type float
        val img: Img<FloatType> = ArrayImgs.floats(5, 5)

        // generate a random number generator
        val rnd = Random()

        // iterate all pixels of that image and set it to a random number
        for (pixelValue in img) pixelValue.set(rnd.nextFloat())

        // print out all pixel values together with their position using a
        // Cursor
        val cursor = img.localizingCursor()
        println("random values: ")
        while (cursor.hasNext()) println("value=" + cursor.next().get() + " @ " + Util.printCoordinates(cursor))

        // random access to a central pixel
        val ra = img.randomAccess()
        ra.setPosition(longArrayOf(2, 2))
        ra.get().set(1000.0f)
        println("\ncenter value reset: ")
        cursor.reset()
        while (cursor.hasNext()) println("value=" + cursor.next().get() + " @ " + Util.printCoordinates(cursor))
        return img
    }

    /**
     * Create an image from an existing float[] array and print out their
     * locations and values.
     *
     * @return
     */
    @JvmStatic
    fun createImgFromArray(): Img<FloatType> {
        val array = FloatArray(5 * 5)
        for (i in array.indices) array[i] = i.toFloat()
        val img: Img<FloatType> = ArrayImgs.floats(array, 5, 5)

        // print out all pixel values together with their position using a
        // Cursor
        val cursor = img.cursor()
        println("\nincreasing numbers: ")
        while (cursor.hasNext()) println("value=" + cursor.next().get() + " @ " + Util.printCoordinates(cursor))
        return img
    }

    @JvmStatic
    fun main(args: Array<String>) {

        // create a new image, iterate and perform random access
        createNewImg()

        // create a new image from an existing float[] array
        createImgFromArray()
    }
}
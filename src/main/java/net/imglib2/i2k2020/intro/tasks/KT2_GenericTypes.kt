package net.imglib2.i2k2020.intro.tasks

import net.imglib2.img.Img
import net.imglib2.img.array.ArrayImgs
import net.imglib2.type.numeric.ARGBType
import net.imglib2.type.numeric.NumericType
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.complex.ComplexDoubleType
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.type.numeric.real.FloatType

object KT2_GenericTypes {
    /**
     * Add a value from an image of [NumericType]
     *
     * @param img
     * - the input
     * @param value
     * - the value
     */
    fun <T : NumericType<T>?> add(img: Img<T>, value: T) {
        for (pixelValue in img) pixelValue!!.add(value)
    }

    /**
     * Subtract a value from an image of [RealType]
     *
     * @param img
     * - the input
     */
    fun <T : RealType<T>?> sqrt(img: Img<T>) {
        for (pixelValue in img) pixelValue!!.setReal(Math.sqrt(pixelValue.realDouble))
    }

    @JvmStatic
    fun main(args: Array<String>) {

        // create small images of various types
        val imgF: Img<FloatType> = ArrayImgs.floats(5, 5)
        val imgUB: Img<UnsignedByteType> = ArrayImgs.unsignedBytes(5, 5)
        val imgC: Img<ComplexDoubleType> = ArrayImgs.complexDoubles(5, 5)
        val imgARGB: Img<ARGBType> = ArrayImgs.argbs(5, 5)

        // adding works with all these types
        add(imgF, FloatType(2.5f))
        add(imgUB, UnsignedByteType(5))
        add(imgC, ComplexDoubleType(1.0, 2.0))
        add(imgARGB, ARGBType(ARGBType.rgba(128, 64, 64, 0)))
        for (type in imgARGB) println(type)

        // square root only with some of them
        sqrt(imgF)
        sqrt(imgUB)
        // sqrt( imgC ); <<< build error
        // sqrt( imgARGB ); <<< build error
    }
}
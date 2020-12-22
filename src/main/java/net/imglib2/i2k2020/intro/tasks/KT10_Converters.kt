package net.imglib2.i2k2020.intro.tasks

import ij.IJ
import ij.ImageJ
import net.imglib2.RandomAccessibleInterval
import net.imglib2.converter.Converters
import net.imglib2.img.display.imagej.ImageJFunctions
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.type.numeric.ARGBType
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.type.numeric.real.DoubleType
import net.imglib2.type.numeric.real.FloatType
import net.imglib2.view.Views

object KT10_Converters {
    /**
     * Show how to use virtual converters to transform types (read-only) and
     * perform pixel-wise computation, a cosine of the square root in this case
     *
     * @param img
     */
    fun <T : RealType<T>?> displayCosine(img: RandomAccessibleInterval<T>?) {

        // virtually convert to float
        val converted = Converters.convert(
            img,
            { i: T, o: FloatType ->
                o.set(
                    i!!.realFloat
                )
            }, FloatType()
        )

        // virtually compute with double precision
        val result = Converters.convert(
            converted,
            { i: FloatType, o: DoubleType ->
                o.set(
                    Math.cos(Math.sqrt(i.get().toDouble()))
                )
            }, DoubleType()
        )
        ImageJFunctions.show(result)
    }

    /**
     * Using a writable converter to set all red values of an ARGB image to 0
     *
     * @param img
     */
    fun writeConverter1(img: RandomAccessibleInterval<ARGBType>?) {
        ImageJFunctions.show(img).title = "original"

        // uses a WriteConvertedRandomAccessibleInterval, setting values is
        // supported
        for (red in Views.iterable(Converters.argbChannel(img, 1))) red.set(0)
        ImageJFunctions.show(img).title = "red=0"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        ImageJ()
        val blobs: RandomAccessibleInterval<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif")
        )
        val clown: RandomAccessibleInterval<ARGBType> = ImagePlusImgs.from(
            IJ.openImage("http://imagej.nih.gov/ij/images/clown.jpg")
        )

        // cosine of the blobs image
        displayCosine(blobs)

        // cosine of the green channel of the clown image
        displayCosine(Converters.convert(clown,
            { i: ARGBType, o: UnsignedByteType ->
                o.set(
                    ARGBType.green(i.get())
                )
            }, UnsignedByteType()
        )
        )

        // set all red values of an ARGB image to 0
        writeConverter1(clown)
    }
}
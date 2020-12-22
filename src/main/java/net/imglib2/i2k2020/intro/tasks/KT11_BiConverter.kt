package net.imglib2.i2k2020.intro.tasks

import ij.IJ
import ij.ImageJ
import net.imglib2.RandomAccessibleInterval
import net.imglib2.algorithm.gauss3.Gauss3
import net.imglib2.converter.Converters
import net.imglib2.img.Img
import net.imglib2.img.array.ArrayImgs
import net.imglib2.img.display.imagej.ImageJFunctions
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.type.numeric.real.DoubleType
import net.imglib2.type.numeric.real.FloatType
import net.imglib2.util.Intervals
import net.imglib2.view.Views

object KT11_BiConverter {
    /**
     * Virtually compute a Difference-of-Gaussian from two gaussian convolution
     *
     * @param img
     * @param sigma1
     * @param sigma2
     */
    fun <T : RealType<T>?> differenceOfGaussian(img: RandomAccessibleInterval<T>?, sigma1: Double, sigma2: Double) {
        val img1: RandomAccessibleInterval<FloatType> = ArrayImgs.floats(*Intervals.dimensionsAsLongArray(img))
        val img2: RandomAccessibleInterval<FloatType> = ArrayImgs.floats(*Intervals.dimensionsAsLongArray(img))
        Gauss3.gauss(sigma1, Views.extendMirrorSingle(img), img1)
        Gauss3.gauss(sigma2, Views.extendMirrorSingle(img), img2)
        ImageJFunctions.show(img1)
        ImageJFunctions.show(img2)
        val dog = Converters.convert(
            img1,
            img2,
            { i1: FloatType, i2: FloatType, o: DoubleType ->
                o.set(
                    i1.realDouble - i2.realDouble
                )
            },
            DoubleType()
        )
        ImageJFunctions.show(dog)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        ImageJ()
        val blobs: RandomAccessibleInterval<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif")
        )

        // 2d DoG
        differenceOfGaussian(blobs, 8.0, 12.0)
        val img: Img<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("https://preibischlab.mdc-berlin.de/download/lightsheet0.tif.zip")
        )

        // 3d DoG
        differenceOfGaussian(img, 3.0, 4.0)
    }
}
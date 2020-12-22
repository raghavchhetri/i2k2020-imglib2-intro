package net.imglib2.i2k2020.intro.tasks

import bdv.util.BdvFunctions
import bdv.util.BdvOptions
import bdv.util.BdvStackSource
import ij.IJ
import net.imglib2.RandomAccessibleInterval
import net.imglib2.converter.Converters
import net.imglib2.img.Img
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.type.logic.BitType
import net.imglib2.type.numeric.ARGBType
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType

object ComplexTask_KT1_Thresholding {
    /**
     * Using a converter to implement a thresholding operation
     *
     * @param img
     * @param threshold
     * @return
     */
    fun <T : Comparable<T>?> threshold(
        img: RandomAccessibleInterval<T>?,
        threshold: T
    ): RandomAccessibleInterval<BitType> {

        // virtual thresholding with a converter
        return Converters.convert(
            img,
            { i: T, o: BitType ->
                o.set(
                    i!!.compareTo(threshold) >= 0
                )
            }, BitType()
        )
    }

    /**
     * Visualize the result of the thresholding
     *
     * @param img
     * @param threshold
     */
    fun <T : RealType<T>?> testThresholding(
        img: RandomAccessibleInterval<T>,
        threshold: T
    ) {
        val segmented = threshold(img, threshold)

        // show the realrandomaccessibles
        var bdv: BdvStackSource<*>
        var options = BdvOptions()
        if (img.numDimensions() == 2) options = options.is2D

        // BigDataViewer can show RealRandomAccessibles directly (ImageJ cannot)
        bdv = BdvFunctions.show(img, "input", options)
        bdv.setColor(ARGBType(ARGBType.rgba(0, 255, 0, 0)))
        bdv = BdvFunctions.show(segmented, "segmented", options.addTo(bdv))
        bdv.setColor(ARGBType(ARGBType.rgba(255, 0, 255, 0)))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val blobs: Img<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif")
        )
        testThresholding(blobs, UnsignedByteType(128))
    }
}
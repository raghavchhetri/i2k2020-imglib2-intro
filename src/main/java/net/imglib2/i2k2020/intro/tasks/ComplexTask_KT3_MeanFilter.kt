package net.imglib2.i2k2020.intro.tasks

import bdv.util.BdvFunctions
import bdv.util.BdvOptions
import bdv.util.BdvStackSource
import bdv.viewer.DisplayMode
import ij.IJ
import ij.ImageJ
import net.imglib2.RandomAccessibleInterval
import net.imglib2.converter.Converters
import net.imglib2.converter.RealFloatConverter
import net.imglib2.img.Img
import net.imglib2.img.display.imagej.ImageJFunctions
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.type.numeric.real.FloatType
import net.imglib2.view.Views
import java.util.ArrayList

object ComplexTask_KT3_MeanFilter {
    /**
     * Virtually compute the mean filter (d=3) in a specific dimension
     *
     * @param img
     * @param type
     * - precision (e.g. FloatType)
     * @param dim
     * - which dimension
     * @param normalize
     * - whether to normalize the output (divide by 3) or not
     * @return
     */
    fun <T : RealType<T>?, S : RealType<S>?> meanFilter3(
        img: RandomAccessibleInterval<T>,
        type: S,
        dim: Int,
        normalize: Boolean
    ): RandomAccessibleInterval<S> {
        val translation = LongArray(img.numDimensions())
        translation[dim] = 1

        // sum of left and right pixel
        var mean: RandomAccessibleInterval<S> = Converters.convert(
            ComplexTask_KT2_Gradient.shiftInverseMirrored(img, translation),
            ComplexTask_KT2_Gradient.shiftMirrored(img, translation),
            { i1, i2, o ->
                if (i1 != null) {
                    if (i2 != null) {
                        o?.setReal(i1.getRealDouble() + i2.getRealDouble())
                    }
                }
            },
            //{ i1, i2, o -> o.setReal(i1.getRealDouble() + i2.getRealDouble()) },
            type
        )

        // sum of center + left + right
        mean = Converters.convert(
            mean,
            img,
            { i1: S, i2: T, o: S -> o!!.setReal(i1!!.realDouble + i2!!.realDouble) },
            type
        )
        return if (normalize) Converters.convert(mean,
            { i: S, o: S -> o!!.setReal(i!!.realDouble / 3.0) }, type
        ) else mean
    }

    /**
     * Virtually compute the mean filter (d=3) in all dimensions
     *
     * @param img
     * @param type
     * - precision (e.g. FloatType)
     */
    fun <T : RealType<T>?, S : RealType<S>?> meanFilter3(
        img: RandomAccessibleInterval<T>,
        type: S
    ): RandomAccessibleInterval<S> {
        var mean = meanFilter3(img, type, 0, false)
        for (d in 1 until img.numDimensions()) mean = meanFilter3(mean, type, d, false)
        return Converters.convert(mean,
            { i: S, o: S ->
                o!!.setReal(
                    i!!.realDouble / Math.pow(
                        3.0,
                        img.numDimensions().toDouble()
                    )
                )
            }, type
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        ImageJ()
        val precision = FloatType()
        val blobs: RandomAccessibleInterval<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("http://imagej.nih.gov/ij/images/boats.gif")
        )
        val imgs = ArrayList<RandomAccessibleInterval<FloatType>>()
        imgs.add(Converters.convert(blobs, RealFloatConverter(), precision))
        imgs.add(meanFilter3(blobs, precision))
        ImageJFunctions.show(Views.stack(imgs))
        val img: Img<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("https://preibischlab.mdc-berlin.de/download/lightsheet0.tif.zip")
        )
        var bdv: BdvStackSource<*>
        bdv = BdvFunctions.show(img, "input")
        bdv = BdvFunctions.show(meanFilter3(img, precision), "mean 3x3x3", BdvOptions().addTo(bdv))
        bdv.bdvHandle.viewerPanel.setDisplayMode(DisplayMode.SINGLE)
        //bdv.bdvHandle.viewerPanel.setDisplayMode(bdv.viewer.DisplayMode.SINGLE)
        bdv.setDisplayRange(0.0, 255.0)
        bdv.setCurrent()
    }
}

// Added "null checks" (lines 47-54)
// Replaced bdv.viewer.DisplayMode.SINGLE by DisplayMode.SINGLE (lines 112-113)
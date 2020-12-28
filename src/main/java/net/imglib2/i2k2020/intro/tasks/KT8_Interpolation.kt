package net.imglib2.i2k2020.intro.tasks

import bdv.util.BdvFunctions
import bdv.util.BdvOptions
import bdv.util.BdvStackSource
import bdv.viewer.DisplayMode
import ij.IJ
import net.imglib2.FinalInterval
import net.imglib2.Interval
import net.imglib2.RandomAccessibleInterval
import net.imglib2.RealRandomAccessible
import net.imglib2.img.Img
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.view.Views

object KT8_Interpolation {
    /**
     * Visualize different types of interplolation in BigDataViewer, which can
     * show RealRandomAccesses(!) directly
     *
     * @param img
     */
    fun <T : RealType<T>?> interpolation(img: RandomAccessibleInterval<T>) {

        // which area to render
        val interval: Interval = FinalInterval(img)

        // nearest neighbor interpolation
        val nn = Views.interpolate(
            Views.extendZero(img),
            NearestNeighborInterpolatorFactory()
        )

        // linear interpolation
        val linear = Views.interpolate(
            Views.extendZero(img),
            NLinearInterpolatorFactory()
        )

        // linear interpolation
        val lanczos = Views.interpolate(
            Views.extendZero(img),
            LanczosInterpolatorFactory()
        )

        // show the realrandomaccessibles
        var bdv: BdvStackSource<*>
        var options = BdvOptions()
        if (img.numDimensions() == 2) options = options.is2D

        // BigDataViewer can show RealRandomAccessibles directly (ImageJ cannot)
        bdv = BdvFunctions.show(nn, interval, "nearest neighbor", options)
        bdv = BdvFunctions.show(linear, interval, "linear", options.addTo(bdv))
        bdv = BdvFunctions.show(lanczos, interval, "lanczos", options.addTo(bdv))

        // enable single-source mode
        //bdv.bdvHandle.viewerPanel.setDisplayMode(bdv.viewer.DisplayMode.SINGLE)
        bdv.bdvHandle.viewerPanel.setDisplayMode(DisplayMode.SINGLE)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val blobs: Img<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif")
        )
        interpolation(blobs)
    }
}
//Note: Replaced bdv.viewer.DisplayMode.SINGLE by DisplayMode.SINGLE (lines 62-63)
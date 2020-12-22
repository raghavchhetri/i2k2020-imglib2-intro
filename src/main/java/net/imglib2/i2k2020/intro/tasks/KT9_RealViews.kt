package net.imglib2.i2k2020.intro.tasks

import bdv.util.BdvFunctions
import bdv.util.BdvOptions
import bdv.util.BdvStackSource
import ij.IJ
import net.imglib2.*
import net.imglib2.img.Img
import net.imglib2.img.display.imagej.ImageJFunctions
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory
import net.imglib2.realtransform.AffineTransform2D
import net.imglib2.realtransform.RealTransformRandomAccessible
import net.imglib2.realtransform.RealViews
import net.imglib2.realtransform.ThinplateSplineTransform
import net.imglib2.type.numeric.ARGBType
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.util.Intervals
import net.imglib2.view.Views
import java.util.*

object KT9_RealViews {
    /**
     * Perform a 45 degree rotation on a 2d image and display the result in
     * BigDataViewer and ImageJ. ImageJ requires to raster the RealRandomAccess
     * and to estimate the bounding box.
     *
     * @param img
     */
    fun <T : RealType<T>?> rotation(img: RandomAccessibleInterval<T>) {
        val rotation = AffineTransform2D()
        rotation.rotate(Math.toRadians(45.0))

        // linear interpolation
        val linear = Views.interpolate(
            Views.extendZero(img),
            NLinearInterpolatorFactory()
        )
        val transformed: RealRandomAccessible<T> = RealViews.affine(linear, rotation)

        // show the realrandomaccessibles
        var bdv: BdvStackSource<*>
        var options = BdvOptions()
        if (img.numDimensions() == 2) options = options.is2D

        // BigDataViewer can show RealRandomAccessibles directly (ImageJ cannot)
        bdv = BdvFunctions.show(img, "input", options)
        bdv.setColor(ARGBType(ARGBType.rgba(0, 255, 0, 0)))
        bdv = BdvFunctions.show(transformed, img, "transformed", options.addTo(bdv))
        bdv.setColor(ARGBType(ARGBType.rgba(255, 0, 255, 0)))

        // find the boundaries of the rotated image
        val realInterval: RealInterval = rotation.estimateBounds(img)
        val intervalTransformed = Intervals.largestContainedInterval(realInterval)

        // the interval we want to display is the union of the interval from the
        // transformed
        // and the original image
        val interval: Interval = Intervals.union(intervalTransformed, img)

        // display as a rastered image in ImageJ
        val rastered: RandomAccessible<T> = Views.raster(transformed)
        val rasteredInterval: RandomAccessibleInterval<T> = Views.interval(rastered, interval)
        ImageJFunctions.show(Views.interval(Views.extendZero(img), interval)).title = "transformed"
        ImageJFunctions.show(rasteredInterval).title = "rotated"
    }

    /**
     * Illustrate how to do a non-rigid n-dimensional ThinPlateSpline
     * transformation and display with BigDataViewer
     *
     * @param img
     */
    fun <T : RealType<T>?> thinPlateSpline(img: RandomAccessibleInterval<T>) {
        val maxDist = 5.0
        val numPoints = 20
        val n = img.numDimensions()
        val p = Array(n) { DoubleArray(numPoints) }
        val q = Array(n) { DoubleArray(numPoints) }
        val rnd = Random(2323)
        for (i in 0 until numPoints) {
            for (d in 0 until n) {
                p[d][i] = (rnd.nextInt(img.dimension(d).toInt()) + img.min(d)).toDouble()
                q[d][i] = p[d][i] + (rnd.nextDouble() - 0.5) * maxDist * 2
            }
        }
        val tps = ThinplateSplineTransform(p, q)
        // WrappedIterativeInvertibleRealTransform<ThinplateSplineTransform>
        // tpsInv = new WrappedIterativeInvertibleRealTransform<>( tps );

        // linear interpolation
        val linear = Views.interpolate(
            Views.extendZero(img),
            NLinearInterpolatorFactory()
        )
        val transformed: RealRandomAccessible<T> =  // RealViews.transform( linear, tpsInv );
            RealTransformRandomAccessible(linear, tps)

        // show the realrandomaccessibles
        var bdv: BdvStackSource<*>
        var options = BdvOptions()
        if (img.numDimensions() == 2) options = options.is2D

        // BigDataViewer can show RealRandomAccessibles directly (ImageJ cannot)
        bdv = BdvFunctions.show(img, "input", options)
        bdv.setColor(ARGBType(ARGBType.rgba(0, 255, 0, 0)))
        bdv = BdvFunctions.show(transformed, img, "thin-plate transformed", options.addTo(bdv))
        bdv.setColor(ARGBType(ARGBType.rgba(255, 0, 255, 0)))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val blobs: Img<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif")
        )

        // apply a 45 degree rotation to a 2d image
        rotation(blobs)

        // random deformations to an nd image
        thinPlateSpline(blobs)
    }
}
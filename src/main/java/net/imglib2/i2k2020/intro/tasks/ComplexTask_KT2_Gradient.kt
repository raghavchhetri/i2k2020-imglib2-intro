package net.imglib2.i2k2020.intro.tasks

import bdv.util.BdvFunctions
import bdv.util.BdvOptions
import bdv.util.BdvStackSource
import ij.IJ
import ij.ImageJ
import net.imglib2.RandomAccessibleInterval
import net.imglib2.converter.Converters
import net.imglib2.converter.RealFloatConverter
import net.imglib2.img.Img
import net.imglib2.img.display.imagej.ImageJFunctions
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.type.numeric.ARGBType
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.type.numeric.real.FloatType
import net.imglib2.view.Views
import java.util.ArrayList

object ComplexTask_KT2_Gradient {
    /**
     * virtually create a RAI that is mirrored and shifted according to the
     * translation vector
     *
     * @param img
     * @param translation
     * @return
     */
    fun <T> shiftMirrored(img: RandomAccessibleInterval<T>?, translation: LongArray): RandomAccessibleInterval<T> {
        return Views.interval(
            Views.translate(
                Views.extendMirrorSingle(img),
                *translation
            ),
            img
        )
    }

    /**
     * virtually create a RAI that is mirrored and inversely shifted according
     * to the translation vector
     *
     * @param img
     * @param translation
     * @return
     */
    fun <T> shiftInverseMirrored(
        img: RandomAccessibleInterval<T>?,
        translation: LongArray
    ): RandomAccessibleInterval<T> {
        return Views.interval(
            Views.translateInverse(
                Views.extendMirrorSingle(img),
                *translation
            ),
            img
        )
    }

    /**
     * Compute the gradient in a specific dimension
     *
     * @param img
     * - the input
     * @param type
     * - the precision (e.g. FloatType)
     * @param dim
     * - which dimension
     * @return
     */
    fun <T : RealType<T>?, S : RealType<S>?> gradient(
        img: RandomAccessibleInterval<T>,
        type: S,
        dim: Int
    ): RandomAccessibleInterval<S> {
        val translation = LongArray(img.numDimensions())
        translation[dim] = 1
        return Converters.convert(
            shiftMirrored(img, translation),
            shiftInverseMirrored(img, translation),
            { i1: T, i2: T, o: S -> o!!.setReal(i1!!.realDouble - i2!!.realDouble) },
            type
        )
    }

    /**
     * Compute the magnitude of the gradient in all dimensions
     *
     * @param img
     * @param type
     * - the precision (e.g. FloatType)
     * @return
     */
    fun <T : RealType<T>?, S : RealType<S>?> gradientMagnitude(
        img: RandomAccessibleInterval<T>,
        type: S
    ): RandomAccessibleInterval<S> {
        return if (img.numDimensions() == 1) {
            gradient(img, type, 0)
        } else {
            // a bi-converter for the sqr(gradients) in x and y
            var gradients = Converters.convert(
                gradient(img, type, 0),
                gradient(img, type, 1),
                { i1: S, i2: S, o: S ->
                    o!!.setReal(
                        Math.pow(i1!!.realDouble, 2.0) + Math.pow(
                            i2!!.realDouble, 2.0
                        )
                    )
                },
                type
            )

            // we covered the first two dimensions
            for (d in 2 until img.numDimensions()) {
                // add the sqr(gradient) of the next dimensions to it
                gradients = Converters.convert(
                    gradients,
                    gradient(img, type, d),
                    { i1: S, i2: S, o: S ->
                        o!!.setReal(
                            i1!!.realDouble + Math.pow(
                                i2!!.realDouble,
                                2.0
                            )
                        )
                    },
                    type
                )
            }

            // still do Math.sqrt
            Converters.convert(gradients,
                { i: S, o: S -> o!!.setReal(Math.sqrt(i!!.realDouble)) }, type
            )
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        ImageJ()
        val blobs: RandomAccessibleInterval<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif")
        )
        val imgs = ArrayList<RandomAccessibleInterval<FloatType>>()
        imgs.add(Converters.convert(blobs, RealFloatConverter(), FloatType()))
        imgs.add(gradient(blobs, FloatType(), 0))
        imgs.add(gradient(blobs, FloatType(), 1))
        imgs.add(gradientMagnitude(blobs, FloatType()))
        ImageJFunctions.show(Views.stack(imgs))
        val img: Img<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("https://preibischlab.mdc-berlin.de/download/lightsheet0.tif.zip")
        )
        var bdv: BdvStackSource<*>
        bdv = BdvFunctions.show(img, "input")
        bdv = BdvFunctions.show(gradientMagnitude(img, FloatType()), "gradient", BdvOptions().addTo(bdv))
        bdv.setColor(ARGBType(ARGBType.rgba(0, 255, 0, 0)))
        bdv.setDisplayRange(0.0, 255.0)
    }
}
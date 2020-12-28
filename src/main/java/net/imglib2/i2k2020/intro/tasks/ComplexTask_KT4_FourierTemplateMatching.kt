package net.imglib2.i2k2020.intro.tasks

import ij.IJ
import ij.ImageJ
import net.imglib2.FinalDimensions
import net.imglib2.Interval
import net.imglib2.RandomAccessibleInterval
import net.imglib2.algorithm.fft2.FFT
import net.imglib2.algorithm.fft2.FFTConvolution
import net.imglib2.algorithm.fft2.FFTMethods
import net.imglib2.algorithm.gauss3.Gauss3
import net.imglib2.converter.Converters
import net.imglib2.converter.RealFloatConverter
import net.imglib2.converter.readwrite.RealFloatSamplerConverter
import net.imglib2.img.Img
import net.imglib2.img.ImgFactory
import net.imglib2.img.array.ArrayImgFactory
import net.imglib2.img.array.ArrayImgs
import net.imglib2.img.display.imagej.ImageJFunctions
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.type.Type
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.complex.ComplexFloatType
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.type.numeric.real.FloatType
import net.imglib2.util.RealSum
import net.imglib2.view.Views
import java.util.*
import java.util.concurrent.Executors

object ComplexTask_KT4_FourierTemplateMatching {
    /**
     * Makes a physical copy of the RAI
     *
     * @param input
     * - typically virtually converted input
     * @param outputFactory
     * - factory for the output
     * @return
     */
    fun <T : Type<T>?> materialize(input: Interval, outputFactory: ImgFactory<T>): RandomAccessibleInterval<T> {
        val output = outputFactory.create(input)
        return if (Views.isZeroMin(input)) output else Views.translate(output, *input.minAsLongArray())
    }

    /**
     * normalizes so the sum of all pixels is 1 (this is necessary for
     * (de)convolution
     *
     * @param img
     * - the PSF
     * @return a virtually normalized PSF
     */
    fun <T : RealType<T>?> normalize(
        img: RandomAccessibleInterval<T>?
    ): RandomAccessibleInterval<FloatType> {
        val sum = RealSum()
        for (type in Views.iterable(img)) sum.add(type!!.realDouble)
        val s = sum.sum
        return Converters.convert(
            img,
            { i: T, o: FloatType ->
                o.setReal(
                    i!!.realDouble / s
                )
            }, FloatType()
        )
    }

    fun <T : RealType<T>?> fft(
        img: RandomAccessibleInterval<T>
    ): Img<ComplexFloatType> {
        val numDimensions = img.numDimensions()
        val dim = LongArray(numDimensions)
        img.dimensions(dim)

        // compute the size of the complex-valued output and the required
        // padding based on the prior extended input image
        val paddedDimensions = LongArray(numDimensions)
        val fftDimensions = LongArray(numDimensions)
        FFTMethods.dimensionsRealToComplexFast(FinalDimensions.wrap(dim), paddedDimensions, fftDimensions)

        // compute the new interval for the input image
        val fftInterval = FFTMethods.paddingIntervalCentered(img, FinalDimensions.wrap(paddedDimensions))

        // use mirroing outofbounds
        val imgInput: RandomAccessibleInterval<T> = Views.interval(Views.extendMirrorSingle(img), fftInterval)

        // compute the FFT
        val service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val fftImg = FFT.realToComplex(imgInput, ArrayImgFactory(ComplexFloatType()), service)
        service.shutdown()
        return fftImg
    }

    /**
     * Simulates an image, convolves it, and finds the points again
     */
    fun simulate() {

        // create an empty image
        val img: Img<FloatType> = ArrayImgs.floats(400, 400, 200)

        // put random noise and dots
        val rnd = Random()
        for (t in img) t.set(rnd.nextFloat())
        val ra = img.randomAccess()
        for (i in 0..499) {
            for (d in 0 until img.numDimensions()) ra.setPosition(rnd.nextInt(img.dimension(d).toInt()), d)
            //ra.get().add(FloatType(100))
            ra.get().add(FloatType())
        }
        ImageJFunctions.show(img).title = "image"

        // create a gaussian kernel
        val kernel: RandomAccessibleInterval<FloatType> = ImagePlusImgs.from(
            IJ.openImage("https://preibischlab.mdc-berlin.de/download/psf-lightsheet.tif")
        )

        // blur the kernel a bit to have a more visible effect
        Gauss3.gauss(2.0, Views.extendZero(kernel), kernel)
        ImageJFunctions.show(kernel).title = "kernel"
        val convolved = img.factory().create(img)

        // FFT Convolution
        val conv = FFTConvolution(
            img,
            normalize(kernel),  // virtually normalize the kernel
            convolved,
            ArrayImgFactory(ComplexFloatType())
        )
        conv.convolve()
        ImageJFunctions.show(convolved).title = "convolved"

        // FFT Deconvolution / Correlation
        val deconvolved = img.factory().create(img)
        val deconv = FFTConvolution(
            convolved,
            normalize(kernel),  // virtually normalize the kernel
            deconvolved,
            ArrayImgFactory(ComplexFloatType())
        )
        deconv.div = true
        deconv.convolve()

        // blur the result a little
        Gauss3.gauss(0.75, Views.extendMirrorSingle(deconvolved), deconvolved)
        ImageJFunctions.show(deconvolved).title = "deconv/restored"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        ImageJ()
        val blobs: RandomAccessibleInterval<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif")
        )
        ImageJFunctions.show(blobs).title = "blobs"
        /**
         * illustrate how to compute an FFT
         */
        ImageJFunctions.show(fft(blobs)).title =
            "power spectrum of fft of blobs"
        /**
         * illustrate how to perform FFT-based convolution
         */
        // mean filtering
        val kernel: Img<UnsignedByteType> = ArrayImgs.unsignedBytes(13, 13)
        for (t in kernel) t.setOne()

        // has to be computed with FloatType as the kernel needs to be
        // normalized
        // (alternatively use LongType or IntType and divide by the sum of the
        // kernel afterwards)
        val conv = FFTConvolution(
            Converters.convert( // use a writeable converter
                blobs,
                RealFloatSamplerConverter()
            ),
            normalize(kernel),  // virtually normalize the kernel
            ArrayImgFactory(ComplexFloatType())
        )
        conv.convolve()
        ImageJFunctions.show(blobs).title = "blobs convolved with mean kernel 13x13"
        /**
         * illustrate how to perform FFT-based correlation/deconvolution
         */
        val imgIn: RandomAccessibleInterval<UnsignedByteType> = ImagePlusImgs.from(
            IJ.openImage("https://preibischlab.mdc-berlin.de/download/lightsheet0.tif.zip")
        )

        // problem: the range of unsignedbytetype is not sufficient for the
        // output of the
        // correlation/deconvolution (lies between -2000 and 2000),
        // therefore we materialize a float image of the same size and location
        val imgFloat = materialize(imgIn, ArrayImgFactory(FloatType()))
        val template: RandomAccessibleInterval<FloatType> = ImagePlusImgs.from(
            IJ.openImage("https://preibischlab.mdc-berlin.de/download/psf-lightsheet.tif")
        )
        val fc = FFTConvolution(
            Converters.convert(imgIn, RealFloatConverter(), FloatType()),
            normalize(template),
            imgFloat,
            ArrayImgFactory(ComplexFloatType())
        )
        fc.div = true
        fc.convolve()

        // blur the very unstable result of the FFT-based deconvolution
        Gauss3.gauss(0.75, Views.extendMirrorSingle(imgFloat), imgFloat)
        ImageJFunctions.show(imgIn).title = "input"
        ImageJFunctions.show(template).title = "template"
        ImageJFunctions.show(imgFloat).setDisplayRange(-2000.0, 2000.0)
        /**
         * Run a simulation that first convolves and then deconvolves the image
         */
        simulate()
    }
}
//Changed ra.get().add(FloatType(100)) to ra.get().add(FloatType())
//line 110-111
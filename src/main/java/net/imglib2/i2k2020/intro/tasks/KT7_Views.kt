package net.imglib2.i2k2020.intro.tasks

import bdv.util.BdvFunctions
import bdv.util.BdvOptions
import bdv.util.BdvStackSource
import ij.IJ
import ij.ImageJ
import net.imglib2.FinalInterval
import net.imglib2.Interval
import net.imglib2.RandomAccessibleInterval
import net.imglib2.img.Img
import net.imglib2.img.ImgFactory
import net.imglib2.img.array.ArrayImgs
import net.imglib2.img.cell.CellImgFactory
import net.imglib2.img.display.imagej.ImageJFunctions
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.type.numeric.ARGBType
import net.imglib2.type.numeric.NumericType
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.util.Intervals
import net.imglib2.util.Util
import net.imglib2.view.Views
import java.util.ArrayList

object KT7_Views {
    /**
     * Crop an image using Views and display it overlaid with the original using
     * BigDataViewer
     *
     * @param img
     */
    fun <T : NumericType<T>?> crop(img: RandomAccessibleInterval<T>) {

        // show the full image
        val bdv: BdvStackSource<*> = BdvFunctions.show(img, "img")

        // make an interval instance from the image that we modify
        var displayInterval: Interval? = FinalInterval(img)

        // define an interval that shrinks the image by 1/3 of its dimension on
        // each side
        for (d in 0 until img.numDimensions()) displayInterval =
            Intervals.expand(displayInterval, -img.dimension(d) / 3, d)
        println("full interval: " + Util.printInterval(img))

        // apply the interval to the img
        val cropped: RandomAccessibleInterval<T> = Views.interval(img, displayInterval)
        println("cropped interval: " + Util.printInterval(cropped))

        // show the cropped image
        BdvFunctions.show(cropped, "cropped", BdvOptions().addTo(bdv)).setColor(ARGBType(ARGBType.rgba(0, 255, 0, 0)))
    }

    /**
     * Permute the axes of a 3d image and show them with ImageJ
     *
     * @param img
     */
    fun <T : NumericType<T>?> permuteAxes(img: RandomAccessibleInterval<T>) {
        if (img.numDimensions() != 3) {
            println("this is an example for a 3d image.")
            return
        }
        ImageJFunctions.show(img).title = "xy"
        ImageJFunctions.show(Views.permute(img, 0, 1)).title = "yx"
        ImageJFunctions.show(Views.permute(img, 0, 2)).title = "xz"
        ImageJFunctions.show(Views.permute(img, 1, 2)).title = "yz"
    }

    /**
     * show central hyperslices of all dimensions with ImageJ
     *
     * @param img
     */
    fun <T : NumericType<T>?> hyperSlicing(img: RandomAccessibleInterval<T>) {

        // show the central slice in all dimensions
        for (d in 0 until img.numDimensions()) ImageJFunctions.show(
            Views.hyperSlice(img, d, (img.max(d) - img.min(d)) / 2 + img.min(d))
        ).title = "cut $d"
    }

    /**
     * Illustrate how to reduce an RAI to 2 dimensions by hyperslicing, and then
     * add a 3d dimension with size 50
     *
     * @param img
     */
    fun <T : NumericType<T>?> addRemoveDimensions(img: RandomAccessibleInterval<T>) {
        println("img: " + Util.printInterval(img))
        var sliced = img

        // hyperslice the image until it is a 2d image
        while (sliced.numDimensions() > 2) sliced = Views.hyperSlice(
            sliced,
            0,
            (img.max(0) - img.min(0)) / 2 + img.min(0)
        )
        println("sliced: " + Util.printInterval(sliced))

        // make it a 2d image with size==50 in y
        val expanded: RandomAccessibleInterval<T> = Views.addDimension(sliced, 0, 49)
        println("expanded: " + Util.printInterval(sliced))
        ImageJFunctions.show(expanded)
    }

    /**
     * Illustrate how to make an RAI iterable and compute its max, then show
     * that same iteration order can be enforced with Views.flatIterable
     *
     * @param img
     */
    fun <T : RealType<T>?> makeIterable(img: RandomAccessibleInterval<T>?) {

        // iterate a RandomAccessibleInterval and use the max method
        System.out.println("max intensity: " + KT3_GenericAccess.max(Views.iterable(img)))

        // iterate a CellImg and an ArrayImg the same way
        // array image
        val imgA: Img<UnsignedByteType> = ArrayImgs.unsignedBytes(5, 5)

        // cell image with blocksize 2
        val cellImgFactory: ImgFactory<UnsignedByteType> = CellImgFactory(UnsignedByteType(), 2)
        val imgB = cellImgFactory.create(5, 5)

        // iterate as in task4
        KT4_LargerImages.testIteration(imgA, imgB)
        println()

        // iterate CellImg as flatIterable
        KT4_LargerImages.testIteration(imgA, Views.flatIterable(imgB))
    }

    /**
     * Illustrate outofbounds strategies for NumericType images and display
     * using ImageJ
     *
     * Note: once the deprecated method for extendValue is removed, this code
     * will not be deprecated anymore.
     *
     * Can you figure out know why?
     *
     * @param img
     */
    fun <T : NumericType<T>?> outOfBounds(img: RandomAccessibleInterval<T>?) {
        ImageJFunctions.show(img).title = "input image"

        // an interval that adds 250 pixels all around
        val expansion: Interval = Intervals.expand(img, 250)

        // will crash, no image data
        // ImageJFunctions.show( Views.interval( img, expansion ) );

        // try different outofbounds
        ImageJFunctions.show(Views.interval(Views.extendZero(img), expansion)).title = "Zero outofbounds"
        ImageJFunctions.show(Views.interval(Views.extendMirrorSingle(img), expansion)).title = "Mirror outofbounds"
        ImageJFunctions.show(Views.interval(Views.extendPeriodic(img), expansion)).title = "Periodic outofbounds"

        // value outofbounds dorequires a value, we use the value of the first
        // pixel
        val value = Views.iterable(img).firstElement()!!.copy()
        ImageJFunctions.show(
            Views.interval(
                Views.extendValue(img, value),
                expansion
            )
        ).title = "Value outofbounds"

        // random out of bounds does not work as it requires a RealType (see
        // below)
    }

    /**
     * Illustrate outofbounds strategy that only works with RealType and display
     * using ImageJ
     *
     * @param img
     */
    fun <T : RealType<T>?> outOfBoundsRealType(img: RandomAccessibleInterval<T>?) {
        ImageJFunctions.show(img).title = "input image"

        // an interval that adds 250 pixels all around
        val expansion: Interval = Intervals.expand(img, 250)
        ImageJFunctions.show(Views.interval(Views.extendRandom(img, 0.0, 255.0), expansion)).title =
            "Random outofbounds"
    }

    /**
     * Illustrate what subsampling does: leaving out pixels
     *
     * @param img
     */
    fun <T : NumericType<T>?> subsampling(img: RandomAccessibleInterval<T>?) {
        for (step in 1..10) ImageJFunctions.show(Views.subsample(img, step.toLong())).title = "subsampling=$step"
    }

    /**
     * Shows how to mirror images using Views.translate, Views.interval and
     * MirrorOutOfBoundsStrategy
     *
     * @param img
     */
    fun <T : NumericType<T>?> mirroring(img: RandomAccessibleInterval<T>) {
        ImageJFunctions.show(img).title = "original"

        // mirror in each dimension using mirror outofbounds
        for (d in 0 until img.numDimensions()) {
            val translation = LongArray(img.numDimensions())
            translation[d] = img.dimension(d)
            val mirrorInterval: Interval = Intervals.translate(img, *translation)
            ImageJFunctions.show(
                Views.interval(
                    Views.extendMirrorSingle(img),
                    mirrorInterval
                )
            ).title = "mirror d=$d"
        }

        // mirror in all dimensions
        val translation = LongArray(img.numDimensions())
        img.dimensions(translation)
        val mirrorInterval: Interval = Intervals.translate(img, *translation)
        ImageJFunctions.show(
            Views.interval(
                Views.extendMirrorSingle(img),
                mirrorInterval
            )
        ).title = "mirror all dims"
    }

    /**
     * Illustrate how to use the Views.stack method for stacking n-1 dimensional
     * RAIs into an n-dimensional RAI
     *
     * @param img
     * @param extraImg
     * @param step
     */
    fun <T : NumericType<T>?> stacking(
        img: RandomAccessibleInterval<T>,
        extraImg: RandomAccessibleInterval<T>,
        step: Int
    ) {
        var extraImg = extraImg
        ImageJFunctions.show(img).title = "original"

        // restack every step-th (n-1 dim) image of the n-dim image (e.g. every
        // 10th slice of a 3d image)
        val hyperSlices = ArrayList<RandomAccessibleInterval<T>>()
        var pos = 0
        while (pos < img.dimension(img.numDimensions() - 1)) {
            hyperSlices.add(Views.hyperSlice(img, img.numDimensions() - 1, pos.toLong()))
            pos += step
        }

        // add an extra image at the end, assuming it has n-1 dimensions
        // compared to img
        while (extraImg.numDimensions() >= img.numDimensions()) extraImg = Views.hyperSlice(
            extraImg,
            0,
            (img.max(0) - img.min(0)) / 2 + img.min(0)
        )
        hyperSlices.add(Views.interval(Views.extendMirrorSingle(extraImg), hyperSlices[0]))
        ImageJFunctions.show(Views.stack(hyperSlices)).title = "restacked and extra image"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        ImageJ()
        val img: Img<UnsignedByteType> =
            ImagePlusImgs.from(IJ.openImage("https://preibischlab.mdc-berlin.de/download/lightsheet0.tif.zip"))
        val clown: Img<ARGBType> = ImagePlusImgs.from(IJ.openImage("http://imagej.nih.gov/ij/images/clown.jpg"))
        val blobs: Img<UnsignedByteType> = ImagePlusImgs.from(IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif"))

        // test cropping
        crop(img)

        // permute axes
        permuteAxes(img)

        // hyperslices
        hyperSlicing(img)

        // removing and adding dimensions
        addRemoveDimensions(img)

        // iterate a RandomAccessbileInterval and introduce flatiterable
        makeIterable(blobs)

        // illustrate outofbounds
        outOfBounds(clown)
        outOfBoundsRealType(blobs)

        // subsample image with integer steps
        subsampling(clown)

        // flip/mirror an image
        mirroring(clown)

        // stacking
        stacking(img, blobs, 10)
    }
}
package net.imglib2.i2k2020.intro.tasks

import ij.IJ
import net.imglib2.img.Img
import net.imglib2.img.display.imagej.ImageJFunctions
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.type.NativeType
import net.imglib2.type.numeric.NumericType
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType
import java.io.IOException

object KT5_OpenAndSave {
    /**
     * Open an existing file as NumericType
     */
    fun <T> testNumericTypeOpening() where T : NumericType<T>?, T : NativeType<T>? {

        // RGB image
        val imgFN = "http://imagej.nih.gov/ij/images/clown.jpg"

        // open as NumericType
        val img: Img<T> = ImagePlusImgs.from(IJ.openImage(imgFN))
        //println("Type=" + img.firstElement().javaClass.getName())
    }

    /**
     * Open an existing file as RealType
     */
    fun <T> testRealTypeOpening() where T : RealType<T>?, T : NativeType<T>? {

        // 8-bit unsigned image
        val imgFN = "http://imagej.nih.gov/ij/images/blobs.gif"

        // open as RealType
        val img: Img<T> = ImagePlusImgs.from(IJ.openImage(imgFN))
        //println("Type=" + img.firstElement().javaClass.getName())
    }

    /**
     * Open an existing file as UnsignedByteType
     */
    fun testUnsignedByteTypeOpening() {

        // 8-bit unsigned image
        val imgFN = "http://imagej.nih.gov/ij/images/blobs.gif"

        // open as RealType
        val img: Img<UnsignedByteType> = ImagePlusImgs.from(IJ.openImage(imgFN))
        println("Type=" + img.firstElement().javaClass.name)
    }

    /**
     * Fail to open an existing RGB image as RealType
     */
    fun <T> failToOpen() where T : RealType<T>?, T : NativeType<T>? {

        // RGB image
        val imgFN = "http://imagej.nih.gov/ij/images/clown.jpg"

        // open as RealType
        val img: Img<T> = ImagePlusImgs.from(IJ.openImage(imgFN))
        //println("Type=" + img.firstElement().javaClass.getName())
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T> main(args: Array<String>) where T : RealType<T>?, T : NativeType<T>? {

        // open generic images using ImageJ
        //testNumericTypeOpening()
        //testRealTypeOpening()

        // open specifically types images using ImageJ
        testUnsignedByteTypeOpening()

        // example of a crash when demanding a type for opening that the image
        // does not have
        // failToOpen();

        // save an image using ImageJ
        IJ.save(ImageJFunctions.wrap(KT1_CreateImg.createImgFromArray(), "test"), "test.tif")
        IJ.save(ImageJFunctions.wrap(KT1_CreateImg.createImgFromArray(), "test"), "test.jpg")
    }
}
//Note: Lines 24, 37, 63 disabled-- img.firstElement().javaClass
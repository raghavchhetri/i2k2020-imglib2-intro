package net.imglib2.i2k2020.intro.tasks

import bdv.util.BdvFunctions
import bdv.util.BdvOptions
import bdv.util.BdvStackSource
import ij.IJ
import ij.ImageJ
import net.imglib2.img.Img
import net.imglib2.img.display.imagej.ImageJFunctions
import net.imglib2.img.imageplus.ImagePlusImgs
import net.imglib2.type.NativeType
import net.imglib2.type.numeric.ARGBType
import net.imglib2.type.numeric.NumericType
import net.imglib2.type.numeric.RealType
import net.imglib2.type.numeric.integer.UnsignedByteType
import java.io.IOException

object KT6_Viewing {
    /**
     * View different images in ImageJ
     */
    fun <T, S> viewingImageJ() where T : RealType<T>?, T : NativeType<T>?, S : NumericType<S>?, S : NativeType<S>? {

        // show the ImageJ window
        ImageJ()

        // display the image from Task1
        ImageJFunctions.show(KT1_CreateImg.createImgFromArray())

        // display blobs example (8 bit)
        val img1: Img<T> = ImagePlusImgs.from(IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif"))
        ImageJFunctions.show(img1)

        // display clown example (RGB)
        val img2: Img<S> = ImagePlusImgs.from(IJ.openImage("http://imagej.nih.gov/ij/images/clown.jpg"))
        ImageJFunctions.show(img2)

        // display 3D image stack and adjust the 3rd dimension properties
        val img3: Img<UnsignedByteType> =
            ImagePlusImgs.from(IJ.openImage("https://preibischlab.mdc-berlin.de/download/lightsheet0.tif.zip"))
        ImageJFunctions.show(img3)
    }

    /**
     * View different images in BigDataViewer
     */
    fun <T> viewingBDV() where T : RealType<T>?, T : NativeType<T>? {

        // load and display first image
        val img1: Img<T> =
            ImagePlusImgs.from(IJ.openImage("https://preibischlab.mdc-berlin.de/download/lightsheet0.tif.zip"))
        var bdv1: BdvStackSource<*>? = BdvFunctions.show(img1, "stack 1")

        // load and display second image, add to same BDV instance
        val img2: Img<T> =
            ImagePlusImgs.from(IJ.openImage("https://preibischlab.mdc-berlin.de/download/lightsheet1.tif.zip"))
        bdv1 = BdvFunctions.show(img2, "stack 2", BdvOptions().addTo(bdv1))

        // display blobs example (8 bit) in new BDV instance
        val img3: Img<UnsignedByteType> = ImagePlusImgs.from(IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif"))
        var bdv2: BdvStackSource<*>? = BdvFunctions.show(img3, "2d UnsignedByteType image", BdvOptions().is2D)

        // display clown example (RGB)
        val img4: Img<ARGBType> = ImagePlusImgs.from(IJ.openImage("http://imagej.nih.gov/ij/images/clown.jpg"))
        bdv2 = BdvFunctions.show(img4, "2d ARGBType image", BdvOptions().is2D.addTo(bdv2))
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {

        // various images displayed with ImageJ
        //viewingImageJ()

        // various images displayed with BigDataViewer
        //viewingBDV()
    }
}
//Note: lines 73, 76 disabled
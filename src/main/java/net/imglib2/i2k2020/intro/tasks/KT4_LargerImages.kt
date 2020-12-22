package net.imglib2.i2k2020.intro.tasks

import net.imglib2.IterableInterval
import net.imglib2.img.Img
import net.imglib2.img.ImgFactory
import net.imglib2.img.array.ArrayImgs
import net.imglib2.img.cell.CellImgFactory
import net.imglib2.img.planar.PlanarImgs
import net.imglib2.type.numeric.integer.UnsignedByteType
import net.imglib2.util.Util

object KT4_LargerImages {
    /**
     * compare how a CellImg and ArrayImg are iterated, make two 5x5 images and
     * test
     */
    fun testIteration() {

        // array image
        val imgA: Img<UnsignedByteType> = ArrayImgs.unsignedBytes(5, 5)

        // cell image with blocksize 2
        val cellImgFactory: ImgFactory<UnsignedByteType> = CellImgFactory(UnsignedByteType(), 2)
        val imgB = cellImgFactory.create(5, 5)
        testIteration(imgA, imgB)
    }

    /**
     * Print out the iteration order for two types of images
     *
     * @param imgA
     * @param imgB
     */
    fun testIteration(imgA: IterableInterval<*>, imgB: IterableInterval<*>) {
        val cursorA = imgA.localizingCursor()
        val cursorB = imgB.localizingCursor()
        while (cursorA.hasNext() && cursorB.hasNext()) {
            cursorA.fwd()
            cursorB.fwd()
            println(
                Util.printCoordinates(cursorA) + " <> " + Util.printCoordinates(cursorB) + " equal? " + Util.locationsEqual(
                    cursorA,
                    cursorB
                )
            )
        }
    }

    /**
     * try to instantiate images of different sizes with ArrayImg, PlanarImg and
     * CellImg
     *
     * @param dim
     * - the size of image
     */
    fun instantiateImgs(dim: LongArray) {
        println(
            """
                
                --- Instantiating different Img types for dim: ${Util.printCoordinates(dim)}:
                """.trimIndent()
        )

        // ArrayImg
        try {
            val arrayImg: Img<UnsignedByteType> = ArrayImgs.unsignedBytes(*dim)
            println("  ArrayImg instantiated successfully: $arrayImg")
        } catch (e: RuntimeException) {
            println("  ArrayImg failed to be instantiated: $e")
        }

        // PlanarImg
        try {
            val planarImg: Img<UnsignedByteType> = PlanarImgs.unsignedBytes(*dim)
            println("  PlanarImg instantiated successfully: $planarImg")
        } catch (e: RuntimeException) {
            println("  PlanarImg failed to be instantiated: $e")
        }

        // CellImg has no convenience methods. Using the factory, which also
        // allows generic instantiations
        try {
            val cellImgFactory: ImgFactory<UnsignedByteType> = CellImgFactory(UnsignedByteType(), 100)
            val cellImg = cellImgFactory.create(*dim)
            println("  CellImg instantiated successfully: $cellImg")
        } catch (e: RuntimeException) {
            println("  CellImg failed to be instantiated: $e")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {

        // visualize different iterations on arrayimgs and cellimgs
        testIteration()

        // will work with all img types
        instantiateImgs(longArrayOf(2048, 2048, 50))

        // will fail on ArrayImg because the total number of pixels is bigger
        // than 2^31 (2147483647) which is around 2GB for 8bit
        instantiateImgs(longArrayOf(2048, 2048, 550))

        // will fail on ArrayImg and PlanarImg because the total number of
        // pixels per plane is bigger than 2^31 (2147483647)
        // note: ImageJ cannot handle this image
        instantiateImgs(longArrayOf(47000, 47000))
    }
}
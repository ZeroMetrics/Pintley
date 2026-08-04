package com.example.pintly

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache

/**
 * The photos behind the "Name That Bird" category.
 *
 * The photos live in **app/src/main/assets/birds** as plain JPEGs, named for the
 * bird with underscores instead of spaces (`Blue-footed_Booby.jpg`). Adding a bird
 * is a two-step, no-code job: drop the JPEG in, add a line to `birds/credits.txt`.
 *
 * There is no image-loading library here on purpose — [android.content.res.AssetManager]
 * and [BitmapFactory] handle a dozen local files perfectly well, and Glide or Coil
 * would be a large dependency for a small job.
 */
object BirdImages {

    private const val DIR = "birds"
    private const val CREDITS = "$DIR/credits.txt"

    /** A photographer + licence, shown under the prompt once the bird is revealed. */
    data class Credit(val photographer: String, val licence: String) {
        override fun toString(): String = "Photo: $photographer · $licence"
    }

    private var names: List<String>? = null
    private var credits: Map<String, Credit>? = null

    /**
     * Bitmaps are big and decoding is slow, so keep the recent ones. An eighth of the
     * heap is the usual budget; at 1400px a photo is roughly 5 MB decoded.
     */
    private val cache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt().coerceAtLeast(4 * 1024)
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    /** Every bird photo in assets/birds. `credits.txt` is filtered out by extension. */
    fun list(context: Context): List<String> = names ?: try {
        (context.assets.list(DIR) ?: emptyArray())
            .filter { it.endsWith(".jpg", ignoreCase = true) }
            .sorted()
            .also { names = it }
    } catch (e: Exception) {
        emptyList<String>().also { names = it }
    }

    /** `"Blue-footed_Booby.jpg"` -> `"Blue-footed Booby"`. */
    fun displayName(file: String): String =
        file.substringBeforeLast('.').replace('_', ' ')

    /** The credit for [file], or null if `credits.txt` has no line for it. */
    fun credit(context: Context, file: String): Credit? = loadCredits(context)[file]

    /**
     * Decode [file], downsampled to roughly [reqWidth] x [reqHeight] and cached.
     *
     * ARGB_8888 is kept deliberately: RGB_565 would halve the memory but these photos
     * have smooth sky gradients that would visibly band.
     */
    fun bitmap(context: Context, file: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        cache.get(file)?.let { return it }
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.assets.open("$DIR/$file").use { BitmapFactory.decodeStream(it, null, bounds) }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, reqWidth, reqHeight)
            }
            context.assets.open("$DIR/$file").use { BitmapFactory.decodeStream(it, null, options) }
                ?.also { cache.put(file, it) }
        } catch (e: Exception) {
            null
        }
    }

    /** Largest power-of-two shrink that still leaves the photo at least the size asked for. */
    private fun sampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        if (reqWidth <= 0 || reqHeight <= 0 || width <= 0 || height <= 0) return 1
        var sample = 1
        while (width / (sample * 2) >= reqWidth && height / (sample * 2) >= reqHeight) {
            sample *= 2
        }
        return sample
    }

    /** Parse `credits.txt` once: `filename | photographer | licence | source URL`. */
    private fun loadCredits(context: Context): Map<String, Credit> = credits ?: try {
        context.assets.open(CREDITS).bufferedReader().useLines { lines ->
            lines.map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .mapNotNull { line ->
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size < 3) null else parts[0] to Credit(parts[1], parts[2])
                }
                .toMap()
        }.also { credits = it }
    } catch (e: Exception) {
        emptyMap<String, Credit>().also { credits = it }
    }
}

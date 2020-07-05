package com.xicheng.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever

/**
 * Created by Square
 * Date :2020/7/4
 * Description :
 * Version :
 */
class CoverHelper() {
    private val mediaMetadataRetriever = MediaMetadataRetriever()
    public fun getCover(uri: String): Bitmap? {
        mediaMetadataRetriever.setDataSource(uri)
        val picture = mediaMetadataRetriever.embeddedPicture
        return if (picture != null) {
            BitmapFactory.decodeByteArray(picture, 0, picture.size)
        } else null
    }
}
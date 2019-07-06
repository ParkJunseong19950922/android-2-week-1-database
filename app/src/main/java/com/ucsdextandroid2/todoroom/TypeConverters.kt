package com.ucsdextandroid2.todoroom

import android.net.Uri
import androidx.room.TypeConverter
import java.lang.Exception

/**
 * Created by rjaylward on 2019-07-06
 */

class UriTypeConverters {

    companion object {
        @TypeConverter
        @JvmStatic
        fun fromUri(value: Uri?): String {
            return value.toString()
        }

        @TypeConverter
        @JvmStatic
        fun toUri(value: String?): Uri? {
            return try{Uri.parse(value)} catch (e:Exception) {null}
        }
    }
}
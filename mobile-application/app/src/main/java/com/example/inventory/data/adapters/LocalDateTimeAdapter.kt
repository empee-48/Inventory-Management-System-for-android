package com.example.inventory.data.adapters

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @RequiresApi(Build.VERSION_CODES.O)
    override fun write(out: JsonWriter?, value: LocalDateTime?) {
        if (value == null) {
            out?.nullValue()
        } else {
            out?.value(formatter.format(value))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun read(reader: JsonReader?): LocalDateTime? {
        return if (reader?.peek() == null) {
            null
        } else {
            val dateString = reader.nextString()
            if (dateString != null) {
                LocalDateTime.parse(dateString, formatter)
            } else {
                null
            }
        }
    }
}

package com.example.inventory.data.adapters

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter : TypeAdapter<LocalDate>() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @RequiresApi(Build.VERSION_CODES.O)
    override fun write(out: JsonWriter?, value: LocalDate?) {
        if (value == null) {
            out?.nullValue()
        } else {
            out?.value(formatter.format(value))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun read(reader: JsonReader?): LocalDate? {
        return if (reader?.peek() == null) {
            null
        } else {
            val dateString = reader.nextString()
            if (dateString != null) {
                LocalDate.parse(dateString, formatter)
            } else {
                null
            }
        }
    }
}
package com.wiffle.caffinate.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "drinks")
@TypeConverters(Converters::class)
data class Drink(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String = "",
    val category: String = "Energy Drink",
    val caffeineContent: Int,
    val sugarContent: Int = 0,
    val size: String = "16 fl oz (473ml)",
    val sizeInMl: Int = 473,
    val location: String = "",
    val rating: Float = 0f,
    val imageUrl: String = "",
    val consumedDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val calories: Int = 0,
    val timesConsumed: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

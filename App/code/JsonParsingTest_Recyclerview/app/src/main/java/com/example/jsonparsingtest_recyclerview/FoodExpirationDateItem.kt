package com.example.jsonparsingtest_recyclerview


import com.google.gson.annotations.SerializedName

data class FoodExpirationDateItem(
    @SerializedName("Category")
    val category: String,
    @SerializedName("ExpirationDate")
    val expirationDate: Int,
    @SerializedName("Food")
    val food: String
)
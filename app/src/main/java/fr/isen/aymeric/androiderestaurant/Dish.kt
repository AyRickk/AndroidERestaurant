package fr.isen.aymeric.androiderestaurant

data class Dish(
    val id: String,
    val name_fr: String,
    val images: List<String>,
    val ingredients: List<Ingredient>,
    val prices: List<Price>
)

data class Ingredient(
    val id: String,
    val name_fr: String
)

data class Price(
    val id: String,
    val price: String,
    val size: String
)

data class MenuResponse(val data: List<Category>)
data class Category(val name_fr: String, val items: List<Dish>)
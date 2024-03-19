package fr.isen.aymeric.androiderestaurant

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException

data class CartItem(val dish: Dish, var quantity: Int) {}

fun getCartItems(context: Context): MutableList<CartItem> {
    val gson = Gson()
    val json = try {
        context.openFileInput("cart.json")?.bufferedReader().use { it?.readText() }
    } catch (e: FileNotFoundException) {
        null
    }
    if (json.isNullOrEmpty()) {
        return mutableListOf()
    }
    val type = object : TypeToken<MutableList<CartItem>>() {}.type
    return gson.fromJson(json, type)
}

fun getCartItemsCount(context: Context): MutableList<CartItem> {
    val gson = Gson()
    val json = try {
        context.openFileInput("cart.json")?.bufferedReader().use { it?.readText() }
    } catch (e: FileNotFoundException) {
        null
    }
    if (json.isNullOrEmpty()) {
        return mutableListOf()
    }
    val type = object : TypeToken<MutableList<CartItem>>() {}.type
    return gson.fromJson(json, type)
}

fun getTotalCartItems(context: Context): Int {
    val existingCartItems = getCartItemsCount(context)
    return existingCartItems.sumBy { it.quantity }
}

fun isCartEmpty(context: Context): Boolean {
    return getCartItemsCount(context).isEmpty()
}

fun ClearCart(context: Context) {
    context.openFileOutput("cart.json", Context.MODE_PRIVATE).use {
        it.write("".toByteArray())
    }
}
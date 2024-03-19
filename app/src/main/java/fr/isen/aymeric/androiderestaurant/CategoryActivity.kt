package fr.isen.aymeric.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import fr.isen.aymeric.androiderestaurant.ui.theme.AndroidERestaurantTheme
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.aymeric.androiderestaurant.CategoryActivity.Companion.imageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import okhttp3.Request.Builder

class CategoryActivity : ComponentActivity() {
    private lateinit var requestQueue: RequestQueue
    private var category: String? = null
    var dishes by mutableStateOf(emptyList<Dish>())
    var refreshKey by mutableStateOf(0)

    companion object {
        lateinit var imageLoader: ImageLoader
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLoader = ImageLoader.Builder(this)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
        category = intent.getStringExtra("category")
        requestQueue = Volley.newRequestQueue(this)
        setContent {
            AndroidERestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFB17E4B),
                ) {
                    ScaffoldCategory(this@CategoryActivity, category ?: "Default Text")
                }
            }
        }

        val cachedDishes = getDishesFromCache()
        if (cachedDishes != null) {
            dishes = cachedDishes.toMutableList()
        } else {
            fetchMenu()
        }
        Log.i("DISHES", "Dishes: $dishes")
    }

    private fun saveDishesToCache(dishes: List<Dish>) {
        val sharedPreferences = getSharedPreferences("DishesCache", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(dishes)
        editor.putString("dishes_$category", json)
        editor.apply()
    }

    private fun getDishesFromCache(): List<Dish>? {
        val sharedPreferences = getSharedPreferences("DishesCache", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("dishes_$category", null) ?: return null
        val type = object : TypeToken<List<Dish>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun fetchMenu() {
        // Invalidate cache
        val sharedPreferences = getSharedPreferences("DishesCache", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("dishes_$category")
        editor.apply()

        // Update refreshKey to trigger recomposition
        refreshKey++

        // Fetch dishes from server
        val url = "http://test.api.catering.bluecodegames.com/menu"
        val requestBody = JSONObject().apply {
            put("id_shop", "1")
        }.toString()

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                handleResponse(response)
            },
            Response.ErrorListener { error ->
                Log.e("DISHES", "Error: ${error.toString()}")
            }
        ) {
            override fun getBody(): ByteArray = requestBody.toByteArray()
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }
        requestQueue.add(stringRequest)
    }

    fun handleResponse(response: String) {
        // Conversion de la réponse JSON en objets
        val menuResponse = Gson().fromJson(response, MenuResponse::class.java)
        val category = menuResponse.data.find { it.name_fr == category }

        category?.items?.let { dishes ->
            CoroutineScope(Dispatchers.Main).launch {
                val validatedDishes = dishes.map { dish ->
                    val validImages = dish.images.filter { imageUrl ->
                        async { isImageUrlValid(imageUrl) }.await()
                    }
                    dish.copy(images = validImages)
                }.toList()

                this@CategoryActivity.dishes = validatedDishes
                Log.i("DISHES2", "Dishes: $validatedDishes")
                saveDishesToCache(validatedDishes)
            }
        }
    }

    private suspend fun isImageUrlValid(imageUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Builder().url(imageUrl).head().build()
                val response = OkHttpClient().newCall(request).execute()
                response.isSuccessful && response.body != null
            } catch (e: Exception) {
                false
            }
        }
    }
}

@Composable
fun CategoryActivity.CategoryScreen(category: String) {
    DishList(dishes, category, refreshKey, onRefresh = {
        fetchMenu()
    })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldCategory(activity: CategoryActivity, topBarText: String) {


    var presses by remember { mutableIntStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val cartItemsCount = remember {
        mutableIntStateOf(getTotalCartItems(activity))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(0xFF011222),
                    titleContentColor = Color(0xFFfef8d8),
                ),
                title = {
                    Text(
                        topBarText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.muscle_rats_no_text),
                            contentDescription = "Localized description",
                            modifier = Modifier
                                .height(80.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF011222),
                contentColor = Color(0xFFfef8d8),
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Muscle Rat's Restaurant",
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Open the BasketActivity
                    val intent = Intent(activity, BasketActivity::class.java)
                    activity.startActivity(intent)

                },
                containerColor = Color(0xFF011222),
                contentColor = Color(0xFFfef8d8),
            ) {
                BadgedBox(badge = { Badge { Text(cartItemsCount.intValue.toString()) } }) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Basket"
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            activity.CategoryScreen(topBarText)
        }

    }
}

//Afficher la liste des noms de plats
@Composable
fun DishList(dishes: List<Dish>, category: String, refreshKey: Int, onRefresh: () -> Unit) {
    val refreshing by remember { mutableStateOf(false) }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = refreshing),
        onRefresh = {
            onRefresh()
        }
    ) {
        LazyColumn {
            items(dishes, key = { dish -> dish.name_fr + refreshKey }) { dish ->
                ElevatedCardCategory(dish = dish, category)
            }
        }
    }
}

@Composable
fun ElevatedCardCategory(dish: Dish, category: String) {
    val context = LocalContext.current
    val activity = LocalContext.current as CategoryActivity

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = dish.name_fr,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            LoadImageWithFallback(dish.images)
            Button(
                onClick = {
                    val intent = Intent(context, DetailsActivity::class.java)
                    intent.putExtra("dishName", dish.name_fr)
                    intent.putExtra("category", category)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            ) {
                Text(text = "Details", color = Color.White)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("Prix: ${dish.prices.first().price} €")
            }
        }
    }
}


@Composable
fun LoadImageWithFallback(images: List<String>) {
    var index by remember { mutableIntStateOf(0) }
    var attempts by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val painter = rememberImagePainter(
        data = if (images.isNotEmpty() && index < images.size && images[index].isNotBlank()) images[index] else "",
        imageLoader = imageLoader,
        onExecute = { _, _ -> true },
        builder = {
            crossfade(true)
            error(R.drawable.fallback_image)
            fallback(R.drawable.fallback_image)
            listener(
                onError = { _, _ ->
                    if (attempts < 3 && index + 1 < images.size) {
                        Log.i("DISHES", "Error loading image: ${images[index]}")
                        index++
                        attempts++
                    }
                }
            )
        }
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    )
}




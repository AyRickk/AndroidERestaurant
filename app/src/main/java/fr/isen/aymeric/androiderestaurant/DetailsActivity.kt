package fr.isen.aymeric.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.aymeric.androiderestaurant.ui.theme.AndroidERestaurantTheme
import fr.isen.aymeric.androiderestaurant.CategoryActivity.Companion.imageLoader
import java.lang.reflect.Type


class DetailsActivity : ComponentActivity() {
    private var dishName: String? = null
    private var category: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dishName = intent.getStringExtra("dishName")
        category = intent.getStringExtra("category")
        val dishes = getDishesFromCache()
        val dish = dishes?.find { it.name_fr == dishName }

        setContent {
            AndroidERestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFB17E4B),
                ) {
                    dish?.let {
                        ScaffoldDetails(this@DetailsActivity, it, category ?: "")
                    }
                }
            }
        }
    }

    private fun getDishesFromCache(): List<Dish>? {
        val sharedPreferences = getSharedPreferences("DishesCache", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("dishes_$category", null) ?: return null
        val type = object : TypeToken<List<Dish>>() {}.type
        return Gson().fromJson(json, type)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScaffoldDetails(activity: DetailsActivity, dish: Dish, category: String) {
    var quantity by remember { mutableStateOf(1) } // Step 1

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        dish.images.size
    }

    //Vérifier si le panier est vide

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
                        category,
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
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = dish.name_fr,
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )
            // Image Carousel using HorizontalPager

            if (dish.images.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) { page ->
                    val imageUrl = dish.images[page]
                    Image(
                        painter = rememberImagePainter(
                            data = imageUrl,
                            imageLoader = imageLoader(activity)
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.fallback_image),
                    contentDescription = "Localized description",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Text(
                text = "Ingrédients : ${dish.ingredients.joinToString { it.name_fr }}",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
            // Quantity Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    enabled = quantity > 1
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = quantity.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                IconButton(
                    onClick = { quantity++ }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }

            var snackbarMessage by remember { mutableStateOf("") }

            // Add to Cart Button
            Button(
                onClick = {
                    addToCart(activity, dish, quantity) // Step 2
                    snackbarMessage = "Item added to cart"
                    cartItemsCount.intValue = getTotalCartItems(activity) // Update cartItemsCount here
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val price = dish.prices.first().price.toFloat() * quantity
                Text(text = "Add to basket : $price €", color = Color.White)
            }

            // Observe the snackbar message state here
            if (snackbarMessage.isNotEmpty()) {
                showSnackbar(activity, snackbarMessage)
            }
        }
    }
}

fun addToCart(context: Context, dish: Dish, quantity: Int) {
    val cartItem = CartItem(dish, quantity)
    val gson = Gson()

    val existingCartItems = getCartItemsCount(context)
    val existingCartItem = existingCartItems.find { it.dish == dish }

    if (existingCartItem != null) {
        existingCartItem.quantity += quantity
    } else {
        existingCartItems.add(cartItem)
    }

    val json = gson.toJson(existingCartItems)
    context.openFileOutput("cart.json", Context.MODE_PRIVATE).use {
        it.write(json.toByteArray())
    }
}

@Composable
fun showSnackbar(activity: ComponentActivity, message: String) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = message) {
        snackbarHostState.showSnackbar(message)
    }
    SnackbarHost(hostState = snackbarHostState)
}

@Composable
fun imageLoader(context: Context): ImageLoader {
    return ImageLoader.Builder(context)
        .crossfade(true)
        .build()
}
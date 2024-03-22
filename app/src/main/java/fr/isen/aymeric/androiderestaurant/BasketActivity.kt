package fr.isen.aymeric.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import fr.isen.aymeric.androiderestaurant.ui.theme.AndroidERestaurantTheme

class BasketActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidERestaurantTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScaffoldBasket(this@BasketActivity)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldBasket(activity: BasketActivity) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val cartItemsState = remember { mutableStateListOf(*getCartItems(activity).toTypedArray()) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF011222),
                    titleContentColor = Color(0xFFfef8d8),
                ),
                title = {
                    Text(
                        "Your Basket",
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

        ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (isCartEmpty(activity)) {
                item {
                    Text(
                        text = "Your basket is empty",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                var cartItems = getCartItems(activity)

                items(cartItemsState) { cartItem ->
                    val totalItemPrice = remember {
                        mutableFloatStateOf(cartItem.quantity * cartItem.dish.prices[0].price.toFloat())
                    }

                    val itemQuantity = remember {
                        mutableIntStateOf(cartItem.quantity)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        ElevatedCard(
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = cartItem.dish.name_fr,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Image(
                                painter = rememberImagePainter(cartItem.dish.images[0]),
                                contentDescription = "Localized description",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = {
                                        if (cartItem.quantity > 1) {
                                            cartItem.quantity -= 1
                                            activity.openFileOutput(
                                                "cart.json",
                                                Context.MODE_PRIVATE
                                            ).use {
                                                it.write(Gson().toJson(cartItems).toByteArray())
                                            }
                                            itemQuantity.intValue = cartItem.quantity
                                            totalItemPrice.floatValue =
                                                cartItem.quantity * cartItem.dish.prices[0].price.toFloat()
                                        }
                                    },
                                    enabled = cartItem.quantity > 1
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                                }
                                Text(text = "${itemQuantity.intValue}")
                                IconButton(
                                    onClick = {
                                        cartItem.quantity += 1
                                        activity.openFileOutput("cart.json", Context.MODE_PRIVATE)
                                            .use {
                                                it.write(Gson().toJson(cartItems).toByteArray())
                                            }
                                        itemQuantity.intValue = cartItem.quantity
                                        totalItemPrice.floatValue =
                                            cartItem.quantity * cartItem.dish.prices[0].price.toFloat()
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Prix: ${totalItemPrice.floatValue} € (unité: ${cartItem.dish.prices[0].price} €)")
                                IconButton(
                                    onClick = {
                                        cartItemsState.remove(cartItem)
                                        activity.openFileOutput("cart.json", Context.MODE_PRIVATE)
                                            .use {
                                                it.write(
                                                    Gson().toJson(cartItemsState.toList())
                                                        .toByteArray()
                                                )
                                            }
                                    }
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    Button(
                        //clear cart
                        onClick = {
                            ClearCart(activity)
                            cartItemsState.clear()
                        },
                    ) {
                        Text(
                            text = "Clear cart",
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(onClick = {
                        // Open the OrderActivity
                        val intent = Intent(activity, OrderActivity::class.java)
                        activity.startActivity(intent)
                    }) {
                        Text(
                            text = "Order : ${cartItemsState.sumBy { it.quantity }} items for ${cartItemsState.sumBy { it.quantity * it.dish.prices[0].price.toInt() }} €",
                            textAlign = TextAlign.Center
                        )

                    }
                }

            }
        }
    }
}
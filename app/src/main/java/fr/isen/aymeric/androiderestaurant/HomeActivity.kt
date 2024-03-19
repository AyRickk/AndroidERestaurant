package fr.isen.aymeric.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.isen.aymeric.androiderestaurant.ui.theme.AndroidERestaurantTheme
import android.app.Application
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.request.CachePolicy

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageLoader = ImageLoader.Builder(this)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
        setContent {
            AndroidERestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFB17E4B),
                ) {
                    ScaffoldHome(this@HomeActivity)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldHome(activity: HomeActivity) {

    val cartItemsCount = remember {
        mutableIntStateOf(getTotalCartItems(activity))
    }

    Scaffold(
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
                .background(Color(0xFFB17E4B))
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top, // Aligne les éléments au centre
            horizontalAlignment = Alignment.CenterHorizontally, // Aligne horizontalement au centre
        ) {
            Image(
                painter = painterResource(id = R.drawable.muscle_rats_no_bg),
                contentDescription = "Description de l'image"
            )
            ElevatedButton(onClick = {}, text = "Entrées")
            ElevatedButton(onClick = {}, text = "Plats")
            ElevatedButton(onClick = {}, text = "Desserts")
        }
    }
}


fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

@Composable
fun ElevatedButton(
    onClick: (String) -> Unit,
    text: String
) {
    val context = LocalContext.current
    ElevatedButton(
        onClick = {
            onClick(text)
            showToast(context, text)
            val intent = Intent(context, CategoryActivity::class.java)
            intent.putExtra("category", text)
            context.startActivity(intent)
        },
        modifier = Modifier
            .width(150.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF011222),
            contentColor = Color(0xFFfef8d8),
        )
    ) {
        Text(text)
    }
}

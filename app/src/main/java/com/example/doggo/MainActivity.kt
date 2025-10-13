package com.example.doggo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.doggo.ui.theme.DoggoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoggoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DoggoScreen()
                }
            }
        }
    }
}

@Composable
fun DoggoScreen() {
    // list gambar yang kamu punya di drawable
    val dogs = listOf(
        R.drawable.dog1,
        R.drawable.dog2,
        R.drawable.dog3
    )

    var currentDog by remember { mutableStateOf(dogs.random()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = currentDog),
            contentDescription = "Doggo",
            modifier = Modifier
                .size(300.dp)
                .padding(bottom = 24.dp)
        )

        Button(onClick = { currentDog = dogs.random() }) {
            Text("Show Random Dog")
        }
    }
}

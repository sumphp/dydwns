package com.example.app_04_key_pad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_04_key_pad.R
import com.example.app_04_key_pad.ui.theme.SumphpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SumphpTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(top = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "Add Contact",
                modifier = Modifier.size(15.dp),
                tint = Color.Green
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "연락처 추가",
                color = Color.Green,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "010-7161-5171",
            fontSize = 40.sp,
            modifier = Modifier.padding(top = 130.dp)
        )

        val dialPadItems = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(top = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(count = dialPadItems.size) { index ->
                val item = dialPadItems[index]
                DialPadKey(text = item)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.padding(bottom = 80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.video),
                contentDescription = "Video Call",
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(30.dp))
            Image(
                painter = painterResource(id = R.drawable.call),
                contentDescription = "Call",
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(30.dp))
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Backspace",
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
fun DialPadKey(text: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 40.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SumphpTheme {
        MainScreen()
    }
}
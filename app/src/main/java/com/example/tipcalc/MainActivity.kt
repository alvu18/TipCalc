package com.example.tipcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tipcalc.ui.theme.TipCalcTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TipCalcTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TipCalculatorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TipCalculatorScreen(modifier: Modifier = Modifier) {
    var orderSum by remember { mutableStateOf("") }
    var dishesCount by remember { mutableStateOf("") }
    var tipPercent by remember { mutableFloatStateOf(0f) }
    var showTotal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val discountPercent = calculateDiscount(dishesCount.toIntOrNull() ?: 0)

    val tipAmount = calculateTip(orderSum.toFloatOrNull() ?: 0f, tipPercent)
    val discountAmount = calculateDiscountAmount(orderSum.toFloatOrNull() ?: 0f, discountPercent)

    val total = (orderSum.toFloatOrNull() ?: 0f) + tipAmount - discountAmount

    Scaffold(
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            // Сумма заказа
            OutlinedTextField(
                value = orderSum,
                onValueChange = { orderSum = it },
                label = { Text("Сумма заказа") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Количество блюд
            OutlinedTextField(
                value = dishesCount,
                onValueChange = { dishesCount = it },
                label = { Text("Количество блюд") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Text("Процент чаевых: ${tipPercent.toInt()}%")

            Slider(
                value = tipPercent,
                onValueChange = { newValue ->
                    tipPercent = newValue
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Чаевые: ${"%.2f".format(tipAmount)} ₽",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                valueRange = 0f..25f,
                steps = 4  // шаг 5%
            )

            Spacer(Modifier.height(20.dp))

            // Радиокнопки (НЕ нажимаются пользователем)
            Text("Скидка (программный выбор): $discountPercent%")

            Column {
                DiscountRadio("1–2 блюда → 3%", selected = discountPercent == 3)
                DiscountRadio("3–5 блюд → 5%", selected = discountPercent == 5)
                DiscountRadio("6–10 блюд → 7%", selected = discountPercent == 7)
                DiscountRadio("Более 10 блюд → 10%", selected = discountPercent == 10)
            }

            Spacer(Modifier.height(20.dp))

            // Поле со скидкой / Итогом
            Text(
                text = if (!showTotal)
                    "Скидка: ${"%.2f".format(discountAmount)} ₽"
                else
                    "Итого: ${"%.2f".format(total)} ₽",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(20.dp))


            Button(
                onClick = { showTotal = !showTotal },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showTotal) {
                    Text("Скидка")
                }
                else {
                    Text("Итого")

                }
            }

        }
    }
}

@Composable
fun DiscountRadio(text: String, selected: Boolean) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = {})
        Text(text)
    }
}

fun calculateTip(sum: Float, percent: Float): Float = sum * percent / 100f

fun calculateDiscount(dishes: Int): Int = when {
    dishes in 1..2 -> 3
    dishes in 3..5 -> 5
    dishes in 6..10 -> 7
    dishes > 10 -> 10
    else -> 0
}

fun calculateDiscountAmount(sum: Float, discountPercent: Int): Float =
    sum * discountPercent / 100f
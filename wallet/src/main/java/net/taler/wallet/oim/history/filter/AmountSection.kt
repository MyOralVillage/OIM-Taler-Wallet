import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import net.taler.database.filter.AmountFilter
import net.taler.database.data_models.Amount
import net.taler.common.R.drawable.*

@Composable
fun AmountFilterSelector(
    onFilterChanged: (AmountFilter?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCurrency by remember { mutableStateOf("CHF") }
    var filterType by remember { mutableStateOf<FilterType>(FilterType.None) }

    // Exact amount
    var exactValue by remember { mutableStateOf("") }
    var exactFraction by remember { mutableStateOf("") }

    // Range amounts
    var rangeMinValue by remember { mutableStateOf("") }
    var rangeMinFraction by remember { mutableStateOf("") }
    var rangeMaxValue by remember { mutableStateOf("") }
    var rangeMaxFraction by remember { mutableStateOf("") }

    // Multiple amounts
    var multipleAmounts by remember { mutableStateOf<List<Amount>>(emptyList()) }
    var tempValue by remember { mutableStateOf("") }
    var tempFraction by remember { mutableStateOf("") }

    val supportsDecimals = selectedCurrency != "XOF"

    val currentFilter by remember {
        derivedStateOf {
            when (filterType) {
                FilterType.None -> null
                FilterType.Exact -> {
                    if (exactValue.isNotBlank()) {
                        AmountFilter.Exact(
                            Amount(
                                value = exactValue.toLongOrNull() ?: 0L,
                                fraction = if (supportsDecimals) exactFraction.toIntOrNull()
                                    ?: 0 else 0,
                                currency = selectedCurrency
                            )
                        )
                    } else null
                }

                FilterType.Range -> {
                    if (rangeMinValue.isNotBlank() && rangeMaxValue.isNotBlank()) {
                        AmountFilter.Range(
                            min = Amount(
                                value = rangeMinValue.toLongOrNull() ?: 0L,
                                fraction = if (supportsDecimals) rangeMinFraction.toIntOrNull()
                                    ?: 0 else 0,
                                currency = selectedCurrency
                            ),
                            max = Amount(
                                value = rangeMaxValue.toLongOrNull() ?: 0L,
                                fraction = if (supportsDecimals) rangeMaxFraction.toIntOrNull()
                                    ?: 0 else 0,
                                currency = selectedCurrency
                            )
                        )
                    } else null
                }

                FilterType.Multiple -> {
                    if (multipleAmounts.isNotEmpty()) {
                        AmountFilter.OneOrMoreOf(multipleAmounts.toSet())
                    } else null
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Currency Selection Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("CHF", "EUR", "XOF", "SLE").forEach { currency ->
                CurrencyButton(
                    currency = currency,
                    isSelected = selectedCurrency == currency,
                    onClick = {
                        selectedCurrency = currency
                        filterType = FilterType.None
                        multipleAmounts = emptyList()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Filter Type Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTypeButton(
                filterType = FilterType.None,
                isSelected = filterType == FilterType.None,
                currency = selectedCurrency,
                onClick = { filterType = FilterType.None },
                modifier = Modifier.weight(1f)
            )
            FilterTypeButton(
                filterType = FilterType.Exact,
                isSelected = filterType == FilterType.Exact,
                currency = selectedCurrency,
                onClick = { filterType = FilterType.Exact },
                modifier = Modifier.weight(1f)
            )
            FilterTypeButton(
                filterType = FilterType.Range,
                isSelected = filterType == FilterType.Range,
                currency = selectedCurrency,
                onClick = { filterType = FilterType.Range },
                modifier = Modifier.weight(1f)
            )
            FilterTypeButton(
                filterType = FilterType.Multiple,
                isSelected = filterType == FilterType.Multiple,
                currency = selectedCurrency,
                onClick = { filterType = FilterType.Multiple },
                modifier = Modifier.weight(1f)
            )
        }

        // Filter Configuration based on type
        when (filterType) {
            FilterType.None -> {
                // Show preview only
            }
            FilterType.Exact -> {
                AmountInput(
                    value = exactValue,
                    fraction = exactFraction,
                    onValueChange = { exactValue = it },
                    onFractionChange = { exactFraction = it },
                    supportsDecimals = supportsDecimals,
                    currency = selectedCurrency
                )
            }
            FilterType.Range -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmountInput(
                        value = rangeMinValue,
                        fraction = rangeMinFraction,
                        onValueChange = { rangeMinValue = it },
                        onFractionChange = { rangeMinFraction = it },
                        supportsDecimals = supportsDecimals,
                        currency = selectedCurrency,
                        label = "MIN"
                    )
                    AmountInput(
                        value = rangeMaxValue,
                        fraction = rangeMaxFraction,
                        onValueChange = { rangeMaxValue = it },
                        onFractionChange = { rangeMaxFraction = it },
                        supportsDecimals = supportsDecimals,
                        currency = selectedCurrency,
                        label = "MAX"
                    )
                }
            }
            FilterType.Multiple -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AmountInput(
                            value = tempValue,
                            fraction = tempFraction,
                            onValueChange = { tempValue = it },
                            onFractionChange = { tempFraction = it },
                            supportsDecimals = supportsDecimals,
                            currency = selectedCurrency,
                            modifier = Modifier.weight(1f)
                        )
                        Card(
                            modifier = Modifier
                                .size(56.dp)
                                .clickable {
                                    if (tempValue.isNotBlank()) {
                                        val newAmount = Amount(
                                            value = tempValue.toLongOrNull() ?: 0L,
                                            fraction = if (supportsDecimals) tempFraction.toIntOrNull()
                                                ?: 0 else 0,
                                            currency = selectedCurrency
                                        )
                                        if (!multipleAmounts.contains(newAmount)) {
                                            multipleAmounts = multipleAmounts + newAmount
                                        }
                                        tempValue = ""
                                        tempFraction = ""
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(
                                    bitmap = ImageBitmap.imageResource(android.R.drawable.ic_input_add),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Display added amounts
                    multipleAmounts.forEach { amount ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    multipleAmounts = multipleAmounts - amount
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Image(
                                        bitmap = ImageBitmap.imageResource(getSmallestDenomination(selectedCurrency)),
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Image(
                                    bitmap = ImageBitmap.imageResource(android.R.drawable.ic_delete),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Preview Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val filter = currentFilter) {
                    null -> {
                        Image(
                            bitmap = ImageBitmap.imageResource(android.R.drawable.ic_delete),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    is AmountFilter.Exact -> {
                        Image(
                            bitmap = ImageBitmap.imageResource(getSmallestDenomination(selectedCurrency)),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    is AmountFilter.Range -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = ImageBitmap.imageResource(getSmallestDenomination(selectedCurrency)),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                contentScale = ContentScale.Fit
                            )
                            Box(
                                modifier = Modifier.size(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(3) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.onSurface,
                                                    RoundedCornerShape(4.dp)
                                                )
                                        )
                                    }
                                }
                            }
                            Image(
                                bitmap = ImageBitmap.imageResource(getLargestDenomination(selectedCurrency)),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    is AmountFilter.OneOrMoreOf -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val denominations = getDenominationsForMultiple(selectedCurrency)
                            denominations.take(3).forEach { resId ->
                                Image(
                                    bitmap = ImageBitmap.imageResource(resId),
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyButton(
    currency: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Currency flag or symbol representation using denominations
            Image(
                bitmap = ImageBitmap.imageResource(getSmallestDenomination(currency)),
                contentDescription = currency,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun FilterTypeButton(
    filterType: FilterType,
    isSelected: Boolean,
    currency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            when (filterType) {
                FilterType.None -> {
                    Image(
                        bitmap = ImageBitmap.imageResource(android.R.drawable.ic_delete),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
                FilterType.Exact -> {
                    Image(
                        bitmap = ImageBitmap.imageResource(getSmallestDenomination(currency)),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                FilterType.Range -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = ImageBitmap.imageResource(getSmallestDenomination(currency)),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                        Box(modifier = Modifier.size(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.onSurface,
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                        Image(
                            bitmap = ImageBitmap.imageResource(getLargestDenomination(currency)),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                FilterType.Multiple -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(3) {
                            Image(
                                bitmap = ImageBitmap.imageResource(getSmallestDenomination(currency)),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountInput(
    value: String,
    fraction: String,
    onValueChange: (String) -> Unit,
    onFractionChange: (String) -> Unit,
    supportsDecimals: Boolean,
    currency: String,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (label != null) {
                Image(
                    bitmap = ImageBitmap.imageResource(
                        if (label == "MIN") getSmallestDenomination(currency)
                        else getLargestDenomination(currency)
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }

            TextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                        onValueChange(newValue)
                    }
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (supportsDecimals) {
                Image(
                    bitmap = ImageBitmap.imageResource(getSmallestDenomination(currency)),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )

                TextField(
                    value = fraction,
                    onValueChange = { newValue ->
                        if ((newValue.all { it.isDigit() } && newValue.length <= 2) || newValue.isEmpty()) {
                            onFractionChange(newValue)
                        }
                    },
                    modifier = Modifier.width(80.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}

private enum class FilterType {
    None, Exact, Range, Multiple
}

private fun getSmallestDenomination(currency: String): Int {
    return when (currency) {
        "CHF" -> chf_zero_point_five
        "XOF" -> xof_one
        "EUR" -> eur_zero_point_zero_one
        "SLE" -> sle_zero_point_zero_one
        else -> android.R.drawable.ic_menu_help
    }
}

private fun getLargestDenomination(currency: String): Int {
    return when (currency) {
        "CHF" -> chf_hundred_thousand
        "XOF" -> xof_ten_thousand
        "EUR" -> eur_two_hundred
        "SLE" -> sle_one_thousand
        else -> android.R.drawable.ic_menu_help
    }
}

private fun getDenominationsForMultiple(currency: String): List<Int> {
    return when (currency) {
        "CHF" -> listOf(chf_one_hundred, chf_two_hundred, chf_five_hundred)
        "XOF" -> listOf(xof_one_hundred, xof_two_hundred, xof_five_hundred)
        "EUR" -> listOf(eur_one, eur_two, eur_five)
        "SLE" -> listOf(sle_one, sle_two, sle_five)
        else -> emptyList()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAmountFilterSelector() {
    MaterialTheme {
        Surface {
            AmountFilterSelector()
        }
    }
}
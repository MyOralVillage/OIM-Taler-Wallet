/*
 * This file is part of GNU Taler
 * (C) 2025 Taler Systems S.A.
 * GPLv3-or-later
 */

package net.taler.wallet.oim.send.app

import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import net.taler.wallet.oim.send.screens.PurposeScreen
import net.taler.wallet.oim.send.screens.QrScreen
import net.taler.wallet.oim.send.screens.SendScreen

// DB / model imports (database module)
import net.taler.database.TranxHistory
import net.taler.database.data_models.Amount
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.Timestamp
import net.taler.database.data_models.TranxPurp
import net.taler.database.data_models.tranxPurpLookup

private enum class Screen { Send, Purpose, Qr }

@Composable
fun OimSendApp() {
    OimTheme {
        val ctx = LocalContext.current.applicationContext

        // Initialize local TX DB once (TranxHistory is @RequiresApi 34)
        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                !TranxHistory.isIniti
            ) {
                TranxHistory.init(ctx)
            }
        }

        var screen by remember { mutableStateOf(Screen.Send) }
        var amount by remember { mutableStateOf(0) }
        var chosenPurpose by remember { mutableStateOf<String?>(null) }

        // Currency (QR + Amount)
        val currencyCode = "SLE"
        val currencyLabel = "Leones"
        val balance = 25 // demo header

        when (screen) {
            Screen.Send -> SendScreen(
                balance = balance,
                amount = amount,
                onAdd = { amount += it },
                onRemoveLast = { removed -> amount = (amount - removed).coerceAtLeast(0) },
                onChoosePurpose = { screen = Screen.Purpose },
                onSend = { screen = Screen.Purpose }
            )

            Screen.Purpose -> PurposeScreen(
                balance = balance,
                onBack = { screen = Screen.Send },
                onDone = { pickedPurpose ->
                    chosenPurpose = pickedPurpose

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        if (!TranxHistory.isIniti) TranxHistory.init(ctx)

                        val amt = Amount.fromString(currencyCode, amount.toString())
                        val tid = "DEMO_TX_${System.currentTimeMillis()}" // stable-enough, still “hardcoded”
                        val ts = Timestamp.now()
                        val dir = FilterableDirection.OUTGOING

                        // Map the picked string to a TranxPurp:
                        val purp: TranxPurp? = mapPickedPurposeToTranxPurp(pickedPurpose)

                        TranxHistory.newTransaction(
                            tid = tid,
                            purp = purp,   // null is fine if no match yet
                            amt = amt,
                            dir = dir,
                            tms = ts
                        )
                    }

                    screen = Screen.Qr
                }
            )

            Screen.Qr -> QrScreen(
                amount = amount,
                currencyCode = currencyCode,    // used in QR payload
                displayLabel = currencyLabel,   // visual label
                purpose = chosenPurpose ?: "",
                onBack = { screen = Screen.Send }
            )
        }
    }
}

/**
 * Try to resolve the UI label/cmp into one of the sealed TranxPurp objects.
 * Order:
 *  1) exact CMP key (as-is)
 *  2) CMP uppercased
 *  3) label with spaces→underscores, uppercased
 *  4) match against assetLabel case-insensitively
 */
private fun mapPickedPurposeToTranxPurp(picked: String): TranxPurp? {
    // try direct CMP keys
    tranxPurpLookup[picked]?.let { return it }
    tranxPurpLookup[picked.uppercase()]?.let { return it }
    tranxPurpLookup[picked.replace(' ', '_').uppercase()]?.let { return it }

    // try assetLabel (case-insensitive)
    return tranxPurpLookup.values.firstOrNull { it.assetLabel.equals(picked, ignoreCase = true) }
}

<<<<<<< HEAD
/*
<<<<<<< HEAD
 * This file is part of GNU Taler
 * (C) 2025 Taler Systems S.A.
<<<<<<< HEAD
 *
 * GNU Taler is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * GNU Taler is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>
 */

/*
=======
>>>>>>> 5c7011a (fixed preview animations)
=======
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
 * GPLv3-or-later
=======
/**
 * ## OimTopBarCentered
 *
 * Center-aligned top bar composable displaying the user’s wallet balance and a
 * *Send* button in the upper-right corner. Part of the OIM Send UI, it provides
 * quick access to initiate transfers while visually anchoring the balance
 * display with the “chest” icon.
 *
 * The component retrieves its button and chest imagery via drawable mappers
 * from [net.taler.wallet.oim.res_mapping_extensions.Buttons].
 *
 * @param balance The current wallet balance shown at the top center.
 * @param onSendClick Callback invoked when the Send button is pressed.
 *
 * @see net.taler.wallet.oim.res_mapping_extensions.Buttons
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
 */

package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.res_mapping_extensions.Buttons
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper

<<<<<<< HEAD
<<<<<<< HEAD
// TODO refactor to use res_mapping_extensions;
// idk how you want to set up to choose the
// correct chest icon (maybe based on currency?)

//@Composable
//fun OimTopBarCentered(
//    balance: Int,
//    onSendClick: () -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(top = 8.dp)
//    ) {
//        // Top-right SEND
//        Box(
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .size(64.dp)
//                .clip(RoundedCornerShape(32.dp))
//                .background(Color(0x33000000))
//                .clickable { onSendClick() },
//            contentAlignment = Alignment.Center
//        ) {
//            Image(
//                painter = assetPainterOrPreview(ICON_SEND),
//                contentDescription = "Send",
//                modifier = Modifier.size(44.dp),
//                contentScale = ContentScale.Fit
//            )
//        }
//
//        // Centered chest + balance
//        Column(
//            modifier = Modifier.align(Alignment.TopCenter),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Image(
//                painter = assetPainterOrPreview(ICON_CHEST),
//                contentDescription = "Chest",
//                modifier = Modifier.size(64.dp),
//                contentScale = ContentScale.Fit
//            )
//            Spacer(Modifier.height(8.dp))
//            Text(
//                text = "$balance Leones",
//                color = Color.White,
//                fontSize = 28.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//    }
//}
=======
@Composable
fun OimTopBarCentered(
    balance: Int,
    onSendClick: () -> Unit
) {
=======
@Composable
fun OimTopBarCentered(
    balance: Amount,
    onSendClick: () -> Unit
) {
<<<<<<< HEAD
    val sendBitmap = Buttons("send").resourceMapper()
    val chestBitmap = Buttons("chest_open").resourceMapper()

>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
>>>>>>> 89f0c7f (refactored svgs to webp, reduced og taler/res by ~80%; total APK size down by ~50%. Needs more fixes/integration)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Top-right SEND button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0x33000000))
                .clickable { onSendClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
<<<<<<< HEAD
<<<<<<< HEAD
                painter = assetPainterOrPreview(ICON_SEND, PreviewAssets.id(ICON_SEND)),
=======
                bitmap = sendBitmap,
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
                painter = painterResource(Buttons("send").resourceMapper()),
>>>>>>> 89f0c7f (refactored svgs to webp, reduced og taler/res by ~80%; total APK size down by ~50%. Needs more fixes/integration)
                contentDescription = "Send",
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Centered chest + balance
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
<<<<<<< HEAD
<<<<<<< HEAD
                painter = assetPainterOrPreview(ICON_CHEST, PreviewAssets.id(ICON_CHEST)),
=======
                bitmap = chestBitmap,
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
                painter = painterResource(Buttons("chest_open").resourceMapper()),
>>>>>>> 89f0c7f (refactored svgs to webp, reduced og taler/res by ~80%; total APK size down by ~50%. Needs more fixes/integration)
                contentDescription = "Chest",
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${balance.amountStr} ${balance.spec?.name ?: balance.currency}",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
<<<<<<< HEAD
}
<<<<<<< HEAD
>>>>>>> 5c7011a (fixed preview animations)
=======
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
}
>>>>>>> 321d128 (updated send to be more dynamic)

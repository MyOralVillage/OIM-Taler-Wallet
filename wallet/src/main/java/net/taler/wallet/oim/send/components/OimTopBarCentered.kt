/*
<<<<<<< HEAD
 * This file is part of GNU Taler
 * (C) 2025 Taler Systems S.A.
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
 * GPLv3-or-later
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Top-right SEND
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
                painter = assetPainterOrPreview(ICON_SEND, PreviewAssets.id(ICON_SEND)),
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
                painter = assetPainterOrPreview(ICON_CHEST, PreviewAssets.id(ICON_CHEST)),
                contentDescription = "Chest",
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$balance Leones",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
>>>>>>> 5c7011a (fixed preview animations)

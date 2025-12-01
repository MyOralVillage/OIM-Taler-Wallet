package net.taler.wallet.oim.history.components
/**
 * HISTORY MODULE – SIDE BUTTON COMPONENTS
 *
 * This file defines the composables used for the vertical action bar displayed
 * on the OIM transaction history screen. These buttons allow users to:
 *  - switch between the River View and the Timeline/List View,
 *  - open the send flow,
 *  - open the receive flow.
 *
 * MAIN COMPOSABLES:
 *  - HistorySideButtons(): Lays out the three-column sidebar containing
 *    Receive, Toggle-View, and Send buttons. It sizes icons dynamically
 *    based on the current window height using LocalWindowInfo.
 *
 *  - HistorySideButton(): The reusable UI component for each individual button.
 *    Displays a bitmap icon with rounded background, click behaviour, and
 *    responsive sizing.
 *
 * INTEGRATION:
 *  - Used directly inside OimTransactionHistoryScreen.
 *  - Connected to UIIcons + OimColours for theming and consistent icon mapping.
 *  - Acts as the main interaction entry point for switching between the
 *    RiverCanvas view and the standard TransactionList view.
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import net.taler.wallet.oim.utils.assets.OimColours
import net.taler.wallet.oim.utils.res_mappers.UIIcons

/**  * Column of side buttons for transaction history screen.  */
@Composable
fun HistorySideButtons(
    showRiver: Boolean,
    onToggleView: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val size = LocalWindowInfo.current.containerSize

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 5.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Receive button
        HistorySideButton(
            bitmap = UIIcons("receive").resourceMapper(),
            bgColor = OimColours.INCOMING_COLOUR,
            onClick = onReceiveClick,
            size = size
        )

        // Toggle view button
        HistorySideButton(
            bitmap = if (showRiver) UIIcons("river").resourceMapper()
            else UIIcons("timeline").resourceMapper(),
            bgColor = Color.Transparent,
            onClick = onToggleView,
            contentDescription = if (showRiver) "Show list" else "Show river",
            size = size
        )

        // Send button
        HistorySideButton(
            bitmap = UIIcons("send").resourceMapper(),
            bgColor = OimColours.OUTGOING_COLOUR,
            onClick = onSendClick,
            size = size
        )
    }
}

/** Individual side button with bitmap icon.  */
@Composable
fun HistorySideButton(
    bitmap: ImageBitmap,
    bgColor: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.IntSize,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size((size.height * 0.06f).dp)
            .clip(RoundedCornerShape(19.dp))
            .background(bgColor.copy(alpha = 0.9f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = Modifier.size((size.height * 0.06f).dp)
        )
    }
}
/*
 * This file is part of GNU Taler
 * (C) 2020 Taler Systems S.A.
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

package net.taler.lib.android

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.taler.common.R
import net.taler.common.databinding.BottomsheetErrorBinding

/**
 * A simple [BottomSheetDialogFragment] for displaying error messages.
 *
 * Includes:
 * - A main message ([mainText]).
 * - An optional detailed message ([detailText]).
 * - Close button to dismiss the sheet.
 * - Share button to share the error message via other apps.
 */
class ErrorBottomSheet : BottomSheetDialogFragment() {

    companion object {
        /**
         * Creates a new instance of [ErrorBottomSheet] with the given main and detailed texts.
         *
         * @param mainText The primary error message to display.
         * @param detailText Optional detailed message.
         * @return A configured [ErrorBottomSheet] instance.
         */
        fun newInstance(mainText: String, detailText: String) = ErrorBottomSheet().apply {
            arguments = Bundle().apply {
                putString("TEXT_MAIN", mainText)
                putString("TEXT_DETAIL", detailText)
            }
            setStyle(STYLE_NORMAL, R.style.ErrorBottomSheetTheme)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ui = BottomsheetErrorBinding.inflate(inflater, container, false)
        val args = requireArguments()
        val mainText = args.getString("TEXT_MAIN")
        val detailText = args.getString("TEXT_DETAIL")

        ui.mainText.text = mainText
        ui.detailText.text = detailText

        // Close button dismisses the bottom sheet
        ui.closeButton.setOnClickListener { dismiss() }

        // Share button allows sending the error message to other apps
        ui.shareButton.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "$mainText\n\n$detailText")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        return ui.root
    }
}
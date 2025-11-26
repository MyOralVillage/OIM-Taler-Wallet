/*
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

package net.taler.common.data_models

/**
 * Exception thrown when parsing or constructing an [Amount] fails due to invalid format,
 * illegal currency identifier, or fractional overflow.
 * @param msg Optional error message for debugging.
 * @param cause Underlying exception, if any.
 */
class AmountParserException(msg: String? = null, cause: Throwable? = null) :
    Exception(msg, cause)

/**
 * Exception thrown when arithmetic operations (addition, subtraction, multiplication)
 * cause an [Amount]'s integral part to exceed [Amount.Companion.MAX_VALUE].
 *
 * @param msg Optional error message for debugging.
 * @param cause Underlying exception, if any.
 */
class AmountOverflowException(msg: String? = null, cause: Throwable? = null) :
    Exception(msg, cause)

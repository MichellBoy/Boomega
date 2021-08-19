/*
 * Boomega
 * Copyright (C)  2021  Daniel Gyoerffy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dansoftware.boomega.gui.util

fun java.awt.Color.toFXColor(): javafx.scene.paint.Color {
    return javafx.scene.paint.Color.rgb(red, green, blue, alpha / 255.0)
}

fun javafx.scene.paint.Color.toAWTColor(): java.awt.Color {
    return java.awt.Color(red.toFloat(), green.toFloat(), blue.toFloat(), opacity.toFloat())
}
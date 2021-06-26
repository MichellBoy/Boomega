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

package com.dansoftware.boomega.gui.theme;

import com.dansoftware.boomega.i18n.I18N;
import jfxtras.styles.jmetro.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * A DarkTheme is a {@link Theme} implementation that applies a dark-looking theme.
 *
 * @author Daniel Gyorffy
 */
public class DarkTheme extends JMetroTheme {

    public DarkTheme() {
        super(Style.DARK);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected @NotNull List<String> additionalStyleSheets() {
        return Collections.singletonList(getClass().getResource("dark.css").toExternalForm());
    }

    public static ThemeMeta<DarkTheme> getMeta() {
        return new ThemeMeta<>(DarkTheme.class, () -> I18N.getValue("app.ui.theme.dark"), InternalThemeDesigner.INSTANCE);
    }
}

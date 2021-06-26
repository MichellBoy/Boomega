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

package com.dansoftware.boomega.gui.updatedialog.segment.notification;

import com.dansoftware.boomega.gui.context.Context;
import com.dansoftware.boomega.i18n.I18N;
import com.dansoftware.boomega.update.UpdateInformation;
import com.dansoftware.sgmdialog.FixedContentSegment;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NotificationSegment extends FixedContentSegment {

    private final Context context;
    private final UpdateInformation updateInformation;

    public NotificationSegment(@NotNull Context context, @NotNull UpdateInformation updateInformation) {
        super(I18N.getValues().getString("update.segment.start.name"));
        this.context = Objects.requireNonNull(context);
        this.updateInformation = updateInformation;
    }

    @Override
    protected @NotNull Node createContent() {
        return new NotificationSegmentView(updateInformation);
    }
}

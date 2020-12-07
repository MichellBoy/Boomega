package com.dansoftware.libraryapp.gui.pluginmanager;

import com.dansoftware.libraryapp.gui.context.Context;
import com.dansoftware.libraryapp.gui.context.ContextTransformable;
import com.dansoftware.libraryapp.gui.pluginmanager.adder.PluginAdderModule;
import com.dansoftware.libraryapp.gui.pluginmanager.list.PluginListModule;
import com.dansoftware.libraryapp.gui.theme.Theme;
import com.dansoftware.libraryapp.gui.theme.Themeable;
import com.dlsc.workbenchfx.Workbench;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * A {@link PluginManager} is a place where the user can add/remove application plugins.
 *
 * @author Daniel Gyorffy
 */
public class PluginManager extends Workbench implements Themeable, ContextTransformable {

    private final Context asContext;

    PluginManager(@NotNull List<File> pluginFiles) {
        asContext = Context.from(this);
        ObservableList<File> observablePluginList = FXCollections.observableArrayList(pluginFiles);
        getModules().addAll(new PluginListModule(asContext, observablePluginList), new PluginAdderModule(asContext, observablePluginList));
        Theme.registerThemeable(this);
    }

    @Override
    public void handleThemeApply(Theme oldTheme, Theme newTheme) {
        oldTheme.getGlobalApplier().applyBack(this);
        newTheme.getGlobalApplier().apply(this);
    }

    @Override
    public @NotNull Context getContext() {
        return this.asContext;
    }
}

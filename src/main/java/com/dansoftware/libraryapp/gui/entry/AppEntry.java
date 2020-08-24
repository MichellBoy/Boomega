package com.dansoftware.libraryapp.gui.entry;

import com.dansoftware.libraryapp.db.Database;
import com.dansoftware.libraryapp.gui.entry.login.LoginActivity;
import com.dansoftware.libraryapp.gui.entry.login.data.LoginData;
import com.dansoftware.libraryapp.gui.entry.mainview.MainActivity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public class AppEntry implements Context {

    private static final ObservableSet<AppEntry> showingEntries =
            FXCollections.synchronizedObservableSet(FXCollections.observableSet());

    private static final ObservableSet<AppEntry> showingEntriesUnmodifiable =
            FXCollections.unmodifiableObservableSet(showingEntries);

    private final BooleanProperty showing;
    private final LoginActivity loginActivity;

    private Context subContext;

    public AppEntry() {
        this(LoginData.empty());
    }

    public AppEntry(@NotNull LoginData loginData) {
        this.loginActivity = new LoginActivity(loginData);
        this.subContext = loginActivity;
        this.showing = new SimpleBooleanProperty();
        this.showing.bind(this.loginActivity.showingProperty());
        this.showing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                AppEntry.showingEntries.add(this);
            } else {
                AppEntry.showingEntries.remove(this);
            }
        });
    }

    public boolean show() {
        Optional<Database> databaseOptional = loginActivity.show();
        databaseOptional.ifPresent(database -> {
            var mainActivity = new MainActivity(database);
            this.subContext = mainActivity;
            this.showing.unbind();
            this.showing.bind(mainActivity.showingProperty());
            mainActivity.show();
        });

        return databaseOptional.isPresent();
    }

    public boolean isShowing() {
        return showing.get();
    }

    public ReadOnlyBooleanProperty showingProperty() {
        return showing;
    }

    @Override
    public void showOverlay(Region region) {
        this.subContext.showOverlay(region);
    }

    @Override
    public void showOverlay(Region region, boolean blocking) {
        this.subContext.showOverlay(region, blocking);
    }

    @Override
    public void hideOverlay(Region region) {
        this.subContext.hideOverlay(region);
    }

    @Override
    public void showErrorDialog(String title, String message, Consumer<ButtonType> onResult) {
        this.subContext.showErrorDialog(title, message, onResult);
    }

    @Override
    public void showErrorDialog(String title, String message, Exception exception, Consumer<ButtonType> onResult) {
        this.subContext.showErrorDialog(title, message, exception, onResult);
    }

    @Override
    public void showInformationDialog(String title, String message, Consumer<ButtonType> onResult) {
        this.subContext.showInformationDialog(title, message, onResult);
    }

    @Override
    public void requestFocus() {
        this.subContext.requestFocus();
    }

    /**
     * Returns a read-only set that contains all {@link AppEntry} objects that is
     * showing.
     *
     * @return the set of AppEntry objects.
     */
    public static ObservableSet<AppEntry> getShowingEntries() {
        return showingEntriesUnmodifiable;
    }
}

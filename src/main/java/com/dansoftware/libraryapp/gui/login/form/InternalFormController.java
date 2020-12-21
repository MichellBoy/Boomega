package com.dansoftware.libraryapp.gui.login.form;

import com.dansoftware.libraryapp.appdata.Preferences;
import com.dansoftware.libraryapp.appdata.logindata.LoginData;
import com.dansoftware.libraryapp.db.Credentials;
import com.dansoftware.libraryapp.db.Database;
import com.dansoftware.libraryapp.db.DatabaseMeta;
import com.dansoftware.libraryapp.db.NitriteDatabase;
import com.dansoftware.libraryapp.gui.context.Context;
import com.dansoftware.libraryapp.gui.entry.DatabaseTracker;
import com.dansoftware.libraryapp.gui.login.DatabaseLoginListener;
import com.dansoftware.libraryapp.gui.mainview.MainActivity;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * Controller for the actual login form; handles the login requests
 *
 * @author Daniel Gyorffy
 */
public class InternalFormController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(InternalFormController.class);

    @FXML
    private TextField usernameInput;

    @FXML
    private TextField passwordInput;

    @FXML
    private CheckBox rememberBox;

    private final Context context;
    private final Preferences preferences;
    private final DatabaseTracker databaseTracker;
    private final LoginData loginData;
    private final Supplier<DatabaseMeta> selectedItemAccessor;
    private final DatabaseLoginListener databaseLoginListener;

    public InternalFormController(@NotNull Context context,
                                  @NotNull Preferences preferences,
                                  @NotNull DatabaseTracker databaseTracker,
                                  @NotNull LoginData loginData,
                                  @NotNull DatabaseLoginListener databaseLoginListener,
                                  @NotNull Supplier<DatabaseMeta> selectedItemAccessor) {
        this.context = Objects.requireNonNull(context, "Context shouldn't be null");
        this.preferences = Objects.requireNonNull(preferences);
        this.databaseTracker = Objects.requireNonNull(databaseTracker);
        this.loginData = Objects.requireNonNull(loginData, "LoginData shouldn't be null");
        this.selectedItemAccessor = Objects.requireNonNull(selectedItemAccessor, "SelectedItemAccessor shouldn't be null");
        this.databaseLoginListener = Objects.requireNonNull(databaseLoginListener, "DatabaseLoginListener shouldn't be null");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fillForm(this.loginData);
    }

    private void fillForm(@NotNull LoginData loginData) {
        DatabaseMeta autoLoginDatabase = loginData.getAutoLoginDatabase();
        if (autoLoginDatabase != null) {
            rememberBox.setSelected(Boolean.TRUE);

            Credentials credentials = loginData.getAutoLoginCredentials();
            if (credentials != null) {
                this.usernameInput.setText(credentials.getUsername());
                this.passwordInput.setText(credentials.getPassword());
            }
        }
    }

    @FXML
    private void login() {
        DatabaseMeta dbMeta = selectedItemAccessor.get();
        if (databaseTracker.isDatabaseUsed(dbMeta)) {
            MainActivity.getByDatabase(dbMeta)
                    .map(MainActivity::getContext)
                    .ifPresent(Context::toFront);
            return;
        }

        String username = StringUtils.trim(usernameInput.getText());
        String password = StringUtils.trim(passwordInput.getText());

        //creating an object that holds the credentials (username/password)
        Credentials credentials = new Credentials(username, password);
        loginData.setAutoLogin(rememberBox.isSelected());
        loginData.setAutoLoginCredentials(rememberBox.isSelected() ? credentials : null);

        Database database = NitriteDatabase.getAuthenticator()
                .onFailed((title, message, t) -> {
                    this.context.showErrorDialog(title, message, ((Exception) t));

                    logger.error("Failed to create/open the database", t);
                }).auth(dbMeta, credentials);

        if (database != null) {
            //creating the database was successful
            logger.debug("Signing in was successful; closing the LoginWindow");
            preferences.editor().put(Preferences.Key.LOGIN_DATA, loginData);
            databaseLoginListener.onDatabaseOpened(database);
            context.close();
        }
    }
}

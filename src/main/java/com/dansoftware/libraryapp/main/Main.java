package com.dansoftware.libraryapp.main;

import com.dansoftware.libraryapp.appdata.Preferences;
import com.dansoftware.libraryapp.appdata.logindata.LoginData;
import com.dansoftware.libraryapp.exception.UncaughtExceptionHandler;
import com.dansoftware.libraryapp.gui.entry.Context;
import com.dansoftware.libraryapp.gui.entry.DatabaseTracker;
import com.dansoftware.libraryapp.gui.theme.Theme;
import com.dansoftware.libraryapp.gui.updateview.UpdateActivity;
import com.dansoftware.libraryapp.launcher.ActivityLauncher;
import com.dansoftware.libraryapp.launcher.LauncherMode;
import com.dansoftware.libraryapp.log.LogFile;
import com.dansoftware.libraryapp.update.UpdateSearcher;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * The main class and javafx application starter.
 *
 * <p>
 * Responsible for initializing the application and launching the GUI
 *
 * @author Daniel Gyorffy
 */
public class Main extends BaseApplication {

    private static final Logger logger;

    static {
        //Configure the logger
        var logFile = new LogFile();
        System.setProperty("log.file.path", logFile.getAbsolutePath());
        System.setProperty("log.file.path.full", logFile.getPathWithExtension());

        logger = LoggerFactory.getLogger(Main.class);

        //Set the default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }


    /**
     * The main-method of the application;
     *
     * <p>
     * Starts the {@link InstanceService}.
     * <p>
     * If the {@link InstanceService} didn't stop the app,
     * the main launches the application with a
     * preloader.
     *
     * @see BaseApplication#launchApp(Class, String...)
     * @see InstanceService#open(String[])
     */
    public static void main(String[] args) {
        InstanceService.open(args);
        BaseApplication.launchApp(Main.class, args);
    }

    @Override
    public void init() {
        Preferences preferences = Preferences.getPreferences();
        logger.info("Configurations has been read successfully!");

        //adding the saved databases from the login-data to DatabaseTracker
        LoginData loginData = preferences.get(Preferences.Key.LOGIN_DATA);
        loginData.getLastDatabases().forEach(DatabaseTracker.getGlobal()::addDatabase);

        Locale.setDefault(preferences.get(Preferences.Key.LOCALE));
        logger.info("Locale is: {}", Locale.getDefault());

        Theme.setDefault(preferences.get(Preferences.Key.THEME));
        logger.info("Theme is: {}", Theme.getDefault());

        //searching for updates
        UpdateSearcher updateSearcher = new UpdateSearcher(Globals.VERSION_INFO);
        UpdateSearcher.UpdateSearchResult searchResult = updateSearcher.search();

        new InitActivityLauncher(getApplicationArgs(), preferences, loginData, searchResult).launch();
    }


    @Override
    public void stop() throws IOException {
        //writing all configurations
        Preferences preferences = Preferences.getPreferences();
        preferences.editor().commit();
    }

    /**
     * An {@link ActivityLauncher} implementation for starting the application.
     *
     * <p>
     * When an activity is launched, it also shows an "notifier box" if
     * a new update is available.
     */
    private static final class InitActivityLauncher extends ActivityLauncher {

        private final Preferences preferences;
        private final LoginData loginData;
        private final UpdateSearcher.UpdateSearchResult searchResult;

        private InitActivityLauncher(@NotNull List<String> args,
                                     @NotNull Preferences preferences,
                                     @NotNull LoginData loginData,
                                     @NotNull UpdateSearcher.UpdateSearchResult searchResult) {
            super(LauncherMode.INIT, args);
            this.preferences = preferences;
            this.loginData = loginData;
            this.searchResult = searchResult;
        }

        @Override
        protected LoginData getLoginData() {
            return loginData;
        }

        @Override
        protected void saveLoginData(LoginData loginData) {
            preferences.editor()
                    .set(Preferences.Key.LOGIN_DATA, loginData)
                    .tryCommit();
        }

        @Override
        protected void onActivityLaunched(Context context) {
            UpdateActivity updateActivity = new UpdateActivity(context, searchResult);
            updateActivity.show(false);
        }
    }
}

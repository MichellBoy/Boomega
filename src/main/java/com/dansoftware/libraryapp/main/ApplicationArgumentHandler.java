package com.dansoftware.libraryapp.main;

import com.dansoftware.libraryapp.gui.notification.Notification;
import com.dansoftware.libraryapp.gui.notification.NotificationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import static com.dansoftware.libraryapp.util.CommonUtils.isEmpty;

/**
 * This class responsible for checking the application arguments
 */
public class ApplicationArgumentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInitializer.class);

    private static File launchedFile;

    private ApplicationArgumentHandler() {
    }

    /**
     * Parses the String array as the application arguments.
     *
     * <p>
     * <b style='color:red'>Should be invoked by the main-method</b>
     *
     *
     * @param args the application arguments to parse
     * @see Main#main(String[])
     */
    public static void scan(String[] args) {
        if (isEmpty(args))
            return;

        String filePath = args[0];
        File file = new File(filePath);

        if (file.exists()) {
            launchedFile = file;
        } else {
            var cause = new FileNotFoundException(filePath);

            Notification.create()
                    .level(NotificationLevel.ERROR)
                    .msg("argument.handler.file.error")
                    .args(new Object[]{ file.getName() })
                    .cause(cause)
                    .show();

            LOGGER.error("Couldn't open file: " + file.getAbsolutePath(), cause);
        }
    }

    /**
     * Returns the launched file with an Optional wrapper
     *
     * @return the launched file with Optional
     */
    public static Optional<File> getLaunchedFile() {
        return Optional.ofNullable(launchedFile);
    }

}

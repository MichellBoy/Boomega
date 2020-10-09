package com.dansoftware.libraryapp.gui.firsttime.imp;

import com.dansoftware.libraryapp.appdata.Preferences;
import com.dansoftware.libraryapp.appdata.PreferencesImporter;
import com.dansoftware.libraryapp.gui.entry.Context;
import com.dansoftware.libraryapp.locale.I18N;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfigurationImportController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationImportController.class);

    private boolean imported;
    private final Context context;
    private final Preferences preferences;

    private File chosenFile;

    @FXML
    private TextField customLocationInput;

    @FXML
    private Button fileOpenerBtn;

    @FXML
    private ToggleGroup radioGroup;

    public ConfigurationImportController(@NotNull Context context, @NotNull Preferences preferences) {
        this.context = context;
        this.preferences = preferences;
    }

    @FXML
    private void importFromCustomLocation(ActionEvent event) {
        FileChooser fileChooser = createFileChooser();
        File file = fileChooser.showOpenDialog(context.getContextWindow());
        customLocationInput.setText(file.getAbsolutePath());
        chosenFile = file;
    }

    @FXML
    private void close() {
        if (chosenFile != null)
            try {
                PreferencesImporter preferencesImporter = new PreferencesImporter(preferences);
                preferencesImporter.importFromZip(chosenFile, preferences);
                imported = true;
                closeWindow();
            } catch (IOException | PreferencesImporter.InvalidZipContentException e) {
                logger.error("Failed to import configurations", e);
                context.showErrorDialog(
                        I18N.getAlertMsg("configuration.import.failed.title"),
                        I18N.getAlertMsg("configuration.import.failed.msg"), e
                );
            }
        else
            closeWindow();
    }

    private FileChooser createFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*"));
        FileChooser.ExtensionFilter zipFiles = new FileChooser.ExtensionFilter("Zip files", "*.zip");
        fileChooser.getExtensionFilters().add(zipFiles);
        fileChooser.setSelectedExtensionFilter(zipFiles);
        return fileChooser;
    }

    private void closeWindow() {
        ((Stage) context.getContextWindow()).close();
    }

    public boolean isImported() {
        return imported;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileOpenerBtn.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.FOLDER));
    }
}

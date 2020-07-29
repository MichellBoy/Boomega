package com.dansoftware.libraryapp.gui.updateview.page;

import com.dansoftware.libraryapp.gui.updateview.UpdateView;
import com.dansoftware.libraryapp.locale.I18N;
import com.dansoftware.libraryapp.update.UpdateInformation;
import com.sandec.mdfx.MDFXNode;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.ResourceBundle;

public class UpdatePageDetail extends UpdatePage {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePageDetail.class);

    private UpdatePageDownload updatePageDownload;

    @FXML
    private ScrollPane previewScrollPane;

    public UpdatePageDetail(@NotNull UpdateView updateView, @NotNull UpdatePage previous, @NotNull UpdateInformation information) {
        super(updateView, previous, information, UpdatePageDetail.class.getResource("UpdatePageDetail.fxml"));
    }

    private void loadPreview() {
        new Thread(new RawTextDownloaderTask(getInformation().getReviewUrl()) {{ //init block
            setOnRunning(e -> {
                ProgressBar progressBar = new ProgressBar();
                progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                previewScrollPane.setContent(progressBar);
            });

            setOnFailed(e -> {
                Throwable cause = e.getSource().getException();
                logger.error("Couldn't load the markdown-preview", cause);
                previewScrollPane.setContent(new PreviewErrorPlaceHolder(cause));
            });

            setOnSucceeded(e -> {
                String markdownRaw = getValue();
                // rendering the markdown-text into a javaFX node:
                var markdownDisplay = new MDFXNode(markdownRaw);
                previewScrollPane.setContent(markdownDisplay);
                previewScrollPane.setFitToHeight(false);
                previewScrollPane.setFitToWidth(true);
            });
        }}).start();
    }

    @FXML
    private void goToNextPage() {
        this.updatePageDownload = Objects.isNull(updatePageDownload) ?
                new UpdatePageDownload(getUpdateView(), this, getInformation()) : updatePageDownload;

        getUpdateView().setUpdatePage(updatePageDownload);
    }

    @Override
    public void reload() {
        this.loadPreview();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        previewScrollPane.setFitToWidth(true);
        loadPreview();
    }

    private final class PreviewErrorPlaceHolder extends StackPane {
        PreviewErrorPlaceHolder(Throwable cause) {
            var label = new Label(I18N.getGeneralWord("update.view.details.preview.failed"));
            var detailBtn = new Button(I18N.getGeneralWord("update.view.details.preview.failed.more"));
            detailBtn.setOnAction(event -> {
                UpdatePageDetail.this
                        .getUpdateView()
                        .getContext()
                        .showErrorDialog(I18N.getGeneralWord("update.view.details.preview.failed"),
                                null, (Exception) cause, buttonType -> {
                                });
            });

            var vBox = new VBox(5, label, detailBtn);
            var root = new Group(vBox);
            this.getChildren().add(root);
        }
    }

    /**
     * A ReviewDataDownloaderTask defines a process to download raw text from
     * the internet by the specified URL.
     * <p>
     * It can be easily executed on a background-thread.
     *
     * <pre>{@code
     * String url = ...; // with the http(s) protocol
     * var task = new RawTextDownloaderTask(url);
     * new Thread(task).start();
     * }</pre>
     *
     * We can handle the result by using the methods defined in {@link Task}
     * ({@link Task#setOnSucceeded(EventHandler)}, {@link Task#setOnFailed(EventHandler)} etc...)
     *
     * @see Task
     */
    private static class RawTextDownloaderTask extends Task<String> {
        private final String url;

        public RawTextDownloaderTask(@NotNull String url) {
            this.url = url;
        }

        @Override
        protected String call() throws Exception {

            URL url = new URL(this.url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            try (var input = new BufferedInputStream(urlConnection.getInputStream())) {
                StringBuilder stringBuilder = new StringBuilder();

                byte[] buf = new byte[500];
                int bytesRead;
                while ((bytesRead = input.read(buf)) > 0) {
                    stringBuilder.append(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
                }

                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        }
    }
}

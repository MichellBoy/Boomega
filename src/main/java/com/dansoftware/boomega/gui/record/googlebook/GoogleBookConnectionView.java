package com.dansoftware.boomega.gui.record.googlebook;

import com.dansoftware.boomega.db.Database;
import com.dansoftware.boomega.db.data.Record;
import com.dansoftware.boomega.db.data.ServiceConnection;
import com.dansoftware.boomega.googlebooks.GoogleBooksQueryBuilder;
import com.dansoftware.boomega.googlebooks.SingleGoogleBookQuery;
import com.dansoftware.boomega.googlebooks.Volume;
import com.dansoftware.boomega.gui.context.Context;
import com.dansoftware.boomega.gui.googlebooks.GoogleBookDetailsPane;
import com.dansoftware.boomega.gui.googlebooks.SearchParameters;
import com.dansoftware.boomega.gui.googlebooks.join.GoogleBookJoinerOverlay;
import com.dansoftware.boomega.gui.record.show.RecordTable;
import com.dansoftware.boomega.gui.util.BaseFXUtils;
import com.dansoftware.boomega.gui.util.I18NButtonTypes;
import com.dansoftware.boomega.i18n.I18N;
import com.dansoftware.boomega.util.concurrent.CachedExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jfxtras.styles.jmetro.JMetroStyleClass;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A {@link GoogleBookConnectionView} is a panel that shows the connection between
 * a Google Book volume and a {@link Record}. It allows to remove, modify and create
 * connection.
 *
 * @author Daniel Gyorffy
 */
public class GoogleBookConnectionView extends VBox {

    private static final Logger logger = LoggerFactory.getLogger(GoogleBookConnectionView.class);

    private static final String STYLE_CLASS = "google-book-dock";

    private final Context context;
    private final Database database;
    private final Cache<String, DetailsPane> cache;

    private final ObjectProperty<Runnable> onRefreshed = new SimpleObjectProperty<>();
    private final ObjectProperty<Task<Volume>> lastTask = new SimpleObjectProperty<>();
    private final ObjectProperty<DetailsPane> currentDetailsPane = new SimpleObjectProperty<>();
    private final StringProperty currentGoogleHandle = new SimpleStringProperty() {
        @Override
        protected void invalidated() {
            final Task<Volume> task = lastTask.get();
            if (task != null && task.isRunning()) {
                logger.debug("Running task found");
                task.cancel();
                logger.debug("Task cancelled");
            }
        }
    };

    private List<Record> items;

    public GoogleBookConnectionView(@NotNull Context context,
                                    @NotNull Database database,
                                    @Nullable List<Record> items) {
        this.context = context;
        this.database = database;
        this.cache = buildCache();
        this.getStyleClass().add(STYLE_CLASS);
        this.setItems(items);
    }

    private Cache<String, DetailsPane> buildCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    public void setOnRefreshed(@Nullable Runnable onRefreshed) {
        this.onRefreshed.set(onRefreshed);
    }

    public void setItems(@Nullable List<Record> items) {
        this.createCache();
        this.items = items;
        this.currentGoogleHandle.set(retrieveGoogleBookHandle(items));
        logger.debug("Google book handle retrieved: {}", currentGoogleHandle.get());
        buildBaseContent(currentGoogleHandle.get(), Optional.ofNullable(items).orElseGet(Collections::emptyList));
    }

    private void createCache() {
        final String handle = currentGoogleHandle.get();
        final DetailsPane pane = currentDetailsPane.get();
        if (handle != null && pane != null) {
            logger.debug("Creating cache for: '{}'...", currentGoogleHandle.get());
            cache.put(handle, pane);
            currentGoogleHandle.set(null);
            currentDetailsPane.set(null);
        }
    }

    private void refresh() {
        setItems(items);
        if (onRefreshed.get() != null) {
            onRefreshed.get().run();
        }
    }

    private void setContent(@NotNull Node content) {
        this.getChildren().setAll(content);
    }

    private void showProgress() {
        var progressBar = new ProgressBar();
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        getChildren().add(0, progressBar);
    }

    private void stopProgress() {
        getChildren().removeIf(e -> e instanceof ProgressBar);
    }

    private void buildBaseContent(@Nullable String googleHandle,
                                  @NotNull List<Record> items) {
        if (googleHandle != null) {
            buildDetailsPane(googleHandle);
        } else if (items.size() == 1) {
            setContent(new NoConnectionPlaceHolder(context, database, items.get(0), this::refresh));
        } else if (items.size() > 1) {
            setContent(new MultipleSelectionPlaceHolder());
        } else {
            setContent(new NoSelectionPlaceHolder());
        }
    }

    private void buildDetailsPane(String googleBookLink) {
        DetailsPane pane = cache.getIfPresent(googleBookLink);
        if (pane != null) {
            pane.items = items;
            currentDetailsPane.set(pane);
            setContent(pane);
        } else {
            Task<Volume> task = new VolumePullTask(googleBookLink);
            lastTask.set(task);
            CachedExecutor.INSTANCE.submit(task);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private String retrieveGoogleBookHandle(@Nullable List<Record> items) {
        if (items == null)
            return null;
        List<String> distinctHandles = items.stream()
                .map(Record::getServiceConnection)
                .map(ServiceConnection::getGoogleBookHandle)
                .distinct()
                .collect(Collectors.toList());
        if (distinctHandles.size() == 1)
            return distinctHandles.get(0);
        return null;
    }

    private class VolumePullTask extends Task<Volume> {

        private final String googleHandle;

        VolumePullTask(@NotNull String googleHandle) {
            this.googleHandle = googleHandle;
            this.initEventHandlers();
        }

        private void initEventHandlers() {
            this.setOnRunning(e -> showProgress());
            this.setOnSucceeded(event -> {
                logger.debug("Pull task succeeded.");
                stopProgress();
                currentDetailsPane.set(new DetailsPane(context, database, this.getValue(), items, GoogleBookConnectionView.this::refresh));
                setContent(currentDetailsPane.get());
            });
            this.setOnFailed(event -> {
                logger.error("Pull task failed.", event.getSource().getException());
                stopProgress();
                setContent(new ErrorPlaceHolder(context, event.getSource().getException()));
            });
        }

        @Override
        protected Volume call() throws Exception {
            logger.debug("Starting pull task...");
            return new SingleGoogleBookQuery(googleHandle).load();
        }
    }

    private static final class DetailsPane extends VBox {

        private final Context context;
        private final Database database;
        private final Runnable refresh;

        private List<Record> items;

        DetailsPane(@NotNull Context context,
                    @NotNull Database database,
                    @NotNull Volume volume,
                    @NotNull List<Record> items,
                    @NotNull Runnable refresh) {
            this.context = context;
            this.database = database;
            this.refresh = refresh;
            this.items = items;
            buildUI(volume);
        }

        private void buildUI(Volume volume) {
            setVgrow(this, Priority.ALWAYS);
            setSpacing(5.0);
            final var mainPane = new GoogleBookDetailsPane(context, volume);
            mainPane.setMinHeight(0);
            VBox.setVgrow(mainPane, Priority.ALWAYS);
            getChildren().add(mainPane);
            getChildren().add(buildRemoveButton());
        }

        private Button buildRemoveButton() {
            final var button = new Button(I18N.getValue("google.books.dock.remove_connection"));
            button.getStyleClass().add("remove-button");
            button.prefWidthProperty().bind(this.widthProperty());
            button.setOnAction(event -> {

                context.showDialog(
                        I18N.getValue("google.books.dock.remove.confirmation.title", items.size()),
                        buildPreviewTable(items),
                        it -> {
                            if (BaseFXUtils.typeEquals(it, ButtonType.YES)) {
                                CachedExecutor.INSTANCE.submit(buildConnectionRemoveTask());
                            }
                        }, I18NButtonTypes.CANCEL, I18NButtonTypes.YES);
            });
            return button;
        }

        private RecordTable buildPreviewTable(List<Record> items) {
            var recordTable = new RecordTable(0);
            recordTable.addColumn(RecordTable.ColumnType.INDEX_COLUMN);
            recordTable.addColumn(RecordTable.ColumnType.TYPE_INDICATOR_COLUMN);
            recordTable.addColumn(RecordTable.ColumnType.AUTHOR_COLUMN);
            recordTable.addColumn(RecordTable.ColumnType.TITLE_COLUMN);
            recordTable.getItems().setAll(items);
            recordTable.setPrefHeight(200);
            return recordTable;
        }

        private Task<Void> buildConnectionRemoveTask() {
            var task = new Task<Void>() {
                @SuppressWarnings("ConstantConditions")
                @Override
                protected Void call() {
                    items.stream()
                            .peek(record -> record.getServiceConnection().setGoogleBookLink(null))
                            .forEach(database::updateRecord);
                    return null;
                }
            };
            task.setOnRunning(event -> context.showIndeterminateProgress());
            task.setOnFailed(event -> {
                context.stopProgress();
                //TODO: ERROR DIALOG
                logger.error("Couldn't remove Google Book connection", event.getSource().getException());
                refresh.run();
            });
            task.setOnSucceeded(event -> {
                context.stopProgress();
                refresh.run();

                context.showInformationNotification(
                        I18N.getValue("google.books.dock.success_unjoin.title"),
                        null,
                        Duration.seconds(2)
                );
            });
            return task;
        }
    }

    private static final class ErrorPlaceHolder extends StackPane {

        private final Context context;
        private final Throwable cause;

        ErrorPlaceHolder(@NotNull Context context, Throwable cause) {
            this.context = context;
            this.cause = cause;
            buildUI();
        }

        private void buildUI() {
            this.getStyleClass().add(JMetroStyleClass.BACKGROUND);
            this.getChildren().add(
                    new Group(new VBox(20,
                            new StackPane(buildLabel()),
                            new StackPane(buildDetailsButton())))
            );
            VBox.setVgrow(this, Priority.ALWAYS);
        }

        private Label buildLabel() {
            var label = new Label(I18N.getValue("google.books.dock.placeholder.error"));
            label.getStyleClass().add("place-holder-label");
            return label;
        }

        private Button buildDetailsButton() {
            var button = new Button(
                    I18N.getValue("google.books.dock.placeholder.error.details"),
                    new MaterialDesignIconView(MaterialDesignIcon.DETAILS)
            );
            button.setOnAction(event -> context.showErrorDialog(StringUtils.EMPTY, StringUtils.EMPTY, (Exception) cause));
            return button;
        }
    }

    private static final class NoConnectionPlaceHolder extends StackPane {

        private final Context context;
        private final Database database;
        private final Record record;
        private final Runnable refresh;

        NoConnectionPlaceHolder(@NotNull Context context,
                                @NotNull Database database,
                                @NotNull Record record,
                                @NotNull Runnable refresh) {
            this.context = context;
            this.database = database;
            this.record = record;
            this.refresh = refresh;
            buildUI();
        }

        private void buildUI() {
            VBox.setVgrow(this, Priority.ALWAYS);
            this.getStyleClass().add(JMetroStyleClass.BACKGROUND);
            getChildren().add(buildContent());
        }

        private Node buildContent() {
            return new Group(new VBox(20,
                    new StackPane(buildLabel()),
                    buildConnectionButton()));
        }

        private Label buildLabel() {
            var label = new Label(I18N.getValue("google.books.dock.placeholder.noconn"));
            label.getStyleClass().add("place-holder-label");
            return label;
        }

        private Button buildConnectionButton() {
            var button = new Button(
                    I18N.getValue("google.books.dock.connection"),
                    new MaterialDesignIconView(MaterialDesignIcon.GOOGLE)
            );
            button.setOnAction(event -> showGoogleBookJoiner());
            return button;
        }

        public void showGoogleBookJoiner() {
            context.showOverlay(
                    new GoogleBookJoinerOverlay(context, buildSearchParameters(record), volume -> {
                        CachedExecutor.INSTANCE.submit(buildJoinActionTask(volume));
                    })
            );
        }

        private SearchParameters buildSearchParameters(@NotNull Record record) {
            return new SearchParameters()
                    .printType(record.getRecordType() == Record.Type.BOOK ?
                            GoogleBooksQueryBuilder.PrintType.BOOKS :
                            GoogleBooksQueryBuilder.PrintType.MAGAZINES)
                    .isbn(record.getIsbn())
                    .authors(String.join(",", Optional.ofNullable(record.getAuthors()).orElse(Collections.emptyList())))
                    .publisher(record.getPublisher())
                    .title(record.getTitle())
                    .language(record.getLanguage());
        }

        private Task<Void> buildJoinActionTask(Volume volume) {
            var task = new Task<Void>() {
                @SuppressWarnings("ConstantConditions")
                @Override
                protected Void call() {
                    record.getServiceConnection().setGoogleBookLink(volume.getSelfLink());
                    database.updateRecord(record);
                    return null;
                }
            };
            task.setOnRunning(event -> context.showIndeterminateProgress());
            task.setOnFailed(event -> {
                //TODO: ERROR DIALOG
                logger.error("Couldn't join with Google Book", event.getSource().getException());
                context.stopProgress();
            });
            task.setOnSucceeded(event -> {
                context.stopProgress();
                refresh.run();

                context.showInformationNotification(
                        I18N.getValue("google.books.dock.success_join.title"),
                        null,
                        Duration.seconds(2)
                );
            });
            return task;
        }
    }

    private static final class MultipleSelectionPlaceHolder extends StackPane {

        MultipleSelectionPlaceHolder() {
            buildUI();
        }

        private void buildUI() {
            this.getStyleClass().add(JMetroStyleClass.BACKGROUND);
            this.getChildren().add(buildLabel());
            VBox.setVgrow(this, Priority.ALWAYS);
        }

        private Label buildLabel() {
            var label = new Label(I18N.getValue("google.books.dock.placeholder.multiple"));
            label.getStyleClass().add("place-holder-label");
            return label;
        }
    }

    private static final class NoSelectionPlaceHolder extends StackPane {

        NoSelectionPlaceHolder() {
            buildUI();
        }

        private void buildUI() {
            this.getStyleClass().add(JMetroStyleClass.BACKGROUND);
            this.getChildren().add(buildLabel());
            VBox.setVgrow(this, Priority.ALWAYS);
        }

        private Label buildLabel() {
            var label = new Label(I18N.getValue("google.books.dock.placeholder.noselection"));
            label.getStyleClass().add("place-holder-label");
            return label;
        }
    }
}

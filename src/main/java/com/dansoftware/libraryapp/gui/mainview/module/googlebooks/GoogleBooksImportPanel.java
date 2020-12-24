package com.dansoftware.libraryapp.gui.mainview.module.googlebooks;

import com.dansoftware.libraryapp.db.Database;
import com.dansoftware.libraryapp.googlebooks.GoogleBooksQuery;
import com.dansoftware.libraryapp.googlebooks.GoogleBooksQueryBuilder;
import com.dansoftware.libraryapp.googlebooks.Volume;
import com.dansoftware.libraryapp.googlebooks.Volumes;
import com.dansoftware.libraryapp.gui.context.Context;
import com.dansoftware.libraryapp.locale.I18N;
import com.dansoftware.libraryapp.util.ExploitativeExecutor;
import javafx.concurrent.Task;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class GoogleBooksImportPanel extends VBox {

    private static final Logger logger = LoggerFactory.getLogger(GoogleBooksImportPanel.class);

    private final Database database;

    private final Context context;
    private final GoogleBooksImportForm form;
    private final GoogleBooksSearchResultTable table;

    GoogleBooksImportPanel(@NotNull Context context, @NotNull Database database) {
        this.context = context;
        this.database = database;
        this.form = createForm(context);
        this.table = buildTable();
        this.buildUI();
    }

    private GoogleBooksImportForm createForm(Context context) {
        return new GoogleBooksImportForm(context, buildOnSearchAction());
    }

    private Consumer<GoogleBooksImportForm.SearchData> buildOnSearchAction() {
       return searchData -> {
            if (searchData.isValid())
                ExploitativeExecutor.INSTANCE.submit(buildSearchTask(searchData));
            else ;
            //TODO: DIALOG ABOUT NOT VALID FORM
        };
    }

    private SearchTask buildSearchTask(GoogleBooksImportForm.SearchData searchData) {
        var searchTask = new SearchTask(searchData);
        searchTask.setOnRunning(e -> context.showIndeterminateProgress());
        searchTask.setOnFailed(e -> {
            context.stopProgress();
            Throwable exception = e.getSource().getException();
            logger.error("Search problem ", exception);
            context.showErrorDialog(
                    I18N.getGoogleBooksImportValue("google.books.search.failed.title"),
                    I18N.getGoogleBooksImportValue("google.books.search.failed.msg"),
                    (Exception) exception,
                    buttonType -> {});
        });
        searchTask.setOnSucceeded(e -> {
            context.stopProgress();
            form.setExpanded(false);
            Volumes volumes = searchTask.getValue();
            List<Volume> items = volumes.getItems();
            table.getItems().clear();
            if (items != null && !items.isEmpty())
                table.getItems().setAll(items.stream().map(Volume::getVolumeInfo).collect(Collectors.toList()));
        });
        return searchTask;
    }

    private GoogleBooksSearchResultTable buildTable() {
        var table = new GoogleBooksSearchResultTable(context, 0);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    private void buildUI() {
        getChildren().add(form);
        getChildren().add(table);
    }

    private static final class SearchTask extends Task<Volumes> {

        private final GoogleBooksImportForm.SearchData searchData;

        SearchTask(@NotNull GoogleBooksImportForm.SearchData searchData) {
            this.searchData = searchData;
        }

        @Override
        protected Volumes call() throws Exception {
            GoogleBooksQuery query = new GoogleBooksQueryBuilder()
                    .inText(searchData.getGeneralText())
                    .inAuthor(searchData.getAuthor())
                    .inTitle(searchData.getTitle())
                    .inPublisher(searchData.getPublisher())
                    .isbn(searchData.getIsbn())
                    .language(searchData.getLanguage())
                    .printType(searchData.getFilter().getType())
                    .sortType(searchData.getSort().getType())
                    .maxResults(searchData.getMaxResults())
                    .build();
            return query.load();
        }
    }
}



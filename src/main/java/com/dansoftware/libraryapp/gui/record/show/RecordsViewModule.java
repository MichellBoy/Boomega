package com.dansoftware.libraryapp.gui.record.show;

import com.dansoftware.libraryapp.appdata.Preferences;
import com.dansoftware.libraryapp.db.Database;
import com.dansoftware.libraryapp.db.data.Record;
import com.dansoftware.libraryapp.gui.context.Context;
import com.dansoftware.libraryapp.gui.context.NotifiableModule;
import com.dansoftware.libraryapp.i18n.ABCCollators;
import com.dansoftware.libraryapp.i18n.I18N;
import com.dansoftware.libraryapp.util.ExploitativeExecutor;
import com.dlsc.workbenchfx.model.WorkbenchModule;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecordsViewModule extends WorkbenchModule
        implements NotifiableModule<RecordsViewModule.Message> {

    public static final class Message {
        private final Action action;
        private final Record record;

        public Message(Record record, @NotNull Action action) {
            this.record = record;
            this.action = action;
        }

        public enum Action {
            DELETED, INSERTED, UPDATED
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RecordsViewModule.class);

    private final Preferences.Key<Integer> itemsPerPageConfigKey =
            new Preferences.Key<>("books.view.items.per.page", Integer.class, () -> 10);

    private final Preferences.Key<TableColumnsInfo> colConfigKey =
            new Preferences.Key<>("books.view.table.columns", TableColumnsInfo.class, TableColumnsInfo::byDefault);

    private final Preferences.Key<Locale> abcConfigKey =
            new Preferences.Key<>(
                    "google.books.module.table.abcsort",
                    Locale.class,
                    Locale::getDefault
            );

    private final Context context;
    private final Preferences preferences;
    private final Database database;

    private final ObjectProperty<RecordsView> content =
            new SimpleObjectProperty<>();

    private final IntegerProperty itemsPerPage =
            new SimpleIntegerProperty();

    private final IntegerProperty totalItems =
            new SimpleIntegerProperty();

    private final ObjectProperty<Locale> abcLocale =
            new SimpleObjectProperty<>();

    private ToolbarItem columnChooserItem;

    public RecordsViewModule(@NotNull Context context,
                             @NotNull Preferences preferences,
                             @NotNull Database database) {
        super(I18N.getRecordsViewValue("record.book.view.module.name"), MaterialDesignIcon.LIBRARY);
        this.context = context;
        this.preferences = preferences;
        this.database = database;
        this.readConfig();
        this.countTotalItems();
        this.buildToolbar();
    }

    @Override
    public Node activate() {
        if (content.get() == null)
            content.set(buildContent());
        return content.get();
    }

    @Override
    public boolean destroy() {
        writeConfig();
        content.set(null);
        return true;
    }

    @Override
    public void commitData(Message data) {
        if (content.get() != null) {
            switch (data.action) {
                case DELETED:
                    getTable().getItems().remove(data.record);
                    break;
                case INSERTED:
                    if (itemsPerPage.get() > getTable().getItems().size()) {
                        getTable().getItems().add(data.record);
                        totalItems.set(totalItems.get() + 1);
                    }
                    break;
                case UPDATED:
                    break;
            }
            getTable().refresh();
        }
    }

    private void countTotalItems() {
        ExploitativeExecutor.INSTANCE.submit(() -> totalItems.set(database.getTotalRecordCount()));
    }

    private void readConfig() {
        itemsPerPage.set(preferences.get(itemsPerPageConfigKey));
        abcLocale.set(preferences.get(abcConfigKey));
    }

    private void readColumnConfigurations(RecordTable table) {
        List<RecordTable.ColumnType> columnTypes =
                preferences.get(colConfigKey).columnTypes;
        columnTypes.forEach(table::addColumn);
        columnChooserItem.getItems().stream()
                .map(menuItem -> (TableColumnMenuItem) menuItem)
                .forEach(menuItem -> menuItem.setSelected(table.isColumnShown(menuItem.columnType)));
    }

    private void writeConfig() {
        preferences.editor()
                .put(itemsPerPageConfigKey, itemsPerPage.get())
                .put(colConfigKey, new TableColumnsInfo(getTable().getShowingColumns()))
                .put(abcConfigKey, abcLocale.get());
    }

    private RecordsView buildContent() {
        RecordsView recordsView = new RecordsView(context, database);
        loadBooks(recordsView);
        readColumnConfigurations(recordsView.getBooksTable());
        return recordsView;
    }

    private void buildToolbar() {
        this.getToolbarControlsRight().add(buildItemsPerPageItem());
        this.getToolbarControlsRight().add(buildSeparator());
        this.getToolbarControlsRight().add(buildCountItem());
        this.getToolbarControlsRight().add(buildSeparator());
        this.getToolbarControlsRight().add(buildRefreshItem());
        this.getToolbarControlsRight().add(buildScrollToTopItem());

        this.getToolbarControlsLeft().add(columnChooserItem = buildColumnChooserItem());
        this.getToolbarControlsLeft().add(buildColumnResetItem());
        this.getToolbarControlsLeft().add(buildABCChooserItem());
    }

    private ToolbarItem buildCountItem() {
        var toolbarItem = new ToolbarItem();
        toolbarItem.textProperty().bind(
                new SimpleStringProperty(I18N.getRecordsViewValue("record.book.count"))
                        .concat(StringUtils.SPACE)
                        .concat(totalItems)
        );
        return toolbarItem;
    }

    private ToolbarItem buildItemsPerPageItem() {
        Spinner<Integer> spinner = new Spinner<>(1, Integer.MAX_VALUE, itemsPerPage.get());
        this.itemsPerPage.bind(spinner.valueProperty());
        this.buildItemsPerPagePolicy();
        return new ToolbarItem(
                new StackPane(
                        new Group(
                                new HBox(2.5,
                                        new StackPane(
                                                new Label(I18N.getRecordsViewValue("record.item.per.page"))
                                        ),
                                        spinner
                                )
                        )
                )
        );
    }

    private void buildItemsPerPagePolicy() {
        this.itemsPerPage.addListener((observable, oldValue, newValue) -> {
            if (content.get() != null) {
                int oldValueInt = oldValue.intValue();
                int newValueInt = newValue.intValue();
                logger.debug("Old value: {}, new value: {}", oldValueInt, newValueInt);
                if (oldValueInt < newValueInt) {
                    ExploitativeExecutor.INSTANCE.submit(
                            new TableRecordsGetTask(context, getTable(), database, oldValueInt, newValueInt - oldValueInt)
                    );
                } else {
                    try {
                        getTable().getItems().remove(newValueInt, oldValueInt);
                        getTable().refresh();
                    } catch (IndexOutOfBoundsException e) {
                        logger.error("{}", e.getClass());
                    }
                }
            }
        });
    }

    private ToolbarItem buildRefreshItem() {
        return buildToolbarItem(MaterialDesignIcon.REFRESH, "record.toolbar.refresh", event -> refresh());
    }

    private ToolbarItem buildScrollToTopItem() {
        return buildToolbarItem(MaterialDesignIcon.BORDER_TOP, "record.table.scrolltop", event -> getContent().scrollToTop());
    }

    private ToolbarItem buildColumnChooserItem() {
        var toolbarItem = new ToolbarItem(
                I18N.getRecordsViewValue("record.table.preferred.columns"),
                new FontAwesomeIconView(FontAwesomeIcon.COLUMNS));
        Stream.of(RecordTable.ColumnType.values())
                .map(TableColumnMenuItem::new)
                .forEach(toolbarItem.getItems()::add);
        return toolbarItem;
    }

    private ToolbarItem buildColumnResetItem() {
        return buildToolbarItem(MaterialDesignIcon.TABLE, "record.table.colreset", event -> {
            getTable().buildDefaultColumns();
            columnChooserItem.getItems().stream()
                    .map(item -> (TableColumnMenuItem) item)
                    .forEach(item -> item.setSelected(item.columnType.isDefaultVisible()));
        });
    }

    private ToolbarItem buildABCChooserItem() {
        var toolbarItem = new ToolbarItem(I18N.getRecordsViewValue("record.table.abc"));
        var toggleGroup = new ToggleGroup();
        ABCCollators.getAvailableCollators().forEach((locale, collatorSupplier) -> {
            toolbarItem.getItems().add(new AbcMenuItem(locale, collatorSupplier, toggleGroup));
        });
        return toolbarItem;
    }

    private ToolbarItem buildSeparator() {
        return new ToolbarItem(new Separator(Orientation.VERTICAL));
    }

    private ToolbarItem buildToolbarItem(MaterialDesignIcon icon, String i18nTooltip, EventHandler<MouseEvent> onClick) {
        var toolbarItem = new ToolbarItem(new MaterialDesignIconView(icon), onClick);
        toolbarItem.setTooltip(new Tooltip(I18N.getRecordsViewValue(i18nTooltip)));
        return toolbarItem;
    }

    private RecordTable getTable() {
        return content.get().getBooksTable();
    }

    private RecordsView getContent() {
        return content.get();
    }

    private void refresh() {
        this.loadBooks(getContent());
    }

    private void loadBooks(RecordsView content) {
        ExploitativeExecutor.INSTANCE.submit(
                new TableRecordsGetTask(
                        context,
                        content.getBooksTable(),
                        database,
                        0,
                        itemsPerPage.get()
                )
        );
    }

    @SuppressWarnings("DuplicatedCode")
    private final class TableColumnMenuItem extends CheckMenuItem {

        private final RecordTable.ColumnType columnType;

        TableColumnMenuItem(RecordTable.ColumnType columnType) {
            super(I18N.getRecordsViewValue(columnType.getI18Nkey()));
            this.columnType = columnType;
            this.setOnAction(e -> {
                if (!this.isSelected()) {
                    getTable().removeColumn(columnType);
                } else {
                    getTable().removeAllColumns();
                    columnChooserItem.getItems().stream()
                            .map(menuItem -> (TableColumnMenuItem) menuItem)
                            .filter(CheckMenuItem::isSelected)
                            .map(menuItem -> menuItem.columnType)
                            .forEach(colType -> getTable().addColumn(colType));
                }
            });
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final class AbcMenuItem extends RadioMenuItem {
        private final Locale locale;

        AbcMenuItem(@NotNull Locale locale,
                    @NotNull Supplier<Collator> collatorSupplier,
                    @NotNull ToggleGroup toggleGroup) {
            super(locale.getDisplayLanguage(), new MaterialDesignIconView(MaterialDesignIcon.TRANSLATE));
            this.locale = locale;
            this.setUserData(locale);
            this.setSelected(locale.equals(abcLocale.get()));
            selectedProperty().addListener((observable, oldValue, selected) -> {
                if (selected) {
                    getTable().setSortingComparator((Comparator) collatorSupplier.get());
                    RecordsViewModule.this.abcLocale.set(locale);
                }
            });
            setToggleGroup(toggleGroup);
        }
    }

    /**
     * Used for storing the preferred table columns in the configurations.
     */
    static class TableColumnsInfo {

        private final List<RecordTable.ColumnType> columnTypes;

        TableColumnsInfo(@NotNull List<RecordTable.ColumnType> columnTypes) {
            this.columnTypes = Objects.requireNonNull(columnTypes);
        }

        public static TableColumnsInfo byDefault() {
            return new TableColumnsInfo(
                    Arrays.stream(RecordTable.ColumnType.values())
                            .filter(RecordTable.ColumnType::isDefaultVisible)
                            .collect(Collectors.toList())
            );
        }
    }
}
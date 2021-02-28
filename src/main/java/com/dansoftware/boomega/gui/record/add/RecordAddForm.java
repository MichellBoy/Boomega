package com.dansoftware.boomega.gui.record.add;

import com.dansoftware.boomega.db.data.Record;
import com.dansoftware.boomega.db.data.ServiceConnection;
import com.dansoftware.boomega.googlebooks.GoogleBooksQueryBuilder;
import com.dansoftware.boomega.googlebooks.Volume;
import com.dansoftware.boomega.gui.context.Context;
import com.dansoftware.boomega.gui.googlebooks.SearchParameters;
import com.dansoftware.boomega.gui.googlebooks.join.GoogleBookJoinerOverlay;
import com.dansoftware.boomega.gui.googlebooks.tile.GoogleBookTile;
import com.dansoftware.boomega.gui.record.RecordValues;
import com.dansoftware.boomega.gui.util.LanguageSelections;
import com.dansoftware.boomega.i18n.I18N;
import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.util.BindingMode;
import com.dlsc.formsfx.model.util.ResourceBundleService;
import com.dlsc.formsfx.view.controls.SimpleTextControl;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.dlsc.formsfx.view.util.ColSpan;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.*;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Rating;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * A {@link RecordAddForm} provides a UI for typing Book/Magazine data.
 *
 * <p>
 * It has a {@link Record.Type} property that represents what type of database record
 * typed.
 *
 * <p>
 * The {@link #setOnRecordAdded(Consumer)} method can be used for listening to commits
 *
 * @author Daniel Gyorffy
 */
public class RecordAddForm extends VBox {

    private static final Logger logger = LoggerFactory.getLogger(RecordAddForm.class);

    private static final String STYLE_CLASS = "record-add-form";

    private final ObjectProperty<Record.Type> recordType = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            handleTypeChange(get());
        }
    };

    private final ObjectProperty<Consumer<Record>> onRecordAdded =
            new SimpleObjectProperty<>();

    private final ObjectProperty<Form> currentForm =
            new SimpleObjectProperty<>();

    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty subtitle = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> publishedDate = new SimpleObjectProperty<>();
    private final StringProperty publisher = new SimpleStringProperty("");
    private final StringProperty magazineName = new SimpleStringProperty("");
    private final StringProperty authors = new SimpleStringProperty("");
    private final StringProperty language = new SimpleStringProperty("");
    private final StringProperty isbn = new SimpleStringProperty("");
    private final StringProperty subject = new SimpleStringProperty("");
    private final IntegerProperty numberOfCopies = new SimpleIntegerProperty(1);
    private final IntegerProperty rating = new SimpleIntegerProperty(0);
    private final StringProperty googleBookHandle = new SimpleStringProperty();

    private final VBox contentVBox;
    private final Context context;
    private final FormNotesEditor notesEditor;

    private VBox googleBookTileBox;

    public RecordAddForm(@NotNull Context context, @NotNull Record.Type initialType) {
        this.context = context;
        this.contentVBox = new VBox();
        this.notesEditor = buildNotesControl();
        this.recordType.set(initialType);
        this.getStyleClass().add(STYLE_CLASS);
        this.buildBaseUI();
    }

    private void buildBaseUI() {
        this.getChildren().add(buildScrollPane());
        this.getChildren().add(buildCommitButton());
    }

    private ScrollPane buildScrollPane() {
        var scrollPane = new ScrollPane();
        scrollPane.setContent(contentVBox);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    private Node buildCommitButton() {
        var button = new Button(
                I18N.getValue("record.add.form.commit"),
                new MaterialDesignIconView(MaterialDesignIcon.CONTENT_SAVE));
        button.getStyleClass().add("record-add-button");
        button.setDefaultButton(true);
        button.disableProperty().bind(currentForm.get().validProperty().not());
        //setting the mouse event handler instead of action event handler because of "html editor->enter bug"
        button.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                commitRecord();
            }
        });
        button.prefWidthProperty().bind(this.widthProperty());
        button.addEventFilter(KeyEvent.KEY_TYPED, Event::consume);
        return button;
    }

    private void handleTypeChange(@NotNull Record.Type recordType) {
        Form form = null;
        if (recordType == Record.Type.BOOK)
            form = buildBookForm();
        else if (recordType == Record.Type.MAGAZINE)
            form = buildMagazineForm();
        currentForm.set(form);
        buildFormUI();
    }

    private void buildFormUI() {
        contentVBox.getChildren().setAll(
                renderForm(),
                buildNewRatingControl(),
                buildGoogleBookJoiner(),
                notesEditor
        );
    }

    private Node renderForm() {
        var formRenderer = new FormRenderer(currentForm.get());
        addAutoCompletionToLangField(formRenderer);
        return formRenderer;
    }

    public void setValues(@Nullable RecordValues values) {
        if (values == null) {
            clearForm();
        } else {
            this.recordType.set(values.getRecordType());
            this.title.setValue(values.getTitle());
            this.subtitle.setValue(values.getSubtitle());
            this.publisher.setValue(values.getPublisher());
            this.magazineName.setValue(values.getMagazineName());
            this.authors.setValue(values.getAuthors());
            this.language.setValue(values.getLanguage());
            this.isbn.setValue(values.getIsbn());
            this.subject.setValue(values.getSubject());
            this.notesEditor.setHtmlText(values.getNotes());
            this.rating.setValue(values.getRating());
            this.removeGoogleBookConnection();
            this.createGoogleBookConnection(values.getVolumeObject());
            this.publishedDate.setValue(values.getPublishedDate());
        }
    }

    private void addAutoCompletionToLangField(FormRenderer src) {
        SimpleTextControl control = (SimpleTextControl) src.lookup(".languageSelector");
        TextField textField = (TextField) control.lookup(".text-field");
        LanguageSelections.applyOnTextField(context, textField);
    }

    private Node buildNewRatingControl() {
        var rating = new Rating(5);
        StackPane.setAlignment(rating, Pos.CENTER_LEFT);
        rating.setRating(this.rating.get());
        rating.ratingProperty().addListener((o, old, newRating) -> this.rating.set(newRating.intValue()));
        var hBox = new HBox(
                5,
                new StackPane(new Label(I18N.getValue("record.add.form.rating"))),
                new StackPane(rating) {{
                    HBox.setHgrow(this, Priority.ALWAYS);
                }}
        );
        VBox.setMargin(hBox, new Insets(0, 0, 0, 40));
        return hBox;
    }

    private Node buildGoogleBookJoiner() {
        var vBox = new VBox(5);
        vBox.getChildren().add(buildGoogleBookButton(vBox));
        this.googleBookTileBox = vBox;
        return vBox;
    }

    private FormNotesEditor buildNotesControl() {
        var notesEditor = new FormNotesEditor();
        notesEditor.setPrefHeight(200);
        VBox.setMargin(notesEditor, new Insets(10, 40, 10, 40));
        return notesEditor;
    }

    private Button buildGoogleBookButton(VBox vBox) {
        var button = new Button(I18N.getValue("record.add.form.gjoin"),
                new MaterialDesignIconView(MaterialDesignIcon.GOOGLE));
        button.setOnAction(event ->
                context.showOverlay(new GoogleBookJoinerOverlay(
                        context, new SearchParameters()
                        .printType(recordType.get() == Record.Type.BOOK ?
                                GoogleBooksQueryBuilder.PrintType.BOOKS :
                                GoogleBooksQueryBuilder.PrintType.MAGAZINES
                        )
                        .isbn(isbn.get())
                        .authors(authors.get())
                        .publisher(publisher.get())
                        .title(title.get())
                        .language(language.get()),
                        this::createGoogleBookConnection))
        );
        button.prefWidthProperty().bind(vBox.widthProperty());
        VBox.setMargin(button, new Insets(15, 40, 10, 40));
        return button;
    }

    private void commitRecord() {
        Consumer<Record> addAction = onRecordAdded.get();
        if (addAction != null) {
            addAction.accept(buildRecordObject());
            clearForm();
        }
    }

    private void clearForm() {
        title.set("");
        subtitle.set("");
        publishedDate.set(null);
        publisher.set("");
        magazineName.set("");
        authors.set("");
        language.set("");
        isbn.set("");
        subject.set("");
        notesEditor.setHtmlText("");
        numberOfCopies.setValue(null);
        rating.setValue(null);
        googleBookHandle.set(null);
        removeGoogleBookConnection();
    }

    private GoogleBookTile createGoogleBookTile(@NotNull Volume volume) {
        final var googleBookTile = new GoogleBookTile(context, volume, closedVolume -> {
            googleBookHandle.set(null);
            googleBookTileBox.getChildren().removeIf(element -> element instanceof GoogleBookTile);
        });
        VBox.setMargin(googleBookTile, new Insets(0, 40, 0, 40));
        return googleBookTile;
    }

    private void createGoogleBookConnection(Volume volume) {
        this.googleBookTileBox.getChildren().removeIf(e -> e instanceof GoogleBookTile);
        this.googleBookHandle.setValue(volume.getSelfLink());
        this.googleBookTileBox.getChildren().add(createGoogleBookTile(volume));
    }

    private void removeGoogleBookConnection() {
        googleBookHandle.set(null);
        googleBookTileBox.getChildren().removeIf(e -> e instanceof GoogleBookTile);
    }

    private Record buildRecordObject() {
        return new Record.Builder(recordType.get())
                .title(title.get())
                .subtitle(subtitle.get())
                .publishedDate(publishedDate.get())
                .publisher(publisher.get())
                .magazineName(magazineName.get())
                .authors(List.of(authors.get().split(",")))
                .language(language.get())
                .isbn(isbn.get())
                .subject(subject.get())
                .notes(notesEditor.getHtmlText())
                .numberOfCopies(numberOfCopies.get())
                .rating(rating.get())
                .serviceConnection(new ServiceConnection(googleBookHandle.get()))
                .build();
    }

    private Form buildBookForm() {
        return Form.of(
                Group.of(
                        Field.ofStringType(authors)
                                .label("record.add.form.authors")
                                .placeholder("record.add.form.authors.prompt")
                                .required(false)
                                .span(ColSpan.HALF),
                        Field.ofStringType(title)
                                .label("record.add.form.title")
                                .placeholder("record.add.form.title.prompt")
                                .required("record.title.required")
                                .span(ColSpan.HALF),
                        Field.ofStringType(subtitle)
                                .label("record.add.form.subtitle")
                                .placeholder("record.add.form.subtitle.prompt")
                                .required(false)
                                .span(ColSpan.HALF),
                        Field.ofStringType(isbn)
                                .label("record.add.form.isbn")
                                .placeholder("record.add.form.isbn.prompt")
                                .required(false)
                                .span(ColSpan.HALF),
                        Field.ofStringType(language)
                                .styleClass("languageSelector")
                                .label("record.add.form.lang")
                                .placeholder("record.add.form.lang.prompt")
                                .required(false)
                                .span(ColSpan.HALF),
                        Field.ofStringType(publisher)
                                .label("record.add.form.publisher")
                                .placeholder("record.add.form.publisher.prompt")
                                .required(false)
                                .span(ColSpan.HALF),
                        Field.ofStringType(subject)
                                .label("record.add.form.subject")
                                .placeholder("record.add.form.subject.prompt")
                                .required(false)
                                .span(ColSpan.HALF),
                        Field.ofDate(publishedDate)
                                .label("record.add.form.date")
                                .placeholder("record.add.form.date.prompt")
                                .required(false)
                                .format("record.date.error")
                                .span(ColSpan.HALF),
                        Field.ofIntegerType(numberOfCopies)
                                .label("record.add.form.nofcopies")
                                .required(false)
                                .placeholder("record.add.form.nofcopies.prompt")
                                .span(ColSpan.HALF)

                )
        ).i18n(new ResourceBundleService(I18N.getValues()))
                .binding(BindingMode.CONTINUOUS);
    }

    private Form buildMagazineForm() {
        return Form.of(
                Group.of(
                        Field.ofStringType(magazineName)
                                .label("record.add.form.magazinename")
                                .placeholder("record.add.form.magazinename.prompt")
                                .required("record.magazinename.required")
                                .span(ColSpan.HALF),
                        Field.ofStringType(title)
                                .label("record.add.form.title")
                                .placeholder("record.add.form.title.prompt")
                                .required("record.title.required")
                                .span(ColSpan.HALF),
                        Field.ofStringType(publisher)
                                .label("record.add.form.publisher")
                                .placeholder("record.add.form.publisher.prompt")
                                .required(false)
                                .span(ColSpan.HALF),
                        Field.ofDate(publishedDate)
                                .label("record.add.form.date")
                                .placeholder("record.add.form.date.prompt")
                                .required(false)
                                .format("record.date.error")
                                .span(ColSpan.HALF),
                        Field.ofStringType(language)
                                .styleClass("languageSelector")
                                .label("record.add.form.lang")
                                .placeholder("record.add.form.lang.prompt")
                                .required(false)
                                .span(ColSpan.HALF)
                )
        ).i18n(new ResourceBundleService(I18N.getValues()))
                .binding(BindingMode.CONTINUOUS);
    }

    public Consumer<Record> getOnRecordAdded() {
        return onRecordAdded.get();
    }

    public ObjectProperty<Consumer<Record>> onRecordAddedProperty() {
        return onRecordAdded;
    }

    public void setOnRecordAdded(Consumer<Record> onRecordAdded) {
        this.onRecordAdded.set(onRecordAdded);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getSubtitle() {
        return subtitle.get();
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public LocalDate getPublishedDate() {
        return publishedDate.get();
    }

    public ObjectProperty<LocalDate> publishedDateProperty() {
        return publishedDate;
    }

    public String getPublisher() {
        return publisher.get();
    }

    public StringProperty publisherProperty() {
        return publisher;
    }

    public String getMagazineName() {
        return magazineName.get();
    }

    public StringProperty magazineNameProperty() {
        return magazineName;
    }

    public String getAuthors() {
        return authors.get();
    }

    public StringProperty authorsProperty() {
        return authors;
    }

    public String getLanguage() {
        return language.get();
    }

    public StringProperty languageProperty() {
        return language;
    }

    public String getIsbn() {
        return isbn.get();
    }

    public StringProperty isbnProperty() {
        return isbn;
    }

    public String getSubject() {
        return subject.get();
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public String getNotes() {
        return notesEditor.getHtmlText();
    }

    public int getNumberOfCopies() {
        return numberOfCopies.get();
    }

    public IntegerProperty numberOfCopiesProperty() {
        return numberOfCopies;
    }

    public int getRating() {
        return rating.get();
    }

    public IntegerProperty ratingProperty() {
        return rating;
    }

    public Record.Type getRecordType() {
        return recordType.get();
    }

    public ObjectProperty<Record.Type> recordTypeProperty() {
        return recordType;
    }

    public String getGoogleBookHandle() {
        return googleBookHandle.get();
    }

    public StringProperty googleBookHandleProperty() {
        return googleBookHandle;
    }

}
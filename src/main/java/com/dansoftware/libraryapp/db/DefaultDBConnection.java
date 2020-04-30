package com.dansoftware.libraryapp.db;

import com.dansoftware.libraryapp.db.util.*;
import com.dansoftware.libraryapp.db.util.parse.*;
import com.dansoftware.libraryapp.log.GuiLog;

import java.io.File;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.isNull;

/**
 * This class is responsible for communicating with the
 * embedded sqlite database.
 */
public final class DefaultDBConnection extends DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DefaultDBConnection.class.getName());

    private static DefaultDBConnection instance;

    private static final String CREATE_TABLES_SCRIPT = "/com/dansoftware/libraryapp/db/create_tables.sql";
    private static final String CREATE_VIEWS_SCRIPT = "/com/dansoftware/libraryapp/db/create_views.sql";

    private final static String JDBC_DRIVER = "org.sqlite.JDBC";

    private static File databaseFile;

    /**
     * Don't let anyone to create an instance of this class.
     *
     * @throws SQLException if some sql exception occurs
     */
    private DefaultDBConnection() throws SQLException {
    }


    @Override
    public DataPackage loadAllData() throws SQLException {

        DataPackage dataPackage = new DataPackage(
                new RecordCollection<>(),
                new RecordCollection<>(),
                new RecordCollection<>(),
                new RecordCollection<>()
        );

        String sql;
        ResultSetParser parser;

        sql = "SELECT * FROM books_joined;";
        parser = new JoinedTableParser();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            parser.parse(resultSet, dataPackage);
        }

        sql = "SELECT * FROM single_authors";
        parser = new AuthorTableParser();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            parser.parse(resultSet, dataPackage);
        }

        sql = "SELECT * FROM single_publishers";
        parser = new PublisherTableParser();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            parser.parse(resultSet, dataPackage);
        }

        sql = "SELECT * FROM single_subjects";
        parser = new SubjectTableParser();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            parser.parse(resultSet, dataPackage);
        }

        return dataPackage;
    }

    @Override
    protected InputStream getTableCreatorScriptStream() {
        return getClass().getResourceAsStream(CREATE_TABLES_SCRIPT);
    }

    @Override
    protected InputStream getViewCreatorScriptStream() {
        return getClass().getResourceAsStream(CREATE_VIEWS_SCRIPT);
    }

    @Override
    protected String getJDBCDriver() {
        return JDBC_DRIVER;
    }

    @Override
    protected JDBCURLGenerator getJDBCUrlMaker() {
        return new SqliteURLGenerator(databaseFile = new DataBaseFileRecognizer().getDBFile());
    }

    /**
     * This method creates an instance of this class and handles the {@link SQLException}
     *
     * @return the database connection object
     * @see DefaultDBConnection#getInstance()
     */
    private static DefaultDBConnection createDBConnectionObject() {
        try {
            return new DefaultDBConnection();
        } catch (SQLException e) {
            LOGGER.log(new GuiLog(Level.SEVERE, e, "dbconnection.failed", new Object[]{databaseFile.getName()}));
        }

        return null;
    }

    /**
     * This static method creates the DBConnection if it isn't created yet, and
     * returns it.
     *
     * @return the single instance of the DBConnection class
     */
    public static DefaultDBConnection getInstance() {
        if (isNull(instance)) instance = createDBConnectionObject();

        return instance;
    }

}

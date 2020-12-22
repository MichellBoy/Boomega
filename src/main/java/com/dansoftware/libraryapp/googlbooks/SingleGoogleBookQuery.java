package com.dansoftware.libraryapp.googlbooks;

import com.google.gson.Gson;
import javafx.concurrent.Task;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Used for loading the data of one particular Google Book.
 *
 * @author Daniel Gyorffy
 */
public class SingleGoogleBookQuery extends Task<Volume> {

    private final String url;

    SingleGoogleBookQuery(@NotNull String url) {
        this.url = url;
    }

    public Volume load() throws IOException {
        try(var input = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            return new Gson().fromJson(input, Volume.class);
        }
    }

    @Override
    protected Volume call() throws Exception {
        return load();
    }
}

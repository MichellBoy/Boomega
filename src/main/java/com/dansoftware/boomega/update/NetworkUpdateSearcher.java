package com.dansoftware.boomega.update;

import com.dansoftware.boomega.util.adapter.VersionInteger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

/**
 * An {@link UpdateSearcher} that is used for actually searching for updates on the internet.
 *
 * @author Daniel Gyorffy
 */
class NetworkUpdateSearcher extends UpdateSearcher {

    /**
     * The updater server-address
     */
    private static final String LOCATION = "https://update-server-ed6c3.firebaseio.com/libraryapp.json";

    private final VersionInteger base;

    /**
     * Creates a basic update searcher object;
     *
     * @param base the base version that the object should compare to; mustn't be null
     * @throws NullPointerException if base is null.
     */
    public NetworkUpdateSearcher(@NotNull VersionInteger base) {
        this.base = Objects.requireNonNull(base, "The base mustn't be null");
    }

    @Override
    public @NotNull UpdateSearchResult search() {
        UpdateSearchResult result = new UpdateSearchResult();
        try {
            var information = loadInfo();
            if (information != null) {
                VersionInteger newVersion = new VersionInteger(information.getVersion());
                if (base.isOlderThan(newVersion)) {
                    result.setNewUpdate(true);
                    result.setInformation(information);
                }
            }
        } catch (IOException | RuntimeException e) {
            result.setFailed(true);
            result.setFailedCause(e);
        }

        return result;
    }

    @Override
    protected @Nullable UpdateInformation loadInfo() throws IOException {
        URL url = new URL(LOCATION);
        URLConnection connection = url.openConnection();
        connection.connect();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(UpdateInformation.class, new UpdateInformationDeserializer())
                .create();

        try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return gson.fromJson(reader, UpdateInformation.class);
        }
    }
}
package com.dansoftware.libraryapp.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

/**
 * A ValueConstructingProcess is used by {@link com.dansoftware.libraryapp.config.AppConfig.Key} objects
 * to define how to construct the particular json-element into a java object.
 *
 * @param <T> defines what is the type of the value that we want to construct
 */
interface ValueConstructingProcess<T> {

    /**
     * Creates a {@link ValueConstructingProcess} that can construct a {@link JsonElement} into
     * a particular java object using {@link Gson}.
     *
     * @param type the class-reference of the object that we want to construct
     * @return the {@link ValueConstructingProcess} object
     */
    static <T> ValueConstructingProcess<T> defaultProcess(@NotNull Class<T> type) {
        return element -> new Gson().fromJson(element.toString(), type);
    }

    T construct(@NotNull JsonElement element);
}

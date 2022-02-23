/*
 * Boomega
 * Copyright (C)  2022  Daniel Gyoerffy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dansoftware.boomega.main

import com.dansoftware.boomega.config.source.ConfigSource
import com.dansoftware.boomega.config.source.JsonFileSource
import com.dansoftware.boomega.plugin.RealtimePluginService
import com.dansoftware.boomega.plugin.api.PluginService
import com.dansoftware.boomega.update.GithubReleasesFetcher
import com.dansoftware.boomega.update.ReleasesFetcher
import com.dansoftware.boomega.util.joinToFilePath
import com.dansoftware.boomega.util.userDirectoryPath
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.name.Names
import com.google.inject.util.Modules
import java.io.File
import javax.inject.Named

class RealtimeAppModule : Module by Modules.combine(
    PreferencesModule(),
    UpdateModule(),
    PluginModule()
)

class PreferencesModule : AbstractModule() {
    override fun configure() {
        bind(ConfigSource::class.java).to(JsonFileSource::class.java)
    }

    @Provides
    @Named("configFilePath")
    fun provideConfigFilePath() = joinToFilePath(userDirectoryPath, ".libraryapp2020", "bmcfg")
}

class UpdateModule : AbstractModule() {
    override fun configure() {
        bind(ReleasesFetcher::class.java).to(GithubReleasesFetcher::class.java)
        bind(String::class.java)
            .annotatedWith(Names.named("appVersion"))
            .toInstance(System.getProperty("boomega.version"))
        bind(String::class.java)
            .annotatedWith(Names.named("GithubRepositoryOwner"))
            .toInstance("Dansoftowner")
        bind(String::class.java)
            .annotatedWith(Names.named("GithubRepositoryName"))
            .toInstance("Boomega")
    }
}

class PluginModule : AbstractModule() {
    override fun configure() {
        bind(PluginService::class.java).to(RealtimePluginService::class.java)
    }

    @Provides
    @Named("jarDirectory")
    fun providePluginDirectory() = File(System.getProperty("boomega.plugin.dir"))
}
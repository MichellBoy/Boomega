package com.dansoftware.libraryapp.gui.util

import com.dansoftware.libraryapp.locale.I18N
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import java.io.BufferedInputStream

/**
 * A [LibraryAppStage] is a [Stage] implementation that
 * supports internationalized titles and automatically adds the libraryapp icon-bundle.
 *
 * @author Daniel Gyorffy
 */
abstract class LibraryAppStage constructor() : Stage() {

    /**
     * For defining the resource-locations for window-icons
     */
    companion object {
        /**
         * The 16px libraryapp icon's path.
         * The icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [ www.flaticon.com](https://www.flaticon.com/)
         *
         *
         * [Go to website](https://www.flaticon.com/free-icon/bookshelf_3100669?term=library&page=1&position=12)
         */
        private const val LOGO_16 = "/com/dansoftware/libraryapp/image/logo/bookshelf_16.png"

        /**
         * The 32px libraryapp icon's path.
         * The icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [ www.flaticon.com](https://www.flaticon.com/)
         *
         *
         * [Go to website](https://www.flaticon.com/free-icon/bookshelf_3100669?term=library&page=1&position=12)
         */
        private const val LOGO_32 = "/com/dansoftware/libraryapp/image/logo/bookshelf_32.png"

        /**
         * The 128px libraryapp icon's path.
         * The icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [ www.flaticon.com](https://www.flaticon.com/)
         *
         *
         * [Go to website](https://www.flaticon.com/free-icon/bookshelf_3100669?term=library&page=1&position=12)
         */
        private const val LOGO_128 = "/com/dansoftware/libraryapp/image/logo/bookshelf_128.png"

        /**
         * The 256px libraryapp icon's path.
         * The icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [ www.flaticon.com](https://www.flaticon.com/)
         *
         *
         * [Go to website](https://www.flaticon.com/free-icon/bookshelf_3100669?term=library&page=1&position=12)
         */
        private const val LOGO_256 = "/com/dansoftware/libraryapp/image/logo/bookshelf_256.png"

        /**
         * The 512px libraryapp icon's path.
         * The icon made by [Freepik](https://www.flaticon.com/authors/freepik) from [ www.flaticon.com](https://www.flaticon.com/)
         *
         *
         * [Go to website](https://www.flaticon.com/free-icon/bookshelf_3100669?term=library&page=1&position=12)
         */
        private const val LOGO_512 = "/com/dansoftware/libraryapp/image/logo/bookshelf_512.png"
    }

    init {
        this.icons.addAll(
                loadImage(LOGO_16),
                loadImage(LOGO_32),
                loadImage(LOGO_128),
                loadImage(LOGO_256),
                loadImage(LOGO_512)
        )
    }

    private fun loadImage(url: String): Image {
        BufferedInputStream(this.javaClass.getResourceAsStream(url)).use {
            return Image(it)
        }
    }

    protected constructor(i18n: String) : this() {
        title = I18N.getWindowTitles().getString(i18n)
    }

    protected constructor(i18n: String, content: Parent) : this(i18n) {
        scene = Scene(content)
    }
}
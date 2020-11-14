package com.dansoftware.libraryapp.locale

import java.util.*

/**
 * A [HungarianLanguagePack] is a [LanguagePack] that provides translation for the Hungarian language.
 *
 * @author Daniel Gyorffy
 */
@Suppress("unused")
class HungarianLanguagePack : InternalLanguagePack(Locale("hu"))

/**
 * An [EnglishLanguagePack] is a [LanguagePack] that provides english translation
 *
 * @author Daniel Gyorffy
 */
class EnglishLanguagePack : InternalLanguagePack(Locale.ENGLISH)

/**
 * An [InternalLanguagePack] is an abstract [LanguagePack] implementation
 * that represents a Language pack that is nested into the application by default.
 *
 * @author Daniel Gyorffy
 */
sealed class InternalLanguagePack(locale: Locale) : LanguagePack(locale) {

    companion object {
        private const val WINDOW_TITLES = "com.dansoftware.libraryapp.locale.WindowTitles"
        private const val FIRST_TIME_DIALOG = "com.dansoftware.libraryapp.locale.FirstTimeDialog"
        private const val UPDATE_DIALOG = "com.dansoftware.libraryapp.locale.UpdateDialog"
        private const val PROGRESS_MESSAGES = "com.dansoftware.libraryapp.locale.ProgressMessages"
        private const val FXML_VALUES = "com.dansoftware.libraryapp.locale.FXMLValues"
        private const val GENERAL_WORDS = "com.dansoftware.libraryapp.locale.GeneralWords"
        private const val ALERT_MESSAGES = "com.dansoftware.libraryapp.locale.AlertMessages"
        private const val BUTTON_TYPES = "com.dansoftware.libraryapp.locale.ButtonTypes"
    }

    override fun getTranslator(): LanguageTranslator? {
        return LanguageTranslator("Dániel", "Györffy", "dansoftwareowner@gmail.com")
    }

    override fun getButtonTypeValues(): ResourceBundle {
        return getBundle(BUTTON_TYPES)
    }

    public override fun getWindowTitles(): ResourceBundle {
        return getBundle(WINDOW_TITLES)
    }

    public override fun getFirstTimeDialogValues(): ResourceBundle {
        return getBundle(FIRST_TIME_DIALOG)
    }

    public override fun getProgressMessages(): ResourceBundle {
        return getBundle(PROGRESS_MESSAGES)
    }

    public override fun getFXMLValues(): ResourceBundle {
        return getBundle(FXML_VALUES)
    }

    public override fun getGeneralWords(): ResourceBundle {
        return getBundle(GENERAL_WORDS)
    }

    public override fun getAlertMessages(): ResourceBundle {
        return getBundle(ALERT_MESSAGES)
    }

    public override fun getUpdateDialogValues(): ResourceBundle {
        return getBundle(UPDATE_DIALOG)
    }
}
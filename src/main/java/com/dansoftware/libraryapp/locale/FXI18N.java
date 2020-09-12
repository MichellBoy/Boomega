package com.dansoftware.libraryapp.locale;

import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Used for internationalizing some embedded javaFX elements if the default {@link Locale} is not supported by
 * the javaFX platform (for example, the default {@link ButtonType}s only supports 10 locale).
 *
 * <p>
 * It uses (even illegal) reflective access operations for get the job done.
 *
 * @author Daniel Gyorffy
 */
final class FXI18N {

    private static final Logger logger = LoggerFactory.getLogger(FXI18N.class);

    /**
     * The path for custom ResourceBundle for internationalizing javaFX
     */
    private static final String BUNDLE_NAME = "com/sun/javafx/scene/control/skin/resources/controls";

    static {
        //list of supported languages in javaFX i18n
        List<Locale> supportedLocales = List.of(
                Locale.ENGLISH,
                Locale.GERMAN,
                Locale.FRENCH,
                Locale.ITALIAN,
                Locale.JAPANESE,
                Locale.KOREAN,
                Locale.CHINESE,
                Locale.SIMPLIFIED_CHINESE,
                new Locale("sv"),
                new Locale("pt", "BR"),
                new Locale("es")
        );

        Locale locale = Locale.getDefault();
        if (supportedLocales.contains(locale)) {
            logger.debug("Default locale is supported by javaFX i18n: '{}'", locale);
            logger.debug("No need for internationalizing ButtonTypes");
        } else {
            logger.debug("Default locale is not available in javaFX i18n: '{}'", locale);
            logger.debug("Trying to internationalize ButtonTypes...");
            internationalizeButtonTypes();
        }
    }

    /**
     * Internationalizes the default {@link ButtonType}s through reflection.
     */
    private static void internationalizeButtonTypes() {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);

            ButtonType[] buttonTypes = {
                    ButtonType.APPLY,
                    ButtonType.OK,
                    ButtonType.CANCEL,
                    ButtonType.CLOSE,
                    ButtonType.YES,
                    ButtonType.NO,
                    ButtonType.FINISH,
                    ButtonType.NEXT,
                    ButtonType.PREVIOUS
            };

            Class<ButtonType> buttonTypeClass = ButtonType.class;

            Field keyField = buttonTypeClass.getDeclaredField("key");
            Field textField = buttonTypeClass.getDeclaredField("text");

            keyField.setAccessible(true);
            textField.setAccessible(true);

            FieldUtils.removeFinalModifier(keyField);
            FieldUtils.removeFinalModifier(textField);

            for (ButtonType buttonType : buttonTypes) {
                String key = (String) keyField.get(buttonType);
                String i18nVal = bundle.getString(key);
                textField.set(buttonType, i18nVal);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Some error occurred during internationalizing the ButtonTypes");
        }

    }

    private FXI18N() {
    }
}

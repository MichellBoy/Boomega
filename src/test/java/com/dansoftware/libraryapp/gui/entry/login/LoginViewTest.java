package com.dansoftware.libraryapp.gui.entry.login;

import com.dansoftware.libraryapp.db.Account;
import com.dansoftware.libraryapp.gui.theme.Theme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.util.Locale;

public class LoginViewTest extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale.setDefault(Locale.ENGLISH);

        LoginView loginView = new LoginView(new Account(
                new File("C:\\Users\\judal\\Documents\\test.db"), "Username", "MyPáswórd"
        ));

        Theme.DARK.apply(loginView);

        primaryStage.setScene(new Scene(loginView));
        primaryStage.show();
    }
}

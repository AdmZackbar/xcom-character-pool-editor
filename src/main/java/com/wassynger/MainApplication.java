package com.wassynger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApplication extends Application
{
   private MainController controller;

   public static void main(String[] args)
   {
      launch(args);
   }

   @Override
   public void start(Stage primaryStage)
   {
      Config.INSTANCE.load();
      controller = new MainController();
      primaryStage.setTitle("XCOM Character Pool Editor");
      primaryStage.setScene(new Scene(controller.getView()));
      primaryStage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> handleCloseRequest());
      primaryStage.show();
   }

   private void handleCloseRequest()
   {
      // Will eventually want to check if we have unsaved work or some other
      // reason to deny a shutdown
      Config.INSTANCE.save();
      controller.shutdown();
   }
}

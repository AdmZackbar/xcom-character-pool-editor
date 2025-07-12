package com.wassynger.xcom.pooleditor.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;

public class FxUtilities
{
   public static void load(String path, Object controller)
   {
      FXMLLoader loader = new FXMLLoader(FxUtilities.class.getClassLoader().getResource(path));
      loader.setController(controller);
      loader.setRoot(controller);
      try
      {
         loader.load();
      }
      catch (IOException e)
      {
         throw new IllegalStateException(String.format("Failed to load %s", path), e);
      }
   }

   public static void showError(String title, String message, Throwable throwable)
   {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(message);
      alert.setContentText(throwable.getLocalizedMessage());
      alert.show();
   }

   private FxUtilities()
   {
      // Disallow instantiation
   }
}

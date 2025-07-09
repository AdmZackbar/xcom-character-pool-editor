package com.wassynger;

import java.io.File;
import java.io.IOException;

import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

public class MainController
{
   private final MainView view;

   public MainController()
   {
      this.view = new MainView();
      view.addEventHandler(MainView.ON_QUIT, event -> onQuit());
      view.addEventHandler(MainView.ON_LOAD, event -> onLoad());
   }

   private void onQuit()
   {
      view.fireEvent(new WindowEvent(view.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
   }

   private void onLoad()
   {
      FileChooser fc = new FileChooser();
      fc.setTitle("Select Character Pool");
      fc.getExtensionFilters()
            .addAll(new FileChooser.ExtensionFilter("Binary file", "*.bin"),
                  new FileChooser.ExtensionFilter("Any", "*"));
      File file = fc.showOpenDialog(view.getScene().getWindow());
      if (file != null)
      {
         onLoad(file);
      }
   }

   private void onLoad(File file)
   {
      try
      {
         view.setCharacterPool(Parser.load(file.toPath()));
      }
      catch (IOException e)
      {
         FxUtilities.showError("File Load Error", "Failed to load character pool", e);
         view.setCharacterPool(null);
      }
   }

   public MainView getView()
   {
      return view;
   }
}

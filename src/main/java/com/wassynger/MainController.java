package com.wassynger;

import java.io.File;
import java.io.IOException;

import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

public class MainController
{
   private final MainView view;

   private File recentDirectory = null;

   public MainController()
   {
      this.view = new MainView();
      view.addEventHandler(MainView.ON_QUIT, event -> onQuit());
      view.addEventHandler(MainView.ON_LOAD, event -> onLoad());
      view.addEventHandler(MainView.ON_SAVE, event -> onSave());
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
      if (recentDirectory != null)
      {
         fc.setInitialDirectory(recentDirectory);
      }
      File file = fc.showOpenDialog(view.getScene().getWindow());
      if (file != null)
      {
         recentDirectory = file.getParentFile();
         loadPool(file);
      }
   }

   private void loadPool(File file)
   {
      try
      {
         view.getCharPoolView().setCharPool(CharPoolReader.load(file.toPath()));
      }
      catch (IOException e)
      {
         FxUtilities.showError("File Load Error", "Failed to load character pool", e);
         view.getCharPoolView().setCharPool(null);
      }
   }

   private void onSave()
   {
      FileChooser fc = new FileChooser();
      fc.setTitle("Save Pool to File");
      fc.setInitialFileName(view.getCharPoolView().getCharPool().getName());
      if (recentDirectory != null)
      {
         fc.setInitialDirectory(recentDirectory);
      }
      File file = fc.showSaveDialog(view.getScene().getWindow());
      if (file != null)
      {
         recentDirectory = file.getParentFile();
         savePool(file);
      }
   }

   private void savePool(File file)
   {
      try (PropertyWriterImpl writer = new PropertyWriterImpl(file.toPath()))
      {
         writer.write(view.getCharPoolView().getCharPool());
      }
      catch (Exception e)
      {
         FxUtilities.showError("File Save Error", "Failed to save character pool", e);
         view.getCharPoolView().setCharPool(null);
      }
   }

   public MainView getView()
   {
      return view;
   }
}

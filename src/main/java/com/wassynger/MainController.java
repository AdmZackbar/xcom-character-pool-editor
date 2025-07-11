package com.wassynger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javafx.stage.DirectoryChooser;
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
      view.addEventHandler(MainView.ON_LOAD_MOD, event -> onLoadMod());
      tryLoadDefaultConfig();
   }

   private void tryLoadDefaultConfig()
   {
      try
      {
         File file = new File(getClass().getClassLoader().getResource("BaseXComGame.int").toURI());
         ConfigReader.loadXComGameIni(file.toPath());
         view.getCharPoolView().refresh();
      }
      catch (Exception e)
      {
         System.err.printf("Failed to load base XCOM config: %s", e);
      }
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
      try (PropertyReaderImpl reader = new PropertyReaderImpl(file.toPath()))
      {
         view.getCharPoolView().setCharPool(reader.readCharacterPool());
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

   private void onLoadMod()
   {
      DirectoryChooser dc = new DirectoryChooser();
      dc.setTitle("Select Mod Directory");
      File directory = dc.showDialog(view.getScene().getWindow());
      if (directory != null)
      {
         loadMod(directory);
      }
   }

   private void loadMod(File directory)
   {
      try (Stream<Path> files = Files.walk(directory.toPath()))
      {
         if (files.filter(Files::isRegularFile)
               .filter(p -> "XComGame.int".equals(p.getFileName().toString()))
               .findFirst()
               .map(ConfigReader::loadXComGameIni)
               .orElse(false))
         {
            // Reload info into view
            view.getCharPoolView().refresh();
         }
      }
      catch (Exception e)
      {
         FxUtilities.showError("Mod Load Error", "Failed to load mod", e);
      }
   }

   public MainView getView()
   {
      return view;
   }
}

package com.wassynger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

public class MainController
{
   private final MainView view;

   public MainController()
   {
      this.view = new MainView();
      view.addEventHandler(MainView.ON_QUIT, event -> onQuit());
      view.addEventHandler(MainView.ON_POOL_LOAD, event -> onLoad());
      view.addEventHandler(MainView.ON_POOL_SAVE, event -> onSave());
      view.addEventHandler(MainView.ON_POOL_ADD, event -> onAddPool());
      view.addEventHandler(MainView.ON_POOL_REMOVE, event -> onRemovePool());
      view.addEventHandler(MainView.ON_MOD_LOAD, event -> onLoadMod());
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
      fc.setTitle("Select Character Pool(s)");
      fc.getExtensionFilters()
            .addAll(new FileChooser.ExtensionFilter("Binary file", "*.bin"),
                  new FileChooser.ExtensionFilter("Any", "*"));
      Config.INSTANCE.getFile(Config.Setting.LOAD_POOL_DIR).ifPresent(fc::setInitialDirectory);
      List<File> files = fc.showOpenMultipleDialog(view.getScene().getWindow());
      if (files != null && !files.isEmpty())
      {
         Config.INSTANCE.set(Config.Setting.LOAD_POOL_DIR, files.get(0).getParentFile());
         files.forEach(this::loadPool);
      }
   }

   private void loadPool(File file)
   {
      try (PropertyReaderImpl reader = new PropertyReaderImpl(file.toPath()))
      {
         CharacterPool newPool = reader.readCharacterPool();
         // Check if we need to replace older loaded pool
         for (int i = 0; i < view.getCharPools().size(); i++)
         {
            // Compare based on file name
            if (view.getCharPools().get(i).getFileName().equals(newPool.getFileName()))
            {
               // Replace and select it
               view.getCharPools().set(i, newPool);
               view.getCharPoolSelectionModel().select(i);
               return;
            }
         }
         // Otherwise just add it and select it
         view.getCharPools().add(newPool);
         view.getCharPoolSelectionModel().select(newPool);
      }
      catch (Exception e)
      {
         FxUtilities.showError("File Load Error", "Failed to load character pool", e);
      }
   }

   private void onSave()
   {
      FileChooser fc = new FileChooser();
      fc.setTitle("Save Pool to File");
      fc.setInitialFileName(view.getCharPoolView().getCharPool().getName());
      Config.INSTANCE.getFile(Config.Setting.LOAD_POOL_DIR).ifPresent(fc::setInitialDirectory);
      File file = fc.showSaveDialog(view.getScene().getWindow());
      if (file != null)
      {
         Config.INSTANCE.set(Config.Setting.LOAD_POOL_DIR, file.getParentFile());
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
      }
   }

   private void onAddPool()
   {
      // TODO get initial name from user?
      view.getCharPools().add(new CharacterPool("NewPool", "NewPool.bin", Collections.emptyList()));
   }

   private void onRemovePool()
   {
      // will eventually need to guard against removing pools with unsaved
      // changes (with a dialog confirmation)
      view.getCharPools().remove(view.getCharPoolSelectionModel().getSelectedItem());
   }

   private void onLoadMod()
   {
      DirectoryChooser dc = new DirectoryChooser();
      dc.setTitle("Select Mod Directory");
      Config.INSTANCE.getFile(Config.Setting.LOAD_MOD_DIR).ifPresent(dc::setInitialDirectory);
      File directory = dc.showDialog(view.getScene().getWindow());
      if (directory != null)
      {
         Config.INSTANCE.set(Config.Setting.LOAD_MOD_DIR, directory);
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

package com.wassynger.xcom.pooleditor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.concurrent.Task;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

import com.wassynger.xcom.pooleditor.data.CharacterPool;
import com.wassynger.xcom.pooleditor.data.CharacterPoolReader;
import com.wassynger.xcom.pooleditor.data.CharacterPoolWriter;
import com.wassynger.xcom.pooleditor.data.EditableCharPool;
import com.wassynger.xcom.pooleditor.data.EditableCharacter;
import com.wassynger.xcom.pooleditor.ui.FxUtilities;

public class MainController
{
   private final ExecutorService threadPool;
   private final MainView view;

   public MainController()
   {
      this.threadPool = Executors.newCachedThreadPool();
      this.view = new MainView();
      view.addEventHandler(MainView.ON_QUIT, event -> onQuit());
      view.addEventHandler(MainView.ON_POOL_LOAD, event -> onLoad());
      view.addEventHandler(MainView.ON_POOL_SAVE, event -> onSave());
      view.addEventHandler(MainView.ON_POOL_ADD, event -> onAddPool());
      view.addEventHandler(MainView.ON_POOL_REMOVE, event -> onRemovePool());
      view.addEventHandler(MainView.ON_MOD_LOAD, event -> onLoadMod());
      view.addEventHandler(CharPoolView.ON_CHAR_ADD, event -> onAddCharacter());
      view.addEventHandler(CharPoolView.ON_CHAR_REMOVE, event -> onRemoveCharacter());
      tryLoadDefaultConfig();
      tryLoadPreviousPools();
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
         System.err.printf("Failed to load base XCOM config: %s%n", e);
      }
   }

   private void tryLoadPreviousPools()
   {
      try
      {
         threadPool.execute(new LoadPoolTask(
               Config.INSTANCE.getList(Config.Setting.LOADED_MODS).stream().map(Paths::get).map(Path::toFile)
                     // Ensure file still exists
                     .filter(File::isFile).collect(Collectors.toList())));
      }
      catch (Exception e)
      {
         System.err.printf("Failed to load previous mods: %s", e);
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
      Config.INSTANCE.getFile(Config.Setting.LOAD_POOL_DIR)
            // Ensure directory still exists
            .filter(File::isDirectory).ifPresent(fc::setInitialDirectory);
      List<File> files = fc.showOpenMultipleDialog(view.getScene().getWindow());
      if (files != null && !files.isEmpty())
      {
         Config.INSTANCE.set(Config.Setting.LOAD_POOL_DIR, files.get(0).getParentFile());
         threadPool.execute(new LoadPoolTask(files));
      }
   }

   private void onSave()
   {
      EditableCharPool pool = view.getCharPoolView().getCharPool();
      File file = Optional.ofNullable(pool.getBasePool().getPath())
            .map(Path::toFile)
            .filter(File::isFile)
            .orElseGet(this::getSaveDirectory);
      if (file != null && file.isFile())
      {
         Config.INSTANCE.set(Config.Setting.LOAD_POOL_DIR, file.getParentFile());
         backupFile(file);
         threadPool.execute(new SavePoolTask(pool, file));
      }
   }

   private File getSaveDirectory()
   {
      EditableCharPool pool = view.getCharPoolView().getCharPool();
      FileChooser fc = new FileChooser();
      fc.setTitle("Save Pool to File");
      fc.setInitialFileName(String.format("%s.bin", pool.getBasePool().getName()));
      fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary file", "*.bin"));
      Config.INSTANCE.getFile(Config.Setting.LOAD_POOL_DIR)
            // Ensure directory still exists
            .filter(File::isDirectory).ifPresent(fc::setInitialDirectory);
      return fc.showSaveDialog(view.getScene().getWindow());
   }

   private void backupFile(File file)
   {
      try
      {
         File backupDir = new File("backup");
         if (!backupDir.isDirectory() && !backupDir.mkdir())
         {
            System.err.println("Failed to create backup dir");
         }
         else
         {
            File backupFile = new File("backup", file.getName());
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }
      }
      catch (Exception e)
      {
         System.err.printf("Failed to backup save file: %s%n", e);
      }
   }

   private void onAddPool()
   {
      TextInputDialog dialog = new TextInputDialog("NewPool");
      dialog.setTitle("Name Dialog");
      dialog.setHeaderText("Set Pool Name");
      dialog.showAndWait().ifPresent(this::addPool);
   }

   private void addPool(String name)
   {
      view.getCharPools()
            .add(EditableCharPool.create(
                  new CharacterPool(null, name, String.format("%s.bin", name), Collections.emptyList())));
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
         threadPool.execute(new LoadModTask(directory));
      }
   }

   private void onAddCharacter()
   {
      // TODO
   }

   private void onRemoveCharacter()
   {
      // TODO
   }

   public MainView getView()
   {
      return view;
   }

   public void shutdown()
   {
      threadPool.shutdown();
      try
      {
         if (!threadPool.awaitTermination(1, TimeUnit.MINUTES))
         {
            System.out.println("Thread pool failed to shutdown within a minute.");
         }
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
      }
   }

   class LoadPoolTask extends Task<List<CharacterPool>>
   {
      private final List<File> files;

      private int index = 0;

      public LoadPoolTask(List<File> files)
      {
         this.files = files;
         view.getProgressView().bind(this);
      }

      @Override
      protected List<CharacterPool> call()
      {
         return files.stream().map(this::loadPool).filter(Objects::nonNull).collect(Collectors.toList());
      }

      private CharacterPool loadPool(File file)
      {
         updateMessage(String.format("Loading %s...", file.getName()));
         updateProgress(index++, files.size());
         try (CharacterPoolReader reader = CharacterPoolReader.open(file.toPath()))
         {
            return reader.read();
         }
         catch (Exception e)
         {
            // TODO
            return null;
         }
      }

      @Override
      protected void succeeded()
      {
         EditableCharacter selectedChar = view.getCharPoolView().getCharSelectionModel().getSelectedItem();
         getValue().stream().map(EditableCharPool::create).forEach(this::addOrReplacePool);
         view.getCharPoolView().getCharSelectionModel().select(selectedChar);
      }

      private void addOrReplacePool(EditableCharPool pool)
      {
         // Check if we need to replace older loaded pool
         for (int i = 0; i < view.getCharPools().size(); i++)
         {
            // Compare based on file
            if (Objects.equals(view.getCharPools().get(i).getBasePool().getPath(), pool.getBasePool().getPath()))
            {
               // Replace and select it
               view.getCharPools().set(i, pool);
               selectPoolIfSingle(pool);
               return;
            }
         }
         // Otherwise just add it
         view.getCharPools().add(pool);
         selectPoolIfSingle(pool);
      }

      private void selectPoolIfSingle(EditableCharPool pool)
      {
         // Select it if it's the only one
         if (files.size() == 1)
         {
            view.getCharPoolSelectionModel().select(pool);
         }
      }

      @Override
      protected void failed()
      {
         FxUtilities.showError("File Load Error", "Failed to load character pool", getException());
      }
   }

   class SavePoolTask extends Task<Void>
   {
      private final EditableCharPool pool;
      private final File file;

      public SavePoolTask(EditableCharPool pool, File file)
      {
         this.pool = pool;
         this.file = file;
         view.getProgressView().bind(this);
      }

      @Override
      protected Void call() throws Exception
      {
         updateMessage("Saving pool to file...");
         try (CharacterPoolWriter writer = CharacterPoolWriter.open(file.toPath()))
         {
            CharacterPool updatedPool = new CharacterPool(file.toPath(), pool.getBasePool().getName(), file.getName(),
                  pool.getCharacters().stream().map(EditableCharacter::computeEditedChar).collect(Collectors.toList()));
            writer.write(updatedPool);
         }
         return null;
      }

      @Override
      protected void succeeded()
      {
         // Load the saved pool
         threadPool.execute(new LoadPoolTask(Collections.singletonList(file)));
      }

      @Override
      protected void failed()
      {
         FxUtilities.showError("File Save Error", "Failed to save character pool", getException());
      }
   }

   class LoadModTask extends Task<Boolean>
   {
      private final File directory;

      public LoadModTask(File directory)
      {
         this.directory = directory;
         view.getProgressView().bind(this);
      }

      @Override
      protected Boolean call() throws Exception
      {
         updateMessage("Loading mod config...");
         try (Stream<Path> files = Files.walk(directory.toPath()))
         {
            return files.filter(Files::isRegularFile)
                  .filter(p -> "XComGame.int".equals(p.getFileName().toString()))
                  .findFirst()
                  .map(ConfigReader::loadXComGameIni)
                  .orElse(false);
         }
      }

      @Override
      protected void succeeded()
      {
         if (getValue())
         {
            // Reload info into view
            view.getCharPoolView().refresh();
         }
      }

      @Override
      protected void failed()
      {
         FxUtilities.showError("Mod Load Error", "Failed to load mod", getException());
      }
   }
}

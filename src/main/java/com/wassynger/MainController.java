package com.wassynger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

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
         threadPool.execute(new LoadPoolTask(files));
      }
   }

   private void onSave()
   {
      CharacterPool pool = Objects.requireNonNull(view.getCharPoolView().getCharPool());
      FileChooser fc = new FileChooser();
      fc.setTitle("Save Pool to File");
      fc.setInitialFileName(pool.getName());
      Config.INSTANCE.getFile(Config.Setting.LOAD_POOL_DIR).ifPresent(fc::setInitialDirectory);
      File file = fc.showSaveDialog(view.getScene().getWindow());
      if (file != null)
      {
         Config.INSTANCE.set(Config.Setting.LOAD_POOL_DIR, file.getParentFile());
         threadPool.execute(new SavePoolTask(pool, file));
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
         threadPool.execute(new LoadModTask(directory));
      }
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
         try (PropertyReaderImpl reader = new PropertyReaderImpl(file.toPath()))
         {
            return reader.readCharacterPool();
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
         for (CharacterPool pool : getValue())
         {
            // Check if we need to replace older loaded pool
            for (int i = 0; i < view.getCharPools().size(); i++)
            {
               // Compare based on file name
               if (view.getCharPools().get(i).getFileName().equals(pool.getFileName()))
               {
                  // Replace and select it
                  view.getCharPools().set(i, pool);
                  view.getCharPoolSelectionModel().select(i);
               }
            }
            // Otherwise just add it and select it
            view.getCharPools().add(pool);
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
      private final CharacterPool pool;
      private final File file;

      public SavePoolTask(CharacterPool pool, File file)
      {
         this.pool = pool;
         this.file = file;
         view.getProgressView().bind(this);
      }

      @Override
      protected Void call() throws Exception
      {
         updateMessage("Saving pool to file...");
         try (PropertyWriterImpl writer = new PropertyWriterImpl(file.toPath()))
         {
            writer.write(pool);
         }
         return null;
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

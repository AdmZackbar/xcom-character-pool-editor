package com.wassynger;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public enum Config
{
   INSTANCE;

   private static final Path CONFIG_PATH = Paths.get("config.properties");

   private final Properties config;

   Config()
   {
      this.config = new Properties();
   }

   void load()
   {
      if (!Files.isRegularFile(CONFIG_PATH))
      {
         // Nothing to load
         return;
      }
      try (InputStream is = Files.newInputStream(CONFIG_PATH))
      {
         config.load(is);
      }
      catch (Exception e)
      {
         System.err.printf("Failed to load config file: %s", e);
         config.clear();
      }
   }

   void save()
   {
      try (OutputStream os = Files.newOutputStream(CONFIG_PATH))
      {
         config.store(os, "config");
      }
      catch (Exception e)
      {
         System.err.printf("Failed to write settings to file: %s", e);
      }
   }

   public Optional<File> getFile(Setting setting)
   {
      return Optional.ofNullable(config.getProperty(setting.key)).map(File::new);
   }

   public void set(Setting setting, File file)
   {
      if (file == null)
      {
         config.remove(setting.key);
         return;
      }
      config.setProperty(setting.key, file.getAbsolutePath());
   }

   public enum Setting
   {
      LOAD_MOD_DIR("loadModDir"),
      LOAD_POOL_DIR("loadPoolDir");

      private final String key;

      Setting(String key)
      {
         this.key = key;
      }
   }
}

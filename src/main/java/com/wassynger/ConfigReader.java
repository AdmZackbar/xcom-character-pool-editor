package com.wassynger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.configuration2.INIConfiguration;

final class ConfigReader
{
   public static boolean loadXComGameIni(Path iniFile)
   {
      try (InputStream is = Files.newInputStream(iniFile);
           InputStreamReader isr = new InputStreamReader(is, guessCharset(iniFile)))
      {
         INIConfiguration config = new INIConfiguration();
         config.read(isr);
         for (String section : config.getSections())
         {
            if (section == null)
            {
               // Apparently section can be null
               continue;
            }
            for (StringTemplate template : StringTemplate.values())
            {
               if (template.tryAdd(config, section))
               {
                  // Exit early if 1 matches
                  break;
               }
            }
         }
         return true;
      }
      catch (Exception e)
      {
         System.err.printf("Failed to load INI: %s", e);
         return false;
      }
   }

   private static Charset guessCharset(Path iniFile)
   {
      // Guess encoding based on presence of byte order mark
      try (InputStream is = Files.newInputStream(iniFile))
      {
         byte[] buffer = new byte[2];
         int res = is.read(buffer);
         if (res == 2)
         {
            // If present, assume UTF-16 (either endianness)
            if (buffer[0] == (byte) 0xFE && buffer[1] == (byte) 0xFF)
            {
               return StandardCharsets.UTF_16BE;
            }
            else if (buffer[0] == (byte) 0xFF && buffer[1] == (byte) 0xFE)
            {
               return StandardCharsets.UTF_16LE;
            }
         }
      }
      catch (IOException e)
      {
         // Do nothing
      }
      // Otherwise, assume UTF-8
      return StandardCharsets.UTF_8;
   }

   private ConfigReader()
   {
      // Do nothing
   }
}

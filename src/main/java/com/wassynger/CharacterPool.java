package com.wassynger;

import java.nio.file.Path;
import java.util.List;

public class CharacterPool
{
   private final Path path;
   private final String name;
   private final String fileName;
   private final List<Character> characters;

   public CharacterPool(Path path, String name, String fileName, List<Character> characters)
   {
      this.path = path;
      this.name = name;
      this.fileName = fileName;
      this.characters = characters;
   }

   public Path getPath()
   {
      return path;
   }

   public String getName()
   {
      return name;
   }

   public String getFileName()
   {
      return fileName;
   }

   public List<Character> getCharacters()
   {
      return characters;
   }
}

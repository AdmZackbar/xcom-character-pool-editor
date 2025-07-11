package com.wassynger;

import java.util.List;

public class CharacterPool
{
   private final String name;
   private final String fileName;
   private final List<Character> characters;

   public CharacterPool(String name, String fileName, List<Character> characters)
   {
      this.name = name;
      this.fileName = fileName;
      this.characters = characters;
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

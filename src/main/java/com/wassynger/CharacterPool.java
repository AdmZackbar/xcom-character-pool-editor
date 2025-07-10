package com.wassynger;

import java.util.List;

public class CharacterPool
{
   private final String name;
   private final List<Character> characters;

   public CharacterPool(String name, List<Character> characters)
   {
      this.name = name;
      this.characters = characters;
   }

   public String getName()
   {
      return name;
   }

   public List<Character> getCharacters()
   {
      return characters;
   }
}

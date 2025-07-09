package com.wassynger;

import java.util.List;
import java.util.Objects;

public class CharacterPool
{
   private final List<Character> characters;

   public CharacterPool(List<Character> characters)
   {
      this.characters = Objects.requireNonNull(characters);
   }

   public List<Character> getCharacters()
   {
      return characters;
   }
}

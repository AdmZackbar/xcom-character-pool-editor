package com.wassynger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CharacterTemplate
{
   private static final Map<String, CharacterTemplate> LOOKUP_MAP;

   static
   {
      LOOKUP_MAP = new HashMap<>();
      add("Soldier", "Soldier");
      add("ReaperSoldier", "Reaper");
      add("SkirmisherSoldier", "Skirmisher");
      add("TemplarSoldier", "Templar");
      add("SparkSoldier", "SPARK");
   }

   private static CharacterTemplate add(String str, String loc)
   {
      CharacterTemplate template = new CharacterTemplate(str, loc);
      LOOKUP_MAP.put(str, template);
      return template;
   }

   public static CharacterTemplate getOrAdd(String str)
   {
      return LOOKUP_MAP.computeIfAbsent(str, s -> add(s, s));
   }

   public static List<CharacterTemplate> getAll()
   {
      return new ArrayList<>(LOOKUP_MAP.values());
   }

   private final String raw;
   private final String loc;

   private CharacterTemplate(String raw, String loc)
   {
      this.raw = raw;
      this.loc = loc;
   }

   public String getRaw()
   {
      return raw;
   }

   public String getLocalizedString()
   {
      return loc;
   }

   @Override
   public String toString()
   {
      return "CharacterTemplate{" + "raw='" + raw + '\'' + ", loc='" + loc + '\'' + '}';
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof CharacterTemplate))
      {
         return false;
      }
      CharacterTemplate that = (CharacterTemplate) o;
      return Objects.equals(raw, that.raw) && Objects.equals(loc, that.loc);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(raw, loc);
   }
}

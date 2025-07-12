package com.wassynger.xcom.pooleditor.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.INIConfiguration;

public enum StringTemplate
{
   CHARACTER("X2CharacterTemplate", "strCharacterName"),
   CLASS("X2SoldierClassTemplate", "DisplayName"),
   COUNTRY("X2CountryTemplate", "DisplayName");

   private final Pattern sectionPattern;
   private final String locKey;
   private final Map<String, StringEntry> map;

   StringTemplate(String headerName, String locKey)
   {
      this.sectionPattern = Pattern.compile(String.format("^(\\S+) %s", headerName), Pattern.UNICODE_CHARACTER_CLASS);
      this.locKey = locKey;
      this.map = new HashMap<>();
   }

   public StringEntry add(String raw, String loc)
   {
      StringEntry template = new StringEntry(raw, loc, true);
      map.put(raw, template);
      return template;
   }

   public StringEntry getOrAdd(String str)
   {
      return map.computeIfAbsent(str, s -> add(s, s));
   }

   public boolean tryAdd(INIConfiguration config, String section)
   {
      Matcher matcher = sectionPattern.matcher(section);
      if (matcher.matches())
      {
         String str = matcher.group(1);
         String name = config.getSection(section).getString(locKey, str);
         add(str, name);
         return true;
      }
      return false;
   }

   public List<StringEntry> getAll()
   {
      return new ArrayList<>(map.values());
   }
}

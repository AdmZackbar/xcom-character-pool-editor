package com.wassynger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum PropertyType
{
   STRING("StrProperty"),
   NAME("NameProperty"),
   INT("IntProperty"),
   BOOL("BoolProperty"),
   STRUCT("StructProperty"),
   ARRAY("ArrayProperty");

   private static final Map<String, PropertyType> NAME_MAP;

   static
   {
      NAME_MAP = new HashMap<>();
      for (PropertyType type : PropertyType.values())
      {
         NAME_MAP.put(type.name, type);
      }
   }

   public static PropertyType get(String str)
   {
      return Objects.requireNonNull(NAME_MAP.get(str));
   }

   private final String name;

   PropertyType(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }
}

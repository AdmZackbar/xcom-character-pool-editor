package com.wassynger.xcom.pooleditor.data;

import java.util.HashMap;
import java.util.Map;

/**
 * All supported types of {@link Property}.
 *
 * @author Zach Wassynger
 */
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

   /**
    * Finds and returns the property type that is represented by the given
    * string.
    *
    * @param str the given string
    * @return the property type
    * @throws IllegalArgumentException if the type does not exist for the string
    */
   public static PropertyType get(String str)
   {
      if (!NAME_MAP.containsKey(str))
      {
         throw new IllegalArgumentException(String.format("no type exists for %s", str));
      }
      return NAME_MAP.get(str);
   }

   private final String name;

   PropertyType(String name)
   {
      this.name = name;
   }

   /**
    * Returns the raw string that represents this type when serialized.
    *
    * @return the name
    */
   public String getName()
   {
      return name;
   }
}

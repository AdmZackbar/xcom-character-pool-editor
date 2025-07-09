package com.wassynger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class Property
{
   private final String name;
   private final Type type;
   private final Object data;

   public Property(String name, Type type, Object data)
   {
      this.name = name;
      this.type = type;
      this.data = data;
   }

   public String getName()
   {
      return name;
   }

   public Type getType()
   {
      return type;
   }

   public Object getData()
   {
      return data;
   }

   @Override
   public String toString()
   {
      return "Property{" + "name='" + name + '\'' + ", type=" + type + ", data=" + data + '}';
   }

   public enum Type
   {
      STRING("StrProperty"),
      NAME("NameProperty"),
      INT("IntProperty"),
      BOOL("BoolProperty"),
      STRUCT("StructProperty"),
      ARRAY("ArrayProperty");

      private static final Map<String, Type> NAME_MAP;

      static
      {
         NAME_MAP = new HashMap<>();
         for (Type type : Type.values())
         {
            NAME_MAP.put(type.name, type);
         }
      }

      public static Type get(String str)
      {
         return Objects.requireNonNull(NAME_MAP.get(str));
      }

      private final String name;

      Type(String name)
      {
         this.name = name;
      }
   }
}

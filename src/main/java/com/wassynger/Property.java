package com.wassynger;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class Property
{
   // size of 'None' string + 'None\0' + padding
   static final int NONE_NUM_BYTES = computeStringNumBytes("None") + Integer.BYTES;

   static int computeStringNumBytes(String str)
   {
      if (str == null)
      {
         return 0;
      }
      // size of string (int) + string length + null terminator
      return Integer.BYTES + (str.getBytes(StandardCharsets.UTF_8)).length + Byte.BYTES;
   }

   private final PropertyType type;
   private final String name;
   private final PropertyValue value;

   public Property(PropertyType type, String name, PropertyValue value)
   {
      this.type = Objects.requireNonNull(type);
      this.name = Objects.requireNonNull(name);
      this.value = Objects.requireNonNull(value);
   }

   public PropertyType getType()
   {
      return type;
   }

   public String getName()
   {
      return name;
   }

   public PropertyValue getValue()
   {
      return value;
   }

   public int computeLength()
   {
      // name length + name + padding + type name length + type name + padding + value
      return computeStringNumBytes(name) + Integer.BYTES + computeStringNumBytes(type.getName()) + Integer.BYTES +
             value.length();
   }

   @Override
   public String toString()
   {
      return "Property{" + "type=" + type + ", name='" + name + '\'' + ", value=" + value + '}';
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof Property))
      {
         return false;
      }
      Property property = (Property) o;
      return type == property.type && Objects.equals(name, property.name) && Objects.equals(value, property.value);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(type, name, value);
   }
}

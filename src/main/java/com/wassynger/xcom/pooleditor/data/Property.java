package com.wassynger.xcom.pooleditor.data;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents data found in Unreal Engine's 'UEProperty' objects. There are
 * different property types (detailed in {@link PropertyType}). The value stored
 * in each property is dependent on this type.
 *
 * @author Zach Wassynger
 */
public final class Property
{
   // size of 'None' string + 'None\0' + padding
   static final int NONE_NUM_BYTES = computeStringNumBytes("None") + Integer.BYTES;

   /**
    * Helper function to compute the size of the string (when serialized) in
    * bytes.
    *
    * @param str the string
    * @return the size in bytes
    */
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

   /**
    * Creates a new property with the given arguments.
    *
    * @param type  the property type, non-null
    * @param name  the name, non-null
    * @param value the value, non-null
    * @throws NullPointerException if any args are null
    */
   public Property(PropertyType type, String name, PropertyValue value)
   {
      this.type = Objects.requireNonNull(type);
      this.name = Objects.requireNonNull(name);
      this.value = Objects.requireNonNull(value);
   }

   /**
    * Returns the type.
    *
    * @return the type
    */
   public PropertyType getType()
   {
      return type;
   }

   /**
    * Returns the name.
    *
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Returns the value.
    *
    * @return the value
    */
   public PropertyValue getValue()
   {
      return value;
   }

   /**
    * Computes and returns the size of this property (when serialized) in bytes.
    *
    * @return the size in bytes
    */
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

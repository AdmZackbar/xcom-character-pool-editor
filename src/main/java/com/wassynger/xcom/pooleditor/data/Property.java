package com.wassynger.xcom.pooleditor.data;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

   public static Map<PropertyField, PropertyValue> toMap(List<Property> list)
   {
      return list.stream()
            .collect(Collectors.toMap(Property::getField, Property::getValue, (a, b) -> a, LinkedHashMap::new));
   }

   public static List<Property> toList(Map<PropertyField, PropertyValue> map)
   {
      return map.entrySet().stream().map(e -> new Property(e.getKey(), e.getValue())).collect(Collectors.toList());
   }

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

   private final PropertyField field;
   private final PropertyValue value;

   /**
    * Creates a new property with the given arguments.
    *
    * @param field the field non-null
    * @param value the value, non-null
    * @throws NullPointerException if any args are null
    */
   public Property(PropertyField field, PropertyValue value)
   {
      this.field = Objects.requireNonNull(field);
      this.value = Objects.requireNonNull(value);
   }

   /**
    * Returns the field.
    *
    * @return the field
    */
   public PropertyField getField()
   {
      return field;
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
      return computeStringNumBytes(field.getName()) + Integer.BYTES + computeStringNumBytes(field.getType().getName()) +
             Integer.BYTES + value.length();
   }

   @Override
   public String toString()
   {
      return "Property{" + "field=" + field + ", value=" + value + '}';
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof Property))
      {
         return false;
      }
      Property property = (Property) o;
      return Objects.equals(field, property.field) && Objects.equals(value, property.value);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(field, value);
   }
}

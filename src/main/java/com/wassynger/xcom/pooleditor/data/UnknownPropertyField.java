package com.wassynger.xcom.pooleditor.data;

import java.util.Objects;

/**
 * Represents a property field that does not match any 'known' fields. Stores
 * the custom name and type.
 *
 * @author Zach Wassynger
 * @since 5.0
 */
final class UnknownPropertyField implements PropertyField
{
   private final String name;
   private final PropertyType type;

   /**
    * Creates a new field for the given name and type.
    *
    * @param name the given name, non-null
    * @param type the given type, non-null
    * @throws NullPointerException if name or type is null
    */
   public UnknownPropertyField(String name, PropertyType type)
   {
      this.name = Objects.requireNonNull(name);
      this.type = Objects.requireNonNull(type);
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public PropertyType getType()
   {
      return type;
   }

   @Override
   public String toString()
   {
      return "UnknownField{" + "name='" + name + '\'' + ", type=" + type + '}';
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof UnknownPropertyField))
      {
         return false;
      }
      UnknownPropertyField that = (UnknownPropertyField) o;
      return Objects.equals(name, that.name) && type == that.type;
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(name, type);
   }
}

package com.wassynger;

import java.util.Objects;

final class Property
{
   private final PropertyType type;
   private final String name;
   private final Object data;

   public Property(PropertyType type, String name, Object data)
   {
      this.type = type;
      this.name = name;
      this.data = data;
   }

   public PropertyType getType()
   {
      return type;
   }

   public String getName()
   {
      return name;
   }

   public Object getData()
   {
      return data;
   }

   @Override
   public String toString()
   {
      return "Property{" + "type=" + type + ", name='" + name + '\'' + ", data=" + data + '}';
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof Property))
      {
         return false;
      }
      Property property = (Property) o;
      return type == property.type && Objects.equals(name, property.name) && Objects.equals(data, property.data);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(type, name, data);
   }
}

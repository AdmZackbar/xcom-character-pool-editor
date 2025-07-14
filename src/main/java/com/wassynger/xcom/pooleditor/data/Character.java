package com.wassynger.xcom.pooleditor.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Character
{
   static Character fromProperties(List<Property> properties)
   {
      Map<PropertyField, PropertyValue> map = new LinkedHashMap<>();
      for (Property property : properties)
      {
         PropertyField field = CharacterField.get(property.getName())
               .map(PropertyField.class::cast)
               .orElse(new UnknownField(property.getName(), property.getType()));
         map.put(field, property.getValue());
         if (CharacterField.APPEARANCE.getName().equals(property.getName()))
         {
            // Special case, iterate and add children of appearance
            StructPropertyValue appearance = (StructPropertyValue) property.getValue();
            for (Property aProp : appearance.getEntries())
            {
               AppearanceField.get(aProp.getName()).ifPresent(f -> map.put(f, aProp.getValue()));
            }
         }
      }
      return new Character(map);
   }

   private final Map<PropertyField, PropertyValue> map;

   private Character(Map<PropertyField, PropertyValue> map)
   {
      this.map = map;
   }

   public PropertyValue get(PropertyField field)
   {
      return map.get(field);
   }

   public Optional<String> tryGet(PropertyField field)
   {
      return Optional.ofNullable(map.get(field)).map(PropertyValue::getDisplayValue);
   }

   ArrayPropertyValue.Entry toEntry()
   {
      return new ArrayPropertyValue.Entry(map.entrySet()
            .stream()
            .filter(e -> !(e.getKey() instanceof AppearanceField))
            .map(e -> new Property(e.getKey().getType(), e.getKey().getName(), e.getValue()))
            .collect(Collectors.toList()));
   }

   @Override
   public String toString()
   {
      return "Character{" + "map=" + map + '}';
   }

   static final class UnknownField implements PropertyField
   {
      private final String name;
      private final PropertyType type;

      public UnknownField(String name, PropertyType type)
      {
         this.name = name;
         this.type = type;
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
         if (!(o instanceof UnknownField))
         {
            return false;
         }
         UnknownField that = (UnknownField) o;
         return Objects.equals(name, that.name) && type == that.type;
      }

      @Override
      public int hashCode()
      {
         return Objects.hash(name, type);
      }
   }
}

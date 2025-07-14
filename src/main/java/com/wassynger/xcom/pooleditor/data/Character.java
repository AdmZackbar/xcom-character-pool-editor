package com.wassynger.xcom.pooleditor.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Character
{
   static Character fromProperties(List<Property> properties)
   {
      Map<PropertyField, PropertyValue> map = Property.toMap(properties);
      if (map.containsKey(CharacterField.APPEARANCE))
      {
         // Special case, iterate and add children of appearance
         StructPropertyValue appearance = (StructPropertyValue) map.get(CharacterField.APPEARANCE);
         for (Property aProp : appearance.getEntries())
         {
            map.put(aProp.getField(), aProp.getValue());
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
            // Remove appearance field entries (since they will be in the struct)
            .filter(e -> !(e.getKey() instanceof AppearanceField))
            .map(e -> new Property(e.getKey(), e.getValue()))
            .collect(Collectors.toList()));
   }

   @Override
   public String toString()
   {
      return "Character{" + "map=" + map + '}';
   }
}

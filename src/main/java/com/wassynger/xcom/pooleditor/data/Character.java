package com.wassynger.xcom.pooleditor.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Character
{
   private static final String APPEARANCE_STRUCT_TYPE_NAME = "TAppearance";

   static Character fromProperties(List<Property> properties)
   {
      Map<PropertyField, PropertyValue> map = new LinkedHashMap<>();
      for (Property property : properties)
      {
         if (CharacterField.APPEARANCE.getName().equals(property.getName()))
         {
            // Special case, iterate and add children of appearance
            StructPropertyValue appearance = (StructPropertyValue) property.getValue();
            for (Property aProp : appearance.getEntries())
            {
               AppearanceField.get(aProp.getName()).ifPresent(f -> map.put(f, aProp.getValue()));
            }
         }
         else
         {
            CharacterField.get(property.getName()).ifPresent(f -> map.put(f, property.getValue()));
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
      return new ArrayPropertyValue.Entry(Stream.concat(
                  map.entrySet().stream().map(e -> new Property(e.getKey().getType(), e.getKey().getName(),
                        e.getValue())),
                  Stream.of(new Property(PropertyType.STRUCT, CharacterField.APPEARANCE.getName(),
                        new StructPropertyValue(APPEARANCE_STRUCT_TYPE_NAME, computePropertyValues()))))
            .collect(Collectors.toList()));
   }

   private List<Property> computePropertyValues()
   {
      return map.entrySet()
            .stream()
            .map(e -> new Property(e.getKey().getType(), e.getKey().getName(), e.getValue()))
            .collect(Collectors.toList());
   }

   @Override
   public String toString()
   {
      return "Character{" + "map=" + map + '}';
   }
}

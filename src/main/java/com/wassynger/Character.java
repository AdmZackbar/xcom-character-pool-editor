package com.wassynger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Character
{
   private final Map<CharacterProperty, Property> propertyMap;

   Character(List<Property> data)
   {
      this.propertyMap = new EnumMap<>(CharacterProperty.class);
      for (Property property : data)
      {
         CharacterProperty.get(property.getName()).ifPresent(n -> propertyMap.put(n, property));
      }
   }

   public String get(CharacterProperty property)
   {
      return (String) propertyMap.get(property).getData();
   }

   public Optional<String> tryGet(CharacterProperty property)
   {
      return Optional.ofNullable(propertyMap.get(property)).map(Property::getData).map(String.class::cast);
   }

   public void set(CharacterProperty property, String value)
   {
      propertyMap.put(property, new Property(property.getRaw(), Property.Type.STRING, value));
   }

   public boolean isSelected(CharacterProperty property)
   {
      return (Boolean) propertyMap.get(property).getData();
   }

   List<Property> getProperties(CharacterProperty property)
   {
      return ((List<?>) propertyMap.get(property).getData()).stream()
            .map(Property.class::cast)
            .collect(Collectors.toList());
   }

   @Override
   public String toString()
   {
      return "Character{" + "propertyMap=" + propertyMap + '}';
   }
}

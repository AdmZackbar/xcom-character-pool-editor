package com.wassynger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

   List<Property> getProperties()
   {
      return new ArrayList<>(propertyMap.values());
   }

   @Override
   public String toString()
   {
      return "Character{" + "propertyMap=" + propertyMap + '}';
   }
}

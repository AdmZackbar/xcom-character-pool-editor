package com.wassynger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Character
{
   private final Map<CharacterProperty, Property> propertyMap;
   private final Map<AppearanceProperty, Property> appearanceMap;

   Character(List<Property> data)
   {
      this.propertyMap = new EnumMap<>(CharacterProperty.class);
      this.appearanceMap = new EnumMap<>(AppearanceProperty.class);
      for (Property property : data)
      {
         CharacterProperty.get(property.getName()).ifPresent(n -> propertyMap.put(n, property));
      }
      if (propertyMap.containsKey(CharacterProperty.APPEARANCE))
      {
         for (Property property : getProperties(CharacterProperty.APPEARANCE))
         {
            AppearanceProperty.get(property.getName()).ifPresent(p -> appearanceMap.put(p, property));
         }
      }
   }

   public String get(CharacterProperty property)
   {
      return (String) propertyMap.get(property).getData();
   }

   public Object get(AppearanceProperty property)
   {
      return appearanceMap.containsKey(property) ? appearanceMap.get(property).getData() : null;
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

   public <T extends Enum<T> & StaticEnum> Optional<T> getAppearanceEnum(AppearanceProperty property, Class<T> cls)
   {
      return Optional.ofNullable(appearanceMap.get(property))
            .map(Property::getData)
            .map(Integer.class::cast)
            .flatMap(v -> StaticEnum.fromValue(cls, v));
   }

   @Override
   public String toString()
   {
      return "Character{" + "propertyMap=" + propertyMap + '}';
   }
}

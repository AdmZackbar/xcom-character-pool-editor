package com.wassynger;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Character
{
   private final Map<CharacterField, Property> propertyMap;
   private final Map<AppearanceField, Property> appearanceMap;

   Character(List<Property> data)
   {
      this.propertyMap = new EnumMap<>(CharacterField.class);
      this.appearanceMap = new EnumMap<>(AppearanceField.class);
      for (Property property : data)
      {
         CharacterField.get(property.getName()).ifPresent(n -> propertyMap.put(n, property));
      }
      if (propertyMap.containsKey(CharacterField.APPEARANCE))
      {
         for (Property property : getProperties(CharacterField.APPEARANCE))
         {
            AppearanceField.get(property.getName()).ifPresent(p -> appearanceMap.put(p, property));
         }
      }
   }

   public Object get(CharacterField field)
   {
      return propertyMap.containsKey(field) ? propertyMap.get(field).getData() : null;
   }

   public Object get(AppearanceField field)
   {
      return appearanceMap.containsKey(field) ? appearanceMap.get(field).getData() : null;
   }

   public Optional<String> tryGet(CharacterField field)
   {
      return Optional.ofNullable(propertyMap.get(field)).map(Property::getData).map(Objects::toString);
   }

   public Optional<Boolean> isSelected(CharacterField field)
   {
      return Optional.ofNullable(propertyMap.get(field)).filter(p -> p.getType() == PropertyType.BOOL).map(Property::getData).map(Boolean.class::cast);
   }

   List<Property> getProperties(CharacterField field)
   {
      Property property = propertyMap.get(field);
      if (property == null || property.getType() != PropertyType.STRUCT)
      {
         return Collections.emptyList();
      }
      return ((List<?>) propertyMap.get(field).getData()).stream()
            .map(Property.class::cast)
            .collect(Collectors.toList());
   }

   public <T extends Enum<T> & StaticEnum> Optional<T> getAppearanceEnum(AppearanceField field, Class<T> cls)
   {
      return Optional.ofNullable(appearanceMap.get(field))
            .filter(p -> p.getType() == PropertyType.INT)
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

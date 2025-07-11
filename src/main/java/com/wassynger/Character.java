package com.wassynger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
         StructPropertyValue appearance = (StructPropertyValue) propertyMap.get(CharacterField.APPEARANCE).getValue();
         for (Property property : appearance.getEntries())
         {
            AppearanceField.get(property.getName()).ifPresent(p -> appearanceMap.put(p, property));
         }
      }
   }

   public PropertyValue get(CharacterField field)
   {
      return propertyMap.containsKey(field) ? propertyMap.get(field).getValue() : null;
   }

   public PropertyValue get(AppearanceField field)
   {
      return appearanceMap.containsKey(field) ? appearanceMap.get(field).getValue() : null;
   }

   public Optional<String> tryGet(CharacterField field)
   {
      return Optional.ofNullable(propertyMap.get(field)).map(Property::getValue).map(PropertyValue::getDisplayValue);
   }

   public Optional<Boolean> isSelected(CharacterField field)
   {
      return Optional.ofNullable(propertyMap.get(field))
            .filter(p -> p.getType() == PropertyType.BOOL)
            .map(Property::getValue)
            .map(BoolPropertyValue.class::cast)
            .map(BoolPropertyValue::getValue);
   }

   public <T extends Enum<T> & StaticEnum> Optional<T> getAppearanceEnum(AppearanceField field, Class<T> cls)
   {
      return Optional.ofNullable(appearanceMap.get(field))
            .filter(p -> p.getType() == PropertyType.INT)
            .map(Property::getValue)
            .map(IntPropertyValue.class::cast)
            .map(IntPropertyValue::getValue)
            .flatMap(v -> StaticEnum.fromValue(cls, v));
   }

   ArrayPropertyValue.Entry toEntry()
   {
      return new ArrayPropertyValue.Entry(new ArrayList<>(propertyMap.values()));
   }

   @Override
   public String toString()
   {
      return "Character{" + "propertyMap=" + propertyMap + '}';
   }
}

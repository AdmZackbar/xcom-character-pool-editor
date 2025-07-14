package com.wassynger.xcom.pooleditor.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EditableCharacter
{
   public static EditableCharacter create(Character character)
   {
      return new EditableCharacter(character);
   }

   private final Character baseChar;
   private final Map<PropertyField, Property<?>> propertyMap;
   private final ReadOnlyBooleanWrapper edited;

   private EditableCharacter(Character baseChar)
   {
      this.baseChar = baseChar;
      this.propertyMap = new HashMap<>();
      for (CharacterField field : CharacterField.values())
      {
         createProperty(field, baseChar.get(field)).ifPresent(p -> propertyMap.put(field, p));
      }
      for (AppearanceField field : AppearanceField.values())
      {
         createProperty(field, baseChar.get(field)).ifPresent(p -> propertyMap.put(field, p));
      }
      this.edited = new ReadOnlyBooleanWrapper(this, "edited", false);
      edited.bind(Bindings.createBooleanBinding(this::computeEdited, propertyMap.values().toArray(new Observable[0])));
   }

   private Optional<Property<?>> createProperty(PropertyField field, PropertyValue value)
   {
      switch (field.getType())
      {
      case BOOL:
         return Optional.of(new SimpleBooleanProperty(this, field.getName(),
               value != null && ((BoolPropertyValue) value).getValue()));
      case INT:
         return Optional.of(new SimpleIntegerProperty(this, field.getName(),
               value != null ? ((IntPropertyValue) value).getValue() : 0));
      case STRING:
      case NAME:
         return Optional.of(
               new SimpleStringProperty(this, field.getName(), value != null ? value.getDisplayValue() : null));
      default:
         return Optional.empty();
      }
   }

   private boolean computeEdited()
   {
      return propertyMap.keySet().stream().anyMatch(this::computeEdited);
   }

   private boolean computeEdited(PropertyField field)
   {
      // TODO handle defaults when key missing
      switch (field.getType())
      {
      case BOOL:
         return Optional.ofNullable(baseChar.get(field))
                      .map(BoolPropertyValue.class::cast)
                      .map(BoolPropertyValue::getValue)
                      .filter(v -> v)
                      .isPresent() != boolProperty(field).get();
      case INT:
         return Optional.ofNullable(baseChar.get(field))
                      .map(IntPropertyValue.class::cast)
                      .map(IntPropertyValue::getValue)
                      .orElse(0) != intProperty(field).get();
      case STRING:
      case NAME:
         return !Objects.equals(
               Optional.ofNullable(baseChar.get(field)).map(PropertyValue::getDisplayValue).orElse(null),
               strProperty(field).getValue());
      default:
         throw new AssertionError(String.format("unhandled type: %s", field.getType()));
      }
   }

   public Character getBaseChar()
   {
      return baseChar;
   }

   public Character computeEditedChar()
   {
      Map<PropertyField, PropertyValue> map = baseChar.toEntry()
            .getProperties()
            .stream()
            .collect(Collectors.toMap(p -> CharacterField.get(p.getName())
                  .map(PropertyField.class::cast)
                  .orElse(new Character.UnknownField(p.getName(), p.getType())),
                  com.wassynger.xcom.pooleditor.data.Property::getValue));
      for (CharacterField field : CharacterField.values())
      {
         if (field.getType() != PropertyType.STRUCT)
         {
            map.put(field, computeValue(field));
         }
      }
      StructPropertyValue value = (StructPropertyValue) map.get(CharacterField.APPEARANCE);
      if (value != null)
      {
         List<com.wassynger.xcom.pooleditor.data.Property> properties = new ArrayList<>();
         for (AppearanceField field : AppearanceField.values())
         {
            if (propertyMap.containsKey(field))
            {
               properties.add(new com.wassynger.xcom.pooleditor.data.Property(field.getType(), field.getName(),
                     computeValue(field)));
            }
         }
         map.put(CharacterField.APPEARANCE, new StructPropertyValue(value.getStructType(), properties));
      }
      return Character.fromProperties(map.entrySet()
            .stream()
            .map(e -> new com.wassynger.xcom.pooleditor.data.Property(e.getKey().getType(), e.getKey().getName(),
                  e.getValue()))
            .collect(Collectors.toList()));
   }

   private PropertyValue computeValue(PropertyField field)
   {
      Property<?> property = propertyMap.get(field);
      switch (field.getType())
      {
      case BOOL:
         return new BoolPropertyValue(((BooleanProperty) property).get());
      case INT:
         return new IntPropertyValue(((IntegerProperty) property).get());
      case STRING:
      case NAME:
         return new StringPropertyValue(((StringProperty) property).get());
      default:
         throw new AssertionError(String.format("Unhandled type: %s", field.getType()));
      }
   }

   public BooleanProperty boolProperty(PropertyField field)
   {
      return (BooleanProperty) propertyMap.computeIfAbsent(field,
            f -> new SimpleBooleanProperty(this, field.getName()));
   }

   public IntegerProperty intProperty(PropertyField field)
   {
      return (IntegerProperty) propertyMap.computeIfAbsent(field,
            f -> new SimpleIntegerProperty(this, field.getName()));
   }

   public StringProperty strProperty(PropertyField field)
   {
      return (StringProperty) propertyMap.computeIfAbsent(field, f -> new SimpleStringProperty(this, field.getName()));
   }

   public boolean isEdited()
   {
      return edited.get();
   }

   public ReadOnlyBooleanProperty editedProperty()
   {
      return edited.getReadOnlyProperty();
   }
}

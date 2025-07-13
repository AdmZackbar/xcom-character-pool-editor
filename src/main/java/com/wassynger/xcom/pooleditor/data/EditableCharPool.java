package com.wassynger.xcom.pooleditor.data;

import java.util.stream.Collectors;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EditableCharPool
{
   public static EditableCharPool create(CharacterPool characterPool)
   {
      return new EditableCharPool(characterPool);
   }

   private final CharacterPool basePool;
   private final ObservableList<EditableCharacter> characters;
   private final ReadOnlyBooleanWrapper edited;

   private EditableCharPool(CharacterPool basePool)
   {
      this.basePool = basePool;
      this.characters = FXCollections.observableArrayList(c -> new Observable[] { c.editedProperty() });
      characters.addAll(basePool.getCharacters().stream().map(EditableCharacter::create).collect(Collectors.toList()));
      this.edited = new ReadOnlyBooleanWrapper(this, "edited", false);
      edited.bind(Bindings.createBooleanBinding(this::computeEdited, characters));
   }

   private boolean computeEdited()
   {
      // TODO also check for new or removed characters
      return characters.stream().anyMatch(EditableCharacter::isEdited);
   }

   public CharacterPool getBasePool()
   {
      return basePool;
   }

   public ObservableList<EditableCharacter> getCharacters()
   {
      return characters;
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

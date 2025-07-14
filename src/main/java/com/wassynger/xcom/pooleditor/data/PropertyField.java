package com.wassynger.xcom.pooleditor.data;

/**
 * Describes a {@link Property} with a name and {@link PropertyType}.
 *
 * @author Zach Wassynger
 */
public interface PropertyField
{
   /**
    * Looks up and returns a known field, if it exists. Otherwise, creates a
    * custom field for the given parameters.
    *
    * @param name the given name, non-null
    * @param type the given type, non-null
    * @return the field
    * @throws NullPointerException if any args are null
    */
   static PropertyField get(String name, PropertyType type)
   {
      return CharacterField.get(name)
            .map(PropertyField.class::cast)
            .orElse(AppearanceField.get(name)
                  .map(PropertyField.class::cast)
                  .orElse(new UnknownPropertyField(name, type)));
   }

   /**
    * Returns the name.
    *
    * @return the name
    */
   String getName();

   /**
    * Returns the type.
    *
    * @return the type
    */
   PropertyType getType();
}

package com.wassynger;

import java.util.Optional;

/**
 * Describes an enum with a fixed set of values. All enum entries are associated
 * with an integer value.
 *
 * @author Zach Wassynger
 */
public interface StaticEnum
{
   /**
    * Attempts to find and return the enum value associated with the given
    * integer value.
    *
    * @param cls   the enum class type
    * @param value the given value to check for
    * @return the enum if found, empty optional otherwise
    * @param <T> the enum type
    */
   static <T extends Enum<T> & StaticEnum> Optional<T> fromValue(Class<T> cls, int value)
   {
      for (T item : cls.getEnumConstants())
      {
         if (item.getValue() == value)
         {
            return Optional.of(item);
         }
      }
      return Optional.empty();
   }

   /**
    * Returns the integer value associated with this enum value.
    *
    * @return the integer value
    */
   int getValue();

   /**
    * Returns a friendly localized string of this enum value.
    *
    * @return the associated localized string
    */
   String getLocalizedString();
}

package com.wassynger;

import java.io.IOException;

/**
 * Container for the object data within a {@link Property}. Each value must be
 * able to provide the information below.
 *
 * @author Zach Wassynger
 */
public interface PropertyValue
{
   /**
    * Returns a friendly, human-readable string that represents the data.
    *
    * @return the display string
    */
   String getDisplayValue();

   /**
    * Writes this data to the given writer.
    *
    * @param writer the given writer, non-null
    * @throws IOException          if some error occurred while writing
    * @throws NullPointerException if writer is null
    */
   void write(PropertyWriter writer) throws IOException;

   /**
    * Returns the size of this data when serialized in bytes.
    *
    * @return the size in bytes
    */
   int length();
}

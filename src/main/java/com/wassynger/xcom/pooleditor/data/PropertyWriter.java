package com.wassynger.xcom.pooleditor.data;

import java.io.IOException;

/**
 * Has the ability to serialize {@linkplain Property properties} to a data
 * stream.
 *
 * @author Zach Wassynger
 */
public interface PropertyWriter extends AutoCloseable
{
   /**
    * Writes the given property. Starts by writing the name of the property,
    * followed by padding. Then the name of the property type is written, and
    * finally the value. If the property is null, 'None' is written.
    *
    * @param property the given property
    * @throws IOException if some error occurred while writing to file
    * @see #writeNone()
    */
   void write(Property property) throws IOException;

   /**
    * Writes the byte. Only the single byte is written.
    *
    * @param value the given byte to write
    * @throws IOException if some error occurred while writing to file
    */
   void write(byte value) throws IOException;

   /**
    * Writes the given integer. 4 bytes are written.
    *
    * @param value the given value to write
    * @throws IOException if some error occurred while writing to file
    */
   void write(int value) throws IOException;

   /**
    * Writes the given string in the ASCII format, along with a null terminator.
    * Prepended by the length of the string (with the null terminator). If the
    * given string is null or empty, the length of '4' is written, then '0' is
    * written (no null terminator is written or accounted for).
    *
    * @param str the given string to write
    * @throws IOException if some error occurred while writing to file
    */
   void write(String str) throws IOException;

   /**
    * Writes padding in the form of an integer (0).
    *
    * @throws IOException if some error occurred while writing to file
    */
   void writePadding() throws IOException;

   /**
    * Writes 'None' as a string. Also writes padding after.
    *
    * @throws IOException if some error occurred while writing to file
    * @see #write(String)
    */
   void writeNone() throws IOException;

   @Override
   void close() throws IOException;
}

package com.wassynger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class Parser
{
   /**
    * Reads in a character pool file and returns the set of characters within.
    *
    * @param path the file containing character pool data
    * @return the loaded character pool
    * @throws IOException              if an error occurred while reading the file
    * @throws IllegalArgumentException if a parsing error occurred
    */
   public static CharacterPool load(Path path) throws IOException
   {
      Parser parser = new Parser(ByteBuffer.wrap(Files.readAllBytes(path)).order(ByteOrder.LITTLE_ENDIAN));
      parser.readHeader();
      Property property = parser.next();
      List<Property> properties = new ArrayList<>();
      while (parser.bb.hasRemaining())
      {
         properties.add(property);
         property = parser.next();
      }
      properties.add(property);
      assert parser.startPos.isEmpty();
      return new CharacterPool(path.getFileName().toString(), properties.stream()
            .filter(Objects::nonNull)
            .filter(p -> "CharacterPool".equals(p.getName()))
            .map(p -> ((List<?>) p.getData()).stream().map(Property.class::cast).collect(Collectors.toList()))
            .flatMap(List::stream)
            .map(p -> new Character(
                  ((List<?>) p.getData()).stream().map(Property.class::cast).collect(Collectors.toList())))
            .collect(Collectors.toList()));
   }

   private final ByteBuffer bb;
   // Contains the start positions of all tracked blobs
   private final Deque<Integer> startPos;

   private Parser(ByteBuffer bb)
   {
      this.bb = bb;
      this.startPos = new ArrayDeque<>();
   }

   /**
    * Reads in the expected starting header of 0xFFFFFFFF.
    */
   public void readHeader()
   {
      assertIntValue(0xFFFFFFFF);
   }

   /**
    * Reads in the next property. Can be null if 'None' is the next token, or if
    * there is nothing else to read in.
    *
    * @return the next property
    */
   public Property next()
   {
      if (!bb.hasRemaining())
      {
         return null;
      }
      String name = getAscii();
      if ("None".equals(name))
      {
         // 'None' is always followed by padding
         readPadding();
         return null;
      }
      readPadding();
      Property.Type type = Property.Type.get(getAscii());
      readPadding();
      switch (type)
      {
      case BOOL:
         return readBool(name);
      case INT:
         return readInt(name);
      case STRING:
         return readString(name);
      case NAME:
         return readName(name);
      case STRUCT:
         return readStruct(name);
      case ARRAY:
         return readArray(name);
      default:
         throw new AssertionError(String.format("Unhandled type: %s", type));
      }
   }

   private Property readBool(String name)
   {
      // Size (always 0)
      assertIntValue(0);
      readPadding();
      boolean value = getBool();
      return new Property(name, Property.Type.BOOL, value);
   }

   private Property readInt(String name)
   {
      // Size (always 4)
      assertIntValue(Integer.BYTES);
      readPadding();
      int value = getInt();
      return new Property(name, Property.Type.INT, value);
   }

   private Property readString(String name)
   {
      // String size + 4 (1x padding)
      int size = getInt();
      markStart();
      readPadding();
      String value = getAscii();
      assertSize(size + Integer.BYTES);
      return new Property(name, Property.Type.STRING, value);
   }

   private Property readName(String name)
   {
      // String size + 8 (2x padding)
      int size = getInt();
      markStart();
      readPadding();
      String value = getAscii();
      Property property = new Property(name, Property.Type.NAME, value);
      // Don't know what this value is, but it's not always 0
      getInt();
      assertSize(size + Integer.BYTES);
      return property;
   }

   private Property readStruct(String name)
   {
      // length of struct?
      int size = getInt();
      readPadding();
      // Struct class name
      getAscii();
      readPadding();
      // Size of struct starts after the class name and padding
      markStart();
      List<Property> properties = new ArrayList<>();
      Property property = next();
      while (property != null)
      {
         properties.add(property);
         property = next();
      }
      assertSize(size);
      return new Property(name, Property.Type.STRUCT, properties);
   }

   private Property readArray(String name)
   {
      // Size - 4 for character pool array, otherwise num bytes?
      getInt();
      readPadding();
      int numElements = getInt();
      Property property;
      // Don't know why this is, but the character pool array also contains some
      // information before the array of characters. For now keep this hardcoded
      if ("CharacterPool".equals(name))
      {
         List<Property> headers = new ArrayList<>();
         property = next();
         while (property != null)
         {
            headers.add(property);
            property = next();
         }
         // number of elements is repeated after the initial section
         assertIntValue(numElements);
      }
      List<Property> children = new ArrayList<>();
      for (int i = 0; i < numElements; i++)
      {
         property = next();
         List<Property> properties = new ArrayList<>();
         while (property != null)
         {
            properties.add(property);
            property = next();
         }
         // Wrap the properties in a struct property
         children.add(new Property(String.format("Child %d", i), Property.Type.STRUCT, properties));
      }
      return new Property(name, Property.Type.ARRAY, children);
   }

   /* ******* *
    * HELPERS *
    * ******* */

   /**
    * Reads in an expected 4 byte padding block of 0x00000000.
    */
   private void readPadding()
   {
      assertIntValue(0);
   }

   /**
    * Reads in a 4 byte integer.
    *
    * @return the next int value
    */
   private int getInt()
   {
      assertHasData(Integer.BYTES);
      return bb.getInt();
   }

   /**
    * Reads in an ASCII string by first reading in the 'length' of the incoming
    * string, then reading in that number of characters.
    *
    * @return the next ASCII string
    */
   private String getAscii()
   {
      int len = getInt();
      if (len < 0)
      {
         throw new IllegalArgumentException(String.format("invalid string length: %d", len));
      }
      if (len == 0)
      {
         // Nothing else to read in
         return "";
      }
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < len - 1; i++)
      {
         sb.append((char) bb.get());
      }
      // Should end with null terminator
      assertByteValue((byte) '\0');
      return sb.toString();
   }

   /**
    * Reads in a single byte and interprets it as a boolean value.
    *
    * @return the next boolean value
    */
   private boolean getBool()
   {
      assertHasData(Byte.BYTES);
      return bb.get() != 0;
   }

   /**
    * Checks if there is enough data to read in. Throws an exception if not.
    *
    * @param size the amount of data to read in
    */
   private void assertHasData(int size)
   {
      if (bb.remaining() < size)
      {
         throw new IllegalArgumentException(
               String.format("Missing data, expected %d bytes (got %d)", size, bb.remaining()));
      }
   }

   /**
    * Reads in an integer and checks if it matches the given value. Throws an
    * exception if not.
    *
    * @param expected the expected integer value
    */
   private void assertIntValue(int expected)
   {
      assertHasData(Integer.BYTES);
      int actual = bb.getInt();
      if (actual != expected)
      {
         throw new IllegalArgumentException(
               String.format("Pos %d: unexpected int value '0x%08X', expected '0x%08X'", bb.position() - Integer.BYTES,
                     actual, expected));
      }
   }

   /**
    * Reads in a byte and checks if it matches the given value. Throws an
    * exception if not.
    *
    * @param expected the expected byte value
    */
   private void assertByteValue(byte expected)
   {
      assertHasData(Byte.BYTES);
      byte actual = bb.get();
      if (actual != expected)
      {
         throw new IllegalArgumentException(
               String.format("Pos %d: unexpected byte value '0x%02X', expected '0x%02X'", bb.position() - Byte.BYTES,
                     actual, expected));
      }
   }

   /**
    * Marks the start of a blob.
    *
    * @see #assertSize(int)
    */
   private void markStart()
   {
      startPos.push(bb.position());
   }

   /**
    * Marks the end of a blob and asserts that the size of the blob matches what
    * is expected. Throws an exception if it does not match.
    *
    * @param expected the expected size
    * @see #markStart()
    */
   private void assertSize(int expected)
   {
      int actual = bb.position() - startPos.pop();
      if (actual != expected)
      {
         throw new IllegalArgumentException(
               String.format("Pos %d: unexpected size '%d', expected '%d'", bb.position(), actual, expected));
      }
   }
}

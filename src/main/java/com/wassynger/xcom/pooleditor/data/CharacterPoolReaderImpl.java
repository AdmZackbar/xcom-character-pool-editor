package com.wassynger.xcom.pooleditor.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Reads in {@link CharacterPool} and associated properties from a file.
 *
 * @author Zach Wassynger
 */
class CharacterPoolReaderImpl implements CharacterPoolReader
{
   private final Path path;
   private final String fileName;
   private final InputStream is;
   // Contains the start positions of all tracked blobs
   private final Deque<Integer> startPos;

   private int pos = 0;
   private boolean hasRemaining = true;

   CharacterPoolReaderImpl(Path path) throws IOException
   {
      this.path = Objects.requireNonNull(path);
      // Remove trailing extension
      this.fileName = path.getFileName().toString().replaceFirst("\\..+$", "");
      this.is = Files.newInputStream(path);
      this.startPos = new ArrayDeque<>();
   }

   /**
    * Reads in the character pool from the internal file.
    *
    * @return the parsed character pool
    * @throws IOException if some error occurred while reading the file
    */
   @Override
   public CharacterPool read() throws IOException
   {
      readHeader();
      Property property = readProperty();
      List<Property> properties = new ArrayList<>();
      while (hasRemaining)
      {
         properties.add(property);
         property = readProperty();
      }
      properties.add(property);
      assert startPos.isEmpty();
      ArrayPropertyValue charPoolArrayProperty = properties.stream()
            .filter(p -> p.getType() == PropertyType.ARRAY)
            .filter(p -> "CharacterPool".equals(p.getName()))
            .map(p -> ((ArrayPropertyValue) p.getValue()))
            .findFirst()
            .orElse(null);
      if (charPoolArrayProperty == null)
      {
         return null;
      }
      String filePath = charPoolArrayProperty.getHeader("PoolFileName")
            .map(p -> p.getValue().getDisplayValue())
            .orElse(fileName);
      List<Character> characters = charPoolArrayProperty.getEntries()
            .stream()
            .map(ArrayPropertyValue.Entry::getProperties)
            .map(Character::fromProperties)
            .collect(Collectors.toList());
      return new CharacterPool(path, fileName, filePath, characters);
   }

   private void readHeader() throws IOException
   {
      readAndCheckValue(0xFFFFFFFF);
   }

   public Property readProperty() throws IOException
   {
      if (!hasRemaining)
      {
         return null;
      }
      try
      {
         String name = readString();
         readPadding();
         if ("None".equals(name))
         {
            return null;
         }
         PropertyType type = PropertyType.get(readString());
         readPadding();
         PropertyValue value = readValue(name, type);
         return new Property(type, name, value);
      }
      catch (IOException e)
      {
         if (!hasRemaining)
         {
            return null;
         }
         throw e;
      }
   }

   private PropertyValue readValue(String name, PropertyType type) throws IOException
   {
      switch (type)
      {
      case BOOL:
         return readBoolValue();
      case INT:
         return readIntValue();
      case STRING:
         return readStringValue();
      case NAME:
         return readNameValue();
      case STRUCT:
         return readStructValue();
      case ARRAY:
         // Don't know why this is, but the character pool array also contains some
         // information before the array of characters. For now keep this hardcoded
         return readArrayValue("CharacterPool".equals(name));
      default:
         throw new AssertionError(String.format("unhandled type for '%s': %s", name, type));
      }
   }

   private PropertyValue readBoolValue() throws IOException
   {
      readAndCheckValue(0);
      readPadding();
      return new BoolPropertyValue(readBool());
   }

   private PropertyValue readIntValue() throws IOException
   {
      readAndCheckValue(Integer.BYTES);
      readPadding();
      return new IntPropertyValue(readInt());
   }

   private PropertyValue readStringValue() throws IOException
   {
      // string length + 4 (padding)
      int size = readInt();
      markStartBlob();
      readPadding();
      String str = readString();
      checkBlobSize(size + Integer.BYTES);
      return new StringPropertyValue(str);
   }

   private PropertyValue readNameValue() throws IOException
   {
      // string length + 8 (padding and unknown int value)
      int size = readInt();
      markStartBlob();
      readPadding();
      String str = readString();
      int num = readInt();
      checkBlobSize(size + Integer.BYTES);
      return new NamePropertyValue(str, num);
   }

   private PropertyValue readStructValue() throws IOException
   {
      // length of entries
      int size = readInt();
      readPadding();
      String structType = readString();
      readPadding();
      // Size of struct starts after the class name and padding
      markStartBlob();
      List<Property> entries = new ArrayList<>();
      Property property = readProperty();
      while (property != null)
      {
         entries.add(property);
         property = readProperty();
      }
      checkBlobSize(size);
      return new StructPropertyValue(structType, entries);
   }

   private PropertyValue readArrayValue(boolean parseHeaders) throws IOException
   {
      // size is 4 for character pool, but otherwise is the actual data len?
      // TODO figure out how to handle better
      int size = readInt();
      readPadding();
      int numElements = readInt();
      Property property;
      List<Property> headers = new ArrayList<>();
      if (parseHeaders)
      {
         property = readProperty();
         while (property != null)
         {
            headers.add(property);
            property = readProperty();
         }
         // number of elements is repeated after the initial section
         int numElements2 = readInt();
         if (numElements != numElements2)
         {
            throw new IOException(
                  String.format("pos %d: expected num elements to be equal: %d != %d", pos - Integer.BYTES, numElements,
                        numElements2));
         }
      }
      List<ArrayPropertyValue.Entry> entries = new ArrayList<>();
      for (int i = 0; i < numElements; i++)
      {
         property = readProperty();
         List<Property> properties = new ArrayList<>();
         while (property != null)
         {
            properties.add(property);
            property = readProperty();
         }
         entries.add(new ArrayPropertyValue.Entry(properties));
      }
      return new ArrayPropertyValue(headers, entries);
   }

   public boolean readBool() throws IOException
   {
      int actual = is.read();
      if (actual == -1)
      {
         hasRemaining = false;
         throw new IOException(String.format("pos %d: missing data for bool", pos));
      }
      pos++;
      return actual != 0;
   }

   public int readInt() throws IOException
   {
      int len = Integer.BYTES;
      byte[] data = new byte[len];
      int actual = is.read(data);
      if (actual != len)
      {
         if (actual == -1)
         {
            hasRemaining = false;
         }
         throw new IOException(String.format("pos %d: missing data for int, got %d", pos, actual));
      }
      pos += actual;
      return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
   }

   public String readString() throws IOException
   {
      int len = readInt();
      if (len == 0)
      {
         // Nothing else to read
         return "";
      }
      byte[] data = new byte[len];
      int actual = is.read(data);
      if (actual != len)
      {
         if (actual == -1)
         {
            hasRemaining = false;
         }
         throw new IOException(
               String.format("pos %d: missing data for string; expected %d bytes, got %d", pos, len, actual));
      }
      pos += actual;
      if (data[len - 1] != '\0')
      {
         throw new IOException(
               String.format("pos %d: expected '\\0' to end string, got %c", pos - Byte.BYTES, data[len - 1]));
      }
      return new String(data, 0, len - 1, StandardCharsets.UTF_8);
   }

   public void readPadding() throws IOException
   {
      int value = readInt();
      if (value != 0)
      {
         throw new IOException(String.format("pos %d: expected padding, got 0x%08X", pos - Integer.BYTES, value));
      }
   }

   @Override
   public void close() throws IOException
   {
      is.close();
   }

   private void readAndCheckValue(int expected) throws IOException
   {
      int actual = readInt();
      if (actual != expected)
      {
         throw new IOException(
               String.format("pos %d: unexpected int value 0x%08X, expected 0x%08X", pos - Integer.BYTES, actual,
                     expected));
      }
   }

   /**
    * Marks the start of a blob.
    *
    * @see #checkBlobSize(int)
    */
   private void markStartBlob()
   {
      startPos.push(pos);
   }

   /**
    * Marks the end of a blob and asserts that the size of the blob matches what
    * is expected. Throws an exception if it does not match.
    *
    * @param expected the expected size
    * @see #markStartBlob()
    */
   private void checkBlobSize(int expected)
   {
      int actual = pos - startPos.pop();
      if (actual != expected)
      {
         throw new IllegalArgumentException(
               String.format("pos %d: unexpected size '%d', expected '%d'", pos, actual, expected));
      }
   }
}

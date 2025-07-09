package com.wassynger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Parser
{
   private static final byte HEADER_BYTE = (byte) 0xFF;
   private static final byte[] EXPECTED_HEADER = new byte[] { HEADER_BYTE, HEADER_BYTE, HEADER_BYTE, HEADER_BYTE };

   public static CharacterPool load(Path path) throws IOException
   {
      Parser parser = new Parser(ByteBuffer.wrap(Files.readAllBytes(path)).order(ByteOrder.LITTLE_ENDIAN));
      parser.readHeader();
      // 'character pool'
      parser.readAscii();
      parser.readPadding();
      // 'array property'
      parser.readAscii();
      parser.readPadding();
      // TODO figure out numbers here
      parser.readInt();
      parser.readPadding();
      parser.readInt();
      // 'pool file name'
      parser.readAscii();
      parser.readPadding();
      // 'str property'
      parser.readAscii();
      parser.readPadding();
      // name property type length
      parser.readInt();
      parser.readPadding();
      // pool name
      parser.readAscii();
      // 'none'
      parser.readAscii();
      // TODO figure out this int
      parser.readPadding();
      parser.readInt();
      // Characters
      List<Character> characters = new ArrayList<>();
      while (parser.hasRemaining())
      {
         List<Property> properties = new ArrayList<>();
         Property property = parser.parseProperty();
         while (property != null)
         {
            properties.add(property);
            property = parser.parseProperty();
         }
         characters.add(new Character(properties));
      }
      return new CharacterPool(characters);
   }

   private final ByteBuffer bb;

   private Parser(ByteBuffer bb)
   {
      this.bb = bb;
   }

   public boolean hasRemaining()
   {
      return bb.hasRemaining();
   }

   public void readHeader() throws IOException
   {
      if (bb.remaining() < EXPECTED_HEADER.length)
      {
         throw new IOException(String.format("Unexpected header: %s (expected %s)", Arrays.toString(bb.array()),
               Arrays.toString(EXPECTED_HEADER)));
      }
      byte[] header = new byte[EXPECTED_HEADER.length];
      bb.get(header);
      if (!Arrays.equals(header, EXPECTED_HEADER))
      {
         throw new IOException(String.format("Unexpected header: %s (expected %s)", Arrays.toString(header),
               Arrays.toString(EXPECTED_HEADER)));
      }
   }

   public void readPadding() throws IOException
   {
      if (bb.remaining() < 4)
      {
         throw new IOException("Missing data");
      }
      byte[] data = new byte[4];
      bb.get(data);
      // TODO check if 0?
   }

   public int readInt() throws IOException
   {
      if (bb.remaining() < 4)
      {
         throw new IOException("Not enough data for int");
      }
      return bb.getInt();
   }

   public String readAscii() throws IOException
   {
      return readAscii(readInt());
   }

   public String readAscii(int len)
   {
      if (len <= 0)
      {
         return "";
      }
      else if (len == 1)
      {
         // Read null terminator
         bb.get();
         return "";
      }
      byte[] data = new byte[len - 1];
      bb.get(data);
      // Read null terminator
      bb.get();
      return new String(data, StandardCharsets.US_ASCII);
   }

   public boolean readBool()
   {
      return bb.get() != 0;
   }

   public Property parseProperty() throws IOException
   {
      String name = readAscii();
      if ("None".equals(name))
      {
         readPadding();
         return null;
      }
      readPadding();
      Property.Type type = Property.Type.get(readAscii());
      readPadding();
      switch (type)
      {
      case STRING:
         return parseString(name);
      case NAME:
         return parseName(name);
      case INT:
         return parseInt(name);
      case BOOL:
         return parseBool(name);
      case STRUCT:
         return parseStruct(name);
      // TODO ARRAY
      default:
         throw new AssertionError(String.format("Unhandled case: %s", type));
      }
   }

   private Property parseString(String name) throws IOException
   {
      // Size + 4
      readInt();
      readPadding();
      String value = readAscii();
      return new Property(name, Property.Type.STRING, value);
   }

   private Property parseName(String name) throws IOException
   {
      // Size + 8
      readInt();
      readPadding();
      String value = readAscii();
      Property property = new Property(name, Property.Type.NAME, value);
      readPadding();
      return property;
   }

   private Property parseInt(String name) throws IOException
   {
      // Size (always 4)
      readInt();
      readPadding();
      int value = readInt();
      return new Property(name, Property.Type.INT, value);
   }

   private Property parseBool(String name) throws IOException
   {
      // Size (always 0)
      readInt();
      readPadding();
      boolean value = readBool();
      return new Property(name, Property.Type.BOOL, value);
   }

   private Property parseStruct(String name) throws IOException
   {
      // length of struct?
      int len = readInt();
      readPadding();
      // Name of the struct? ignore
      String str = readAscii();
      readPadding();
      List<Property> properties = new ArrayList<>();
      Property property = parseProperty();
      while (property != null)
      {
         properties.add(property);
         property = parseProperty();
      }
      return new Property(name, Property.Type.STRUCT, properties);
   }
}

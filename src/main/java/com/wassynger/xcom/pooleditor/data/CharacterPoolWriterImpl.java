package com.wassynger.xcom.pooleditor.data;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Basic implementation of {@link CharacterPoolWriter} that writes to a file via
 * a buffered output stream. The data is written in a little endian format via
 * byte buffers.
 *
 * @author Zach Wassynger
 */
final class CharacterPoolWriterImpl implements CharacterPoolWriter, PropertyWriter
{
   private final OutputStream os;

   CharacterPoolWriterImpl(Path path) throws IOException
   {
      this.os = new BufferedOutputStream(Files.newOutputStream(Objects.requireNonNull(path)));
   }

   /**
    * Writes the given character pool to the file.
    *
    * @param pool the given pool, non-null
    * @throws IOException          if some error occurred while writing to file
    * @throws NullPointerException if the pool is null
    */
   @Override
   public void write(CharacterPool pool) throws IOException
   {
      writeHeader();
      write(new Property(PropertyField.get("CharacterPool", PropertyType.ARRAY), new ArrayPropertyValue(
            Collections.singletonList(new Property(PropertyField.get("PoolFileName", PropertyType.STRING),
                  new StringPropertyValue(pool.getFileName()))),
            pool.getCharacters().stream().map(Character::toEntry).collect(Collectors.toList()))));
   }

   private void writeHeader() throws IOException
   {
      write(0xFFFFFFFF);
   }

   @Override
   public void write(Property property) throws IOException
   {
      if (property == null)
      {
         writeNone();
         return;
      }
      write(property.getField().getName());
      writePadding();
      write(property.getField().getType().getName());
      writePadding();
      property.getValue().write(this);
   }

   @Override
   public void write(String str) throws IOException
   {
      if (str == null || str.isEmpty())
      {
         write(0);
         return;
      }
      byte[] data = str.getBytes(Property.STRING_CHARSET);
      // size + 1 (for null terminator)
      int len = data.length + 1;
      write(len);
      os.write(createBuffer(len).put(data).put((byte) 0).array());
   }

   @Override
   public void write(byte value) throws IOException
   {
      os.write(value);
   }

   @Override
   public void write(int value) throws IOException
   {
      os.write(createBuffer(Integer.BYTES).putInt(value).array());
   }

   // Create a little endian byte buffer of the given size
   private ByteBuffer createBuffer(int len)
   {
      return ByteBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN);
   }

   @Override
   public void writePadding() throws IOException
   {
      write(0x00000000);
   }

   @Override
   public void writeNone() throws IOException
   {
      write("None");
      writePadding();
   }

   @Override
   public void close() throws IOException
   {
      os.close();
   }
}

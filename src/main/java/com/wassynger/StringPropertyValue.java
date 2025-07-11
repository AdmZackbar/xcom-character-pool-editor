package com.wassynger;

import java.io.IOException;

class StringPropertyValue implements PropertyValue
{
   private final String str;

   public StringPropertyValue(String str)
   {
      this.str = str;
   }

   @Override
   public String getDisplayValue()
   {
      return str;
   }

   @Override
   public void write(PropertyWriter writer) throws IOException
   {
      // raw string length + '\0' (if not empty) + padding
      int size = (str != null && !str.isEmpty() ? str.length() + Byte.BYTES : 0) + Integer.BYTES;
      writer.write(size);
      writer.writePadding();
      writer.write(str);
   }

   @Override
   public int length()
   {
      // 'size' + padding + str
      return Integer.BYTES + Integer.BYTES + Property.computeStringNumBytes(str);
   }

   @Override
   public String toString()
   {
      return "StringPropertyValue{" + "str='" + str + '\'' + '}';
   }
}

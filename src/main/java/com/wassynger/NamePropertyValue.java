package com.wassynger;

import java.io.IOException;

class NamePropertyValue implements PropertyValue
{
   private final String str;
   private final int num;

   public NamePropertyValue(String str, int num)
   {
      this.str = str;
      this.num = num;
   }

   @Override
   public String getDisplayValue()
   {
      return str;
   }

   @Override
   public void write(PropertyWriter writer) throws IOException
   {
      // raw string length + '\0' + padding + num (int length)
      int size = str.length() + Byte.BYTES + Integer.BYTES + Integer.BYTES;
      writer.write(size);
      writer.writePadding();
      writer.write(str);
      writer.write(num);
   }

   @Override
   public int length()
   {
      // 'size' + padding + str + num (integer length)
      return Integer.BYTES + Integer.BYTES + Property.computeStringNumBytes(str) + Integer.BYTES;
   }

   @Override
   public String toString()
   {
      return "NamePropertyValue{" + "str='" + str + '\'' + ", num=" + num + '}';
   }
}

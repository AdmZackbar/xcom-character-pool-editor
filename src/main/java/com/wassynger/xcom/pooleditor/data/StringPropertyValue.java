package com.wassynger.xcom.pooleditor.data;

import java.io.IOException;
import java.util.Objects;

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
      int size = Property.computeStringNumBytes(str);
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

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof StringPropertyValue))
      {
         return false;
      }
      StringPropertyValue that = (StringPropertyValue) o;
      return Objects.equals(str, that.str);
   }

   @Override
   public int hashCode()
   {
      return Objects.hashCode(str);
   }
}

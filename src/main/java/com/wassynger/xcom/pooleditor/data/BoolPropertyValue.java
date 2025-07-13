package com.wassynger.xcom.pooleditor.data;

import java.io.IOException;
import java.util.Objects;

class BoolPropertyValue implements PropertyValue
{
   private final boolean value;

   BoolPropertyValue(boolean value)
   {
      this.value = value;
   }

   public boolean getValue()
   {
      return value;
   }

   @Override
   public String getDisplayValue()
   {
      return value ? "true" : "false";
   }

   @Override
   public void write(PropertyWriter writer) throws IOException
   {
      // size is always '0'
      writer.write(0);
      writer.writePadding();
      writer.write((byte) (value ? 1 : 0));
   }

   @Override
   public int length()
   {
      // 'size' + padding + byte value
      return Integer.BYTES + Integer.BYTES + Byte.BYTES;
   }

   @Override
   public String toString()
   {
      return "BoolPropertyValue{" + "value=" + value + '}';
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof BoolPropertyValue))
      {
         return false;
      }
      BoolPropertyValue that = (BoolPropertyValue) o;
      return value == that.value;
   }

   @Override
   public int hashCode()
   {
      return Objects.hashCode(value);
   }
}

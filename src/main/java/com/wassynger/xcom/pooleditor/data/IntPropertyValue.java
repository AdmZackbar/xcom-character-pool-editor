package com.wassynger.xcom.pooleditor.data;

import java.io.IOException;
import java.util.Objects;

class IntPropertyValue implements PropertyValue
{
   private final int value;

   public IntPropertyValue(int value)
   {
      this.value = value;
   }

   public int getValue()
   {
      return value;
   }

   @Override
   public String getDisplayValue()
   {
      // TODO improve?
      return Integer.toString(value);
   }

   @Override
   public void write(PropertyWriter writer) throws IOException
   {
      // size is always '4'
      writer.write(Integer.BYTES);
      writer.writePadding();
      writer.write(value);
   }

   @Override
   public int length()
   {
      // 'size' + padding + int value
      return Integer.BYTES + Integer.BYTES + Integer.BYTES;
   }

   @Override
   public String toString()
   {
      return "IntPropertyValue{" + "value=" + value + '}';
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof IntPropertyValue))
      {
         return false;
      }
      IntPropertyValue that = (IntPropertyValue) o;
      return value == that.value;
   }

   @Override
   public int hashCode()
   {
      return Objects.hashCode(value);
   }
}

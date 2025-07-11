package com.wassynger;

import java.io.IOException;

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
}

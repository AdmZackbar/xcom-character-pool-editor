package com.wassynger.xcom.pooleditor.data;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

class StructPropertyValue implements PropertyValue
{
   private final String structType;
   private final List<Property> entries;

   public StructPropertyValue(String structType, List<Property> entries)
   {
      this.structType = structType;
      this.entries = entries != null ? entries : Collections.emptyList();
   }

   public String getStructType()
   {
      return structType;
   }

   public List<Property> getEntries()
   {
      return entries;
   }

   @Override
   public String getDisplayValue()
   {
      if (entries.isEmpty())
      {
         return "[]";
      }
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (Property entry : entries)
      {
         sb.append(entry.getField().getName()).append(":").append(entry.getValue()).append(",");
      }
      // Remove trailing comma and end array
      sb.deleteCharAt(sb.length() - 1).append("]");
      return sb.toString();
   }

   @Override
   public void write(PropertyWriter writer) throws IOException
   {
      // total length (except this size integer)
      int size = entries.stream().mapToInt(Property::computeLength).sum() + Property.NONE_NUM_BYTES;
      writer.write(size);
      writer.writePadding();
      writer.write(structType);
      writer.writePadding();
      for (Property entry : entries)
      {
         writer.write(entry);
      }
      // End with a 'None'
      writer.writeNone();
   }

   @Override
   public int length()
   {
      int childrenSize = entries.stream().mapToInt(Property::computeLength).sum();
      // size + padding + struct name + padding + children + 'None' + padding
      return Integer.BYTES + Integer.BYTES + Property.computeStringNumBytes(structType) + Integer.BYTES + childrenSize +
             Property.NONE_NUM_BYTES;
   }

   @Override
   public String toString()
   {
      return "StructPropertyValue{" + "structType='" + structType + '\'' + ", entries=" + entries + '}';
   }
}

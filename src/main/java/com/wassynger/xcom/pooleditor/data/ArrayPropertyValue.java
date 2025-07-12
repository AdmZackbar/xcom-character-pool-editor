package com.wassynger.xcom.pooleditor.data;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class ArrayPropertyValue implements PropertyValue
{
   private final List<Property> headers;
   private final List<Entry> entries;

   public ArrayPropertyValue(List<Property> headers, List<Entry> entries)
   {
      this.headers = headers;
      this.entries = entries;
   }

   public Optional<Property> getHeader(String name)
   {
      return headers.stream().filter(p -> Objects.equals(name, p.getName())).findFirst();
   }

   public List<Entry> getEntries()
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
      for (Entry entry : entries)
      {
         if (entry.properties.isEmpty())
         {
            sb.append("{}").append(",");
            continue;
         }
         sb.append("{");
         for (Property property : entry.properties)
         {
            sb.append(property.getName()).append(":").append(property.getValue()).append(",");
         }
         // Remove trailing comma and end object
         sb.deleteCharAt(sb.length() - 1).append('}').append(',');
      }
      // Remove trailing comma and end array
      sb.deleteCharAt(sb.length() - 1).append("]");
      return sb.toString();
   }

   @Override
   public void write(PropertyWriter writer) throws IOException
   {
      // size
      // TODO 4 or length?
      writer.write(4);
      writer.writePadding();
      writer.write(entries.size());
      if (!headers.isEmpty())
      {
         for (Property header : headers)
         {
            writer.write(header);
         }
         writer.writeNone();
         writer.write(entries.size());
      }
      for (Entry entry : entries)
      {
         for (Property property : entry.properties)
         {
            writer.write(property);
         }
         // End each entry with 'None'
         writer.writeNone();
      }
      // End with a 'None' if we had no headers
      if (headers.isEmpty())
      {
         writer.writeNone();
      }
   }

   @Override
   public int length()
   {
      // size + padding + num entries (int) + headers + none +
      // (num entries if headers exist) + entries + none
      int headerSize = headers.isEmpty() ? 0 : headers.stream().mapToInt(Property::computeLength).sum() + Property.NONE_NUM_BYTES;
      int entriesSize = entries.stream().mapToInt(Entry::computeLength).sum();
      return Integer.BYTES + Integer.BYTES + Integer.BYTES + headerSize + entriesSize + Property.NONE_NUM_BYTES;
   }

   @Override
   public String toString()
   {
      return "ArrayPropertyValue{" + "headers=" + headers + ", entries=" + entries + '}';
   }

   public static class Entry
   {
      private final List<Property> properties;

      public Entry(List<Property> properties)
      {
         this.properties = properties;
      }

      public List<Property> getProperties()
      {
         return properties;
      }

      public int computeLength()
      {
         return properties.stream().mapToInt(Property::computeLength).sum() + Property.NONE_NUM_BYTES;
      }

      @Override
      public String toString()
      {
         return "Entry{" + "properties=" + properties + '}';
      }
   }
}

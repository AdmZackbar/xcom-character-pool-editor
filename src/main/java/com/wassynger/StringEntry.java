package com.wassynger;

import java.util.Objects;

public final class StringEntry
{
   private final String str;
   private final String localized;
   private final boolean supported;

   public StringEntry(String str, String localized, boolean supported)
   {
      this.str = str;
      this.localized = localized;
      this.supported = supported;
   }

   public String getStr()
   {
      return str;
   }

   public String getLocalized()
   {
      return localized;
   }

   public boolean isSupported()
   {
      return supported;
   }

   @Override
   public String toString()
   {
      return "StringEntry{" + "str='" + str + '\'' + ", localized='" + localized + '\'' + ", supported=" + supported +
             '}';
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof StringEntry))
      {
         return false;
      }
      StringEntry that = (StringEntry) o;
      return supported == that.supported && Objects.equals(str, that.str) && Objects.equals(localized, that.localized);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(str, localized, supported);
   }
}

package com.wassynger;

public enum Race implements StaticEnum
{
   CAUCASIAN(0, "Caucasian"),
   AFRICAN(1, "African"),
   EAST_ASIAN(2, "East Asian"),
   HISPANIC(3, "Hispanic");

   private final int value;
   private final String loc;

   Race(int value, String loc)
   {
      this.value = value;
      this.loc = loc;
   }

   @Override
   public int getValue()
   {
      return value;
   }

   @Override
   public String getLocalizedString()
   {
      return loc;
   }
}

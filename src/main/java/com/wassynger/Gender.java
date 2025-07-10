package com.wassynger;

public enum Gender implements StaticEnum
{
   MALE(1, "Male"),
   FEMALE(2, "Female");

   private final int value;
   private final String loc;

   Gender(int value, String loc)
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

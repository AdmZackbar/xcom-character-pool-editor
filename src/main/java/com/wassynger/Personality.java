package com.wassynger;

public enum Personality implements StaticEnum
{
   BY_THE_BOOK(0,"By The Book"),
   LAID_BACK(1,"Laid Back"),
   NORMAL(2,"Normal"),
   TWITCHY(3,"Twitchy"),
   HAPPY_GO_LUCKY(4,"Happy Go Lucky"),
   HARD_LUCK(5,"Hard Luck"),
   INTENSE(6,"Intense"),
   // New with TLP
   ANGRY(7,"Angry"),
   COCKY(8,"Cocky"),
   SUSPICIOUS(9,"Suspicious"),
   SMUG(10,"Smug"),
   LETS_GO(11,"Let's Go!");

   private final int value;
   private final String loc;

   Personality(int value, String loc)
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

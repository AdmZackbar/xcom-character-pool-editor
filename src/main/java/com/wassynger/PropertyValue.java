package com.wassynger;

import java.io.IOException;

public interface PropertyValue
{
   String getDisplayValue();

   void write(PropertyWriter writer) throws IOException;

   int length();
}

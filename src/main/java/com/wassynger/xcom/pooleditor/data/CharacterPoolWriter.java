package com.wassynger.xcom.pooleditor.data;

import java.io.IOException;
import java.nio.file.Path;

public interface CharacterPoolWriter extends AutoCloseable
{
   static CharacterPoolWriter open(Path file) throws IOException
   {
      return new CharacterPoolWriterImpl(file);
   }

   void write(CharacterPool pool) throws IOException;

   @Override
   void close() throws IOException;
}

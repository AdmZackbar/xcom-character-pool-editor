package com.wassynger.xcom.pooleditor.data;

import java.io.IOException;
import java.nio.file.Path;

public interface CharacterPoolReader extends AutoCloseable
{
   static CharacterPoolReader open(Path path) throws IOException
   {
      return new CharacterPoolReaderImpl(path);
   }

   CharacterPool read() throws IOException;

   @Override
   void close() throws IOException;
}

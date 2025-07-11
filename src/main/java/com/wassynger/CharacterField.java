package com.wassynger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum CharacterField
{
   FIRST_NAME("strFirstName", PropertyType.STRING),
   LAST_NAME("strLastName", PropertyType.STRING),
   NICKNAME("strNickName", PropertyType.STRING),
   COUNTRY("Country", PropertyType.NAME),
   BIOGRAPHY("BackgroundText", PropertyType.STRING),
   CLASS("m_SoldierClassTemplateName", PropertyType.NAME),
   TEMPLATE("CharacterTemplateName", PropertyType.NAME),
   IS_SOLDIER("AllowedTypeSoldier", PropertyType.BOOL),
   IS_VIP("AllowedTypeVIP", PropertyType.BOOL),
   IS_DARK_VIP("AllowedTypeDarkVIP", PropertyType.BOOL),
   CREATION_DATE("PoolTimestamp", PropertyType.STRING),
   APPEARANCE("kAppearance", PropertyType.STRUCT);

   private static final Map<String, CharacterField> FIELD_MAP;

   static
   {
      FIELD_MAP = new HashMap<>();
      for (CharacterField property : CharacterField.values())
      {
         FIELD_MAP.put(property.getRaw(), property);
      }
   }

   public static Optional<CharacterField> get(String str)
   {
      return Optional.ofNullable(FIELD_MAP.get(str));
   }

   private final String raw;
   private final PropertyType type;

   CharacterField(String raw, PropertyType type)
   {
      this.raw = raw;
      this.type = type;
   }

   public String getRaw()
   {
      return raw;
   }

   public PropertyType getType()
   {
      return type;
   }
}

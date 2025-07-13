package com.wassynger.xcom.pooleditor.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum CharacterField implements PropertyField
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
         FIELD_MAP.put(property.getName(), property);
      }
   }

   public static Optional<CharacterField> get(String str)
   {
      return Optional.ofNullable(FIELD_MAP.get(str));
   }

   private final String name;
   private final PropertyType type;

   CharacterField(String name, PropertyType type)
   {
      this.name = name;
      this.type = type;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public PropertyType getType()
   {
      return type;
   }
}

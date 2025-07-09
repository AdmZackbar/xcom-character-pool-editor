package com.wassynger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum CharacterProperty
{
   FIRST_NAME(Property.Type.STRING, "strFirstName", "First Name"),
   LAST_NAME(Property.Type.STRING, "strLastName", "Last Name"),
   NICKNAME(Property.Type.STRING, "strNickName", "Nickname"),
   COUNTRY(Property.Type.NAME, "Country", "Country"),
   BIOGRAPHY(Property.Type.STRING, "BackgroundText", "Biography"),
   CLASS(Property.Type.NAME, "m_SoldierClassTemplateName", "Soldier Class"),
   TEMPLATE(Property.Type.NAME, "CharacterTemplateName", "Character Template"),
   IS_SOLDIER(Property.Type.BOOL, "AllowedTypeSoldier", "Can Be Soldier"),
   IS_VIP(Property.Type.BOOL, "AllowedTypeVIP", "Can Be VIP"),
   IS_DARK_VIP(Property.Type.BOOL, "AllowedTypeDarkVIP", "Can Be Dark VIP"),
   CREATION_DATE(Property.Type.STRING, "PoolTimestamp", "Created On"),
   APPEARANCE(Property.Type.STRUCT, "kAppearance", "Appearance");

   private static final Map<String, CharacterProperty> RAW_PROPERTY_MAP;

   static
   {
      RAW_PROPERTY_MAP = new HashMap<>();
      for (CharacterProperty property : CharacterProperty.values())
      {
         RAW_PROPERTY_MAP.put(property.getRaw(), property);
      }
   }

   public static Optional<CharacterProperty> get(String str)
   {
      return Optional.ofNullable(RAW_PROPERTY_MAP.get(str));
   }

   private final Property.Type type;
   private final String raw;
   private final String loc;

   CharacterProperty(Property.Type type, String raw, String loc)
   {
      this.type = type;
      this.raw = raw;
      this.loc = loc;
   }

   public String getRaw()
   {
      return raw;
   }

   public String getLocalized()
   {
      return loc;
   }
}

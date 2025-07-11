package com.wassynger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum AppearanceField
{
   HEAD("nmHead", PropertyType.NAME),
   GENDER("iGender", PropertyType.INT),
   RACE("iRace", PropertyType.INT),
   HAIRCUT("nmHaircut", PropertyType.NAME),
   HAIR_COLOR("iHairColor", PropertyType.INT),
   FACIAL_HAIR("iFacialHair", PropertyType.INT),
   BEARD("nmBeard", PropertyType.NAME),
   SKIN_COLOR("iSkinColor", PropertyType.INT),
   EYE_COLOR("iEyeColor", PropertyType.INT),
   FLAG("nmFlag", PropertyType.NAME),
   VOICE("iVoice", PropertyType.INT),
   ATTITUDE("iAttitude", PropertyType.INT),
   ARMOR_DECO("iArmorDeco", PropertyType.INT),
   ARMOR_TINT("iArmorTint", PropertyType.INT),
   ARMOR_TINT_SECONDARY("iArmorTintSecondary", PropertyType.INT),
   WEAPON_TINT("iWeaponTint", PropertyType.INT),
   TATTOO_TINT("iTattooTint", PropertyType.INT),
   WEAPON_PATTERN("nmWeaponPattern", PropertyType.NAME),
   PAWN("nmPawn", PropertyType.NAME),
   TORSO("nmTorso", PropertyType.NAME),
   ARMS("nmArms", PropertyType.NAME),
   LEGS("nmLegs", PropertyType.NAME),
   HELMET("nmHelmet", PropertyType.NAME),
   EYE("nmEye", PropertyType.NAME),
   TEETH("nmTeeth", PropertyType.NAME),
   FACE_PROP_LOWER("nmFacePropLower", PropertyType.NAME),
   FACE_PROP_UPPER("nmFacePropUpper", PropertyType.NAME),
   PATTERNS("nmPatterns", PropertyType.NAME),
   VOICE_NAME("nmVoice", PropertyType.NAME),
   LANGUAGE("nmLanguage", PropertyType.NAME),
   TATTOO_LEFT_ARM("nmTattoo_LeftArm", PropertyType.NAME),
   TATTOO_RIGHT_ARM("nmTattoo_RightArm", PropertyType.NAME),
   SCARS("nmScars", PropertyType.NAME),
   TORSO_UNDERLAY("nmTorso_Underlay", PropertyType.NAME),
   ARMS_UNDERLAY("nmArms_Underlay", PropertyType.NAME),
   LEGS_UNDERLAY("nmLegs_Underlay", PropertyType.NAME),
   FACE_PAINT("nmFacePaint", PropertyType.NAME),
   LEFT_ARM("nmLeftArm", PropertyType.NAME),
   RIGHT_ARM("nmRightArm", PropertyType.NAME),
   LEFT_ARM_DECO("nmLeftArmDeco", PropertyType.NAME),
   RIGHT_ARM_DECO("nmRightArmDeco", PropertyType.NAME),
   LEFT_FOREARM("nmLeftForearm", PropertyType.NAME),
   RIGHT_FOREARM("nmRightForearm", PropertyType.NAME),
   THIGHS("nmThighs", PropertyType.NAME),
   SHINS("nmShins", PropertyType.NAME),
   TORSO_DECO("nmTorsoDeco", PropertyType.NAME),
   GHOST_PAWN("bGhostPawn", PropertyType.BOOL);

   private static final Map<String, AppearanceField> FIELD_MAP;

   static
   {
      FIELD_MAP = new HashMap<>();
      for (AppearanceField property : AppearanceField.values())
      {
         FIELD_MAP.put(property.getRaw(), property);
      }
   }

   public static Optional<AppearanceField> get(String str)
   {
      return Optional.ofNullable(FIELD_MAP.get(str));
   }

   private final String raw;
   private final PropertyType type;

   AppearanceField(String raw, PropertyType type)
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

package com.wassynger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum AppearanceProperty
{
   HEAD("nmHead", Property.Type.NAME),
   GENDER("iGender", Property.Type.INT),
   RACE("iRace", Property.Type.INT),
   HAIRCUT("nmHaircut", Property.Type.NAME),
   HAIR_COLOR("iHairColor", Property.Type.INT),
   FACIAL_HAIR("iFacialHair", Property.Type.INT),
   BEARD("nmBeard", Property.Type.NAME),
   SKIN_COLOR("iSkinColor", Property.Type.INT),
   EYE_COLOR("iEyeColor", Property.Type.INT),
   FLAG("nmFlag", Property.Type.NAME),
   VOICE("iVoice", Property.Type.INT),
   ATTITUDE("iAttitude", Property.Type.INT),
   ARMOR_DECO("iArmorDeco", Property.Type.INT),
   ARMOR_TINT("iArmorTint", Property.Type.INT),
   ARMOR_TINT_SECONDARY("iArmorTintSecondary", Property.Type.INT),
   WEAPON_TINT("iWeaponTint", Property.Type.INT),
   TATTOO_TINT("iTattooTint", Property.Type.INT),
   WEAPON_PATTERN("nmWeaponPattern", Property.Type.NAME),
   PAWN("nmPawn", Property.Type.NAME),
   TORSO("nmTorso", Property.Type.NAME),
   ARMS("nmArms", Property.Type.NAME),
   LEGS("nmLegs", Property.Type.NAME),
   HELMET("nmHelmet", Property.Type.NAME),
   EYE("nmEye", Property.Type.NAME),
   TEETH("nmTeeth", Property.Type.NAME),
   FACE_PROP_LOWER("nmFacePropLower", Property.Type.NAME),
   FACE_PROP_UPPER("nmFacePropUpper", Property.Type.NAME),
   PATTERNS("nmPatterns", Property.Type.NAME),
   VOICE_NAME("nmVoice", Property.Type.NAME),
   LANGUAGE("nmLanguage", Property.Type.NAME),
   TATTOO_LEFT_ARM("nmTattoo_LeftArm", Property.Type.NAME),
   TATTOO_RIGHT_ARM("nmTattoo_RightArm", Property.Type.NAME),
   SCARS("nmScars", Property.Type.NAME),
   TORSO_UNDERLAY("nmTorso_Underlay", Property.Type.NAME),
   ARMS_UNDERLAY("nmArms_Underlay", Property.Type.NAME),
   LEGS_UNDERLAY("nmLegs_Underlay", Property.Type.NAME),
   FACE_PAINT("nmFacePaint", Property.Type.NAME),
   LEFT_ARM("nmLeftArm", Property.Type.NAME),
   RIGHT_ARM("nmRightArm", Property.Type.NAME),
   LEFT_ARM_DECO("nmLeftArmDeco", Property.Type.NAME),
   RIGHT_ARM_DECO("nmRightArmDeco", Property.Type.NAME),
   LEFT_FOREARM("nmLeftForearm", Property.Type.NAME),
   RIGHT_FOREARM("nmRightForearm", Property.Type.NAME),
   THIGHS("nmThighs", Property.Type.NAME),
   SHINS("nmShins", Property.Type.NAME),
   TORSO_DECO("nmTorsoDeco", Property.Type.NAME),
   GHOST_PAWN("bGhostPawn", Property.Type.BOOL);

   private static final Map<String, AppearanceProperty> RAW_PROPERTY_MAP;

   static
   {
      RAW_PROPERTY_MAP = new HashMap<>();
      for (AppearanceProperty property : AppearanceProperty.values())
      {
         RAW_PROPERTY_MAP.put(property.getRaw(), property);
      }
   }

   public static Optional<AppearanceProperty> get(String str)
   {
      return Optional.ofNullable(RAW_PROPERTY_MAP.get(str));
   }

   private final String raw;
   private final Property.Type type;

   AppearanceProperty(String raw, Property.Type type)
   {
      this.raw = raw;
      this.type = type;
   }

   public String getRaw()
   {
      return raw;
   }
}

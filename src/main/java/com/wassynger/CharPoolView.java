package com.wassynger;

import java.util.Objects;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.SegmentedButton;

public class CharPoolView extends BorderPane
{
   private final ObjectProperty<CharacterPool> charPool;
   private final StackPane viewPlaceholder;
   private final HeadView headView;
   private final BodyView bodyView;
   private final WeaponView weaponView;

   @FXML
   private GridPane viewChar;
   @FXML
   private ScrollPane viewAppDetail;
   @FXML
   private ListView<Character> listChar;
   @FXML
   private Label labelPoolName;
   @FXML
   private Label labelCreationDate;
   @FXML
   private TextField fieldFName;
   @FXML
   private TextField fieldLName;
   @FXML
   private TextField fieldNName;
   @FXML
   private TextArea fieldBio;
   @FXML
   private ComboBox<String> cBoxCountry;
   @FXML
   private ComboBox<String> cBoxSType;
   @FXML
   private ComboBox<String> cBoxClass;
   @FXML
   private ComboBox<Race> cBoxRace;
   @FXML
   private ComboBox<String> cBoxVoice;
   @FXML
   private ComboBox<Personality> cBoxAttitude;
   @FXML
   private CheckBox chkSoldier;
   @FXML
   private CheckBox chkVip;
   @FXML
   private CheckBox chkDarkVip;
   @FXML
   private SegmentedButton segButtonSex;
   @FXML
   private SegmentedButton segButtonEdit;
   @FXML
   private ToggleButton buttonMale;
   @FXML
   private ToggleButton buttonFemale;
   @FXML
   private ToggleButton buttonHead;
   @FXML
   private ToggleButton buttonBody;
   @FXML
   private ToggleButton buttonWeapon;

   public CharPoolView()
   {
      this.charPool = new SimpleObjectProperty<>(this, "characterPool");
      this.viewPlaceholder = new StackPane(new Label("No Character Selected"));
      this.headView = new HeadView();
      this.bodyView = new BodyView();
      this.weaponView = new WeaponView();
      FxUtilities.load("CharPoolView.fxml", this);
   }

   @FXML
   private void initialize()
   {
      charPool.addListener((obs, old, newValue) -> onCharPoolChanged(newValue));
      centerProperty().bind(Bindings.when(listChar.getSelectionModel().selectedItemProperty().isNotNull())
            .then((Node) viewChar)
            .otherwise(viewPlaceholder));

      segButtonEdit.getToggleGroup()
            .selectedToggleProperty()
            .addListener((obs, old, newValue) -> onSelectedAppearanceToggleChanged(old, newValue));
      viewAppDetail.contentProperty()
            .bind(Bindings.createObjectBinding(this::computeAppearanceDetail,
                  segButtonEdit.getToggleGroup().selectedToggleProperty()));

      listChar.setCellFactory(list -> new FormattedListCell<>(this::formatCharacter));
      listChar.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, newValue) -> onSelectedCharChanged(newValue));

      cBoxRace.setCellFactory(list -> new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBoxRace.setButtonCell(new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBoxRace.getItems().addAll(Race.values());
      cBoxAttitude.setCellFactory(list -> new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBoxAttitude.setButtonCell(new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBoxAttitude.getItems().addAll(Personality.values());
   }

   private void onSelectedAppearanceToggleChanged(Toggle old, Toggle newValue)
   {
      if (newValue == null)
      {
         segButtonEdit.getToggleGroup().selectToggle(old);
      }
   }

   private Node computeAppearanceDetail()
   {
      if (segButtonEdit.getToggleGroup().getSelectedToggle() == buttonHead)
      {
         return headView;
      }
      else if (segButtonEdit.getToggleGroup().getSelectedToggle() == buttonBody)
      {
         return bodyView;
      }
      else if (segButtonEdit.getToggleGroup().getSelectedToggle() == buttonWeapon)
      {
         return weaponView;
      }
      return null;
   }

   private String formatCharacter(Character c)
   {
      return String.format("%s %s %s", c.get(CharacterProperty.FIRST_NAME), c.get(CharacterProperty.NICKNAME),
            c.get(CharacterProperty.LAST_NAME));
   }

   private void onCharPoolChanged(CharacterPool newValue)
   {
      if (newValue == null)
      {
         labelPoolName.setText("No Pool Opened");
         listChar.getItems().clear();
         return;
      }
      labelPoolName.setText(newValue.getName());
      listChar.getItems().setAll(newValue.getCharacters());
      listChar.getSelectionModel().clearSelection();
   }

   private void onSelectedCharChanged(Character newValue)
   {
      if (newValue == null)
      {
         return;
      }
      fieldFName.setText(newValue.get(CharacterProperty.FIRST_NAME));
      fieldLName.setText(newValue.get(CharacterProperty.LAST_NAME));
      fieldNName.setText(getNickname(newValue));
      fieldBio.setText(newValue.get(CharacterProperty.BIOGRAPHY));
      labelCreationDate.setText(newValue.tryGet(CharacterProperty.CREATION_DATE).orElse("Unknown"));
      setCBoxValue(cBoxCountry, newValue.get(CharacterProperty.COUNTRY));
      setCBoxValue(cBoxSType, newValue.get(CharacterProperty.TEMPLATE));
      setCBoxValue(cBoxClass, newValue.get(CharacterProperty.CLASS));
      // TODO handle unknown cases for race/attitude/gender
      newValue.getAppearanceEnum(AppearanceProperty.RACE, Race.class).ifPresent(cBoxRace.getSelectionModel()::select);
      setCBoxValue(cBoxVoice, newValue.get(AppearanceProperty.VOICE_NAME));
      newValue.getAppearanceEnum(AppearanceProperty.ATTITUDE, Personality.class)
            .ifPresent(cBoxAttitude.getSelectionModel()::select);
      chkSoldier.setSelected(newValue.isSelected(CharacterProperty.IS_SOLDIER));
      chkVip.setSelected(newValue.isSelected(CharacterProperty.IS_VIP));
      chkDarkVip.setSelected(newValue.isSelected(CharacterProperty.IS_DARK_VIP));
      newValue.getAppearanceEnum(AppearanceProperty.GENDER, Gender.class)
            .map(g -> g == Gender.MALE ? buttonMale : buttonFemale)
            .ifPresent(b -> b.setSelected(true));
      // Head subview
      setCBoxValue(headView.cBoxHelmet, newValue.get(AppearanceProperty.HELMET));
      setCBoxValue(headView.cBoxHead, newValue.get(AppearanceProperty.HEAD));
      setCBoxValue(headView.cBoxSkinColor, newValue.get(AppearanceProperty.SKIN_COLOR));
      setCBoxValue(headView.cBoxEyeColor, newValue.get(AppearanceProperty.EYE_COLOR));
      setCBoxValue(headView.cBoxScar, newValue.get(AppearanceProperty.SCARS));
      setCBoxValue(headView.cBoxPaint, newValue.get(AppearanceProperty.FACE_PAINT));
      setCBoxValue(headView.cBoxHair, newValue.get(AppearanceProperty.HAIRCUT));
      setCBoxValue(headView.cBoxFaceHair, newValue.get(AppearanceProperty.FACIAL_HAIR));
      setCBoxValue(headView.cBoxHairColor, newValue.get(AppearanceProperty.HAIR_COLOR));
      setCBoxValue(headView.cBoxPropUpper, newValue.get(AppearanceProperty.FACE_PROP_UPPER));
      setCBoxValue(headView.cBoxPropLower, newValue.get(AppearanceProperty.FACE_PROP_LOWER));
      // Body subview
      setCBoxValue(bodyView.cBoxTorso, newValue.get(AppearanceProperty.TORSO));
      setCBoxValue(bodyView.cBoxTorsoDeco, newValue.get(AppearanceProperty.TORSO_DECO));
      setCBoxValue(bodyView.cBoxTorsoUnder, newValue.get(AppearanceProperty.TORSO_UNDERLAY));
      setCBoxValue(bodyView.cBoxArms, newValue.get(AppearanceProperty.ARMS));
      setCBoxValue(bodyView.cBoxArmsUnder, newValue.get(AppearanceProperty.ARMS_UNDERLAY));
      setCBoxValue(bodyView.cBoxArmLeft, newValue.get(AppearanceProperty.LEFT_ARM));
      setCBoxValue(bodyView.cBoxForearmLeft, newValue.get(AppearanceProperty.LEFT_FOREARM));
      setCBoxValue(bodyView.cBoxArmLeftDeco, newValue.get(AppearanceProperty.LEFT_ARM_DECO));
      setCBoxValue(bodyView.cBoxArmRight, newValue.get(AppearanceProperty.RIGHT_ARM));
      setCBoxValue(bodyView.cBoxForearmRight, newValue.get(AppearanceProperty.RIGHT_FOREARM));
      setCBoxValue(bodyView.cBoxArmRightDeco, newValue.get(AppearanceProperty.RIGHT_ARM_DECO));
      setCBoxValue(bodyView.cBoxLegs, newValue.get(AppearanceProperty.LEGS));
      setCBoxValue(bodyView.cBoxLegsUnder, newValue.get(AppearanceProperty.LEGS_UNDERLAY));
      setCBoxValue(bodyView.cBoxThighs, newValue.get(AppearanceProperty.THIGHS));
      setCBoxValue(bodyView.cBoxShins, newValue.get(AppearanceProperty.SHINS));
      // Weapon subview
      setCBoxValue(weaponView.cBoxTint, newValue.get(AppearanceProperty.WEAPON_TINT));
      setCBoxValue(weaponView.cBoxPattern, newValue.get(AppearanceProperty.WEAPON_PATTERN));
   }

   private String getNickname(Character character)
   {
      // Remove surrounding ' quotes
      return character.tryGet(CharacterProperty.NICKNAME)
            .filter(str -> str.length() > 2)
            .map(str -> str.substring(1, str.length() - 1))
            .orElse("");
   }

   private void setCBoxValue(ComboBox<String> cBox, Object value)
   {
      cBox.getItems().setAll(Objects.toString(value));
      cBox.getSelectionModel().selectFirst();
   }

   public void setCharPool(CharacterPool charPool)
   {
      this.charPool.set(charPool);
   }

   static class HeadView extends GridPane
   {
      @FXML
      private ComboBox<String> cBoxHelmet;
      @FXML
      private ComboBox<String> cBoxHead;
      @FXML
      private ComboBox<String> cBoxSkinColor;
      @FXML
      private ComboBox<String> cBoxEyeColor;
      @FXML
      private ComboBox<String> cBoxScar;
      @FXML
      private ComboBox<String> cBoxPaint;
      @FXML
      private ComboBox<String> cBoxHair;
      @FXML
      private ComboBox<String> cBoxFaceHair;
      @FXML
      private ComboBox<String> cBoxHairColor;
      @FXML
      private ComboBox<String> cBoxPropUpper;
      @FXML
      private ComboBox<String> cBoxPropLower;

      public HeadView()
      {
         FxUtilities.load("AppearanceHeadView.fxml", this);
      }
   }

   static class BodyView extends GridPane
   {
      @FXML
      private ComboBox<String> cBoxTorso;
      @FXML
      private ComboBox<String> cBoxTorsoDeco;
      @FXML
      private ComboBox<String> cBoxTorsoUnder;
      @FXML
      private ComboBox<String> cBoxArms;
      @FXML
      private ComboBox<String> cBoxArmsUnder;
      @FXML
      private ComboBox<String> cBoxArmLeft;
      @FXML
      private ComboBox<String> cBoxForearmLeft;
      @FXML
      private ComboBox<String> cBoxArmLeftDeco;
      @FXML
      private ComboBox<String> cBoxArmRight;
      @FXML
      private ComboBox<String> cBoxForearmRight;
      @FXML
      private ComboBox<String> cBoxArmRightDeco;
      @FXML
      private ComboBox<String> cBoxLegs;
      @FXML
      private ComboBox<String> cBoxLegsUnder;
      @FXML
      private ComboBox<String> cBoxThighs;
      @FXML
      private ComboBox<String> cBoxShins;

      public BodyView()
      {
         FxUtilities.load("AppearanceBodyView.fxml", this);
      }
   }

   static class WeaponView extends GridPane
   {
      @FXML
      private ComboBox<String> cBoxTint;
      @FXML
      private ComboBox<String> cBoxPattern;

      public WeaponView()
      {
         FxUtilities.load("AppearanceWeaponView.fxml", this);
      }
   }
}

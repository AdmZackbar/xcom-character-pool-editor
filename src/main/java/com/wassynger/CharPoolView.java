package com.wassynger;

import java.util.Comparator;
import java.util.stream.Collectors;

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
   private ComboBox<StringEntry> cBoxCountry;
   @FXML
   private ComboBox<StringEntry> cBoxSType;
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

      listChar.setCellFactory(list -> new FormattedListCell<>(this::computeFullName));
      listChar.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, newValue) -> onSelectedCharChanged(newValue));

      cBoxCountry.setCellFactory(list -> new FormattedListCell<>(StringEntry::getLocalized));
      cBoxCountry.setButtonCell(new FormattedListCell<>(StringEntry::getLocalized));
      cBoxSType.setCellFactory(list -> new FormattedListCell<>(StringEntry::getLocalized));
      cBoxSType.setButtonCell(new FormattedListCell<>(StringEntry::getLocalized));
      cBoxRace.setCellFactory(list -> new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBoxRace.setButtonCell(new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBoxRace.getItems().addAll(Race.values());
      cBoxAttitude.setCellFactory(list -> new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBoxAttitude.setButtonCell(new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBoxAttitude.getItems().addAll(Personality.values());

      // Load info
      refresh();
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

   private String computeFullName(Character c)
   {
      String fName = c.tryGet(CharacterField.FIRST_NAME).orElse("");
      String lName = c.tryGet(CharacterField.LAST_NAME).orElse("");
      return c.tryGet(CharacterField.NICKNAME)
            .filter(s -> !s.isEmpty())
            .map(nName -> String.format("%s %s %s", fName, nName, lName))
            .orElse(String.format("%s %s", fName, lName));
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
      fieldFName.setText(newValue.tryGet(CharacterField.FIRST_NAME).orElse(""));
      fieldLName.setText(newValue.tryGet(CharacterField.LAST_NAME).orElse(""));
      fieldNName.setText(getNickname(newValue));
      fieldBio.setText(newValue.tryGet(CharacterField.BIOGRAPHY).orElse(""));
      labelCreationDate.setText(newValue.tryGet(CharacterField.CREATION_DATE).orElse("Unknown"));
      setCBoxValue(cBoxCountry,
            StringTemplate.COUNTRY.getOrAdd(newValue.get(CharacterField.COUNTRY).getDisplayValue()));
      setCBoxValue(cBoxSType,
            StringTemplate.CHARACTER.getOrAdd(newValue.get(CharacterField.TEMPLATE).getDisplayValue()));
      setCBoxValue(cBoxClass, newValue.get(CharacterField.CLASS));
      // TODO handle unknown cases for race/attitude/gender
      newValue.getAppearanceEnum(AppearanceField.RACE, Race.class).ifPresent(cBoxRace.getSelectionModel()::select);
      setCBoxValue(cBoxVoice, newValue.get(AppearanceField.VOICE_NAME));
      newValue.getAppearanceEnum(AppearanceField.ATTITUDE, Personality.class)
            .ifPresent(cBoxAttitude.getSelectionModel()::select);
      chkSoldier.setSelected(newValue.isSelected(CharacterField.IS_SOLDIER).orElse(true));
      chkVip.setSelected(newValue.isSelected(CharacterField.IS_VIP).orElse(true));
      chkDarkVip.setSelected(newValue.isSelected(CharacterField.IS_DARK_VIP).orElse(true));
      newValue.getAppearanceEnum(AppearanceField.GENDER, Gender.class)
            .map(g -> g == Gender.MALE ? buttonMale : buttonFemale)
            .ifPresent(b -> b.setSelected(true));
      // Head subview
      setCBoxValue(headView.cBoxHelmet, newValue.get(AppearanceField.HELMET));
      setCBoxValue(headView.cBoxHead, newValue.get(AppearanceField.HEAD));
      setCBoxValue(headView.cBoxSkinColor, newValue.get(AppearanceField.SKIN_COLOR));
      setCBoxValue(headView.cBoxEyeColor, newValue.get(AppearanceField.EYE_COLOR));
      setCBoxValue(headView.cBoxScar, newValue.get(AppearanceField.SCARS));
      setCBoxValue(headView.cBoxPaint, newValue.get(AppearanceField.FACE_PAINT));
      setCBoxValue(headView.cBoxHair, newValue.get(AppearanceField.HAIRCUT));
      setCBoxValue(headView.cBoxFaceHair, newValue.get(AppearanceField.FACIAL_HAIR));
      setCBoxValue(headView.cBoxHairColor, newValue.get(AppearanceField.HAIR_COLOR));
      setCBoxValue(headView.cBoxPropUpper, newValue.get(AppearanceField.FACE_PROP_UPPER));
      setCBoxValue(headView.cBoxPropLower, newValue.get(AppearanceField.FACE_PROP_LOWER));
      // Body subview
      setCBoxValue(bodyView.cBoxTorso, newValue.get(AppearanceField.TORSO));
      setCBoxValue(bodyView.cBoxTorsoDeco, newValue.get(AppearanceField.TORSO_DECO));
      setCBoxValue(bodyView.cBoxTorsoUnder, newValue.get(AppearanceField.TORSO_UNDERLAY));
      setCBoxValue(bodyView.cBoxArms, newValue.get(AppearanceField.ARMS));
      setCBoxValue(bodyView.cBoxArmsUnder, newValue.get(AppearanceField.ARMS_UNDERLAY));
      setCBoxValue(bodyView.cBoxArmLeft, newValue.get(AppearanceField.LEFT_ARM));
      setCBoxValue(bodyView.cBoxForearmLeft, newValue.get(AppearanceField.LEFT_FOREARM));
      setCBoxValue(bodyView.cBoxArmLeftDeco, newValue.get(AppearanceField.LEFT_ARM_DECO));
      setCBoxValue(bodyView.cBoxArmRight, newValue.get(AppearanceField.RIGHT_ARM));
      setCBoxValue(bodyView.cBoxForearmRight, newValue.get(AppearanceField.RIGHT_FOREARM));
      setCBoxValue(bodyView.cBoxArmRightDeco, newValue.get(AppearanceField.RIGHT_ARM_DECO));
      setCBoxValue(bodyView.cBoxLegs, newValue.get(AppearanceField.LEGS));
      setCBoxValue(bodyView.cBoxLegsUnder, newValue.get(AppearanceField.LEGS_UNDERLAY));
      setCBoxValue(bodyView.cBoxThighs, newValue.get(AppearanceField.THIGHS));
      setCBoxValue(bodyView.cBoxShins, newValue.get(AppearanceField.SHINS));
      // Weapon subview
      setCBoxValue(weaponView.cBoxTint, newValue.get(AppearanceField.WEAPON_TINT));
      setCBoxValue(weaponView.cBoxPattern, newValue.get(AppearanceField.WEAPON_PATTERN));
   }

   private String getNickname(Character character)
   {
      // Remove surrounding ' quotes
      return character.tryGet(CharacterField.NICKNAME)
            .filter(str -> str.length() > 2)
            .map(str -> str.substring(1, str.length() - 1))
            .orElse("");
   }

   private void setCBoxValue(ComboBox<StringEntry> cBox, StringEntry entry)
   {
      if (!cBox.getItems().contains(entry))
      {
         cBox.getItems().add(entry);
      }
      cBox.getSelectionModel().select(entry);
   }

   private void setCBoxValue(ComboBox<String> cBox, PropertyValue value)
   {
      if (value == null)
      {
         cBox.getItems().clear();
         return;
      }
      cBox.getItems().setAll(value.getDisplayValue());
      cBox.getSelectionModel().selectFirst();
   }

   public CharacterPool getCharPool()
   {
      return charPool.get();
   }

   public void setCharPool(CharacterPool charPool)
   {
      this.charPool.set(charPool);
   }

   public ObjectProperty<CharacterPool> charPoolProperty()
   {
      return charPool;
   }

   public void refresh()
   {
      refresh(cBoxCountry, StringTemplate.COUNTRY);
      refresh(cBoxSType, StringTemplate.CHARACTER);
      // Need to refresh selection now that we have reloaded values
      onSelectedCharChanged(listChar.getSelectionModel().getSelectedItem());
   }

   private void refresh(ComboBox<StringEntry> cBox, StringTemplate template)
   {
      // Can't use setAll - causes visual bug with the button cell where the
      // selected value doesn't get updated
      cBox.getItems().clear();
      cBox.getItems()
            .addAll(template.getAll()
                  .stream()
                  .sorted(Comparator.comparing(StringEntry::getLocalized))
                  .collect(Collectors.toList()));
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

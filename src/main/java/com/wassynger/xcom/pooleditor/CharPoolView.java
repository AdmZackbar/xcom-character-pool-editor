package com.wassynger.xcom.pooleditor;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import com.wassynger.xcom.pooleditor.data.AppearanceField;
import com.wassynger.xcom.pooleditor.data.Character;
import com.wassynger.xcom.pooleditor.data.CharacterField;
import com.wassynger.xcom.pooleditor.data.EditableCharPool;
import com.wassynger.xcom.pooleditor.data.EditableCharacter;
import com.wassynger.xcom.pooleditor.data.Gender;
import com.wassynger.xcom.pooleditor.data.Personality;
import com.wassynger.xcom.pooleditor.data.PropertyField;
import com.wassynger.xcom.pooleditor.data.PropertyValue;
import com.wassynger.xcom.pooleditor.data.Race;
import com.wassynger.xcom.pooleditor.data.StaticEnum;
import com.wassynger.xcom.pooleditor.data.StringEntry;
import com.wassynger.xcom.pooleditor.data.StringTemplate;
import com.wassynger.xcom.pooleditor.ui.FormattedListCell;
import com.wassynger.xcom.pooleditor.ui.FxUtilities;
import org.controlsfx.control.SegmentedButton;

public class CharPoolView extends BorderPane
{
   private static final EventType<Event> ANY = new EventType<>(Event.ANY, "CHAR_POOL_VIEW");
   public static final EventType<Event> ON_CHAR_ADD = new EventType<>(ANY, "ON_CHAR_ADD");
   public static final EventType<Event> ON_CHAR_REMOVE = new EventType<>(ANY, "ON_CHAR_REMOVE");

   private final ObjectProperty<EditableCharPool> charPool;
   private final StackPane viewPlaceholder;
   private final HeadView headView;
   private final BodyView bodyView;
   private final WeaponView weaponView;

   @FXML
   private GridPane viewChar;
   @FXML
   private ScrollPane viewAppDetail;
   @FXML
   private ListView<EditableCharacter> listChar;
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
   private Button buttonAddChar;
   @FXML
   private Button buttonRemoveChar;
   @FXML
   private ComboBox<StringEntry> cBoxCountry;
   @FXML
   private ComboBox<StringEntry> cBoxSType;
   @FXML
   private ComboBox<StringEntry> cBoxClass;
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

      buttonAddChar.setOnAction(event -> this.fireEvent(new Event(ON_CHAR_ADD)));
      buttonRemoveChar.setOnAction(event -> this.fireEvent(new Event(ON_CHAR_REMOVE)));
      buttonRemoveChar.disableProperty().bind(getCharSelectionModel().selectedItemProperty().isNull());

      listChar.setCellFactory(list -> new FormattedListCell<>(this::computeFullName,
            listChar.getSelectionModel().selectedItemProperty()));
      listChar.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, newValue) -> onSelectedCharChanged(old, newValue));

      initStringEntryCBox(cBoxCountry, StringTemplate.COUNTRY, CharacterField.COUNTRY);
      initStringEntryCBox(cBoxSType, StringTemplate.CHARACTER, CharacterField.TEMPLATE);
      initStringEntryCBox(cBoxClass, StringTemplate.CLASS, CharacterField.CLASS);
      segButtonSex.getToggleGroup().selectedToggleProperty().addListener((obs, old, newValue) ->
      {
         EditableCharacter character = listChar.getSelectionModel().getSelectedItem();
         if (character != null)
         {
            character.intProperty(AppearanceField.GENDER)
                  .set((newValue == buttonMale ? Gender.MALE : Gender.FEMALE).getValue());
         }
      });
      initStaticEnumCBox(Race.class, cBoxRace, AppearanceField.RACE);
      initStaticEnumCBox(Personality.class, cBoxAttitude, AppearanceField.ATTITUDE);

      // Load info
      refresh();
   }

   private void initStringEntryCBox(ComboBox<StringEntry> cBox, StringTemplate template, PropertyField field)
   {
      cBox.getItems().addAll(template.getAll());
      cBox.setCellFactory(list -> new FormattedListCell<>(StringEntry::getLocalized));
      cBox.setButtonCell(new FormattedListCell<>(StringEntry::getLocalized));
      cBox.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, newValue) -> onStringEntryCBoxSelectionChanged(newValue, field));
   }

   private void onStringEntryCBoxSelectionChanged(StringEntry newValue, PropertyField field)
   {
      EditableCharacter character = listChar.getSelectionModel().getSelectedItem();
      if (character != null)
      {
         character.strProperty(field).set(newValue.getStr());
      }
   }

   private <T extends StaticEnum> void initStaticEnumCBox(Class<T> cls, ComboBox<T> cBox, PropertyField field)
   {
      cBox.setCellFactory(list -> new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBox.setButtonCell(new FormattedListCell<>(StaticEnum::getLocalizedString));
      cBox.getItems().addAll(cls.getEnumConstants());
      cBox.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, newValue) -> onStaticEnumCBoxSelectionChanged(newValue, field));
   }

   private void onStaticEnumCBoxSelectionChanged(StaticEnum newValue, PropertyField field)
   {
      EditableCharacter character = listChar.getSelectionModel().getSelectedItem();
      if (character != null)
      {
         character.intProperty(field).set(newValue.getValue());
      }
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

   private String computeFullName(EditableCharacter c)
   {
      // TODO update
      String fName = c.getBaseChar().tryGet(CharacterField.FIRST_NAME).orElse("");
      String lName = c.getBaseChar().tryGet(CharacterField.LAST_NAME).orElse("");
      String edited = c.isEdited() ? "*" : "";
      return c.getBaseChar()
            .tryGet(CharacterField.NICKNAME)
            .filter(s -> !s.isEmpty())
            .map(nName -> String.format("%s %s %s%s", fName, nName, lName, edited))
            .orElse(String.format("%s %s%s", fName, lName, edited));
   }

   private void onCharPoolChanged(EditableCharPool newValue)
   {
      listChar.getSelectionModel().clearSelection();
      if (newValue == null)
      {
         labelPoolName.setText("No Pool Opened");
         listChar.setItems(FXCollections.emptyObservableList());
         return;
      }
      labelPoolName.setText(newValue.getBasePool().getName());
      listChar.setItems(newValue.getCharacters());
   }

   private void unbindCharacter(EditableCharacter character)
   {
      Bindings.unbindBidirectional(fieldFName.textProperty(), character.strProperty(CharacterField.FIRST_NAME));
      Bindings.unbindBidirectional(fieldLName.textProperty(), character.strProperty(CharacterField.LAST_NAME));
      Bindings.unbindBidirectional(fieldNName.textProperty(), character.strProperty(CharacterField.NICKNAME));
      Bindings.unbindBidirectional(fieldBio.textProperty(), character.strProperty(CharacterField.BIOGRAPHY));
      labelCreationDate.textProperty().unbind();
      Bindings.unbindBidirectional(chkSoldier.selectedProperty(), character.boolProperty(CharacterField.IS_SOLDIER));
      Bindings.unbindBidirectional(chkVip.selectedProperty(), character.boolProperty(CharacterField.IS_VIP));
      Bindings.unbindBidirectional(chkDarkVip.selectedProperty(), character.boolProperty(CharacterField.IS_DARK_VIP));
   }

   private void onSelectedCharChanged(EditableCharacter old, EditableCharacter newValue)
   {
      if (old != null)
      {
         unbindCharacter(old);
      }
      if (newValue == null)
      {
         return;
      }
      Bindings.bindBidirectional(fieldFName.textProperty(), newValue.strProperty(CharacterField.FIRST_NAME));
      Bindings.bindBidirectional(fieldLName.textProperty(), newValue.strProperty(CharacterField.LAST_NAME));
      Bindings.bindBidirectional(fieldNName.textProperty(), newValue.strProperty(CharacterField.NICKNAME));
      Bindings.bindBidirectional(fieldBio.textProperty(), newValue.strProperty(CharacterField.BIOGRAPHY));
      labelCreationDate.textProperty()
            .bind(Bindings.when(newValue.editedProperty())
                  .then("Edited")
                  .otherwise(newValue.strProperty(CharacterField.CREATION_DATE)));
      setCBoxValue(cBoxCountry,
            StringTemplate.COUNTRY.getOrAdd(newValue.getBaseChar().get(CharacterField.COUNTRY).getDisplayValue()));
      setCBoxValue(cBoxSType,
            StringTemplate.CHARACTER.getOrAdd(newValue.getBaseChar().get(CharacterField.TEMPLATE).getDisplayValue()));
      setCBoxValue(cBoxClass,
            StringTemplate.CLASS.getOrAdd(newValue.getBaseChar().get(CharacterField.CLASS).getDisplayValue()));
      Bindings.bindBidirectional(chkSoldier.selectedProperty(), newValue.boolProperty(CharacterField.IS_SOLDIER));
      Bindings.bindBidirectional(chkVip.selectedProperty(), newValue.boolProperty(CharacterField.IS_VIP));
      Bindings.bindBidirectional(chkDarkVip.selectedProperty(), newValue.boolProperty(CharacterField.IS_DARK_VIP));
      updateAppearance(newValue);
   }

   private void updateAppearance(EditableCharacter character)
   {
      // TODO handle unknown cases for race/attitude/gender
      cBoxRace.getSelectionModel()
            .select(StaticEnum.fromValue(Race.class, character.intProperty(AppearanceField.RACE).get()).orElse(null));
      setCBoxValue(cBoxVoice, character.getBaseChar().get(AppearanceField.VOICE_NAME));
      cBoxAttitude.getSelectionModel()
            .select(StaticEnum.fromValue(Personality.class, character.intProperty(AppearanceField.ATTITUDE).get())
                  .orElse(null));
      StaticEnum.fromValue(Gender.class, character.intProperty(AppearanceField.GENDER).get())
            .map(g -> g == Gender.MALE ? buttonMale : buttonFemale)
            .ifPresent(b -> b.setSelected(true));
      // TODO
      // Head subview
      setCBoxValue(headView.cBoxHelmet, character.getBaseChar().get(AppearanceField.HELMET));
      setCBoxValue(headView.cBoxHead, character.getBaseChar().get(AppearanceField.HEAD));
      setCBoxValue(headView.cBoxSkinColor, character.getBaseChar().get(AppearanceField.SKIN_COLOR));
      setCBoxValue(headView.cBoxEyeColor, character.getBaseChar().get(AppearanceField.EYE_COLOR));
      setCBoxValue(headView.cBoxScar, character.getBaseChar().get(AppearanceField.SCARS));
      setCBoxValue(headView.cBoxPaint, character.getBaseChar().get(AppearanceField.FACE_PAINT));
      setCBoxValue(headView.cBoxHair, character.getBaseChar().get(AppearanceField.HAIRCUT));
      setCBoxValue(headView.cBoxFaceHair, character.getBaseChar().get(AppearanceField.FACIAL_HAIR));
      setCBoxValue(headView.cBoxHairColor, character.getBaseChar().get(AppearanceField.HAIR_COLOR));
      setCBoxValue(headView.cBoxPropUpper, character.getBaseChar().get(AppearanceField.FACE_PROP_UPPER));
      setCBoxValue(headView.cBoxPropLower, character.getBaseChar().get(AppearanceField.FACE_PROP_LOWER));
      // Body subview
      setCBoxValue(bodyView.cBoxTorso, character.getBaseChar().get(AppearanceField.TORSO));
      setCBoxValue(bodyView.cBoxTorsoDeco, character.getBaseChar().get(AppearanceField.TORSO_DECO));
      setCBoxValue(bodyView.cBoxTorsoUnder, character.getBaseChar().get(AppearanceField.TORSO_UNDERLAY));
      setCBoxValue(bodyView.cBoxArms, character.getBaseChar().get(AppearanceField.ARMS));
      setCBoxValue(bodyView.cBoxArmsUnder, character.getBaseChar().get(AppearanceField.ARMS_UNDERLAY));
      setCBoxValue(bodyView.cBoxArmLeft, character.getBaseChar().get(AppearanceField.LEFT_ARM));
      setCBoxValue(bodyView.cBoxForearmLeft, character.getBaseChar().get(AppearanceField.LEFT_FOREARM));
      setCBoxValue(bodyView.cBoxArmLeftDeco, character.getBaseChar().get(AppearanceField.LEFT_ARM_DECO));
      setCBoxValue(bodyView.cBoxArmRight, character.getBaseChar().get(AppearanceField.RIGHT_ARM));
      setCBoxValue(bodyView.cBoxForearmRight, character.getBaseChar().get(AppearanceField.RIGHT_FOREARM));
      setCBoxValue(bodyView.cBoxArmRightDeco, character.getBaseChar().get(AppearanceField.RIGHT_ARM_DECO));
      setCBoxValue(bodyView.cBoxLegs, character.getBaseChar().get(AppearanceField.LEGS));
      setCBoxValue(bodyView.cBoxLegsUnder, character.getBaseChar().get(AppearanceField.LEGS_UNDERLAY));
      setCBoxValue(bodyView.cBoxThighs, character.getBaseChar().get(AppearanceField.THIGHS));
      setCBoxValue(bodyView.cBoxShins, character.getBaseChar().get(AppearanceField.SHINS));
      // Weapon subview
      setCBoxValue(weaponView.cBoxTint, character.getBaseChar().get(AppearanceField.WEAPON_TINT));
      setCBoxValue(weaponView.cBoxPattern, character.getBaseChar().get(AppearanceField.WEAPON_PATTERN));
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

   public EditableCharPool getCharPool()
   {
      return charPool.get();
   }

   public void setCharPool(EditableCharPool charPool)
   {
      this.charPool.set(charPool);
   }

   public ObjectProperty<EditableCharPool> charPoolProperty()
   {
      return charPool;
   }

   public SelectionModel<EditableCharacter> getCharSelectionModel()
   {
      return listChar.getSelectionModel();
   }

   public void refresh()
   {
      refresh(cBoxCountry, StringTemplate.COUNTRY);
      refresh(cBoxSType, StringTemplate.CHARACTER);
      refresh(cBoxClass, StringTemplate.CLASS);
      // Need to refresh selection now that we have reloaded values
      // TODO still needed?
//      onSelectedCharChanged(null, listChar.getSelectionModel().getSelectedItem());
   }

   private void refresh(ComboBox<StringEntry> cBox, StringTemplate template)
   {
      for (StringEntry entry : template.getAll())
      {
         if (!cBox.getItems().contains(entry))
         {
            cBox.getItems().add(entry);
         }
      }
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

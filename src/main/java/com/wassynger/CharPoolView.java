package com.wassynger;

import java.util.List;
import java.util.Objects;
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
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.SegmentedButton;

public class CharPoolView extends BorderPane
{
   private final ObjectProperty<CharacterPool> charPool;
   private final StackPane viewPlaceholder;

   @FXML
   private GridPane viewChar;
   @FXML
   private BorderPane viewAppDetail;
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
      FxUtilities.load("CharPoolView.fxml", this);
   }

   @FXML
   private void initialize()
   {
      charPool.addListener((obs, old, newValue) -> onCharPoolChanged(newValue));
      centerProperty().bind(Bindings.when(listChar.getSelectionModel().selectedItemProperty().isNotNull())
            .then((Node) viewChar)
            .otherwise(viewPlaceholder));

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
         viewAppDetail.setCenter(null);
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
      ScrollPane scrollPane = new ScrollPane(createView(newValue.getProperties(CharacterProperty.APPEARANCE)));
      scrollPane.setFitToWidth(true);
      scrollPane.setFitToHeight(true);
      viewAppDetail.setCenter(scrollPane);
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

   private GridPane createView(List<Property> properties)
   {
      GridPane detailView = new GridPane();
      detailView.setHgap(4.0);
      detailView.setVgap(4.0);
      ColumnConstraints col1 = new ColumnConstraints();
      col1.setMinWidth(USE_PREF_SIZE);
      detailView.getColumnConstraints().add(col1);
      int i = 0;
      for (Property property : properties)
      {
         if (property.getType() == Property.Type.STRUCT)
         {
            detailView.addRow(i++, new Label(property.getName()), createView(
                  ((List<?>) property.getData()).stream().map(Property.class::cast).collect(Collectors.toList())));
         }
         else
         {
            detailView.addRow(i++, new Label(property.getName()), new Label(Objects.toString(property.getData())));
         }
      }
      return detailView;
   }

   public void setCharPool(CharacterPool charPool)
   {
      this.charPool.set(charPool);
   }
}

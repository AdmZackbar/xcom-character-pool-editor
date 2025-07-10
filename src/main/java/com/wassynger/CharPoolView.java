package com.wassynger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class CharPoolView extends BorderPane
{
   private final ObjectProperty<CharacterPool> charPool;
   private final StackPane viewPlaceholder;

   @FXML
   private GridPane viewChar;
   @FXML
   private ScrollPane scrollPane;
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
   private CheckBox chkSoldier;
   @FXML
   private CheckBox chkVip;
   @FXML
   private CheckBox chkDarkVip;

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
         scrollPane.setContent(null);
         return;
      }
      fieldFName.setText(newValue.get(CharacterProperty.FIRST_NAME));
      fieldLName.setText(newValue.get(CharacterProperty.LAST_NAME));
      fieldNName.setText(newValue.get(CharacterProperty.NICKNAME));
      fieldBio.setText(newValue.get(CharacterProperty.BIOGRAPHY));
      labelCreationDate.setText(newValue.tryGet(CharacterProperty.CREATION_DATE).orElse("Unknown"));
      cBoxCountry.getItems().setAll(newValue.get(CharacterProperty.COUNTRY));
      cBoxCountry.getSelectionModel().selectFirst();
      cBoxSType.getItems().setAll(newValue.get(CharacterProperty.TEMPLATE));
      cBoxSType.getSelectionModel().selectFirst();
      cBoxClass.getItems().setAll(newValue.get(CharacterProperty.CLASS));
      cBoxClass.getSelectionModel().selectFirst();
      chkSoldier.setSelected(newValue.isSelected(CharacterProperty.IS_SOLDIER));
      chkVip.setSelected(newValue.isSelected(CharacterProperty.IS_VIP));
      chkDarkVip.setSelected(newValue.isSelected(CharacterProperty.IS_DARK_VIP));
      scrollPane.setContent(createView(newValue.getProperties(CharacterProperty.APPEARANCE)));
   }

   private GridPane createView(List<Property> properties)
   {
      GridPane detailView = new GridPane();
      detailView.setHgap(4.0);
      detailView.setVgap(4.0);
      detailView.setPadding(new Insets(4.0));
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

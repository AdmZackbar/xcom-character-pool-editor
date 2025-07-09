package com.wassynger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class MainView extends BorderPane
{
   private static final EventType<Event> ANY = new EventType<>(Event.ANY, "CHAR_POOL_EDITOR");
   public static final EventType<Event> ON_LOAD = new EventType<>(ANY, "ON_LOAD");
   public static final EventType<Event> ON_QUIT = new EventType<>(ANY, "ON_QUIT");

   private final ObjectProperty<CharacterPool> characterPool;
   private final ListView<Character> characterListView;

   @FXML
   private MenuItem itemLoad;
   @FXML
   private MenuItem itemQuit;
   @FXML
   private Label labelPlaceholder;

   public MainView()
   {
      this.characterPool = new SimpleObjectProperty<>(this, "characterPool");
      this.characterListView = new ListView<>();
      characterListView.setCellFactory(list -> new FormattedListCell<>(this::formatCharacter));
      characterListView.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, newValue) -> onSelectedCharacterChanged(newValue));
      FxUtilities.load("MainView.fxml", this);
   }

   private void onSelectedCharacterChanged(Character newValue)
   {
      if (newValue == null)
      {
         setCenter(null);
         return;
      }
      ScrollPane scrollPane = new ScrollPane(createView(newValue.getProperties()));
      scrollPane.setFitToWidth(true);
      scrollPane.setFitToHeight(true);
      setCenter(scrollPane);
   }

   private GridPane createView(List<Property> properties)
   {
      GridPane detailView = new GridPane();
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

   private String formatCharacter(Character c)
   {
      return String.format("%s %s %s", c.get(CharacterProperty.FIRST_NAME), c.get(CharacterProperty.NICKNAME),
            c.get(CharacterProperty.LAST_NAME));
   }

   @FXML
   private void initialize()
   {
      itemLoad.setOnAction(event -> this.fireEvent(new Event(ON_LOAD)));
      itemQuit.setOnAction(event -> this.fireEvent(new Event(ON_QUIT)));

      characterPool.addListener((obs, old, newValue) -> onCharacterPoolChanged(newValue));
   }

   private void onCharacterPoolChanged(CharacterPool newValue)
   {
      if (newValue == null)
      {
         setLeft(null);
         setCenter(labelPlaceholder);
         characterListView.getItems().clear();
         return;
      }
      setLeft(characterListView);
      characterListView.getItems().setAll(newValue.getCharacters());
      characterListView.getSelectionModel().clearSelection();
   }

   public void setCharacterPool(CharacterPool characterPool)
   {
      this.characterPool.set(characterPool);
   }
}

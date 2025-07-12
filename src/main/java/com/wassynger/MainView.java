package com.wassynger;

import java.util.Comparator;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.BorderPane;

public class MainView extends BorderPane
{
   private static final EventType<Event> ANY = new EventType<>(Event.ANY, "CHAR_POOL_EDITOR");
   public static final EventType<Event> ON_LOAD = new EventType<>(ANY, "ON_LOAD");
   public static final EventType<Event> ON_SAVE = new EventType<>(ANY, "ON_SAVE");
   public static final EventType<Event> ON_LOAD_MOD = new EventType<>(ANY, "ON_LOAD_MOD");
   public static final EventType<Event> ON_QUIT = new EventType<>(ANY, "ON_QUIT");

   private final ObservableList<CharacterPool> charPools;
   private final CharPoolView charPoolView;

   @FXML
   private MenuItem itemLoad;
   @FXML
   private MenuItem itemSave;
   @FXML
   private MenuItem itemLoadMod;
   @FXML
   private MenuItem itemQuit;
   @FXML
   private Label labelPlaceholder;
   @FXML
   private ListView<CharacterPool> listPool;

   public MainView()
   {
      this.charPools = FXCollections.observableArrayList();
      this.charPoolView = new CharPoolView();
      FxUtilities.load("MainView.fxml", this);
   }

   @FXML
   private void initialize()
   {
      itemLoad.setOnAction(event -> this.fireEvent(new Event(ON_LOAD)));
      itemSave.setOnAction(event -> this.fireEvent(new Event(ON_SAVE)));
      itemSave.disableProperty().bind(charPoolView.charPoolProperty().isNull());
      itemLoadMod.setOnAction(event -> this.fireEvent(new Event(ON_LOAD_MOD)));
      itemQuit.setOnAction(event -> this.fireEvent(new Event(ON_QUIT)));

      listPool.setItems(charPools.sorted(Comparator.comparing(CharacterPool::getName)));
      listPool.setCellFactory(list -> new FormattedListCell<>(this::computePoolText));
      charPoolView.charPoolProperty().bind(listPool.getSelectionModel().selectedItemProperty());
      centerProperty().bind(Bindings.when(charPoolView.charPoolProperty().isNotNull())
            .then((Node) charPoolView)
            .otherwise(labelPlaceholder));
      labelPlaceholder.textProperty().bind(Bindings.createStringBinding(this::computePlaceholderText, charPools));
   }

   private String computePoolText(CharacterPool pool)
   {
      return String.format("%s (%d)", pool.getName(), pool.getCharacters().size());
   }

   private String computePlaceholderText()
   {
      if (charPools.isEmpty())
      {
         return "Load character pool to begin";
      }
      return "Select character pool to view";
   }

   public ObservableList<CharacterPool> getCharPools()
   {
      return charPools;
   }

   public SelectionModel<CharacterPool> getCharPoolSelectionModel()
   {
      return listPool.getSelectionModel();
   }

   public CharPoolView getCharPoolView()
   {
      return charPoolView;
   }
}

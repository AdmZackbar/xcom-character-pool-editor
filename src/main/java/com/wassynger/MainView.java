package com.wassynger;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

public class MainView extends BorderPane
{
   private static final EventType<Event> ANY = new EventType<>(Event.ANY, "CHAR_POOL_EDITOR");
   public static final EventType<Event> ON_LOAD = new EventType<>(ANY, "ON_LOAD");
   public static final EventType<Event> ON_SAVE = new EventType<>(ANY, "ON_SAVE");
   public static final EventType<Event> ON_LOAD_MOD = new EventType<>(ANY, "ON_LOAD_MOD");
   public static final EventType<Event> ON_QUIT = new EventType<>(ANY, "ON_QUIT");

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

   public MainView()
   {
      this.charPoolView = new CharPoolView();
      FxUtilities.load("MainView.fxml", this);
      setCenter(charPoolView);
   }

   @FXML
   private void initialize()
   {
      itemLoad.setOnAction(event -> this.fireEvent(new Event(ON_LOAD)));
      itemSave.setOnAction(event -> this.fireEvent(new Event(ON_SAVE)));
      itemSave.disableProperty().bind(charPoolView.charPoolProperty().isNull());
      itemLoadMod.setOnAction(event -> this.fireEvent(new Event(ON_LOAD_MOD)));
      itemQuit.setOnAction(event -> this.fireEvent(new Event(ON_QUIT)));
   }

   public CharPoolView getCharPoolView()
   {
      return charPoolView;
   }
}

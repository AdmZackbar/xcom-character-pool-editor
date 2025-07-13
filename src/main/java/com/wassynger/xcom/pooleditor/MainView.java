package com.wassynger.xcom.pooleditor;

import java.util.Comparator;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.BorderPane;

import com.wassynger.xcom.pooleditor.data.EditableCharPool;
import com.wassynger.xcom.pooleditor.ui.FormattedListCell;
import com.wassynger.xcom.pooleditor.ui.FxUtilities;
import com.wassynger.xcom.pooleditor.ui.ProgressView;

public class MainView extends BorderPane
{
   private static final EventType<Event> ANY = new EventType<>(Event.ANY, "CHAR_POOL_EDITOR");
   public static final EventType<Event> ON_POOL_LOAD = new EventType<>(ANY, "ON_POOL_LOAD");
   public static final EventType<Event> ON_POOL_SAVE = new EventType<>(ANY, "ON_POOL_SAVE");
   public static final EventType<Event> ON_POOL_ADD = new EventType<>(ANY, "ON_POOL_ADD");
   public static final EventType<Event> ON_POOL_REMOVE = new EventType<>(ANY, "ON_POOL_REMOVE");
   public static final EventType<Event> ON_MOD_LOAD = new EventType<>(ANY, "ON_MOD_LOAD");
   public static final EventType<Event> ON_QUIT = new EventType<>(ANY, "ON_QUIT");

   private final ObservableList<EditableCharPool> charPools;
   private final ProgressView progressView;
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
   private ListView<EditableCharPool> listPool;
   @FXML
   private Button buttonAddPool;
   @FXML
   private Button buttonRemovePool;
   @FXML
   private Button buttonLoadPool;

   public MainView()
   {
      this.charPools = FXCollections.observableArrayList();
      this.progressView = new ProgressView();
      this.charPoolView = new CharPoolView();
      FxUtilities.load("MainView.fxml", this);
   }

   @FXML
   private void initialize()
   {
      itemLoad.setOnAction(event -> this.fireEvent(new Event(ON_POOL_LOAD)));
      itemSave.setOnAction(event -> this.fireEvent(new Event(ON_POOL_SAVE)));
      itemSave.disableProperty().bind(charPoolView.charPoolProperty().isNull());
      itemLoadMod.setOnAction(event -> this.fireEvent(new Event(ON_MOD_LOAD)));
      itemQuit.setOnAction(event -> this.fireEvent(new Event(ON_QUIT)));

      listPool.setItems(charPools.sorted(Comparator.comparing(p -> p.getBasePool().getName())));
      getCharPoolSelectionModel().selectedItemProperty()
            .addListener((obs, old, newValue) -> onSelectedPoolChanged(newValue));
      onSelectedPoolChanged(null);
      charPoolView.charPoolProperty().bind(listPool.getSelectionModel().selectedItemProperty());
      centerProperty().bind(Bindings.createObjectBinding(this::computeCenter, progressView.activeProperty(),
            getCharPoolSelectionModel().selectedItemProperty()));
      labelPlaceholder.textProperty().bind(Bindings.createStringBinding(this::computePlaceholderText, charPools));

      buttonAddPool.setOnAction(event -> this.fireEvent(new Event(ON_POOL_ADD)));
      buttonRemovePool.setOnAction(event -> this.fireEvent(new Event(ON_POOL_REMOVE)));
      buttonRemovePool.disableProperty().bind(charPoolView.charPoolProperty().isNull());
      buttonLoadPool.setOnAction(event -> this.fireEvent(new Event(ON_POOL_LOAD)));
   }

   private void onSelectedPoolChanged(EditableCharPool pool)
   {
      listPool.setCellFactory(
            list -> new FormattedListCell<>(this::computePoolText, pool != null ? pool.editedProperty() : null));
   }

   private String computePoolText(EditableCharPool pool)
   {
      return String.format("%s (%d)%s", pool.getBasePool().getName(), pool.getCharacters().size(),
            pool.isEdited() ? "*" : "");
   }

   private Node computeCenter()
   {
      if (progressView.isActive())
      {
         return progressView;
      }
      if (getCharPoolSelectionModel().getSelectedItem() != null)
      {
         return charPoolView;
      }
      return labelPlaceholder;
   }

   private String computePlaceholderText()
   {
      if (charPools.isEmpty())
      {
         return "Load character pool to begin";
      }
      return "Select character pool to view";
   }

   public ObservableList<EditableCharPool> getCharPools()
   {
      return charPools;
   }

   public SelectionModel<EditableCharPool> getCharPoolSelectionModel()
   {
      return listPool.getSelectionModel();
   }

   public ProgressView getProgressView()
   {
      return progressView;
   }

   public CharPoolView getCharPoolView()
   {
      return charPoolView;
   }
}

package com.wassynger;

import java.util.function.BinaryOperator;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;

public class ProgressView extends Pane
{
   private static final double GAP = 8.0;

   private final Label labelDetails;
   private final ProgressIndicator progressIndicator;
   private final BooleanProperty active;

   private Container container = null;

   public ProgressView()
   {
      this.getStyleClass().add("progress-view");
      this.labelDetails = new Label();
      this.progressIndicator = new ProgressIndicator();
      this.active = new SimpleBooleanProperty(this, "active", false);
      this.getChildren().addAll(progressIndicator, labelDetails);
   }

   @Override
   protected void layoutChildren()
   {
      double w = getWidth();
      double h = getHeight();
      double s = Math.min(w, h);

      Insets padding = getPadding();
      // Constrain label size to overall size
      double labelW = Math.min(labelDetails.prefWidth(h), w - padding.getLeft() - padding.getRight());
      double labelH = Math.min(labelDetails.prefHeight(labelW), h - padding.getTop() - padding.getBottom());
      // Image must be [16,256] pixels (w/h) - scaled according to overall size
      double imageS = Math.max(Math.min(s / 2.0, 256.0), 16.0);
      if (h > padding.getTop() + labelH + GAP + imageS + padding.getBottom())
      {
         // Layout normally - label on top, image below
         // Centered horizontally
         double imageX = w / 2.0 - imageS / 2.0;
         // Either centered or under the label (if the label is too tall)
         double imageY = Math.max(h / 2.0 - imageS / 2.0, labelH + padding.getTop() + GAP);
         progressIndicator.resizeRelocate(snapPosition(imageX), snapPosition(imageY), snapSize(imageS),
               snapSize(imageS));
         // Centered horizontally
         double labelX = w / 2.0 - labelW / 2.0;
         // Either centered between the image and the top, or along the top
         double labelY = (imageY / 2.0 + labelH + GAP < imageY) ?
               imageY / 2.0 :
               Math.max(padding.getTop(), imageY - labelH - GAP);
         labelDetails.resizeRelocate(snapPosition(labelX), snapPosition(labelY), snapSize(labelW), snapSize(labelH));
      }
      else
      {
         // Layout horizontally - label on the left, image to the right
         // Resize the label if needed to fit horizontally with the image
         labelW = Math.min(labelW, w - imageS - GAP - padding.getLeft() - padding.getRight());
         // Centered (w/r to the label and image), or as far left as possible
         double labelX = Math.max(padding.getLeft(), (w / 2.0 - (labelW + imageS + GAP) / 2.0));
         // Centered, or along the top
         double labelY = Math.max(padding.getTop(), h / 2.0 - labelH / 2.0);
         labelDetails.resizeRelocate(snapPosition(labelX), snapPosition(labelY), snapSize(labelW), snapSize(labelH));
         // To the right of the label
         double imageX = labelX + labelW + GAP;
         // Centered, or along the top
         double imageY = Math.max(padding.getTop(), h / 2.0 - imageS / 2.0);
         progressIndicator.resizeRelocate(snapPosition(imageX), snapPosition(imageY), snapSize(imageS),
               snapSize(imageS));
      }
   }

   public String getDetails()
   {
      return labelDetails.getText();
   }

   public void setDetails(String details)
   {
      labelDetails.setText(details);
   }

   public StringProperty detailsProperty()
   {
      return labelDetails.textProperty();
   }

   public double getProgress()
   {
      return progressIndicator.getProgress();
   }

   public void setProgress(double progress)
   {
      progressIndicator.setProgress(progress);
   }

   public DoubleProperty progressProperty()
   {
      return progressIndicator.progressProperty();
   }

   public boolean isActive()
   {
      return active.get();
   }

   public void setActive(boolean active)
   {
      this.active.set(active);
   }

   public BooleanProperty activeProperty()
   {
      return active;
   }

   public void bind(Task<?> task)
   {
      bind(task, null);
   }

   public void bind(Task<?> task, BinaryOperator<String> detailsCallback)
   {
      if (task == null || task.isDone())
      {
         // Nothing to do
         return;
      }
      // Instead of throwing an exception like most properties would, just
      // overwrite the bind. Might need to change the language of bind/unbind
      // to better reflect this behavior
      unbind();
      container = new Container(this, task, detailsCallback);
   }

   public void unbind()
   {
      if (container != null)
      {
         container.unbind();
      }
      else
      {
         detailsProperty().unbind();
         progressProperty().unbind();
         activeProperty().unbind();
      }
   }

   private static class Container
   {
      private final ProgressView view;
      private final Task<?> task;
      private final BinaryOperator<String> detailsCallback;
      private final ChangeListener<Worker.State> stateListener = (obs, old, newValue) -> onStateChanged(newValue);
      private final ChangeListener<String> titleListener = (obs, old, newValue) -> onTitleChanged(newValue);
      private final ChangeListener<String> messageListener = (obs, old, newValue) -> onMessageChanged(newValue);
      private final ChangeListener<? super Number> progressListener = (obs, old, newValue) -> onProgressChanged(newValue.doubleValue());

      public Container(ProgressView view, Task<?> task, BinaryOperator<String> detailsCallback)
      {
         this.view = view;
         this.task = task;
         this.detailsCallback = detailsCallback != null ? detailsCallback : this::computeDetails;
         task.stateProperty().addListener(stateListener);
         task.titleProperty().addListener(titleListener);
         task.messageProperty().addListener(messageListener);
         task.progressProperty().addListener(progressListener);
         // Init state
         onStateChanged(task.getState());
         updateDetails(task.getTitle(), task.getMessage());
         view.setProgress(task.getProgress());
      }

      private String computeDetails(String title, String message)
      {
         return title != null && !title.isEmpty() ? String.format("%s%n%s", title, message) : message;
      }

      private void onStateChanged(Worker.State newValue)
      {
         switch (newValue)
         {
         case RUNNING:
            view.setActive(true);
            break;
         case SUCCEEDED:
         case FAILED:
         case CANCELLED:
            view.setActive(false);
            unbind();
            break;
         default:
            // Do nothing
         }
      }

      private void onTitleChanged(String newValue)
      {
         updateDetails(newValue, task.getMessage());
      }

      private void onMessageChanged(String newValue)
      {
         updateDetails(task.getTitle(), newValue);
      }

      private void updateDetails(String title, String message)
      {
         view.setDetails(detailsCallback.apply(title, message));
      }

      private void onProgressChanged(double newValue)
      {
         view.setProgress(newValue);
      }

      public void unbind()
      {
         if (view.container == this)
         {
            view.container = null;
         }
         task.stateProperty().removeListener(stateListener);
         task.titleProperty().removeListener(titleListener);
         task.messageProperty().removeListener(messageListener);
         task.progressProperty().removeListener(progressListener);
      }
   }
}

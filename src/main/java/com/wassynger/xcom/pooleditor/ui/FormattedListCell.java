package com.wassynger.xcom.pooleditor.ui;

import javafx.scene.control.ListCell;
import javafx.util.Callback;

public class FormattedListCell<T> extends ListCell<T>
{
   private final Callback<? super T, String> formatter;

   /**
    * Creates a new formatted table cell.
    *
    * @param formatter the formatter callback, non-null
    * @throws NullPointerException if formatter is null
    */
   public FormattedListCell(Callback<? super T, String> formatter)
   {
      this.formatter = formatter;
   }

   @Override
   protected void updateItem(T item, boolean empty)
   {
      super.updateItem(item, empty);
      setText(item != null && !empty ? tryFormatItem(item) : "");
   }

   private String tryFormatItem(T item)
   {
      try
      {
         return formatter.call(item);
      }
      catch (Exception e)
      {
         return item.toString();
      }
   }
}

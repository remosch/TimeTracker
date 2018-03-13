package com.example.android.timetracker.dialogs;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.android.timetracker.R;
import com.example.android.timetracker.db.TimeDataContract;


public class DeleteTimeDataDialog extends DialogFragment {
  // Klassenvariablen
  public final static String ID_KEY = "Key_TimeDataId";
  private long _id = -1;

  @Override
  public void setArguments(Bundle args) {
    super.setArguments(args);

    // Auslesen der ID aus den übergebenen Argumenten
    _id = args.getLong(ID_KEY, -1);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    // Sichern der ID vor der Zerstörung des Dialoges
    outState.putLong(ID_KEY, _id);

    super.onSaveInstanceState(outState);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Auslesen der ID aus der Sicherung, falls diese vorhanden ist
    if (savedInstanceState != null && savedInstanceState.containsKey(ID_KEY)) {
      _id = savedInstanceState.getLong(ID_KEY);
    }

    // Prüfen, dass ID gesetzt wurde
    if (_id == -1) {
      throw new IllegalArgumentException("Please set id with 'setArguments' method an kes 'ID_KEY'");
    }

    // ID als finale variable für Dialog sichern
    final long id = _id;

    // Dialog initialisieren
    return new AlertDialog.Builder(getContext())
        .setTitle(R.string.DialogTitleDeleteItem) // Titel des Dialoges
        .setMessage(R.string.DialogMessageDeleteItem) // Nachricht des Dialoges
        .setPositiveButton(R.string.DeleteButton, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // Uri zum Löschen generieren
            Uri deleteUri = ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, id);

            // Löschen über Content Provider
            getContext().getContentResolver().delete(deleteUri, null, null);
          }
        }) // Löschen Button (positive Antwort)
        .setNegativeButton(R.string.CancelButton, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // Dialog einfach schließen
            dialog.dismiss();
          }
        }) // Abbrechen Button (negative Antwort)
        .create(); // Dialog erstellen
  }
}

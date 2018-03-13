package com.example.android.timetracker.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

import com.example.android.timetracker.R;
import com.example.android.timetracker.utils.ExportBroadcastReceiver;
import com.example.android.timetracker.utils.ExportService;

public class ExportDialog extends AppCompatDialogFragment {
  // Konstanten
  private final static String _MAX_VALUE_KEY = "ExportMaxValueKey";
  private final static String _CURRENT_VALUE_KEY = "ExportCurrentValueKey";

  // Klassenvariablen
  private ProgressDialog _dialog;
  private ExportBroadcastReceiver _receiver;
  private int _maxValue = 0;
  private int _currentValue = 0;

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // Speichern der Daten für die Anzeige, wenn Dialog verschwindet
    outState.putInt(_MAX_VALUE_KEY, _dialog.getMax());
    outState.putInt(_CURRENT_VALUE_KEY, _dialog.getProgress());
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Daten wiederherstellen, falls Dialog vorher zerstört wurde
    if (savedInstanceState != null
            && savedInstanceState.containsKey(_MAX_VALUE_KEY)
            && savedInstanceState.containsKey(_CURRENT_VALUE_KEY)) {
      _maxValue = savedInstanceState.getInt(_MAX_VALUE_KEY);
      _currentValue = savedInstanceState.getInt(_CURRENT_VALUE_KEY);
    }

    // Dialog initialisieren
    _dialog = new ProgressDialog(getContext());
    // Dialog Title
    _dialog.setTitle(R.string.ExportDialogTitle);
    // Dialog Text
    _dialog.setMessage(getString(R.string.ExportDialogMessage));
    // Schließen durch "daneben" Tippen, verhindern
    _dialog.setCanceledOnTouchOutside(false);
    // Abbrechen durch den Zurückbutton
    _dialog.setCancelable(true);
    _dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        cancelExport();
      }
    });
    // Typ des Dialoges festlegen (Allgemein oder Fortschritt)
    _dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    // Abbrechen Button hinzufügen
    _dialog.setButton(Dialog.BUTTON_NEGATIVE,
            getString(R.string.CancelButton),
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // Export Service mitteielen, dass die Aktion abgebrochen werden soll
                cancelExport();
              }
            });
    // Setzen der wiederhergestellten Werte
    if (_maxValue > 0) {
      _dialog.setMax(_maxValue);
    }
    if (_currentValue > 0) {
      _dialog.setProgress(_currentValue);
    }

    // Starten des Receivers
    _receiver = new ExportBroadcastReceiver(_dialog);
    IntentFilter filter = new IntentFilter();
    filter.addCategory(Intent.CATEGORY_DEFAULT); // Standardkategorie
    filter.addAction(ExportService.ACTION_SEND_MAX);
    filter.addAction(ExportService.ACTION_SEND_PROGRESS);
    filter.addAction(ExportService.RESULT_EXPORT_FINISHED);
    getContext().registerReceiver(_receiver, filter);

    return _dialog;
  }

  // Receiver deregistrieren, wenn Dialog entfernt wird
  @Override
  public void onDetach() {
    super.onDetach();
    getContext().unregisterReceiver(_receiver);
    _receiver = null;
  }

  private void cancelExport() {
    // Senden an den Export Service, dass der Export abgebrochen wurde
    Intent cancelIntent = new Intent(getContext(), ExportService.class);
    cancelIntent.setAction(ExportService.ACTION_CANCEL_EXPORT);
    getContext().startService(cancelIntent);
  }
}

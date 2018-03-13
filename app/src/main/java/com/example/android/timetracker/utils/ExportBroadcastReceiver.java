package com.example.android.timetracker.utils;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExportBroadcastReceiver extends BroadcastReceiver {
  private final ProgressDialog _dialog;

  public ExportBroadcastReceiver(ProgressDialog dialog) {
    _dialog = dialog;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (_dialog == null) {
      return;
    }

    switch (intent.getAction()) {
      case ExportService.ACTION_SEND_MAX:
        // Max. Wert setzen (aus den Extras)
        int max = intent.getIntExtra(ExportService.EXTRA_PROGRESS, 0);
        _dialog.setMax(max);
        break;

      case ExportService.ACTION_SEND_PROGRESS:
        // Aktuellen Stand setzen (aus den Extras)
        int current = intent.getIntExtra(ExportService.EXTRA_PROGRESS, 0);
        _dialog.setProgress(current);
        break;

      case ExportService.RESULT_EXPORT_FINISHED:
        // Dialog schließen
        _dialog.dismiss();
        break;
    }
  }
}

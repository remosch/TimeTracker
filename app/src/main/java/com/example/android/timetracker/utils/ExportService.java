package com.example.android.timetracker.utils;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.example.android.timetracker.BuildConfig;
import com.example.android.timetracker.db.TimeDataContract;

public class ExportService extends IntentService {
  // Konstanten
  public static final String ACTION_START_EXPORT =
    BuildConfig.APPLICATION_ID + ".ACTION_START_EXPORT";
  public static final String ACTION_CANCEL_EXPORT =
    BuildConfig.APPLICATION_ID + ".ACTION_CANCEL_EXPORT";
  public static final String RESULT_EXPORT_FINISHED =
    BuildConfig.APPLICATION_ID + ".RESULT_EXPORT_FINISHED";
  public static final String ACTION_SEND_MAX =
    BuildConfig.APPLICATION_ID + ".ACTION_SEND_MAX";
  public static final String ACTION_SEND_PROGRESS =
    BuildConfig.APPLICATION_ID + ".ACTION_SEND_PROGRESS";
  public static final String EXTRA_PROGRESS = "EXTRA_PROGRESS";

  // Flag für den Abbruch des Exports
  private boolean _isCancelled;

  /**
   * Constructor
   */
  public ExportService() {
    super("ExportService");
  }

  @Override
  public void onStart(@Nullable Intent intent, int startId) {
    // Abbruchaktion in lokalen Variable speichern
    if (ACTION_CANCEL_EXPORT.equals(intent.getAction())) {
      _isCancelled = true;
    }

    super.onStart(intent, startId);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    // Prüfen, ob der Service die Aufgabe verarbeiten kann
    if (!ACTION_START_EXPORT.equals(intent.getAction())) {
      return;
    }

    // Cancel-Status zurücksetzen
    _isCancelled = false;

    // Export
    exportData();
  }

  private void exportData() {
    Cursor data = null;

    try {
      // Abfrage aller Daten aus der Datenbak
      data = getApplicationContext().getContentResolver()
        .query(TimeDataContract.TimeData.CONTENT_URI, // Uri zu Daten
          null, // Alle Spalten
          null, // Filter
          null, // Filter Argumente
          null); // Sortierung

      // Anzahl der Datensätze bestimmen
      int dataCount = data == null ? 0 : data.getCount();

      // Abbrechen, falls keine Daten vorhanden, oder Benutzerabbruch
      if (dataCount == 0 || _isCancelled) {
        return;
      }

      // Maximalen Wert für den Dialog setzen
      sendMax(dataCount + 1); // +1 für die Spaltenzeile in CSV

      // Extern erreichbaren Speicherort bestimmen
      File externalStorage = Environment.getExternalStorageDirectory();

      // Prüfen, ob dieser Speicherort beschreibbar ist
      if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
        return;
      }

      // Export Unterverzeichnis
      File exportPath = new File(externalStorage, "export");

      // Export Datei
      File exportFile = new File(exportPath, "TimeDataLog.csv");

      // Anlegen, der eventuell nicht vorhandener Unterordner
      if (!exportFile.exists()) {
        exportPath.mkdirs();
      }

      // Writer für die CSV Datei
      BufferedWriter writer = null;
      try {
        // Writer initialisieren
        writer = new BufferedWriter(new FileWriter(exportFile));

        // Lesen der Spaltennamen aus der Ergenbismenge
        String[] columnList = data.getColumnNames();

        // Eine Zeile in der CSV-Datei
        StringBuilder line = new StringBuilder();

        // Zusammensetzen der Spaltenzeile
        for (String columnName : columnList) {
          // ";" vorsetzen, falls nicht die erste Spalte
          if (line.length() > 0) {
            line.append(';');
          }

          // Spaltennamen schreiben
          line.append(columnName);
        }

        // Schreiben der Zeile in die Datei
        writer.append(line);

        // Fortschritt melden
        sendProgress(1); // Spaltenüberschriften, erste Zeile

        // Lesen aller Daten aus der Ergebnismenge
        while (data.moveToNext() && !_isCancelled) {
          // Neue Zeile zur Datei hinzufügen
          writer.newLine();

          // Leeren des Inhaltes in der Zeilenvariable
          line.delete(0, line.length());

          // Spaltenwerte auslesen
          for (int columnIndex = 0; columnIndex < columnList.length; columnIndex++) {
            // ";" vorsetzen, falls nicht die erste Spalte
            if (line.length() > 0) {
              line.append(';');
            }

            // Prüfen auf "NULL"-Wert
            if (data.isNull(columnIndex)) {
              line.append("<NULL>");
            } else {
              line.append(data.getString(columnIndex));
            }
          }

          // Fortschritt melden
          sendProgress(data.getPosition() + 2); // +1 für '0' basierte Position + 1 für Überschriften

          // Velangsamen des Exports für die Dialog-Anzeige
          try {
            Thread.sleep(250);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          // Schreiben der Zeile in die Datei
          writer.append(line);
        }
      } catch (IOException e) {
        // Fehler beim Schreiben in die Datei
        e.printStackTrace();
      } finally {
        try {
          if (writer != null) {
            // Daten aus dem Arbeitsspeicher (Cache) in die Datei speichern
            writer.flush();

            // Ressourcen für Writer freigeben
            writer.close();
          }

          // Datei löschen, falls Benutzerabbruch
          if (_isCancelled && exportFile != null && exportFile.exists()) {
            exportFile.delete();
          }
        } catch (IOException e) {
          // Beim Bereinigen des Writers ist etwas schief gelaufen
          e.printStackTrace();
        }
      }
    } finally {
      // Schließen des Zeicher nach getanner Arbeit
      if (data != null) {
        data.close();
      }

      // Signalisieren, dass der Export abgeschlossen ist
      sendFinished();
    }
  }

  /**
   * Senden des maximalen Wertes für den Export über Broadcast
   *
   * @param maxValue maximaler Wert
   */
  private void sendMax(int maxValue) {
    Intent maxIntent = new Intent();
    maxIntent.setAction(ACTION_SEND_MAX);
    maxIntent.putExtra(EXTRA_PROGRESS, maxValue);
    maxIntent.addCategory(Intent.CATEGORY_DEFAULT);
    sendBroadcast(maxIntent);
  }

  /**
   * Senden des aktuellen Wertes für den Export über Broadcast
   *
   * @param current aktueller Exportwert
   */
  private void sendProgress(int current) {
    Intent progressIntent = new Intent();
    progressIntent.setAction(ACTION_SEND_PROGRESS);
    progressIntent.putExtra(EXTRA_PROGRESS, current);
    progressIntent.addCategory(Intent.CATEGORY_DEFAULT);
    sendBroadcast(progressIntent);
  }

  /**
   * Senden, dass der Export abgeschlossen wurde, über Broadcast
   */
  private void sendFinished() {
    Intent maxIntent = new Intent();
    maxIntent.setAction(RESULT_EXPORT_FINISHED);
    maxIntent.addCategory(Intent.CATEGORY_DEFAULT);
    sendBroadcast(maxIntent);
  }
}

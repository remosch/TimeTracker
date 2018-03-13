package com.example.android.timetracker.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

import com.example.android.timetracker.R;
import com.example.android.timetracker.db.TimeDataContract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by admin on 11.01.2018.
 */

public class CsvExporter extends AsyncTask<Void, Integer, Void> {

    private final Context _context;
    private ProgressDialog _dialog;

    public CsvExporter(Context context) {
        _context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Dialog initialisieren
        _dialog = new ProgressDialog(_context);
        // Dialog Title
        _dialog.setTitle(R.string.ExportDialogTitle);
        // Dialog Text
        _dialog.setMessage(_context.getString(R.string.ExportDialogMessage));
        // Schließen durch "daneben" Tippen, verhindern
        _dialog.setCanceledOnTouchOutside(false);
        // Abbrechen durch den Zurückbutton
        _dialog.setCancelable(true);
        // Typ des Dialoges festlegen (Allgemein oder Fortschritt)
        _dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // Abbrechen Button hinzufügen
        _dialog.setButton(Dialog.BUTTON_NEGATIVE,
                _context.getString(R.string.CancelButton),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // AsncTask mitteielen, dass die Aktion abgebrochen werden soll
                        cancel(false);
                    }
                });

        // Dialog anzeigen
        _dialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // Prüfen, ob Dialog angezeigt wird
        if (_dialog != null && _dialog.isShowing()) {
            // Schließen des Dialoges
            _dialog.dismiss();
            _dialog = null;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        // Prüfen auf den Inhalt
        if (_dialog != null && values != null && values.length == 1) {
            // Weitergabe des aktuellen Standes an das Dialog
            _dialog.setProgress(values[0]);
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        Cursor data = null;

        try {
            // Abfrage aller Daten aus der Datenbak
            data = _context.getContentResolver()
                    .query(TimeDataContract.TimeData.CONTENT_URI, // Uri zu Daten
                            null, // Alle Spalten
                            null, // Filter
                            null, // Filter Argumente
                            null); // Sortierung

            // Anzahl der Datensätze bestimmen
            int dataCount = data == null ? 0 : data.getCount();

            // Abbrechen, falls keine Daten vorhanden, oder Benutzerabbruch
            if (dataCount == 0 || isCancelled()) {
                return null;
            }

            // Maximalen Wert für den Dialog setzen
            if (_dialog != null) {
                _dialog.setMax(dataCount + 1); // +1 für die Spaltenzeile in CSV
            }

            // Extern erreichbaren Speicherort bestimmen
            File externalStorage = Environment.getExternalStorageDirectory();

            // Prüfen, ob dieser Speicherort beschreibbar ist
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return null;
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

                // Lesen aller Daten aus der Ergebnismenge
                while (data.moveToNext()) {
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
                    publishProgress(data.getPosition() + 2); // +1 für '0' basierte Position + 1 für Überschriften

                    // Verlangsamen des Exports
                    try {
                        Thread.sleep(250);
                    }catch (InterruptedException e){
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
                    if (isCancelled() && exportFile != null && exportFile.exists()) {
                        exportFile.delete();
                    }

                } catch (IOException e) {
                    // Beim Bereinigen des Writers ist etwas schief gelaufen
                    e.printStackTrace();
                }
            }

            return null;
        } finally {
            // Schließen des Zeicher nach getanner Arbeit
            if (data != null) {
                data.close();
            }
        }
    }
}

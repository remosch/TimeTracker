package com.example.android.timetracker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.timetracker.db.DbHelper;
import com.example.android.timetracker.db.TimeDataContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeTrackerActivity extends AppCompatActivity {

    private EditText _startDateTime;
    private EditText _endDateTime;
    private Button _startCommand;
    private Button _endCommand;

    private DateFormat _dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_tracker);

        _startDateTime = (EditText) findViewById(R.id.StartDateTime);
        _endDateTime = (EditText) findViewById(R.id.EndDateTime);
        _startCommand = (Button) findViewById(R.id.StartCommand);
        _endCommand = (Button) findViewById(R.id.EndCommand);

        // Tastatureingaben verhindern
        _startDateTime.setKeyListener(null);
        _endDateTime.setKeyListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialisierung aus der Datenbank
        initFromDb();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Button Klick registrieren
        _startCommand.setOnClickListener(new StartButtonClicked());
        _endCommand.setOnClickListener(new EndButtonClicked());
    }

    @Override
    protected void onPause() {
        super.onPause();

        _startCommand.setOnClickListener(null);
        _endCommand.setOnClickListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Verarbeiten dem Menüklicks
        switch (item.getItemId()) {
            case R.id.ListDataMenuItem:
                // Expliziter Intent
                Intent listDataIntent = new Intent(this, ListDataActivity.class);
                startActivity(listDataIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    // Initialisierung der Daten aus der Datenbank
    private void initFromDb() {
        // Deaktivieren der Buttons
        _startCommand.setEnabled(false);
        _endCommand.setEnabled(false);

        // Laden des offenen Datensatzes, falls vorhanden
        Cursor data = getContentResolver().query(
                TimeDataContract.TimeData.NOT_FINISHED_CONTENT_URI,
                new String[]{TimeDataContract.TimeData.Columns.START_TIME},
                null, // Keine Bedingungen
                null, // Keine Argumente
                null); // Keine Sortierung

        // Prüfen, ob Daten vorhanden sind
        if (data.moveToFirst()) {
            try {
                // mindestens ein Datensatz vorhanden
                Calendar startTime = TimeDataContract.Converter.parse(data.getString(0));
                _startDateTime.setText(_dateTimeFormatter.format(startTime.getTime()));
            } catch (ParseException e) {
                // Fehler bei der Konvertierung der Startzeit
                _startDateTime.setText("Falscher Datumsformat in der Datenbank");
            }
            // Beenden Button aktivieren
            _endDateTime.setText("");
            _endCommand.setEnabled(true);
        } else {
            // Start Button aktivieren
            _startDateTime.setText("");
            _endDateTime.setText("");
            _startCommand.setEnabled(true);
        }

        // Schließen, da die Daten nicht weiter benutzt werden
        data.close();
    }

    // Klasse für die Behandlung des Klicks auf den Start-Button
    class StartButtonClicked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // Aktuelles Datum und Uhrzeit
            Calendar currentTime = Calendar.getInstance();
            // Formatierung in ISO 8601
            String dbCurrentTime =
                    TimeDataContract.Converter.format(currentTime);
            // Zuordnen der Zeit dem Startzeit Spalte
            ContentValues values = new ContentValues();
            values.put(
                    TimeDataContract.TimeData.Columns.START_TIME,
                    dbCurrentTime);
            // Einfügen des Datensatzen in die Datenbank
            getContentResolver()
                    .insert(TimeDataContract.TimeData.CONTENT_URI, values);
            // Ausgabe des aktuellen Datums auf der Oberfläche
            _startDateTime.setText(
                    _dateTimeFormatter.format(currentTime.getTime()));
        }
    }

    class EndButtonClicked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // Aktuelles Datum und Uhrzeit
            Calendar currentTime = Calendar.getInstance();
            // Formatierung in ISO 8601
            String dbCurrentTime = TimeDataContract.Converter.format(currentTime);
            // Zuordnen der Zeit dem Endzeit Spalte
            ContentValues values = new ContentValues();
            values.put(TimeDataContract.TimeData.Columns.END_TIME, dbCurrentTime);
            // Aktualisieren des offenen Datensatzes in der Datenbank
            getContentResolver().update(TimeDataContract.TimeData.NOT_FINISHED_CONTENT_URI,
                    values, null, null);
            // Ausgabe des aktuellen Datums auf der Oberfläche
            _endDateTime.setText(_dateTimeFormatter.format(currentTime.getTime()));
        }
    }
}

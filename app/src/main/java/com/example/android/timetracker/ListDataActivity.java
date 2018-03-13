package com.example.android.timetracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.example.android.timetracker.db.TimeDataContract;
import com.example.android.timetracker.dialogs.DeleteTimeDataDialog;
import com.example.android.timetracker.dialogs.ExportDialog;
import com.example.android.timetracker.utils.CsvExporter;
import com.example.android.timetracker.utils.DateTimeViewBinder;
import com.example.android.timetracker.utils.ExportService;

/**
 * Created by admin on 09.01.2018.
 */

public class ListDataActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // Klassenvariablen
    private ListView _list;
    private SimpleCursorAdapter _adapter; // Adapter für die Listendarstellung
    private final static int _DATA_LOADER_ID = 100; // ID des Loaders (Hintergrundladen)
    private final static String[] _LOAD_COLUMNS = new String[]{
            TimeDataContract.TimeData.Columns._ID,
            TimeDataContract.TimeData.Columns.START_TIME,
            TimeDataContract.TimeData.Columns.END_TIME
    }; // Spalten zum Laden
    private final static String[] _VISIBLE_COLUMNS = new String[]{
            TimeDataContract.TimeData.Columns.START_TIME,
            TimeDataContract.TimeData.Columns.END_TIME
    }; // Sichtbare Spalten
    private final static int[] _ROW_VIEW_IDS = new int[]{
            R.id.StartTimeValue,
            R.id.EndTimeValue
    }; // IDs für Zeilen-Views
    private final static String _SORT_ORDER =
            TimeDataContract.TimeData.Columns.START_TIME + " DESC";

    private final static int _REQUEST_WRITE_PERMISSION_ID = 100; // ID für die Berechtigungsabfrage


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        // Suchen der Liste im Layout
        _list = (ListView) findViewById(R.id.DataList);

        // Initialisierung des Adapters
        _adapter = new SimpleCursorAdapter(
                this, // Context
                R.layout.row_time_data, // Layout für die Zeile
                null, // Daten für die Darstellung
                _VISIBLE_COLUMNS, // Darzustellende Spalten
                _ROW_VIEW_IDS, // IDs der Views für die Darstellung
                android.support.v4.widget.SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        ); // Änderungen an den Daten beobachten

        // Formatierung der Daten im Adapter beeinflussen
        _adapter.setViewBinder(new DateTimeViewBinder());

        // Adapter der Liste zuordnen
        _list.setAdapter(_adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Loader starten
        getSupportLoaderManager().restartLoader(
                _DATA_LOADER_ID, // ID des Loaders, der gestartet werden soll
                null, // keine Zusatzinformationen an Loader übergeben
                this); // Klasse, die das Loader-Interface implementiert
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Loader freigeben
        getSupportLoaderManager().destroyLoader(_DATA_LOADER_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Registrieren des Kontextmenüs für die Liste
        registerForContextMenu(_list);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Deregistrieren des Kontextmenüs für die Liste
        unregisterForContextMenu(_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_data_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ExportMenuItem:
                // Abfrage der Berechtigung
                if (ActivityCompat.checkSelfPermission(
                        this, // Context
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == // Benötigte Berechtigung
                        PackageManager.PERMISSION_GRANTED) { // Status der Berechtigung
                    // Berechtigung vorhanden, kann exportiert werden
                    CsvExporter exporter = new CsvExporter(this);
                    exporter.execute();
                } else {
                    // Berechtigung vom Benutzer erfragen
                    ActivityCompat.requestPermissions(
                            this, // Context
                            new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE // Gewünschte Berechtigungen
                            },
                            _REQUEST_WRITE_PERMISSION_ID // ID für Callback
                    );
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.DataList:
                // Kontextmenü für die Liste aufbauen
                getMenuInflater().inflate(R.menu.ctx_menu_data_list, menu);
                break;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.DeleteMenuItem:
                // Metainformationen zum Menüpunkt auslesen
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                // Argumente initialisieren
                Bundle arguments = new Bundle();
                arguments.putLong(DeleteTimeDataDialog.ID_KEY, info.id);

                // Dialog initialisieren
                DeleteTimeDataDialog dialog = new DeleteTimeDataDialog();
                dialog.setArguments(arguments);

                // Dialog anzeigen
                dialog.show(getSupportFragmentManager(), "DeleteDialog");
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Prüfen, von welcher Abfrage die Antwort ankommt
        if (requestCode == _REQUEST_WRITE_PERMISSION_ID) {
            // Prüfen, ob die Berechtigung erteilt wurde
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0])
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Berechtigung erteilt, Export kann nun durchgeführt werden
                exportData();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        CursorLoader loader = null;

        // Unterscheidung zwischen unterschiedlichen Loadern
        switch (loaderId) {
            case _DATA_LOADER_ID:
                loader = new CursorLoader(
                        this, // Context
                        TimeDataContract.TimeData.CONTENT_URI, // URI für den Content Provider
                        _LOAD_COLUMNS, // Zu ladende Spalten
                        null, // Filter
                        null, // Filter Argumente
                        _SORT_ORDER // Sortierung
                );
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Unterscheidung zwischen unterschiedlichen Loadern
        switch (loader.getId()) {
            case _DATA_LOADER_ID:
                _adapter.swapCursor(data); // Austauschen der Daten gegen neue im Adapter
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Unterscheidung zwischen unterschiedlichen Loadern
        switch (loader.getId()) {
            case _DATA_LOADER_ID:
                _adapter.swapCursor(null); // Daten freigeben
                break;
        }
    }

    private void exportData() {
        // Dialog anzeigen (erst, wenn die Oberfläche dazu fertig ist)
        _list.post(new Runnable() {
            @Override
            public void run() {
                // Dialog starten
                ExportDialog dialog = new ExportDialog();
                dialog.show(getSupportFragmentManager(), "ExportDialog");

                // Service für Export initialisieren
                Intent exportService = new Intent(
                        ListDataActivity.this, // Context
                        ExportService.class); // Service, dass gestartet werden soll
                exportService.setAction(ExportService.ACTION_START_EXPORT); // Action auf Export setzen

                // Service starten
                startService(exportService);
            }
        });
    }
}

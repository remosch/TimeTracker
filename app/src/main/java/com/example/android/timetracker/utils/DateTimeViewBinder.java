package com.example.android.timetracker.utils;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import com.example.android.timetracker.R;
import com.example.android.timetracker.db.TimeDataContract;

public class DateTimeViewBinder implements SimpleCursorAdapter.ViewBinder {
  private DateFormat _dateTimeFormatter = DateFormat.getDateTimeInstance(
    DateFormat.SHORT, DateFormat.SHORT); // Formatter für Datum und Uhrzeit

  @Override
  public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    // Nur TextViews verarbeiten
    if ((view instanceof TextView) == false) {
      return false;
    }

    // Wert auf NULL in der Datenbank prüfen
    if (cursor.isNull(columnIndex)) {
      ((TextView) view).setText(R.string.EmptyValuePlaceholder);
      return true;
    }

    // Wert als Datum parsen und formatieren
    try {
      Calendar date = TimeDataContract.Converter.parse(cursor.getString(columnIndex));
      // Formatieren der Ausgabe
      String value = _dateTimeFormatter.format(date.getTime());
      ((TextView) view).setText(value);
    } catch (ParseException e) {
      // Wert kann nicht in Datum umgewandelt werden
      ((TextView) view).setText(R.string.DateParseErrorMessage);
    }

    return true;
  }
}

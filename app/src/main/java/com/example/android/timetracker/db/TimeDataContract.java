package com.example.android.timetracker.db;

import android.net.Uri;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class TimeDataContract {
    /**
     * Eindeutiger Name des Providers innerhalb des Betriebssystems
     */
    public static final String AUTHORITY = "de.webducer.androidbuch.zeiterfassung.provider";

    /**
     * Basis URI zu dem Content Provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Kontrakt für Zeiten
     */
    public static final class TimeData {
        /**
         * Unterverzeichnis für die Dateb
         */
        public static final String CONTENT_DIRECTORY = "time";

        /**
         * Unterverzeichnis für offenen Datensatz
         */
        public static final String NOT_FINISHED_CONTENT_DIRECTORY = CONTENT_DIRECTORY + "/not_finished";

        /**
         * URI zu den Daten
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY);

        /**
         * Uri zu dem offenen Datensatz
         */
        public static final Uri NOT_FINISHED_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, NOT_FINISHED_CONTENT_DIRECTORY);


        /**
         * Datentyp für die Auflistung der Daten
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_DIRECTORY;

        /**
         * Datentyp für einen einzelnen Datensatz
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_DIRECTORY;

        /**
         * Verfügbare Spalten
         */
        public interface Columns extends BaseColumns {
            /**
             * Start der Aufzeichnung in ISO-8601 Format (z.B.: 2016-11-23 17:34)
             */
            String START_TIME = "start_time";

            /**
             * Ende der Aufzeichnung in ISO-8601 Format (z.B.: 2016-11-23 17:34)
             */
            String END_TIME = "end_time";
        }
    }

    /**
     * Konvertierung vom Datum und Uhrzeit für die Spalten
     */
    public static final class Converter {
        // ISO 8601-Format
        private static final String _DB_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm";

        /**
         * Formatter für das Speichern in der Datenbank oder Lesen aus dieser
         */
        public static final DateFormat DB_FORMATTER = new SimpleDateFormat(_DB_FORMAT_PATTERN, Locale.GERMANY);

        /**
         * Konvertieren der Calendar-Instance in ein String zum Speichern in die Datenbank
         *
         * @param calendarDate Calendar-Instanz
         * @return ISO 8601 formatierte Zeit für die Calendar-Instanz
         */
        public static String format(Calendar calendarDate) {
            return DB_FORMATTER.format(calendarDate.getTime());
        }

        /**
         * Konvertieren des Datum-Strings aus der Datenbank in eine Calendar-Instance
         *
         * @param dbTime Datum-String aus der Datenbank in ISO 8601 Format
         * @return Calendar-Instanz, die das Datum in der Datenbank repräsentiert
         * @throws ParseException Fehler, bei nicht validen Daten
         */
        public static Calendar parse(String dbTime) throws ParseException {
            Calendar calendar = Calendar.getInstance();
            return parse(dbTime, calendar);
        }

        /**
         * Konvertieren des Datum-String aus der Datenbank in die übergebene Calendar-Instanz
         *
         * @param dbTime       Datum-String aus der Datenbank in ISO 8601 Format
         * @param baseCalendar Calendar-Instanz, die mit dem Datum aus der Datenbank initialisiert werden soll
         * @return Basiskalender
         * @throws ParseException Fehler, bein nicht validen Daten
         */
        public static Calendar parse(String dbTime, Calendar baseCalendar) throws ParseException {
            baseCalendar.setTime(DB_FORMATTER.parse(dbTime));
            return baseCalendar;
        }
    }
}

package in.ac.iitd.btp.sensordata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tushar on 08-01-2016.
 */
public class RecordingTable extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "sensorDatabase";
    private static final String REC_TABLE_NAME = "recordingTable";
    private static final String BKP_TABLE_NAME = "backupTable";
    private String[] COLUMNS_UP;
    public String lastFileWritten;

    RecordingTable(Context context, List<Sensor> sensors) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        COLUMNS_UP = new String[sensors.size() + 2];
        for(int i = 0; i < COLUMNS_UP.length - 2; i++) {
            COLUMNS_UP[i] = "'" + sensors.get(i).getName() + "'";
        }
        COLUMNS_UP[COLUMNS_UP.length - 2] = "'Location'";
        COLUMNS_UP[COLUMNS_UP.length - 1] = "'Time'";
        Log.d("array", Arrays.toString(COLUMNS_UP));
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String TABLE_CREATE = "CREATE TABLE " + REC_TABLE_NAME + " (";
        for(int i = 0; i < COLUMNS_UP.length; i++) {
            TABLE_CREATE += COLUMNS_UP[i] + " TEXT";
            if(i < COLUMNS_UP.length - 1)
                TABLE_CREATE += ", ";
        }
        TABLE_CREATE += ");";
        db.execSQL(TABLE_CREATE);
    }

    public void addRecord(String[] data) {
        SQLiteDatabase db = this.getWritableDatabase();
        String inserter = "INSERT INTO '" + REC_TABLE_NAME + "' VALUES(";
        for(int i = 0; i < COLUMNS_UP.length; i++) {
            inserter += "'" + data[i] + "'";
            if(i != COLUMNS_UP.length - 1)
                inserter += ", ";
        }
        inserter += ");";
        db.execSQL(inserter);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean exportDatabase() {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        /**First of all we check if the external storage of the device is available for writing.
         * Remember that the external storage is not necessarily the sd card. Very often it is
         * the device storage.
         */
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e("problem", "Media not Mounted");
            return false;
        }
        else {
            //We use the Download directory for saving our .csv file.
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            File file;
            PrintWriter printWriter = null;
            try
            {
                file = new File(exportDir, "MyCSVFile" + Calendar.getInstance().getTimeInMillis() + ".csv");
                file.createNewFile();
                printWriter = new PrintWriter(new FileWriter(file));
                Log.d("address", file.getAbsolutePath());
                lastFileWritten = file.getAbsolutePath();

                Cursor curCSV = getReadableDatabase().rawQuery("SELECT * FROM '" + REC_TABLE_NAME + "';", null);
                //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
                String colN = Arrays.toString(COLUMNS_UP);
                printWriter.println(colN.substring(1, colN.length() - 1));
                //printWriter.println("DATE,ITEM,AMOUNT,CURRENCY");
                while(curCSV.moveToNext())
                {
                    String[] dat = new String[COLUMNS_UP.length];
                    for(int i = 0; i < COLUMNS_UP.length; i++) {
                        dat[i] = curCSV.getString(i);
                    }
                    String str = Arrays.toString(dat);
                    printWriter.println(str.substring(1, str.length() - 1));
                }

                curCSV.close();
            }

            catch(Exception exc) {
                //if there are any exceptions, return false
                Log.e("file exception", "Got problem here");
                exc.printStackTrace();
                return false;
            }
            finally {
                if(printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            return true;
        }
    }
}

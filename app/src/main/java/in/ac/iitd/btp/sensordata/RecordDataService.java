package in.ac.iitd.btp.sensordata;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordDataService extends Service implements SensorEventListener, LocationListener {
    Thread mThread;
    Map<Sensor, String> dataMap;
    SensorManager sMgr;
    RecordingTable table;
    Location latestLoc;
    LocationManager lMgr;
    public RecordDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        dataMap = new HashMap<Sensor, String>();
        sMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sList = MainActivity.getSensorList(getApplicationContext());
        for(Sensor s: sList) {
            dataMap.put(s, "");
        }
        table = new RecordingTable(getApplicationContext(), sList);
        lMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("Thread", "running");
        Intent sensorsListIntent = new Intent(MainActivity.sensorList);
        String sen = "";
        sensorsListIntent.putExtra(MainActivity.sensorList_Extra, sen);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sensorsListIntent);
        List<Sensor> sList = MainActivity.getSensorList(getApplicationContext());
        for(Sensor s : sList) {
            sMgr.registerListener(this, s, 100);
        }
        try {
            lMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        } catch(SecurityException e) {
            Log.e("GPS Permission", "GPS Permission not provided");
            e.printStackTrace();
        }

        mThread=  new Thread(new Runnable() {
            @Override
            public void run() {
                List<Sensor> sList = MainActivity.getSensorList(getApplicationContext());
                String[] sData = new String[sList.size() + 2];
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(100);
                        for(int i = 0; i < sList.size(); i++) {
                            Sensor s = sList.get(i);
                            sData[i] = dataMap.get(s);
                        }
                        sData[sList.size() + 1] = String.valueOf(Calendar.getInstance().getTimeInMillis());
                        if(latestLoc == null) {
                            sData[sList.size()] = "[,,,]";
                        } else {
                            sData[sList.size()] = "[" + String.valueOf(latestLoc.getLatitude()) + "," + String.valueOf(latestLoc.getLongitude()) + ","
                                    + String.valueOf(latestLoc.getAltitude()) + "," + String.valueOf(latestLoc.getSpeed()) + "]";
                        }
                    } catch (InterruptedException e) {
                        Log.e("interrupt", "interrupt reached");
                        e.printStackTrace();
                        deregister();
                        final boolean exported = table.exportDatabase();
                        Log.d("write to file", exported ? "success" : "failure");
                        (new Handler(Looper.getMainLooper())).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), (exported ? "File Written to " + table.lastFileWritten : "File writing failed"), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    } catch (SecurityException e) {
                        Log.d("location error", "security error received");
                        e.printStackTrace();
                        sData[sList.size()] = "[,,]";
                    } finally {
                        table.addRecord(sData);
                        Log.d("add record", "record added");
                    }
                }
            }
        });
        mThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mThread.interrupt();
        super.onDestroy();
    }

    public void deregister() {
        if(sMgr != null) {
            for(Sensor s : MainActivity.getSensorList(getApplicationContext())) {
                sMgr.unregisterListener(this, s);
            }
        }
        sMgr = null;
        Log.d("deregister", "unregistered all");
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Called when sensor values have changed.
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p/>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        dataMap.put(event.sensor, Arrays.toString(event.values));
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p/>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Called when the location has changed.
     * <p/>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location location) {
        latestLoc = location;
    }

    /**
     * Called when the provider status changes. This method is called when
     * a provider is unable to fetch a location or if the provider has recently
     * become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     * @param status   {@link LocationProvider#OUT_OF_SERVICE} if the
     *                 provider is out of service, and this is not expected to change in the
     *                 near future; {@link LocationProvider#TEMPORARILY_UNAVAILABLE} if
     *                 the provider is temporarily unavailable but is expected to be available
     *                 shortly; and {@link LocationProvider#AVAILABLE} if the
     *                 provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific
     *                 status variables.
     *                 <p/>
     *                 <p> A number of common key/value pairs for the extras Bundle are listed
     *                 below. Providers that use any of the keys on this list must
     *                 provide the corresponding value as described below.
     *                 <p/>
     *                 <ul>
     *                 <li> satellites - the number of satellites used to derive the fix
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {

    }
}

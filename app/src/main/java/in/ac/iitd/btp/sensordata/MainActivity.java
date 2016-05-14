package in.ac.iitd.btp.sensordata;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String sensorList = "List of Sensors";
    public static final String sensorList_Extra = "List";
    public static final String isRecording = "Recording Now";
    public static final String recIntentStorage = "Service Intent";

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(sensorList)) {
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<Sensor> sList = getSensorList(this);
        String[] sNames = new String[sList.size()];
        for(int i = 0; i < sList.size(); i++) {
            Sensor s = sList.get(i);
            sNames[i] = s.getName() + '\n' + s.getResolution() + '\n' + s.getMaximumRange() + "\n" + s.getMinDelay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sNames[i] += '\n' + s.getStringType() + '\n' + s.getMaxDelay() + '\n' + s.getReportingMode();
            }
        }
        ListView homeList = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.sensor_layout, R.id.nameView, sNames);
        homeList.setAdapter(adapter);

        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(sensorList);
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, iFilter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (getPreferences(0).getBoolean(isRecording, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_24dp, getTheme()));
            } else {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_24dp));
            }
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean rec = !getPreferences(0).getBoolean(isRecording, false);
                getPreferences(0).edit().putBoolean(isRecording, rec).commit();
                FloatingActionButton fab = (FloatingActionButton) view;
                if (rec) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_24dp, getTheme()));
                    } else {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_24dp));
                    }
                    Intent recordingIntent = new Intent(getApplicationContext(), RecordDataService.class);
                    recordingIntent.addCategory("Start Recording");
                    startService(recordingIntent);
                    getPreferences(0).edit().putString(recIntentStorage, recordingIntent.toUri(Intent.URI_INTENT_SCHEME)).commit();
                    Toast.makeText(getApplicationContext(), "Starting Recording", Toast.LENGTH_SHORT).show();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_fiber_manual_record_white_24dp, getTheme()));
                    } else {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_fiber_manual_record_white_24dp));
                    }
                    Toast.makeText(getApplicationContext(), "Stopping Recording", Toast.LENGTH_SHORT).show();
                    try {
                        Intent recordingIntent = Intent.parseUri(getPreferences(0).getString(recIntentStorage, null), Intent.URI_INTENT_SCHEME);
                        stopService(recordingIntent);
                        getPreferences(0).edit().remove("Service Intent");
                    } catch (URISyntaxException e) {
                        Log.d("Recording Intent", "Intent URI not parsed");
                        e.printStackTrace();
                    }
                }
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static List<Sensor> getSensorList(Context context) {
        List<Sensor> list = new ArrayList<Sensor>();
        Sensor s;
        int[] types = new int[]{  Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_AMBIENT_TEMPERATURE,
                Sensor.TYPE_GAME_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, Sensor.TYPE_GRAVITY,
                Sensor.TYPE_GYROSCOPE, Sensor.TYPE_HEART_RATE, Sensor.TYPE_LIGHT, Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_PRESSURE, Sensor.TYPE_PROXIMITY, Sensor.TYPE_RELATIVE_HUMIDITY,
                Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_SIGNIFICANT_MOTION, Sensor.TYPE_STEP_COUNTER,
                Sensor.TYPE_STEP_DETECTOR};
        SensorManager sMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        for(int type: types) {
            s = sMgr.getDefaultSensor(type);
            if(s != null) {
                list.add(s);
            }
        }
        return list;
    }
}

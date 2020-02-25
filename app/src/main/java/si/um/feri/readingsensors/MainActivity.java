package si.um.feri.readingsensors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.example.readingsensors.R;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int accDelay = SensorManager.SENSOR_DELAY_NORMAL; // delay for accelerometer

    private static final String TAG = "accelerometer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // get access of system sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // get reference for accelerometer
        sensorManager.registerListener(this, accelerometer , accDelay); // register accelerometer with normal sample rate
    }

    /**
     * Invoked every time the sensor detects a change.
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            processAccelerometer(event);
        }
    }

    private void processAccelerometer(SensorEvent event) {
        float[] values = event.values;

        float x = values[0];
        float y = values[1];
        float z = values[2];
        Log.d(TAG, "x= "+x+" y= "+y+" z= "+z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer , accDelay); // register sensor when activity resumed
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); // unregister sensor when activity paused
    }
}

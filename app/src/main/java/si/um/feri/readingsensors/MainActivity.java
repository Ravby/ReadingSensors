package si.um.feri.readingsensors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.readingsensors.R;

import si.um.feri.lib.AccelerometerSample;
import si.um.feri.lib.Experiment;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int accDelay = SensorManager.SENSOR_DELAY_NORMAL; // delay for accelerometer
    private Experiment experiment;

    EditText experimentNameEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startSensorBtn = findViewById(R.id.btn_start_test);
        startSensorBtn.setOnClickListener(startSensorOnClickListener);
        Button stopSensorBtn = findViewById(R.id.btn_stop_test);
        stopSensorBtn.setOnClickListener(stopSensorOnClickListener);

        experimentNameEt = findViewById(R.id.et_experiment_name);
        experiment = new Experiment();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // get access of system sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // get reference for accelerometer
    }

    /**
     * Invoked every time the sensor detects a change.
     *
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
        Log.v(TAG, "x= " + x + " y= " + y + " z= " + z);

        experiment.addSample(new AccelerometerSample(x, y, z), event.timestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private View.OnClickListener startSensorOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startButtonClicked();
        }
    };

    private View.OnClickListener stopSensorOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopButtonClicked();
        }
    };

    private void startButtonClicked() {
        sensorManager.registerListener(this, accelerometer, accDelay); // register sensor when activity resumed
        experiment.setExperimentName(experimentNameEt.getText().toString());
        experiment.start();
    }

    private void stopButtonClicked() {
        sensorManager.unregisterListener(this); // unregister sensor when activity paused
        experiment.stop();
    }
}

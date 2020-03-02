package si.um.feri.readingsensors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import si.um.feri.readingsensors.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import si.um.feri.lib.AccelerometerSample;
import si.um.feri.lib.Experiment;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 100;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int accDelay = SensorManager.SENSOR_DELAY_NORMAL; // delay for accelerometer
    private Experiment experiment;

    EditText experimentNameEt;
    Button startSensorBtn, stopSensorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startSensorBtn = findViewById(R.id.btn_start_test);
        startSensorBtn.setOnClickListener(startSensorOnClickListener);
        stopSensorBtn = findViewById(R.id.btn_stop_test);
        stopSensorBtn.setOnClickListener(stopSensorOnClickListener);
        stopSensorBtn.setEnabled(false);

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
        startSensorBtn.setEnabled(false);
        stopSensorBtn.setEnabled(true);
        sensorManager.registerListener(this, accelerometer, accDelay); // register sensor when activity resumed
        experiment.setExperimentName(experimentNameEt.getText().toString());
        experiment.start();
    }

    private void stopButtonClicked() {
        startSensorBtn.setEnabled(true);
        stopSensorBtn.setEnabled(false);
        sensorManager.unregisterListener(this); // unregister sensor when activity paused
        experiment.stop();
        saveExperimentToFile();
    }

    private void saveExperimentToFile() {

        if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
            String fileName = experiment.getExperimentName()+".csv";
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkPermission()) {
                    writeToFile(fileName, experiment.toCsv());
                } else {
                    requestPermission();
                }
            } else {
                writeToFile(fileName, experiment.toCsv());
            }
        }
    }

    private void writeToFile(String fileName, String content) {
        File file = new File(this.getExternalFilesDir("tests"), fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return;
        }
        try (FileOutputStream os = new FileOutputStream(file)){
            os.write(content.getBytes());
            Toast.makeText(this,"Experiment : "+fileName + " successfully saved!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // always request permission
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

        //ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Write permission granted.");
                String fileName = experiment.getExperimentName()+".csv";
                writeToFile(fileName, experiment.toCsv());
            } else {
                Log.v(TAG, "Write permission denied.");
            }
        }
    }

    private static boolean isExternalStorageReadOnly() {
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    private static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}

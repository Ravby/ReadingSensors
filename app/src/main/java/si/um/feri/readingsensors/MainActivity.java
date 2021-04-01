package si.um.feri.readingsensors;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private int accelerometerDelay;
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
        accelerometerDelay = SensorManager.SENSOR_DELAY_FASTEST; // could be set from UI

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // get access of system sensors
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // get reference for accelerometer
        }
    }

    /**
     * Invoked every time the sensor detects a change.
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

    private final View.OnClickListener startSensorOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startButtonClicked();
        }
    };

    private final View.OnClickListener stopSensorOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopButtonClicked();
        }
    };

    private void startButtonClicked() {
        startSensorBtn.setEnabled(false);
        stopSensorBtn.setEnabled(true);
        sensorManager.registerListener(this, accelerometer, accelerometerDelay);
        experiment.setName(experimentNameEt.getText().toString());
        experiment.start();
    }

    private void stopButtonClicked() {
        startSensorBtn.setEnabled(true);
        stopSensorBtn.setEnabled(false);
        sensorManager.unregisterListener(this, accelerometer);
        experiment.stop();
        saveExperimentToFile();
    }

    private void saveExperimentToFile() {

        if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
            String fileName = experiment.getName() + ".csv";
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
        boolean fileCreated;
        try {
            fileCreated = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (fileCreated) {
            try (FileOutputStream os = new FileOutputStream(file)) {
                os.write(content.getBytes());
                Toast.makeText(this, "Experiment : " + fileName + " successfully saved!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Write permission granted.");
                String fileName = experiment.getName() + ".csv";
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

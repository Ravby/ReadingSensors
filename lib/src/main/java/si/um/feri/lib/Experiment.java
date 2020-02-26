package si.um.feri.lib;

import java.util.ArrayList;

public class Experiment {
    private String experimentName;
    private String experimentDescription;
    private long startTime;
    private long endTime;
    private double duration;
    private long firstSampleTimestamp;
    private boolean isFirstSample;

    private ArrayList<AccelerometerSample> sampleList;

    public Experiment() {
        sampleList = new ArrayList<>();
    }

    public Experiment(String experimentName) {
        this.experimentName = experimentName;
        sampleList = new ArrayList<>();
    }

    public void start() {
        sampleList.clear();
        isFirstSample = true;
        startTime = System.nanoTime();
    }

    public void stop() {
        endTime = System.nanoTime();
        duration = nanoToSeconds(endTime - startTime);
    }

    public static double nanoToSeconds(long nano) {
        return nano / 1e9;
    }

    public void addSample(AccelerometerSample sample, long timestamp) {
        if(isFirstSample) {
            firstSampleTimestamp = timestamp;
            isFirstSample = false;
        }
        sample.timestamp = nanoToSeconds(timestamp - firstSampleTimestamp);
        sampleList.add(sample);
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getExperimentDescription() {
        return experimentDescription;
    }

    public void setExperimentDescription(String experimentDescription) {
        this.experimentDescription = experimentDescription;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getDuration() {
        return duration;
    }

    public ArrayList<AccelerometerSample> getSampleList() {
        return sampleList;
    }

    public void setSampleList(ArrayList<AccelerometerSample> sampleList) {
        this.sampleList = sampleList;
    }
}

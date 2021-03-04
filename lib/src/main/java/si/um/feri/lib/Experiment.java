package si.um.feri.lib;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Experiment {
    private String name;
    private String description = "";
    private String startDateTime;
    private String endDateTime;
    private long startTime;
    private long endTime;
    private double duration;
    private long firstSampleTimestamp;
    private boolean isFirstSample;

    private ArrayList<AccelerometerSample> sampleList;

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Experiment() {
        this(LocalDateTime.now().format(formatter));
    }

    public Experiment(String name) {
        this.name = name;
        sampleList = new ArrayList<>();
    }

    public void start() {
        sampleList.clear();
        isFirstSample = true;
        startTime = System.nanoTime();
        startDateTime = LocalDateTime.now().format(formatter);
    }

    public void stop() {
        endTime = System.nanoTime();
        duration = nanoToSeconds(endTime - startTime);
        endDateTime = LocalDateTime.now().format(formatter);
    }

    public static double nanoToSeconds(long nano) {
        return nano / 1e9;
    }

    public void addSample(AccelerometerSample sample, long timestamp) {
        if (isFirstSample) {
            firstSampleTimestamp = timestamp;
            isFirstSample = false;
        }
        sample.timestamp = nanoToSeconds(timestamp - firstSampleTimestamp);
        sampleList.add(sample);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(";").append(description).append(";").append(startDateTime)
                .append(";").append(endDateTime).append(";").append("\n");
        for (AccelerometerSample sample : sampleList) {
            sb.append(sample.timestamp);
            sb.append(";");
            sb.append(sample.x);
            sb.append(";");
            sb.append(sample.y);
            sb.append(";");
            sb.append(sample.z);
            sb.append(";");
            sb.append("\n");
        }
        return sb.toString();
    }
}

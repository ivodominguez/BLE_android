package entity;

import java.util.Date;

/**
 *
 * @author Diego Justi
 */

public class SensorData {

    private String sensorId;
    private String sensorNodeId;
    private String info;
    private Date date;

    public SensorData(String info) {

        date = new Date();
        this.info = info;
    }

    public SensorData(String sensorId, String sensorNodeId, String info) {
        this.sensorId = sensorId;
        this.sensorNodeId = sensorNodeId;
        this.info = info;
//        time = LocalTime.now();
//        date = LocalDate.now();
        date = new Date();
    }


    public SensorData(String sensorId, String sensorNodeId, Date date, String info) {
        this.sensorId = sensorId;
        this.sensorNodeId = sensorNodeId;
        this.date = date;
        this.info = info;
    }

    public SensorData() {
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorNodeId() {
        return sensorNodeId;
    }

    public void setSensorNodeId(String sensorNodeId) {
        this.sensorNodeId = sensorNodeId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}


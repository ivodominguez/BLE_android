package entity;

/**
 *
 * @author Diego Justi
 */

public class Sensor {

    private String id;
    private String name;
    private boolean active;

    public Sensor(String id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public Sensor(){
        name = "";
        id = "";
        active = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}


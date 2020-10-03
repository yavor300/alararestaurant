package alararestaurant.domain.dtos.jsons;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class EmployeeImportDto implements Serializable {

    @Expose
    private String name;

    @Expose
    private int age;

    @Expose
    private String position;

    public EmployeeImportDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}

import java.io.Serializable;

public class Identity implements Serializable{

    private String name;

    public Identity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;

public class Query implements Serializable{

    private String query;
    private ArrayList<String> options;
    private LocalTime deadline;

    public Query(String query, ArrayList<String> options, LocalTime deadline) {
        this.query = query;
        this.options = options;
        this.deadline = deadline;
    }

    public Query(String query, ArrayList<String> options) {
        this.query = query;
        this.options = options;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public LocalTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalTime deadline) {
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return query;
    }
}

import java.io.Serializable;
import java.util.ArrayList;

public class Query implements Serializable{

    private String query;
    private ArrayList<String> options;

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

    @Override
    public String toString() {
        return query;
    }
}

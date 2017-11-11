import java.util.ArrayList;

public class QuerySet {
    private ArrayList<Query> queries = new ArrayList<>();

    public boolean hasNext(){
        return queries.size() > 0;
    }

    public Query getNext(){
        return queries.remove(0);
    }

    public void addQuery(Query q){
        queries.add(q);
    }

    public void removeQuery(Query q){
        queries.remove(q);
    }

    public void clear(){
        queries = new ArrayList<>();
    }
}
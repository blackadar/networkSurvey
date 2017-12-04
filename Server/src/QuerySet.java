import java.io.*;
import java.util.ArrayList;

public class QuerySet implements Serializable {
    private ArrayList<Query> queries = new ArrayList<>();
    private int timePerQuery; //Default to be overwritten by parseText

    public QuerySet(int timePerQuery) {
        this.timePerQuery = timePerQuery;
    }

    @Deprecated
    /**
     * Our initial implementation will only read from a text file for the set of Queries.
     */
    public static QuerySet readObject(String path) {
        try {
            FileInputStream fin = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fin);
            return (QuerySet) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Error reading QuerySet.");
            e.printStackTrace();
        }
        return null;
    }

    public static QuerySet parseText(String path) {
        ArrayList<String> fromFile = new ArrayList<>();
        QuerySet toReturn = null;
        try {

            FileInputStream fin = new FileInputStream(path);
            DataInputStream din = new DataInputStream(fin);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(din));
            String line;

            while ((line = buffer.readLine()) != null) {
                line = line.trim();
                if ((line.length() != 0)) {
                    fromFile.add(line);
                }
            }
        } catch (IOException | AssertionError e) {
            System.err.println("Unable to read QuerySet from file " + path + ".");
            e.printStackTrace();
            return null;
        }
        try {
            toReturn = new QuerySet(Integer.parseInt(fromFile.get(0)));
            int workingIndex = 1;
            while (workingIndex < fromFile.size()) {
                while (!(fromFile.get(workingIndex).startsWith(":")) && workingIndex < fromFile.size()) {
                    workingIndex++;
                }
                if (workingIndex < fromFile.size()) {
                    String question = fromFile.get(workingIndex).substring(1, fromFile.get(workingIndex).length());
                    workingIndex++;

                    ArrayList<String> options = new ArrayList<>();
                    while (workingIndex < fromFile.size() && fromFile.get(workingIndex).startsWith(">")) {
                        options.add(fromFile.get(workingIndex).substring(1, fromFile.get(workingIndex).length()).trim());
                        workingIndex++;
                    }
                    toReturn.queries.add(new Query(question, options));
                }
            }

        } catch (NumberFormatException e) {
            System.err.println("QuerySet unable to be initialized.");
        }
        return toReturn;
    }

    public boolean hasNext() {
        return queries.size() > 0;
    }

    public Query getNext() {
        if (queries.size() > 0) {
            return queries.remove(0);
        } else {
            return null;
        }
    }

    public void addQuery(Query q) {
        queries.add(q);
    }

    public void removeQuery(Query q) {
        queries.remove(q);
    }

    public void clear() {
        queries = new ArrayList<>();
    }

    public int getTimePerQuery() { return timePerQuery;}

    @Deprecated
    /**
     * Our initial implementation will only read from a text file for the set of Queries.
     */
    public void write(String path) {
        try {
            FileOutputStream fout = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Error saving QuerySet.");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Query q : queries) {
            builder.append(":").append(q.getQuery()).append("\n");
            for (String s : q.getOptions()) {
                builder.append("  >").append(s).append("\n");
            }
        }
        return builder.toString();
    }
}
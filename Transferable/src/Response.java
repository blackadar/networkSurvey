import java.io.Serializable;

public class Response implements Serializable{
    Query responseTo;
    int optionSelection;

    public Response(Query responseTo, int optionSelection){
        this.responseTo = responseTo;
        this.optionSelection = optionSelection;
    }
}

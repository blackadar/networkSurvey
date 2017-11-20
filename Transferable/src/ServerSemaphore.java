import java.io.Serializable;

public class ServerSemaphore extends Semaphore implements Serializable{

    private boolean queuing;                  // 0 Server is waiting for clients to connect, until host is ready
    private boolean awaitingConfirmation;     // 1 Server is waiting for clients to confirm their presence
    private boolean inQuery;                  // 2 Server is currently in query mode, displaying a question
    private boolean displayingAnswer;         // 3 Server is displaying the answer to the question
    private boolean awaitingQuery;            // 4 Server is waiting for host to provide another question
    private boolean finishedQueries;          // 5 Server is now finished queries, sending placement in data space
    private boolean resetting;                // 6 Server is recycling state, client should reciprocate

    //Data Space
    private int placement;                    // Relevant to finishedQueries

    public ServerSemaphore(Identity identity, boolean[] state){
        if(state.length < 5) throw new IllegalArgumentException("Unable to parse state array length " + state.length + ".");
        this.identity = identity;
        queuing = state[0];
        awaitingConfirmation = state[1];
        inQuery = state[2];
        displayingAnswer = state[3];
        awaitingQuery = state[4];
        finishedQueries = state[5];
        resetting = state[6];
    }

    public ServerSemaphore(Identity identity, boolean[] state, boolean[] commands){
        this(identity,state);
        this.returnState = commands[0];
        this.displayMessage = commands[1];
        this.requestUserAttention = commands[2];
        this.reset = commands[3];
    }

    public ServerSemaphore(Identity identity, boolean[] state, boolean[] commands, String message){
        this(identity, state, commands);
        this.message = message;
    }

    public ServerSemaphore(Identity identity, boolean[] state, boolean[] commands, int placement){
        this(identity, state, commands);
        this.placement = placement;
    }

    public ServerSemaphore(Identity identity, boolean[] state, boolean[] commands, String message, int placement){
        this(identity, state, commands, message);
        this.placement = placement;
    }

    @Override
    public boolean[] getState(){
        return new boolean[]{queuing,awaitingConfirmation,inQuery,displayingAnswer,awaitingQuery,finishedQueries,resetting};
    }

    public int getPlacement(){
        return placement;
    }
}

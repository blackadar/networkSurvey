import java.io.Serializable;

public class ClientSemaphore extends Semaphore implements Serializable{

    // Client State
    private boolean awaitingReady;            // 0 Client is awaiting the start of the game
    private boolean awaitingUserResponse;     // 1 Client is awaiting user selection
    private boolean awaitingServerQuery;      // 2 Client is awaiting Server query provision



    public ClientSemaphore(Identity identity, boolean[] state){
        this.identity = identity;
        awaitingReady = state[0];
        awaitingUserResponse = state[1];
        awaitingServerQuery = state[2];
    }

    public ClientSemaphore(Identity identity, boolean[] state, boolean[] commands){
        this(identity, state);
        this.returnState = commands[0];
        this.displayMessage = commands[1];
        this.requestUserAttention = commands[2];
        this.reset = commands[3];
    }

    public ClientSemaphore(Identity identity, boolean[] state, boolean[] commands, String message){
        this(identity, state, commands);
        this.message = message;
    }

    @Override
    public boolean[] getState() {
        return new boolean[]{awaitingReady,awaitingUserResponse,awaitingServerQuery};
    }
}

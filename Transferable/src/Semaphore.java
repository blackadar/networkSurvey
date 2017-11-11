import java.io.Serializable;

public abstract class Semaphore implements Serializable{

    Identity identity;

    // Commands
    boolean returnState;              // 0 Return a semaphore immediately
    boolean displayMessage;           // 1 Server -> Client: Display Message from Server   Client -> Server: Alert host of raised hand
    boolean requestUserAttention;     // 2 Attempt to draw user's attention to a relevant event
    boolean reset;                    // 3 Return to init

    // Data Space
    String message;                   //Relevant to displayMessage

    public abstract boolean[] getState();

    public String getMessage(){
        return message;
    }

    public Identity getIdentity() {
        return identity;
    }

    public boolean[] getCommands(){
        return new boolean[]{returnState,displayMessage,requestUserAttention,reset};
    }

    public boolean hasCommand(){
        return (returnState || displayMessage || requestUserAttention || reset);
    }
}

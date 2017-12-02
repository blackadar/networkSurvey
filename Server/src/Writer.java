import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Writer {

    private File file;
    private PrintWriter printer;

    public Writer(String fileLocation) throws FileNotFoundException {
        file = new File(fileLocation);
        printer = new PrintWriter(file);
    }

    public void write(String toFile){
        printer.write(toFile + ",");
        printer.flush();
    }

    public void newLine(){
        printer.write("\n");
        printer.flush();
    }

    public void close(){
        this.printer.close();
    }
}
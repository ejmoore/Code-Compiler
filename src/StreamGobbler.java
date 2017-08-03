import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class StreamGobbler extends Thread {
   private InputStream        inputStream;
   private Consumer< String > consumer;

   public StreamGobbler( InputStream inputStream,
         Consumer< String > consumer ) {
      this.inputStream = inputStream;
      this.consumer = consumer;
   }

   public void run( ) {
      new BufferedReader( new InputStreamReader( inputStream ) )
            .lines( )
            .forEach( consumer );
   }
}
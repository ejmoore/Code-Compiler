import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ShellTools {

   public static int bashExec(
                        File workingDirectory,
                        String command,
                        long timeout,
                        TimeUnit timeunit,
                        Consumer< String > outConsumer,
                        Consumer< String > errConsumer ) throws IOException {
     if ( timeunit == null ) {
        throw new IllegalArgumentException( );
     }

     Process process = new ProcessBuilder( "bash", "-c", command )
           .directory( workingDirectory )
           .start( );
     StreamGobbler outStreamGobbler = new StreamGobbler(
           process.getInputStream( ), outConsumer );
     outStreamGobbler.start( );
     ;
     StreamGobbler errStreamGobbler = new StreamGobbler(
           process.getErrorStream( ), errConsumer );
     errStreamGobbler.start( );
     ;
     try {
        if ( !process.waitFor( timeout, timeunit ) ) {
           process.destroy( );
           if ( !process.waitFor( 10, TimeUnit.SECONDS ) ) {
              process.destroyForcibly( );
           }
           process.waitFor( );
        }
     } catch ( InterruptedException e ) {
        e.printStackTrace( );
     }
     return process.exitValue( );
  }

}

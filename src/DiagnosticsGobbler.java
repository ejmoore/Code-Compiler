import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

public final class DiagnosticsGobbler< S > implements DiagnosticListener< S > {
   private List< Diagnostic< ? extends S > > diagnostics = Collections
         .synchronizedList( new ArrayList< Diagnostic< ? extends S > >( ) );

   public void report( Diagnostic< ? extends S > diagnostic ) {
      if ( diagnostic == null ) {
         throw new IllegalArgumentException( );
      }
      // Filter Duplicates
      if ( !contains( diagnostic ) ) {
         diagnostics.add( diagnostic );
      }
   }

   private boolean contains( Diagnostic< ? extends S > diagnostic ) {
      boolean result = false;
      for ( Diagnostic< ? extends S > d : diagnostics ) {
         if ( d.getKind( ).equals( diagnostic.getKind( ) ) &&
               ( d.getCode( ) != null
                     && d.getCode( ).equals( diagnostic.getCode( ) ) )
               &&
               d.getColumnNumber( ) == diagnostic.getColumnNumber( ) &&
               d.getEndPosition( ) == diagnostic.getEndPosition( ) &&
               ( d.getSource( ) != null
                     && d.getSource( ) instanceof JavaFileObject &&
                     ( ( JavaFileObject ) d.getSource( ) ).toUri( ).equals(
                           ( ( JavaFileObject ) diagnostic.getSource( ) )
                                 .toUri( ) ) )
               &&
               d.getLineNumber( ) == diagnostic.getLineNumber( ) &&
               d.getPosition( ) == diagnostic.getEndPosition( ) &&
               d.getStartPosition( ) == diagnostic.getStartPosition( ) ) {
            result = true;
            break;
         }
      }
      return result;
   }

   /**
    * Gets a list view of diagnostics collected by this object.
    *
    * @return a list view of diagnostics
    */
   public List< Diagnostic< ? extends S > > getDiagnostics( ) {
      return Collections.unmodifiableList( diagnostics );
   }
}

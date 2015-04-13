package netflix.karyon.jersey.blocking;

import static org.junit.Assert.assertEquals;
import io.netty.util.ResourceLeakDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import netflix.karyon.Karyon;
import netflix.karyon.KaryonServer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.governator.guice.BootstrapModule;

public class JerseyBlockingTest {
  private static KaryonServer server; 
  
  static ByteArrayOutputStream buffer;
  
  @BeforeClass
  public static void setUpBefore() throws Exception {
    server = Karyon.forApplication( JerseyBlockingModule.class, (BootstrapModule[])null );
      
    server.start();
  }

  @AfterClass
  public static void cleanUpAfter() throws Exception {
      server.shutdown();
  }
  
  private static int postData( String path, String payload ) throws IOException {
    URL url = new URL( path );

    HttpURLConnection con = (HttpURLConnection)url.openConnection();
    con.setRequestMethod("POST");
    
    con.setRequestProperty("Content-Type","application/json");

    con.setDoOutput(true); 
    con.setDoInput(true); 
    con.setConnectTimeout( 10000 );
    con.setReadTimeout( 20000 );
    
    con.getOutputStream().write( payload.getBytes("UTF-8") );
    con.getOutputStream().flush();
    con.getOutputStream().close();
    
    int status = con.getResponseCode();
    
    if( status != 200 ) {
      return status;
    }
    
    //read the response
    byte[] buffer = new byte[ 1024 ];
    while( con.getInputStream().read( buffer ) > 0 ) {
        ;
    }
    
    return 200;
  }
  
  
  private String makePayload( int size ) {
    StringBuilder buffer = new StringBuilder();
    
    buffer.append("{\"key\":\"");
    for( int i = 0; i < size; ++i ) {
      buffer.append(( Byte.toString( (byte)( i & 0xFF ) ) ) );
    }
    
    return buffer.append("\"}").toString();
  }
  
  @Test
  public void runJerseyTest() throws InterruptedException {
    ExecutorService service = Executors.newCachedThreadPool();
    
    final Random rnd = new Random();
    final AtomicInteger errors = new AtomicInteger();

    //let Netty blow in our face
    ResourceLeakDetector.setLevel( ResourceLeakDetector.Level.PARANOID );

    //tap to the logger, so we can catch leak error
    Logger.getLogger( "io.netty.util.ResourceLeakDetector" ).setFilter( new Filter() {
      @Override
      public boolean isLoggable(LogRecord record) {
        if( record.getLevel() == Level.SEVERE && record.getMessage().contains("LEAK") ) {
          errors.incrementAndGet();
        }
        
        return true;
      }
    });
    
    for( int i = 0; i < 200; ++i ) {
      service.execute( new Runnable() {
        @Override
        public void run() {
          try {
            int response = postData("http://localhost:7001/test", makePayload( Math.max(1, rnd.nextInt( 1024 ) ) ) );

            if( response != 200 ) {
              errors.addAndGet( 1 );
            }
          }
          catch( Exception e ) {
            errors.addAndGet( 1 );          }
        }
      });
      
      Thread.sleep( rnd.nextInt( 100 ) );
    }

    //aid netty leak detection
    System.gc();
    
    for( int i = 0; i < 100; ++i ) {
        try {
            //do not exceeded Netty content length ~1M
            int response = postData("http://localhost:7001/test", makePayload( Math.max(1, rnd.nextInt( 127 * 1024 ) ) ) );
        
            if( response != 200 ) {
              errors.addAndGet( 1 );
            }
        }
        catch( Exception e ) {
            errors.addAndGet( 1 );          
        }
        
        //aid netty leak detection
        System.gc();
    }

    service.shutdown();
    service.awaitTermination( 100, TimeUnit.SECONDS );
    
    assertEquals( "Errors: ", 0, errors.intValue() );
  }
  
}

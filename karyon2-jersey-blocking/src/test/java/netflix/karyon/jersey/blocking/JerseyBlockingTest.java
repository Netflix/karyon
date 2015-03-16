package netflix.karyon.jersey.blocking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import netflix.karyon.Karyon;
import netflix.karyon.KaryonServer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.governator.guice.BootstrapModule;

public class JerseyBlockingTest {
  private static KaryonServer server; 
  
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

    con.getOutputStream().write( payload.getBytes("UTF-8") );
    con.getOutputStream().close();
    
    return con.getResponseCode();
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
    
    for( int i = 0; i < 200; ++i ) {
      service.execute( new Runnable() {
        @Override
        public void run() {
          try {
            int response = postData("http://localhost:7001/test", makePayload( Math.max(1, rnd.nextInt( 500 ) ) ) );

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
    
    service.shutdown();
    service.awaitTermination( 10, TimeUnit.SECONDS );
    
    assertEquals( "Errors: ", 0, errors.intValue() );
  }
  
}

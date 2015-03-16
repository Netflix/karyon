package netflix.karyon.jersey.blocking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;

public class ObservableInputStreamAdapter extends InputStream {
  private static final Logger log = LoggerFactory.getLogger( ObservableInputStreamAdapter.class );

  private final Lock lock = new ReentrantLock();
  private final Condition hasMore = lock.newCondition();
  
  private volatile boolean done = false;
  private volatile Throwable error = null;
  private final ByteBuf buffer;

  private boolean await() throws IOException {
    //await in here
    while( !done && !buffer.isReadable() ) {
      try {
        hasMore.await();
      } 
      catch (InterruptedException e) {
        // Restore interrupt status and bailout
        Thread.currentThread().interrupt();
        
        log.error("Interrupted: " + e.getMessage());
        throw new IOException( e );
      }
    }

    if( error != null ) {
      throw new IOException( error );
    }

    if( done && !buffer.isReadable() ) {
      return false;
    }
    
    return true;
  }
  
  public ObservableInputStreamAdapter(final ByteBufAllocator allocator, final Observable<ByteBuf> content) {
    buffer = allocator.buffer();
    
    content.subscribe(new Observer<ByteBuf>() {
      @Override
      public void onCompleted() {
        lock.lock();
        try {
          done = true;
          
          log.debug( "Processing complete" );
          
          hasMore.signalAll();
        }
        finally {
          lock.unlock();
        }
      }

      @Override
      public void onError(Throwable e) {
        lock.lock();
        try {
          error = e;
          done  = true;

          log.error("Observer, got error: " + e.getMessage());
          
          hasMore.signalAll();
        }
        finally {
          lock.unlock();
        }
      }

      @Override
      public void onNext(ByteBuf byteBuf) {
        lock.lock();
        try {
          buffer.writeBytes( byteBuf );
          
          hasMore.signalAll();
        }
        finally {
          lock.unlock();
        }
      }
    });
  }

  @Override
  public int available() throws IOException {
    return done ? buffer.readableBytes() : 0;
  }

  @Override
  public void mark(int readlimit) {
  }

  @Override
  public boolean markSupported() {
    return false;
  }

  @Override
  public int read() throws IOException {
    lock.lock();
    
    try {
      if( !await() ) {
        return -1;
      }
      
      return buffer.readByte() & 0xff;
    }
    finally {
      lock.unlock();
    }
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    if( b == null ) {
      throw new NullPointerException( "Null buffer" );
    }
    
    if( len < 0 || off < 0 || len > b.length - off ) {
      throw new IndexOutOfBoundsException( "Invalid index" ); 
    }
    
    if( len == 0 ) {
      return 0;
    }
    
    lock.lock();

    try {
      if( !await() ) {
        return -1;
      }

      int size = Math.min( len, buffer.readableBytes() );
      
      buffer.readBytes( b, off, size );
      
      return size;
    }
    finally {
      lock.unlock();
    }
  }

  @Override
  public void reset() throws IOException {
    throw new IOException( "Not Supported" );
  }

  @Override
  public long skip(long n) throws IOException {
    throw new IOException( "Not Supported" );
  }
}
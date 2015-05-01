package netflix.karyon.transport.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Nitesh Kant
 */
public class HttpContentInputStream extends InputStream {

    private static final Logger logger = LoggerFactory.getLogger(HttpContentInputStream.class);

    private final Lock lock = new ReentrantLock();
    private volatile boolean isClosed = false;

    private volatile boolean isCompleted = false;
    private volatile Throwable completedWithError = null;
    private final Condition contentAvailabilityMonitor = lock.newCondition();
    private final ByteBuf contentBuffer;

    public HttpContentInputStream(final ByteBufAllocator allocator, final Observable<ByteBuf> content) {
        contentBuffer = allocator.buffer();
        content.subscribe(new Observer<ByteBuf>() {
            @Override
            public void onCompleted() {
                lock.lock();
                try {
                    isCompleted = true;
                  
                    logger.debug( "Processing complete" );
                    contentAvailabilityMonitor.signalAll();
                }
                finally {
                    lock.unlock();
                }
            }

            @Override
            public void onError(Throwable e) {
                lock.lock();
                try {
                    completedWithError = e;
                    isCompleted = true;
                  
                    logger.error("Observer, got error: " + e.getMessage());
                    contentAvailabilityMonitor.signalAll();
                }
                finally {
                    lock.unlock();
                }
            }

            @Override
            public void onNext(ByteBuf byteBuf) {
                lock.lock();
                try {
                  
                    //This is not only to stop writing 0 bytes as it might seems
                    //In case of no payload request, like GET
                    //We are getting onNext( 0 bytes ), onComplete during the stress conditions
                    //AFTER we say subscriber.onCompleted() and tiered down and close
                    //request stream, this doesn't contradict logic, in fact 0 bytes is just the same as nothing to write
                    //but that save us annoying log record every time this happens
                    if( byteBuf.readableBytes() > 0 ) {
                      contentBuffer.writeBytes( byteBuf );
                    }
                  
                    contentAvailabilityMonitor.signalAll();
                }
                catch( Exception e ) {
                  logger.error("Error on server", e);
                }
                finally {
                    lock.unlock();
                }
            }
        });
    }

    @Override
    public int available() throws IOException {
        return isCompleted ? contentBuffer.readableBytes() : 0;
    }

    @Override
    public void mark(int readlimit) {
        contentBuffer.markReaderIndex();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        lock.lock();
        
        try {
            if( !await() ) {
              return -1;
            }
          
            return contentBuffer.readByte() & 0xff;
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

          int size = Math.min( len, contentBuffer.readableBytes() );
        
          contentBuffer.readBytes( b, off, size );
        
          return size;
        }
        finally {
          lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        //we need a sync block here, as the double close sometimes is reality and we want to decrement ref. counter only once
        synchronized ( this ) {
            if ( isClosed ) {
                return;
            }
          
            isClosed = true;
        }
      
        contentBuffer.release();
    }
    
    @Override
    public void reset() throws IOException {
        contentBuffer.resetReaderIndex();
    }

    @Override
    public long skip(long n) throws IOException {
        //per InputStream contract, if n is negative, 0 bytes are skipped
        if( n <= 0 ) {
            return 0;
        }
      
        if (n > Integer.MAX_VALUE) {
            return skipBytes(Integer.MAX_VALUE);
        } else {
            return skipBytes((int) n);
        }
    }

    private int skipBytes(int n) throws IOException {
        int nBytes = Math.min(available(), n);
        contentBuffer.skipBytes(nBytes);
        return nBytes;
    }
    
    private boolean await() throws IOException {
      //await in here
      while( !isCompleted && !contentBuffer.isReadable() ) {
          try {
              contentAvailabilityMonitor.await();
          } 
          catch (InterruptedException e) {
              // Restore interrupt status and bailout
              Thread.currentThread().interrupt();
            
              logger.error("Interrupted: " + e.getMessage());
              throw new IOException( e );
          }
      }

      if( completedWithError != null ) {
          throw new IOException( completedWithError );
      }

      if( isCompleted && !contentBuffer.isReadable() ) {
          return false;
      }
      
      return true;
  }
}
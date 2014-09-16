package com.netflix.karyon.transport.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Nitesh Kant
 */
public class HttpContentInputStream extends InputStream {

    private static final Logger logger = LoggerFactory.getLogger(HttpContentInputStream.class);

    private volatile boolean isCompleted = false;
    private volatile Throwable completedWithError = null;
    private final Object contentAvailabilityMonitor = new Object();
    private final ByteBuf contentBuffer;

    public HttpContentInputStream(final ByteBufAllocator allocator, final Observable<ByteBuf> content) {
        contentBuffer = allocator.buffer();
        content.subscribe(new Observer<ByteBuf>() {
            @Override
            public void onCompleted() {
                isCompleted = true;
            }

            @Override
            public void onError(Throwable e) {
                completedWithError = e;
                isCompleted = true;
            }

            @Override
            public void onNext(ByteBuf byteBuf) {
                contentBuffer.writeBytes(byteBuf);
                synchronized (contentAvailabilityMonitor) { // This is never
                    contentAvailabilityMonitor.notify();
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
        return blockingRead(new ReadFunc() {
            @Override
            public int read() {
                return contentBuffer.readByte() & 0xff;
            }
        });
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return blockingRead(new ReadFunc() {
            @Override
            public int read() throws IOException {
                int available = available();
                if (available == 0) {
                    return -1;
                }

                int availableLength = Math.min(available, len);
                contentBuffer.readBytes(b, off, availableLength);
                return availableLength;
            }
        });
    }

    @Override
    public void reset() throws IOException {
        contentBuffer.resetReaderIndex();
    }

    @Override
    public long skip(long n) throws IOException {
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

    private int blockingRead(ReadFunc readFunc) throws IOException {
        if (null != completedWithError) {
            // If observable finished with error, read is aborted irrespective of whether there is some more data in
            // the buffer or not.
            throw new IOException("Content Observable finished with error.", completedWithError);
        }

        if (isCompleted) {
            if (!contentBuffer.isReadable()) {
                return -1;
            } else {
                return readFunc.read();
            }
        } else if (!contentBuffer.isReadable()) {
            synchronized (contentAvailabilityMonitor) {
                try {
                    contentAvailabilityMonitor.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Reset the interrupted flag for the upstream to handle.
                    logger.error("Interrupted while waiting for content.", e);
                    throw new IOException("Interrupted while waiting for content.", e);
                }
            }

            /*Do not call within the sync method.*/ return read(); // Since we have been notified, we check again to see if content is available.
        } else {
            return readFunc.read();
        }
    }

    private static interface ReadFunc {

        int read() throws IOException;
    }
}
package com.picupmedia.cameraview.video.encoding;

import com.picupmedia.cameraview.internal.Pool;

import java.nio.ByteBuffer;

/**
 * A simple {@link Pool(int, Factory)} implementation for byte buffers.
 */
class ByteBufferPool extends Pool<ByteBuffer> {

    ByteBufferPool(final int bufferSize, int maxPoolSize) {
        super(maxPoolSize, new Factory<ByteBuffer>() {
            @Override
            public ByteBuffer create() {
                return ByteBuffer.allocateDirect(bufferSize);
            }
        });
    }
}

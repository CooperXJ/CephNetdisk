package com.cooper.demo.MyDownloadCallable;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

import static com.amazonaws.services.s3.internal.Constants.MB;

@Data
@AllArgsConstructor
class newDownloadS3ObjectCallable implements Callable<Long> {
    private static final Log LOG = LogFactory.getLog(newDownloadS3ObjectCallable.class);
    private final Callable<S3ObjectInputStream> serviceCall;
    private final File destinationFile;
    private final long position;
    private static final int BUFFER_SIZE = 2 * MB;

    @Override
    public Long call() throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile(destinationFile, "rw");
        FileChannel channel = randomAccessFile.getChannel();
        channel.position(position);
        S3ObjectInputStream objectContent = null;
        long filePosition;

        try {

            objectContent = serviceCall.call();
            final byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            while ((bytesRead = objectContent.read(buffer)) > -1) {
                byteBuffer.limit(bytesRead);

                while (byteBuffer.hasRemaining()) {
                    channel.write(byteBuffer);
                }
                byteBuffer.clear();
            }

            filePosition = channel.position();
        } finally {
            IOUtils.closeQuietly(objectContent, LOG);
            IOUtils.closeQuietly(randomAccessFile, LOG);
            IOUtils.closeQuietly(channel, LOG);
        }
        return filePosition;
    }
}
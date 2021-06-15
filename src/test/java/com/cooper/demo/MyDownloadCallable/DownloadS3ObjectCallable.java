package com.cooper.demo.MyDownloadCallable;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.cooper.demo.UploadObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.Callable;

import static com.amazonaws.services.s3.internal.Constants.MB;

/**
 * @author 薛进
 * @version 1.0
 * @Description
 * @date 2021/4/7 10:33 上午
 */
@Data
@AllArgsConstructor
public class DownloadS3ObjectCallable implements Callable<Long> {
    private final File destinationFile;
    private final long filePositionToWrite;  //文件写的位置
    private final Callable<S3ObjectInputStream> serviceCall;
    private int partNUmber;  //当前块
    private final MyDownloadImpl myDownload;
    private final Log LOG = LogFactory.getLog(DownloadS3ObjectCallable.class);
    private static final int BUFFER_SIZE = 2 * MB;

    @Override
    public Long call() throws Exception {
        int i = 0;
        RandomAccessFile randomAccessFile = new RandomAccessFile(destinationFile, "rw");
        FileChannel channel = randomAccessFile.getChannel();
        channel.position(filePositionToWrite);
        S3ObjectInputStream objectContent = null;
        long filePosition;
        long startPosition = filePositionToWrite;

        try {
            objectContent = serviceCall.call();
            final byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            while ((bytesRead = objectContent.read(buffer)) > -1) {
                if(myDownload.isCancelled()){
                    objectContent.abort();
                    break;
                }

                byteBuffer.limit(bytesRead);

                while (byteBuffer.hasRemaining()) {
                    int write = channel.write(byteBuffer);
                    startPosition+=write;
                    myDownload.getProgress().updateProgress(write);
                    System.out.println("下载中---------"  +myDownload.getProgress().getPercentTransferred());
                    myDownload.setDownloadLog(partNUmber,startPosition);
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

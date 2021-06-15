package com.cooper.demo.MyDownloadCallable;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.FileLocks;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.exception.FileLockException;
import com.amazonaws.services.s3.transfer.internal.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.concurrent.*;

import static com.amazonaws.services.s3.internal.ServiceUtils.createParentDirectoryIfNecessary;

/**
 * @author 薛进
 * @version 1.0
 * @Description
 * @date 2021/4/6 3:29 下午
 */
public class MyDownloadCallable extends AbstractDownloadCallable {
    private static final Log LOG = LogFactory.getLog(PresignUrlDownloadCallable.class);

    private final AmazonS3 s3;
    private final GetObjectRequest request;
    private final PresignedUrlDownloadImpl download;
    private final long perRequestDownloadSize;
    private final Long startByte;
    private final Long endByte;
    private long expectedFileLength;

    public MyDownloadCallable(ExecutorService executor, File dstfile, CountDownLatch latch, PresignedUrlDownloadImpl download,
                              boolean isDownloadParallel, ScheduledExecutorService timedExecutor, long timeout,
                              AmazonS3 s3, GetObjectRequest request, long perRequestDownloadSize,
                              Long startByte, Long endByte) {

        super(constructCallableConfig(executor, dstfile, latch, download, isDownloadParallel, timedExecutor, timeout));

        if (s3 == null || request == null || download == null) {
            throw new IllegalArgumentException();
        }
        this.s3 = s3;
        this.request = request;
        this.download = download;
        this.perRequestDownloadSize = perRequestDownloadSize;
        this.startByte = startByte;
        this.endByte = endByte;
        // only used when resuming download
        this.expectedFileLength = 0L;
    }

    @Override
    protected void setState(Transfer.TransferState transferState) {
        download.setState(transferState);
    }

    @Override
    protected void downloadAsSingleObject() {
        ObjectMetadata object = s3.getObject(request, dstfile);
        updateDownloadStatus(object);
    }

    @Override
    protected void downloadInParallel() throws Exception {
        downloadInParallelUsingRange();
    }

    private void downloadInParallelUsingRange() {
        //如果父文件不存在则创建
        createParentDirectoryIfNecessary(dstfile);

        //加锁
        if (!FileLocks.lock(dstfile)) {
            throw new FileLockException("Fail to lock " + dstfile);
        }

        long currentStart = startByte;
        long currentEnd = 0L;
        long filePositionToWrite = 0L;

        while (currentStart <= endByte) {
            // end is inclusive in setRange, so reduce size by 1
            currentEnd = currentStart + perRequestDownloadSize - 1;

            if (currentEnd > endByte) {
                currentEnd = endByte;
            }

            GetObjectRequest rangeRequest = (GetObjectRequest) request.clone();
            rangeRequest.setRange(currentStart, currentEnd);

            Callable<S3ObjectInputStream> s3Object = serviceCall(rangeRequest);
            futures.add(executor.submit(new newDownloadS3ObjectCallable(s3Object,
                    dstfile,
                    filePositionToWrite)));

            filePositionToWrite += perRequestDownloadSize;
            currentStart = currentEnd + 1;
        }

        Future<File> future = executor.submit(completeAllFutures());

        ((DownloadMonitor) download.getMonitor()).setFuture(future);
    }

    public Callable<S3ObjectInputStream> serviceCall(GetObjectRequest request){
        return () -> {
            S3ObjectInputStream objectContent = s3.getObject(request).getObjectContent();
            return objectContent;
        };
    }

    private void updateDownloadStatus(ObjectMetadata result) {
        if (result == null) {
            download.setState(Transfer.TransferState.Canceled);
            download.setMonitor(new DownloadMonitor(download, null));
        } else {
            download.setState(Transfer.TransferState.Completed);
        }
    }

    private Callable<File> completeAllFutures() {
        return () -> {
            try {
                for (Future<Long> future : futures) {
                    future.get();
                }
                download.setState(Transfer.TransferState.Completed);
            } finally {
                FileLocks.unlock(dstfile);
            }

            return dstfile;
        };
    }
}

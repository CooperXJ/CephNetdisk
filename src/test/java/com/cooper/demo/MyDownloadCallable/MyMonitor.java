package com.cooper.demo.MyDownloadCallable;

import com.amazonaws.services.s3.transfer.internal.TransferMonitor;
import com.amazonaws.services.s3.transfer.internal.future.CompositeFuture;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Future;

/**
 * @author 薛进
 * @version 1.0
 * @Description
 * @date 2021/4/7 11:15 上午
 */
public class MyMonitor implements TransferMonitor {
    private MyCompositeFuture<?> future;

    @Override
    public synchronized MyCompositeFuture<?> getFuture() {
        return future;
    }

    public synchronized void setFuture(MyCompositeFuture<Long> future){
        this.future = future;
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

}

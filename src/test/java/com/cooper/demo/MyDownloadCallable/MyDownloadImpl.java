package com.cooper.demo.MyDownloadCallable;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.internal.TransferMonitor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author 薛进
 * @version 1.0
 * @Description
 * @date 2021/4/6 7:40 下午
 */
public class MyDownloadImpl {
    private volatile TransferProgress progress;
    private volatile Map<Integer,DownloadLog> map = new ConcurrentHashMap<>();
    private MyMonitor monitor;
    private final long size;
    private List<S3ObjectInputStream> s3ObjectInputStreamList = new CopyOnWriteArrayList<>();

    public MyDownloadImpl(TransferProgress progress, int part, long start, long size, long preRequest) {
        this.progress = progress;
        this.size = size;

        for (int i = 0; i < part; i++) {
            DownloadLog downloadLog = new DownloadLog();
            downloadLog.setStart(i*preRequest);
            if((i+1)*preRequest>size){
                downloadLog.setEnd(size);
            }
            else{
                downloadLog.setEnd((i+1)*preRequest-1);
            }
            downloadLog.setCurpos(i*preRequest);
            map.put(i,downloadLog);
        }
    }

    public synchronized void setDownloadLog(Integer partNumber,long curpos){
        DownloadLog downloadLog = map.get(partNumber);
        downloadLog.setCurpos(curpos);
    }

    public synchronized Map getDownloadLog(){
        return map;
    }

    public Map<Integer,DownloadLog> pause(){
        boolean cancel = this.monitor.getFuture().cancel(true);
        System.out.println(cancel);
        return getMap();
    }


    public synchronized boolean isDone(){
        Integer[] keySet = (Integer[]) map.keySet().toArray();
        DownloadLog downloadLog = map.get(keySet[keySet.length - 1]);

        if(downloadLog.getCurpos()-0==size){
            return true;
        }
        return false;
    }

    public synchronized boolean isCancelled(){
        return this.monitor.getFuture().isCancelled();
    }

    public synchronized TransferProgress getProgress() {
        return progress;
    }

    public synchronized void setProgress(TransferProgress progress) {
        this.progress = progress;
    }

    public synchronized Map<Integer, DownloadLog> getMap() {
        return map;
    }

    public synchronized void setMap(Map<Integer, DownloadLog> map) {
        this.map = map;
    }

    public synchronized MyMonitor getMonitor() {
        return monitor;
    }

    public synchronized void setMonitor(MyMonitor monitor) {
        this.monitor = monitor;
    }

    public synchronized long getSize() {
        return size;
    }

    public List<S3ObjectInputStream> getS3ObjectInputStreamList() {
        return s3ObjectInputStreamList;
    }

    public synchronized void setS3ObjectInputStreamList(List<S3ObjectInputStream> s3ObjectInputStreamList) {
        this.s3ObjectInputStreamList = s3ObjectInputStreamList;
    }
}

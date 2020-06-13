package com.cooper.demo.Mapper;

import com.cooper.demo.Bean.RecoverFile;

import java.util.List;

public interface RecoverFileMapper {
    //获取已经删除永久文件的列表
    List<RecoverFile> getDeletedFileList(String username);

    //插入用户删除文件的信息
    void InsertDeleteInfo(RecoverFile file);

    //删除被删除的文件记录
    void deleteFiles(String username,String fileName,String bucketName);

    void deleteFilesAuto(String username,String fileName);
}

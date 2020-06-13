package com.cooper.demo.service.File;

import com.cooper.demo.Bean.RecoverFile;
import com.cooper.demo.Mapper.RecoverFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileService {
    @Autowired
    RecoverFileMapper recoverFileMapper;

    //获取删除的文件列表
    public List<RecoverFile> getDeleteFileList(String username)
    {
        return recoverFileMapper.getDeletedFileList(username);
    }

    //插入删除文件信息
    public void InsertSqlHistory(RecoverFile file)
    {
        recoverFileMapper.InsertDeleteInfo(file);
    }

    //删除Sql中的文件删除记录
    public void deleteSqlHistory(String username,String fileName,String bucketName)
    {
        recoverFileMapper.deleteFiles(username, fileName,bucketName);
    }

    public void deleteFilesAuto(String username,String fileName)
    {
        recoverFileMapper.deleteFilesAuto(username, fileName);
    }
}

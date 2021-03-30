package com.cooper.demo.service.File;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cooper.demo.Bean.FileRecover;
import com.cooper.demo.Mapper.FileRecoverMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;

@Service
public class FileService {
    @Autowired
    FileRecoverMapper fileRecoverMapper;

    //获取删除的文件列表
    public List<FileRecover> getDeleteFileList(String username)
    {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("username",username);
        return fileRecoverMapper.selectList(wrapper);
    }

    //插入删除文件信息
    public void InsertSqlHistory(FileRecover file)
    {
        fileRecoverMapper.insert(file);
    }

    //删除Sql中的文件删除记录
    public void deleteSqlHistory(String username,String fileName,String bucketName)
    {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("username",username);
        wrapper.eq("file_name",fileName);
        wrapper.eq("bucket_name",bucketName);
        fileRecoverMapper.delete(wrapper);
    }

    public void deleteFilesAuto(String username,String fileName)
    {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("username",username);
        wrapper.eq("file_name",fileName);
        fileRecoverMapper.delete(wrapper);
    }
}

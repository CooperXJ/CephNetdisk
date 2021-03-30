package com.cooper.demo.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cooper.demo.Bean.FileRecover;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileRecoverMapper extends BaseMapper<FileRecover> {
    //获取已经删除永久文件的列表
    List<FileRecover> getDeletedFileList(String username);

    //插入用户删除文件的信息
    void InsertDeleteInfo(FileRecover file);

    //删除被删除的文件记录
    void deleteFiles(String username,String fileName,String bucketName);

    void deleteFilesAuto(String username,String fileName);
}

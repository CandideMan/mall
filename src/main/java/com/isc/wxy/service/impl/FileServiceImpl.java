package com.isc.wxy.service.impl;

import com.isc.wxy.service.FileService;
import com.isc.wxy.utils.FTPUtil;
import com.isc.wxy.utils.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by XY W on 2018/5/21.
 */
@Service
public class FileServiceImpl implements FileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);


    public String upload(MultipartFile file, String path){
        String fileName = file.getOriginalFilename();
        //扩展名
        //abc.jpg
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUIDUtil.uuid()+"."+fileExtensionName;
        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);
        try {
            file.transferTo(targetFile);
            //文件已经上传成功了
             ArrayList<File>list=  new ArrayList<File>();
            list.add(targetFile);
            FTPUtil.uploadFile(list);
            //已经上传到ftp服务器上
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        //A:abc.jpg
        //B:abc.jpg
        return targetFile.getName();
    }

}

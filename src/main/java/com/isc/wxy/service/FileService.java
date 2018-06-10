package com.isc.wxy.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by XY W on 2018/5/22.
 */
public interface FileService {
    String upload(MultipartFile file, String path);
}

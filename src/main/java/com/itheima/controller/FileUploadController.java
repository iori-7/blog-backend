package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
public class FileUploadController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result<String> upLoad(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error("上传的文件不能为空");
        }
        //originalFilename 可能为 null，也可能整个文件名里没有 "."，
        //直接用 substring(lastIndexOf(".")) 会抛 StringIndexOutOfBoundsException，这里统一兜底
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            return Result.error("上传的文件缺少扩展名");
        }

        UUID uuid = UUID.randomUUID();//通过uuid保证文件名唯一，不会被其他图片覆盖
        String fileName = uuid + "." + extension;
        String url = aliOssUtil.uploadFile(fileName, file.getInputStream());
        return Result.success(url);
    }
}

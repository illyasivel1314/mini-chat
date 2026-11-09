package com.cetc28s.minichatjava.controller;

import com.cetc28s.minichatjava.exception.BusinessException;
import com.cetc28s.minichatjava.exception.CommonResult;
import com.cetc28s.minichatjava.exception.ErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {
    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    // 单文件大小上限（对齐 PRD F04：图片 ≤10MB、文件 ≤50MB）
    private static final long IMAGE_MAX_SIZE = 10 * 1024 * 1024L;
    private static final long FILE_MAX_SIZE = 50 * 1024 * 1024L;

    // 从配置文件中注入上传目录
    @Value("${spring.file.upload-dir}")
    private String uploadDir;

    @Value("${spring.file.request-address}")
    private String requestAddress;

    @PostMapping("/upload")
    public CommonResult<String> uploadFile(@RequestParam("file") MultipartFile file) {
        // 1. 校验文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCodeEnum.FILE_EMPTY);
        }

        // 2. 按类型校验大小：图片 ≤10MB，文件 ≤50MB（对齐 PRD F04）
        long size = file.getSize();
        boolean isImage = file.getContentType() != null && file.getContentType().startsWith("image/");
        if (isImage && size > IMAGE_MAX_SIZE) {
            throw new BusinessException(ErrorCodeEnum.IMAGE_TOO_LARGE);
        }
        if (!isImage && size > FILE_MAX_SIZE) {
            throw new BusinessException(ErrorCodeEnum.FILE_TOO_LARGE);
        }

        try {
            // 2. 获取原始文件名并生成新的文件名（防止重名）
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + suffix;

            // 3. 构建保存路径
            Path uploadPath = Paths.get(uploadDir);
            // 如果目录不存在，则创建
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 4. 保存文件
            Path filePath = uploadPath.resolve(newFileName);
            file.transferTo(filePath.toFile());

            log.info("文件上传成功: {}", filePath.toString());
            return CommonResult.success("http://" + requestAddress + newFileName);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCodeEnum.FILE_UPLOAD_FAIL);
        }
    }

}

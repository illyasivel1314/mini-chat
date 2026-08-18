package com.cetc28s.minichatjava.model.vo;

import lombok.Data;

@Data
public class MessageFileVo {
    // 文件地址
    private String fileAddress;
    // 文件名称
    private String fileName;
    // 文件后缀
    private String fileExtension;
    // 文件大小
    private int fileSize;
}

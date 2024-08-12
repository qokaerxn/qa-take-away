package com.qokaerxn.controller.admin;

import com.qokaerxn.constant.MessageConstant;
import com.qokaerxn.result.Result;
import com.qokaerxn.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags="通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<String> uploadFile(MultipartFile file){
        System.out.println(file+"");
        try {
            //获取文件原名
            String originalFileName = file.getOriginalFilename();
            System.out.println("原文件名："+originalFileName);
            //获取文件后缀（类型）
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            //随机生成一个文件名
            String objectName = UUID.randomUUID().toString() + extension;

            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            System.out.println("新文件名："+filePath);
            return Result.success(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}

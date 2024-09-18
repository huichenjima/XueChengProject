package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.media.model.util.MimeTypeUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ClassName: MinioTest
 * Package: com.xuecheng.media
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/17 14:01
 * @Version 1.0
 */
//@SpringBootTest
public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void upload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String path="C:\\Users\\19655\\Videos\\Captures\\小绿鲸英文文献阅读器 v2.4.2 2024-07-17 11-05-45.mp4";
//        int i = StringUtils.lastIndexOf(path, ".");
//        String houzui = null;
//        if (i>0)
//            houzui=path.substring(i);
//        //根据扩展名取出mimeType
//        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(houzui);
//        //默认情况
//        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
//        if (extensionMatch!=null)
//            mimeType=extensionMatch.getMimeType();

        String mimeType = MimeTypeUtil.getMimeType(path);
        // 'asiatrip'.
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("testbucket")
                        .object("test1/0.1/小绿鲸.mp4")
                        .filename("C:\\Users\\19655\\Videos\\Captures\\小绿鲸英文文献阅读器 v2.4.2 2024-07-17 11-05-45.mp4")
                        .contentType(mimeType)
                        .build());

    }

    @Test
    public void delete() throws Exception{
        UploadObjectArgs testbucket = UploadObjectArgs.builder()
                .bucket("testbucket")
                .object("小绿鲸.mp4")
                .filename("C:\\Users\\19655\\Videos\\Captures\\小绿鲸英文文献阅读器 v2.4.2 2024-07-17 11-05-45.mp4")
                .build();

        RemoveObjectArgs build = RemoveObjectArgs.builder().bucket("testbucket").object("小绿鲸.mp4").build();
        minioClient.removeObject(build);



    }

    @Test
    public void get() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("test1/0.1/小绿鲸.mp4").build();
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream outputStream = new FileOutputStream("D:\\ev\\XueChengProject\\codemy\\minio_data\\upload\\1a.mp4");
        IOUtils.copy(inputStream,outputStream);
        FileInputStream fileInputStream1=new FileInputStream(new File("C:\\Users\\19655\\Videos\\Captures\\小绿鲸英文文献阅读器 v2.4.2 2024-07-17 11-05-45.mp4"));
        //请注意这里不能使用FilterInputStream因为使用了网络，要比较原始文件的MD5值
        String source_md5 = DigestUtils.md5Hex(fileInputStream1);
        FileInputStream fileInputStream = new FileInputStream(new File("D:\\ev\\XueChengProject\\codemy\\minio_data\\upload\\1a.mp4"));
        String target_md5 = DigestUtils.md5Hex(fileInputStream);
        if (source_md5.equals(target_md5))
            System.out.println("加载成功");
        else
            XueChengPlusException.cast("加载失败，传输错误");



    }


    //将分块文件上传到minio
    @Test
    public void uploadminio() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

//        String path="C:\\Users\\19655\\Videos\\Captures\\小绿鲸英文文献阅读器 v2.4.2 2024-07-17 11-05-45.mp4";

        for (int i = 0; i < 2; i++) {

            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            .object("chunk/"+i)
                            .filename("D:\\ev\\XueChengProject\\codemy\\duandianTest\\chunk\\"+i)
                            .build());

            System.out.println("上传分块"+i+"成功");
            
            

        }

    }
    //合并minio文件
    @Test
    public void test() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        List<ComposeSource> sources= new ArrayList<>();
//        for (int i = 0; i < 6; i++) {
//
//            ComposeSource composeSource = ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build();
//            sources.add(composeSource);
//
//        }
        List<ComposeSource> composeSources = Stream.iterate(0, i -> ++i).limit(2).map(i -> ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build()).collect(Collectors.toList());
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mp4")
                .sources(composeSources)
                .build();
        minioClient.composeObject(composeObjectArgs);
    }


}

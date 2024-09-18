package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ClassName: BigFileTest
 * Package: com.xuecheng.media
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/31 12:45
 * @Version 1.0
 */
public class BigFileTest {
    @Test
    public void testChunk() throws Exception {
        File sourceFile=new File("D:\\ev\\XueChengProject\\codemy\\duandianTest\\小绿鲸英文文献阅读器 v2.4.2 2024-07-17 11-05-45.mp4");
        String chunkFilePath="D:\\ev\\XueChengProject\\codemy\\duandianTest\\chunk\\";

        int chunkSize=1024*1024*5;
        int chunkNum= (int)Math.ceil(sourceFile.length()*1.0/chunkSize);
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile,"r");
        byte[] bytes=new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile,"rw");
            int len=-1;
            while ((len=raf_r.read(bytes))!=-1){
                raf_rw.write(bytes,0,len);
                if (chunkFile.length()>=chunkSize)
                {
                    break;
                }


            }
            raf_rw.close();



        }
        raf_r.close();


    }

    @Test
    public void testMerge() throws IOException {

        File chunkFolder=new File("D:\\ev\\XueChengProject\\codemy\\duandianTest\\chunk\\");
        File mergeFile=new File("D:\\ev\\XueChengProject\\codemy\\duandianTest\\merge.mp4");
        File sourceFile=new File("D:\\ev\\XueChengProject\\codemy\\duandianTest\\小绿鲸英文文献阅读器 v2.4.2 2024-07-17 11-05-45.mp4");
        File[] files = chunkFolder.listFiles();
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName())-Integer.parseInt(o2.getName());
            }
        });
        byte[] bytes=new byte[1024];
        RandomAccessFile raf_rw=new RandomAccessFile(mergeFile,"rw");
        for (File file : fileList) {
            RandomAccessFile raf_r=new RandomAccessFile(file,"r");

            int len=-1;
            while ((len=raf_r.read(bytes))!=-1)
            {
                raf_rw.write(bytes,0,len);
            }
            raf_r.close();


        }
        raf_rw.close();

        FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);

        String source = DigestUtils.md5Hex(fileInputStream_source);
        String merge = DigestUtils.md5Hex(fileInputStream_merge);
        fileInputStream_source.close();
        fileInputStream_merge.close();
        if (source.equals(merge))
            System.out.println("合并成功");
        else
            System.out.println("合并失败");


    }


}

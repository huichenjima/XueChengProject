package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
@Slf4j
public class VideoTask {

    @Autowired
    private MediaFileProcessService mediaFileProcessService;
    @Autowired
    private MediaFileService mediaFileService;

 

    //工具类地址
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;

    private static Logger logger = LoggerFactory.getLogger(VideoTask.class);





    /**
     * 2、分片广播任务 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //查询待处理任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 4);

        //任务数量
        int size = mediaProcessList.size();
        log.debug("取到的视频任务数：{}",size);
        //创建一个线程池
        if (size<=0)
            return;

        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //使用的计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);

        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(()->{

                try {


                //开启任务
                Long taskId = mediaProcess.getId();
                //获取锁
                boolean b = mediaFileProcessService.startTask(taskId);
                String fileId = mediaProcess.getFileId();
                if (!b)
                {
                    log.debug("抢占任务失败，任务id:{}",taskId);
                    return;
                }

                String bucket = mediaProcess.getBucket();
                String objectName = mediaProcess.getFilePath();
                //执行视频转码
                //ffmpeg的路径
                //先下载minio的合并后的avi视频
                File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);

                if (file==null)
                {
                    log.debug("下载文件出错");
                    //保存任务失败的结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"下载文件出错");
                    return;
                }
                String absolutePath = file.getAbsolutePath();
                //源avi视频的路径
                String video_path = absolutePath;

                //转换后mp4文件的名称
                String mp4_name = mediaProcess.getFileId()+".mp4";
                File mp4File=null;
                //先创建一个临时文件，作为转换后的文件
                try {
                    mp4File = File.createTempFile("minio", ".mp4");
                } catch (IOException e) {
                    log.debug("创建临时文件异常，{}",e.getMessage());
                    //保存任务失败的结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"创建临时文件异常");
                    return;
                }
                //转换后mp4文件的路径
                String mp4_path = mp4File.getAbsolutePath();
                //创建工具类对象
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
                //开始视频转换，成功将返回success
                String result = videoUtil.generateMp4();

                if (!result.equals("success"))
                {
                    log.debug("视频转码失败，bucket:{}.objectName:{}，原因：{}",bucket,objectName,result);
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,result);
                    return;
                }


                objectName=mediaFileService.getFilePathByMd5(fileId,".mp4");

                //成功后上传到minio
                boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4_path, "video/mp4", bucket, objectName);

                if(!b1){
                    log.debug("上传MP4到minio失败,taskId:{}",taskId);
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"上传MP4到minio失败");

                }
                //保存任务处理结果
                mediaFileProcessService.saveProcessFinishStatus(taskId,"2",fileId,objectName,"成功");



                }finally {
                    //计数器减一
                    countDownLatch.countDown();
                }









            });
        });
        //阻塞，最多等待30分钟
        countDownLatch.await(30, TimeUnit.MINUTES);


//        XxlJobHelper.log("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);

//        // 业务逻辑
//        for (int i = 0; i < shardTotal; i++) {
//            if (i == shardIndex) {
//                XxlJobHelper.log("第 {} 片, 命中分片开始处理", i);
//            } else {
//                XxlJobHelper.log("第 {} 片, 忽略", i);
//            }
//        }

    }

//
//    /**
//     * 3、命令行任务
//     */
//    @XxlJob("commandJobHandler")
//    public void commandJobHandler() throws Exception {
//        String command = XxlJobHelper.getJobParam();
//        int exitValue = -1;
//
//        BufferedReader bufferedReader = null;
//        try {
//            // command process
//            ProcessBuilder processBuilder = new ProcessBuilder();
//            processBuilder.command(command);
//            processBuilder.redirectErrorStream(true);
//
//            Process process = processBuilder.start();
//            //Process process = Runtime.getRuntime().exec(command);
//
//            BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
//            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));
//
//            // command log
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                XxlJobHelper.log(line);
//            }
//
//            // command exit
//            process.waitFor();
//            exitValue = process.exitValue();
//        } catch (Exception e) {
//            XxlJobHelper.log(e);
//        } finally {
//            if (bufferedReader != null) {
//                bufferedReader.close();
//            }
//        }
//
//        if (exitValue == 0) {
//            // default success
//        } else {
//            XxlJobHelper.handleFail("command exit value("+exitValue+") is failed");
//        }
//
//    }
//
//
//    /**
//     * 4、跨平台Http任务
//     *  参数示例：
//     *      "url: http://www.baidu.com\n" +
//     *      "method: get\n" +
//     *      "data: content\n";
//     */
//    @XxlJob("httpJobHandler")
//    public void httpJobHandler() throws Exception {
//
//        // param parse
//        String param = XxlJobHelper.getJobParam();
//        if (param==null || param.trim().length()==0) {
//            XxlJobHelper.log("param["+ param +"] invalid.");
//
//            XxlJobHelper.handleFail();
//            return;
//        }
//
//        String[] httpParams = param.split("\n");
//        String url = null;
//        String method = null;
//        String data = null;
//        for (String httpParam: httpParams) {
//            if (httpParam.startsWith("url:")) {
//                url = httpParam.substring(httpParam.indexOf("url:") + 4).trim();
//            }
//            if (httpParam.startsWith("method:")) {
//                method = httpParam.substring(httpParam.indexOf("method:") + 7).trim().toUpperCase();
//            }
//            if (httpParam.startsWith("data:")) {
//                data = httpParam.substring(httpParam.indexOf("data:") + 5).trim();
//            }
//        }
//
//        // param valid
//        if (url==null || url.trim().length()==0) {
//            XxlJobHelper.log("url["+ url +"] invalid.");
//
//            XxlJobHelper.handleFail();
//            return;
//        }
//        if (method==null || !Arrays.asList("GET", "POST").contains(method)) {
//            XxlJobHelper.log("method["+ method +"] invalid.");
//
//            XxlJobHelper.handleFail();
//            return;
//        }
//        boolean isPostMethod = method.equals("POST");
//
//        // request
//        HttpURLConnection connection = null;
//        BufferedReader bufferedReader = null;
//        try {
//            // connection
//            URL realUrl = new URL(url);
//            connection = (HttpURLConnection) realUrl.openConnection();
//
//            // connection setting
//            connection.setRequestMethod(method);
//            connection.setDoOutput(isPostMethod);
//            connection.setDoInput(true);
//            connection.setUseCaches(false);
//            connection.setReadTimeout(5 * 1000);
//            connection.setConnectTimeout(3 * 1000);
//            connection.setRequestProperty("connection", "Keep-Alive");
//            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
//            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");
//
//            // do connection
//            connection.connect();
//
//            // data
//            if (isPostMethod && data!=null && data.trim().length()>0) {
//                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
//                dataOutputStream.write(data.getBytes("UTF-8"));
//                dataOutputStream.flush();
//                dataOutputStream.close();
//            }
//
//            // valid StatusCode
//            int statusCode = connection.getResponseCode();
//            if (statusCode != 200) {
//                throw new RuntimeException("Http Request StatusCode(" + statusCode + ") Invalid.");
//            }
//
//            // result
//            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
//            StringBuilder result = new StringBuilder();
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                result.append(line);
//            }
//            String responseMsg = result.toString();
//
//            XxlJobHelper.log(responseMsg);
//
//            return;
//        } catch (Exception e) {
//            XxlJobHelper.log(e);
//
//            XxlJobHelper.handleFail();
//            return;
//        } finally {
//            try {
//                if (bufferedReader != null) {
//                    bufferedReader.close();
//                }
//                if (connection != null) {
//                    connection.disconnect();
//                }
//            } catch (Exception e2) {
//                XxlJobHelper.log(e2);
//            }
//        }
//
//    }

//    /**
//     * 5、生命周期任务示例：任务初始化与销毁时，支持自定义相关逻辑；
//     */
//    @XxlJob(value = "demoJobHandler2", init = "init", destroy = "destroy")
//    public void demoJobHandler2() throws Exception {
//        XxlJobHelper.log("XXL-JOB, Hello World.");
//    }
    public void init(){
        logger.info("init");
    }
    public void destroy(){
        logger.info("destroy");
    }


}

package com.xuecheng.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.util.MimeTypeUtil;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl  extends ServiceImpl<MediaFilesMapper, MediaFiles>  implements MediaFileService {

    @Autowired
    private  MediaFilesMapper mediaFilesMapper;

    @Autowired
    private  MinioClient minioClient;
    @Autowired
    private  MediaFileService currentProxy;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Value("${minio.bucket.files}")
    private  String bucket_mediafiles;
    @Value("${minio.bucket.videofiles}")
    private  String bucket_videofiles;

    @Value("${minio.endpoint}")
    private  String minio_uri;


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/")+"/";
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //将文件上传到minio
    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        UploadObjectArgs uploadObjectArgs = null;
        try {
            uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .object(objectName)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
            System.out.println("上传成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucket, objectName, e.getMessage(), e);
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        return false;

    }

    @Transactional
    @Override
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
            //记录待处理任务
            //判断只有avi才处理
            //向mediaProcess
            addWaitingTask(mediaFiles);
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());

        }
        return mediaFiles;

    }

    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        //文件名称
        String filename = mediaFiles.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //文件mimeType
        String mimeType = MimeTypeUtil.getMimeType(filename);
        //如果是avi视频添加到视频待处理表
        if(mimeType.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1");//未处理
            mediaProcess.setFailCount(0);//失败次数默认为0
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = MimeTypeUtil.getMimeType(filename);
        String defaultFolderPath = getDefaultFolderPath();
        String fileMd5 = getFileMd5(new File(localFilePath));
        String objectname=null;
        if(StringUtils.isEmpty(objectName))
            objectname=defaultFolderPath+fileMd5+extension;
        else
            objectname=objectName;

        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectname);
        if (!result)
            XueChengPlusException.cast("上传文件失败");
//        MediaFiles one = this.lambdaQuery().eq(MediaFiles::getId, fileMd5).one();
//        if (one==null)
//        {
////            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
////            mediaFiles.setBucket(bucket_mediafiles);
////            mediaFiles.setCompanyId(companyId);
////            mediaFiles.setCreateDate(LocalDateTime.now());
////            mediaFiles.setId(fileMd5);
////            mediaFiles.setUrl("/" + bucket + "/" + objectName);
//////        mediaFiles.setTags();
////            mediaFilesMapper.insert(mediaFiles);
////            return BeanUtil.copyProperties(mediaFiles, UploadFileResultDto.class);
//            MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectname);
//            return BeanUtil.copyProperties(mediaFiles,UploadFileResultDto.class);
//
//        }
//        return BeanUtil.copyProperties(one,UploadFileResultDto.class);
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectname);
        if (mediaFiles==null)
            XueChengPlusException.cast("文件上传后保存信息失败，数据库存储失败");
        return BeanUtil.copyProperties(mediaFiles,UploadFileResultDto.class);

    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {

        //先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles!=null)
        {//再查minio
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream!=null)
                    return RestResponse.success(true);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //没找到文件
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {



        //先查询数据库
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);



            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket_videofiles)
                    .object(chunkFileFolderPath+chunkIndex)
                    .build();

        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream!=null)
                return RestResponse.success(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //没找到文件
        return RestResponse.success(false);

    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    @Override
    public RestResponse uploadchunk(String fileMd5, int chunk, String localChunkFilePath)  {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5)+chunk;
        String mimeType = MimeTypeUtil.getMimeType(null);
        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_videofiles, chunkFileFolderPath);
        if (!b)
            return RestResponse.validfail(false,"上传分块文件失败");

        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //合并
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        List<ComposeSource> composeSourceList = Stream
                .iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder().bucket(bucket_videofiles).object(chunkFileFolderPath + i).build())
                .collect(Collectors.toList());
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));

        String objectName = getFilePathByMd5(fileMd5, extension);
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder().bucket(bucket_videofiles).object(objectName).sources(composeSourceList).build();
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错,bucket:{},objectName:{},错误信息:{}",bucket_videofiles,objectName,e.getMessage());
            return RestResponse.validfail(false,"合并文件出错");
        }

        //校验minio文件是否一致
        File file = downloadFileFromMinIO(bucket_videofiles, objectName);

        try(FileInputStream mergefile = new FileInputStream(file)){
            String md5Hex = DigestUtils.md5Hex(mergefile);
            if (!md5Hex.equals(fileMd5))
            {
                log.error("校验合并文件失败md5不一致，原始文件：{}，合并文件：{}",fileMd5,md5Hex);
                return RestResponse.validfail(false,"文件合并校验失败");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(file.length());
        }
        catch (Exception e)
        {
            return RestResponse.validfail(false,"文件合并校验失败");
        }

        //合并成功要开始加入数据库

        currentProxy.addMediaFilesToDb(companyId,fileMd5,uploadFileParamsDto,bucket_videofiles,objectName);

        //删除minio分块文件
//        for (int i = 0; i < chunkTotal; i++) {
//            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucket_videofiles).object(chunkFileFolderPath).build();
//            try {
//                minioClient.removeObject(removeObjectArgs);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
        clearChunkFiles(chunkFileFolderPath,chunkTotal);

        //记录待处理任务

        return RestResponse.success(true);
    }

    public String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }

    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        return mediaFiles;
    }
}

package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * ClassName: MediaFileProcessService
 * Package: com.xuecheng.media.service
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/5 15:05
 * @Version 1.0
 */
public interface MediaFileProcessService {
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);
    public boolean startTask(long id);
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}

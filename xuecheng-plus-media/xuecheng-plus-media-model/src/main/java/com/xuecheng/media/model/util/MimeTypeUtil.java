package com.xuecheng.media.model.util;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;

import javax.activation.MimeType;

/**
 * ClassName: MimeTypeUtil
 * Package: com.xuecheng.media.model.util
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/17 14:55
 * @Version 1.0
 */
public class MimeTypeUtil {
    public static String getMimeType(String filePath)
    {
        if (filePath==null)
            filePath="";
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        int i = StringUtils.lastIndexOf(filePath, ".");
        String houzui = null;
        if (i>0)
            houzui=filePath.substring(i);
        else
            return mimeType; //找不到后缀只能返回默认情况
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(houzui);
        //默认情况

        if (extensionMatch!=null)
            mimeType=extensionMatch.getMimeType();
        return mimeType;
    }
}

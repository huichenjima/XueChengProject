package com.xuecheng.base.exception;

import lombok.Data;

/**
 * ClassName: XueChengPlusException
 * Package: com.xuecheng.base.exception
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/9 21:40
 * @Version 1.0
 */
@Data
public class XueChengPlusException extends RuntimeException{

    private String errMessage;
    private String errCode;

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public XueChengPlusException(String errMessage, String errCode) {
        this.errMessage = errMessage;
        this.errCode = errCode;
    }

    public static  void cast(String message){
        throw new XueChengPlusException(message);
    }
    public static  void cast(String message,String errCode){
        throw new XueChengPlusException(message,errCode);
    }

    public static void  cast(CommonError error){
        throw new XueChengPlusException(error.getErrMessage());
    }

}

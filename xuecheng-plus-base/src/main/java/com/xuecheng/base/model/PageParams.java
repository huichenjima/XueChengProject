package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 * @description 分页查询通用参数
 * @author hechen
 * @date 2022/9/6 14:02
 * @version 1.0
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {

    //当前页码
    @ApiModelProperty(value = "页码")
    private Long pageNo = 1L;

    //每页记录数默认值
    @ApiModelProperty(value = "每页记录数")
    private Long pageSize =10L;

//    public PageParams(){
//
//    }
//
//    public PageParams(long pageNo,long pageSize){
//        this.pageNo = pageNo;
//        this.pageSize = pageSize;
//    }



}
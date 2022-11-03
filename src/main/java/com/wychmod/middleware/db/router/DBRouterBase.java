package com.wychmod.middleware.db.router;

/**
 * @description: 数据源基础配置
 * @author: wychmod
 * @date: 2022/11/3
 */
public class DBRouterBase {

    private String tbIdx;

    public String getTbIdx() {
        return DBContextHolder.getTBKey();
    }
}

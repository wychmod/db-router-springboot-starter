package com.wychmod.middleware.db.router.strategy.impl;

import com.wychmod.middleware.db.router.DBContextHolder;
import com.wychmod.middleware.db.router.DBRouterConfig;
import com.wychmod.middleware.db.router.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: 哈希路由
 * @author: wychmod
 * @date: 2022/11/2
 */
public class DBRouterStrategyHashCode implements IDBRouterStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(DBRouterStrategyHashCode.class);

    private DBRouterConfig dbRouterConfig;

    public DBRouterStrategyHashCode(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }

    @Override
    public void doRouter(String dbKeyAttr) {
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();

        // 扰动函数： 在 JDK 的 HashMap 中，对于一个元素的存放，需要进行哈希散列。而为了让散列更加均匀，所以添加了扰动函数。
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));

        // 库表索引；相当于是把一个长条的桶，切割成段，对应分库分表中的库编号和表编号
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);

        // 设置到 ThreadLocal；
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
        LOG.debug("数据库路由 dbIdx：{} tbIdx：{}",  dbIdx, tbIdx);
    }

    @Override
    public void setDBkey(int dbIdx) {
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTBKey(String.format("%02d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear() {
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }
}

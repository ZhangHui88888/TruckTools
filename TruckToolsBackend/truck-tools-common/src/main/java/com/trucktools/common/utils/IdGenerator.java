package com.trucktools.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * ID生成器
 */
public class IdGenerator {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    /**
     * 获取下一个雪花ID
     */
    public static long nextId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 获取下一个雪花ID（字符串）
     */
    public static String nextIdStr() {
        return SNOWFLAKE.nextIdStr();
    }

    /**
     * 生成UUID
     */
    public static String uuid() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 生成带前缀的批次号
     */
    public static String batchNo(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + IdUtil.fastSimpleUUID().substring(0, 8);
    }
}


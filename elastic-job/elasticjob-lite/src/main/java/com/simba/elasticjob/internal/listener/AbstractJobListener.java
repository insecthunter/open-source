package com.simba.elasticjob.internal.listener;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

import java.nio.charset.StandardCharsets;

/**
 * @Description 作业监听器抽象类
 * @Author yuanjx3
 * @Date 2021/1/19 10:35
 * @Version V1.0
 **/
public abstract class AbstractJobListener implements CuratorCacheListener {
    @Override
    public final void event(Type type, ChildData oldData, ChildData newData) {
        if (null == newData && null == oldData){
            return;
        }
        String path = Type.NODE_DELETED == type ? oldData.getPath() : newData.getPath();
        byte[] data = Type.NODE_DELETED == type ? oldData.getData() : newData.getData();
        if (path.isEmpty()){
            return;
        }
        // 如果子节点的数据发生变化，则去执行数据更新操作
        dataChanged(path, type, null==data?"":new String(data, StandardCharsets.UTF_8));
    }

    // 具体的更新方法由实现类完成
    protected abstract void dataChanged(String path, Type eventType, String data);
}

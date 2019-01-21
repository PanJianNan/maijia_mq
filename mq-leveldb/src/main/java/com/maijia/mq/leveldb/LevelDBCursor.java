package com.maijia.mq.leveldb;

import java.io.Serializable;

/**
 * LevelDBCursor 游标
 *
 * @author panjn
 * @date 2016/12/12
 */
public class LevelDBCursor implements Serializable, Cloneable {

    private static final long serialVersionUID = -348756876229797827L;
    /** 页码 */
    private long pageNo;
    /** 页中的位置 */
    private long index;

    public long getPageNo() {
        return pageNo;
    }

    public void setPageNo(long pageNo) {
        this.pageNo = pageNo;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}

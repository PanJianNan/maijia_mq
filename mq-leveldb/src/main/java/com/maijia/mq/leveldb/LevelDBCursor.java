package com.maijia.mq.leveldb;

import java.io.Serializable;

/**
 * LevelDBCursor
 *
 * @author panjn
 * @date 2016/12/12
 */
public class LevelDBCursor implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -348756876229797827L;

    private long pageNo;

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
}

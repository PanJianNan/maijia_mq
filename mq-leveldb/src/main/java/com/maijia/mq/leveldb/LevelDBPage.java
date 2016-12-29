package com.maijia.mq.leveldb;

import java.io.Serializable;
import java.util.List;

/**
 * it store a page which include some indexes and page no
 *
 * @author panjn
 * @date 2016/12/12
 */
public class LevelDBPage implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3661168670876518992L;

    private long no;

    private List<String> indexes;

    public long getNo() {
        return no;
    }

    public void setNo(long no) {
        this.no = no;
    }

    public List<String> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<String> indexes) {
        this.indexes = indexes;
    }
}

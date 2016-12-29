package com.maijia.mq.leveldb;

import com.maijia.mq.leveldb.other.SerializeUtils;
import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * use read cursor and write cursor to store and read data, and create scheduling house-keeping to clear invalidate data
 *
 * @author panjn
 * @date 2016/12/12
 */
public class QueueMiddleComponent<E> {

    static final String KEY_READ_CURSOR = "rc";

    static final String KEY_WRITE_CURSOR = "wc";

    static final String KEY_DELETE_CURSOR = "dc";

    static final String KEY_PAGE = "p";

    static final String KEY_REAR_PAGE_NO = "rpn";

    static final String KEY_HEAD_PAGE_NO = "hpn";

    static final String SEPERATOR = "_";

    static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * leveldb queue 集合
     */
    public static final Map<String, LevelDBQueue> QUEUE_MAP = Collections.synchronizedMap(new HashMap<String, LevelDBQueue>());

    static final Map<String, Boolean> queuesStat = Collections.synchronizedMap(new HashMap<String, Boolean>());

    protected volatile LevelDBCursor readCursor;

    protected volatile LevelDBCursor writeCursor;

    /**
     * pop a message wouldn't really delete a data, it just move deleteCursor, data would be deleted by house keeping
     */
    protected volatile LevelDBCursor deleteCursor;

    protected volatile LevelDBPage currentPage;

    protected volatile Long rearPageNo;

    protected volatile Long headPageNo;

    /**
     * when operating read , it need to lock
     */
    protected final ReentrantLock readLock = new ReentrantLock();

    /**
     * when operating write, it need to lock
     */
    protected ReentrantLock writeLock = new ReentrantLock();

    /**
     * when operating delete, it need to lock
     */
    protected ReentrantLock deleteLock = new ReentrantLock();

    /**
     * maximum size of page
     */
    private int pageSize = DEFAULT_PAGE_SIZE;

    protected final Logger logger = Logger.getLogger(this.getClass());

    protected final LevelDBPersistenceAdapter adapter;

    protected final String queueName;

    /**
     * create default queue named dq --> defaultQueue
     *
     * @param adapter
     */
    public QueueMiddleComponent(LevelDBPersistenceAdapter adapter) {
        this(adapter, "dq");
    }

    public QueueMiddleComponent(LevelDBPersistenceAdapter adapter, String queueName) {
        this.adapter = adapter;
        this.queueName = queueName;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public synchronized void open() throws IOException {
        adapter.open();
        try {
            QueueMiddleComponent.queuesStat.put(queueName, true);
            load();
        } catch (PersistenceException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            logger.error(e.getMessage(), e);
        }
    }

    public synchronized void close() throws IOException {
        QueueMiddleComponent.queuesStat.put(queueName, false);
        // scan all queue is closed
        Collection<Boolean> stats = QueueMiddleComponent.queuesStat.values();
        boolean allClosed = true;
        for (Boolean stat : stats) {
            if (stat) {
                allClosed = false;
                break;
            }
        }

        if (!allClosed) {
            return;
        }
        adapter.close();
    }

    /**
     * load page info, read,write and delete cursor
     *
     * @throws PersistenceException
     */
    public void load() throws PersistenceException {
        final Lock readLock = this.readLock;
        final Lock writeLock = this.writeLock;
        readLock.lock();
        writeLock.lock();
        try {
            loadPage();
            loadCursor();
        } finally {
            readLock.unlock();
            writeLock.unlock();
        }
    }

    String buildKey(String... keys) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) {
                sb.append(SEPERATOR);
            }
            sb.append(keys[i]);
        }
        return sb.toString();
    }

    protected String wrapperKey(String key) {
        return new StringBuilder(queueName).append("_").append(key).toString();
    }

    public <T> T get(String key, Class<T> type) throws PersistenceException {
        return adapter.get(wrapperKey(key), type);
    }

    protected void put(String key, Serializable ser) throws PersistenceException {
        adapter.put(wrapperKey(key), ser);
    }

    protected void put(String key, Serializable ser, WriteBatch update) throws PersistenceException {
        try {
            update.put(wrapperKey(key).getBytes(), SerializeUtils.serialize(ser));
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    protected void loadPage() throws PersistenceException {
        rearPageNo = get(KEY_REAR_PAGE_NO, Long.class);
        if (null == rearPageNo) {
            rearPageNo = 0L;
            put(KEY_REAR_PAGE_NO, rearPageNo);
        }
        headPageNo = get(KEY_HEAD_PAGE_NO, Long.class);
        if (null == headPageNo) {
            headPageNo = 0L;
            put(KEY_HEAD_PAGE_NO, headPageNo);
        }

        currentPage = get(buildKey(KEY_PAGE, String.valueOf(rearPageNo)), LevelDBPage.class);
        if (null == currentPage) {
            currentPage = new LevelDBPage();
            currentPage.setNo(rearPageNo);
            currentPage.setIndexes(new ArrayList<String>(pageSize));
            put(buildKey(KEY_PAGE, String.valueOf(rearPageNo)), currentPage);
        }
    }

    protected void loadCursor() throws PersistenceException {
        readCursor = get(KEY_READ_CURSOR, LevelDBCursor.class);
        if (null == readCursor) {
            readCursor = new LevelDBCursor();
            readCursor.setPageNo(headPageNo);
            readCursor.setIndex(0);
            put(KEY_READ_CURSOR, readCursor);
        }

        writeCursor = get(KEY_WRITE_CURSOR, LevelDBCursor.class);
        if (null == writeCursor) {
            writeCursor = new LevelDBCursor();
            writeCursor.setPageNo(rearPageNo);
            writeCursor.setIndex(0);
            put(KEY_WRITE_CURSOR, writeCursor);
        }

        deleteCursor = get(KEY_DELETE_CURSOR, LevelDBCursor.class);
        if (null == deleteCursor) {
            deleteCursor = new LevelDBCursor();
            deleteCursor.setPageNo(headPageNo);
            deleteCursor.setIndex(0);
            put(KEY_DELETE_CURSOR, deleteCursor);
        }
    }

    protected Long calculateTotalCount() {
        final long gapPageNum = writeCursor.getPageNo() - readCursor.getPageNo();
        final long leftPageSize = writeCursor.getIndex() - readCursor.getIndex();
        return gapPageNum * pageSize + leftPageSize;
    }

    public String save(Object evt) throws PersistenceException {
        final ReentrantLock writeLock = this.writeLock;
//		writeLock.lock();
        WriteBatch wb = adapter.getDb().createWriteBatch();
        //EventSourceWrapper wrapper = new EventSourceWrapper(adapter.nextId(), evt);
        MessageWrapper<Object> messageWrapper = new MessageWrapper(adapter.nextId(), evt);
        try {
            if (adapter.getDb() == null) {
                // 表示已关闭
                return null;
            }
            if (null == writeCursor) {
                writeCursor = get(KEY_WRITE_CURSOR, LevelDBCursor.class);
                if (null == writeCursor) {
                    writeCursor = new LevelDBCursor();
                    writeCursor.setPageNo(rearPageNo);
                    writeCursor.setIndex(1);
                    put(KEY_WRITE_CURSOR, writeCursor);
                }
            }

            if (isCursorToTheEndPage(writeCursor)) {
                newPage(messageWrapper.getTxnId(), wb);
            } else {
                //long writeIndex = writeCursor.getIndex();
                currentPage.getIndexes().add(messageWrapper.getTxnId());
                writeCursor.setIndex(currentPage.getIndexes().size());
                put(KEY_WRITE_CURSOR, writeCursor, wb);
                savePage(currentPage, wb);
            }
            put(messageWrapper.getTxnId(), messageWrapper, wb);
            DB db = adapter.getDb();
            db.write(wb);
            return messageWrapper.getTxnId();
        } finally {
            try {
                wb.close();
            } catch (IOException e) {
                throw new PersistenceException(e);
            }
//			writeLock.unlock();
        }
    }

    /**
     * return the head data, and move read cursor ahead. if read cursor equals write cursor, it would return null.
     *
     * @return
     * @throws PersistenceException
     */
    public E pop() throws PersistenceException {
        final ReentrantLock readLock = this.readLock;
//		readLock.lock();
        try {
            List<E> list = pop(1);
            if (list == null || list.size() == 0)
                return null;
            return list.get(0);
        } finally {
//			readLock.unlock();
        }
    }

    /**
     * return a batch head data, and move read cursor ahead. if read cursor equals write cursor, it would return min size data.
     *
     * @param bulkSize
     * @return
     * @throws PersistenceException
     */
    public List<E> pop(int bulkSize) throws PersistenceException {
        final ReentrantLock readLock = this.readLock;
//		readLock.lock();
        try {
            long index = readCursor.getIndex();
            List<E> list = _peek(bulkSize);
            if (list == null || list.size() == 0) {
                return list;
            }
            long nextIndex = list.size() + index;
            long nextPageNo = calculateNextReadPage(list.size(), nextIndex);
            if (nextPageNo != readCursor.getPageNo()) {
                readCursor.setIndex(nextIndex % pageSize);
            } else {
                readCursor.setIndex(nextIndex);
            }
            readCursor.setPageNo(nextPageNo);
            put(KEY_READ_CURSOR, readCursor);
            return list;
        } finally {
//			readLock.unlock();
        }
    }

    long calculateNextReadPage(int popSize, long nextIndex) throws PersistenceException {
        /*if(readCursor.getPageNo() < rearPageNo){
            LevelDBPage page = getPage(readCursor.getPageNo());
			long ps = page.getIndexes()==null?pageSize:page.getIndexes().size();
			return (nextIndex/ps) + readCursor.getPageNo();
		}*/
        long increametal = nextIndex / pageSize;
        if (readCursor.getPageNo() == Long.MAX_VALUE && increametal > 0) {
            return increametal - 1;
        }
        return increametal + readCursor.getPageNo();
    }

    /**
     * return the head data, but not move read cursor
     *
     * @return
     * @throws PersistenceException
     */
    public E peek() throws PersistenceException {
        final ReentrantLock readLock = this.readLock;
        readLock.lock();
        try {
            List<E> list = _peek(1);
            if (list == null || list.size() == 0)
                return null;
            return list.get(0);
        } finally {
            readLock.unlock();
        }
    }

    protected List<E> _peek(int bulkSize) throws PersistenceException {
        if (writeCursor.getPageNo() == readCursor.getPageNo() && writeCursor.getIndex() == readCursor.getIndex()) {
            return null;
        }

        long startIndex = readCursor.getIndex();
        List<String> txnIds = new ArrayList<>();
        loadTxnIds(txnIds, (int) startIndex, readCursor.getPageNo(), bulkSize);
        List<E> list = new ArrayList<>(txnIds.size());
        for (String txnId : txnIds) {
            MessageWrapper<E> wrapper = get(txnId, MessageWrapper.class);
            list.add(wrapper == null ? null : wrapper.getMessage());
        }
        return list;
    }

    public List<E> peek(int bulkSize) throws PersistenceException {
        final ReentrantLock readLock = this.readLock;
        readLock.lock();
        try {
            return _peek(bulkSize);
        } finally {
            readLock.unlock();
        }
    }

    void loadTxnIds(final List<String> txnIds, int startIndex, long pageNo, int total) throws PersistenceException {
        LevelDBPage page = getPage(pageNo);
        if (null == page || page.getIndexes() == null || page.getIndexes().size() == 0) {
            return;
        }
        for (int i = startIndex; i < page.getIndexes().size(); i++) {
            txnIds.add(page.getIndexes().get(i));
            if (txnIds.size() == total)
                return;
        }
        if (page.getNo() == currentPage.getNo()) {
            return;
        }
        long nextPageNo = page.getNo() == Long.MAX_VALUE ? 0 : (page.getNo() + 1);
        loadTxnIds(txnIds, 0, nextPageNo, total);
    }

    boolean isCursorToTheEndPage(LevelDBCursor c) {
        long index = c.getIndex();
        return index >= pageSize;
    }

    void newPage(String txnId, WriteBatch wb) throws PersistenceException {
        // TODO once page number is to the LONG maximum, it need to move page whole
        LevelDBPage page = new LevelDBPage();
        if (currentPage.getNo() == Long.MAX_VALUE) {
            currentPage.setNo(0);
        } else {
            page.setNo(currentPage.getNo() + 1);
        }
        page.setIndexes(new ArrayList<String>());
        if (null != txnId && !"".equals(txnId)) {
            page.getIndexes().add(txnId);
        }
        saveRearPageNo(page.getNo(), wb);

        currentPage = page;
        savePage(currentPage, wb);

        writeCursor.setPageNo(page.getNo());
        writeCursor.setIndex(1);
        put(KEY_WRITE_CURSOR, writeCursor, wb);
    }

    void saveRearPageNo(Long pageNo, WriteBatch wb) throws PersistenceException {
        rearPageNo = pageNo;
        put(KEY_REAR_PAGE_NO, rearPageNo, wb);
    }

    void savePage(LevelDBPage page, WriteBatch wb) throws PersistenceException {
        put(buildKey(KEY_PAGE, String.valueOf(page.getNo())), page, wb);
    }

    LevelDBPage getPage(long pageNo) throws PersistenceException {
        return get(buildKey(KEY_PAGE, String.valueOf(pageNo)), LevelDBPage.class);
    }

    public long count() {
        return calculateTotalCount();
    }

    /**
     * 获取未被删除，但是已经pop的消息数量
     *
     * @return
     */
    public long popCount() {
        final long gapPageNum = readCursor.getPageNo() - deleteCursor.getPageNo();
        final long leftPageSize = readCursor.getIndex() - deleteCursor.getIndex();
        return gapPageNum * pageSize + leftPageSize;
    }

    public void houseKeeping() throws PersistenceException {
        final ReentrantLock deleteLock = this.deleteLock;
        if (deleteLock.tryLock()) {
            try {
                if (deleteCursor.getPageNo() == headPageNo &&
                        readCursor.getPageNo() == deleteCursor.getPageNo() && deleteCursor.getIndex() == readCursor.getIndex())
                    return;

                long pageNo = headPageNo;
                for (int i = (int) pageNo; i < readCursor.getPageNo(); i++) {
                    LevelDBPage page = getPage(i);
                    List<String> indexes = page.getIndexes();
                    if (indexes == null || indexes.size() == 0)
                        continue;
                    batchDelete(i, indexes.size(), indexes.toArray(new String[indexes.size()]));
                }
                if (readCursor.getIndex() > 0) {
                    LevelDBPage page = getPage(readCursor.getPageNo());
                    List<String> deleteIndexes = page.getIndexes().subList(0, (int) readCursor.getIndex());
                    if (deleteIndexes.size() > 0) {
                        batchDelete(readCursor.getPageNo(), deleteIndexes.size(), deleteIndexes.toArray(new String[deleteIndexes.size()]));
                    }

                }
            } finally {
                deleteLock.unlock();
            }

        }
    }

    protected void batchDelete(long pageNo, int index, String... keys) throws PersistenceException {
        if (keys == null || keys.length == 0)
            return;
        WriteBatch writeBatch = adapter.getDb().createWriteBatch();
        try {
            for (String key : keys) {
                writeBatch.delete(wrapperKey(key).getBytes());
            }

            deleteCursor.setPageNo(pageNo);
            deleteCursor.setIndex(index);
            put(KEY_DELETE_CURSOR, deleteCursor, writeBatch);
            headPageNo = pageNo;
            put(buildKey(KEY_HEAD_PAGE_NO), headPageNo, writeBatch);
            adapter.getDb().write(writeBatch);
        } catch (Exception e) {
            throw new PersistenceException(e);
        } finally {
            try {
                writeBatch.close();
            } catch (IOException e) {
                throw new PersistenceException(e);
            }
        }
    }

    public synchronized void clear() throws Exception {
        final Lock readLock = this.readLock;
        final Lock writeLock = this.writeLock;
        readLock.lock();
        writeLock.lock();
        try {
            adapter.clear();
            load();
        } finally {
            readLock.unlock();
            writeLock.unlock();
        }
    }
}

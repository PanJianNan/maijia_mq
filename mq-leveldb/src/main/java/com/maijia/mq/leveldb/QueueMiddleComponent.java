package com.maijia.mq.leveldb;

import com.maijia.mq.domain.Message;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * use read cursor and write cursor to store and read data, and create scheduling house-keeping to clear invalidate data
 * <p>
 * LevelDBQueue与LevelDBPersistenceAdapter的中间件
 *
 * @author panjn
 * @date 2016/12/12
 */
public class QueueMiddleComponent {

    static final String KEY_READ_CURSOR = "rc";

    static final String KEY_WRITE_CURSOR = "wc";

    static final String KEY_DELETE_CURSOR = "dc";

    static final String KEY_PAGE = "p";

    static final String KEY_CURRENT_PAGE_NO = "cpn";

    static final String KEY_HEAD_PAGE_NO = "hpn";

    static final String KEY_LAST_DEL_PAGE_NO = "ldpn";

    static final String SEPERATOR = "_";

    static final int DEFAULT_PAGE_SIZE = 1000;

    /**
     * leveldb queue 集合
     */
    public static final Map<String, LevelDBQueue> QUEUE_MAP = new ConcurrentHashMap();

    /**
     * Keep queues' state
     */
    static final Map<String, Boolean> QUEUES_STATE = new ConcurrentHashMap();
    /**
     * 读游标
     */
    protected volatile LevelDBCursor readCursor;
    /**
     * 写游标
     */
    protected volatile LevelDBCursor writeCursor;
    /**
     * 删除位置的游标
     * <p>
     * pop a message wouldn't really delete a data, it just move deleteCursor, data would be deleted by house keeping
     */
    protected volatile LevelDBCursor deleteCursor;
    /**
     * 当前页，即写游标所在的页
     */
    protected volatile LevelDBPage currentPage;
    /**
     * 当前页页码，即写游标所在页的页码
     */
    protected volatile Long currentPageNo;
    /**
     * 拥有有效消息的首页页码
     */
    protected volatile Long headPageNo;

    /**
     * 上一次被删除的无效页页码
     */
    protected volatile Long lastDelPageNo;

    /**
     * when operating read , it need to lock
     * //锁被我外移至LevelDBQueue
     */
    protected final ReentrantLock readLock = new ReentrantLock();

    /**
     * when operating write, it need to lock
     * //锁被我外移至LevelDBQueue
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

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final LevelDBPersistenceAdapter adapter;
    /**
     * 队列名
     */
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
            //将队列设置为可用状态
            QueueMiddleComponent.QUEUES_STATE.put(queueName, true);
            load();
        } catch (PersistenceException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            logger.error(e.getMessage(), e);
        }
    }

    public synchronized void close() throws IOException {
        QueueMiddleComponent.QUEUES_STATE.put(queueName, false);
        // scan all queue is closed
        Collection<Boolean> stats = QueueMiddleComponent.QUEUES_STATE.values();
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
     * Load page info, read,write and delete cursor
     * <p>
     * 加载该队列对应的读、写、删除的游标数据
     *
     * @throws PersistenceException
     */
    public void load() throws PersistenceException {
        //锁被我外移至LevelDBQueue
//        final Lock readLock = this.readLock;
//        final Lock writeLock = this.writeLock;
//        readLock.lock();
//        writeLock.lock();
        try {
            loadPage();
            loadCursor();
        } finally {
//            readLock.unlock();
//            writeLock.unlock();
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

    /**
     * Get an object from leveldb
     *
     * @param key
     * @param type
     * @throws PersistenceException
     */
    public <T> T get(String key, Class<T> type) throws PersistenceException {
        return adapter.get(wrapperKey(key), type);
    }

    /**
     * Inserts the serializable object into the leveldb
     *
     * @param key
     * @param ser
     * @throws PersistenceException
     */
    protected void put(String key, Serializable ser) throws PersistenceException {
        adapter.put(wrapperKey(key), ser);
    }

    /**
     * Batch inserts the serializable object into the leveldb
     * 先将所有的操作记录下来，然后再一起操作。
     *
     * @param key
     * @param ser
     * @throws PersistenceException
     */
    protected void put(String key, Serializable ser, WriteBatch update) throws PersistenceException {
        adapter.put(wrapperKey(key), ser, update);
    }

    /**
     * Load page info from levedb
     *
     * @throws PersistenceException
     */
    protected void loadPage() throws PersistenceException {
        currentPageNo = get(KEY_CURRENT_PAGE_NO, Long.class);
        if (currentPageNo == null) {
            currentPageNo = 0L;
            put(KEY_CURRENT_PAGE_NO, currentPageNo);
        }

        currentPage = get(buildKey(KEY_PAGE, String.valueOf(currentPageNo)), LevelDBPage.class);
        if (currentPage == null) {
            currentPage = new LevelDBPage();
            currentPage.setNo(currentPageNo);
            currentPage.setIndexes(new ArrayList<>(pageSize));
            put(buildKey(KEY_PAGE, String.valueOf(currentPageNo)), currentPage);
        }

        headPageNo = get(KEY_HEAD_PAGE_NO, Long.class);
        if (headPageNo == null) {
            headPageNo = 0L;
            put(KEY_HEAD_PAGE_NO, headPageNo);
        }

        lastDelPageNo = get(KEY_LAST_DEL_PAGE_NO, Long.class);
        if (lastDelPageNo == null) {
            lastDelPageNo = -1L;
            put(KEY_HEAD_PAGE_NO, lastDelPageNo);
        }

    }

    /**
     * Load read、write、delete cursor from leveldb
     *
     * @throws PersistenceException
     */
    protected void loadCursor() throws PersistenceException {
        readCursor = get(KEY_READ_CURSOR, LevelDBCursor.class);
        if (readCursor == null) {
            readCursor = createDefaultCursor(KEY_READ_CURSOR, headPageNo);
        }

        writeCursor = get(KEY_WRITE_CURSOR, LevelDBCursor.class);
        if (writeCursor == null) {
            writeCursor = createDefaultCursor(KEY_WRITE_CURSOR, currentPageNo);
        }

        deleteCursor = get(KEY_DELETE_CURSOR, LevelDBCursor.class);
        if (deleteCursor == null) {
            deleteCursor = createDefaultCursor(KEY_DELETE_CURSOR, headPageNo);
        }
    }

    private LevelDBCursor createDefaultCursor(String cursorKey, long pageNo) throws PersistenceException {
        LevelDBCursor levelDBCursor = new LevelDBCursor();
        levelDBCursor.setPageNo(pageNo);
        levelDBCursor.setIndex(0);

        put(cursorKey, levelDBCursor);
        return levelDBCursor;
    }

    protected Long calculateTotalCount() {
        final long gapPageNum = writeCursor.getPageNo() - readCursor.getPageNo();
        final long leftPageSize = writeCursor.getIndex() - readCursor.getIndex();
        return gapPageNum * pageSize + leftPageSize;
    }

    public String save(Message message) throws PersistenceException {
        //锁被我外移至LevelDBQueue
//        final ReentrantLock writeLock = this.writeLock;
//		writeLock.lock();
        WriteBatch wb = adapter.getDb().createWriteBatch();
        MessageWrapper messageWrapper = new MessageWrapper(adapter.nextId(), message);
        try {
            if (adapter.getDb() == null) {
                // 表示已关闭
                return null;
            }
            if (writeCursor == null) {
                writeCursor = get(KEY_WRITE_CURSOR, LevelDBCursor.class);
                if (null == writeCursor) {
                    writeCursor = new LevelDBCursor();
                    writeCursor.setPageNo(currentPageNo);
                    writeCursor.setIndex(1);
                    put(KEY_WRITE_CURSOR, writeCursor);
                }
            }

            if (isCursorToTheEndPage(writeCursor)) {
                //如果当前页写不下了，就创建新页
                newPage(messageWrapper.getMsgId(), wb);
            } else {
                //如果当前页写的下，把消息id存进当前页索引
                currentPage.getIndexes().add(messageWrapper.getMsgId());
                writeCursor.setIndex(currentPage.getIndexes().size());
                put(KEY_WRITE_CURSOR, writeCursor, wb);
                savePage(currentPage, wb);
            }
            put(messageWrapper.getMsgId(), messageWrapper, wb);
            DB db = adapter.getDb();
            db.write(wb);
            return messageWrapper.getMsgId();
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
    public Message pop() throws PersistenceException {
//        final ReentrantLock readLock = this.readLock;
//		readLock.lock();
        try {
            List<Message> list = pop(1);
            if (list == null || list.size() == 0) {
                return null;
            }
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
    public List<Message> pop(int bulkSize) throws PersistenceException {
//        final ReentrantLock readLock = this.readLock;
//		readLock.lock();
        try {
            long index = readCursor.getIndex();
            List<Message> list = _peek(bulkSize);
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

    /**
     * Calculate next read page
     *
     * @param popSize
     * @param nextIndex
     * @return
     * @throws PersistenceException
     */
    long calculateNextReadPage(int popSize, long nextIndex) throws PersistenceException {
        /*if(readCursor.getPageNo() < currentPageNo){
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
    public Message peek() throws PersistenceException {
        //锁外移至LevelDBQueue
//        final ReentrantLock readLock = this.readLock;
//        readLock.lock();
        try {
            List<Message> list = _peek(1);
            if (list == null || list.size() == 0) {
                return null;
            }
            return list.get(0);
        } finally {
//            readLock.unlock();
        }
    }

    /**
     * Return the head data, but not move read cursor
     *
     * @param bulkSize
     * @return
     * @throws PersistenceException
     */
    protected List<Message> _peek(int bulkSize) throws PersistenceException {
        if (writeCursor.getPageNo() == readCursor.getPageNo() && writeCursor.getIndex() == readCursor.getIndex()) {
            return null;
        }

        long startIndex = readCursor.getIndex();
        List<String> msgIds = new ArrayList<>();
        loadMsgIds(msgIds, (int) startIndex, readCursor.getPageNo(), bulkSize);
        List<Message> list = new ArrayList<>(msgIds.size());
        for (String msgId : msgIds) {
            MessageWrapper wrapper = get(msgId, MessageWrapper.class);
            list.add(wrapper == null ? null : wrapper.getMessage());
        }
        return list;
    }

    public List<Message> peek(int bulkSize) throws PersistenceException {
        //锁外移至LevelDBQueue
//        final ReentrantLock readLock = this.readLock;
//        readLock.lock();
        try {
            return _peek(bulkSize);
        } finally {
//            readLock.unlock();
        }
    }

    /**
     * Load message indexes
     *
     * @param msgIds
     * @param startIndex
     * @param pageNo
     * @param total
     * @throws PersistenceException
     */
    void loadMsgIds(List<String> msgIds, int startIndex, long pageNo, int total) throws PersistenceException {
        LevelDBPage page = getPage(pageNo);
        if (page == null || page.getIndexes() == null || page.getIndexes().size() == 0) {
            return;
        }

        for (int i = startIndex; i < page.getIndexes().size(); i++) {
            msgIds.add(page.getIndexes().get(i));
            if (msgIds.size() == total) {
                return;
            }
        }

        //don't have more unread message
        if (page.getNo() == currentPage.getNo()) {
            return;
        }
        long nextPageNo = page.getNo() == Long.MAX_VALUE ? 0 : (page.getNo() + 1);
        loadMsgIds(msgIds, 0, nextPageNo, total);
    }

    boolean isCursorToTheEndPage(LevelDBCursor cursor) {
        long index = cursor.getIndex();
        return index >= pageSize;
    }

    void newPage(String msgId, WriteBatch wb) throws PersistenceException {
        LevelDBPage page = new LevelDBPage();
        if (currentPage.getNo() == Long.MAX_VALUE) {
            currentPage.setNo(0);
        } else {
            page.setNo(currentPage.getNo() + 1);
        }
        page.setIndexes(new ArrayList<>());
        if (msgId != null && !"".equals(msgId)) {
            page.getIndexes().add(msgId);
        }
        saveCurrentPageNo(page.getNo(), wb);

        currentPage = page;
        savePage(currentPage, wb);

        writeCursor.setPageNo(page.getNo());
        writeCursor.setIndex(1);
        put(KEY_WRITE_CURSOR, writeCursor, wb);
    }

    void saveCurrentPageNo(Long pageNo, WriteBatch wb) throws PersistenceException {
        currentPageNo = pageNo;
        put(KEY_CURRENT_PAGE_NO, currentPageNo, wb);
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
                LevelDBCursor readCursorSnapshot = (LevelDBCursor) readCursor.clone();
                if (deleteCursor.getPageNo() == headPageNo &&
                        readCursorSnapshot.getPageNo() == deleteCursor.getPageNo() && deleteCursor.getIndex() == readCursorSnapshot.getIndex()) {
                    return;
                }

                //===物理删除这些无效消息===
                long pageNo = headPageNo;
                for (long i = pageNo; i < readCursorSnapshot.getPageNo(); i++) {
                    LevelDBPage page = getPage(i);
                    if (page == null) {
                        logger.error(String.format("page is NULL when physical delele invaild msgs, queueName:s% , pageNo:%s", queueName, headPageNo));
                        continue;
                    }
                    List<String> indexes = page.getIndexes();
                    if (indexes == null || indexes.size() == 0) {
                        continue;
                    }
                    batchDelete(i, indexes.size(), indexes.toArray(new String[indexes.size()]));
                }
                if (readCursorSnapshot.getIndex() > 0) {
                    LevelDBPage page = getPage(readCursorSnapshot.getPageNo());
                    List<String> deleteIndexes = page.getIndexes().subList(0, (int) readCursorSnapshot.getIndex());
                    if (deleteIndexes.size() > 0) {
                        batchDelete(readCursorSnapshot.getPageNo(), deleteIndexes.size(), deleteIndexes.toArray(new String[deleteIndexes.size()]));
                    }
                }

                //同时删除删除无效的消息索引结构数据(无效页)
                long invaildPageNo = lastDelPageNo;
                if (invaildPageNo + 1 < headPageNo) {
                    List<String> invaildPageKey = new ArrayList<>();
                    for (long i = invaildPageNo + 1; i < headPageNo; i++) {
                        invaildPageKey.add(buildKey(KEY_PAGE, String.valueOf(i)));
                    }
                    batchDelete(invaildPageKey);
                    lastDelPageNo = headPageNo - 1;
                    put(KEY_LAST_DEL_PAGE_NO, lastDelPageNo);
                }
            } finally {
                deleteLock.unlock();
            }

        }
    }

    private void batchDelete(List<String> keys) throws PersistenceException {
        WriteBatch writeBatch = adapter.getDb().createWriteBatch();
        try {
            for (String key : keys) {
                writeBatch.delete(wrapperKey(key).getBytes());
            }
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

    protected void batchDelete(long pageNo, int index, String... keys) throws PersistenceException {
        if (keys == null || keys.length == 0) {
            return;
        }
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

    /**
     * 清空消息队列 todo
     *
     * @throws Exception
     */
    public synchronized void clear() throws Exception {
        //需要清空该队列在LevelDB中的消息数据，和消息索引结构数据
        //读写锁都被我外移至LevelDBQueue了
//        final Lock readLock = this.readLock;
//        final Lock writeLock = this.writeLock;
//        readLock.lock();
//        writeLock.lock();
//        try {
//            load();
//        } finally {
//            readLock.unlock();
//            writeLock.unlock();
//        }
    }
}

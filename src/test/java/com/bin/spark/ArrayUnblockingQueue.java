package com.bin.spark;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * $ArrayUnblockingQueue
 *
 * @author hezhuo.bai
 * @since 2019年2月20日 下午10:01:17
 */
@SuppressWarnings("restriction")
public class ArrayUnblockingQueue<T> {

    protected static final long INITIAL_VALUE = -1L;

    protected final long indexMask;

    protected final String threadName;

    protected final int bufferSize;

    protected final Processor<T> processor;

    protected final Waitor waitor;

    protected final ProducerSequence put = new ProducerSequence(INITIAL_VALUE);

    protected final ConsumerSequence get = new ConsumerSequence(INITIAL_VALUE);

    protected volatile Elements elements;

    protected ProcessorThread thread;

    protected volatile static long cursor = INITIAL_VALUE;

    public ArrayUnblockingQueue(String threadName, int bufferSize, Processor<T> processor, Waitor waitor) {
        this.threadName = threadName;
        this.bufferSize = bufferSize;
        this.elements = new Elements();
        this.processor = processor;
        this.waitor = waitor;
        this.indexMask = bufferSize - 1;
    }

    public void start() {
        if (Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }
        if (processor == null) {
            throw new IllegalArgumentException("processor must be not null");
        }
        if (waitor == null) {
            throw new IllegalArgumentException("waitor must be not null");
        }
        ProcessorThread thread = new ProcessorThread();
        thread.start();
        this.thread = thread;
    }

    public void put(T data) {
        if (data == null) {
            return;
        }

        Elements elements = this.elements;
        if (elements == null) {
            processor.onThrowable(data, new IllegalStateException("Has shutdown"));
            return;
        }

        long current;
        long next;
        long cachedGatingNext;

        do {
            current = put.get();
            next = current + 1;

            if ((next - bufferSize) > (cachedGatingNext = get.get()) || cachedGatingNext > next) {
                LockSupport.parkNanos(1);
            } else if (put.getValue().compareAndSet(current, next)) {
                break;
            }

        } while (true);

        elements.elementAt(next).value = data;
        waitor.signal();
    }

    public boolean tryPut(T data) {
        if (data == null) {
            return true;
        }

        Elements elements = this.elements;
        if (elements == null) {
            return false;
        }

        long current;
        long next;
        long cachedGatingNext;

        do {
            current = put.get();
            next = current + 1;

            if (next - bufferSize > (cachedGatingNext = get.get()) || cachedGatingNext > next) {
                return false;
            }

        } while (!put.getValue().compareAndSet(current, next));

        elements.elementAt(next).value = data;
        waitor.signal();
        return true;
    }

    public void stop(){
        try {
            stop(0, TimeUnit.MICROSECONDS);
        } catch (TimeoutException e) {

        }
    }

    public void stop(long timeout, TimeUnit timeUnit) throws TimeoutException {

        long endTimeout = System.currentTimeMillis() + timeUnit.toMillis(timeout);

        while (put.get() != get.get()) {
            try {
                if (timeout == 0) {
                    Thread.sleep(1000);
                } else {
                    long waitTime = (endTimeout - System.currentTimeMillis()) / 2;
                    if (waitTime > 0) {
                        Thread.sleep(waitTime);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        thread.interrupt();

        if (timeout > 0 && System.currentTimeMillis() > endTimeout) {
            throw new TimeoutException();
        }
    }

    /**
     * $Processor
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:08:53
     */
    public interface Processor<T> {

        void process(T data);

        void onTimeout(long current);

        void onThrowable(T data, Throwable e);

    }

    /**
     * $Waitor
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:08:47
     */
    public interface Waitor {

        void signal();

        long wait(long next, ProducerSequence put, ProcessHandler handler) throws InterruptedException, TimeoutException;

    }

    /**
     * $ProcessHandler
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:08:42
     */
    public interface ProcessHandler {

        boolean isInterrupt();

    }

    /**
     * $Element
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:08:34
     */
    private static class Element<T> {

        public volatile T value;
    }

    /**
     * $BlockingWaitor
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:08:23
     */
    public static class BlockingWaitor implements Waitor {

        private ReentrantLock lock = new ReentrantLock();

        private Condition notifyCondition = lock.newCondition();

        private AtomicBoolean needSignal = new AtomicBoolean(false);

        public void signal() {
            lock.lock();
            try {
                if (needSignal.getAndSet(false)) {
                    notifyCondition.signalAll();
                }
            } finally {
                lock.unlock();
            }
        }

        public long wait(long next, ProducerSequence put, ProcessHandler handler)
                throws InterruptedException, TimeoutException {
            lock.lock();
            long available;
            try {
                if ((available = put.get()) < next) {
                    needSignal.set(true);
                    if ((available = put.get()) < next && !handler.isInterrupt()) {
                        notifyCondition.await(); // 消费者就等待
                    }
                }
            } finally {
                lock.unlock();
            }
            return available;
        }
    }

    /**
     * $TimeoutWaitor
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:07:48
     */
    public static class TimeoutWaitor implements Waitor {

        private static TimeoutException exception = new TimeoutException();

        private final long timeout;

        private ReentrantLock lock = new ReentrantLock();

        private Condition notifyCondition = lock.newCondition();

        private AtomicBoolean needSignal = new AtomicBoolean(false);

        public TimeoutWaitor(long timeout, TimeUnit timeUnit) {
            this.timeout = timeUnit.toNanos(timeout);
        }

        public void signal() {
            if (needSignal.getAndSet(false)) {
                lock.lock();
                try {
                    notifyCondition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        }

        public long wait(long next, ProducerSequence put, ProcessHandler handler)
                throws InterruptedException, TimeoutException {
            long nanos = timeout;
            long available;
            if ((available = put.get()) < next) {
                lock.lock();
                try {
                    if ((available = put.get()) < next) {
                        needSignal.set(true);
                        if ((available = put.get()) < next && !handler.isInterrupt()) {
                            nanos = notifyCondition.awaitNanos(nanos);
                            if (nanos <= 0) {
                                throw exception;
                            }
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
            return available;
        }
    }

    /**
     * $SleepingWaitor
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:07:35
     */
    public static class SleepingWaitor implements Waitor {

        private final int retries;

        public SleepingWaitor(int retries) {
            this.retries = retries;
        }

        public void signal() {
        }

        public long wait(long next, ProducerSequence put, ProcessHandler handler)
                throws InterruptedException, TimeoutException {
            long available;
            int counter = retries;
            while ((available = put.get()) < next && !handler.isInterrupt()) {
                counter = applyWaitMethod(counter);
            }
            return available;
        }

        private int applyWaitMethod(int counter) {
            if (counter > 100) {
                --counter;
            } else if (counter > 0) {
                --counter;
                Thread.yield();
            } else {
                LockSupport.parkNanos(1L);
            }
            return counter;
        }
    }

    /**
     * $YieldingWaitor
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:07:27
     */
    public static class YieldingWaitor implements Waitor {

        private static final int SPIN_TRIES = 100;

        public long wait(long next, ProducerSequence put, ProcessHandler handler)
                throws InterruptedException, TimeoutException {
            long available;
            int counter = SPIN_TRIES;

            while ((available = put.get()) < next && !handler.isInterrupt()) {
                counter = applyWaitMethod(counter);
            }

            return available;
        }

        private int applyWaitMethod(int counter) {

            if (0 == counter) {
                Thread.yield();
            } else {
                --counter;
            }

            return counter;
        }

        public void signal() {
        }

    }

    /**
     * $Elements
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:07:16
     */
    @sun.misc.Contended
    private class Elements {

        protected final Element<T>[] elements;

        @SuppressWarnings("unchecked")
        public Elements() {
            elements = new Element[bufferSize];
            for (int i = 0; i < bufferSize; i++) {
                elements[i] = new Element<T>();
            }
        }

        protected final Element<T> elementAt(long sequence) {
            return elements[(int) (sequence & indexMask)];
        }
    }

    /**
     * $ProcessorThread
     * @author hezhuo.bai
     * @since 2019年2月20日 下午10:07:08
     */
    private class ProcessorThread extends Thread implements ProcessHandler {

        public ProcessorThread() {
            super(threadName);
        }

        @Override
        public void run() {

            long next = get.get() + 1L;

            T value = null;

            while (!isInterrupt()) {
                try {

                    final long available = waitor.wait(next, put, this);

                    while (next <= available) {
                        Element<T> element = elements.elementAt(cursor = next);
                        processor.process(value = element.value);
                        next++;
                    }

                    get.set(available);

                } catch (TimeoutException e) {
                    processor.onTimeout(next - 1);
                } catch (InterruptedException e) {
                    System.err.println("process interrupt........");
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    processor.onThrowable(value, e);
                    next++;
                }
            }
        }

        public boolean isInterrupt() {
            return Thread.currentThread().isInterrupted();
        }
    }
}

/**
 * $ProducerSequence
 * @author hezhuo.bai
 * @since 2019年2月20日 下午10:09:22
 */
@SuppressWarnings("restriction")
@sun.misc.Contended
class ProducerSequence {

    private volatile AtomicLong value;

    public ProducerSequence(long initialValue) {
        this.value = new AtomicLong(initialValue);
    }

    public AtomicLong getValue() {
        return value;
    }

    public long get() {
        return value.get();
    }

    public void set(long value) {
        this.value.set(value);
    }
}

/**
 * $ConsumerSequence
 * @author hezhuo.bai
 * @since 2019年2月20日 下午10:09:22
 */
@SuppressWarnings("restriction")
@sun.misc.Contended
class ConsumerSequence {

    private volatile AtomicLong value;

    public ConsumerSequence(long initialValue) {
        this.value = new AtomicLong(initialValue);
    }

    public long get() {
        return value.get();
    }

    public void set(long value) {
        this.value.set(value);
    }

    public void getAndSet(long value) {
        this.value.getAndSet(value);
    }
}
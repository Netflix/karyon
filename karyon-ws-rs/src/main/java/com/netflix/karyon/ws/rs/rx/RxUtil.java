package com.netflix.karyon.ws.rs.rx;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.GroupedObservable;
import rx.subjects.PublishSubject;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * Lots of nice utilities to help simplify Rx stream
 * 
 * @author elandau
 *
 */
public class RxUtil {
    private static final Logger LOG = LoggerFactory.getLogger(RxUtil.class);
    
    /**
     * Increment a stateful counter outside the stream.
     * 
     * {code
     * <pre>
     * observable
     *    .doOnNext(RxUtil.increment(mycounter))
     * </pre>
     * }
     *  
     * @param metric
     * @return
     */
    public static <T> Action1<T> increment(final AtomicLong metric) {
        return new Action1<T>() {
            @Override
            public void call(T t1) {
                metric.incrementAndGet();
            }
        };
    }
    
    /**
     * Decrement a stateful counter outside the stream.
     * 
     * {code
     * <pre>
     * observable
     *    .doOnNext(RxUtil.decrement(mycounter))
     * </pre>
     * }
     *  
     * @param metric
     * @return
     */
    public static <T> Action1<T> decrement(final AtomicLong metric) {
        return new Action1<T>() {
            @Override
            public void call(T t1) {
                metric.decrementAndGet();
            }
        };
    }
    
    /**
     * Trace each item emitted on the stream with a given label.  
     * Will log the file and line where the trace occurs.
     * 
     * {code
     * <pre>
     * observable
     *    .doOnNext(RxUtil.trace("next: "))
     * </pre>
     * }
     *  
     * @param label
     */
    public static <T> Action1<T> trace(String label) {
        final String caption = getSourceLabel(label);
        final AtomicLong counter = new AtomicLong();
        return new Action1<T>() {
            @Override
            public void call(T t1) {
                LOG.trace(caption + counter.incrementAndGet() + " " + t1);
            }
        };            
    }

    /**
     * Log info line for each item emitted on the stream with a given label.  
     * Will log the file and line where the trace occurs.
     * 
     * {code
     * <pre>
     * observable
     *    .doOnNext(RxUtil.info("next: "))
     * </pre>
     * }
     *  
     * @param label
     */
    public static <T> Action1<T> info(String label) {
        final String caption = getSourceLabel(label);
        final AtomicLong counter = new AtomicLong();
        return new Action1<T>() {
            @Override
            public void call(T t1) {
                LOG.info(caption + "(" + counter.incrementAndGet() + ") " + t1);
            }
        };            
    }
    
    public static <T> Action0 info0(String label) {
        final String caption = getSourceLabel(label);
        final AtomicLong counter = new AtomicLong();
        return new Action0() {
            @Override
            public void call() {
                LOG.info(caption + "(" + counter.incrementAndGet() + ") ");
            }
        };            
    }

    /**
     * Decrement a countdown latch for each item
     * 
     * {code
     * <pre>
     * observable
     *    .doOnNext(RxUtil.decrement(latch))
     * </pre>
     * }
     */
    public static <T> Action1<T> countdown(final CountDownLatch latch) {
        return new Action1<T>() {
            @Override
            public void call(T t1) {
                latch.countDown();
            }
        };            
    }

    public static Action0 countdown0(final CountDownLatch latch) {
        return new Action0() {
            @Override
            public void call() {
                latch.countDown();
            }
        };            
    }

    /**
     * Decrement a countdown latch for each error
     * 
     * {code
     * <pre>
     * observable
     *    .doOnError(RxUtil.decrement(latch))
     * </pre>
     * }
     */
    public static Action1<Throwable> countdownError(final CountDownLatch latch) {
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable e) {
                latch.countDown();
            }
        };            
    }

    /**
     * Log the request rate every second.
     * 
     * {code
     * <pre>
     * observable
     *    .lift(RxUtil.rate("items per second"))
     * </pre>
     * }
     *  
     * @param label
     */
    public static <T> Operator<T, T> rate(final String label) {
        return rate(label, 1, TimeUnit.SECONDS);
    }
    
    /**
     * Log the request rate at the given interval.
     * 
     * {code
     * <pre>
     * observable
     *    .lift(RxUtil.rate("items per 10 seconds", 10, TimeUnit.SECONDS))
     * </pre>
     * }
     *  
     * @param label
     * @param interval 
     * @param units
     */
    public static <T> Operator<T, T> rate(final String label, final long interval, final TimeUnit units) {
        return new Operator<T, T>() {
            @Override
            public Subscriber<? super T> call(final Subscriber<? super T> child) {
                final AtomicLong counter = new AtomicLong();
                final String caption = getSourceLabel(label);
                
                final Subscription timer = Observable.interval(interval, units).subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long t1) {
                        long count = counter.get();
                        LOG.info(caption + counter + " / (" + interval + " " + units.name() + ")");
                        counter.addAndGet(-count);
                    }
                });

                child.add(timer);
                return new Subscriber<T>() {
                    @Override
                    public void onCompleted() {
                        child.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onNext(T t) {
                        counter.incrementAndGet();
                        child.onNext(t);
                    }
                };
            }
        };            
    }

    /**
     * Log error line when an error occurs.
     * Will log the file and line where the trace occurs.
     * 
     * {code
     * <pre>
     * observable
     *    .doOnError(RxUtil.error("Stream broke"))
     * </pre>
     * }
     *  
     * @param label
     */
    public static Action1<Throwable> error(String label) {
        final String caption = getSourceLabel(label);
        final AtomicLong counter = new AtomicLong();
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable t1) {
                LOG.error(caption + "(" + counter.incrementAndGet() + ") " + t1);
                t1.printStackTrace();
            }
        };            
    }

    /**
     * Log a warning line when an error occurs.
     * Will log the file and line where the trace occurs.
     * 
     * {code
     * <pre>
     * observable
     *    .doOnError(RxUtil.warn("Stream broke"))
     * </pre>
     * }
     *  
     * @param label
     */
    public static Action1<Throwable> warn(String label) {
        final String caption = getSourceLabel(label);
        final AtomicLong counter = new AtomicLong();
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable t1) {
                LOG.warn(caption + "(" + counter.incrementAndGet() + ") " + t1);
            }
        };            
    }

    @Deprecated
    public static <T> Func1<List<T>, Boolean> listNotEmpty() {
        return new Func1<List<T>, Boolean>() {
            @Override
            public Boolean call(List<T> t1) {
                return !t1.isEmpty();
            }
        };
    }

    /**
     * Filter out any collection that is empty.
     * 
     * {code
     * <pre>
     * observable
     *    .filter(RxUtil.collectionNotEmpty())
     * </pre>
     * }
     */
    public static <T> Func1<Collection<T>, Boolean> collectionNotEmpty() {
        return new Func1<Collection<T>, Boolean>() {
            @Override
            public Boolean call(Collection<T> t1) {
                return !t1.isEmpty();
            }
        };
    }

    /**
     * Operator that acts as a pass through.  Use this when you want the operator
     * to be interchangable with the default implementation being a single passthrough.
     * 
     * {code
     * <pre>
     * Operator<T,T> customOperator = RxUtil.passthrough();
     * observable
     *    .lift(customOperator)
     * </pre>
     * }
     */
    public static <T> Operator<T, T> passthrough() {
        return new Operator<T, T>() {
            @Override
            public Subscriber<? super T> call(final Subscriber<? super T> o) {
                return o;
            }
        };
    }
    
    /**
     * Cache all items and emit a single LinkedHashSet with all data when onComplete is called
     * @return
     */
    public static <T> Operator<Set<T>, T> toLinkedHashSet() {
        return new Operator<Set<T>, T>() {
            @Override
            public Subscriber<? super T> call(final Subscriber<? super Set<T>> o) {
                final Set<T> set = Sets.newLinkedHashSet();
                return new Subscriber<T>() {
                    @Override
                    public void onCompleted() {
                        o.onNext(set);
                        o.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        o.onError(e);
                    }

                    @Override
                    public void onNext(T t) {
                        set.add(t);
                    }
                };
            }
        };
    }
    
    private static String getSourceLabel(String label) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            if (element.getClassName().contains("RxUtil") ||
                element.getClassName().startsWith("rx.") || 
                element.getClassName().startsWith("java.lang")) {
                continue;
            }
            return "(" + element.getFileName() + ":" + element.getLineNumber() + ") " + label + " ";
        }
        return label + " ";
    }

    /**
     * Filter that returns true whenever an external state is true
     * {code
     * <pre>
     * final AtomicBoolean condition = new AtomicBoolean();
     * 
     * observable
     *    .filter(RxUtil.isTrue(condition))
     * </pre>
     * }
     * @param condition
     */
    public static Func1<? super Long, Boolean> isTrue(final AtomicBoolean condition) {
        return new Func1<Long, Boolean>() {
            @Override
            public Boolean call(Long t1) {
                return condition.get();
            }
        };
    }

    /**
     * Filter that returns true whenever an external state is false
     * {code
     * <pre>
     * final AtomicBoolean condition = new AtomicBoolean();
     * 
     * observable
     *    .filter(RxUtil.isTrue(condition))
     * </pre>
     * }
     * @param condition
     */
    public static Func1<? super Long, Boolean> isFalse(final AtomicBoolean condition) {
        return new Func1<Long, Boolean>() {
            @Override
            public Boolean call(Long t1) {
                return !condition.get();
            }
        };
    }

    /**
     * Condition that evaluates to true as long as the emitted value is not null
     * @return
     */
    public static <T> Func1<T, Boolean> isNotNull() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T t1) {
                return t1 != null;
            }
        };
    }

    /**
     * Filter that returns true whenever a CAS operation on an external static 
     * AtomicBoolean succeeds 
     * {code
     * <pre>
     * final AtomicBoolean condition = new AtomicBoolean();
     * 
     * observable
     *    .filter(RxUtil.isTrue(condition))
     * </pre>
     * }
     * @param condition
     */
    public static Func1<? super Long, Boolean> compareAndSet(final AtomicBoolean condition, final boolean expect, final boolean value) {
        return new Func1<Long, Boolean>() {
            @Override
            public Boolean call(Long t1) {
                return condition.compareAndSet(expect, value);
            }
        };
    }

    /**
     * Simple operation that sets an external condition for each emitted item
     * {code
     * <pre>
     * final AtomicBoolean condition = new AtomicBoolean();
     * 
     * observable
     *    .doOnNext(RxUtil.set(condition, true))
     * </pre>
     * }
     * @param condition
     * @param value
     * @return
     */
    public static <T> Action1<T> set(final AtomicBoolean condition, final boolean value) {
        return new Action1<T>() {
            @Override
            public void call(T t1) {
                condition.set(value);
            }
        };
    }
    
    /**
     * Filter that always returns a constant value.  Use this to create a default filter
     * when the filter implementation is plugable. 
     * 
     * @param constant
     * @return
     */
    public static <T> Func1<T, Boolean> constantFilter(final boolean constant) {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T t1) {
                return constant;
            }
        };
    }

    /**
     * Observable factory to be used with {@link Observable.defer()} which will round robin
     * through a list of {@link Observable}'s so that each subscribe() returns the next
     * {@link Observable} in the list.  
     * 
     * @param sources
     * @return
     */
    public static <T> Func0<Observable<T>> roundRobinObservableFactory(final Observable<T> ... sources) {
        return new Func0<Observable<T>>() {
            final AtomicInteger count = new AtomicInteger();
            @Override
            public Observable<T> call() {
                int index = count.getAndIncrement() % sources.length;
                return sources[index];
            }
        };
    }
    
    /**
     * Group by keys in a map and emit values on each group as a new complete map is emitted.
     * Unsubscribe from the map once an item is no longer in the map
     *
     * @return
     */
    public static <K, V> Operator<GroupedObservable<K, V>, Map<K, V>> groupByUntilRemovedFromMap() {
        return new Operator<GroupedObservable<K, V>, Map<K, V>>() {
            @Override
            public Subscriber<? super Map<K, V>> call(final Subscriber<? super GroupedObservable<K, V>> child) {
                final PublishSubject<Map<K, V>> subject = PublishSubject.create();
                final Map<K, PublishSubject<Void>> existing = Maps.newConcurrentMap();
                
                subject
                    .doOnNext(new Action1<Map<K, V>>() {
                        @Override
                        public void call(Map<K, V> current) {
                            SetView<K> diff = Sets.difference(existing.keySet(), current.keySet());
                            for (K key : diff) {
                                PublishSubject<Void> subject = existing.remove(key);
                                subject.onCompleted();
                            }
                        }
                    })
                    .flatMap(new Func1<Map<K,V>, Observable<Map.Entry<K, V>>>() {
                        @Override
                        public Observable<Entry<K, V>> call(Map<K, V> t1) {
                            return Observable.from(t1.entrySet());
                        }
                    })
                    .groupByUntil(
                        new Func1<Entry<K, V>, K>() {
                            @Override
                            public K call(Entry<K, V> t1) {
                                return t1.getKey();
                            }
                        }, 
                        new Func1<Entry<K, V>, V>() {
                            @Override
                            public V call(Entry<K, V> t1) {
                                return t1.getValue();
                            }
                            
                        },
                        new Func1<GroupedObservable<K, V>, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(GroupedObservable<K, V> t1) {
                                PublishSubject<Void> subject = PublishSubject.create();
                                existing.put(t1.getKey(), subject);
                                return subject;
                            }
                        }
                    )
                    .subscribe(child);
                
                return new Subscriber<Map<K, V>>() {
                    @Override
                    public void onCompleted() {
                        subject.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subject.onError(e);
                    }

                    @Override
                    public void onNext(Map<K, V> t) {
                        subject.onNext(t);
                    }
                };
            }
        };
    }
}

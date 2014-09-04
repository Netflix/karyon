package com.netflix.karyon.ws.rs.rx;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.ws.rs.Path;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * Utility class to Rx'ify reflection
 * 
 * @author elandau
 */
public class RxReflection {
    /**
     * 
     * @return
     */
    public static Func1<Class<?>, Observable<Class<?>>> getAllSubclasses() {
        return new Func1<Class<?>, Observable<Class<?>>>() {
            @Override
            public Observable<Class<?>> call(final Class<?> type) {
                return Observable.create(new OnSubscribe<Class<?>>() {
                    @Override
                    public void call(Subscriber<? super Class<?>> child) {
                        Class<?> superclass = type;
                        while (superclass != null) {
                            child.onNext(superclass);
                            for (Class<?> iface : superclass.getInterfaces()) {
                                child.onNext(iface);
                            }
                            superclass = superclass.getSuperclass();
                        }
                        child.onCompleted();
                    }
                });
            }
        };
    }
    
    /**
     * 
     * @param annot
     * @return
     */
    public static Func1<Class<?>, Boolean> hasClassAnnotation(final Class<?> annot) {
        return new Func1<Class<?>, Boolean>() {
            @Override
            public Boolean call(Class<?> t1) {
                return null != t1.getAnnotation(Path.class);
            }
        };
    }
    
    /**
     * 
     * @return
     */
    public static Func1<String, Observable<ClassInfo>> scanPackageClasses() {
        return new Func1<String, Observable<ClassInfo>>() {
            @Override
            public Observable<ClassInfo> call(String packageName) {
                try {
                    return Observable.from(ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(packageName));
                } catch (IOException e) {
                    return Observable.error(e);
                }
            }
        };
        
    }

    /**
     * 
     * @return
     */
    public static Func1<ClassInfo, Observable<Class<?>>> nameToClass() {
        return new Func1<ClassInfo, Observable<Class<?>>>() {
            @Override
            public Observable<Class<?>> call(ClassInfo info) {
                try {
                    return Observable.<Class<?>>just(Class.forName(info.getName()));
                } catch (ClassNotFoundException e) {
                    return Observable.empty();
                }
            }
        };
    }

    /**
     * 
     * @return
     */
    public static Func1<? super Class<?>, Boolean> isConcrete() {
        return new Func1<Class<?>, Boolean>() {
            @Override
            public Boolean call(Class<?> t1) {
                return !Modifier.isAbstract( t1.getModifiers() );
            }
        };
    }

    /**
     * 
     * @param type
     * @return
     */
    public static Func1<Class<?>, Observable<Annotation>> getClassAnnotation(final Class<? extends Annotation> type) {
        return new Func1<Class<?>, Observable<Annotation>>() {
            @Override
            public Observable<Annotation> call(Class<?> cls) {
                Annotation annot = cls.getAnnotation(type);
                if (annot != null)
                    return Observable.just(annot);
                else 
                    return Observable.empty();
            }
        };
    }

    /**
     * 
     * @return
     */
    public static Func1<Class<?>, Observable<Method>> getDeclaredMethods() {
        return new Func1<Class<?>, Observable<Method>>() {
            @Override
            public Observable<Method> call(Class<?> t1) {
                return Observable.from(t1.getDeclaredMethods());
            }
        };
    }
    
}

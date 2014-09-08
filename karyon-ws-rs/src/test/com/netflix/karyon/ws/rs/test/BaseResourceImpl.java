package com.netflix.karyon.ws.rs.test;

import java.util.List;

import rx.Observable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public abstract class BaseResourceImpl implements BaseResource {
    @Override
    public Observable<String> getString(String name) {
        return Observable.just(Joiner.on(":").join("str", name));
    }
    
    @Override
    public Observable<String> getInteger(Integer name) {
        return Observable.just(Joiner.on(":").join("int", name));
    }
    
    @Override
    public Observable<String> getDouble(Double name) {
        return Observable.just(Joiner.on(":").join("double", name));
    }

    @Override
    public Observable<String> getLong(Long name) {
        return Observable.just(Joiner.on(":").join("long", name));
    }

    @Override
    public Observable<String> getHeader(String foo) {
        return Observable.just(Joiner.on(":").join("header", foo));
    }

    @Override
    public Observable<String> getTwoPathParam(String first, String last) {
        return Observable.just(Joiner.on(":").join("two", first, last));
    }
    
    @Override
    public Observable<String> getDefaultPathParam(String notDefined) {
        return Observable.just(Joiner.on(":").join("default", notDefined));
    }
    
    @Override
    public List<Integer> getListOfIntegers() {
        return Lists.newArrayList(1, 2, 3);
    }
    
    @Override
    public Integer getInteger() {
        return 1;
    }
    
    @Override
    public Observable<Integer> getObservableListOfIntegers() {
        return Observable.from(1, 2, 3);
    }
    
    @Override
    public Observable<Integer> getObservableInteger() {
        return Observable.from(1);
    }
    
}

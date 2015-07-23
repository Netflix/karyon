package com.netflix.karyon.log4j.admin;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

@Singleton
public class Log4JResource {

    public static class Result {
        Map<String, String> loggers = new TreeMap<>();
        Map<String, String> appenders = new TreeMap<>();
        
        public Map<String, String> getLoggers() {
            return loggers;
        }
        
        public Map<String, String> getAppenders() {
            return appenders;
        }
    }
    
    // log4j/
    public Result get() {
        Result result = new Result();
        LoggerContext ctx = (LoggerContext)LogManager.getContext();
        for (Logger logger : ctx.getLoggers()) {
            result.loggers.put(logger.getName(), logger.getLevel().toString());
        }
        
        for (Entry<String, Appender> appender : ctx.getConfiguration().getAppenders().entrySet()) {
            result.appenders.put(appender.getKey(), appender.getValue().getLayout().toString());
        }
        return result;
    }
}

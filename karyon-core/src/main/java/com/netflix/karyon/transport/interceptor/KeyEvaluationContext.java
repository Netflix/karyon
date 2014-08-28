package com.netflix.karyon.transport.interceptor;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * A context to store results of costly operations during evaluation of filter keys, eg: request URI parsing. <p></p>
 * <b>This context is not thread-safe.</b>
 */
public class KeyEvaluationContext {

    protected final ChannelHandlerContext channelHandlerContext;

    public KeyEvaluationContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    enum KeyEvaluationResult { Apply, Skip, NotExecuted }

    private final Map<InterceptorKey<?, ?>, Boolean> keyEvaluationCache = new HashMap<InterceptorKey<?, ?>, Boolean>();

    protected void updateKeyEvaluationResult(InterceptorKey<?, ?> key, boolean result) {
        keyEvaluationCache.put(key, result);
    }

    protected KeyEvaluationResult getEvaluationResult(InterceptorKey<?, ?> key) {
        Boolean result = keyEvaluationCache.get(key);
        return null == result ? KeyEvaluationResult.NotExecuted
                              : result ? KeyEvaluationResult.Apply : KeyEvaluationResult.Skip;
    }
}

package netflix.karyon.transport.interceptor;

import io.reactivex.netty.channel.Handler;
import netflix.karyon.transport.RequestRouter;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.SerialSubscription;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to execute a chain of interceptors defined by {@link InterceptorSupport}
 *
 * @author Nitesh Kant
 */
public class InterceptorExecutor<I, O, C extends KeyEvaluationContext> {

    private final List<InterceptorHolder<I, C, InboundInterceptor<I, O>>> allIn;
    private final List<InterceptorHolder<I, C, OutboundInterceptor<O>>> allOut;
    private final Handler<I, O> router;

    public InterceptorExecutor(AbstractInterceptorSupport<I, O, C, ?, ?> support, Handler<I, O> router) {
        this.router = router;
        allIn = support.getInboundInterceptors();
        allOut = support.getOutboundInterceptors();
    }

    /**
     * @deprecated Use {@link #InterceptorExecutor(AbstractInterceptorSupport, Handler)} instead.
     */
    @Deprecated
    public InterceptorExecutor(AbstractInterceptorSupport<I, O, C, ?, ?> support, final RequestRouter<I, O> router) {
        this.router = new Handler<I, O>() {
            @Override
            public Observable<Void> handle(I input, O output) {
                return router.route(input, output);
            }
        };
        allIn = support.getInboundInterceptors();
        allOut = support.getOutboundInterceptors();
    }

    /**
     * Executes the interceptor chain for the passed request and response.
     *
     * @param request Request to be executed.
     * @param response Response to be populated.
     * @param keyEvaluationContext The context for {@link InterceptorKey} evaluation.
     *
     * @return The final result of execution after executing all the inbound and outbound interceptors and the router.
     */
    public Observable<Void> execute(final I request, final O response, C keyEvaluationContext) {
        final ExecutionContext context = new ExecutionContext(request, keyEvaluationContext);
        InboundInterceptor<I, O> nextIn = context.nextIn(request);
        Observable<Void> startingPoint;

        if (null != nextIn) {
            startingPoint = nextIn.in(request, response);
        } else if (context.invokeRouter()){
            startingPoint = router.handle(request, response);
        } else {
            return Observable.error(new IllegalStateException("No router defined.")); // No router defined.
        }

        return startingPoint.lift(new Observable.Operator<Void, Void>() {
            @Override
            public Subscriber<? super Void> call(Subscriber<? super Void> child) {
                SerialSubscription subscription = new SerialSubscription();
                ChainSubscriber chainSubscriber = new ChainSubscriber(subscription, context, request, response, child);
                subscription.set(chainSubscriber);
                child.add(subscription);
                return chainSubscriber;
            }
        });
    }

    private enum NextExecutionState { NotStarted, NextInHolder, NextInInterceptor, Router, NextOutInterceptor, End}

    private class ExecutionContext {

        private final C keyEvaluationContext;
        private NextExecutionState nextExecutionState = NextExecutionState.NotStarted;
        /**
         * This list is eagerly created by evaluating keys of all out interceptors as we do not want to hold the
         * request (for key evaluation) till the outbound interceptor execution starts.
         */
        private List<OutboundInterceptor<O>> applicableOutInterceptors;

        private int currentHolderIndex;
        private int currentInterceptorIndex;

        public ExecutionContext(I request, C keyEvaluationContext) {
            this.keyEvaluationContext = keyEvaluationContext;
            applicableOutInterceptors = new ArrayList<OutboundInterceptor<O>>(); // Execution is not multi-threaded.

            for (InterceptorHolder<I, C, OutboundInterceptor<O>> holder : allOut) {
                switch (keyEvaluationContext.getEvaluationResult(holder.getKey())) { // Result is cached.
                    case Apply:
                        applicableOutInterceptors.addAll(holder.getInterceptors());
                        break;
                    case Skip:
                        break;
                    case NotExecuted:
                        boolean apply = holder.getKey().apply(request, keyEvaluationContext);
                        keyEvaluationContext.updateKeyEvaluationResult(holder.getKey(), apply);
                        if (apply) {
                            applicableOutInterceptors.addAll(holder.getInterceptors());
                        }
                        break;
                }
            }
        }

        public InboundInterceptor<I, O> nextIn(I request) {
            switch (nextExecutionState) {
                case NotStarted:
                    nextExecutionState = NextExecutionState.NextInInterceptor; // Index is 0, so we can skip the NextInHolder state.
                    return nextIn(request);
                case NextInHolder:
                    ++currentHolderIndex;
                    currentInterceptorIndex = 0;
                    nextExecutionState = NextExecutionState.NextInInterceptor;
                    return nextIn(request);
                case NextInInterceptor:
                    if (currentHolderIndex >= allIn.size()) {
                        nextExecutionState = NextExecutionState.Router;
                        return null;
                    } else {
                        InterceptorHolder<I, C, InboundInterceptor<I, O>> holder =
                                allIn.get( currentHolderIndex);
                        switch (keyEvaluationContext.getEvaluationResult(holder.getKey())) { // Result is cached.
                            case Apply:
                                return returnNextInterceptor(request, holder);
                            case Skip:
                                nextExecutionState = NextExecutionState.NextInHolder;
                                return nextIn(request);
                            case NotExecuted:
                                boolean apply = holder.getKey().apply(request, keyEvaluationContext);
                                keyEvaluationContext.updateKeyEvaluationResult(holder.getKey(), apply);
                                return nextIn(request);
                        }
                    }
            }

            return null;
        }

        public OutboundInterceptor<O> nextOut() {
            switch (nextExecutionState) {
                case NextOutInterceptor:
                    if (currentInterceptorIndex >= applicableOutInterceptors.size()) {
                        nextExecutionState = NextExecutionState.End;
                        return null;
                    } else {
                        return applicableOutInterceptors.get(currentInterceptorIndex++);
                    }
            }

            return null;
        }

        private InboundInterceptor<I, O> returnNextInterceptor(I request,
                                                               InterceptorHolder<I, C, InboundInterceptor<I, O>> holder) {
            List<InboundInterceptor<I, O>> interceptors = holder.getInterceptors();
            if (currentInterceptorIndex >= interceptors.size()) {
                nextExecutionState = NextExecutionState.NextInHolder;
                return nextIn(request);
            }
            return interceptors.get(currentInterceptorIndex++);
        }

        public boolean invokeRouter() {
            if (NextExecutionState.Router == nextExecutionState) {
                currentHolderIndex = 0;
                nextExecutionState = NextExecutionState.NextOutInterceptor;
                return true;
            } else {
                return false;
            }
        }
    }

    private class ChainSubscriber extends Subscriber<Void> {

        private final SerialSubscription subscription;
        private final ExecutionContext context;
        private final I request;
        private final O response;
        private final Subscriber<? super Void> child;

        public ChainSubscriber(SerialSubscription subscription, ExecutionContext context, I request, O response,
                               Subscriber<? super Void> child) {
            this.subscription = subscription;
            this.context = context;
            this.request = request;
            this.response = response;
            this.child = child;
        }

        @Override
        public void onCompleted() {
            InboundInterceptor<I, O> nextIn = context.nextIn(request);
            OutboundInterceptor<O> nextOut;
            if (null != nextIn) {
                Observable<Void> interceptorResult = nextIn.in(request, response);
                handleResult(interceptorResult);
            } else if (context.invokeRouter()) {
                handleResult(router.handle(request, response));
            } else if (null != (nextOut = context.nextOut())) {
                handleResult(nextOut.out(response));
            } else {
                child.onCompleted();
            }
        }

        private void handleResult(Observable<Void> aResult) {
            ChainSubscriber nextSubscriber = new ChainSubscriber(subscription, context, request, response,
                                                                 child);
            subscription.set(nextSubscriber);
            aResult.unsafeSubscribe(nextSubscriber);
        }

        @Override
        public void onError(Throwable e) {
            child.onError(e);
        }

        @Override
        public void onNext(Void aVoid) {
            child.onNext(aVoid);
        }
    }
}

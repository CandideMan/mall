package com.isc.wxy.access;

import com.google.common.util.concurrent.RateLimiter;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.exception.MallException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * Created by XY W on 2018/6/7.
 */

public class SmoothBurstyInterceptor  extends HandlerInterceptorAdapter {
    public enum LimitType {
        DROP,//丢弃
        WAIT //等待
    }

    /**
     * 限流器
     */
    private RateLimiter limiter;
    /**
     * 限流方式
     */
    private LimitType limitType = LimitType.DROP;

    public SmoothBurstyInterceptor() {
        this.limiter = RateLimiter.create(10);
    }
    /**
     * @param tps       限流量 (每秒处理量稳定吞吐率)
     * @param limitType 限流类型:等待/丢弃(达到限流量)
     */
    public SmoothBurstyInterceptor(int tps, SmoothBurstyInterceptor.LimitType limitType) {
        this.limiter = RateLimiter.create(tps);
        this.limitType = limitType;
    }
    /**
     * @param permitsPerSecond  限流量 (每秒处理量稳定吞吐率)
     * @param limitType 限流类型:等待/丢弃(达到限流量)
     *                  根据指定的稳定吞吐率和预热期来创建RateLimiter，
     *                  这里的吞吐率是指每秒多少许可数（通常是指QPS，每秒多少个请求量），
     *                  在这段预热时间内，RateLimiter每秒分配的许可数会平稳地增长直到预热期结束时达到其最大速率。
     *                  （只要存在足够请求数来使其饱和）
     */
    public SmoothBurstyInterceptor(double permitsPerSecond, SmoothBurstyInterceptor.LimitType limitType) {
        this.limiter = RateLimiter.create(permitsPerSecond, 1000, TimeUnit.MILLISECONDS);
        this.limitType = limitType;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (limitType.equals(LimitType.DROP)) {
            if (limiter.tryAcquire()) {
                return true;
            }else
            {
                throw new MallException(ResponseCode.PLEASE_WAIT);
            }
        }
        else {
            limiter.acquire();
            return true;
        }

    }

}

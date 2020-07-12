package org.dromara.soul.plugin.hystrix.command;


import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import org.dromara.soul.plugin.api.result.SoulResultEnum;
import org.dromara.soul.plugin.base.utils.SoulResultWarp;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import rx.Observable;

/**
 * hystrix command for semaphore and thread
 * @author liangziqiang
 */
public interface Command  {
     /**
      * wrap fetch Observable in {@link HystrixCommand} and {@link HystrixCommandOnThread}
      * @return {@code Observable<R>} that executes and calls back with the result of command execution
      *        or a fallback if the command fails for any reason.
      */
     Observable<Void> fetchObservable();

     /**
      * whether the 'circuit-breaker' is open
      * @return boolean
      */
     boolean isCircuitBreakerOpen();

     /**
      * generate a error when some error occurs
      * @param exchange the exchange
      * @param exception exception instance
      * @return error which be wrap by {@link SoulResultWarp}
      */
     default  Object generateError(ServerWebExchange exchange, Throwable exception) {
          Object error;
          if (exception instanceof HystrixRuntimeException) {
               HystrixRuntimeException e = (HystrixRuntimeException) exception;
               if (e.getFailureType() == HystrixRuntimeException.FailureType.TIMEOUT) {
                    exchange.getResponse().setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
                    error = SoulResultWarp.error(SoulResultEnum.SERVICE_TIMEOUT.getCode(), SoulResultEnum.SERVICE_TIMEOUT.getMsg(), null);
               } else {
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    error = SoulResultWarp.error(SoulResultEnum.SERVICE_RESULT_ERROR.getCode(), SoulResultEnum.SERVICE_RESULT_ERROR.getMsg(), null);
               }
          } else if (exception instanceof HystrixTimeoutException) {
               exchange.getResponse().setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
               error = SoulResultWarp.error(SoulResultEnum.SERVICE_TIMEOUT.getCode(), SoulResultEnum.SERVICE_TIMEOUT.getMsg(), null);
          } else {
               exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
               error = SoulResultWarp.error(SoulResultEnum.SERVICE_RESULT_ERROR.getCode(), SoulResultEnum.SERVICE_RESULT_ERROR.getMsg(), null);
          }
          return error;
     }
}

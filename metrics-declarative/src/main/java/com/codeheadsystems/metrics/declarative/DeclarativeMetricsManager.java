package com.codeheadsystems.metrics.declarative;

import com.codeheadsystems.metrics.MetricFactory;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.impl.NullMetricsImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect to manage integrations with MetricsFactory..
 */
@Aspect
public class DeclarativeMetricsManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeclarativeMetricsManager.class);
  private static final NullMetricsImpl NULL_METRICS = new NullMetricsImpl();
  private static volatile Metrics METRICS;

  /**
   * Instantiates a new Declarative metrics manager.
   */
  public DeclarativeMetricsManager() {
    LOGGER.info("DeclarativeMetricsManager()");
  }

  /**
   * Metrics metrics.
   *
   * @return the metrics
   */
  public static Metrics metrics() {
    return METRICS == null ? NULL_METRICS : METRICS;
  }

  /**
   * Around declarative factory object.
   *
   * @param point the point
   * @return the object
   * @throws Throwable the throwable
   */
  @Around("execution(* *(..)) && @annotation(com.codeheadsystems.metrics.declarative.DeclarativeFactory)")
  public Object aroundDeclarativeFactory(final ProceedingJoinPoint point) throws Throwable {
    System.out.println("aroundDeclarativeFactory({})" + point);
    final Object pjp = point.proceed(point.getArgs());
    if (pjp instanceof MetricFactory) {
      LOGGER.info("aroundDeclarativeFactory() - setting metrics to {}", pjp);
      METRICS = (Metrics) pjp;
    }
    return pjp;
  }

  /**
   * Around metrics object.
   *
   * @param point the point
   * @return the object
   * @throws Throwable the throwable
   */
  @Around("@annotation(com.codeheadsystems.metrics.declarative.Metrics)")
  public Object aroundMetrics(final ProceedingJoinPoint point) throws Throwable {
    System.out.println("aroundMetrics({})" + point);
    return point.proceed(point.getArgs());
  }
}

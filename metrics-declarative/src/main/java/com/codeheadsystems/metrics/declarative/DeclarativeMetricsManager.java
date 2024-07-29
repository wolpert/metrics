package com.codeheadsystems.metrics.declarative;

import com.codeheadsystems.metrics.MetricFactory;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.impl.NullMetricsImpl;
import java.util.concurrent.atomic.AtomicBoolean;
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
  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
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
  public MetricFactory aroundDeclarativeFactory(final ProceedingJoinPoint point) throws Throwable {
    LOGGER.debug("aroundDeclarativeFactory({})", point);
    Object result = point.proceed(point.getArgs());
    if (!(result instanceof final MetricFactory factory)) {
      final String msg = String.format("aroundDeclarativeFactory() - Unable to cast to MetricFactory, got %s from %s",
          result, point.getSignature().getName());
      LOGGER.error(msg);
      throw new ClassCastException(msg);
    }
    LOGGER.info("aroundDeclarativeFactory() - setting metrics to {}", factory);
    METRICS = factory;
    if (!INITIALIZED.compareAndSet(false, true)) {
      LOGGER.warn("aroundDeclarativeFactory() - Initialized MetricsFactory already called");
    }
    return factory;
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
    final String metricName = getMetricName(point);
    final boolean initialized = INITIALIZED.get();
    LOGGER.trace("aroundMetrics({}, {})", metricName, initialized);
    if (!initialized) {
      return point.proceed(point.getArgs());
    } else {
      return METRICS.time(metricName, () -> {
        try {
          return point.proceed(point.getArgs());
        } catch (final Throwable t) {
          throw new RuntimeException(t); // TODO: This is bad but time is using a generic exception and java is chocking.
        }
      });
    }
  }

  private String getMetricName(final ProceedingJoinPoint point) {
    final String className = point.getSignature().getDeclaringType().getSimpleName();
    final String method = point.getSignature().getName();
    return String.format("%s.%s", className, method);
  }
}

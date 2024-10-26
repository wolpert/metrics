package com.codeheadsystems.metrics.declarative;

import com.codeheadsystems.metrics.MetricFactory;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.impl.NullMetricsImpl;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
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
  private static volatile MetricFactory METRICS;

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
    LOGGER.trace("metrics():{}", METRICS);
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
  @Around("execution(* *(..)) && @annotation(com.codeheadsystems.metrics.declarative.Metrics)")
  public Object aroundMetrics(final ProceedingJoinPoint point) throws Throwable {
    final Optional<Method> method = getMethod(point);
    final String metricName = method.map(this::getMetricName).orElseGet(() -> getMetricName(point));
    final Tags tags = method.map(m -> getTags(m, point.getArgs())).orElseGet(Tags::empty);
    final boolean initialized = INITIALIZED.get();
    LOGGER.trace("aroundMetrics({}, {})", metricName, initialized);
    if (!initialized) {
      return point.proceed(point.getArgs());
    } else {
      try {
        return METRICS.time(metricName, tags, () -> {
          try {
            return point.proceed(point.getArgs());
            // Begin wacky exception handling code. If you know better, create a PR please.
            // The following code exists because the time() method uses a generic for Exceptions. When
            // you do that, you cannot ref it directly because you cannot convert the exception into a
            // throwable. (Yes, even though exception inherits from throwable.) So this is why we have
            // o write the throwable around the use of the generic.
            //
            // This is really due to the Exception not being known at compile time.
          } catch (Throwable t) {
            throw new WrappedException(t);
          }
        });
      } catch (WrappedException we) {
        // we have to rethrow because the time() generic
        throw we.getCause();
      }
      // End wacky exception handling code.
    }
  }

  private Tags getTags(final Method method, final Object[] args) {
    final Tags tags = Tags.empty();
    final Parameter[] parameters = method.getParameters();
    if (parameters.length != args.length) {
      LOGGER.warn("[BUG]getTags() - method {} has {} parameters but {} args", method.getName(), parameters.length, args.length);
      return tags;
    }
    for (int i = 0; i < args.length; i++) {
      final Tag tag = parameters[i].getAnnotation(Tag.class);
      if (tag != null) {
        final Object arg = args[i];
        final String key = tag.value().isEmpty() ? parameters[i].getName() : tag.value();
        if (arg == null) { // actually, if arg is null, do we just ignore this?
          tags.add(key, "null");
        } else {
          tags.add(key, arg.toString());
        }
      }
    }
    return tags;
  }

  private Optional<Method> getMethod(final ProceedingJoinPoint point) {
    return Optional.ofNullable(point.getSignature())
        .filter(signature -> signature instanceof MethodSignature)
        .map(signature -> (MethodSignature) signature)
        .map(MethodSignature::getMethod);
  }

  private String getMetricName(final Method method) {
    final com.codeheadsystems.metrics.declarative.Metrics annotation =
        method.getAnnotation(com.codeheadsystems.metrics.declarative.Metrics.class);
    if (annotation != null && !annotation.value().isEmpty()) { // if the value is set, use it.
      return annotation.value();
    } else {
      if (annotation == null) {
        LOGGER.warn("[BUG] there is @Metrics without an @Metrics tag. This should not be possible.");
      }
      final String className = method.getDeclaringClass().getSimpleName();
      return String.format("%s.%s", className, method.getName());
    }
  }

  private String getMetricName(final ProceedingJoinPoint point) {
    final String className = point.getSignature().getDeclaringType().getSimpleName();
    final String method = point.getSignature().getName();
    return String.format("%s.%s", className, method);
  }

  /**
   * This exists because java generics hate when you use them with exception.
   */
  static class WrappedException extends RuntimeException {
    /**
     * Instantiates a new Wrapped exception.
     *
     * @param cause the cause
     */
    WrappedException(Throwable cause) {
      super(cause);
    }
  }

}

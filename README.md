# Metrics

Project provides the ability to use micrometer metrics in your dropwizard
project that uses Dagger for dependency injection.

Also provides a test package (metrics-test) so your tests can extend
BaseMetricsTest and have access to metrics during your development. Makes it
easier to use metrics in general by making testing easier.

## Status

![Metrics Build](https://github.com/wolpert/metrics/actions/workflows/gradle.yml/badge.svg)

## To use

Include the following in your pom/gradle

```groovy
dependencies {
    implementation 'com.codeheadsystems:metrics:2.0.0'
    testImplementation 'com.codeheadsystems:metrics-test:2.0.0'
}
```

## Usage

Create a singleton instance of the metric factory, and call it directly.
If you are using Dagger or spring, it is recommended to inject the
factory into your code instead of using a static variable. 

Metric
instances are created when the with() method is called, and stored in
a thread-local variable. You can nest using with() methods or call the
metrics() method directly. 

Note if you call metrics() without being within a with() block, you will
get null metrics object. No code failure will happen, but no metrics will
be emitted. There will be log statements indicating that this has happened
for debugging purposes.

### Dropwizard

Ideally, the resource manager will enable the metrics object via closure
before your resources are there. Expect an update with a working example.

## Declarative Metrics

As much as I hate aspectj, there is a really good reason to use it
for simplifying metrics. The metrics package will end up providing
an annotation library that works with AspectJ (compile time or runtime)
so you can limit your code integration. This will include naming the
metrics and dynamic tagging support.

## Example code

The following is a basic example case. More complex examples are possible.

### Java code that uses metrics

```java
public class AClass {
  private final MetricsFactory metricsFactory;

  public AClass(final MetricsFactory metricsFactory) {
    this.metricsFactory = metricsFactory;
  }

  public boolean howSoonIsNow() {
    metricsFactory.withMetrics(metrics ->
        metrics.time("MetricName", () -> {
          internalMethod();
          return System.currentTimeMillis() > 1000;
        }));
  }

  private void internalMethod() {
    metricsFactory.metrics().increment("internalMethod.call");
  }
}
```

### Unit Test Example

```java
public class AClassTest extends BaseMetricTest {
  @Test
  public void testDoSomething_works() {
    final ACLass testInstance = new AClass(metricsFactory); // metrics from parent class
    assert testInstance.howSoonIsNow() == true; // The supplier is called from the metrics object
  }
}
```

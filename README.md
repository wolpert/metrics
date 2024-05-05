# Metrics

Project provides the ability to use micrometer metrics
in your dropwizard project that uses Dagger for dependency
injection.

Also provides a test package (metrics-test) so your tests can
extend BaseMetricsTest and have access to metrics during your
development. Makes it easier to use metrics in general by making
testing easier.

## Status
![Metrics Build](https://github.com/wolpert/metrics/actions/workflows/gradle.yml/badge.svg)

## To use

Include the following in your pom/gradle

```groovy
dependencies {
    implementation 'com.codeheadsystems:metrics:1.0.3'
    testImplementation 'com.codeheadsystems:metrics-test:1.0.3'
}
```

## Example code

The following is a basic example case. More complex examples are possible.

### Java code that uses metrics

```java
public class AClass {
  private final Metrics metrics;
  public AClass(final Metrics metrics){
    this.metrics = metrics;
  }
  public boolean doSomething() {
    return metrics.time("MetricName", () -> {
      return System.currentTimeMillis() > 1000;
    });
  }
}
```

### Unit Test Example

```java
public class AClassTest extends BaseMetricTest {
  @Test
  public void testDoSomething_works(){
    final ACLass testInstance = new AClass(metrics); // metrics from parent class
    assert testInstance.doSomething == true; // The supplier is called from the metrics object
  }
}
```

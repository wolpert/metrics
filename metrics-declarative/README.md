# Declarative Metrics

This project provides a way to use annotations to manage metrics
as opposed to instrumenting you code in details. It's designed so
you can be lazy. The performance cost is minimal and can be 
reduced further, though will never be zero with the current 
implementation.

The premise is some code provides for an active metrics factory,
then you can 'declare' your metrics which will time individual method
calls. Tags used will work within the method calls, based on 
arguments passed into methods.

## Example

```java
import javax.swing.text.html.HTML;

public class Service {

  public Service() {
    generateMetricFactory();
  }

  // You need somewhere that is executed on startup to 
  // generate the metric factory. We will use this for
  // all metrics.
  @DeclarativeFactory
  private MetricFactory generateMetricFactory() {
    MetricFactory.builder().build;
  }

  // This will emit a metric named 'Service.simpleCall' with the tag
  // 'customer' set to the value of the client argument. The second
  // argument will be ignored. It will result in a call to 'time' around
  // the method call. Any additional tags in the metric will come from how
  // the metric is configured.
  @Metrics
  public void simpleCall(@Tag("customer") String client, int arg2) {
    // Do something
  }

  // Here the metric name used is 'overridden.metric.name' instead of
  // the default 'Service.serviceMethod2'. No tags are included.
  @Metrics("overridden.metric.name")
  public void serviceMethod2(String arg1, int arg2) {
    // Adding tags to calls below here
    DeclarativeMetricsManager.metrics().and(Tags.of("tag1", "value1"));
    callOtherMethod(); // If this method has the @Metrics annotation,
    // tags there will include what we defined above.
  }

}
```
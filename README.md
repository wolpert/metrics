# Metrics

Project provides the ability to use micrometer metrics in your dropwizard
project that uses Dagger for dependency injection.

Also provides a test package (metrics-test) so your tests can extend
BaseMetricsTest and have access to metrics during your development. Makes it
easier to use metrics in general by making testing easier.

## Status

![Metrics Build](https://github.com/wolpert/metrics/actions/workflows/gradle.yml/badge.svg)

| Library             | Purpose                | Version                                                                                    |
|---------------------|------------------------|--------------------------------------------------------------------------------------------|
| metrics             | Core Library           | ![metrics](https://img.shields.io/maven-central/v/com.codeheadsystems/metrics)             |
| metrics-test        | Testing utilities      | ![metrics](https://img.shields.io/maven-central/v/com.codeheadsystems/metrics-test)        |
| metrics-micrometer  | Micrometer integration | ![metrics](https://img.shields.io/maven-central/v/com.codeheadsystems/metrics-micrometer)  |
| metrics-declarative | Declarative style      | ![metrics](https://img.shields.io/maven-central/v/com.codeheadsystems/metrics-declarative) |


## To use

Include the following in your pom/gradle

```groovy
dependencies {
    implementation 'com.codeheadsystems:metrics:VERSION'
    testImplementation 'com.codeheadsystems:metrics-test:VERSION'
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
    metricsFactory.with(metrics ->
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

## FAQ

### Why not traces?

When people say traces, they mean implementations based on the open-source
jaeger project by uber. Traces are really great and for any service that with
real TPS (greater than 1k) generates lots of data... so much so that you 
don't keep the traces over the long haul because it is simply too much data.

But the advantage of traces is you can grab a bunch of data, use that data
to inject metrics and then sample the traces for debugging your system instead
of logs. Traces/spans that remain are complete; meaning that they represent one
call through your stack from ingestion. If you just sample logs directly, you
cannot be sure that a request in service one calling service two will have logs
from both services.

The best trace solutions are proprietary and expensive. And what I learned at
Amazon, if your service is 32k TPS, no one really looks at logs or traces anyways.
It's better to have a great metrics solution. So traces are good for mid-levels, 
not small services which need logs, or large services which need to simplify
storage management. That is what this code base aims to do.

### How about metric files like Amazon used to do internally?

At amazon before cloudwatch, the internal metric service would 'write to disk'
metric results per service request when writing microservices. This metric output
would contain all metrics generated in that request. A separate process would read
these files and publish the results to the metric backend. This reduced any 
overhead of the service calling a metrics backend at the cost of files on disk.
(Similar to how traces are actually done today.) If you use cloudwatch, this is akin
to using 'metric filters' which scan your logs for metrics to publish. Cheaper than 
if you call the metrics service directly even in batches. And of course, you are less
likely to lose data if the service shutdown. (Not totally true in the old system.)

I plan on created a metric publisher for this project that will use the old Amazon
style, but this is low priority. If you use prometheus, you are pretty close to
doing this anyways... which honestly is the preferred approach. But because folks
may want different connectors, this is an easy method to add them instead of making
publishers in java. (And yes... eventually this library will be in multiple languages.)

# Metrics

Project provides the ability to use micrometer metrics
in your dropwizard project that uses Dagger for dependency
injection.

Also provides a test package (metrics-test) so your tests can
extend BaseMetricsTest and have access to metrics during your
development. Makes it easier to use metrics in general by making
testing easier.

## To use

Include the following in your pom/gradle

```groovy
dependencies {
    implementation 'com.codeheadsystems:metrics:1.0.0'
    testImplementation 'com.codeheadsystems:metrics-test:1.0.0'
}
```
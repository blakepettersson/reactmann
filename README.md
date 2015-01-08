reactmann
========

reactmann is the reactive way to monitor your apps. Built on top of [rxjava] and the [Riemann] protocol to make monitoring suck less.

Caveats
--------------

This is based on a pre-release version of vert.x and should not be considered production quality (yet).

A non-exhaustive list of TODOs':

* TCP only, for now
* No SSL support
* There's no built in way to do notifications yet. For e-mail I'll probably go with something like [vertx-mail-service](https://github.com/vert-x3/vertx-mail-service)
and there'll most likely be a Pagerduty plugin as well.
* Needs performance testing, profiling etc
* Add support for other languages, at the very least for Javascript

Usage
--------------

The simplest way to get started, assuming you're using Maven, is to use the java archetype.

```sh
mvn archetype:generate                                  \
  -DarchetypeGroupId=com.github.blakepettersson         \
  -DarchetypeArtifactId=reactmann-java-archetype        \
  -DarchetypeVersion=0.1.1-preview                      \
  -DgroupId={your group id}                             \
  -Dversion={your version}                              \
  -DartifactId={your artifact id}
```

This will generate a Maven project with a basic verticle, and a main class to run it from an IDE if desired.

In BasicVerticle.java, the generated file looks like this:

```java
public class BasicVerticle extends RiemannVerticle {
    private final Logger log = LoggerFactory.getLogger(BasicVerticle.class);

    @Override
    public void observeStream(Observable<Event> events) {
        events.subscribe(e -> {
            log.info(e);
        });
    }
}
```

To try this out, cd into the project directory and compile it.

```sh
mvn package
java -jar target/{your artifact id}-{your version}-fat.jar
```

This will start up the Reactmann service. A simple way to see if this actually works would be to start up riemann-health.
This tool requires Ruby and Rubygems to be installed first. Open up another terminal and try it out.

```sh
gem install riemann-tools
```

After this step, riemann-health will be available on your PATH. Running it is as simple as

```sh
riemann-health
```

This tool will post to localhost:5555 by default, and you should see incoming events being posted on stdout where the jar is running.

[Riemann]:http://riemann.io
[rxjava]:https://github.com/ReactiveX/RxJava
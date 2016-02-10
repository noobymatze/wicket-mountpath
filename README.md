# Wicket @MountPath annotation

Mapping a path to a WebPage in Wicket is usually done by calling 
`mountPage("/yeah", MyClass.class)` in the `init` method of a WebApplication. 
Sometimes though, it would be nice to just throw a small annotation at the top 
of the WebPage and be done with it, especially, when it comes to `PageParameters`.

There already exists a solution to this 
[problem](https://github.com/wicketstuff/core/wiki/Annotation), which scans
the entire classpath during the boot of the application, which can be restricted
to a certain package (correct me, if I am wrong). This little *experimental* 
library provides an annotation Processor instead, which automatically generates 
the code for mounting pages based on the annotations encountered during the 
compilation.

Here is a small example:

```java
@MountPath("/index")
class IndexPage extends WebPage {
}
```

Will result in the following generated class:

```java
public final class MountPathConfiguration {
  public void configure(WebApplication application) {
    application.mountPage("/index", IndexPage.class);
  }
}
```

This one can thus be used in the WebApplication as you would with any other 
configuration:

```java
public class MyApplication extends WebApplication {

  @Override
  public void init() {
    super.init();

    new MountPathConfiguration().configure(this);
  }
}
```

The code looks terrible and the processing is totally bug ridden, so don't
copy paste the code and use it anywhere as of now!



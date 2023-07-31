An extension that supports automatic refresh configuration without specifying dataId, used for Nacos Spring Boot.

## Prerequisites
- JDK 1.8 and above
- [Maven](http://maven.apache.org/) 3.0 and above
- Spring Boot 2.4 and above

## Usage

Add a dependency using maven:

```xml
<!--add dependency in pom.xml-->
<dependency>
    <groupId>io.github.yanghao888</groupId>
    <artifactId>nacos-config-autorefresh-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```
## Samples

```java
import io.github.yanghao888.boot.context.properties.AutoRefresh;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * User configuration properties.
 *
 * @author Lion
 */
@AutoRefresh /* Support automatic refresh configuration */
@ConfigurationProperties(prefix = "user")
public class UserProperties {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

```java
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application.
 *
 * @author Lion
 */
@RestController
@EnableNacosConfig
@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Autowired
    UserProperties properties;

    @GetMapping("/name")
    public String name() {
        return properties.getName();
    }
}
```

## License
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) Copyright (C) Apache Software Foundation
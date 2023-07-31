/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.yanghao888.boot.context.properties;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoRefreshTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SmartConfigurationPropertiesRebinder.class));

    @Test
    public void testAnnotatedClass() {
        runner.withPropertyValues("test.name=Lion")
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(TestProperties.class);
                    assertThat(context).getBean(TestProperties.class).hasFieldOrPropertyWithValue("name", "Lion");

                    MockPropertySource propertySource = new MockPropertySource()
                            .withProperty("test.name", "Linus");
                    context.getEnvironment().getPropertySources().addFirst(propertySource);

                    ConfigService configService = Mockito.mock(ConfigService.class);
                    context.publishEvent(new NacosConfigReceivedEvent(configService, "", "", "", ""));

                    assertThat(context).hasSingleBean(TestProperties.class);
                    assertThat(context).getBean(TestProperties.class).hasFieldOrPropertyWithValue("name", "Linus");
                });
    }

    @Test
    public void testAnnotatedBeanFactoryMethod() {
        runner.withPropertyValues("user.age=12")
                .withUserConfiguration(UserConfiguration.class, ConfigurationPropertiesAutoConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(User.class);
                    assertThat(context).getBean(User.class).hasFieldOrPropertyWithValue("age", 12);

                    MockPropertySource propertySource = new MockPropertySource()
                            .withProperty("user.age", 18);
                    context.getEnvironment().getPropertySources().addFirst(propertySource);

                    ConfigService configService = Mockito.mock(ConfigService.class);
                    context.publishEvent(new NacosConfigReceivedEvent(configService, "", "", "", ""));

                    assertThat(context).hasSingleBean(User.class);
                    assertThat(context).getBean(User.class).hasFieldOrPropertyWithValue("age", 18);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(TestProperties.class)
    static class TestConfiguration {
    }

    @AutoRefresh
    @ConfigurationProperties(prefix = "test")
    static class TestProperties {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class UserConfiguration {

        @Bean
        @AutoRefresh
        @ConfigurationProperties(prefix = "user")
        public User user() {
            return new User();
        }
    }

    static class User {

        private int age;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

}

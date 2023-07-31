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

import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StopWatch;

import java.util.Map;

/**
 * Listens for {@link NacosConfigReceivedEvent} and rebinds beans that were bound to the
 * {@link Environment} using {@link ConfigurationProperties
 * <code>@ConfigurationProperties</code>}. When these beans are re-bound and
 * re-initialized, the changes are available immediately to any component that is using
 * the <code>@ConfigurationProperties</code> bean.
 *
 * @author Lion
 * @since 1.0.0
 */
@ConditionalOnClass({ConfigurationPropertiesBean.class, NacosConfigReceivedEvent.class})
public class SmartConfigurationPropertiesRebinder implements ApplicationContextAware,
        ApplicationListener<NacosConfigReceivedEvent> {

    private static final Logger log = LoggerFactory.getLogger(SmartConfigurationPropertiesRebinder.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(NacosConfigReceivedEvent event) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Map<String, ConfigurationPropertiesBean> configurationPropertiesBeans = ConfigurationPropertiesBean.getAll(applicationContext);
        configurationPropertiesBeans.forEach((beanName, configurationPropertiesBean) -> {
            if (applicationContext.findAnnotationOnBean(beanName, AutoRefresh.class) != null) {
                Object bean = configurationPropertiesBean.getInstance();
                applicationContext.getAutowireCapableBeanFactory().destroyBean(bean);
                applicationContext.getAutowireCapableBeanFactory().initializeBean(bean, beanName);
            }
        });

        stopWatch.stop();
        if (log.isDebugEnabled()) {
            log.debug("Refresh config completed, cost time in millisecond is: {}", stopWatch.getTotalTimeMillis());
        }
    }
}

/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.common;

import org.coodex.util.ServiceLoaderFacade;

import java.util.Map;

/**
 * 使用concrete.properties解决冲突的SPIFacade。
 * <p>
 * 某个接口有多个services配置时，getInstance的时候会产生冲突，ConcreteSPIFacade使用concrete.properties解决冲突。
 * <pre>
 *     <i>interfaceClass</i>.provider = <i>service Class</i>
 * </pre>
 * <p>
 * Created by davidoff shen on 2016-09-08.
 */
public abstract class ConcreteServiceLoader<T> extends ServiceLoaderFacade<T> {


    protected ConcreteServiceLoader() {
        super();
    }

    private boolean init = false;

    @Override
    protected void loadInstances() {
        synchronized (this) {
            super.loadInstances();
            if (!init) {
                try {
                    Map<String, T> instances = $getInstances();
                    Map<String, T> beans = BeanProviderFacade.getBeanProvider().getBeansOfType(getInterfaceClass());
                    if (beans != null && beans.size() > 0) {
                        for (String key : beans.keySet()) {
                            T t = beans.get(key);
                            if (t != null && !instances.values().contains(t)) {
                                instances.put(key, t);
                            }
                        }
                    }
                } catch (Throwable th) {
                }
                init = true;
            }
        }
    }

    @Override
    protected T conflict(Class<? extends T> providerClass, Map<String, T> map) {
        String key = ConcreteHelper.getProfile().getString(providerClass + ".provider");
        return map.containsKey(key) ? map.get(key) : super.conflict(providerClass, map);
    }

    @Override
    protected T conflict() {
        String key = ConcreteHelper.getProfile().getString(getInterfaceClass().getCanonicalName() + ".provider");
        Map<String, T> instances = $getInstances();
        return instances.containsKey(key) ? instances.get(key) : super.conflict();
    }

    protected final T getDefaultProviderFromProfile() {
        String key = ConcreteHelper.getProfile().getString(getInterfaceClass().getCanonicalName() + ".default");
        Map<String, T> instances = $getInstances();
        return instances.containsKey(key) ? instances.get(key) : null;
    }

    protected T getConcreteDefaultProvider(){
        return super.getDefaultProvider();
    }

    @Override
    public final T getDefaultProvider() {
        T instance = getDefaultProviderFromProfile();
        return instance == null ? getConcreteDefaultProvider() : instance;
    }
}


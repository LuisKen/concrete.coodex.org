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

package org.coodex.concrete.jaxrs;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.jaxrs.client.ClientInstanceFactory;
import org.coodex.concrete.jaxrs.client.impl.JavaProxyClientInstanceFactory;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.ServiceLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * java client for concrete jaxrs service
 * <p>
 * Created by davidoff shen on 2016-12-07.
 */
public final class Client {

    private static final ServiceLoader<ClientInstanceFactory> BUILDER = new ConcreteServiceLoader<ClientInstanceFactory>() {
        private ClientInstanceFactory defaultFactory = new JavaProxyClientInstanceFactory();

        @Override
        public ClientInstanceFactory getConcreteDefaultProvider() {
            return defaultFactory;
        }
    };

    private static Map<String, ConcreteService> INSTANCE_CACHE =
            new HashMap<String, ConcreteService>();

    private static String getKey(Class<? extends ConcreteService> type, String domain) {
        return type.getName() + (domain == null ? "" : ("@" + domain));
    }

    /**
     * 获得实例
     *
     * @param type
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends ConcreteService> T getBean(Class<? extends T> type, String domain) {
        if (type == null) throw new NullPointerException("type MUST NOT NULL.");
        synchronized (INSTANCE_CACHE) {
            String key = getKey(type, domain);
            ConcreteService instance = INSTANCE_CACHE.get(key);
            if (instance == null) {
                instance = BUILDER.getInstance().create(type, domain);
                INSTANCE_CACHE.put(key, instance);
            }
            return (T) instance;
        }
    }

    public static <T extends ConcreteService> T getBean(Class<? extends T> type) {
        return getBean(type, null);
    }


//    private static final ServiceLoaderFacade<InvokerFactory> INVOKER_FACTORY_SPI_FACADE =
//            new ConcreteServiceLoader<InvokerFactory>() {
//            };

//    protected static String getServiceRoot(String domain) {
//
//        String s = domain == null ?
//                ConcreteToolkit.getProfile().getString("concrete.serviceRoot", "").trim() :
//                ConcreteToolkit.getProfile().getString("concrete." + domain + ".serviceRoot", domain);
//        char[] buf = s.toCharArray();
//        int len = buf.length;
//        while (len > 0 && buf[len - 1] == '/') {
//            len--;
//        }
//        return new String(buf, 0, len);
//    }


//    public static Invoker getInvoker(String domain) {
//
//        domain = getServiceRoot(domain);
//
//        for (InvokerFactory factory : INVOKER_FACTORY_SPI_FACADE.getAllInstances()) {
//            if (factory.accept(domain)) {
//                return factory.getInvoker(domain);
//            }
//        }
//        throw new RuntimeException("unable found "
//                + InvokerFactory.class.getName() + " service for [" + domain + "]");
//    }


    // ------------
    public static final Unit getUnitFromContext(DefinitionContext context, MethodInvocation invocation) {
        return JaxRSHelper.getUnitFromContext(context, invocation);
    }

}

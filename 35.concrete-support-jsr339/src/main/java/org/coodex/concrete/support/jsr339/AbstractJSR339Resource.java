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

package org.coodex.concrete.support.jsr339;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.jaxrs.AbstractJAXRSResource;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.concurrent.components.PriorityRunnable;

import javax.ws.rs.container.AsyncResponse;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;


/**
 * Created by davidoff shen on 2016-11-25.
 */
public abstract class AbstractJSR339Resource<T extends ConcreteService> extends AbstractJAXRSResource<T> {

    @Override
    protected int getMethodStartIndex() {
        return 2;
    }

    private final static Executor EXECUTOR = ExecutorsHelper.newPriorityThreadPool(
            ConcreteHelper.getProfile().getInt("jsr339.threadpool.corePoolSize", 0),
            ConcreteHelper.getProfile().getInt("jsr339.threadpool.maximumPoolSize", Integer.MAX_VALUE)
    );


    /**
     * @return
     */
    protected static Executor getExecutor() {
        return EXECUTOR;
    }


    protected void __execute(final String methodName, final AsyncResponse asyncResponse, final String tokenId, final Object... params) {

        final Method method = findMethod(methodName, null);

        getExecutor().execute(new PriorityRunnable(getPriority(method), new Runnable() {
            @Override
            public void run() {
                try {

                    asyncResponse.resume(invokeByTokenId(tokenId, method, params));
                } catch (Throwable th) {
                    asyncResponse.resume(th);
                }
            }
        }));

    }

    protected void execute(String invokeMethodName, AsyncResponse asyncResponse, String tokenId, Object... objects) {
        __execute(invokeMethodName, asyncResponse, tokenId, objects);
    }


}

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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.LogAtomic;
import org.coodex.concrete.api.OperationLog;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.Common;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;

import static org.coodex.concrete.common.AbstractMessageFacade.getLogFormatter;
import static org.coodex.concrete.common.AbstractMessageFacade.getPatternLoader;
import static org.coodex.concrete.common.ConcreteContext.LOGGING;
import static org.coodex.concrete.common.ConcreteContext.getLoggingData;

/**
 * Created by davidoff shen on 2017-05-08.
 */
public class OperationLogInterceptor extends AbstractInterceptor {

    private final static Logger log = LoggerFactory.getLogger(OperationLogInterceptor.class);

    private static final OperationLogger DEFAULT_LOGGER = new OperationLogger() {
        @Override
        public void log(String accountId, String accountName, String category, String subClass, String message) {
            log.info("accountId: {}; accountName: {}; category: {}; subClass: {}; message: {}",
                    accountId, accountName, category, subClass, message);
        }
    };

    private final static ServiceLoader<OperationLogger> LOGGER_SERVICE_LOADER = new ConcreteServiceLoader<OperationLogger>() {
        @Override
        public OperationLogger getConcreteDefaultProvider() {
            return DEFAULT_LOGGER;
        }
    };

    @Override
    public int getOrder() {
        return InterceptOrders.OPERATION_LOG;
    }

    @Override
    public boolean accept(RuntimeContext context) {
        return context.getAnnotation(OperationLog.class) != null || context.getAnnotation(LogAtomic.class) != null;
    }

    static ThreadLocal<Account<? extends Serializable>> OPERATOR = new ThreadLocal<Account<? extends Serializable>>();

    private Account<? extends Serializable> getOperator() {
        try {
            return TokenWrapper.getInstance().currentAccount();
        } catch (Throwable th) {
            return OPERATOR.get();
        } finally {
            OPERATOR.remove();
        }
    }

    @Override
    public Object around(final RuntimeContext context, final MethodInvocation joinPoint) throws Throwable {
        return LOGGING.run(new HashMap<String, Object>(), new ConcreteClosure() {
            @Override
            public Object concreteRun() throws Throwable {
                return $$after(context, joinPoint, joinPoint.proceed());
            }
        });
    }

    //    @Override
    private Object $$after(RuntimeContext context, MethodInvocation joinPoint, Object result) {
        try {
            Account<? extends Serializable> account = getOperator();
            String accountId = account == null ? null : account.getId().toString();
            String accountName = account == null ? "Anonymous" : null;
            if (account != null) {
                accountName = account instanceof NamedAccount ? ((NamedAccount) account).getName() : "Unknown";
            }
            String category = null;
            String subClass = null;
            String messageTemplate = null;
            LogAtomic.LoggingType loggingType = LogAtomic.LoggingType.DATA;
            Class<? extends LogFormatter> formatterClass = LogFormatter.class;
            Class<? extends MessagePatternLoader> patternLoaderClass = MessagePatternLoader.class;
            Class<? extends OperationLogger> loggerClass = OperationLogger.class;


            OperationLog operationLog = context.getAnnotation(OperationLog.class);
            if (operationLog != null) {
                category = operationLog.category();
                formatterClass = operationLog.formatterClass();
                patternLoaderClass = operationLog.patternLoaderClass();
                loggerClass = operationLog.loggerClass();
            }

            LogAtomic logAtomic = context.getAnnotation(LogAtomic.class);
            if (logAtomic != null) {
                if (!Common.isBlank(logAtomic.category()))
                    category = logAtomic.category();
                subClass = logAtomic.subClass();
                loggingType = logAtomic.loggingType();
                messageTemplate = logAtomic.message();
            }
            if (Common.isBlank(subClass)) subClass = context.getDeclaringMethod().getName();


            if (isDoLog(loggingType)) {
                getLogger(loggerClass)
                        .log(accountId, accountName, category, subClass,
                                buildLog(category, subClass, messageTemplate, formatterClass, patternLoaderClass));
            }

        } catch (Throwable th) {
            log.warn("{}", th.getLocalizedMessage(), th);
        } finally {
            return super.after(context, joinPoint, result);
        }
    }

    private boolean isDoLog(LogAtomic.LoggingType loggingType) {
        boolean doLog = false;
        switch (loggingType) {
            case NO:
                break;
            case DATA:
                doLog = getLoggingData().size() > 0;
                break;
            case ALWAYS:
                doLog = true;
        }
        return doLog;
    }


    private static String buildLog(String category, String subClass, String messageTemplate,
                                   Class<? extends LogFormatter> formatterClass,
                                   Class<? extends MessagePatternLoader> patternLoaderClass) {

        String key = null, template = null;
        if (messageTemplate != null && messageTemplate.startsWith("{") && messageTemplate.endsWith("}")) {
            key = Common.trim(messageTemplate, '{', '}', ' ');
        } else if(messageTemplate != null){
            template = messageTemplate;
        }
        if(template == null) {
            if (key == null) key = getMessageKey(category, subClass);
            template = getPatternLoader(patternLoaderClass).getMessageTemplate(key);
        }


        return getLogFormatter(formatterClass)
                .format(messageTemplate, getLoggingData());
    }

    private static String getMessageKey(String category, String subClass) {
        if (Common.isBlank(category) && Common.isBlank(subClass)) return null;

        StringBuilder builder = new StringBuilder();
        if (!Common.isBlank(category)) builder.append(category);
        if (!Common.isBlank(subClass)) {
            if (builder.length() > 0) builder.append('.');
            builder.append(subClass);
        }
        return builder.toString();
    }

    private static OperationLogger getLogger(Class<? extends OperationLogger> loggerClass) {
        return loggerClass == null || loggerClass == OperationLogger.class ?
                LOGGER_SERVICE_LOADER.getInstance() :
                LOGGER_SERVICE_LOADER.getInstance(loggerClass);
    }
}

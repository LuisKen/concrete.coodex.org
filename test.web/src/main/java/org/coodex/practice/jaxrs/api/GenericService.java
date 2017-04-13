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

package org.coodex.practice.jaxrs.api;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.practice.jaxrs.pojo.GenericPojo;

import java.util.List;

/**
 * Created by davidoff shen on 2017-04-13.
 */
public interface GenericService<P extends GenericPojo, X extends GenericPojo> extends ConcreteService {

    P genericTest1001(P x);

    List<P> genericTest1002(List<X> x);
}
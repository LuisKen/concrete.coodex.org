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

package org.coodex.pojomocker.mockers;

import org.coodex.pojomocker.AbstractPrimitiveMocker;
import org.coodex.pojomocker.annotations.INTEGER;

/**
 * Created by davidoff shen on 2017-05-15.
 */
public class DefaultIntegerMocker extends AbstractPrimitiveMocker<Integer, INTEGER> {
    @Override
    protected Object toPrimitive(Integer integer) {
        return integer == null ? 0 : integer.intValue();
    }

    @Override
    protected Integer $mock(INTEGER mockAnnotation) {
        int min = mockAnnotation.min();
        int max = mockAnnotation.max();
        boolean positive = true;
        if(min == max) return min;
        if(min > max){
            int t = min;
            min = max;
            max = t;
        }

        if(min <0 && max > 0){
            if(Math.random() < 0.5){
                max = 0;
                positive = false;
            } else {
                min = 0;
            }
        }

        return (int)(Math.random() * (max - min) * (positive ? 1 : -1));
    }
}

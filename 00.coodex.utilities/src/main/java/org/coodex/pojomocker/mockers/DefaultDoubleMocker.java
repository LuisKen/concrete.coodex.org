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
import org.coodex.pojomocker.annotations.DOUBLE;
import org.coodex.util.Common;

/**
 * Created by davidoff shen on 2017-05-15.
 */
public class DefaultDoubleMocker extends AbstractPrimitiveMocker<Double, DOUBLE> {
    @Override
    protected Object toPrimitive(Double aDouble) {
        return aDouble == null ? 0.0d : aDouble.doubleValue();
    }

    @Override
    protected Double $mock(DOUBLE mockAnnotation) {
        if(mockAnnotation.range() != null && mockAnnotation.range().length > 0){
            return Common.random(mockAnnotation.range());
        }
        double min = mockAnnotation.min();
        double max = mockAnnotation.max();
        if(min == max) return min;
        if(min > max){
            double t = min;
            min = max;
            max = t;
        }

        return Math.random() * (max - min)  + min;
    }
}

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.jdbc.spi.type;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class TypeLiteralCalculation
{
    private final String calculation;

    public TypeLiteralCalculation(String calculation)
    {
        this.calculation = checkNotNull(calculation, "calculation is null");
    }

    public String getCalculation()
    {
        return calculation;
    }

    @Override
    public String toString()
    {
        return calculation;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeLiteralCalculation that = (TypeLiteralCalculation) o;
        return Objects.equal(calculation, that.calculation);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(calculation);
    }
}

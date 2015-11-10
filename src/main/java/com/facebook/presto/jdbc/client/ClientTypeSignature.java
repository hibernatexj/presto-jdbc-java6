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
package com.facebook.presto.jdbc.client;

import com.facebook.presto.jdbc.spi.type.TypeSignature;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public class ClientTypeSignature
{
    private static final Pattern PATTERN = Pattern.compile(".*[<>,].*");
    private final String rawType;
    private final List<ClientTypeSignature> typeArguments;
    private final List<Object> literalArguments;

    public ClientTypeSignature(TypeSignature typeSignature)
    {
        this(
                typeSignature.getBase(),
                toTypeArguments(typeSignature.getParameters()),
                typeSignature.getLiteralParameters());
    }

    @JsonCreator
    public ClientTypeSignature(
            @JsonProperty("rawType") String rawType,
            @JsonProperty("typeArguments") List<ClientTypeSignature> typeArguments,
            @JsonProperty("literalArguments") List<Object> literalArguments)
    {
        checkArgument(rawType != null, "rawType is null");
        this.rawType = rawType;
        checkArgument(!rawType.isEmpty(), "rawType is empty");
        checkArgument(!PATTERN.matcher(rawType).matches(), "Bad characters in rawType type: %s", rawType);
        checkArgument(typeArguments != null, "typeArguments is null");
        checkArgument(literalArguments != null, "literalArgumentsBuilder is null");

        this.typeArguments = ImmutableList.copyOf(typeArguments);

        // Jackson will convert small a small long to an int
        ImmutableList.Builder<Object> literalArgumentsBuilder = ImmutableList.builder();
        for (Object literal : literalArguments) {
            if (literal instanceof Integer) {
                literalArgumentsBuilder.add(((Integer) literal).longValue());
            }
            else {
                literalArgumentsBuilder.add(literal);
            }
        }

        this.literalArguments = literalArgumentsBuilder.build();

        for (Object literal : this.literalArguments) {
            checkArgument(literal instanceof String || literal instanceof Long, "Unsupported literal type: %s", literal.getClass());
        }
    }

    private static List<ClientTypeSignature> toTypeArguments(List<TypeSignature> parameters)
    {
        ImmutableList.Builder<ClientTypeSignature> builder = ImmutableList.builder();
        for (TypeSignature parameter : parameters) {
            builder.add(new ClientTypeSignature(parameter));
        }
        return builder.build();
    }

    @JsonProperty
    public String getRawType()
    {
        return rawType;
    }

    @JsonProperty
    public List<ClientTypeSignature> getTypeArguments()
    {
        return typeArguments;
    }

    @JsonProperty
    public List<Object> getLiteralArguments()
    {
        return literalArguments;
    }

    @Override
    public String toString()
    {
        StringBuilder typeName = new StringBuilder(rawType);
        if (!typeArguments.isEmpty()) {
            typeName.append("<");
            boolean first = true;
            for (ClientTypeSignature argument : typeArguments) {
                if (!first) {
                    typeName.append(",");
                }
                first = false;
                typeName.append(argument.toString());
            }
            typeName.append(">");
        }
        if (!literalArguments.isEmpty()) {
            typeName.append("(");
            boolean first = true;
            for (Object parameter : literalArguments) {
                if (!first) {
                    typeName.append(",");
                }
                first = false;
                if (parameter instanceof String) {
                    typeName.append("'").append(parameter).append("'");
                }
                else {
                    typeName.append(parameter.toString());
                }
            }
            typeName.append(")");
        }

        return typeName.toString();
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

        ClientTypeSignature other = (ClientTypeSignature) o;

        return equal(this.rawType.toLowerCase(Locale.ENGLISH), other.rawType.toLowerCase(Locale.ENGLISH)) &&
                equal(this.typeArguments, other.typeArguments) &&
                equal(this.literalArguments, other.literalArguments);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(rawType.toLowerCase(Locale.ENGLISH), typeArguments, literalArguments);
    }
}

/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes2.specs;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import java.util.function.Function;
import java.util.stream.Stream;

public abstract class CompositeExclude implements ExcludeSpec {
    private final ImmutableSet<ExcludeSpec> components;
    private final int hashCode;

    protected CompositeExclude(ImmutableSet<ExcludeSpec> components) {
        this.components = components;
        this.hashCode = components.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompositeExclude that = (CompositeExclude) o;
        return hashCode == that.hashCode &&
            Objects.equal(components, that.components);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    protected int size() {
        return components.size();
    }

    protected <T extends CompositeExclude> T add(ExcludeSpec other, Function<ImmutableSet<ExcludeSpec>, T> factory) {
        ImmutableSet.Builder<ExcludeSpec> allSpecs = ImmutableSet.builderWithExpectedSize(components.size() + 1);
        allSpecs.addAll(components);
        allSpecs.add(other);
        return factory.apply(allSpecs.build());
    }

    protected final Stream<ExcludeSpec> components() {
        return components.stream();
    }

    @Override
    public String toString() {
        return "{" + getDisplayName() +
            " " + components +
            '}';
    }

    protected abstract String getDisplayName();
}

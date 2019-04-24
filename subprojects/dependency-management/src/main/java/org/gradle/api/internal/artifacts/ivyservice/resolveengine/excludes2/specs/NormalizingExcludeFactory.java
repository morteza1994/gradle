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

import org.gradle.internal.component.model.IvyArtifactName;

import javax.annotation.Nullable;

public class NormalizingExcludeFactory extends DefaultExcludeFactory {
    @Override
    public ExcludeSpec anyOf(ExcludeSpec... specs) {
        if (specs.length == 0) {
            return nothing();
        }
        ExcludeSpec union = null;
        for (ExcludeSpec spec : specs) {
            union = performUnion(union, spec);
        }
        return union;
    }

    @Override
    public ExcludeSpec allOf(ExcludeSpec... specs) {
        if (specs.length == 0) {
            return nothing();
        }
        ExcludeSpec intersection = null;
        for (ExcludeSpec spec : specs) {
            intersection = performIntersection(intersection, spec);
        }
        return intersection;
    }

    private ExcludeSpec performUnion(@Nullable ExcludeSpec left, ExcludeSpec right) {
        if (left == null) {
            return right;
        }
        if (left.equals(right)) {
            return left;
        }
        if (left instanceof CompositeExclude) {
            ExcludeSpec origLeft = left;
            left = right;
            right = origLeft;
        }
        if (left instanceof ExcludeEverything) {
            return left;
        }
        if (right instanceof ExcludeEverything) {
            return right;
        }
        if (left instanceof ExcludeNothing) {
            return right;
        }
        if (right instanceof ExcludeNothing) {
            return left;
        }
        if (left instanceof DefaultExclude) {
            return performUnion((DefaultExclude) left, right);
        }
        if (left instanceof ExcludeAllOf) {
            return performUnion((ExcludeAllOf) left, right);
        }
        if (left instanceof ExcludeAnyOf) {
            return performUnion((ExcludeAnyOf) left, right);
        }
        throw unexpectedSpec(left);
    }

    private UnsupportedOperationException unexpectedSpec(ExcludeSpec spec) {
        return new UnsupportedOperationException("Unexpected spec type: " + spec);
    }

    private ExcludeSpec performUnion(DefaultExclude left, ExcludeSpec right) {
        if (right instanceof DefaultExclude) {
            return performUnion(left, (DefaultExclude) right);
        } else if (right instanceof ExcludeAnyOf) {
            return performUnion((ExcludeAnyOf)right, left);
        } else if (right instanceof ExcludeAllOf) {
            return performUnion((ExcludeAllOf)right, left);
        }
        throw unexpectedSpec(right);
    }

    private ExcludeSpec performUnion(DefaultExclude left, DefaultExclude right) {
        ExcludeSpec merged = trySimplifyUnion(left, right);
        if (merged != null) {
            return merged;
        }
        merged = trySimplifyUnion(right, left);
        if (merged != null) {
            return merged;
        }
        return super.anyOf(left, right);
    }

    private ExcludeSpec trySimplifyUnion(DefaultExclude left, DefaultExclude right) {
        if (left.group != null && left.group.equals(right.group)) {
            if (left.module == null && (right.module != null || right.artifact != null)) {
                return left;
            }
        }
        return null;
    }

    private ExcludeSpec performUnion(ExcludeAllOf left, ExcludeSpec right) {
        if (left.components().anyMatch(right::equals)) {
            // A ∪ (A ∩ B) = A
            return right;
        }
        return allOf(left.components().map(e -> performUnion(e, right)).toArray(ExcludeSpec[]::new));
    }

    private ExcludeSpec performUnion(ExcludeAnyOf left, ExcludeSpec right) {
        return left.add(right, ExcludeAnyOf::new);
    }

    // intersection

    private ExcludeSpec performIntersection(@Nullable ExcludeSpec left, ExcludeSpec right) {
        if (left == null) {
            return right;
        }
        if (left.equals(right)) {
            return left;
        }
        if (left instanceof CompositeExclude) {
            ExcludeSpec origLeft = left;
            left = right;
            right = origLeft;
        }
        if (left instanceof ExcludeEverything) {
            return right;
        }
        if (right instanceof ExcludeEverything) {
            return left;
        }
        if (left instanceof ExcludeNothing) {
            return left;
        }
        if (right instanceof ExcludeNothing) {
            return right;
        }
        if (left instanceof DefaultExclude) {
            return performIntersection((DefaultExclude) left, right);
        }
        if (left instanceof ExcludeAllOf) {
            return performIntersection((ExcludeAllOf) left, right);
        }
        if (left instanceof ExcludeAnyOf) {
            return performIntersection((ExcludeAnyOf) left, right);
        }
        throw unexpectedSpec(left);
    }

    private ExcludeSpec performIntersection(DefaultExclude left, ExcludeSpec right) {
        if (right instanceof DefaultExclude) {
            return performIntersection(left, (DefaultExclude) right);
        } else if (right instanceof ExcludeAnyOf) {
            return performIntersection((ExcludeAnyOf) right, left);
        } else if (right instanceof ExcludeAllOf) {
            return performIntersection((ExcludeAllOf) right, left);
        }
        return super.allOf(left, right);
    }

    private ExcludeSpec performIntersection(DefaultExclude left, DefaultExclude right) {
        ExcludeSpec merged = trySimplifyIntersection(left, right);
        if (merged != null) {
            return merged;
        }
        return super.allOf(left, right);
    }

    private ExcludeSpec trySimplifyIntersection(DefaultExclude left, DefaultExclude right) {
        ValueWrapper<String> group = valueOf(left.group, right.group);
        ValueWrapper<String> module = valueOf(left.module, right.module);
        ValueWrapper<IvyArtifactName> artifact = valueOf(left.artifact, right.artifact);
        if (group.incompatible || module.incompatible || artifact.incompatible) {
            return nothing();
        }
        return new DefaultExclude(group.value, module.value, artifact.value);
    }

    private static <T> ValueWrapper<T> valueOf(T t1, T t2) {
        if (t1 != null && t2 != null) {
            if (t1.equals(t2)) {
                return ValueWrapper.present(t1);
            }
            return ValueWrapper.incompatible();
        }
        if (t1 == null && t2 == null) {
            return ValueWrapper.present(null);
        }
        if (t1 != null) {
            return ValueWrapper.present(t1);
        }
        return ValueWrapper.present(t2);
    }

    private ExcludeSpec performIntersection(ExcludeAllOf left, ExcludeSpec right) {
        return left.add(right, ExcludeAllOf::new);
    }

    private ExcludeSpec performIntersection(ExcludeAnyOf left, ExcludeSpec right) {
        return anyOf(left.components().map(e -> performIntersection(e, right)).toArray(ExcludeSpec[]::new));
    }

    private static class ValueWrapper<T> {
        final T value;
        final boolean incompatible;

        public ValueWrapper(T value, boolean present) {
            this.value = value;
            this.incompatible = !present;
        }

        public static <T> ValueWrapper<T> present(T value) {
            return new ValueWrapper<>(value, true);
        }

        public static <T> ValueWrapper<T> incompatible() {
            return new ValueWrapper<>(null, false);
        }

    }
}

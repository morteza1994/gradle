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

public class DefaultExcludeFactory implements ExcludeFactory {
    @Override
    public ExcludeSpec nothing() {
        return ExcludeNothing.get();
    }

    @Override
    public ExcludeSpec everything() {
        return ExcludeEverything.get();
    }

    @Override
    public ExcludeSpec group(String group) {
        return DefaultExclude.group(group);
    }

    @Override
    public ExcludeSpec module(String module) {
        return DefaultExclude.module(module);
    }

    @Override
    public ExcludeSpec groupAndModule(String group, String module) {
        return DefaultExclude.module(group, module);
    }

    @Override
    public ExcludeSpec artifact(IvyArtifactName artifact) {
        return DefaultExclude.artifact(artifact);
    }

    @Override
    public ExcludeSpec anyOf(ExcludeSpec... specs) {
        return ExcludeAnyOf.of(specs);
    }

    @Override
    public ExcludeSpec allOf(ExcludeSpec... specs) {
        return ExcludeAllOf.of(specs);
    }
}

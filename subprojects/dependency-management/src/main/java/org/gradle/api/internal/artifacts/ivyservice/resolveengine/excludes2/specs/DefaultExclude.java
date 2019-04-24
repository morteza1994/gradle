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

import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.internal.component.model.IvyArtifactName;

import javax.annotation.Nullable;
import java.util.Objects;

class DefaultExclude implements ExcludeSpec {
    final String group;
    final String module;
    final IvyArtifactName artifact;
    private final int hashCode;

    public static ExcludeSpec group(String group) {
        return new DefaultExclude(group, null, null);
    }

    public static ExcludeSpec module(String module) {
        return new DefaultExclude(null, module, null);
    }

    public static ExcludeSpec module(String group, String module) {
        return new DefaultExclude(group, module, null);
    }

    public static ExcludeSpec artifact(IvyArtifactName artifact) {
        return new DefaultExclude(null, null, artifact);
    }

    DefaultExclude(@Nullable String group, @Nullable String module, @Nullable IvyArtifactName artifact) {
        this.group = group;
        this.module = module;
        this.artifact = artifact;
        hashCode = Objects.hash(group, module, artifact);
    }

    @Override
    public boolean excludes(ModuleIdentifier module) {
        if (group == null) {
            return excludesModule(module.getName());
        }
        return excludesGroup(group) && (this.module == null || excludesModule(module.getName()));
    }

    @Override
    public boolean excludesGroup(String group) {
        return Objects.equals(this.group, group);
    }

    @Override
    public boolean excludesModule(String module) {
        return Objects.equals(this.module, module);
    }

    @Override
    public boolean excludesArtifact(IvyArtifactName artifactName) {
        return Objects.equals(this.artifact, artifactName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultExclude that = (DefaultExclude) o;
        return com.google.common.base.Objects.equal(group, that.group) &&
            com.google.common.base.Objects.equal(module, that.module) &&
            com.google.common.base.Objects.equal(artifact, that.artifact);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{exclude ");
        if (group != null) {
            sb.append("group = ").append(group).append(", ");
        }
        if (module != null) {
            sb.append("module = ").append(module).append(", ");
        }
        if (artifact != null) {
            sb.append("artifact = ").append(artifact).append(", ");
        }
        sb.append("}");
        return sb.toString();
    }
}

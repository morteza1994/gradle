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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes2.specs

import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.internal.component.model.DefaultIvyArtifactName
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class NormalizingExcludeFactoryTest extends Specification {

    @Subject
    private NormalizingExcludeFactory factory = new NormalizingExcludeFactory()

    def "excludes simple group"() {
        def excludeSpec = factory.group("foo")

        expect:
        excludeSpec.excludesGroup("foo")
        !excludeSpec.excludesGroup("bar")
        !excludeSpec.excludesModule("bar")
        !excludeSpec.excludesArtifact(new DefaultIvyArtifactName("a", "b", "c"))
        excludeSpec.excludes(DefaultModuleIdentifier.newId("foo", "bar"))
    }

    def "excludes simple module"() {
        def excludeSpec = factory.module("bar")

        expect:
        !excludeSpec.excludesGroup("foo")
        excludeSpec.excludesModule("bar")
        !excludeSpec.excludesModule("foo")
        !excludeSpec.excludesArtifact(new DefaultIvyArtifactName("a", "b", "c"))
        excludeSpec.excludes(DefaultModuleIdentifier.newId("foo", "bar"))
    }

    def "excludes simple group and module"() {
        def excludeSpec = factory.groupAndModule("foo", "bar")

        expect:
        excludeSpec.excludesGroup("foo")
        excludeSpec.excludesModule("bar")
        !excludeSpec.excludesModule("foo")
        !excludeSpec.excludesGroup("bar")
        !excludeSpec.excludesArtifact(new DefaultIvyArtifactName("a", "b", "c"))
        excludeSpec.excludes(DefaultModuleIdentifier.newId("foo", "bar"))
        !excludeSpec.excludes(DefaultModuleIdentifier.newId("foo", "baz"))
    }

    @Unroll("#left ∪ #right = #expected")
    def "union"() {
        expect:
        factory.anyOf(left, right) == expected

        and: "union is commutative"
        factory.anyOf(right, left) == expected

        where:
        left                               | right                | expected
        everything()                       | nothing()            | everything()
        everything()                       | everything()         | everything()
        nothing()                          | nothing()            | nothing()
        everything()                       | group("foo")         | everything()
        nothing()                          | group("foo")         | group("foo")
        group("foo")                       | group("bar")         | anyOf(group("foo"), group("bar"))
        group("foo")                       | module("bar")        | anyOf(group("foo"), module("bar"))
        group("foo")                       | module("foo", "bar") | group("foo")
        anyOf(group("foo"), group("bar"))  | group("foo")         | anyOf(group("foo"), group("bar"))
        anyOf(group("foo"), module("bar")) | module("bar")        | anyOf(module("bar"), group("foo"))
        allOf(group("foo"), module("bar")) | group("foo")         | group("foo")
    }

    @Unroll("#left ∩ #right = #expected")
    def "intersection"() {
        expect:
        factory.allOf(left, right) == expected

        and: "intersection is commutative"
        factory.allOf(right, left) == expected

        where:
        left                               | right         | expected
        everything()                       | nothing()     | nothing()
        everything()                       | everything()  | everything()
        nothing()                          | nothing()     | nothing()
        everything()                       | group("foo")  | group("foo")
        nothing()                          | group("foo")  | nothing()
        group("foo")                       | group("bar")  | nothing()
        group("foo")                       | group("foo")  | group("foo")
        group("foo")                       | module("bar") | module("foo", "bar")
        module("foo", "bar")               | group("foo")  | module("foo", "bar")
        module("foo", "bar")               | group("bar")  | nothing()
        anyOf(group("foo"), module("bar")) | group("foo")  | group("foo")
        allOf(group("foo"), group("foo2")) | module("bar") | allOf(group("foo2"), group("foo"), module("bar"))
    }

    private ExcludeSpec nothing() {
        ExcludeNothing.get()
    }

    private ExcludeSpec everything() {
        ExcludeEverything.get()
    }

    private ExcludeSpec group(String group) {
        DefaultExclude.group(group)
    }

    private ExcludeSpec module(String group) {
        DefaultExclude.module(group)
    }

    private ExcludeSpec module(String group, String name) {
        DefaultExclude.module(group, name)
    }

    private ExcludeSpec anyOf(ExcludeSpec... specs) {
        ExcludeAnyOf.of(specs)
    }

    private ExcludeSpec allOf(ExcludeSpec... specs) {
        ExcludeAllOf.of(specs)
    }
}

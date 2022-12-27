//
// MIT License
//
// Copyright (c) 2021 Vaishnav Anil
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.slimjar.func

// /**
// * Adds `slim` configuration for Kotlin dsl with options
// */
// public fun DependencyHandler.slim(
//    dependencyNotation: String,
//    dependencyOptions: Action<ExternalModuleDependency> = Action { }
// ): ExternalModuleDependency? {
//    return withOptions(SLIM_CONFIGURATION_NAME.get(), dependencyNotation, dependencyOptions)
// }
//
// /**
// * Adds `slimApi` configuration for Kotlin dsl with options
// */
// public fun DependencyHandler.slimApi(
//    dependencyNotation: String,
//    dependencyOptions: Action<ExternalModuleDependency> = Action {}
// ): ExternalModuleDependency? {
//    return withOptions(SLIM_API_CONFIGURATION_NAME.get(), dependencyNotation, dependencyOptions)
// }
//
// /**
// * Alternative for adding `slim` configuration for Kotlin dsl but without options
// */
// public fun DependencyHandler.slim(dependencyNotation: Any): Dependency? = add(SLIM_CONFIGURATION_NAME, dependencyNotation)
//
// /**
// * Alternative for adding `slimApi` configuration for Kotlin dsl but without options
// */
// public fun DependencyHandler.slimApi(dependencyNotation: Any): Dependency? =
//    add(SLIM_API_CONFIGURATION_NAME, dependencyNotation)
//
// /**
// * Creates a configuration with options
// */
// private fun DependencyHandler.withOptions(
//    configuration: String,
//    dependencyNotation: String,
//    dependencyConfiguration: Action<ExternalModuleDependency>
// ): ExternalModuleDependency? = run {
//    uncheckedCast<ExternalModuleDependency>(create(dependencyNotation)).also { dependency ->
//        if (dependency == null) return@run null
//        dependencyConfiguration.execute(dependency)
//        add(configuration, dependency)
//    }
// }

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

package io.github.slimjar.injector.loader;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class IsolatedInjectableClassLoader extends InjectableClassLoader {
    @NotNull private final Map<String, Class<?>> delegatesMap = new HashMap<>();

    public IsolatedInjectableClassLoader() {
        this(new URL[0]);
    }

    public IsolatedInjectableClassLoader(@NotNull final URL... urls) {
        this(urls, Collections.emptySet());
    }

    public IsolatedInjectableClassLoader(
        @NotNull final URL[] urls,
        @NotNull final Collection<Class<?>> delegates
    ) { this(urls, delegates, getSystemClassLoader().getParent()); }

    public IsolatedInjectableClassLoader(
        @NotNull final URL[] urls,
        @NotNull final Collection<Class<?>> delegates,
        @NotNull final ClassLoader parent
    ) {
        super(urls, parent);
        delegates.forEach(delegate -> delegatesMap.put(delegate.getName(), delegate));
    }

    @Override
    protected Class<?> loadClass(
        @NotNull String name,
        boolean resolve
    ) throws ClassNotFoundException {
        // Check if class was already loaded
        final var loaded = findLoadedClass(name);
        if (loaded != null) {
            return loaded;
        }

        // Check if its any of provided classes
        final var delegate = delegatesMap.get(name);
        if (delegate != null) {
            return delegate;
        }

        // Load from 'URLClassPath's
        return super.loadClass(name, resolve);
    }

}

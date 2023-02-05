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

import io.github.slimjar.exceptions.InjectorException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public final class WrappedInjectableClassLoader implements Injectable {
    @NotNull private final URLClassLoader urlClassLoader;
    @NotNull private final Method addURLMethod;

    public WrappedInjectableClassLoader(@NotNull final URLClassLoader urlClassLoader) throws InjectorException {
        this.urlClassLoader = urlClassLoader;
        try {
            this.addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (final NoSuchMethodException err) {
            throw new InjectorException("Unable to find addURL method", err);
        }
    }

    @Override
    public void inject(@NotNull final URL url) throws InjectorException {
        addURLMethod.setAccessible(true);
        try {
            addURLMethod.invoke(urlClassLoader, url);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Shouldn't be possible.
            throw new InjectorException("Unable to invoke addURL method", e);
        }
    }
}

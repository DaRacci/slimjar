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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayDeque;
import java.util.ArrayList;

public final class UnsafeInjectable implements Injectable {
    @NotNull private final ArrayDeque<URL> unopenedURLs;
    @NotNull private final ArrayList<URL> pathURLs;

    public UnsafeInjectable(
        @NotNull final ArrayDeque<URL> unopenedURLs,
        @NotNull final ArrayList<URL> pathURLs
    ) {
        this.unopenedURLs = unopenedURLs;
        this.pathURLs = pathURLs;
    }

    @Override
    public void inject(@NotNull final URL url) throws InjectorException {
        unopenedURLs.addLast(url);
        pathURLs.add(url);
    }

    @Contract("_ -> new")
    public static @NotNull Injectable create(@NotNull final URLClassLoader classLoader) throws InjectorException {
        final var unsafe = Unsafe.getUnsafe();
        final ArrayDeque<URL> unopenedURLs;
        final ArrayList<URL> pathURLs;
        try {
            final Object ucp = fetchField(unsafe, URLClassLoader.class, classLoader, "ucp");
            unopenedURLs = (ArrayDeque<URL>) fetchField(unsafe, ucp, "unopenedUrls");
            pathURLs = (ArrayList<URL>) fetchField(unsafe, ucp, "path");
        } catch (final NoSuchFieldException err) {
            throw new InjectorException("Unable to fetch fields.", err);
        }

        return new UnsafeInjectable(unopenedURLs, pathURLs);
    }

    private static Object fetchField(
        final Unsafe unsafe,
        final Object object,
        final String name
    ) throws NoSuchFieldException {
        return fetchField(unsafe, object.getClass(), object, name);
    }

    private static @NotNull Object fetchField(
        @NotNull final Unsafe unsafe,
        @NotNull final Class<?> clazz,
        @NotNull final Object object,
        @NotNull final String name
    ) throws NoSuchFieldException {
        final Field field = clazz.getDeclaredField(name);
        final long offset = unsafe.objectFieldOffset(field);
        return unsafe.getObject(object, offset);
    }
}

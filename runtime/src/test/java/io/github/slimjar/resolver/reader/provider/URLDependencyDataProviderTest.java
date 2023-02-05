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

package io.github.slimjar.resolver.reader.provider;

import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.mirrors.SimpleMirrorSelector;
import io.github.slimjar.resolver.reader.MockDependencyData;
import io.github.slimjar.resolver.reader.dependency.DependencyDataProvider;
import io.github.slimjar.resolver.reader.dependency.DependencyReader;
import io.github.slimjar.resolver.reader.dependency.GsonDependencyReader;
import io.github.slimjar.resolver.reader.dependency.URLDependencyDataProvider;
import io.github.slimjar.resolver.reader.facade.ReflectiveGsonFacadeFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class URLDependencyDataProviderTest {
    private static final Path DEFAULT_DOWNLOAD_DIRECTORY;
    private static final Collection<Repository> CENTRAL_MIRRORS;

    static {
        CENTRAL_MIRRORS = Collections.singleton(Repository.central());
        final String userHome = System.getProperty("user.home");
        final String defaultPath = String.format("%s/.slimjar", userHome);
        final File file = new File(defaultPath);
        DEFAULT_DOWNLOAD_DIRECTORY = file.toPath();
    }

    @Test
    public void testFileDependencyDataProvider() throws Exception {
        final var mockDependencyData = new MockDependencyData();
        final var mockURL = Mockito.mock(URL.class);
        final var mockConnection = Mockito.mock(URLConnection.class);

        Mockito.when(mockURL.openConnection()).thenReturn(mockConnection);
        Mockito.when(mockConnection.getInputStream()).thenReturn(mockDependencyData.getDependencyDataInputStream());

        final DependencyDataProvider dependencyDataProvider = new URLDependencyDataProvider(new GsonDependencyReader(ReflectiveGsonFacadeFactory.create(DEFAULT_DOWNLOAD_DIRECTORY, CENTRAL_MIRRORS).createFacade()), mockURL);
        Assertions.assertEquals(mockDependencyData.getExpectedSample(), dependencyDataProvider.get(), "Read and provide proper dependencies");
    }

    @Test
    public void testFileDependencyDataProviderReturnReader() throws Exception {
        final var mockDependencyData = new MockDependencyData();
        final var mockURL = Mockito.mock(URL.class);

        Mockito.when(mockURL.openStream()).thenReturn(mockDependencyData.getDependencyDataInputStream());

        final DependencyReader dependencyReader = new GsonDependencyReader(ReflectiveGsonFacadeFactory.create(DEFAULT_DOWNLOAD_DIRECTORY, CENTRAL_MIRRORS).createFacade());
        final URLDependencyDataProvider dependencyDataProvider = new URLDependencyDataProvider(dependencyReader, mockURL);
        Assertions.assertEquals(dependencyReader, dependencyDataProvider.getDependencyReader(), "Provider must use given reader");
    }

    @Test
    public void testFileDependencyDataProviderOnUrlException() throws Exception {
        final var mockURL = Mockito.mock(URL.class);
        final var mockReader = Mockito.mock(DependencyReader.class);
        final var mockConnection = Mockito.mock(URLConnection.class);
        final Exception expectedException = new IOException();

        Mockito.when(mockURL.openConnection()).thenReturn(mockConnection);
        Mockito.when(mockConnection.getInputStream()).thenThrow(expectedException);

        Exception exception = null;
        try {
            new URLDependencyDataProvider(mockReader, mockURL).get();
        } catch (final Exception ex) {
            exception = ex;
        }
        Assertions.assertSame(expectedException, exception, "Provider must elevate exception on url issue");
    }
}

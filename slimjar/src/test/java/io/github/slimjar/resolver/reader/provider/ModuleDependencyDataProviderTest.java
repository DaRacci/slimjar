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

import io.github.slimjar.resolver.data.DependencyData;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.mirrors.SimpleMirrorSelector;
import io.github.slimjar.resolver.reader.dependency.DependencyDataProvider;
import io.github.slimjar.resolver.reader.dependency.GsonDependencyReader;
import io.github.slimjar.resolver.reader.MockDependencyData;
import io.github.slimjar.resolver.reader.dependency.ModuleDependencyDataProvider;
import io.github.slimjar.resolver.reader.facade.ReflectiveGsonFacadeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class ModuleDependencyDataProviderTest {
    private static final Path DEFAULT_DOWNLOAD_DIRECTORY;
    private static final Collection<Repository> CENTRAL_MIRRORS;

    static {
        CENTRAL_MIRRORS = Collections.singleton(Repository.central());
        final String userHome = System.getProperty("user.home");
        final String defaultPath = String.format("%s/.slimjar", userHome);
        DEFAULT_DOWNLOAD_DIRECTORY = new File(defaultPath).toPath();
    }

    @Test
    public void testModuleDependencyDataProviderNonEmpty() throws Exception {
        final var mockDependencyData = new MockDependencyData();
        final var mockURL = Mockito.mock(URL.class);
        final var zipEntry = Mockito.mock(ZipEntry.class);
        final var inputStream = mockDependencyData.getDependencyDataInputStream();

        final var jarURLConnection = createJarConnection(zipEntry, inputStream);
        final var mockProvider = createProvider(mockURL, jarURLConnection);

        Assertions.assertEquals(mockDependencyData.getExpectedSample(), mockProvider.get(), "Read and provide proper dependencies");
    }

    @Test
    public void testModuleDependencyDataProviderEmpty() throws Exception {
        final var mockUrl = Mockito.mock(URL.class);
        final var jarURLConnection = createJarConnection(null, null);
        final var emptyDependency = new DependencyData(
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet()
        );

        final DependencyDataProvider dependencyDataProvider = createProvider(mockUrl, jarURLConnection);
        Assertions.assertEquals(emptyDependency, dependencyDataProvider.get(), "Empty dependency if not exists");
    }

    @Test
    public void testModuleDependencyDataProviderExceptionIfNonJar() throws Exception {
        final var mockUrl = Mockito.mock(URL.class);
        final var urlConnection = Mockito.mock(HttpURLConnection.class);
        final DependencyDataProvider dependencyDataProvider = createProvider(mockUrl, urlConnection);

        Mockito.doReturn(urlConnection).when(mockUrl).openConnection();

        Error error = null;
        try {
            dependencyDataProvider.get();
        } catch (Error thrown) {
            error = thrown;
        }
        Assertions.assertTrue(error instanceof AssertionError, "Non-Jar urlcorrection should throw AssertionError");

    }

    private ModuleDependencyDataProvider createProvider(
        final URL url,
        final Object jarURLConnection
    ) throws IOException, ReflectiveOperationException, NoSuchAlgorithmException, URISyntaxException, InterruptedException {
        final var mockProvider = Mockito.mock(ModuleDependencyDataProvider.class, Mockito.withSettings().useConstructor(new GsonDependencyReader(ReflectiveGsonFacadeFactory.create(DEFAULT_DOWNLOAD_DIRECTORY, CENTRAL_MIRRORS).createFacade()), url));

        Mockito.doReturn(url).when(mockProvider).getURL();
        Mockito.doCallRealMethod().when(mockProvider).get();
        Mockito.doReturn(jarURLConnection).when(url).openConnection();

        return mockProvider;
    }

    private JarURLConnection createJarConnection(
        final ZipEntry zipEntry,
        final InputStream inputStream
    ) throws IOException {
        final var jarURLConnection = Mockito.mock(JarURLConnection.class);
        final var jarFile = Mockito.mock(JarFile.class);

        Mockito.doReturn(jarFile).when(jarURLConnection).getJarFile();
        Mockito.doReturn(zipEntry).when(jarFile).getEntry("slimjar.json");
        Mockito.doReturn(inputStream).when(jarFile).getInputStream(zipEntry);

        return jarURLConnection;
    }
}

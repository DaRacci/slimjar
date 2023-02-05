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

package io.github.slimjar.resolver.enquirer;

import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.pinger.URLPinger;
import io.github.slimjar.resolver.strategy.MavenChecksumPathResolutionStrategy;
import io.github.slimjar.resolver.strategy.PathResolutionStrategy;
import java.net.URL;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RepositoryEnquirerTest {

    @AfterEach
    public void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    public void testPingingEnquirerProvideValidURL() {
        final var pinger = Mockito.mock(URLPinger.class, defaultAnswer -> true);
        final var resolutionStrategy = Mockito.mock(PathResolutionStrategy.class, defaultAnswer -> Collections.singleton("https://a.b.c/repo/dep.jar"));

        final RepositoryEnquirer repositoryEnquirer = new PingingRepositoryEnquirer(
            new Repository(Mockito.mock(URL.class)),
            resolutionStrategy,
            new MavenChecksumPathResolutionStrategy("SHA-256", resolutionStrategy),
            resolutionStrategy,
            pinger
        );

        Assertions.assertNotNull(repositoryEnquirer.enquire(new Dependency("a.b.c","d","", null, Collections.emptySet())), "Valid repo & dep should return non-null URL");
    }

    @Test
    public void testPingingEnquirerProvideInvalidURL() {
        final var mockURL = Mockito.mock(URL.class);
        final var mockPinger = Mockito.mock(URLPinger.class);
        final var resolutionStrategy = Mockito.mock(PathResolutionStrategy.class);

        Mockito.doReturn(Collections.singleton("https://a.b.c/repo/dep.jar"))
            .when(resolutionStrategy)
            .pathTo(null, null);

        Mockito.doReturn(false).when(mockPinger).ping(mockURL);

        final RepositoryEnquirer repositoryEnquirer = new PingingRepositoryEnquirer(null, resolutionStrategy, resolutionStrategy, resolutionStrategy, mockPinger);
        Assertions.assertNull(repositoryEnquirer.enquire(new Dependency("", "", "", null, Collections.emptySet())), "Invalid repo or dep should return null URL");

//        mockConst.close();
    }

    @Test
    public void testPingingEnquirerProvideMalformedURL() {
        final var mockURL = Mockito.mock(URL.class);
        final var mockPinger = Mockito.mock(URLPinger.class);
        final var resolutionStrategy = Mockito.mock(PathResolutionStrategy.class);
        final var repository = new Repository(mockURL);

        Mockito.doReturn(Collections.singleton("some_malformed_url")).when(resolutionStrategy).pathTo(repository, null);

        final RepositoryEnquirer repositoryEnquirer = new PingingRepositoryEnquirer(repository, resolutionStrategy, resolutionStrategy, resolutionStrategy, mockPinger);
        Assertions.assertNull(repositoryEnquirer.enquire(new Dependency("", "", "", null, Collections.emptySet())), "Malformed URL should return null URL");
    }
}

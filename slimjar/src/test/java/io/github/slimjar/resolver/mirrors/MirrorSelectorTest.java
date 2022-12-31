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

package io.github.slimjar.resolver.mirrors;

import io.github.slimjar.resolver.data.Mirror;
import io.github.slimjar.resolver.data.Repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MirrorSelectorTest {
    //    @Test
    //    public void testSelectorForceMirrorCentral() throws MalformedURLException {
    //        final Repository central = new Repository(new URL(Repository.CENTRAL_URL));
    //        final Repository centralMirror = new Repository(new URL(Repository.CENTRAL_URL));
    //        final Collection<Repository> original = Collections.singleton(central);
    //        final MirrorSelector mirrorSelector = new SimpleMirrorSelector();
    //        final Collection<Repository> selected = mirrorSelector.select(original, Collections.emptyList());
    //        Assertions.assertFalse(selected.contains(central), "Selection should remove central");
    //        Assertions.assertTrue(selected.contains(new Repository(new URL(Repository.CENTRAL_URL))), "Selection should contain central mirror");
    //    }
    //
    //    @Test
    //    public void testSelectorForceAltMirrorCentral() throws MalformedURLException {
    //        final Repository central = new Repository(new URL(Repository.CENTRAL_URL));
    //        final Repository centralMirror = new Repository(new URL(Repository.CENTRAL_URL));
    //
    //        final Collection<Repository> original = Collections.singleton(central);
    //        final MirrorSelector mirrorSelector = new SimpleMirrorSelector();
    //
    //        final Collection<Repository> selected = mirrorSelector.select(original, Collections.emptyList());
    //        Assertions.assertFalse(selected.contains(central), "Selection should remove central");
    //        Assertions.assertTrue(selected.contains(new Repository(new URL(Repository.CENTRAL_URL))), "Selection should contain central mirror");
    //    }

    @Test
    public void testSelectorReplace() throws MalformedURLException {
        final Repository central = Repository.central();
        final Collection<Repository> centralMirrors = Collections.singleton(central);
        final Repository originalRepo = new Repository(new URL("https://a.b.c"));
        final Repository mirroredRepo = new Repository(new URL("https://d.e.f"));
        final Mirror mirror = new Mirror(mirroredRepo.url(), originalRepo.url());
        final Collection<Repository> original = Collections.singleton(originalRepo);
        final Collection<Mirror> mirrors = Collections.singleton(mirror);
        final MirrorSelector mirrorSelector = new SimpleMirrorSelector();

        final Collection<Repository> selected = mirrorSelector.select(original, mirrors);
        Assertions.assertFalse(selected.contains(originalRepo), "Selection should remove original");
        Assertions.assertTrue(selected.contains(mirroredRepo), "Selection should contain mirror");
    }
}

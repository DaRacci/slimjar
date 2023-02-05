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

package io.github.slimjar.resolver.pinger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class URLPingerTest {

    @Test
    public void testHttpURLPingerHttp() throws IOException {
        final var mockURL = Mockito.mock(URL.class);
        final var httpURLConnection = Mockito.mock(HttpURLConnection.class);

        Mockito.when(mockURL.openConnection()).thenReturn(httpURLConnection);
        Mockito.when(mockURL.getProtocol()).thenReturn("HTTP");
        Mockito.doNothing().when(httpURLConnection).addRequestProperty("","");
        Mockito.doNothing().when(httpURLConnection).connect();
        Mockito.doReturn(HttpURLConnection.HTTP_OK).when(httpURLConnection).getResponseCode();

        final URLPinger urlPinger = new HttpURLPinger();
        boolean result = urlPinger.ping(mockURL);
        Assertions.assertTrue(result, "Valid http URL");
    }

    @Test
    public void testHttpURLPingerHttps() throws IOException {
        final var mockURL = Mockito.mock(URL.class);
        final var httpsURLConnection = Mockito.mock(HttpsURLConnection.class);

        Mockito.when(mockURL.openConnection()).thenReturn(httpsURLConnection);
        Mockito.when(mockURL.getProtocol()).thenReturn("HTTPS");
        Mockito.doNothing().when(httpsURLConnection).addRequestProperty("","");
        Mockito.doNothing().when(httpsURLConnection).connect();
        Mockito.doReturn(HttpURLConnection.HTTP_OK).when(httpsURLConnection).getResponseCode();

        final URLPinger urlPinger = new HttpURLPinger();
        boolean result = urlPinger.ping(mockURL);
        Assertions. assertTrue(result, "Valid https URL");
    }

    @Test
    public void testHttpURLPingerFailIfNotOk() throws IOException {
        final var mockURL = Mockito.mock(URL.class);
        final var httpsURLConnection = Mockito.mock(HttpsURLConnection.class);

        Mockito.when(mockURL.openConnection()).thenReturn(httpsURLConnection);
        Mockito.when(mockURL.getProtocol()).thenReturn("HTTPS");
        Mockito.doNothing().when(httpsURLConnection).addRequestProperty("","");
        Mockito.doNothing().when(httpsURLConnection).connect();
        Mockito.doReturn(HttpURLConnection.HTTP_BAD_REQUEST).when(httpsURLConnection).getResponseCode();

        final URLPinger urlPinger = new HttpURLPinger();
        boolean result = urlPinger.ping(mockURL);
        Assertions.assertFalse(result, "Non-OK should fail");
    }

    @Test
    public void testHttpURLPingerExceptionOnPing() throws IOException {
        final var mockUrl = Mockito.mock(URL.class);
        final var httpsURLConnection = Mockito.mock(HttpsURLConnection.class);

        Mockito.when(mockUrl.openConnection()).thenReturn(httpsURLConnection);
        Mockito.when(mockUrl.getProtocol()).thenReturn("HTTPS");
        Mockito.doNothing().when(httpsURLConnection).addRequestProperty("","");
        Mockito.doThrow(new IOException()).when(httpsURLConnection).connect();
        Mockito.doReturn(HttpURLConnection.HTTP_BAD_REQUEST).when(httpsURLConnection).getResponseCode();

        final URLPinger urlPinger = new HttpURLPinger();
        boolean result = urlPinger.ping(mockUrl);
        Assertions.assertFalse(result, "Exception should fail");
    }

    @Test
    public void testHttpURLPingerUnsupportedProtocol() {
        final var mockURL = Mockito.mock(URL.class);
        final var urlPinger = new HttpURLPinger();

        Mockito.doReturn("NON-EXISTENT-PROTOCOL").when(mockURL).getProtocol();
        boolean result = urlPinger.ping(mockURL);
        Assertions.assertFalse(result, "Non-OK should fail");
    }

}
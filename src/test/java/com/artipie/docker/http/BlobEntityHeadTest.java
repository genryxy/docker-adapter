/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.docker.http;

import com.artipie.docker.ExampleStorage;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.http.Response;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DockerSlice}.
 * Blob Head endpoint.
 *
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class BlobEntityHeadTest {

    /**
     * Slice being tested.
     */
    private DockerSlice slice;

    @BeforeEach
    void setUp() {
        this.slice = new DockerSlice("/base", new AstoDocker(new ExampleStorage()));
    }

    @Test
    void shouldFindLayer() {
        final String digest = String.format(
            "%s:%s",
            "sha256",
            "aad63a9339440e7c3e1fff2b988991b9bfb81280042fa7f39a5e327023056819"
        );
        final Response response = this.slice.response(
            new RequestLine(
                RqMethod.HEAD,
                String.format("/base/v2/test/blobs/%s", digest)
            ).toString(),
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            response,
            new ResponseMatcher(
                RsStatus.OK,
                new Header("Content-Length", "2803255"),
                new Header("Docker-Content-Digest", digest),
                new Header("Content-Type", "application/octet-stream")
            )
        );
    }

    @Test
    void shouldReturnNotFoundForUnknownDigest() {
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine(
                    RqMethod.HEAD,
                    String.format(
                        "/base/v2/test/blobs/%s",
                        "sha256:0123456789012345678901234567890123456789012345678901234567890123"
                    )
                ).toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new RsHasStatus(RsStatus.NOT_FOUND)
        );
    }
}

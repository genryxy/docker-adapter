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

import com.artipie.asto.Content;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.http.Response;
import com.artipie.http.auth.Permissions;
import com.artipie.http.hm.IsHeader;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DockerSlice}.
 * Upload PUT endpoint.
 *
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UploadEntityPostTest {

    /**
     * Slice being tested.
     */
    private DockerSlice slice;

    /**
     * User with right permissions.
     */
    private TestAuthentication.User user;

    @BeforeEach
    void setUp() {
        this.user = TestAuthentication.ALICE;
        this.slice = new DockerSlice(
            new AstoDocker(new InMemoryStorage()),
            new Permissions.Single(this.user.name(), "write"),
            new TestAuthentication()
        );
    }

    @Test
    void shouldReturnInitialUploadStatus() {
        final Response response = this.slice.response(
            new RequestLine(RqMethod.POST, "/v2/test/blobs/uploads/").toString(),
            this.user.headers(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            response,
            new ResponseMatcher(
                RsStatus.ACCEPTED,
                new IsHeader(
                    "Location",
                    new StringStartsWith(false, "/v2/test/blobs/uploads/")
                ),
                new IsHeader("Range", "0-0"),
                new IsHeader("Content-Length", "0"),
                new IsHeader("Docker-Upload-UUID", new IsNot<>(Matchers.emptyString()))
            )
        );
    }

    @Test
    void shouldReturnUnauthorizedWhenNoAuth() {
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine(RqMethod.POST, "/v2/test/blobs/uploads/").toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new IsUnauthorizedResponse()
        );
    }

    @Test
    void shouldReturnForbiddenWhenUserHasNoRequiredPermissions() {
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine(RqMethod.POST, "/v2/test/blobs/uploads/").toString(),
                TestAuthentication.BOB.headers(),
                Content.EMPTY
            ),
            new IsDeniedResponse()
        );
    }
}

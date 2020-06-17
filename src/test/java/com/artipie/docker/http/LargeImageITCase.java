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

import com.artipie.docker.junit.DockerClient;
import com.artipie.docker.junit.DockerClientSupport;
import com.artipie.docker.junit.DockerRepository;
import java.nio.file.Path;
import java.util.Objects;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Integration test for large file pushing scenario of {@link DockerSlice}.
 *
 * @since 0.3
*/
@DockerClientSupport
@DisabledOnOs(OS.WINDOWS)
public final class LargeImageITCase {
    /**
     * Docker image name.
     */
    private static final String IMAGE = "large-image";

    /**
     * Docker client.
     */
    private DockerClient client;

    /**
     * Docker repository.
     */
    private DockerRepository repository;

    @Test
    void largeImageUploadWorks() throws Exception {
        final Path dockerfile = Path.of(
            Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader()
                    .getResource("large-image/Dockerfile")
            ).toURI()
        );
        final String image = String.format("%s/%s", this.repository.url(), LargeImageITCase.IMAGE);
        try {
            this.client.run("build", dockerfile.getParent().toString(), "-t", image);
            final String output = this.client.run("push", image);
            MatcherAssert.assertThat(output, new StringContains(false, "Pushed"));
        } finally {
            this.client.run("rmi", image);
        }
    }
}

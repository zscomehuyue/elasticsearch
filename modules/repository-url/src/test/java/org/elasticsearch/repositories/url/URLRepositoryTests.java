/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.repositories.url;

import org.elasticsearch.cluster.metadata.RepositoryMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.TestEnvironment;
import org.elasticsearch.repositories.RepositoryException;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

public class URLRepositoryTests extends ESTestCase {

    public void testWhiteListingRepoURL() throws IOException {
        String repoPath = createTempDir().resolve("repository").toUri().toURL().toString();
        Settings baseSettings = Settings.builder()
            .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toString())
            .put(URLRepository.ALLOWED_URLS_SETTING.getKey(), repoPath)
            .put(URLRepository.REPOSITORIES_URL_SETTING.getKey(), repoPath)
            .build();
        RepositoryMetaData repositoryMetaData = new RepositoryMetaData("url", URLRepository.TYPE, baseSettings);
        new URLRepository(repositoryMetaData, TestEnvironment.newEnvironment(baseSettings),
            new NamedXContentRegistry(Collections.emptyList()));
    }

    public void testIfNotWhiteListedMustSetRepoURL() throws IOException {
        String repoPath = createTempDir().resolve("repository").toUri().toURL().toString();
        Settings baseSettings = Settings.builder()
            .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toString())
            .put(URLRepository.REPOSITORIES_URL_SETTING.getKey(), repoPath)
            .build();
        RepositoryMetaData repositoryMetaData = new RepositoryMetaData("url", URLRepository.TYPE, baseSettings);
        try {
            new URLRepository(repositoryMetaData, TestEnvironment.newEnvironment(baseSettings),
                new NamedXContentRegistry(Collections.emptyList()));
            fail("RepositoryException should have been thrown.");
        } catch (RepositoryException e) {
            String msg = "[url] file url [" + repoPath
                + "] doesn't match any of the locations specified by path.repo or repositories.url.allowed_urls";
            assertEquals(msg, e.getMessage());
        }
    }

    public void testMustBeSupportedProtocol() throws IOException {
        Path directory = createTempDir();
        String repoPath = directory.resolve("repository").toUri().toURL().toString();
        Settings baseSettings = Settings.builder()
            .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toString())
            .put(Environment.PATH_REPO_SETTING.getKey(), directory.toString())
            .put(URLRepository.REPOSITORIES_URL_SETTING.getKey(), repoPath)
            .put(URLRepository.SUPPORTED_PROTOCOLS_SETTING.getKey(), "http,https")
            .build();
        RepositoryMetaData repositoryMetaData = new RepositoryMetaData("url", URLRepository.TYPE, baseSettings);
        try {
            new URLRepository(repositoryMetaData, TestEnvironment.newEnvironment(baseSettings),
                new NamedXContentRegistry(Collections.emptyList()));
            fail("RepositoryException should have been thrown.");
        } catch (RepositoryException e) {
            assertEquals("[url] unsupported url protocol [file] from URL [" + repoPath +"]", e.getMessage());
        }
    }

}

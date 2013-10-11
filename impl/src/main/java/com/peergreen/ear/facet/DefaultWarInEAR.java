/**
 * Copyright 2013 Peergreen S.A.S.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.peergreen.ear.facet;

import java.net.URI;

import com.peergreen.ear.WarInEarDD;

public class DefaultWarInEAR implements WarInEarDD {

    @Override
    public URI getArtifactURI() {
        return artifactURI;
    }
    @Override
    public void setArtifactURI(URI artifactURI) {
        this.artifactURI = artifactURI;
    }
    private String webUri;
    private String contextRoot;
    private URI artifactURI;


    @Override
    public String getWebUri() {
        return webUri;
    }
    @Override
    public void setWebUri(String webUri) {
        this.webUri = webUri;
    }
    @Override
    public String getContextRoot() {
        return contextRoot;
    }
    @Override
    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }
}

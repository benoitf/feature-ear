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
package com.peergreen.ear.extension.war.processor;

import java.util.List;

import com.peergreen.deployment.Artifact;
import com.peergreen.deployment.DiscoveryPhasesLifecycle;
import com.peergreen.deployment.ProcessorContext;
import com.peergreen.deployment.ProcessorException;
import com.peergreen.deployment.model.ArtifactModel;
import com.peergreen.deployment.model.Wire;
import com.peergreen.deployment.model.WireScope;
import com.peergreen.deployment.processor.Discovery;
import com.peergreen.deployment.processor.Processor;
import com.peergreen.ear.Ear;
import com.peergreen.ear.WarInEarDD;
import com.peergreen.webcontainer.WebApplication;

/**
 * Change context root if the war is part of an EAR
 * @author Florent Benoit
 */
@Processor
@Discovery(DiscoveryPhasesLifecycle.FACET_CONFLICTS)
public class EarWebApplication implements com.peergreen.deployment.Processor<WebApplication> {

    @Override
    public void handle(WebApplication webApplication, ProcessorContext processorContext) throws ProcessorException {
        // Ear case ?
        // Update the context root
        Iterable<? extends Wire> wires = processorContext.getArtifactModel().getWires(WireScope.TO);
        for (Wire wire : wires) {
            ArtifactModel from = wire.getFrom();
            Artifact earArtifact = from.getArtifact();
            Ear ear = earArtifact.as(Ear.class);
            List<WarInEarDD> wars = ear.getDD().getWars();
            if (wars != null) {
                for (WarInEarDD war : wars) {
                    // Check
                    if (processorContext.getArtifact().uri().equals(war.getArtifactURI())) {
                        // Found a matching URI, sets the context
                        webApplication.setContextPath(war.getContextRoot());
                    }
                }
            }
        }
    }

}

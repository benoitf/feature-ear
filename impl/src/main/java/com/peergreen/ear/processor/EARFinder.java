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
package com.peergreen.ear.processor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;

import org.ow2.util.file.FileUtils;
import org.ow2.util.file.FileUtilsException;
import org.ow2.util.xml.DocumentParser;
import org.ow2.util.xml.DocumentParserException;
import org.ow2.util.xml.EmptyEntityResolver;
import org.ow2.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.peergreen.deployment.Artifact;
import com.peergreen.deployment.DiscoveryPhasesLifecycle;
import com.peergreen.deployment.ProcessorContext;
import com.peergreen.deployment.ProcessorException;
import com.peergreen.deployment.facet.archive.Archive;
import com.peergreen.deployment.facet.archive.ArchiveException;
import com.peergreen.deployment.processor.Discovery;
import com.peergreen.deployment.processor.Processor;
import com.peergreen.deployment.processor.Uri;
import com.peergreen.ear.Ear;
import com.peergreen.ear.WarInEarDD;
import com.peergreen.ear.facet.DefaultEar;
import com.peergreen.ear.facet.DefaultWarInEAR;

/**
 * Adds the EAR facet
 * @author Florent Benoit
 */
@Processor
@Discovery(DiscoveryPhasesLifecycle.FACET_SCANNER)
@Uri(extension="ear")
public class EARFinder implements com.peergreen.deployment.Processor<Archive> {

    /**
     * Accepts EAR archive
     */
    @Override
    public void handle(Archive archive, ProcessorContext processorContext) throws ProcessorException {

        // Analyze the content of the EAR
        URI applicationXMLentry;
        try {
            applicationXMLentry = archive.getResource("META-INF/application.xml");
        } catch (ArchiveException e) {
            throw new ProcessorException("Unable to get application.xml entry", e);
        }

        DefaultEar ear = new DefaultEar();


        // Parse the content
        if (applicationXMLentry != null) {
            try {
                URL applicationXMLURL = applicationXMLentry.toURL();
                // Get document
                Document document = null;
                try {
                    document = DocumentParser.getDocument(applicationXMLURL, false, new EmptyEntityResolver());
                } catch (DocumentParserException e) {
                    throw new ProcessorException("Cannot parse the url", e);
                }

                // Root element = <application>
                Element applicationRootElement = document.getDocumentElement();

                // Servlets
                NodeList webModulesList = applicationRootElement.getElementsByTagName("web");
                for (int i = 0; i < webModulesList.getLength(); i++) {
                    Element webModuleElement = (Element) webModulesList.item(i);

                    // Build instance of Servlet XML struct object
                    DefaultWarInEAR war = new DefaultWarInEAR();

                    String webUri = XMLUtils.getStringValueElement(webModuleElement, "web-uri");
                    String contextRoot = XMLUtils.getStringValueElement(webModuleElement, "context-root");
                    war.setWebUri(webUri);
                    war.setContextRoot(contextRoot);
                    ear.getDD().getWars().add(war);

                }
            } catch (IOException e) {
                throw new ProcessorException("Unable to open stream", e);
            }

            // unpack
            URI uri = processorContext.getArtifact().uri();

            // Path to the war file
            File path = new File(uri.getPath());
            File unpacked;

            // Needs to unpack war if not yet unpacked
            if (path.isFile()) {
                // unpack
                File f = new File(System.getProperty("java.io.tmpdir"), "ear-unpacked");
                unpacked = new File(f, new File(uri).getName());
                try {
                    FileUtils.unpack(new JarFile(new File(uri)), unpacked);
                } catch (FileUtilsException | IOException e) {
                    throw new ProcessorException("Unable to unpack the jar", e);
                }
            } else {
                unpacked = path;
            }

            processorContext.addFacet(Ear.class, ear);


            // web entry ?
            if (ear.getDD().getWars().size() > 0) {
                for (WarInEarDD war : ear.getDD().getWars()) {
                    URI warUri = new File(unpacked, war.getWebUri()).toURI();
                    war.setArtifactURI(warUri);
                    Artifact newArtifact = processorContext.build(war.getWebUri(), warUri);
                    // Adds the war
                    processorContext.addArtifact(newArtifact);
                }
            }

        }

    }



}

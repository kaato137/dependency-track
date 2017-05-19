/*
 * This file is part of Dependency-Track.
 *
 * Dependency-Track is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Dependency-Track is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dependency-Track. If not, see http://www.gnu.org/licenses/.
 */
package org.owasp.dependencytrack.search;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.owasp.dependencytrack.model.License;
import java.io.IOException;

public final class LicenseIndexer extends IndexManager implements ObjectIndexer<License> {

    private static final Logger LOGGER = Logger.getLogger(LicenseIndexer.class);

    protected LicenseIndexer() {
        super(IndexType.LICENSE);
    }

    @Override
    public String[] getSearchFields() {
        return IndexConstants.LICENSE_SEARCH_FIELDS;
    }

    /**
     * Adds a License object to a Lucene index.
     *
     * @param license A persisted License object.
     */
    public void add(License license) {
        final Document doc = new Document();
        addField(doc, IndexConstants.LICENSE_UUID, license.getUuid(), Field.Store.YES, false);
        addField(doc, IndexConstants.LICENSE_LICENSEID, license.getLicenseId(), Field.Store.YES, true);
        addField(doc, IndexConstants.LICENSE_NAME, license.getName(), Field.Store.YES, true);

        try {
            getIndexWriter().addDocument(doc);
            getIndexWriter().commit();
            close();
        } catch (IOException e) {
            LOGGER.error("Error adding object to index");
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Updates a License object in the Lucene index.
     *
     * @param license A persisted License object.
     */
    public synchronized void update(License license) {
        final Document doc = getDocument(IndexConstants.LICENSE_UUID, license.getUuid());
        if (doc == null) {
            LOGGER.warn("Could not find object in index. Adding.");
            add(license);
            return;
        }

        updateField(doc, IndexConstants.LICENSE_UUID, license.getUuid());
        updateField(doc, IndexConstants.LICENSE_LICENSEID, license.getLicenseId());
        updateField(doc, IndexConstants.LICENSE_NAME, license.getName());

        try {
            getIndexWriter().updateDocument(new Term(IndexConstants.LICENSE_UUID, license.getUuid()), doc);
            getIndexWriter().commit();
            close();
        } catch (IOException e) {
            LOGGER.error("Error updating object in index");
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Deletes a License object from the Lucene index.
     *
     * @param license A persisted License object.
     */
    public synchronized void remove(License license) {
        try {
            getIndexWriter().deleteDocuments(new Term(IndexConstants.LICENSE_UUID, license.getUuid()));
            getIndexWriter().commit();
            close();
        } catch (IOException e) {
            LOGGER.error("Error removing object from index");
            LOGGER.error(e.getMessage());
        }
    }

}
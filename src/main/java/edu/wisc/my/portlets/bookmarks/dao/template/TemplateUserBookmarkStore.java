/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.wisc.my.portlets.bookmarks.dao.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wisc.my.portlets.bookmarks.dao.BookmarkStore;
import edu.wisc.my.portlets.bookmarks.domain.BookmarkSet;
import edu.wisc.my.portlets.bookmarks.domain.Entry;
import edu.wisc.my.portlets.bookmarks.domain.support.FolderUtils;

/**
 * Creates new BookmarkSet objects based on bookmarks for a template user,
 * passes all other operations to an enclosed BookmarkStore instance.
 *
 * @author Drew Wills <a href="mailto:drew@unicon.net">drew@unicon.net</a>
 * @version $Revision: 12173 $
 */
public class TemplateUserBookmarkStore implements BookmarkStore {

	// Instance Members.
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final BookmarkStore enclosed;
	private final TemplateBookmarkSetResolver resolver;

	/*
	 * Public API.
	 */

	/**
	 * <p>Constructor for TemplateUserBookmarkStore.</p>
	 *
	 * @param enclosed a {@link edu.wisc.my.portlets.bookmarks.dao.BookmarkStore} object.
	 * @param resolver a {@link edu.wisc.my.portlets.bookmarks.dao.template.TemplateBookmarkSetResolver} object.
	 */
	public TemplateUserBookmarkStore(BookmarkStore enclosed, TemplateBookmarkSetResolver resolver) {

		// Assertions.
		if (enclosed == null) {
			String msg = "Argument 'enclosed' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (resolver == null) {
			String msg = "Argument 'resolver' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		// Instance Members.
		this.enclosed = enclosed;
		this.resolver = resolver;

	}

    /** {@inheritDoc} */
    public BookmarkSet getBookmarkSet(String owner, String name) {

    	BookmarkSet rslt = enclosed.getBookmarkSet(owner, name);
    	BookmarkSet template = resolver.getTemplateBookmarkSet(owner, name, enclosed);
    	if (rslt == null && template != null && template.getChildren().size() > 0) {
    		// Give the user a set of bookmarks if...
    		//  1. they don't already have one; and
    		//  2. there's something to give them
    		rslt = this.createBookmarkSet(owner, name);
    	}

        return rslt;

    }

    /** {@inheritDoc} */
    public void storeBookmarkSet(BookmarkSet bookmarkSet) {
    	enclosed.storeBookmarkSet(bookmarkSet);
    }

    /** {@inheritDoc} */
    public void removeBookmarkSet(String owner, String name) {
    	enclosed.removeBookmarkSet(owner, name);
    }


    /** {@inheritDoc} */
    public BookmarkSet createBookmarkSet(String owner, String name) {

    	BookmarkSet rslt = enclosed.createBookmarkSet(owner, name);

    	BookmarkSet template = resolver.getTemplateBookmarkSet(owner, name, enclosed);
    	if (template != null && template.getChildren().size() > 0) {
    		for (Entry y : template.getChildren().values()) {
    			Entry newEntry = FolderUtils.deepClone(y, false);
    			rslt.getChildren().put((long) newEntry.hashCode(), newEntry);
    		}
    		logger.info("TemplateUserBookmarkStore created a new BookmarkSet for user '"
    				+ owner + "' with " + rslt.getChildren().size() + " bookmarks.");
    	}
    	enclosed.storeBookmarkSet(rslt);

        return rslt;

    }

}

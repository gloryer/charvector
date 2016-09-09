/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.jcr2spi;

import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManager;
import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyEntry;
import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
import org.apache.jackrabbit.jcr2spi.state.ItemState;
import org.apache.jackrabbit.jcr2spi.state.NodeState;
import org.apache.jackrabbit.jcr2spi.state.PropertyState;
import org.apache.jackrabbit.jcr2spi.state.ItemStateCreationListener;
import org.apache.jackrabbit.jcr2spi.util.Dumpable;
import org.apache.jackrabbit.jcr2spi.util.LogUtil;
import org.apache.jackrabbit.jcr2spi.version.VersionHistoryImpl;
import org.apache.jackrabbit.jcr2spi.version.VersionImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.name.NameConstants;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Item;
import javax.jcr.Workspace;
import java.io.PrintStream;
import java.util.Iterator;

/**
 * <code>ItemManagerImpl</code> implements the <code>ItemManager</code> interface.
 */
public class ItemManagerImpl implements Dumpable, ItemManager, ItemStateCreationListener {

    private static Logger log = LoggerFactory.getLogger(ItemManagerImpl.class);

    private final SessionImpl session;

    private final HierarchyManager hierMgr;

    /**
     * A cache for item instances created by this <code>ItemManagerImpl</code>.
     *
     * The <code>ItemState</code>s act as keys for the map. In contrast to
     * o.a.j.core the item state are copied to transient space for reading and
     * will therefor not change upon transient modifications.
     */
    private final ItemCache itemCache;

    /**
     * Creates a new per-session instance <code>ItemManagerImpl</code> instance.
     *
     * @param hierMgr HierarchyManager associated with the new instance
     * @param session the session associated with the new instance
     * @param cache the ItemCache to be used.
     */
    ItemManagerImpl(HierarchyManager hierMgr, SessionImpl session, ItemCache cache) {
        this.hierMgr = hierMgr;
        this.session = session;
        itemCache = cache;

        // start listening to creation of ItemStates upon batch-reading in the
        // workspace item state factory.
        Workspace wsp = session.getWorkspace();
        if (wsp instanceof WorkspaceImpl) {
            ((WorkspaceImpl) wsp).getItemStateFactory().addCreationListener(this);
        }
    }

    //--------------------------------------------------------< ItemManager >---
    /**
     * @see ItemManager#dispose()
     */
    public void dispose() {
        // stop listening
        Workspace wsp = session.getWorkspace();
        if (wsp instanceof WorkspaceImpl) {
            ((WorkspaceImpl) wsp).getItemStateFactory().removeCreationListener(this);
        }
        // aftwards clear the cache.
        itemCache.clear();
    }

    /**
     * @see ItemManager#itemExists(Path)
     */
    public boolean itemExists(Path path) {
        try {
            // session-sanity & permissions are checked upon itemExists(ItemState)
            ItemState itemState = hierMgr.getItemState(path);
            return itemExists(itemState);
        } catch (PathNotFoundException pnfe) {
            return false;
        } catch (ItemNotFoundException infe) {
            return false;
        } catch (RepositoryException re) {
            return false;
        }
    }

    /**
     * @see ItemManager#itemExists(HierarchyEntry)
     */
    public boolean itemExists(HierarchyEntry hierarchyEntry) {
        try {
            // session-sanity & permissions are checked upon itemExists(ItemState)
            ItemState state = hierarchyEntry.getItemState();
            return itemExists(state);
        } catch (ItemNotFoundException e) {
            return false;
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     *
     * @param itemState
     * @return
     */
    private boolean itemExists(ItemState itemState) {
        try {
            // check sanity of session
            session.checkIsAlive();
            // return true, if ItemState is valid. Access rights are granted,
            // otherwise the state would not have been retrieved.
            return itemState.isValid();
        } catch (ItemNotFoundException infe) {
            return false;
        } catch (RepositoryException re) {
            return false;
        }
    }

    /**
     * @see ItemManager#getItem(Path)
     */
    public synchronized Item getItem(Path path)
            throws PathNotFoundException, AccessDeniedException, RepositoryException {
        HierarchyEntry itemEntry = hierMgr.getHierarchyEntry(path);
        try {
            return getItem(itemEntry);
        } catch (ItemNotFoundException infe) {
            throw new PathNotFoundException(LogUtil.safeGetJCRPath(path, session.getPathResolver()));
        }
    }

    /**
     * @see ItemManager#getItem(HierarchyEntry)
     */
    public Item getItem(HierarchyEntry hierarchyEntry) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        ItemState itemState = hierarchyEntry.getItemState();
        return getItem(itemState);
    }

    /**
     *
     * @param itemState
     * @return
     * @throws ItemNotFoundException
     * @throws AccessDeniedException
     * @throws RepositoryException
     */
    private Item getItem(ItemState itemState) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        session.checkIsAlive();
        if (!itemState.isValid()) {
            throw new ItemNotFoundException();
        }

        // first try to access item from cache
        Item item = itemCache.getItem(itemState);
        // not yet in cache, need to create instance
        if (item == null) {
            // create instance of item
            if (itemState.isNode()) {
                item = createNodeInstance((NodeState) itemState);
            } else {
                item = createPropertyInstance((PropertyState) itemState);
            }
        }
        return item;
    }

    /**
     * @see ItemManager#hasChildNodes(NodeEntry)
     */
    public synchronized boolean hasChildNodes(NodeEntry parentEntry)
            throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        // check sanity of session
        session.checkIsAlive();

        Iterator iter = parentEntry.getNodeEntries();
        while (iter.hasNext()) {
            try {
                // check read access by accessing the nodeState (implicit validation check)
                NodeEntry entry = (NodeEntry) iter.next();
                entry.getNodeState();
                return true;
            } catch (ItemNotFoundException e) {
                // should not occur. ignore
                log.debug("Failed to access node state.", e);
            }
        }
        return false;
    }

    /**
     * @see ItemManager#getChildNodes(NodeEntry)
     */
    public synchronized NodeIterator getChildNodes(NodeEntry parentEntry)
            throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        // check sanity of session
        session.checkIsAlive();

        Iterator it = parentEntry.getNodeEntries();
        return new LazyItemIterator(this, it);
    }

    /**
     * @see ItemManager#hasChildProperties(NodeEntry)
     */
    public synchronized boolean hasChildProperties(NodeEntry parentEntry)
            throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        // check sanity of session
        session.checkIsAlive();

        Iterator iter = parentEntry.getPropertyEntries();
        while (iter.hasNext()) {
            try {
                PropertyEntry entry = (PropertyEntry) iter.next();
                // check read access by accessing the propState (also implicit validation).
                entry.getPropertyState();
                return true;
            } catch (ItemNotFoundException e) {
                // should not occur. ignore
                log.debug("Failed to access node state.", e);
            }
        }
        return false;
    }

    /**
     * @see ItemManager#getChildProperties(NodeEntry)
     */
    public synchronized PropertyIterator getChildProperties(NodeEntry parentEntry)
            throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        // check sanity of session
        session.checkIsAlive();

        Iterator propEntries = parentEntry.getPropertyEntries();
        return new LazyItemIterator(this, propEntries);
    }

    //-----------------------------------------------------------< Dumpable >---
    /**
     * @see Dumpable#dump(PrintStream)
     */
    public void dump(PrintStream ps) {
        ps.println("ItemManager (" + this + ")");
        ps.println();
        ps.println("Items in cache:");
        ps.println();
        if (itemCache instanceof Dumpable) {
            ((Dumpable) itemCache).dump(ps);
        } else {
            ps.println("ItemCache (" + itemCache.toString() + ")");
        }
    }

    //----------------------------------------------------< private methods >---
    /**
     * @param state
     * @return a new <code>Node</code> instance.
     * @throws RepositoryException
     */
    private NodeImpl createNodeInstance(NodeState state) throws RepositoryException {
        // we want to be informed on life cycle changes of the new node object
        // in order to maintain item cache consistency
        ItemLifeCycleListener[] listeners = new ItemLifeCycleListener[]{itemCache};

        // check special nodes
        Name ntName = state.getNodeTypeName();
        if (NameConstants.NT_VERSION.equals(ntName)) {
            // version
            return new VersionImpl(this, session, state, listeners);
        } else if (NameConstants.NT_VERSIONHISTORY.equals(ntName)) {
            // version-history
            return new VersionHistoryImpl(this, session, state, listeners);
        } else {
            // create common node object
            return new NodeImpl(this, session, state, listeners);
        }
    }

    /**
     * @param state
     * @return a new <code>Property</code> instance.
     */
    private PropertyImpl createPropertyInstance(PropertyState state) {
        // we want to be informed on life cycle changes of the new property object
        // in order to maintain item cache consistency
        ItemLifeCycleListener[] listeners = new ItemLifeCycleListener[]{itemCache};
        // create property object
        PropertyImpl prop = new PropertyImpl(this, session, state, listeners);
        return prop;
    }

    //------------------------------------------< ItemStateCreationListener >---
    /**
     *
     * @param state
     */
    public void created(ItemState state) {
        if (state.isNode()) {
            try {
                createNodeInstance((NodeState) state);
            } catch (RepositoryException e) {
                // log warning and ignore
                log.warn("Unable to create Node instance: " + e.getMessage());
            }
        } else {
            createPropertyInstance((PropertyState) state);
        }
    }

    public void statusChanged(ItemState state, int previousStatus) {
        // nothing to do -> Item is listening to status changes and forces
        // cleanup of cache entries through it's own status changes.
    }
}

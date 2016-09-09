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
package org.apache.jackrabbit.jcr2spi.hierarchy;

import org.apache.jackrabbit.jcr2spi.state.ItemState;
import org.apache.jackrabbit.jcr2spi.state.Status;
import org.apache.jackrabbit.jcr2spi.state.ItemStateFactory;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;

/**
 * <code>HierarchyEntryImpl</code> implements base functionality for child node
 * and property references.
 */
abstract class HierarchyEntryImpl implements HierarchyEntry {

    private static Logger log = LoggerFactory.getLogger(HierarchyEntryImpl.class);

    /**
     * Cached weak reference to the target ItemState.
     */
    private Reference target;

    /**
     * The name of the target item state.
     */
    protected Name name;

    /**
     * Hard reference to the parent <code>NodeEntry</code>.
     */
    protected NodeEntryImpl parent;

    /**
     * The item state factory to create the the item state.
     */
    protected final EntryFactory factory;

    /**
     * Creates a new <code>HierarchyEntryImpl</code> with the given parent
     * <code>NodeState</code>.
     *
     * @param parent the <code>NodeEntry</code> that owns this child node
     *               reference.
     * @param name   the name of the child item.
     * @param factory
     */
    HierarchyEntryImpl(NodeEntryImpl parent, Name name, EntryFactory factory) {
        this.parent = parent;
        this.name = name;
        this.factory = factory;
    }

    /**
     * Resolves this <code>HierarchyEntryImpl</code> and returns the target
     * <code>ItemState</code> of this reference. This method may return a
     * cached <code>ItemState</code> if this method was called before already
     * otherwise this method will forward the call to {@link #doResolve()}
     * and cache its return value. If an existing state has been invalidated
     * before, an attempt is made to reload it in order to make sure, that
     * a call to {@link ItemState#isValid()} does not equivocally return false.
     *
     * @return the <code>ItemState</code> where this reference points to.
     * @throws ItemNotFoundException if the referenced <code>ItemState</code>
     * does not exist.
     * @throws RepositoryException if an error occurs.
     */
    ItemState resolve() throws ItemNotFoundException, RepositoryException {
        // check if already resolved
        ItemState state = internalGetItemState();
        // not yet resolved. retrieve and keep weak reference to state
        if (state == null) {
            try {
                state = doResolve();
                // set the item state unless 'setItemState' has already been
                // called by the ItemStateFactory (recall internalGetItemState)
                if (internalGetItemState() == null) {
                    setItemState(state);
                }
            } catch (ItemNotFoundException e) {
                remove();
                throw e;
            }
        } else if (state.getStatus() == Status.INVALIDATED) {
            // completely reload this entry, but don't reload recursively
            reload(false);
        }
        return state;
    }

    /**
     * Resolves this <code>HierarchyEntryImpl</code> and returns the target
     * <code>ItemState</code> of this reference.
     *
     * @return the <code>ItemState</code> where this reference points to.
     * @throws ItemNotFoundException if the referenced <code>ItemState</code>
     * does not exist.
     * @throws RepositoryException if another error occurs.
     */
    abstract ItemState doResolve() throws ItemNotFoundException, RepositoryException;

    /**
     * Build the Path of this entry
     *
     * @param workspacePath
     * @return
     * @throws RepositoryException
     */
    abstract Path buildPath(boolean workspacePath) throws RepositoryException;

    /**
     * @return
     */
    ItemState internalGetItemState() {
        ItemState state = null;
        if (target != null) {
            state = (ItemState) target.get();
        }
        return state;
    }

    //-----------------------------------------------------< HierarchyEntry >---
    /**
     * @inheritDoc
     * @see HierarchyEntry#getName()
     */
    public Name getName() {
        return name;
    }

    /**
     * @inheritDoc
     * @see HierarchyEntry#getPath()
     */
    public Path getPath() throws RepositoryException {
        return buildPath(false);
    }

    /**
     * @inheritDoc
     * @see HierarchyEntry#getWorkspacePath()
     */
    public Path getWorkspacePath() throws RepositoryException {
        return buildPath(true);
    }

    /**
     * @inheritDoc
     * @see HierarchyEntry#getParent()
     */
    public NodeEntry getParent() {
        return parent;
    }

    /**
     * @inheritDoc
     * @see HierarchyEntry#getStatus()
     */
    public int getStatus() {
        ItemState state = internalGetItemState();
        if (state == null) {
            return Status._UNDEFINED_;
        } else {
            return state.getStatus();
        }
    }

    /**
     * @inheritDoc
     * @see HierarchyEntry#isAvailable()
     */
    public boolean isAvailable() {
        return internalGetItemState() != null;
    }

    /**
     * {@inheritDoc}<br>
     * @see HierarchyEntry#getItemState()
     */
    public ItemState getItemState() throws ItemNotFoundException, RepositoryException {
        ItemState state = resolve();
        return state;
    }

    /**
     * {@inheritDoc}<br>
     * @see HierarchyEntry#setItemState(ItemState)
     */
    public synchronized void setItemState(ItemState state) {
        ItemState currentState = internalGetItemState();
        if (state == null || state == currentState || denotesNode() != state.isNode()) {
            throw new IllegalArgumentException();
        }
        if (currentState == null) {
            // not connected yet to an item state. either a new entry or
            // an unresolved hierarchy entry.
            target = new WeakReference(state);
        } else {
            // was already resolved before -> merge the existing state
            // with the passed state.
            int currentStatus = currentState.getStatus();
            boolean keepChanges = Status.isTransient(currentStatus) || Status.isStale(currentStatus);
            boolean modified = currentState.merge(state, keepChanges);
            if (currentStatus == Status.INVALIDATED) {
                currentState.setStatus(Status.EXISTING);
            } else if (modified) {
                currentState.setStatus(Status.MODIFIED);
            } // else: not modified. just leave status as it is.
        }
    }

    /**
     * {@inheritDoc}<br>
     * @see HierarchyEntry#invalidate(boolean)
     */
    public void invalidate(boolean recursive) {
        if (getStatus() == Status.EXISTING) {
            ItemState state = internalGetItemState();
            state.setStatus(Status.INVALIDATED);
        } else {
            log.debug("Skip invalidation for HierarchyEntry " + name + " with status " + Status.getName(getStatus()));
        }
    }

    /**
     * {@inheritDoc}
     * @see HierarchyEntry#revert()
     */
    public void revert() throws RepositoryException {
        ItemState state = internalGetItemState();
        if (state == null) {
            // nothing to do
            return;
        }

        int oldStatus = state.getStatus();
        switch (oldStatus) {
            case Status.EXISTING_MODIFIED:
            case Status.STALE_MODIFIED:
                // revert state modifications
                state.revert();
                state.setStatus(Status.EXISTING);
                break;
            case Status.EXISTING_REMOVED:
                // revert state modifications
                state.revert();
                state.setStatus(Status.EXISTING);
                break;
            case Status.NEW:
                // reverting a NEW state is equivalent to its removal.
                // however: no need remove the complete hierarchy as revert is
                // always related to Item#refresh(false) which affects the
                // complete tree (and all add-operations within it) anyway.
                state.setStatus(Status.REMOVED);
                parent.internalRemoveChildEntry(this);
                break;
            case Status.STALE_DESTROYED:
                // overlayed does not exist any more -> reverting of pending
                // transient changes (that lead to the stale status) can be
                // omitted and the entry is complete removed instead.
                remove();
                break;
            default:
                // Cannot revert EXISTING, REMOVED, INVALIDATED, MODIFIED states.
                // State was implicitely reverted or external modifications
                // reverted the modification.
                log.debug("State with status " + oldStatus + " cannot be reverted.");
        }
    }

    /**
     * {@inheritDoc}
     * @see HierarchyEntry#reload(boolean)
     */
    public void reload(boolean recursive) {
        int status = getStatus();
        if (status == Status._UNDEFINED_) {
            // unresolved: entry will be loaded and validated upon resolution.
            return;
        }
        if (Status.isTransient(status) || Status.isStale(status) || Status.isTerminal(status)) {
            // transient || stale: avoid reloading
            // new || terminal: cannot be reloaded from persistent layer anyway.
            log.debug("Skip reload for item with status " + Status.getName(status) + ".");
            return;
        }
        /**
         * Retrieved a fresh ItemState from the persistent layer. Which will
         * then be merged into the current state.
         */
        try {
            ItemStateFactory isf = factory.getItemStateFactory();
            if (denotesNode()) {
                NodeEntry ne = (NodeEntry) this;
                isf.createNodeState(ne.getWorkspaceId(), ne);
            } else {
                PropertyEntry pe = (PropertyEntry) this;
                isf.createPropertyState(pe.getWorkspaceId(), pe);
            }
        } catch (ItemNotFoundException e) {
            // remove hierarchyEntry including all children
            log.debug("Item '" + getName() + "' cannot be found on the persistent layer -> remove.");
            remove();
        } catch (RepositoryException e) {
            // TODO: rather throw?
            log.error("Exception while reloading item: " + e);
        }
    }

    /**
     * {@inheritDoc}
     * @see HierarchyEntry#transientRemove()
     */
    public void transientRemove() throws InvalidItemStateException, RepositoryException {
        ItemState state = internalGetItemState();
        if (state == null) {
            // nothing to do -> correct status must be set upon resolution.
            return;
        }
        // if during recursive removal an invalidated entry is found, reload
        // it in order to determine the current status.
        if (state.getStatus() == Status.INVALIDATED) {
            reload(false);
        }

        switch (state.getStatus()) {
            case Status.NEW:
                state.setStatus(Status.REMOVED);
                parent.internalRemoveChildEntry(this);
                break;
            case Status.EXISTING:
            case Status.EXISTING_MODIFIED:
                state.setStatus(Status.EXISTING_REMOVED);
                // NOTE: parent does not need to be informed. an transiently
                // removed propertyEntry is automatically moved to the 'attic'
                // if a conflict with a new entry occurs.
                break;
            case Status.REMOVED:
            case Status.STALE_DESTROYED:
                throw new InvalidItemStateException("Item has already been removed by someone else. Status = " + Status.getName(state.getStatus()));
            default:
                throw new RepositoryException("Cannot transiently remove an ItemState with status " + Status.getName(state.getStatus()));
        }
    }

    /**
     * @see HierarchyEntry#remove()
     */
    public void remove() {
        internalRemove(false);
    }

    //--------------------------------------------------------------------------
    /**
     *
     * @param keepNew
     */
    void internalRemove(boolean staleParent) {
        ItemState state = internalGetItemState();
        int status = getStatus();
        if (state != null) {
            if (status == Status.EXISTING_MODIFIED) {
                state.setStatus(Status.STALE_DESTROYED);
            } else if (status == Status.NEW && staleParent) {
                // keep status NEW
            } else {
                state.setStatus(Status.REMOVED);
                if (!staleParent) {
                    parent.internalRemoveChildEntry(this);
                }
            }
        } else {
            // unresolved
            if (!staleParent) {
                parent.internalRemoveChildEntry(this);
            }
        }
    }
}

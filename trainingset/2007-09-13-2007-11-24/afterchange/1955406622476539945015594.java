commit c19ca554066ec224eea765399ccf4501f5aa59c4
Author: Angela Schreiber <angela@apache.org>
Date:   Mon Oct 22 06:51:44 2007 +0000

    JCR-1099 jcr2spi NodeEntryImpl.getPath() blows stack due to getIndex() calling itself
    
    git-svn-id: https://svn.apache.org/repos/asf/jackrabbit/trunk@587011 13f79535-47bb-0310-9956-ffa450edef68

diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java
index 8e8a5b7..82c6e23 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java
@@ -352,7 +352,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
         NodeState state = (NodeState) internalGetItemState();
         try {
-            if (state == null || state.getDefinition().allowsSameNameSiblings()) {
+            if (state == null || !state.hasDefinition() || state.getDefinition().allowsSameNameSiblings()) {
                 return parent.getChildIndex(this);
             } else {
                 return Path.INDEX_DEFAULT;
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/NodeState.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/NodeState.java
index 478b23e..9f69536 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/NodeState.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/NodeState.java
@@ -375,6 +375,16 @@ public class NodeState extends ItemState {
     }
 
     /**
+     * Returns true if the definition of this state has already been
+     * calculated. False otherwise.
+     *
+     * @return true if definition has already been calculated.
+     */
+    public boolean hasDefinition() throws RepositoryException {
+        return definition != null;
+    }
+
+    /**
      * Returns the {@link QNodeDefinition definition} defined for this
      * node state. Note, that the definition has been set upon creation or
      * upon move.

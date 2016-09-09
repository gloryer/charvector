commit c8406704341b2e01a196d452fcbbb4f343607823
Author: Marcel Reutegger <mreutegg@apache.org>
Date:   Mon Sep 17 08:22:17 2007 +0000

    JCR-1131: JCR2SPI NodeEntryImpl throws NPE during reorderNodes
    
    git-svn-id: https://svn.apache.org/repos/asf/jackrabbit/trunk@576296 13f79535-47bb-0310-9956-ffa450edef68

diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java
index 6369451..e1dc7dd 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java
@@ -1351,7 +1351,10 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      */
     private void completeTransientChanges() {
         // old parent can forget this one
-        revertInfo.oldParent.childNodeAttic.remove(this);
+        // root entry does not have oldParent
+        if (revertInfo.oldParent != null) {
+            revertInfo.oldParent.childNodeAttic.remove(this);
+        }
         revertInfo.dispose();
         revertInfo = null;
     }

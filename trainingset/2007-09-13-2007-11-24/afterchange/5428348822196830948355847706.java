commit 54283d4882219a6c83ca09483ab55b847c70dea6
Author: Angela Schreiber <angela@apache.org>
Date:   Wed Oct 3 15:43:48 2007 +0000

    Omit check for existing prop/node upon Workspace.move()
    
    git-svn-id: https://svn.apache.org/repos/asf/jackrabbit/trunk@581636 13f79535-47bb-0310-9956-ffa450edef68

diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/SessionImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/SessionImpl.java
index 12bb36f..742e81f 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/SessionImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/SessionImpl.java
@@ -304,7 +304,7 @@ public class SessionImpl implements Session, ManagerProvider {
         Path destPath = getQPath(destAbsPath);
 
         // all validation is performed by Move Operation and state-manager
-        Operation op = Move.create(srcPath, destPath, getHierarchyManager(), getNamespaceResolver());
+        Operation op = Move.create(srcPath, destPath, getHierarchyManager(), getNamespaceResolver(), true);
         itemStateManager.execute(op);
     }
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java
index 1cadfef..294120c 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java
@@ -233,7 +233,7 @@ public class WorkspaceImpl implements Workspace, ManagerProvider {
         Path srcPath = session.getQPath(srcAbsPath);
         Path destPath = session.getQPath(destAbsPath);
 
-        Operation op = Move.create(srcPath, destPath, getHierarchyManager(), getNamespaceResolver());
+        Operation op = Move.create(srcPath, destPath, getHierarchyManager(), getNamespaceResolver(), false);
         getUpdatableItemStateManager().execute(op);
     }
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java
index 47b84d9..cc92145 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java
@@ -123,7 +123,9 @@ public class Move extends AbstractOperation {
 
     //------------------------------------------------------------< Factory >---
     public static Operation create(Path srcPath, Path destPath,
-                                   HierarchyManager hierMgr, NamespaceResolver nsResolver)
+                                   HierarchyManager hierMgr,
+                                   NamespaceResolver nsResolver,
+                                   boolean sessionMove)
         throws ItemExistsException, NoSuchNodeTypeException, RepositoryException {
         // src must not be ancestor of destination
         try {
@@ -158,21 +160,24 @@ public class Move extends AbstractOperation {
         NodeState destParentState = getNodeState(destPath.getAncestor(1), hierMgr, nsResolver);
         QName destName = destElement.getName();
 
-        // lazy check for existing items at destination. since the hierarchy
-        // may not be complete it is possible that an conflict is only detected
-        // upon saving the 'move'.
+        // for session-move perform a lazy check for existing items at destination.
+        // since the hierarchy may not be complete it is possible that an conflict
+        // is only detected upon saving the 'move'.
         NodeEntry destEntry = (NodeEntry) destParentState.getHierarchyEntry();
-        if (destEntry.hasPropertyEntry(destName)) {
-            throw new ItemExistsException("Move destination already exists (Property).");
-        } else if (destEntry.hasNodeEntry(destName)) {
-            NodeEntry existing = destEntry.getNodeEntry(destName, Path.INDEX_DEFAULT);
-            if (existing != null) {
-                try {
-                    if (!existing.getNodeState().getDefinition().allowsSameNameSiblings()) {
-                        throw new ItemExistsException("Node existing at move destination does not allow same name siblings.");
+        if (sessionMove) {
+            if (destEntry.hasPropertyEntry(destName)) {
+                throw new ItemExistsException("Move destination already exists (Property).");
+            }
+            if (destEntry.hasNodeEntry(destName)) {
+                NodeEntry existing = destEntry.getNodeEntry(destName, Path.INDEX_DEFAULT);
+                if (existing != null) {
+                    try {
+                        if (!existing.getNodeState().getDefinition().allowsSameNameSiblings()) {
+                            throw new ItemExistsException("Node existing at move destination does not allow same name siblings.");
+                        }
+                    } catch (ItemNotFoundException e) {
+                        // existing apparent not valid any more -> probably no conflict
                     }
-                } catch (ItemNotFoundException e) {
-                    // existing apparent not valid any more -> probably no conflict
                 }
             }
         }

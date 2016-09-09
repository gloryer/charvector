commit 39fbc0b00381bdc2425d557e2ea85d8d6d3c0a37
Author: Angela Schreiber <angela@apache.org>
Date:   Thu Oct 18 18:41:45 2007 +0000

    JCR-996 Name and Path interfaces in SPI
    JCR-1169 Distribution of commons classes
    
    
    git-svn-id: https://svn.apache.org/repos/asf/jackrabbit/trunk@586065 13f79535-47bb-0310-9956-ffa450edef68

diff --git a/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/JCR2SPI2JCRRepositoryStub.java b/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/JCR2SPI2JCRRepositoryStub.java
index e9e4fca..eb8fff1 100644
--- a/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/JCR2SPI2JCRRepositoryStub.java
+++ b/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/JCR2SPI2JCRRepositoryStub.java
@@ -20,7 +20,7 @@ import org.apache.jackrabbit.test.RepositoryStubException;
 import org.apache.jackrabbit.spi2jcr.RepositoryServiceImpl;
 import org.apache.jackrabbit.spi2jcr.BatchReadConfig;
 import org.apache.jackrabbit.spi.RepositoryService;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.name.NameConstants;
 
 import javax.jcr.Repository;
 import javax.jcr.RepositoryException;
@@ -80,8 +80,8 @@ public class JCR2SPI2JCRRepositoryStub extends DefaultRepositoryStub {
 
         // TODO: make configurable
         BatchReadConfig brconfig = new BatchReadConfig();
-        brconfig.setDepth(QName.NT_FILE, BatchReadConfig.DEPTH_INFINITE);
-        brconfig.setDepth(QName.NT_RESOURCE, BatchReadConfig.DEPTH_INFINITE);
+        brconfig.setDepth(NameConstants.NT_FILE, BatchReadConfig.DEPTH_INFINITE);
+        brconfig.setDepth(NameConstants.NT_RESOURCE, BatchReadConfig.DEPTH_INFINITE);
 
         return new RepositoryServiceImpl(jackrabbitRepo, brconfig);
     }
diff --git a/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/JCR2SPIRepositoryStub.java b/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/JCR2SPIRepositoryStub.java
index 787fa78..20b68d6 100644
--- a/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/JCR2SPIRepositoryStub.java
+++ b/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/JCR2SPIRepositoryStub.java
@@ -22,7 +22,11 @@ import org.apache.jackrabbit.identifier.IdFactoryImpl;
 import org.apache.jackrabbit.jcr2spi.config.RepositoryConfig;
 import org.apache.jackrabbit.spi.RepositoryService;
 import org.apache.jackrabbit.spi.IdFactory;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.PathFactory;
 import org.apache.jackrabbit.value.ValueFactoryImplEx;
+import org.apache.jackrabbit.name.NameFactoryImpl;
+import org.apache.jackrabbit.name.PathFactoryImpl;
 import org.apache.log4j.PropertyConfigurator;
 
 import javax.jcr.Repository;
@@ -68,7 +72,9 @@ public class JCR2SPIRepositoryStub extends RepositoryStub {
 
                 final IdFactory idFactory = IdFactoryImpl.getInstance();
                 final ValueFactory vFactory = ValueFactoryImplEx.getInstance();
-                final RepositoryServiceImpl webdavRepoService = new RepositoryServiceImpl(url, idFactory, vFactory);
+                final NameFactory nFactory = NameFactoryImpl.getInstance();
+                final PathFactory pFactory = PathFactoryImpl.getInstance();
+                final RepositoryServiceImpl webdavRepoService = new RepositoryServiceImpl(url, idFactory, nFactory, pFactory, vFactory);
 
                 RepositoryConfig config = new AbstractRepositoryConfig() {
                     public RepositoryService getRepositoryService() {
diff --git a/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/RepositorySetup.java b/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/RepositorySetup.java
index b2f82bb..e204260 100644
--- a/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/RepositorySetup.java
+++ b/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/RepositorySetup.java
@@ -21,7 +21,7 @@ import org.apache.jackrabbit.core.WorkspaceImpl;
 import org.apache.jackrabbit.core.nodetype.xml.NodeTypeReader;
 import org.apache.jackrabbit.core.nodetype.NodeTypeDef;
 import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
-import org.apache.jackrabbit.BaseException;
+import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
@@ -200,10 +200,12 @@ public class RepositorySetup {
                         }
                     }
                     ntMgr.getNodeTypeRegistry().registerNodeTypes(unregisteredNTs);
-                } catch (BaseException e) {
-                    throw new RepositoryException(e.getMessage());
                 } catch (IOException e) {
                     throw new RepositoryException(e.getMessage());
+                } catch (InvalidNodeTypeDefException e) {
+                    throw new RepositoryException(e.getMessage());
+                } catch (org.apache.jackrabbit.name.NameException e) {
+                    throw new RepositoryException(e.getMessage());
                 } finally {
                     try {
                         is.close();
diff --git a/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/TestAll.java b/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/TestAll.java
index 61495ca..7ac3fa4 100644
--- a/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/TestAll.java
+++ b/contrib/spi/client/src/test/java/org/apache/jackrabbit/jcr2spi/TestAll.java
@@ -67,6 +67,7 @@ public class TestAll extends TestCase {
         suite.addTestSuite(RenameTest.class);
 
         // reorder
+        /*
         suite.addTestSuite(ReorderTest.class);
         suite.addTestSuite(ReorderReferenceableSNSTest.class);
         suite.addTestSuite(ReorderSNSTest.class);
@@ -75,7 +76,7 @@ public class TestAll extends TestCase {
         suite.addTestSuite(ReorderNewAndSavedTest.class);
         suite.addTestSuite(ReorderMixedTest.class);
         suite.addTestSuite(ReorderMoveTest.class);
-
+        */
         // update
         suite.addTestSuite(UpdateTest.class);
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemImpl.java
index c4c6c8e..4ec93e0 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemImpl.java
@@ -27,10 +27,8 @@ import org.apache.jackrabbit.jcr2spi.operation.Operation;
 import org.apache.jackrabbit.jcr2spi.util.LogUtil;
 import org.apache.jackrabbit.jcr2spi.config.CacheBehaviour;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.PathFormat;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.Name;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -95,14 +93,7 @@ public abstract class ItemImpl implements Item, ItemStateLifeCycleListener {
      */
     public String getPath() throws RepositoryException {
         checkStatus();
-        try {
-            return PathFormat.format(getQPath(), session.getNamespaceResolver());
-        } catch (NoPrefixDeclaredException npde) {
-            // should never get here...
-            String msg = "Internal error: encountered unregistered namespace";
-            log.debug(msg);
-            throw new RepositoryException(msg, npde);
-        }
+        return session.getPathResolver().getJCRPath(getQPath());
     }
 
     /**
@@ -504,13 +495,13 @@ public abstract class ItemImpl implements Item, ItemStateLifeCycleListener {
     //------------------------------------< Implementation specific methods >---
     /**
      * Same as <code>{@link Item#getName()}</code> except that
-     * this method returns a <code>QName</code> instead of a
+     * this method returns a <code>Name</code> instead of a
      * <code>String</code>.
      *
-     * @return the name of this item as <code>QName</code>
+     * @return the name of this item as <code>Name</code>
      * @throws RepositoryException if an error occurs.
      */
-    abstract QName getQName() throws RepositoryException;
+    abstract Name getQName() throws RepositoryException;
 
     /**
      * Returns the primary path to this <code>Item</code>.
@@ -537,6 +528,6 @@ public abstract class ItemImpl implements Item, ItemStateLifeCycleListener {
      * @return JCR path
      */
     String safeGetJCRPath() {
-        return LogUtil.safeGetJCRPath(getItemState(), session.getNamespaceResolver());
+        return LogUtil.safeGetJCRPath(getItemState(), session.getPathResolver());
     }
 }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManager.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManager.java
index 34f8504..ba3eb21 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManager.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManager.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi;
 
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyEntry;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManagerImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManagerImpl.java
index ef14009..b64a863 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManagerImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManagerImpl.java
@@ -27,8 +27,9 @@ import org.apache.jackrabbit.jcr2spi.util.Dumpable;
 import org.apache.jackrabbit.jcr2spi.util.LogUtil;
 import org.apache.jackrabbit.jcr2spi.version.VersionHistoryImpl;
 import org.apache.jackrabbit.jcr2spi.version.VersionImpl;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.commons.collections.map.ReferenceMap;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
@@ -145,7 +146,7 @@ public class ItemManagerImpl implements Dumpable, ItemManager {
         try {
             return getItem(itemEntry);
         } catch (ItemNotFoundException infe) {
-            throw new PathNotFoundException(LogUtil.safeGetJCRPath(path, session.getNamespaceResolver()));
+            throw new PathNotFoundException(LogUtil.safeGetJCRPath(path, session.getPathResolver()));
         }
     }
 
@@ -328,7 +329,7 @@ public class ItemManagerImpl implements Dumpable, ItemManager {
             } else {
                 ps.print("- ");
             }
-            ps.println(state + "\t" + LogUtil.safeGetJCRPath(state, session.getNamespaceResolver()) + " (" + item + ")");
+            ps.println(state + "\t" + LogUtil.safeGetJCRPath(state, session.getPathResolver()) + " (" + item + ")");
         }
     }
 
@@ -344,11 +345,11 @@ public class ItemManagerImpl implements Dumpable, ItemManager {
         ItemLifeCycleListener[] listeners = new ItemLifeCycleListener[]{this};
 
         // check special nodes
-        QName ntName = state.getNodeTypeName();
-        if (QName.NT_VERSION.equals(ntName)) {
+        Name ntName = state.getNodeTypeName();
+        if (NameConstants.NT_VERSION.equals(ntName)) {
             // version
             return new VersionImpl(this, session, state, listeners);
-        } else if (QName.NT_VERSIONHISTORY.equals(ntName)) {
+        } else if (NameConstants.NT_VERSIONHISTORY.equals(ntName)) {
             // version-history
             return new VersionHistoryImpl(this, session, state, listeners);
         } else {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ManagerProvider.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ManagerProvider.java
index 352d237..6412f20 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ManagerProvider.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ManagerProvider.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManager;
 import org.apache.jackrabbit.jcr2spi.security.AccessManager;
 import org.apache.jackrabbit.jcr2spi.lock.LockManager;
@@ -24,6 +24,7 @@ import org.apache.jackrabbit.jcr2spi.version.VersionManager;
 import org.apache.jackrabbit.jcr2spi.nodetype.ItemDefinitionProvider;
 import org.apache.jackrabbit.jcr2spi.nodetype.EffectiveNodeTypeProvider;
 import org.apache.jackrabbit.spi.QValueFactory;
+import org.apache.jackrabbit.conversion.NameResolver;
 
 import javax.jcr.ValueFactory;
 import javax.jcr.RepositoryException;
@@ -33,6 +34,12 @@ import javax.jcr.RepositoryException;
  */
 public interface ManagerProvider {
 
+    public org.apache.jackrabbit.conversion.NamePathResolver getNamePathResolver();
+
+    public NameResolver getNameResolver();
+
+    public org.apache.jackrabbit.conversion.PathResolver getPathResolver();
+
     public NamespaceResolver getNamespaceResolver();
 
     public HierarchyManager getHierarchyManager();
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/NodeImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/NodeImpl.java
index 4f0ddce..dd8ab69 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/NodeImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/NodeImpl.java
@@ -20,20 +20,16 @@ import org.apache.jackrabbit.util.ChildrenCollectorFilter;
 import org.apache.jackrabbit.util.IteratorHelper;
 import org.apache.jackrabbit.value.ValueHelper;
 import org.apache.jackrabbit.value.ValueFormat;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.state.ItemStateValidator;
 import org.apache.jackrabbit.jcr2spi.state.NodeReferences;
 import org.apache.jackrabbit.jcr2spi.state.Status;
 import org.apache.jackrabbit.jcr2spi.nodetype.NodeTypeManagerImpl;
 import org.apache.jackrabbit.jcr2spi.nodetype.EffectiveNodeType;
-import org.apache.jackrabbit.jcr2spi.nodetype.NodeTypeConflictException;
+import org.apache.jackrabbit.nodetype.NodeTypeConflictException;
 import org.apache.jackrabbit.jcr2spi.nodetype.NodeTypeImpl;
 import org.apache.jackrabbit.jcr2spi.operation.SetMixin;
 import org.apache.jackrabbit.jcr2spi.operation.AddProperty;
@@ -50,6 +46,7 @@ import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyEntry;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QValue;
+import org.apache.jackrabbit.name.NameConstants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
@@ -95,12 +92,12 @@ public class NodeImpl extends ItemImpl implements Node {
 
     private static Logger log = LoggerFactory.getLogger(NodeImpl.class);
 
-    private QName primaryTypeName;
+    private Name primaryTypeName;
 
     protected NodeImpl(ItemManager itemMgr, SessionImpl session,
                        NodeState state, ItemLifeCycleListener[] listeners) {
         super(itemMgr, session, state, listeners);
-        QName nodeTypeName = state.getNodeTypeName();
+        Name nodeTypeName = state.getNodeTypeName();
         // make sure the nodetype name is valid
         if (session.getNodeTypeManager().hasNodeType(nodeTypeName)) {
             primaryTypeName = nodeTypeName;
@@ -108,7 +105,7 @@ public class NodeImpl extends ItemImpl implements Node {
             // should not occur. Since nodetypes are defined by the 'server'
             // its not possible to determine a fallback nodetype that is
             // always available.
-            throw new IllegalArgumentException("Unknown nodetype " + LogUtil.saveGetJCRName(nodeTypeName, session.getNamespaceResolver()));
+            throw new IllegalArgumentException("Unknown nodetype " + LogUtil.saveGetJCRName(nodeTypeName, session.getNameResolver()));
         }
     }
 
@@ -118,15 +115,8 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     public String getName() throws RepositoryException {
         checkStatus();
-        QName qName = getQName();
-        try {
-            return NameFormat.format(getQName(), session.getNamespaceResolver());
-        } catch (NoPrefixDeclaredException e) {
-            // should never get here...
-            String msg = "Internal error while resolving qualified name " + qName.toString();
-            log.debug(msg);
-            throw new RepositoryException(msg, e);
-        }
+        Name qName = getQName();
+        return session.getNameResolver().getJCRName(getQName());
     }
 
     /**
@@ -177,7 +167,7 @@ public class NodeImpl extends ItemImpl implements Node {
         try {
             Item parent = itemMgr.getItem(parentPath);
             if (!parent.isNode()) {
-                String msg = "Cannot add a node to property " + LogUtil.safeGetJCRPath(parentPath, session.getNamespaceResolver());
+                String msg = "Cannot add a node to property " + LogUtil.safeGetJCRPath(parentPath, session.getPathResolver());
                 log.debug(msg);
                 throw new ConstraintViolationException(msg);
             } else if (!(parent instanceof NodeImpl)) {
@@ -193,8 +183,8 @@ public class NodeImpl extends ItemImpl implements Node {
         }
 
         // 2. get qualified names for node and nt
-        QName nodeName = nodePath.getNameElement().getName();
-        QName ntName = (primaryNodeTypeName == null) ? null : getQName(primaryNodeTypeName);
+        Name nodeName = nodePath.getNameElement().getName();
+        Name ntName = (primaryNodeTypeName == null) ? null : getQName(primaryNodeTypeName);
 
         // 3. create new node (including validation checks)
         return parentNode.createNode(nodeName, ntName);
@@ -224,8 +214,8 @@ public class NodeImpl extends ItemImpl implements Node {
             throw new ItemNotFoundException("Node " + safeGetJCRPath() + " has no child node with name " + destChildRelPath);
         }
 
-        Path.PathElement srcName = getReorderPath(srcChildRelPath).getNameElement();
-        Path.PathElement beforeName = (destChildRelPath == null) ? null : getReorderPath(destChildRelPath).getNameElement();
+        Path.Element srcName = getReorderPath(srcChildRelPath).getNameElement();
+        Path.Element beforeName = (destChildRelPath == null) ? null : getReorderPath(destChildRelPath).getNameElement();
 
         Operation op = ReorderNodes.create(getNodeState(), srcName, beforeName);
         session.getSessionItemStateManager().execute(op);
@@ -248,11 +238,11 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         checkIsWritable();
-        QName propQName = getQName(name);
+        Name propName = getQName(name);
         Property prop;
-        if (hasProperty(propQName)) {
+        if (hasProperty(propName)) {
             // property already exists: pass call to property
-            prop = getProperty(propQName);
+            prop = getProperty(propName);
             Value v = (type == PropertyType.UNDEFINED) ? value : ValueHelper.convert(value, type, session.getValueFactory());
             prop.setValue(v);
         } else {
@@ -261,7 +251,7 @@ public class NodeImpl extends ItemImpl implements Node {
                 throw new ItemNotFoundException("Cannot remove a non-existing property.");
             } else {
                 // new property to be added
-                prop = createProperty(propQName, value, type);
+                prop = createProperty(propName, value, type);
             }
         }
         return prop;
@@ -286,7 +276,7 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         checkIsWritable();
-        QName propName = getQName(name);
+        Name propName = getQName(name);
         Property prop;
         if (hasProperty(propName)) {
             // property already exists: pass call to property
@@ -398,7 +388,7 @@ public class NodeImpl extends ItemImpl implements Node {
         if (value == null) {
             v = null;
         } else {
-            PropertyImpl.checkValidReference(value, PropertyType.REFERENCE, session.getNamespaceResolver());
+            PropertyImpl.checkValidReference(value, PropertyType.REFERENCE, session.getNameResolver());
             v = session.getValueFactory().createValue(value);
         }
         return setProperty(name, v, PropertyType.REFERENCE);
@@ -522,7 +512,7 @@ public class NodeImpl extends ItemImpl implements Node {
     public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
         checkStatus();
         String uuid = getNodeState().getUniqueID();
-        if (uuid == null || !isNodeType(QName.MIX_REFERENCEABLE)) {
+        if (uuid == null || !isNodeType(NameConstants.MIX_REFERENCEABLE)) {
             throw new UnsupportedRepositoryOperationException();
         }
         // Node is referenceable -> NodeId must contain a UUID part
@@ -580,7 +570,7 @@ public class NodeImpl extends ItemImpl implements Node {
      * @param propertyName
      * @return
      */
-    private boolean hasProperty(QName propertyName) {
+    private boolean hasProperty(Name propertyName) {
         return getNodeEntry().hasPropertyEntry(propertyName);
     }
 
@@ -613,7 +603,7 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     public NodeType[] getMixinNodeTypes() throws RepositoryException {
         checkStatus();
-        QName[] mixinNames = getNodeState().getMixinTypeNames();
+        Name[] mixinNames = getNodeState().getMixinTypeNames();
         NodeType[] nta = new NodeType[mixinNames.length];
         for (int i = 0; i < mixinNames.length; i++) {
             nta[i] = session.getNodeTypeManager().getNodeType(mixinNames[i]);
@@ -627,14 +617,10 @@ public class NodeImpl extends ItemImpl implements Node {
     public boolean isNodeType(String nodeTypeName) throws RepositoryException {
         checkStatus();
         // try shortcut first (avoids parsing of name)
-        try {
-            if (NameFormat.format(primaryTypeName, session.getNamespaceResolver()).equals(nodeTypeName)) {
-                return true;
-            }
-        } catch (NoPrefixDeclaredException npde) {
-            throw new RepositoryException("Invalid node type name: " + nodeTypeName, npde);
+        if (session.getNameResolver().getJCRName(primaryTypeName).equals(nodeTypeName)) {
+            return true;
         }
-        // parse to QName and check against effective nodetype
+        // parse to Name and check against effective nodetype
         return isNodeType(getQName(nodeTypeName));
     }
 
@@ -644,7 +630,7 @@ public class NodeImpl extends ItemImpl implements Node {
     public void addMixin(String mixinName) throws NoSuchNodeTypeException,
         VersionException, ConstraintViolationException, LockException, RepositoryException {
         checkIsWritable();
-        QName mixinQName = getQName(mixinName);
+        Name mixinQName = getQName(mixinName);
         try {
             if (!canAddMixin(mixinQName)) {
                 throw new ConstraintViolationException("Cannot add '" + mixinName + "' mixin type.");
@@ -661,7 +647,7 @@ public class NodeImpl extends ItemImpl implements Node {
         } else {
             mixinValue.add(mixinQName);
             // perform the operation
-            Operation op = SetMixin.create(getNodeState(), (QName[]) mixinValue.toArray(new QName[mixinValue.size()]));
+            Operation op = SetMixin.create(getNodeState(), (Name[]) mixinValue.toArray(new Name[mixinValue.size()]));
             session.getSessionItemStateManager().execute(op);
         }
     }
@@ -672,7 +658,7 @@ public class NodeImpl extends ItemImpl implements Node {
     public void removeMixin(String mixinName) throws NoSuchNodeTypeException,
         VersionException, ConstraintViolationException, LockException, RepositoryException {
         checkIsWritable();
-        QName ntName = getQName(mixinName);
+        Name ntName = getQName(mixinName);
         List mixinValue = getMixinTypes();
         // remove name of target mixin
         if (!mixinValue.remove(ntName)) {
@@ -682,10 +668,10 @@ public class NodeImpl extends ItemImpl implements Node {
         // mix:referenceable needs additional assertion: the mixin cannot be
         // removed, if any references are left to this node.
         NodeTypeImpl mixin = session.getNodeTypeManager().getNodeType(ntName);
-        if (mixin.isNodeType(QName.MIX_REFERENCEABLE)) {
+        if (mixin.isNodeType(NameConstants.MIX_REFERENCEABLE)) {
             // build effective node type of remaining mixin's & primary type
             EffectiveNodeType entRemaining;
-            QName[] allRemaining = (QName[]) mixinValue.toArray(new QName[mixinValue.size() + 1]);
+            Name[] allRemaining = (Name[]) mixinValue.toArray(new Name[mixinValue.size() + 1]);
             allRemaining[mixinValue.size()] = primaryTypeName;
             try {
                 entRemaining = session.getEffectiveNodeTypeProvider().getEffectiveNodeType(allRemaining);
@@ -693,7 +679,7 @@ public class NodeImpl extends ItemImpl implements Node {
                 throw new ConstraintViolationException(e);
             }
 
-            if (!entRemaining.includesNodeType(QName.MIX_REFERENCEABLE)) {
+            if (!entRemaining.includesNodeType(NameConstants.MIX_REFERENCEABLE)) {
                 PropertyIterator iter = getReferences();
                 if (iter.hasNext()) {
                     throw new ConstraintViolationException("Mixin type " + mixinName + " can not be removed: the node is being referenced through at least one property of type REFERENCE");
@@ -702,7 +688,7 @@ public class NodeImpl extends ItemImpl implements Node {
         }
 
         // delegate to operation
-        QName[] mixins = (QName[]) mixinValue.toArray(new QName[mixinValue.size()]);
+        Name[] mixins = (Name[]) mixinValue.toArray(new Name[mixinValue.size()]);
         Operation op = SetMixin.create(getNodeState(), mixins);
         session.getSessionItemStateManager().execute(op);
     }
@@ -717,13 +703,13 @@ public class NodeImpl extends ItemImpl implements Node {
      * @return
      */
     private List getMixinTypes() {
-        QName[] mixinValue;
+        Name[] mixinValue;
         if (getNodeState().getStatus() == Status.EXISTING) {
             // jcr:mixinTypes must correspond to the mixins present on the nodestate.
             mixinValue = getNodeState().getMixinTypeNames();
         } else {
             try {
-                PropertyEntry pe = getNodeEntry().getPropertyEntry(QName.JCR_MIXINTYPES);
+                PropertyEntry pe = getNodeEntry().getPropertyEntry(NameConstants.JCR_MIXINTYPES);
                 if (pe != null) {
                     // prop entry exists (and ev. has been transiently mod.)
                     // -> retrieve mixin types from prop
@@ -735,7 +721,7 @@ public class NodeImpl extends ItemImpl implements Node {
             } catch (RepositoryException e) {
                 // should never occur
                 log.warn("Internal error", e);
-                mixinValue = new QName[0];
+                mixinValue = new Name[0];
             }
         }
         List l = new ArrayList();
@@ -849,8 +835,8 @@ public class NodeImpl extends ItemImpl implements Node {
 
         // check if version is in mergeFailed list
         boolean isConflicting = false;
-        if (hasProperty(QName.JCR_MERGEFAILED)) {
-            Value[] vals = getProperty(QName.JCR_MERGEFAILED).getValues();
+        if (hasProperty(NameConstants.JCR_MERGEFAILED)) {
+            Value[] vals = getProperty(NameConstants.JCR_MERGEFAILED).getValues();
             for (int i = 0; i < vals.length && !isConflicting; i++) {
                 isConflicting = vals[i].getString().equals(version.getUUID());
             }
@@ -921,7 +907,7 @@ public class NodeImpl extends ItemImpl implements Node {
             // search nearest ancestor that is referenceable
             NodeImpl referenceableNode = this;
             while (referenceableNode.getDepth() != Path.ROOT_DEPTH
-                && !referenceableNode.isNodeType(QName.MIX_REFERENCEABLE)) {
+                && !referenceableNode.isNodeType(NameConstants.MIX_REFERENCEABLE)) {
                 referenceableNode = (NodeImpl) referenceableNode.getParent();
             }
 
@@ -943,7 +929,7 @@ public class NodeImpl extends ItemImpl implements Node {
                 } else {
                     Path p = referenceableNode.getQPath().computeRelativePath(getQPath());
                     // use prefix mappings of srcSession
-                    String relPath = PathFormat.format(p, session.getNamespaceResolver());
+                    String relPath = session.getPathResolver().getJCRPath(p);
                     if (!correspNode.hasNode(relPath)) {
                         throw new ItemNotFoundException("No corresponding path found in workspace " + workspaceName + "(" + safeGetJCRPath() + ")");
                     } else {
@@ -952,11 +938,6 @@ public class NodeImpl extends ItemImpl implements Node {
                 }
             }
             return correspondingPath;
-        } catch (NameException e) {
-            // should never get here...
-            String msg = "Internal error: failed to determine relative path";
-            log.error(msg, e);
-            throw new RepositoryException(msg, e);
         } finally {
             if (srcSession != null) {
                 // we don't need the other session anymore, logout
@@ -1012,15 +993,10 @@ public class NodeImpl extends ItemImpl implements Node {
             if (itemMgr.itemExists(parentPath)) {
                 Item parent = itemMgr.getItem(parentPath);
                 if (parent.isNode()) {
-                    try {
-                        Path relQPath = parentPath.computeRelativePath(nPath);
-                        NodeImpl parentNode = ((NodeImpl)parent);
-                        // call the restore
-                        restore(parentNode, relQPath, version, removeExisting);
-                    } catch (MalformedPathException e) {
-                        // should not occur
-                        throw new RepositoryException(e);
-                    }
+                    Path relQPath = parentPath.computeRelativePath(nPath);
+                    NodeImpl parentNode = ((NodeImpl)parent);
+                    // call the restore
+                    restore(parentNode, relQPath, version, removeExisting);
                 } else {
                     // the item at parentParentPath is Property
                     throw new ConstraintViolationException("Cannot restore to a parent presenting a property (relative path = '" + relPath + "'");
@@ -1085,7 +1061,7 @@ public class NodeImpl extends ItemImpl implements Node {
             if (!targetNode.isCheckedOut()) {
                 throw new VersionException("Parent " + targetNode.safeGetJCRPath()
                     + " for non-existing restore target '"
-                    + LogUtil.safeGetJCRPath(relQPath, session.getNamespaceResolver())
+                    + LogUtil.safeGetJCRPath(relQPath, session.getPathResolver())
                     + "' must be checked out.");
             }
             targetNode.checkIsLocked();
@@ -1101,7 +1077,7 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
         checkIsVersionable();
-        return (VersionHistory) getProperty(QName.JCR_VERSIONHISTORY).getNode();
+        return (VersionHistory) getProperty(NameConstants.JCR_VERSIONHISTORY).getNode();
     }
 
     /**
@@ -1109,7 +1085,7 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
         checkIsVersionable();
-        return (Version) getProperty(QName.JCR_BASEVERSION).getNode();
+        return (Version) getProperty(NameConstants.JCR_BASEVERSION).getNode();
     }
 
     /**
@@ -1147,7 +1123,7 @@ public class NodeImpl extends ItemImpl implements Node {
     public boolean holdsLock() throws RepositoryException {
         // lock can be inherited from a parent > do not check for node being lockable.
         checkStatus();
-        if (isNew() || !isNodeType(QName.MIX_LOCKABLE)) {
+        if (isNew() || !isNodeType(NameConstants.MIX_LOCKABLE)) {
             // a node that is new or not lockable never holds a lock
             return false;
         } else {
@@ -1172,13 +1148,13 @@ public class NodeImpl extends ItemImpl implements Node {
      * @return
      * @throws RepositoryException
      */
-    boolean isNodeType(QName qName) throws RepositoryException {
+    boolean isNodeType(Name qName) throws RepositoryException {
         // first do trivial checks without using type hierarchy
         if (qName.equals(primaryTypeName)) {
             return true;
         }
         // check if contained in mixin types
-        QName[] mixins = getNodeState().getMixinTypeNames();
+        Name[] mixins = getNodeState().getMixinTypeNames();
         for (int i = 0; i < mixins.length; i++) {
             if (mixins[i].equals(qName)) {
                 return true;
@@ -1201,15 +1177,15 @@ public class NodeImpl extends ItemImpl implements Node {
 
     //-----------------------------------------------------------< ItemImpl >---
     /**
-     * @see ItemImpl#getQName()
+     * @see ItemImpl#getName()
      */
-    QName getQName() throws RepositoryException {
+    Name getQName() throws RepositoryException {
         if (getNodeState().isRoot()) {
             // shortcut. the given state represents the root or an orphaned node
-            return QName.ROOT;
+            return NameConstants.ROOT;
         }
 
-        return getNodeState().getQName();
+        return getNodeState().getName();
     }
 
 
@@ -1252,7 +1228,7 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     private void checkIsLockable() throws UnsupportedRepositoryOperationException, RepositoryException {
         checkStatus();
-        if (!isNodeType(QName.MIX_LOCKABLE)) {
+        if (!isNodeType(NameConstants.MIX_LOCKABLE)) {
             String msg = "Unable to perform locking operation on non-lockable node: " + getPath();
             log.debug(msg);
             throw new LockException(msg);
@@ -1282,7 +1258,7 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     private void checkIsVersionable() throws UnsupportedRepositoryOperationException, RepositoryException {
         checkStatus();
-        if (!isNodeType(QName.MIX_VERSIONABLE)) {
+        if (!isNodeType(NameConstants.MIX_VERSIONABLE)) {
             String msg = "Unable to perform versioning operation on non versionable node: " + getPath();
             log.debug(msg);
             throw new UnsupportedRepositoryOperationException(msg);
@@ -1305,7 +1281,7 @@ public class NodeImpl extends ItemImpl implements Node {
      * @throws LockException
      * @throws RepositoryException
      */
-    private synchronized Node createNode(QName nodeName, QName nodeTypeName)
+    private synchronized Node createNode(Name nodeName, Name nodeTypeName)
         throws ItemExistsException, NoSuchNodeTypeException, VersionException,
         ConstraintViolationException, LockException, RepositoryException {
 
@@ -1340,7 +1316,7 @@ public class NodeImpl extends ItemImpl implements Node {
      * @throws RepositoryException
      */
     // TODO: protected due to usage within VersionImpl, VersionHistoryImpl (check for alternatives)
-    protected Property getProperty(QName qName) throws PathNotFoundException, RepositoryException {
+    protected Property getProperty(Name qName) throws PathNotFoundException, RepositoryException {
         checkStatus();
         try {
             PropertyEntry pEntry = getNodeEntry().getPropertyEntry(qName, true);
@@ -1364,7 +1340,7 @@ public class NodeImpl extends ItemImpl implements Node {
      * could be found.
      * @throws RepositoryException if another error occurs.
      */
-    private Property createProperty(QName qName, Value value, int type)
+    private Property createProperty(Name qName, Value value, int type)
             throws ConstraintViolationException, RepositoryException {
         QPropertyDefinition def = getApplicablePropertyDefinition(qName, type, false);
         int targetType = def.getRequiredType();
@@ -1373,11 +1349,11 @@ public class NodeImpl extends ItemImpl implements Node {
         }
         QValue qvs;
         if (targetType == PropertyType.UNDEFINED) {
-            qvs = ValueFormat.getQValue(value, session.getNamespaceResolver(), session.getQValueFactory());
+            qvs = ValueFormat.getQValue(value, session.getNamePathResolver(), session.getQValueFactory());
             targetType = qvs.getType();
         } else {
             Value targetValue = ValueHelper.convert(value, targetType, session.getValueFactory());
-            qvs = ValueFormat.getQValue(targetValue, session.getNamespaceResolver(), session.getQValueFactory());
+            qvs = ValueFormat.getQValue(targetValue, session.getNamePathResolver(), session.getQValueFactory());
         }
         return createProperty(qName, targetType, def, new QValue[] {qvs});
     }
@@ -1392,7 +1368,7 @@ public class NodeImpl extends ItemImpl implements Node {
      * @throws ConstraintViolationException
      * @throws RepositoryException
      */
-    private Property createProperty(QName qName, Value[] values, int type)
+    private Property createProperty(Name qName, Value[] values, int type)
         throws ConstraintViolationException, RepositoryException {
         QPropertyDefinition def = getApplicablePropertyDefinition(qName, type, true);
         int targetType = def.getRequiredType();
@@ -1417,7 +1393,7 @@ public class NodeImpl extends ItemImpl implements Node {
             }
         }
         Value[] targetValues = ValueHelper.convert(values, targetType, session.getValueFactory());
-        QValue[] qvs = ValueFormat.getQValues(targetValues, session.getNamespaceResolver(), session.getQValueFactory());
+        QValue[] qvs = ValueFormat.getQValues(targetValues, session.getNamePathResolver(), session.getQValueFactory());
         return createProperty(qName, targetType, def, qvs);
     }
 
@@ -1432,7 +1408,7 @@ public class NodeImpl extends ItemImpl implements Node {
      * @throws ConstraintViolationException
      * @throws RepositoryException
      */
-    private Property createProperty(QName qName, int type, QPropertyDefinition def,
+    private Property createProperty(Name qName, int type, QPropertyDefinition def,
                                     QValue[] qvs)
         throws ConstraintViolationException, RepositoryException {
         Operation op = AddProperty.create(getNodeState(), qName, type, def, qvs);
@@ -1446,17 +1422,17 @@ public class NodeImpl extends ItemImpl implements Node {
      * @return
      * @throws RepositoryException
      */
-    private QName getQName(String jcrName) throws RepositoryException {
-        QName qName;
+    private Name getQName(String jcrName) throws RepositoryException {
+        Name qName;
         try {
-            qName = NameFormat.parse(jcrName, session.getNamespaceResolver());
+            qName = session.getNameResolver().getQName(jcrName);
         } catch (NameException upe) {
             throw new RepositoryException("invalid name: "+ jcrName, upe);
         }
         return qName;
     }
 
-    private boolean canAddMixin(QName mixinName) throws NoSuchNodeTypeException,
+    private boolean canAddMixin(Name mixinName) throws NoSuchNodeTypeException,
         NodeTypeConflictException {
         NodeTypeManagerImpl ntMgr = session.getNodeTypeManager();
 
@@ -1473,7 +1449,7 @@ public class NodeImpl extends ItemImpl implements Node {
         }
 
         // get list of existing nodetypes
-        QName[] existingNts = getNodeState().getNodeTypeNames();
+        Name[] existingNts = getNodeState().getNodeTypeNames();
         // build effective node type representing primary type including existing mixin's
         EffectiveNodeType entExisting = session.getEffectiveNodeTypeProvider().getEffectiveNodeType(existingNts);
 
@@ -1485,7 +1461,7 @@ public class NodeImpl extends ItemImpl implements Node {
 
         // second, build new effective node type for nts including the new mixin
         // types, detecting eventual incompatibilities
-        QName[] resultingNts = new QName[existingNts.length + 1];
+        Name[] resultingNts = new Name[existingNts.length + 1];
         System.arraycopy(existingNts, 0, resultingNts, 0, existingNts.length);
         resultingNts[existingNts.length] = mixinName;
         session.getEffectiveNodeTypeProvider().getEffectiveNodeType(resultingNts);
@@ -1516,12 +1492,12 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     private Path getReorderPath(String relativePath) throws RepositoryException {
         try {
-            Path p = PathFormat.parse(relativePath, session.getNamespaceResolver());
+            Path p = session.getPathResolver().getQPath(relativePath);
             if (p.isAbsolute() || p.getLength() != 1 || p.getDepth() != 1) {
                 throw new RepositoryException("Invalid relative path: " + relativePath);
             }
             return p;
-        } catch (MalformedPathException e) {
+        } catch (NameException e) {
             String msg = "Invalid relative path: " + relativePath;
             log.debug(msg);
             throw new RepositoryException(msg, e);
@@ -1536,9 +1512,9 @@ public class NodeImpl extends ItemImpl implements Node {
      */
     private Path getQPath(String relativePath) throws RepositoryException {
         try {
-            Path p = PathFormat.parse(relativePath, session.getNamespaceResolver());
+            Path p = session.getPathResolver().getQPath(relativePath);
             return getQPath(p);
-        } catch (MalformedPathException e) {
+        } catch (NameException e) {
             String msg = "Invalid relative path: " + relativePath;
             log.debug(msg);
             throw new RepositoryException(msg, e);
@@ -1552,17 +1528,11 @@ public class NodeImpl extends ItemImpl implements Node {
      * @throws RepositoryException
      */
     private Path getQPath(Path relativePath) throws RepositoryException {
-        try {
-            // shortcut
-            if (relativePath.getLength() == 1 && relativePath.getNameElement() == Path.CURRENT_ELEMENT) {
-                return getQPath();
-            }
-            return Path.create(getQPath(), relativePath, true);
-        } catch (MalformedPathException e) {
-            String msg = "Invalid relative path: " + relativePath;
-            log.debug(msg);
-            throw new RepositoryException(msg, e);
+        // shortcut
+        if (relativePath.getLength() == 1 && relativePath.getNameElement() == session.getPathFactory().getCurrentElement()) {
+            return getQPath();
         }
+        return session.getPathFactory().create(getQPath(), relativePath, true);
     }
 
     /**
@@ -1580,13 +1550,13 @@ public class NodeImpl extends ItemImpl implements Node {
     private NodeEntry resolveRelativeNodePath(String relPath) throws RepositoryException {
         NodeEntry targetEntry = null;
         try {
-            Path rp = PathFormat.parse(relPath, session.getNamespaceResolver());
+            Path rp = session.getPathResolver().getQPath(relPath);
             // shortcut
             if (rp.getLength() == 1) {
-                Path.PathElement pe = rp.getNameElement();
-                if (pe == Path.CURRENT_ELEMENT) {
+                Path.Element pe = rp.getNameElement();
+                if (pe.denotesCurrent()) {
                     targetEntry = getNodeEntry();
-                } else if (pe == Path.PARENT_ELEMENT) {
+                } else if (pe.denotesParent()) {
                     targetEntry = getNodeEntry().getParent();
                 } else {
                     // try to get child entry + force loading of not known yet
@@ -1602,7 +1572,7 @@ public class NodeImpl extends ItemImpl implements Node {
             }
         } catch (PathNotFoundException e) {
             // item does not exist -> ignore and return null
-        } catch (MalformedPathException e) {
+        } catch (org.apache.jackrabbit.conversion.NameException e) {
             String msg = "Invalid relative path: " + relPath;
             log.debug(msg);
             throw new RepositoryException(msg, e);
@@ -1625,12 +1595,12 @@ public class NodeImpl extends ItemImpl implements Node {
     private PropertyEntry resolveRelativePropertyPath(String relPath) throws RepositoryException {
         PropertyEntry targetEntry = null;
         try {
-            Path rp = PathFormat.parse(relPath, session.getNamespaceResolver());
+            Path rp = session.getPathResolver().getQPath(relPath);
             if (rp.getLength() == 1 && rp.getNameElement().denotesName()) {
                 // a single path element must always denote a name. '.' and '..'
                 // will never point to a property. If the NodeEntry does not
                 // contain such a pe, the targetEntry is 'null;
-                QName propName = rp.getNameElement().getName();
+                Name propName = rp.getNameElement().getName();
                 // check if property entry exists
                 targetEntry = getNodeEntry().getPropertyEntry(propName, true);
             } else {
@@ -1645,7 +1615,7 @@ public class NodeImpl extends ItemImpl implements Node {
                     // ignore -> return null;
                 }
             }
-        } catch (MalformedPathException e) {
+        } catch (org.apache.jackrabbit.conversion.NameException e) {
             String msg = "failed to resolve property path " + relPath + " relative to " + safeGetJCRPath();
             log.debug(msg);
             throw new RepositoryException(msg, e);
@@ -1665,7 +1635,7 @@ public class NodeImpl extends ItemImpl implements Node {
      *                                      could be found
      * @throws RepositoryException          if another error occurs
      */
-    private QPropertyDefinition getApplicablePropertyDefinition(QName propertyName,
+    private QPropertyDefinition getApplicablePropertyDefinition(Name propertyName,
                                                                 int type,
                                                                 boolean multiValued)
             throws ConstraintViolationException, RepositoryException {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/PropertyImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/PropertyImpl.java
index 9d9f3f8..762c440 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/PropertyImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/PropertyImpl.java
@@ -19,14 +19,13 @@ package org.apache.jackrabbit.jcr2spi;
 import org.apache.jackrabbit.jcr2spi.state.PropertyState;
 import org.apache.jackrabbit.jcr2spi.operation.SetPropertyValue;
 import org.apache.jackrabbit.jcr2spi.operation.Operation;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.value.ValueFormat;
 import org.apache.jackrabbit.value.ValueHelper;
+import org.apache.jackrabbit.conversion.NameResolver;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -66,15 +65,8 @@ public class PropertyImpl extends ItemImpl implements Property {
      */
     public String getName() throws RepositoryException {
         checkStatus();
-        QName name = getQName();
-        try {
-            return NameFormat.format(name, session.getNamespaceResolver());
-        } catch (NoPrefixDeclaredException npde) {
-            // should never get here...
-            String msg = "Internal error: encountered unregistered namespace " + name.getNamespaceURI();
-            log.debug(msg);
-            throw new RepositoryException(msg, npde);
-        }
+        Name name = getQName();
+        return session.getNameResolver().getJCRName(name);
     }
 
     /**
@@ -140,7 +132,7 @@ public class PropertyImpl extends ItemImpl implements Property {
         QValue[] qValues = null;
         if (values != null) {
             Value[] vs = ValueHelper.convert(values, targetType, session.getValueFactory());
-            qValues = ValueFormat.getQValues(vs, session.getNamespaceResolver(), session.getQValueFactory());
+            qValues = ValueFormat.getQValues(vs, session.getNamePathResolver(), session.getQValueFactory());
         }
         setInternalValues(qValues, targetType);
     }
@@ -176,7 +168,7 @@ public class PropertyImpl extends ItemImpl implements Property {
                     if (reqType != PropertyType.STRING) {
                         // type conversion required
                         Value v = ValueHelper.convert(string, reqType, session.getValueFactory());
-                        qValue = ValueFormat.getQValue(v, session.getNamespaceResolver(), session.getQValueFactory());
+                        qValue = ValueFormat.getQValue(v, session.getNamePathResolver(), session.getQValueFactory());
                     } else {
                         // no type conversion required
                         qValue = session.getQValueFactory().create(string, PropertyType.STRING);
@@ -250,7 +242,7 @@ public class PropertyImpl extends ItemImpl implements Property {
         if (value == null) {
             setInternalValues(null, reqType);
         } else {
-            checkValidReference(value, reqType, session.getNamespaceResolver());
+            checkValidReference(value, reqType, session.getNameResolver());
             QValue qValue = session.getQValueFactory().create(value.getUUID(), PropertyType.REFERENCE);
             setInternalValues(new QValue[]{qValue}, reqType);
         }
@@ -261,7 +253,7 @@ public class PropertyImpl extends ItemImpl implements Property {
      */
     public Value getValue() throws ValueFormatException, RepositoryException {
         QValue value = getQValue();
-        return ValueFormat.getJCRValue(value, session.getNamespaceResolver(), session.getJcrValueFactory());
+        return ValueFormat.getJCRValue(value, session.getNamePathResolver(), session.getJcrValueFactory());
     }
 
     /**
@@ -271,7 +263,7 @@ public class PropertyImpl extends ItemImpl implements Property {
         QValue[] qValues = getQValues();
         Value[] values = new Value[qValues.length];
         for (int i = 0; i < qValues.length; i++) {
-            values[i] = ValueFormat.getJCRValue(qValues[i], session.getNamespaceResolver(), session.getJcrValueFactory());
+            values[i] = ValueFormat.getJCRValue(qValues[i], session.getNamePathResolver(), session.getJcrValueFactory());
         }
         return values;
     }
@@ -360,7 +352,7 @@ public class PropertyImpl extends ItemImpl implements Property {
         switch (value.getType()) {
             case PropertyType.NAME:
             case PropertyType.PATH:
-                Value jcrValue = ValueFormat.getJCRValue(value, session.getNamespaceResolver(), session.getJcrValueFactory());
+                Value jcrValue = ValueFormat.getJCRValue(value, session.getNamePathResolver(), session.getJcrValueFactory());
                 length = jcrValue.getString().length();
                 break;
             default:
@@ -389,14 +381,14 @@ public class PropertyImpl extends ItemImpl implements Property {
 
     //-----------------------------------------------------------< ItemImpl >---
     /**
-     * Returns the QName defined with this <code>PropertyState</code>
+     * Returns the Name defined with this <code>PropertyState</code>
      *
      * @return
-     * @see PropertyState#getQName()
-     * @see ItemImpl#getQName()
+     * @see PropertyState#getName()
+     * @see ItemImpl#getName()
      */
-    QName getQName() {
-        return getPropertyState().getQName();
+    Name getQName() {
+        return getPropertyState().getName();
     }
 
     //------------------------------------------------------< check methods >---
@@ -492,10 +484,10 @@ public class PropertyImpl extends ItemImpl implements Property {
         if (requiredType != value.getType()) {
             // type conversion required
             Value v = ValueHelper.convert(value, requiredType, session.getValueFactory());
-            qValue = ValueFormat.getQValue(v, session.getNamespaceResolver(), session.getQValueFactory());
+            qValue = ValueFormat.getQValue(v, session.getNamePathResolver(), session.getQValueFactory());
         } else {
             // no type conversion required
-            qValue = ValueFormat.getQValue(value, session.getNamespaceResolver(), session.getQValueFactory());
+            qValue = ValueFormat.getQValue(value, session.getNamePathResolver(), session.getQValueFactory());
         }
         setInternalValues(new QValue[]{qValue}, requiredType);
     }
@@ -535,15 +527,11 @@ public class PropertyImpl extends ItemImpl implements Property {
      * @throws ValueFormatException
      * @throws RepositoryException
      */
-    static void checkValidReference(Node value, int propertyType, NamespaceResolver nsResolver) throws ValueFormatException, RepositoryException {
+    static void checkValidReference(Node value, int propertyType, NameResolver resolver) throws ValueFormatException, RepositoryException {
         if (propertyType == PropertyType.REFERENCE) {
-            try {
-                String jcrName = NameFormat.format(QName.MIX_REFERENCEABLE, nsResolver);
-                if (!value.isNodeType(jcrName)) {
-                    throw new ValueFormatException("Target node must be of node type mix:referenceable");
-                }
-            } catch (NoPrefixDeclaredException e) {
-                throw new RepositoryException(e);
+            String jcrName = resolver.getJCRName(NameConstants.MIX_REFERENCEABLE);
+            if (!value.isNodeType(jcrName)) {
+                throw new ValueFormatException("Target node must be of node type mix:referenceable");
             }
         } else {
             throw new ValueFormatException("Property must be of type REFERENCE.");
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/SessionImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/SessionImpl.java
index 742e81f..c7cf2b9 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/SessionImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/SessionImpl.java
@@ -41,18 +41,21 @@ import org.apache.jackrabbit.jcr2spi.operation.Operation;
 import org.apache.jackrabbit.jcr2spi.name.LocalNamespaceMappings;
 import org.apache.jackrabbit.jcr2spi.config.RepositoryConfig;
 import org.apache.jackrabbit.jcr2spi.config.CacheBehaviour;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.jackrabbit.spi.SessionInfo;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.IdFactory;
 import org.apache.jackrabbit.spi.XASessionInfo;
 import org.apache.jackrabbit.spi.QValueFactory;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.PathFactory;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.PathResolver;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.conversion.DefaultNamePathResolver;
 import org.apache.commons.collections.map.ReferenceMap;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
@@ -118,6 +121,7 @@ public class SessionImpl implements Session, ManagerProvider {
     private final SessionInfo sessionInfo;
 
     private final LocalNamespaceMappings nsMappings;
+    private final NamePathResolver npResolver;
     private final NodeTypeManagerImpl ntManager;
 
     private final SessionItemStateManager itemStateManager;
@@ -136,10 +140,11 @@ public class SessionImpl implements Session, ManagerProvider {
 
         // build local name-mapping
         nsMappings = new LocalNamespaceMappings(workspace.getNamespaceRegistryImpl());
+        npResolver = new DefaultNamePathResolver(nsMappings, true);
 
         // build nodetype manager
         ntManager = new NodeTypeManagerImpl(workspace.getNodeTypeRegistry(), this, getJcrValueFactory());
-        validator = new ItemStateValidator(this);
+        validator = new ItemStateValidator(this, getPathFactory());
 
         itemStateManager = createSessionItemStateManager(workspace.getUpdatableItemStateManager(), workspace.getItemStateFactory());
         itemManager = createItemManager(getHierarchyManager());
@@ -220,18 +225,13 @@ public class SessionImpl implements Session, ManagerProvider {
     public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
         // sanity check performed by getNodeById
         Node node = getNodeById(getIdFactory().createNodeId(uuid));
-        if (node instanceof NodeImpl && ((NodeImpl)node).isNodeType(QName.MIX_REFERENCEABLE)) {
+        if (node instanceof NodeImpl && ((NodeImpl)node).isNodeType(NameConstants.MIX_REFERENCEABLE)) {
             return node;
         } else {
             // fall back
-            try {
-                String mixReferenceable = NameFormat.format(QName.MIX_REFERENCEABLE, getNamespaceResolver());
-                if (node.isNodeType(mixReferenceable)) {
-                    return node;
-                }
-            } catch (NoPrefixDeclaredException e) {
-                // should not occur.
-                throw new RepositoryException(e);
+            String mixReferenceable = getNameResolver().getJCRName(NameConstants.MIX_REFERENCEABLE);
+            if (node.isNodeType(mixReferenceable)) {
+                return node;
             }
             // there is a node with that uuid but the node does not expose it
             throw new ItemNotFoundException(uuid);
@@ -274,8 +274,6 @@ public class SessionImpl implements Session, ManagerProvider {
             return getItemManager().getItem(qPath.getNormalizedPath());
         } catch (AccessDeniedException ade) {
             throw new PathNotFoundException(absPath);
-        } catch (MalformedPathException e) {
-            throw new RepositoryException(e);
         }
     }
 
@@ -284,12 +282,8 @@ public class SessionImpl implements Session, ManagerProvider {
      */
     public boolean itemExists(String absPath) throws RepositoryException {
         checkIsAlive();
-        try {
-            Path qPath = getQPath(absPath);
-            return getItemManager().itemExists(qPath.getNormalizedPath());
-        } catch (MalformedPathException e) {
-            throw new RepositoryException(e);
-        }
+        Path qPath = getQPath(absPath);
+        return getItemManager().itemExists(qPath.getNormalizedPath());
     }
 
     /**
@@ -304,7 +298,7 @@ public class SessionImpl implements Session, ManagerProvider {
         Path destPath = getQPath(destAbsPath);
 
         // all validation is performed by Move Operation and state-manager
-        Operation op = Move.create(srcPath, destPath, getHierarchyManager(), getNamespaceResolver(), true);
+        Operation op = Move.create(srcPath, destPath, getHierarchyManager(), getPathResolver(), true);
         itemStateManager.execute(op);
     }
 
@@ -371,13 +365,8 @@ public class SessionImpl implements Session, ManagerProvider {
                 }
             }
             // parentState is the nearest existing nodeState or the root state.
-            try {
-                Path relPath = parentPath.computeRelativePath(targetPath);
-                isGranted = getAccessManager().isGranted(parentState, relPath, actionsArr);
-            } catch (MalformedPathException e) {
-                // should not occurs
-                throw new RepositoryException(e);
-            }
+            Path relPath = parentPath.computeRelativePath(targetPath);
+            isGranted = getAccessManager().isGranted(parentState, relPath, actionsArr);
         }
 
         if (!isGranted) {
@@ -396,7 +385,7 @@ public class SessionImpl implements Session, ManagerProvider {
         // NOTE: check if path corresponds to Node and is writable is performed
         // within the SessionImporter.
         Importer importer = new SessionImporter(parentPath, this, itemStateManager, uuidBehavior);
-        return new ImportHandler(importer, getNamespaceResolver(), workspace.getNamespaceRegistry());
+        return new ImportHandler(importer, getNamespaceResolver(), workspace.getNamespaceRegistry(), getNameFactory());
     }
 
     /**
@@ -650,6 +639,25 @@ public class SessionImpl implements Session, ManagerProvider {
     }
 
     //---------------------------------------------------< ManagerProvider > ---
+
+    public NamePathResolver getNamePathResolver() {
+        return npResolver;
+    }
+
+    /**
+     * @see ManagerProvider#getNameResolver()
+     */
+    public NameResolver getNameResolver() {
+        return npResolver;
+    }
+
+    /**
+     * @see ManagerProvider#getPathResolver()
+     */
+    public PathResolver getPathResolver() {
+        return npResolver;
+    }
+
     /**
      * @see ManagerProvider#getNamespaceResolver()
      */
@@ -729,10 +737,18 @@ public class SessionImpl implements Session, ManagerProvider {
     }
 
     // TODO public for SessionImport only. review
-    public IdFactory getIdFactory() {
+    public IdFactory getIdFactory() throws RepositoryException {
         return workspace.getIdFactory();
     }
 
+    public NameFactory getNameFactory() throws RepositoryException {
+        return workspace.getNameFactory();
+    }
+
+    PathFactory getPathFactory() throws RepositoryException {
+        return workspace.getPathFactory();
+    }
+
     /**
      * Returns the <code>ItemStateManager</code> associated with this session.
      *
@@ -773,12 +789,12 @@ public class SessionImpl implements Session, ManagerProvider {
      */
     Path getQPath(String absPath) throws RepositoryException {
         try {
-            Path p = PathFormat.parse(absPath, getNamespaceResolver());
+            Path p = getPathResolver().getQPath(absPath);
             if (!p.isAbsolute()) {
                 throw new RepositoryException("Not an absolute path: " + absPath);
             }
             return p;
-        } catch (MalformedPathException mpe) {
+        } catch (NameException mpe) {
             String msg = "Invalid path: " + absPath;
             log.debug(msg);
             throw new RepositoryException(msg, mpe);
@@ -791,7 +807,7 @@ public class SessionImpl implements Session, ManagerProvider {
      * was obtained from a different session, the 'corresponding' version
      * state for this session is retrieved.
      *
-     * @param node
+     * @param version
      * @return
      */
     NodeState getVersionState(Version version) throws RepositoryException {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java
index e9d32b3..52e0183 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java
@@ -16,7 +16,6 @@
  */
 package org.apache.jackrabbit.jcr2spi;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManager;
 import org.apache.jackrabbit.jcr2spi.state.UpdatableItemStateManager;
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
@@ -46,7 +45,13 @@ import org.apache.jackrabbit.spi.IdFactory;
 import org.apache.jackrabbit.spi.RepositoryService;
 import org.apache.jackrabbit.spi.SessionInfo;
 import org.apache.jackrabbit.spi.QValueFactory;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.PathFactory;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.conversion.PathResolver;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 import org.xml.sax.ContentHandler;
@@ -233,7 +238,7 @@ public class WorkspaceImpl implements Workspace, ManagerProvider {
         Path srcPath = session.getQPath(srcAbsPath);
         Path destPath = session.getQPath(destAbsPath);
 
-        Operation op = Move.create(srcPath, destPath, getHierarchyManager(), getNamespaceResolver(), false);
+        Operation op = Move.create(srcPath, destPath, getHierarchyManager(), getPathResolver(), false);
         getUpdatableItemStateManager().execute(op);
     }
 
@@ -257,7 +262,8 @@ public class WorkspaceImpl implements Workspace, ManagerProvider {
         session.checkIsAlive();
         if (qManager == null) {
             qManager = new QueryManagerImpl(session, session.getLocalNamespaceMappings(),
-                session.getItemManager(), session.getHierarchyManager(), wspManager);
+                    session.getNamePathResolver(), session.getItemManager(),
+                    session.getHierarchyManager(), wspManager);
         }
         return qManager;
     }
@@ -286,7 +292,7 @@ public class WorkspaceImpl implements Workspace, ManagerProvider {
         session.checkIsAlive();
 
         if (obsManager == null) {
-            obsManager = createObservationManager(getNamespaceResolver(), getNodeTypeRegistry());
+            obsManager = createObservationManager(getNamePathResolver(), getNodeTypeRegistry());
         }
         return obsManager;
     }
@@ -351,6 +357,27 @@ public class WorkspaceImpl implements Workspace, ManagerProvider {
 
     //----------------------------------------------------< ManagerProvider >---
     /**
+     * @see ManagerProvider#getNamePathResolver()
+     */
+    public org.apache.jackrabbit.conversion.NamePathResolver getNamePathResolver() {
+        return session.getNamePathResolver();
+    }
+
+    /**
+     * @see ManagerProvider#getNameResolver()
+     */
+    public NameResolver getNameResolver() {
+        return session.getNameResolver();
+    }
+
+    /**
+     * @see ManagerProvider#getPathResolver()
+     */
+    public PathResolver getPathResolver() {
+        return session.getPathResolver();
+    }
+
+    /**
      * @see ManagerProvider#getNamespaceResolver()
      */
     public NamespaceResolver getNamespaceResolver() {
@@ -415,7 +442,15 @@ public class WorkspaceImpl implements Workspace, ManagerProvider {
         // NOTE: wspManager has already been disposed upon SessionItemStateManager.dispose()
     }
 
-    IdFactory getIdFactory() {
+    NameFactory getNameFactory() throws RepositoryException {
+        return wspManager.getNameFactory();
+    }
+
+    PathFactory getPathFactory() throws RepositoryException {
+        return wspManager.getPathFactory();
+    }
+
+    IdFactory getIdFactory() throws RepositoryException {
         return wspManager.getIdFactory();
     }
 
@@ -493,7 +528,7 @@ public class WorkspaceImpl implements Workspace, ManagerProvider {
      *
      * @return a new <code>ObservationManager</code> instance
      */
-    protected ObservationManager createObservationManager(NamespaceResolver nsResolver, NodeTypeRegistry ntRegistry) {
-        return new ObservationManagerImpl(wspManager, nsResolver, ntRegistry);
+    protected ObservationManager createObservationManager(NamePathResolver resolver, NodeTypeRegistry ntRegistry) {
+        return new ObservationManagerImpl(wspManager, resolver, ntRegistry);
     }
 }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceManager.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceManager.java
index ecce6eb..060c4f4 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceManager.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceManager.java
@@ -65,8 +65,8 @@ import org.apache.jackrabbit.jcr2spi.config.CacheBehaviour;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyEventListener;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManager;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManagerImpl;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.RepositoryService;
 import org.apache.jackrabbit.spi.SessionInfo;
 import org.apache.jackrabbit.spi.NodeId;
@@ -81,6 +81,8 @@ import org.apache.jackrabbit.spi.EventFilter;
 import org.apache.jackrabbit.spi.QNodeTypeDefinition;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.Event;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.PathFactory;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -195,10 +197,18 @@ public class WorkspaceManager implements UpdatableItemStateManager, NamespaceSto
         return service.getWorkspaceNames(sessionInfo);
     }
 
-    public IdFactory getIdFactory() {
+    public IdFactory getIdFactory() throws RepositoryException {
         return idFactory;
     }
 
+    public NameFactory getNameFactory() throws RepositoryException {
+        return service.getNameFactory();
+    }
+
+    public PathFactory getPathFactory() throws RepositoryException {
+        return service.getPathFactory();
+    }
+
     public ItemStateFactory getItemStateFactory() {
         return isf;
     }
@@ -336,7 +346,7 @@ public class WorkspaceManager implements UpdatableItemStateManager, NamespaceSto
      *          if this implementation does not support observation.
      */
     public EventFilter createEventFilter(int eventTypes, Path path, boolean isDeep,
-                                         String[] uuids, QName[] nodeTypes,
+                                         String[] uuids, Name[] nodeTypes,
                                          boolean noLocal)
         throws UnsupportedRepositoryOperationException, RepositoryException {
         return service.createEventFilter(sessionInfo, eventTypes, path, isDeep, uuids, nodeTypes, noLocal);
@@ -356,8 +366,8 @@ public class WorkspaceManager implements UpdatableItemStateManager, NamespaceSto
      *
      * @return
      */
-    private HierarchyManager createHierarchyManager(TransientItemStateFactory tisf, IdFactory idFactory) {
-        return new HierarchyManagerImpl(tisf, idFactory);
+    private HierarchyManager createHierarchyManager(TransientItemStateFactory tisf, IdFactory idFactory) throws RepositoryException {
+        return new HierarchyManagerImpl(tisf, idFactory, getPathFactory());
     }
 
     /**
@@ -374,8 +384,8 @@ public class WorkspaceManager implements UpdatableItemStateManager, NamespaceSto
      * @param nsCache the namespace cache.
      * @return
      */
-    private NamespaceRegistryImpl createNamespaceRegistry(NamespaceCache nsCache) {
-        return new NamespaceRegistryImpl(this, nsCache);
+    private NamespaceRegistryImpl createNamespaceRegistry(NamespaceCache nsCache) throws RepositoryException {
+        return new NamespaceRegistryImpl(this, nsCache, getNameFactory(), getPathFactory());
     }
 
     /**
@@ -388,7 +398,7 @@ public class WorkspaceManager implements UpdatableItemStateManager, NamespaceSto
             public Iterator getAllDefinitions() throws RepositoryException {
                 return service.getQNodeTypeDefinitions(sessionInfo);
             }
-            public Iterator getDefinitions(QName[] nodeTypeNames) throws NoSuchNodeTypeException, RepositoryException {
+            public Iterator getDefinitions(Name[] nodeTypeNames) throws NoSuchNodeTypeException, RepositoryException {
                 return service.getQNodeTypeDefinitions(sessionInfo, nodeTypeNames);
             }
             public void registerNodeTypes(QNodeTypeDefinition[] nodeTypeDefs) throws NoSuchNodeTypeException, RepositoryException {
@@ -397,7 +407,7 @@ public class WorkspaceManager implements UpdatableItemStateManager, NamespaceSto
             public void reregisterNodeTypes(QNodeTypeDefinition[] nodeTypeDefs) throws NoSuchNodeTypeException, RepositoryException {
                 throw new UnsupportedOperationException("NodeType registration not yet defined by the SPI");
             }
-            public void unregisterNodeTypes(QName[] nodeTypeNames) throws NoSuchNodeTypeException, RepositoryException {
+            public void unregisterNodeTypes(Name[] nodeTypeNames) throws NoSuchNodeTypeException, RepositoryException {
                 throw new UnsupportedOperationException("NodeType registration not yet defined by the SPI");
             }
         };
@@ -640,7 +650,7 @@ public class WorkspaceManager implements UpdatableItemStateManager, NamespaceSto
                         default:
                             type = "Unknown";
                     }
-                    log.debug("  {}; {}", e.getQPath(), type);
+                    log.debug("  {}; {}", e.getPath(), type);
                 }
             }
         }
@@ -722,7 +732,7 @@ public class WorkspaceManager implements UpdatableItemStateManager, NamespaceSto
          */
         public void visit(AddProperty operation) throws RepositoryException {
             NodeId parentId = operation.getParentId();
-            QName propertyName = operation.getPropertyName();
+            Name propertyName = operation.getPropertyName();
             if (operation.isMultiValued()) {
                 batch.addProperty(parentId, propertyName, operation.getValues());
             } else {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeAttic.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeAttic.java
index 84fbcb6..4397967 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeAttic.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeAttic.java
@@ -18,7 +18,7 @@ package org.apache.jackrabbit.jcr2spi.hierarchy;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 
 import java.util.Set;
 import java.util.HashSet;
@@ -38,7 +38,7 @@ class ChildNodeAttic {
     ChildNodeAttic() {
     }
 
-    boolean contains(QName name, int index) {
+    boolean contains(Name name, int index) {
         for (Iterator it = attic.iterator(); it.hasNext();) {
             NodeEntryImpl ne = (NodeEntryImpl) it.next();
             if (ne.matches(name, index)) {
@@ -48,7 +48,7 @@ class ChildNodeAttic {
         return false;
     }
 
-    List get(QName name) {
+    List get(Name name) {
         List l = new ArrayList();
         for (Iterator it = attic.iterator(); it.hasNext();) {
             NodeEntryImpl ne = (NodeEntryImpl) it.next();
@@ -65,7 +65,7 @@ class ChildNodeAttic {
      * @param index The original index of the NodeEntry before it has been moved.
      * @return
      */
-    NodeEntry get(QName name, int index) {
+    NodeEntry get(Name name, int index) {
         for (Iterator it = attic.iterator(); it.hasNext();) {
             NodeEntryImpl ne = (NodeEntryImpl) it.next();
             if (ne.matches(name, index)) {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntries.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntries.java
index 81db90c..44fe610 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntries.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntries.java
@@ -16,8 +16,8 @@
  */
 package org.apache.jackrabbit.jcr2spi.hierarchy;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.ChildInfo;
 
 import javax.jcr.ItemNotFoundException;
@@ -74,7 +74,7 @@ public interface ChildNodeEntries {
      * @param nodeName the child node name.
      * @return same name sibling nodes with the given <code>nodeName</code>.
      */
-    List get(QName nodeName);
+    List get(Name nodeName);
 
     /**
      * Returns the <code>NodeEntry</code> with the given
@@ -86,7 +86,7 @@ public interface ChildNodeEntries {
      * @return the <code>NodeEntry</code> or <code>null</code> if there
      * is no such <code>NodeEntry</code>.
      */
-    NodeEntry get(QName nodeName, int index);
+    NodeEntry get(Name nodeName, int index);
 
     /**
      * Return the <code>NodeEntry</code> that matches the given nodeName and
@@ -97,7 +97,7 @@ public interface ChildNodeEntries {
      * @return
      * @throws IllegalArgumentException if the given uniqueID is null.
      */
-    NodeEntry get(QName nodeName, String uniqueID);
+    NodeEntry get(Name nodeName, String uniqueID);
 
     /**
      * Find the matching NodeEntry for the given <code>ChildInfo</code>. Returns
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntriesImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntriesImpl.java
index a405aac..708931a 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntriesImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntriesImpl.java
@@ -18,8 +18,8 @@ package org.apache.jackrabbit.jcr2spi.hierarchy;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.ChildInfo;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.jcr2spi.state.Status;
@@ -172,16 +172,16 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
     }
 
     /**
-     * @see ChildNodeEntries#get(QName)
+     * @see ChildNodeEntries#get(Name)
      */
-    public List get(QName nodeName) {
+    public List get(Name nodeName) {
         return entriesByName.getList(nodeName);
     }
 
     /**
-     * @see ChildNodeEntries#get(QName, int)
+     * @see ChildNodeEntries#get(Name, int)
      */
-    public NodeEntry get(QName nodeName, int index) {
+    public NodeEntry get(Name nodeName, int index) {
         if (index < Path.INDEX_DEFAULT) {
             throw new IllegalArgumentException("index is 1-based");
         }
@@ -189,9 +189,9 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
     }
 
     /**
-     * @see ChildNodeEntries#get(QName, String)
+     * @see ChildNodeEntries#get(Name, String)
      */
-    public NodeEntry get(QName nodeName, String uniqueID) {
+    public NodeEntry get(Name nodeName, String uniqueID) {
         if (uniqueID == null || nodeName == null) {
             throw new IllegalArgumentException();
         }
@@ -253,7 +253,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
      * @return
      */
     private LinkedEntries.LinkNode internalAdd(NodeEntry entry, int index) {
-        QName nodeName = entry.getQName();
+        Name nodeName = entry.getName();
 
         // retrieve ev. sibling node with same index. if index is 'undefined'
         // the existing entry is always null and no reordering occurs.
@@ -295,7 +295,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
                 throw new NoSuchElementException();
             }
             LinkedEntries.LinkNode insertLN = internalAdd(entry, Path.INDEX_UNDEFINED);
-            reorder(entry.getQName(), insertLN, beforeLN);
+            reorder(entry.getName(), insertLN, beforeLN);
         } else {
             // 'before' is null -> simply append new entry at the end
             add(entry);
@@ -312,7 +312,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
     public synchronized NodeEntry remove(NodeEntry childEntry) {
         LinkedEntries.LinkNode ln = entries.removeNodeEntry(childEntry);
         if (ln != null) {
-            entriesByName.remove(childEntry.getQName(), ln);
+            entriesByName.remove(childEntry.getName(), ln);
             return childEntry;
         } else {
             return null;
@@ -348,7 +348,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
 
         NodeEntry previousBefore = insertLN.getNextLinkNode().getNodeEntry();
         if (previousBefore != beforeEntry) {
-            reorder(insertEntry.getQName(), insertLN, beforeLN);
+            reorder(insertEntry.getName(), insertLN, beforeLN);
         }
         return previousBefore;
     }
@@ -359,7 +359,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
      * @param insertLN
      * @param beforeLN
      */
-    private void reorder(QName insertName, LinkedEntries.LinkNode insertLN, LinkedEntries.LinkNode beforeLN) {
+    private void reorder(Name insertName, LinkedEntries.LinkNode insertLN, LinkedEntries.LinkNode beforeLN) {
         // reorder named map
         if (entriesByName.containsSiblings(insertName)) {
             int position;
@@ -375,7 +375,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
                     LinkedEntries.LinkNode ln = (LinkedEntries.LinkNode) it.next();
                     if (ln == beforeLN) {
                         break;
-                    } else if (ln != insertLN && ln.getNodeEntry().getQName().equals(insertName)) {
+                    } else if (ln != insertLN && ln.getNodeEntry().getName().equals(insertName)) {
                         position++;
                     } // else: ln == inserLN OR no SNS -> not relevant for position count
                 }
@@ -495,7 +495,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
          */
         private final class LinkNode extends Node {
 
-            private final QName qName;
+            private final Name qName;
 
             protected LinkNode() {
                 super();
@@ -504,7 +504,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
 
             protected LinkNode(Object value) {
                 super(new WeakReference(value));
-                qName = ((NodeEntry) value).getQName();
+                qName = ((NodeEntry) value).getName();
             }
 
             protected void setValue(Object value) {
@@ -582,7 +582,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
 
     //--------------------------------------------------------------------------
     /**
-     * Mapping of QName to LinkNode OR List of LinkNode(s) in case of SNSiblings.
+     * Mapping of Name to LinkNode OR List of LinkNode(s) in case of SNSiblings.
      */
     private static class NameMap {
 
@@ -595,7 +595,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
          * @param qName
          * @return
          */
-        public boolean containsSiblings(QName qName) {
+        public boolean containsSiblings(Name qName) {
             return snsMap.containsKey(qName);
         }
 
@@ -607,7 +607,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
          * @return a single <code>NodeEntry</code> or a <code>List</code> of
          * NodeEntry objects.
          */
-        private Object get(QName qName) {
+        private Object get(Name qName) {
             Object val = nameMap.get(qName);
             if (val != null) {
                 return ((LinkedEntries.LinkNode) val).getNodeEntry();
@@ -633,7 +633,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
          * @param qName
          * @return
          */
-        public List getList(QName qName) {
+        public List getList(Name qName) {
             Object obj = get(qName);
             if (obj == null) {
                 return Collections.EMPTY_LIST;
@@ -646,7 +646,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
             }
         }
 
-        public NodeEntry getNodeEntry(QName qName, int index) {
+        public NodeEntry getNodeEntry(Name qName, int index) {
             Object obj = get(qName);
             if (obj == null) {
                 return null;
@@ -663,7 +663,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
             return null;
         }
 
-        public LinkedEntries.LinkNode getLinkNode(QName qName, int index) {
+        public LinkedEntries.LinkNode getLinkNode(Name qName, int index) {
             if (index < Path.INDEX_DEFAULT) {
                 throw new IllegalArgumentException("Illegal index " + index);
             }
@@ -679,7 +679,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
             }
         }
 
-        public void put(QName qName, LinkedEntries.LinkNode value) {
+        public void put(Name qName, LinkedEntries.LinkNode value) {
             // if 'nameMap' already contains a single entry -> move it to snsMap
             LinkedEntries.LinkNode single = (LinkedEntries.LinkNode) nameMap.remove(qName);
             List l;
@@ -699,7 +699,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
             }
         }
 
-        public LinkedEntries.LinkNode remove(QName qName, LinkedEntries.LinkNode value) {
+        public LinkedEntries.LinkNode remove(Name qName, LinkedEntries.LinkNode value) {
             Object rm = nameMap.remove(qName);
             if (rm == null) {
                 List l = (List) snsMap.get(qName);
@@ -710,7 +710,7 @@ final class ChildNodeEntriesImpl implements ChildNodeEntries {
             return ((LinkedEntries.LinkNode) rm);
         }
 
-        public void reorder(QName qName, LinkedEntries.LinkNode insertValue, int position) {
+        public void reorder(Name qName, LinkedEntries.LinkNode insertValue, int position) {
             List sns = (List) snsMap.get(qName);
             if (sns == null) {
                 // no same name siblings -> no special handling required
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildPropertyEntries.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildPropertyEntries.java
index 53b9562..69666fe 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildPropertyEntries.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildPropertyEntries.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.hierarchy;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 
 import java.util.Collection;
 
@@ -31,16 +31,16 @@ public interface ChildPropertyEntries {
      * @param propertyName
      * @return true if a property entry with the given name exists.
      */
-    public boolean contains(QName propertyName);
+    public boolean contains(Name propertyName);
 
     /**
-     * Return the PropertyEntry with the given <code>QName</code> or
+     * Return the PropertyEntry with the given <code>Name</code> or
      * <code>null</code>.
      *
      * @param propertyName
      * @return
      */
-    public PropertyEntry get(QName propertyName);
+    public PropertyEntry get(Name propertyName);
 
     /**
      * Returns an unmodifiable collection containing all <code>PropertyEntry</code>
@@ -53,7 +53,7 @@ public interface ChildPropertyEntries {
     /**
      * Returns an unmodifiable collection containing all existing property names.
      *
-     * @return Collection of <code>QName</code>
+     * @return Collection of <code>Name</code>
      */
     public Collection getPropertyNames();
 
@@ -73,11 +73,11 @@ public interface ChildPropertyEntries {
     public void addAll(Collection propertyEntries);
 
     /**
-     * Remove the collection entry with the given <code>QName</code>.
+     * Remove the collection entry with the given <code>Name</code>.
      *
      * @param propertyName
      * @return true If this <code>ChildPropertyEntries</code> contained any
-     * entry with the given <code>QName</code>. False otherwise.
+     * entry with the given <code>Name</code>. False otherwise.
      */
-    public boolean remove(QName propertyName);
+    public boolean remove(Name propertyName);
 }
\ No newline at end of file
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildPropertyEntriesImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildPropertyEntriesImpl.java
index 9a4dd6e..4d78c35 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildPropertyEntriesImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildPropertyEntriesImpl.java
@@ -18,7 +18,7 @@ package org.apache.jackrabbit.jcr2spi.hierarchy;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 
 import java.util.Map;
 import java.util.Iterator;
@@ -48,16 +48,16 @@ public class ChildPropertyEntriesImpl implements ChildPropertyEntries {
     }
 
     /**
-     * @see ChildPropertyEntries#contains(QName)
+     * @see ChildPropertyEntries#contains(Name)
      */
-    public boolean contains(QName propertyName) {
+    public boolean contains(Name propertyName) {
         return properties.containsKey(propertyName);
     }
 
     /**
-     * @see ChildPropertyEntries#get(QName)
+     * @see ChildPropertyEntries#get(Name)
      */
-    public PropertyEntry get(QName propertyName) {
+    public PropertyEntry get(Name propertyName) {
         Object ref = properties.get(propertyName);
         if (ref == null) {
             // no entry exists with the given name
@@ -80,7 +80,7 @@ public class ChildPropertyEntriesImpl implements ChildPropertyEntries {
         synchronized (properties) {
             Set entries = new HashSet(properties.size());
             for (Iterator it = getPropertyNames().iterator(); it.hasNext();) {
-                QName propName = (QName) it.next();
+                Name propName = (Name) it.next();
                 entries.add(get(propName));
             }
             return Collections.unmodifiableCollection(entries);
@@ -99,7 +99,7 @@ public class ChildPropertyEntriesImpl implements ChildPropertyEntries {
      */
     public void add(PropertyEntry propertyEntry) {
         Reference ref = new WeakReference(propertyEntry);
-        properties.put(propertyEntry.getQName(), ref);
+        properties.put(propertyEntry.getName(), ref);
     }
 
     /**
@@ -115,9 +115,9 @@ public class ChildPropertyEntriesImpl implements ChildPropertyEntries {
     }
 
     /**
-     * @see ChildPropertyEntries#remove(QName)
+     * @see ChildPropertyEntries#remove(Name)
      */
-    public boolean remove(QName propertyName) {
+    public boolean remove(Name propertyName) {
         return properties.remove(propertyName) != null;
     }
 }
\ No newline at end of file
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/EntryFactory.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/EntryFactory.java
index 284ed9f..4878992 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/EntryFactory.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/EntryFactory.java
@@ -20,7 +20,8 @@ import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.jackrabbit.jcr2spi.state.TransientItemStateFactory;
 import org.apache.jackrabbit.spi.IdFactory;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.PathFactory;
 
 /**
  * <code>EntryFactory</code>...
@@ -34,6 +35,8 @@ public class EntryFactory {
      */
     private final IdFactory idFactory;
 
+    private final PathFactory pathFactory;
+
     private final NodeEntry rootEntry;
 
     /**
@@ -46,8 +49,9 @@ public class EntryFactory {
      */
     private final TransientItemStateFactory isf;
 
-    public EntryFactory(TransientItemStateFactory isf, IdFactory idFactory, NodeEntryListener listener) {
+    public EntryFactory(TransientItemStateFactory isf, IdFactory idFactory, NodeEntryListener listener, PathFactory pathFactory) {
         this.idFactory = idFactory;
+        this.pathFactory = pathFactory;
         this.isf = isf;
         this.listener = listener;
         this.rootEntry = NodeEntryImpl.createRootEntry(this);
@@ -61,14 +65,14 @@ public class EntryFactory {
         return rootEntry;
     }
 
-    public NodeEntry createNodeEntry(NodeEntry parent, QName qName, String uniqueId) {
+    public NodeEntry createNodeEntry(NodeEntry parent, Name qName, String uniqueId) {
         if (!(parent instanceof NodeEntryImpl)) {
             throw new IllegalArgumentException();
         }
         return NodeEntryImpl.createNodeEntry((NodeEntryImpl) parent, qName, uniqueId, this);
     }
 
-    public PropertyEntry createPropertyEntry(NodeEntry parent, QName qName) {
+    public PropertyEntry createPropertyEntry(NodeEntry parent, Name qName) {
         if (!(parent instanceof NodeEntryImpl)) {
             throw new IllegalArgumentException();
         }
@@ -79,6 +83,10 @@ public class EntryFactory {
         return idFactory;
     }
 
+    public PathFactory getPathFactory() {
+        return pathFactory;
+    }
+
     public TransientItemStateFactory getItemStateFactory() {
         return isf;
     }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEntry.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEntry.java
index 3c57d6a..8ac9769 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEntry.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEntry.java
@@ -16,8 +16,8 @@
  */
 package org.apache.jackrabbit.jcr2spi.hierarchy;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.jcr2spi.state.ChangeLog;
 import org.apache.jackrabbit.jcr2spi.state.Status;
@@ -41,7 +41,7 @@ public interface HierarchyEntry {
     /**
      * @return the name of this hierarchy entry.
      */
-    public QName getQName();
+    public Name getName();
 
     /**
      * @return the path of this hierarchy entry.
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEntryImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEntryImpl.java
index 8067ef1..13d1978 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEntryImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEntryImpl.java
@@ -16,8 +16,8 @@
  */
 package org.apache.jackrabbit.jcr2spi.hierarchy;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.jcr2spi.state.ChangeLog;
 import org.apache.jackrabbit.jcr2spi.state.Status;
@@ -45,7 +45,7 @@ abstract class HierarchyEntryImpl implements HierarchyEntry {
     /**
      * The name of the target item state.
      */
-    protected QName name;
+    protected Name name;
 
     /**
      * Hard reference to the parent <code>NodeEntry</code>.
@@ -66,7 +66,7 @@ abstract class HierarchyEntryImpl implements HierarchyEntry {
      * @param name   the name of the child item.
      * @param factory
      */
-    HierarchyEntryImpl(NodeEntryImpl parent, QName name, EntryFactory factory) {
+    HierarchyEntryImpl(NodeEntryImpl parent, Name name, EntryFactory factory) {
         this.parent = parent;
         this.name = name;
         this.factory = factory;
@@ -153,9 +153,9 @@ abstract class HierarchyEntryImpl implements HierarchyEntry {
     //-----------------------------------------------------< HierarchyEntry >---
     /**
      * @inheritDoc
-     * @see HierarchyEntry#getQName()
+     * @see HierarchyEntry#getName()
      */
-    public QName getQName() {
+    public Name getName() {
         return name;
     }
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEventListener.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEventListener.java
index f02a4bf..3650442 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEventListener.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEventListener.java
@@ -25,7 +25,7 @@ import org.apache.jackrabbit.spi.EventFilter;
 import org.apache.jackrabbit.spi.Event;
 import org.apache.jackrabbit.spi.EventBundle;
 import org.apache.jackrabbit.spi.NodeId;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 
 import javax.jcr.RepositoryException;
 import java.util.Collection;
@@ -54,7 +54,8 @@ public class HierarchyEventListener implements InternalEventListener {
             EventFilter filter = null;
             try {
                 // listen to all events except 'local' ones
-                filter = wspManager.createEventFilter(Event.ALL_TYPES, Path.ROOT, true, null, null, true);
+                Path root = wspManager.getPathFactory().getRootPath();
+                filter = wspManager.createEventFilter(Event.ALL_TYPES, root, true, null, null, true);
             } catch (RepositoryException e) {
                 // spi does not support observation, or another error occurred.
             }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyManager.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyManager.java
index 355778d..a6c903e 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyManager.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyManager.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.hierarchy;
 
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.spi.ItemId;
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyManagerImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyManagerImpl.java
index 524281b..81678cf 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyManagerImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyManagerImpl.java
@@ -18,10 +18,11 @@ package org.apache.jackrabbit.jcr2spi.hierarchy;
 
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.jcr2spi.state.TransientItemStateFactory;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.ItemId;
 import org.apache.jackrabbit.spi.IdFactory;
 import org.apache.jackrabbit.spi.NodeId;
+import org.apache.jackrabbit.spi.PathFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
@@ -41,9 +42,9 @@ public class HierarchyManagerImpl implements HierarchyManager {
     private final UniqueIdResolver uniqueIdResolver;
     private final IdFactory idFactory;
 
-    public HierarchyManagerImpl(TransientItemStateFactory isf, IdFactory idFactory) {
+    public HierarchyManagerImpl(TransientItemStateFactory isf, IdFactory idFactory, PathFactory pathFactory) {
         uniqueIdResolver = new UniqueIdResolver(isf);
-        rootEntry = new EntryFactory(isf, idFactory, uniqueIdResolver).createRootEntry();
+        rootEntry = new EntryFactory(isf, idFactory, uniqueIdResolver, pathFactory).createRootEntry();
         this.idFactory = idFactory;
     }
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntry.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntry.java
index cbb0ce4..47e0c48 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntry.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntry.java
@@ -18,8 +18,8 @@ package org.apache.jackrabbit.jcr2spi.hierarchy;
 
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.state.PropertyState;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
@@ -70,7 +70,7 @@ public interface NodeEntry extends HierarchyEntry {
     /**
      * @return the index of this child node entry to suppport same-name siblings.
      * If the index of this entry cannot be determined
-     * {@link org.apache.jackrabbit.name.Path#INDEX_UNDEFINED} is returned.
+     * {@link org.apache.jackrabbit.spi.Path#INDEX_UNDEFINED} is returned.
      */
     public int getIndex();
 
@@ -112,34 +112,34 @@ public interface NodeEntry extends HierarchyEntry {
      * Determines if there is a valid <code>NodeEntry</code> with the
      * specified <code>nodeName</code>.
      *
-     * @param nodeName <code>QName</code> object specifying a node name
+     * @param nodeName <code>Name</code> object specifying a node name
      * @return <code>true</code> if there is a <code>NodeEntry</code> with
      * the specified <code>nodeName</code>.
      */
-    public boolean hasNodeEntry(QName nodeName);
+    public boolean hasNodeEntry(Name nodeName);
 
     /**
      * Determines if there is a valid <code>NodeEntry</code> with the
      * specified <code>name</code> and <code>index</code>.
      *
-     * @param nodeName  <code>QName</code> object specifying a node name.
+     * @param nodeName  <code>Name</code> object specifying a node name.
      * @param index 1-based index if there are same-name child node entries.
      * @return <code>true</code> if there is a <code>NodeEntry</code> with
      * the specified <code>name</code> and <code>index</code>.
      */
-    public boolean hasNodeEntry(QName nodeName, int index);
+    public boolean hasNodeEntry(Name nodeName, int index);
 
     /**
      * Returns the valid <code>NodeEntry</code> with the specified name
      * and index or <code>null</code> if there's no matching entry.
      *
-     * @param nodeName <code>QName</code> object specifying a node name.
+     * @param nodeName <code>Name</code> object specifying a node name.
      * @param index 1-based index if there are same-name child node entries.
      * @return The <code>NodeEntry</code> with the specified name and index
      * or <code>null</code> if there's no matching entry.
      * @throws RepositoryException If an unexpected error occurs.
      */
-    public NodeEntry getNodeEntry(QName nodeName, int index) throws RepositoryException;
+    public NodeEntry getNodeEntry(Name nodeName, int index) throws RepositoryException;
 
     /**
      * Returns the valid <code>NodeEntry</code> with the specified name
@@ -148,14 +148,14 @@ public interface NodeEntry extends HierarchyEntry {
      * sure, that it's list of child entries is up to date and eventually
      * try to load the node entry.
      *
-     * @param nodeName <code>QName</code> object specifying a node name.
+     * @param nodeName <code>Name</code> object specifying a node name.
      * @param index 1-based index if there are same-name child node entries.
      * @param loadIfNotFound
      * @return The <code>NodeEntry</code> with the specified name and index
      * or <code>null</code> if there's no matching entry.
      * @throws RepositoryException If an unexpected error occurs.
      */
-    public NodeEntry getNodeEntry(QName nodeName, int index, boolean loadIfNotFound) throws RepositoryException;
+    public NodeEntry getNodeEntry(Name nodeName, int index, boolean loadIfNotFound) throws RepositoryException;
 
     /**
      * Returns a unmodifiable iterator of <code>NodeEntry</code> objects
@@ -174,7 +174,7 @@ public interface NodeEntry extends HierarchyEntry {
      * @return list of <code>NodeEntry</code> objects
      * @throws RepositoryException If an unexpected error occurs.
      */
-    public List getNodeEntries(QName nodeName) throws RepositoryException;
+    public List getNodeEntries(Name nodeName) throws RepositoryException;
 
     /**
      * Adds a new child NodeEntry to this entry.
@@ -184,7 +184,7 @@ public interface NodeEntry extends HierarchyEntry {
      * @return the new <code>NodeEntry</code>
      * @throws RepositoryException If an unexpected error occurs.
      */
-    public NodeEntry addNodeEntry(QName nodeName, String uniqueID, int index) throws RepositoryException;
+    public NodeEntry addNodeEntry(Name nodeName, String uniqueID, int index) throws RepositoryException;
 
     /**
      * Adds a new, transient child <code>NodeEntry</code>
@@ -196,27 +196,27 @@ public interface NodeEntry extends HierarchyEntry {
      * @return
      * @throws RepositoryException If an error occurs.
      */
-    public NodeState addNewNodeEntry(QName nodeName, String uniqueID, QName primaryNodeType, QNodeDefinition definition) throws RepositoryException;
+    public NodeState addNewNodeEntry(Name nodeName, String uniqueID, Name primaryNodeType, QNodeDefinition definition) throws RepositoryException;
 
     /**
-     * Determines if there is a property entry with the specified <code>QName</code>.
+     * Determines if there is a property entry with the specified <code>Name</code>.
      *
-     * @param propName <code>QName</code> object specifying a property name
+     * @param propName <code>Name</code> object specifying a property name
      * @return <code>true</code> if there is a property entry with the specified
-     * <code>QName</code>.
+     * <code>Name</code>.
      */
-    public boolean hasPropertyEntry(QName propName);
+    public boolean hasPropertyEntry(Name propName);
 
     /**
      * Returns the valid <code>PropertyEntry</code> with the specified name
      * or <code>null</code> if no matching entry exists.
      *
-     * @param propName <code>QName</code> object specifying a property name.
+     * @param propName <code>Name</code> object specifying a property name.
      * @return The <code>PropertyEntry</code> with the specified name or
      * <code>null</code> if no matching entry exists.
      * @throws RepositoryException If an unexpected error occurs.
      */
-    public PropertyEntry getPropertyEntry(QName propName) throws RepositoryException;
+    public PropertyEntry getPropertyEntry(Name propName) throws RepositoryException;
 
     /**
      * Returns the valid <code>PropertyEntry</code> with the specified name
@@ -225,13 +225,13 @@ public interface NodeEntry extends HierarchyEntry {
      * sure, that it's list of property entries is up to date and eventually
      * try to load the property entry with the given name.
      *
-     * @param propName <code>QName</code> object specifying a property name.
+     * @param propName <code>Name</code> object specifying a property name.
      * @param loadIfNotFound
      * @return The <code>PropertyEntry</code> with the specified name or
      * <code>null</code> if no matching entry exists.
      * @throws RepositoryException If an unexpected error occurs.
      */
-    public PropertyEntry getPropertyEntry(QName propName,  boolean loadIfNotFound) throws RepositoryException;
+    public PropertyEntry getPropertyEntry(Name propName,  boolean loadIfNotFound) throws RepositoryException;
 
     /**
      * Returns an unmodifiable Iterator over those children that represent valid
@@ -243,7 +243,7 @@ public interface NodeEntry extends HierarchyEntry {
 
     /**
      * Add an existing <code>PropertyEntry</code> with the given name.
-     * Please note the difference to {@link #addNewPropertyEntry(QName, QPropertyDefinition)}
+     * Please note the difference to {@link #addNewPropertyEntry(Name, QPropertyDefinition)}
      * which adds a new, transient entry.
      *
      * @param propName
@@ -251,10 +251,10 @@ public interface NodeEntry extends HierarchyEntry {
      * @throws ItemExistsException if a child item exists with the given name
      * @throws RepositoryException if an unexpected error occurs.
      */
-    public PropertyEntry addPropertyEntry(QName propName) throws ItemExistsException, RepositoryException;
+    public PropertyEntry addPropertyEntry(Name propName) throws ItemExistsException, RepositoryException;
 
     /**
-     * Adds property entries for the given <code>QName</code>s. It depends on
+     * Adds property entries for the given <code>Name</code>s. It depends on
      * the status of this <code>NodeEntry</code>, how conflicts are resolved
      * and whether or not existing entries that are missing in the iterator
      * get removed.
@@ -274,7 +274,7 @@ public interface NodeEntry extends HierarchyEntry {
      * @throws ItemExistsException
      * @throws RepositoryException if an unexpected error occurs.
      */
-    public PropertyState addNewPropertyEntry(QName propName, QPropertyDefinition definition) throws ItemExistsException, RepositoryException;
+    public PropertyState addNewPropertyEntry(Name propName, QPropertyDefinition definition) throws ItemExistsException, RepositoryException;
 
     /**
      * Reorders this NodeEntry before the sibling entry specified by the given
@@ -300,7 +300,7 @@ public interface NodeEntry extends HierarchyEntry {
      * @throws RepositoryException If the entry to be moved is not a child of this
      * NodeEntry or if an unexpected error occurs.
      */
-    public NodeEntry move(QName newName, NodeEntry newParent, boolean transientMove) throws RepositoryException;
+    public NodeEntry move(Name newName, NodeEntry newParent, boolean transientMove) throws RepositoryException;
 
     /**
      * @return true if this <code>NodeEntry</code> is transiently moved.
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java
index e1dc7dd..8e8a5b7 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java
@@ -16,9 +16,8 @@
  */
 package org.apache.jackrabbit.jcr2spi.hierarchy;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.Event;
 import org.apache.jackrabbit.spi.ItemId;
@@ -26,6 +25,7 @@ import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.IdFactory;
 import org.apache.jackrabbit.spi.PropertyId;
+import org.apache.jackrabbit.spi.PathFactory;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.jcr2spi.state.ChangeLog;
@@ -33,6 +33,8 @@ import org.apache.jackrabbit.jcr2spi.state.Status;
 import org.apache.jackrabbit.jcr2spi.state.PropertyState;
 import org.apache.jackrabbit.jcr2spi.state.ItemStateLifeCycleListener;
 import org.apache.jackrabbit.jcr2spi.util.StateUtility;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.name.PathBuilder;
 import org.apache.commons.collections.iterators.IteratorChain;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
@@ -81,7 +83,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * Map of properties.<br>
-     * Key = {@link QName} of property,<br>
+     * Key = {@link Name} of property,<br>
      * Value = {@link PropertyEntry}.
      */
     private final ChildPropertyEntries properties;
@@ -112,7 +114,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @param name      the name of the child node.
      * @param factory   the entry factory.
      */
-    private NodeEntryImpl(NodeEntryImpl parent, QName name, String uniqueID, EntryFactory factory) {
+    private NodeEntryImpl(NodeEntryImpl parent, Name name, String uniqueID, EntryFactory factory) {
         super(parent, name, factory);
         this.uniqueID = uniqueID; // NOTE: don't use setUniqueID (for mod only)
 
@@ -129,7 +131,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @return
      */
     static NodeEntry createRootEntry(EntryFactory factory) {
-        return new NodeEntryImpl(null, QName.ROOT, null, factory);
+        return new NodeEntryImpl(null, NameConstants.ROOT, null, factory);
     }
 
     /**
@@ -140,7 +142,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @param factory
      * @return
      */
-    static NodeEntry createNodeEntry(NodeEntryImpl parent, QName name, String uniqueId, EntryFactory factory) {
+    static NodeEntry createNodeEntry(NodeEntryImpl parent, Name name, String uniqueId, EntryFactory factory) {
         return new NodeEntryImpl(parent, name, uniqueId, factory);
     }
 
@@ -291,11 +293,13 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
         if (uniqueID != null) {
             return idFactory.createNodeId(uniqueID);
         } else {
+            PathFactory pf = factory.getPathFactory();
             if (parent == null) {
                 // root node
-                return idFactory.createNodeId((String) null, Path.ROOT);
+                return idFactory.createNodeId((String) null, pf.getRootPath());
             } else {
-                return idFactory.createNodeId(parent.getId(), Path.create(getQName(), getIndex()));
+                Path p = pf.create(getName(), getIndex());
+                return idFactory.createNodeId(parent.getId(), p);
             }
         }
     }
@@ -309,8 +313,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
             // uniqueID and root-node -> internal id is always the same as getId().
             return getId();
         } else {
+            PathFactory pf = factory.getPathFactory();
             NodeId parentId = (revertInfo != null) ? revertInfo.oldParent.getWorkspaceId() : parent.getWorkspaceId();
-            return idFactory.createNodeId(parentId, Path.create(getWorkspaceQName(), getWorkspaceIndex()));
+            return idFactory.createNodeId(parentId, pf.create(getWorkspaceName(), getWorkspaceIndex()));
         }
     }
 
@@ -372,9 +377,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      */
     public HierarchyEntry getDeepEntry(Path path) throws PathNotFoundException, RepositoryException {
         NodeEntryImpl entry = this;
-        Path.PathElement[] elems = path.getElements();
+        Path.Element[] elems = path.getElements();
         for (int i = 0; i < elems.length; i++) {
-            Path.PathElement elem = elems[i];
+            Path.Element elem = (Path.Element) elems[i];
             // check for root element
             if (elem.denotesRoot()) {
                 if (getParent() != null) {
@@ -384,7 +389,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
             }
 
             int index = elem.getNormalizedIndex();
-            QName name = elem.getName();
+            Name name = elem.getName();
 
             // first try to resolve to known node or property entry
             NodeEntry cne = (entry.childNodeEntries == null) ? null : entry.getNodeEntry(name, index, false);
@@ -414,20 +419,14 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
                 * 2) if the NameElement does not have SNS-index => try Property
                 * 3) else throw
                 */
-                Path remainingPath;
-                try {
-                    Path.PathBuilder pb = new Path.PathBuilder();
-                    for (int j = i; j < elems.length; j++) {
-                        pb.addLast(elems[j]);
-                    }
-                    remainingPath = pb.getPath();
-                } catch (MalformedPathException e) {
-                    // should not get here
-                    throw new RepositoryException("Invalid path");
+                PathBuilder pb = new PathBuilder(factory.getPathFactory());
+                for (int j = i; j < elems.length; j++) {
+                    pb.addLast(elems[j]);
                 }
+                Path remainingPath = pb.getPath();
 
                 NodeId parentId = entry.getId();
-                IdFactory idFactory = entry.factory.getIdFactory();
+                IdFactory idFactory = factory.getIdFactory();
 
                 NodeId nodeId = idFactory.createNodeId(parentId, remainingPath);
                 NodeEntry ne = entry.loadNodeEntry(nodeId);
@@ -458,7 +457,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
     public HierarchyEntry lookupDeepEntry(Path workspacePath) {
         NodeEntryImpl entry = this;
         for (int i = 0; i < workspacePath.getLength(); i++) {
-            Path.PathElement elem = workspacePath.getElement(i);
+            Path.Element elem = workspacePath.getElements()[i];
             // check for root element
             if (elem.denotesRoot()) {
                 if (getParent() != null) {
@@ -469,7 +468,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
             }
 
             int index = elem.getNormalizedIndex();
-            QName childName = elem.getName();
+            Name childName = elem.getName();
 
             // first try to resolve node
             NodeEntry cne = entry.lookupNodeEntry(null, childName, index);
@@ -487,9 +486,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * @inheritDoc
-     * @see NodeEntry#hasNodeEntry(QName)
+     * @see NodeEntry#hasNodeEntry(Name)
      */
-    public synchronized boolean hasNodeEntry(QName nodeName) {
+    public synchronized boolean hasNodeEntry(Name nodeName) {
         try {
             List namedEntries = childNodeEntries().get(nodeName);
             if (namedEntries.isEmpty()) {
@@ -505,9 +504,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * @inheritDoc
-     * @see NodeEntry#hasNodeEntry(QName, int)
+     * @see NodeEntry#hasNodeEntry(Name, int)
      */
-    public synchronized boolean hasNodeEntry(QName nodeName, int index) {
+    public synchronized boolean hasNodeEntry(Name nodeName, int index) {
         try {
             return getNodeEntry(nodeName, index) != null;
         } catch (RepositoryException e) {
@@ -518,17 +517,17 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * @inheritDoc
-     * @see NodeEntry#getNodeEntry(QName, int)
+     * @see NodeEntry#getNodeEntry(Name, int)
      */
-    public synchronized NodeEntry getNodeEntry(QName nodeName, int index) throws RepositoryException {
+    public synchronized NodeEntry getNodeEntry(Name nodeName, int index) throws RepositoryException {
         return getNodeEntry(nodeName, index, false);
     }
 
     /**
      * @inheritDoc
-     * @see NodeEntry#getNodeEntry(QName, int, boolean)
+     * @see NodeEntry#getNodeEntry(Name, int, boolean)
      */
-    public NodeEntry getNodeEntry(QName nodeName, int index, boolean loadIfNotFound) throws RepositoryException {
+    public NodeEntry getNodeEntry(Name nodeName, int index, boolean loadIfNotFound) throws RepositoryException {
         // TODO: avoid loading the child-infos if childNodeEntries == null
         List entries = childNodeEntries().get(nodeName);
         NodeEntry cne = null;
@@ -542,9 +541,11 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
                 }
             }
         } else if (loadIfNotFound
-                && !containsAtticChild(entries, nodeName, index) 
+                && !containsAtticChild(entries, nodeName, index)
                 && Status.NEW != getStatus()) {
-            NodeId cId = factory.getIdFactory().createNodeId(getId(), Path.create(nodeName, index));
+
+            PathFactory pf = factory.getPathFactory();
+            NodeId cId = factory.getIdFactory().createNodeId(getId(), pf.create(nodeName, index));
             cne = loadNodeEntry(cId);
         }
         return cne;
@@ -566,9 +567,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
     }
 
     /**
-     * @see NodeEntry#getNodeEntries(QName)
+     * @see NodeEntry#getNodeEntries(Name)
      */
-    public synchronized List getNodeEntries(QName nodeName) throws RepositoryException {
+    public synchronized List getNodeEntries(Name nodeName) throws RepositoryException {
         List namedEntries = childNodeEntries().get(nodeName);
         if (namedEntries.isEmpty()) {
             return Collections.EMPTY_LIST;
@@ -589,18 +590,18 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * @inheritDoc
-     * @see NodeEntry#addNodeEntry(QName, String, int)
+     * @see NodeEntry#addNodeEntry(Name, String, int)
      */
-    public NodeEntry addNodeEntry(QName nodeName, String uniqueID, int index) throws RepositoryException {
+    public NodeEntry addNodeEntry(Name nodeName, String uniqueID, int index) throws RepositoryException {
         return internalAddNodeEntry(nodeName, uniqueID, index, childNodeEntries());
     }
 
     /**
      * @inheritDoc
-     * @see NodeEntry#addNewNodeEntry(QName, String, QName, QNodeDefinition)
+     * @see NodeEntry#addNewNodeEntry(Name, String, Name, QNodeDefinition)
      */
-    public NodeState addNewNodeEntry(QName nodeName, String uniqueID,
-                                     QName primaryNodeType, QNodeDefinition definition) throws RepositoryException {
+    public NodeState addNewNodeEntry(Name nodeName, String uniqueID,
+                                     Name primaryNodeType, QNodeDefinition definition) throws RepositoryException {
         NodeEntry entry = internalAddNodeEntry(nodeName, uniqueID, Path.INDEX_UNDEFINED, childNodeEntries());
         NodeState state = factory.getItemStateFactory().createNewNodeState(entry, primaryNodeType, definition);
         if (!entry.isAvailable()) {
@@ -617,8 +618,8 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @param childEntries
      * @return
      */
-    private NodeEntry internalAddNodeEntry(QName nodeName, String uniqueID,
-                                               int index, ChildNodeEntries childEntries) {
+    private NodeEntry internalAddNodeEntry(Name nodeName, String uniqueID,
+                                           int index, ChildNodeEntries childEntries) {
         NodeEntry entry = factory.createNodeEntry(this, nodeName, uniqueID);
         childEntries.add(entry, index);
         return entry;
@@ -626,18 +627,18 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * @inheritDoc
-     * @see NodeEntry#hasPropertyEntry(QName)
+     * @see NodeEntry#hasPropertyEntry(Name)
      */
-    public synchronized boolean hasPropertyEntry(QName propName) {
+    public synchronized boolean hasPropertyEntry(Name propName) {
         PropertyEntry entry = properties.get(propName);
         return EntryValidation.isValidPropertyEntry(entry);
     }
 
     /**
      * @inheritDoc
-     * @see NodeEntry#getPropertyEntry(QName)
+     * @see NodeEntry#getPropertyEntry(Name)
      */
-    public synchronized PropertyEntry getPropertyEntry(QName propName) {
+    public synchronized PropertyEntry getPropertyEntry(Name propName) {
         PropertyEntry entry = properties.get(propName);
         if (EntryValidation.isValidPropertyEntry(entry)) {
             return entry;
@@ -648,9 +649,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * @inheritDoc
-     * @see NodeEntry#getPropertyEntry(QName, boolean)
+     * @see NodeEntry#getPropertyEntry(Name, boolean)
      */
-    public PropertyEntry getPropertyEntry(QName propName, boolean loadIfNotFound) throws RepositoryException {
+    public PropertyEntry getPropertyEntry(Name propName, boolean loadIfNotFound) throws RepositoryException {
         PropertyEntry entry = properties.get(propName);
         if (entry == null && loadIfNotFound && Status.NEW != getStatus()) {
             PropertyId propId = factory.getIdFactory().createPropertyId(getId(), propName);
@@ -687,9 +688,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * @inheritDoc
-     * @see NodeEntry#addPropertyEntry(QName)
+     * @see NodeEntry#addPropertyEntry(Name)
      */
-    public PropertyEntry addPropertyEntry(QName propName) throws ItemExistsException {
+    public PropertyEntry addPropertyEntry(Name propName) throws ItemExistsException {
         // TODO: check for existing prop.
         return internalAddPropertyEntry(propName);
     }
@@ -701,7 +702,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @param propName
      * @return
      */
-    private PropertyEntry internalAddPropertyEntry(QName propName) {
+    private PropertyEntry internalAddPropertyEntry(Name propName) {
         PropertyEntry entry = factory.createPropertyEntry(this, propName);
         properties.add(entry);
 
@@ -724,7 +725,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
         // add all entries that are missing
         for (Iterator it = propNames.iterator(); it.hasNext();) {
-            QName propName = (QName) it.next();
+            Name propName = (Name) it.next();
             if (!properties.contains(propName)) {
                 addPropertyEntry(propName);
             }
@@ -736,7 +737,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
         ItemState state = internalGetItemState();
         if (containsExtra && (state == null || state.getStatus() == Status.INVALIDATED)) {
             for (Iterator it = diff.iterator(); it.hasNext();) {
-                QName propName = (QName) it.next();
+                Name propName = (Name) it.next();
                 PropertyEntry pEntry = properties.get(propName);
                 if (pEntry != null) {
                     pEntry.remove();
@@ -747,9 +748,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * @inheritDoc
-     * @see NodeEntry#addNewPropertyEntry(QName, QPropertyDefinition)
+     * @see NodeEntry#addNewPropertyEntry(Name, QPropertyDefinition)
      */
-    public PropertyState addNewPropertyEntry(QName propName, QPropertyDefinition definition)
+    public PropertyState addNewPropertyEntry(Name propName, QPropertyDefinition definition)
             throws ItemExistsException, RepositoryException {
         // check for an existing property
         PropertyEntry existing = properties.get(propName);
@@ -793,7 +794,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
     /**
      * @param propName
      */
-    void internalRemovePropertyEntry(QName propName) {
+    void internalRemovePropertyEntry(Name propName) {
         if (!properties.remove(propName)) {
             propertiesInAttic.remove(propName);
         }
@@ -821,9 +822,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
     }
 
    /**
-    * @see NodeEntry#move(QName, NodeEntry, boolean)
+    * @see NodeEntry#move(Name, NodeEntry, boolean)
     */
-   public NodeEntry move(QName newName, NodeEntry newParent, boolean transientMove) throws RepositoryException {
+   public NodeEntry move(Name newName, NodeEntry newParent, boolean transientMove) throws RepositoryException {
        if (parent == null) {
            // the root may never be moved
            throw new RepositoryException("Root cannot be moved.");
@@ -840,7 +841,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
        NodeEntryImpl entry = (NodeEntryImpl) parent.childNodeEntries().remove(this);
        if (entry != this) {
            // should never occur
-           String msg = "Internal error. Attempt to move NodeEntry (" + getQName() + ") which is not connected to its parent.";
+           String msg = "Internal error. Attempt to move NodeEntry (" + getName() + ") which is not connected to its parent.";
            log.error(msg);
            throw new RepositoryException(msg);
        }
@@ -864,7 +865,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @see NodeEntry#refresh(Event)
      */
     public void refresh(Event childEvent) {
-        QName eventName = childEvent.getQPath().getNameElement().getName();
+        Name eventName = childEvent.getPath().getNameElement().getName();
         switch (childEvent.getType()) {
             case Event.NODE_ADDED:
                 if (childNodeEntries == null) {
@@ -872,7 +873,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
                     return;
                 }
 
-                int index = childEvent.getQPath().getNameElement().getNormalizedIndex();
+                int index = childEvent.getPath().getNameElement().getNormalizedIndex();
                 String uniqueChildID = null;
                 if (childEvent.getItemId().getPath() == null) {
                     uniqueChildID = childEvent.getItemId().getUniqueID();
@@ -902,7 +903,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
             case Event.PROPERTY_ADDED:
                 // create a new property reference if it has not been
                 // added by some earlier 'add' event
-                HierarchyEntry child = lookupEntry(childEvent.getItemId(), childEvent.getQPath());
+                HierarchyEntry child = lookupEntry(childEvent.getItemId(), childEvent.getPath());
                 if (child == null) {
                     internalAddPropertyEntry(eventName);
                 } else {
@@ -912,14 +913,14 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
             case Event.NODE_REMOVED:
             case Event.PROPERTY_REMOVED:
-                child = lookupEntry(childEvent.getItemId(), childEvent.getQPath());
+                child = lookupEntry(childEvent.getItemId(), childEvent.getPath());
                 if (child != null) {
                     child.remove();
                 } // else: child-Entry has not been loaded yet -> ignore
                 break;
 
             case Event.PROPERTY_CHANGED:
-                child = lookupEntry(childEvent.getItemId(), childEvent.getQPath());
+                child = lookupEntry(childEvent.getItemId(), childEvent.getPath());
                 if (child == null) {
                     // prop-Entry has not been loaded yet -> add propEntry
                     internalAddPropertyEntry(eventName);
@@ -955,29 +956,26 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @see HierarchyEntryImpl#buildPath(boolean)
      */
     Path buildPath(boolean wspPath) throws RepositoryException {
+        PathFactory pf = factory.getPathFactory();
         // shortcut for root state
         if (parent == null) {
-            return Path.ROOT;
+            return pf.getRootPath();
         }
         // build path otherwise
-        try {
-            Path.PathBuilder builder = new Path.PathBuilder();
-            buildPath(builder, this, wspPath);
-            return builder.getPath();
-        } catch (MalformedPathException e) {
-            String msg = "Failed to build path of " + this;
-            throw new RepositoryException(msg, e);
-        }
+        PathBuilder builder = new PathBuilder(pf);
+        buildPath(builder, this, wspPath);
+        return builder.getPath();
     }
 
     /**
      * Adds the path element of an item id to the path currently being built.
      * On exit, <code>builder</code> contains the path of this entry.
      *
-     * @param builder builder currently being used
-     * @param hEntry HierarchyEntry of the state the path should be built for.
+     * @param builder
+     * @param nEntry NodeEntryImpl of the state the path should be built for.
+     * @param wspPath true if the workspace path should be built
      */
-    private static void buildPath(Path.PathBuilder builder, NodeEntryImpl nEntry, boolean wspPath) throws RepositoryException {
+    private static void buildPath(PathBuilder builder, NodeEntryImpl nEntry, boolean wspPath) throws RepositoryException {
         NodeEntryImpl parentEntry = (wspPath && nEntry.revertInfo != null) ? nEntry.revertInfo.oldParent : nEntry.parent;
         // shortcut for root state
         if (parentEntry == null) {
@@ -989,18 +987,14 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
         buildPath(builder, parentEntry, wspPath);
 
         int index = (wspPath) ? nEntry.getWorkspaceIndex() : nEntry.getIndex();
-        QName name = (wspPath) ? nEntry.getWorkspaceQName() : nEntry.getQName();
+        Name name = (wspPath) ? nEntry.getWorkspaceName() : nEntry.getName();
         // add to path
         if (index == Path.INDEX_UNDEFINED) {
             throw new RepositoryException("Invalid index " + index + " with nodeEntry " + nEntry);
         }
 
         // TODO: check again. special treatment for default index for consistency with PathFormat.parse
-        if (index == Path.INDEX_DEFAULT) {
-            builder.addLast(name);
-        } else {
-            builder.addLast(name, index);
-        }
+        builder.addLast(name, index);
     }
 
     //-----------------------------------------------< private || protected >---
@@ -1012,7 +1006,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
         if (propertyEntry.getParent() != this) {
             throw new IllegalArgumentException("Internal error: Parent mismatch.");
         }
-        QName propName = propertyEntry.getQName();
+        Name propName = propertyEntry.getName();
         if (propertiesInAttic.containsKey(propName)) {
             properties.add((PropertyEntry) propertiesInAttic.remove(propName));
         } // else: propEntry has never been moved to the attic (see 'addPropertyEntry')
@@ -1024,8 +1018,8 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @param oldIndex
      * @return
      */
-    boolean matches(QName oldName, int oldIndex) {
-        return getWorkspaceQName().equals(oldName) && getWorkspaceIndex() == oldIndex;
+    boolean matches(Name oldName, int oldIndex) {
+        return getWorkspaceName().equals(oldName) && getWorkspaceIndex() == oldIndex;
     }
 
     /**
@@ -1033,16 +1027,16 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @param oldName
      * @return
      */
-    boolean matches(QName oldName) {
-        return getWorkspaceQName().equals(oldName);
+    boolean matches(Name oldName) {
+        return getWorkspaceName().equals(oldName);
     }
 
 
-    private QName getWorkspaceQName() {
+    private Name getWorkspaceName() {
         if (revertInfo != null) {
             return revertInfo.oldName;
         } else {
-            return getQName();
+            return getName();
         }
     }
 
@@ -1095,7 +1089,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @return
      */
     private HierarchyEntry lookupEntry(ItemId eventId, Path eventPath) {
-        QName childName = eventPath.getNameElement().getName();
+        Name childName = eventPath.getNameElement().getName();
         HierarchyEntry child;
         if (eventId.denotesNode()) {
             String uniqueChildID = (eventId.getPath() == null) ? eventId.getUniqueID() : null;
@@ -1109,7 +1103,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
         return (child == null || child.getStatus() == Status.NEW) ? null : child;
     }
 
-    private NodeEntry lookupNodeEntry(String uniqueChildId, QName childName, int index) {
+    private NodeEntry lookupNodeEntry(String uniqueChildId, Name childName, int index) {
         NodeEntry child = null;
         if (uniqueChildId != null) {
             child = childNodeAttic.get(uniqueChildId);
@@ -1126,7 +1120,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
         return child;
     }
 
-    private PropertyEntry lookupPropertyEntry(QName childName) {
+    private PropertyEntry lookupPropertyEntry(Name childName) {
         // for external prop-removal the attic must be consulted first
         // in order not access a NEW prop shadowing a transiently removed
         // property with the same name.
@@ -1139,16 +1133,16 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
 
     /**
      * Deals with modified jcr:uuid and jcr:mixinTypes property.
-     * See {@link #notifyUUIDorMIXINRemoved(QName)}
+     * See {@link #notifyUUIDorMIXINRemoved(Name)}
      *
      * @param child
      */
     private void notifyUUIDorMIXINModified(PropertyEntry child) {
         try {
-            if (QName.JCR_UUID.equals(child.getQName())) {
+            if (NameConstants.JCR_UUID.equals(child.getName())) {
                 PropertyState ps = child.getPropertyState();
                 setUniqueID(ps.getValue().getString());
-            } else if (QName.JCR_MIXINTYPES.equals(child.getQName())) {
+            } else if (NameConstants.JCR_MIXINTYPES.equals(child.getName())) {
                 NodeState state = (NodeState) internalGetItemState();
                 if (state != null) {
                     PropertyState ps = child.getPropertyState();
@@ -1156,9 +1150,9 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
                 } // nodestate not yet loaded -> ignore change
             }
         } catch (ItemNotFoundException e) {
-            log.debug("Property with name " + child.getQName() + " does not exist (anymore)");
+            log.debug("Property with name " + child.getName() + " does not exist (anymore)");
         } catch (RepositoryException e) {
-            log.debug("Unable to access child property " + child.getQName(), e.getMessage());
+            log.debug("Unable to access child property " + child.getName(), e.getMessage());
         }
     }
 
@@ -1168,13 +1162,13 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      *
      * @param propName
      */
-    private void notifyUUIDorMIXINRemoved(QName propName) {
-        if (QName.JCR_UUID.equals(propName)) {
+    private void notifyUUIDorMIXINRemoved(Name propName) {
+        if (NameConstants.JCR_UUID.equals(propName)) {
             setUniqueID(null);
-        } else if (QName.JCR_MIXINTYPES.equals(propName)) {
+        } else if (NameConstants.JCR_MIXINTYPES.equals(propName)) {
             NodeState state = (NodeState) internalGetItemState();
             if (state != null) {
-                state.setMixinTypeNames(QName.EMPTY_ARRAY);
+                state.setMixinTypeNames(Name.EMPTY_ARRAY);
             }
         }
     }
@@ -1205,9 +1199,6 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * with this NodeEntry. NOTE, that if the childNodeEntries have not been
      * loaded yet, no attempt is made to do so.
      *
-     * @param createNewList if true, both properties and childNodeEntries are
-     * copied to new list, since recursive calls may call this node state to
-     * inform the removal of a child entry.
      * @param includeAttic
      * @return
      */
@@ -1239,7 +1230,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * this <code>NodeEntry</code>.
      */
     private int getChildIndex(NodeEntry cne) throws ItemNotFoundException, RepositoryException {
-        List sns = childNodeEntries().get(cne.getQName());
+        List sns = childNodeEntries().get(cne.getName());
         // index is one based
         int index = Path.INDEX_DEFAULT;
         for (Iterator it = sns.iterator(); it.hasNext(); ) {
@@ -1267,7 +1258,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
      * @param childIndex
      * @return
      */
-    private boolean containsAtticChild(List siblings, QName childName, int childIndex) {
+    private boolean containsAtticChild(List siblings, Name childName, int childIndex) {
         // check if a matching entry exists in the attic
         if (childNodeAttic.contains(childName, childIndex)) {
             return true;
@@ -1368,12 +1359,12 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
     private class RevertInfo implements ItemStateLifeCycleListener {
 
         private NodeEntryImpl oldParent;
-        private QName oldName;
+        private Name oldName;
         private int oldIndex;
 
         private Map reorderedChildren;
 
-        private RevertInfo(NodeEntryImpl oldParent, QName oldName, int oldIndex) {
+        private RevertInfo(NodeEntryImpl oldParent, Name oldName, int oldIndex) {
             this.oldParent = oldParent;
             this.oldName = oldName;
             this.oldIndex = oldIndex;
@@ -1397,7 +1388,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
                 // must be disposed manually
                 for (Iterator it = reorderedChildren.keySet().iterator(); it.hasNext();) {
                     NodeEntry ne = (NodeEntry) it.next();
-                    List sns = childNodeEntries.get(ne.getQName());
+                    List sns = childNodeEntries.get(ne.getName());
                     if (sns.size() > 1) {
                         for (Iterator snsIt = sns.iterator(); snsIt.hasNext();) {
                             NodeEntryImpl sibling = (NodeEntryImpl) snsIt.next();
@@ -1413,7 +1404,7 @@ public class NodeEntryImpl extends HierarchyEntryImpl implements NodeEntry {
         }
 
         private boolean isMoved() {
-            return oldParent != getParent() || !getQName().equals(oldName);
+            return oldParent != getParent() || !getName().equals(oldName);
         }
 
         private void reordered(NodeEntry insertEntry, NodeEntry previousBefore) {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/PropertyEntryImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/PropertyEntryImpl.java
index 9a53ed9..2bbd4a7 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/PropertyEntryImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/PropertyEntryImpl.java
@@ -16,9 +16,8 @@
  */
 package org.apache.jackrabbit.jcr2spi.hierarchy;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.PropertyId;
 import org.apache.jackrabbit.jcr2spi.state.PropertyState;
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
@@ -40,7 +39,7 @@ public class PropertyEntryImpl extends HierarchyEntryImpl implements PropertyEnt
      * @param name      the name of the property.
      * @param factory
      */
-    private PropertyEntryImpl(NodeEntryImpl parent, QName name, EntryFactory factory) {
+    private PropertyEntryImpl(NodeEntryImpl parent, Name name, EntryFactory factory) {
         super(parent, name, factory);
     }
 
@@ -52,7 +51,7 @@ public class PropertyEntryImpl extends HierarchyEntryImpl implements PropertyEnt
      * @param factory
      * @return new <code>PropertyEntry</code>
      */
-    static PropertyEntry create(NodeEntryImpl parent, QName name, EntryFactory factory) {
+    static PropertyEntry create(NodeEntryImpl parent, Name name, EntryFactory factory) {
         return new PropertyEntryImpl(parent, name, factory);
     }
 
@@ -71,17 +70,8 @@ public class PropertyEntryImpl extends HierarchyEntryImpl implements PropertyEnt
      * @see HierarchyEntryImpl#buildPath(boolean)
      */
     Path buildPath(boolean workspacePath) throws RepositoryException {
-        try {
-            Path.PathBuilder builder = new Path.PathBuilder();
-            builder.addAll(parent.buildPath(workspacePath).getElements());
-            // add property name to parent path
-            builder.addLast(getQName());
-
-            return builder.getPath();
-        } catch (MalformedPathException e) {
-            String msg = "Failed to build path of " + this;
-            throw new RepositoryException(msg, e);
-        }
+        Path parentPath = parent.buildPath(workspacePath);
+        return factory.getPathFactory().create(parentPath, getName(), true);
     }
 
     //------------------------------------------------------< PropertyEntry >---
@@ -89,14 +79,14 @@ public class PropertyEntryImpl extends HierarchyEntryImpl implements PropertyEnt
      * @see PropertyEntry#getId()
      */
     public PropertyId getId() {
-        return factory.getIdFactory().createPropertyId(parent.getId(), getQName());
+        return factory.getIdFactory().createPropertyId(parent.getId(), getName());
     }
 
     /**
      * @see PropertyEntry#getWorkspaceId()
      */
     public PropertyId getWorkspaceId() {
-        return factory.getIdFactory().createPropertyId(parent.getWorkspaceId(), getQName());
+        return factory.getIdFactory().createPropertyId(parent.getWorkspaceId(), getName());
     }
 
     /**
@@ -124,7 +114,7 @@ public class PropertyEntryImpl extends HierarchyEntryImpl implements PropertyEnt
     public void remove() {
         removeEntry(this);
         if (getStatus() != Status.STALE_DESTROYED) {
-            parent.internalRemovePropertyEntry(getQName());
+            parent.internalRemovePropertyEntry(getName());
         }
     }
 }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/lock/LockManagerImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/lock/LockManagerImpl.java
index cd66e90..1e48789 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/lock/LockManagerImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/lock/LockManagerImpl.java
@@ -33,7 +33,7 @@ import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.jcr2spi.state.PropertyState;
 import org.apache.jackrabbit.spi.LockInfo;
 import org.apache.jackrabbit.spi.NodeId;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.name.NameConstants;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -292,7 +292,7 @@ public class LockManagerImpl implements LockManager, SessionListener {
      */
     private NodeState getLockHoldingState(NodeState nodeState) {
         NodeEntry entry = nodeState.getNodeEntry();
-        while (!entry.hasPropertyEntry(QName.JCR_LOCKISDEEP)) {
+        while (!entry.hasPropertyEntry(NameConstants.JCR_LOCKISDEEP)) {
             NodeEntry parent = entry.getParent();
             if (parent == null) {
                 // reached root state without finding a locked node
@@ -540,7 +540,7 @@ public class LockManagerImpl implements LockManager, SessionListener {
         private void startListening() {
             if (cacheBehaviour == CacheBehaviour.OBSERVATION) {
                 try {
-                    PropertyState ps = lockHoldingState.getPropertyState(QName.JCR_LOCKISDEEP);
+                    PropertyState ps = lockHoldingState.getPropertyState(NameConstants.JCR_LOCKISDEEP);
                     ps.addListener(this);
                 } catch (RepositoryException e) {
                     log.warn("Internal error", e);
@@ -551,8 +551,8 @@ public class LockManagerImpl implements LockManager, SessionListener {
         private void stopListening() {
             if (cacheBehaviour == CacheBehaviour.OBSERVATION) {
                 try {
-                    if (lockHoldingState.hasPropertyName(QName.JCR_LOCKISDEEP)) {
-                        PropertyState ps = lockHoldingState.getPropertyState(QName.JCR_LOCKISDEEP);
+                    if (lockHoldingState.hasPropertyName(NameConstants.JCR_LOCKISDEEP)) {
+                        PropertyState ps = lockHoldingState.getPropertyState(NameConstants.JCR_LOCKISDEEP);
                         ps.removeListener(this);
                     }
                 } catch (ItemNotFoundException e) {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/LocalNamespaceMappings.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/LocalNamespaceMappings.java
index 427fce8..8c55f1d 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/LocalNamespaceMappings.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/LocalNamespaceMappings.java
@@ -16,13 +16,11 @@
  */
 package org.apache.jackrabbit.jcr2spi.name;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.AbstractNamespaceResolver;
-import org.apache.jackrabbit.name.NamespaceListener;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameCache;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.jcr2spi.SessionImpl;
 import org.apache.jackrabbit.util.XMLChar;
+import org.apache.jackrabbit.namespace.AbstractNamespaceResolver;
+import org.apache.jackrabbit.namespace.NamespaceListener;
 
 import javax.jcr.NamespaceException;
 import javax.jcr.RepositoryException;
@@ -42,12 +40,12 @@ import java.util.Map;
  * instance) and keeps track of local namespace mappings added by the session.
  * <p>
  * The namespace resolution methods required by the
- * {@link NamespaceResolver NamespaceResolver} are implemented by first
+ * {@link org.apache.jackrabbit.namespace.NamespaceResolver NamespaceResolver} are implemented by first
  * looking up the local namespace mapping and then backing to the
  * underlying namespace registry.
  */
 public class LocalNamespaceMappings extends AbstractNamespaceResolver
-    implements NamespaceListener, NameCache {
+    implements NamespaceListener {
 
     /** The underlying global and persistent namespace registry. */
     private final NamespaceRegistryImpl nsReg;
@@ -58,6 +56,9 @@ public class LocalNamespaceMappings extends AbstractNamespaceResolver
     /** URI to prefix mappings of local namespaces. */
     private final HashMap uriToPrefix = new HashMap();
 
+   // private final NameResolver nameResolver;
+   // private final PathResolver pathResolver;
+
     /**
      * Creates a local namespace manager with the given underlying
      * namespace registry.
@@ -82,16 +83,16 @@ public class LocalNamespaceMappings extends AbstractNamespaceResolver
         if (prefix == null || uri == null) {
             throw new IllegalArgumentException("prefix/uri can not be null");
         }
-        if (QName.NS_EMPTY_PREFIX.equals(prefix)
-                || QName.NS_DEFAULT_URI.equals(uri)) {
+        if (Name.NS_EMPTY_PREFIX.equals(prefix)
+                || Name.NS_DEFAULT_URI.equals(uri)) {
             throw new NamespaceException("default namespace is reserved and can not be changed");
         }
         // special case: xml namespace
-        if (uri.equals(QName.NS_XML_URI)) {
+        if (uri.equals(Name.NS_XML_URI)) {
             throw new NamespaceException("xml namespace is reserved and can not be changed.");
         }
         // special case: prefixes xml*
-        if (prefix.toLowerCase().startsWith(QName.NS_XML_PREFIX)) {
+        if (prefix.toLowerCase().startsWith(Name.NS_XML_PREFIX)) {
             throw new NamespaceException("reserved prefix: " + prefix);
         }
         // check if the prefix is a valid XML prefix
@@ -167,46 +168,6 @@ public class LocalNamespaceMappings extends AbstractNamespaceResolver
         return new HashMap(prefixToURI);
     }
 
-    //-------------------------------------------------------------< NameCache >
-
-    /**
-     * {@inheritDoc}
-     */
-    public QName retrieveName(String jcrName) {
-        if (prefixToURI.size() == 0) {
-            return nsReg.retrieveName(jcrName);
-        }
-        return null;
-    }
-
-    /**
-     * {@inheritDoc}
-     */
-    public String retrieveName(QName name) {
-        if (prefixToURI.size() == 0
-                || !uriToPrefix.containsKey(name.getNamespaceURI())) {
-            return nsReg.retrieveName(name);
-        }
-        return null;
-    }
-
-    /**
-     * {@inheritDoc}
-     */
-    public void cacheName(String jcrName, QName name) {
-        if (prefixToURI.size() == 0
-                || !uriToPrefix.containsKey(name.getNamespaceURI())) {
-            nsReg.cacheName(jcrName, name);
-        }
-    }
-
-    /**
-     * {@inheritDoc}
-     */
-    public void evictAllNames() {
-        nsReg.evictAllNames();
-    }
-
     //--------------------------------------------------< NamespaceResolver >---
     /**
      * {@inheritDoc}
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/NamespaceCache.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/NamespaceCache.java
index 9221d5f..5e223be 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/NamespaceCache.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/NamespaceCache.java
@@ -16,8 +16,8 @@
  */
 package org.apache.jackrabbit.jcr2spi.name;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NamespaceListener;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.namespace.NamespaceListener;
 import org.apache.jackrabbit.util.XMLChar;
 import org.apache.jackrabbit.spi.RepositoryService;
 import org.slf4j.Logger;
@@ -51,34 +51,34 @@ public class NamespaceCache {
 
     static {
         // reserved prefixes
-        RESERVED_PREFIXES.add(QName.NS_XML_PREFIX);
-        RESERVED_PREFIXES.add(QName.NS_XMLNS_PREFIX);
+        RESERVED_PREFIXES.add(Name.NS_XML_PREFIX);
+        RESERVED_PREFIXES.add(Name.NS_XMLNS_PREFIX);
         // predefined (e.g. built-in) prefixes
-        RESERVED_PREFIXES.add(QName.NS_REP_PREFIX);
-        RESERVED_PREFIXES.add(QName.NS_JCR_PREFIX);
-        RESERVED_PREFIXES.add(QName.NS_NT_PREFIX);
-        RESERVED_PREFIXES.add(QName.NS_MIX_PREFIX);
-        RESERVED_PREFIXES.add(QName.NS_SV_PREFIX);
-        RESERVED_PREFIXES.add(QName.NS_EMPTY_PREFIX);
+        RESERVED_PREFIXES.add(Name.NS_REP_PREFIX);
+        RESERVED_PREFIXES.add(Name.NS_JCR_PREFIX);
+        RESERVED_PREFIXES.add(Name.NS_NT_PREFIX);
+        RESERVED_PREFIXES.add(Name.NS_MIX_PREFIX);
+        RESERVED_PREFIXES.add(Name.NS_SV_PREFIX);
+        RESERVED_PREFIXES.add(Name.NS_EMPTY_PREFIX);
         // reserved namespace URI's
-        RESERVED_URIS.add(QName.NS_XML_URI);
-        RESERVED_URIS.add(QName.NS_XMLNS_URI);
+        RESERVED_URIS.add(Name.NS_XML_URI);
+        RESERVED_URIS.add(Name.NS_XMLNS_URI);
         // predefined (e.g. built-in) namespace URI's
-        RESERVED_URIS.add(QName.NS_REP_URI);
-        RESERVED_URIS.add(QName.NS_JCR_URI);
-        RESERVED_URIS.add(QName.NS_NT_URI);
-        RESERVED_URIS.add(QName.NS_MIX_URI);
-        RESERVED_URIS.add(QName.NS_SV_URI);
-        RESERVED_URIS.add(QName.NS_DEFAULT_URI);
+        RESERVED_URIS.add(Name.NS_REP_URI);
+        RESERVED_URIS.add(Name.NS_JCR_URI);
+        RESERVED_URIS.add(Name.NS_NT_URI);
+        RESERVED_URIS.add(Name.NS_MIX_URI);
+        RESERVED_URIS.add(Name.NS_SV_URI);
+        RESERVED_URIS.add(Name.NS_DEFAULT_URI);
         // reserved and predefined namespaces
-        RESERVED_NAMESPACES.put(QName.NS_XML_PREFIX, QName.NS_XML_URI);
-        RESERVED_NAMESPACES.put(QName.NS_XMLNS_PREFIX, QName.NS_XMLNS_URI);
-        RESERVED_NAMESPACES.put(QName.NS_REP_PREFIX, QName.NS_REP_URI);
-        RESERVED_NAMESPACES.put(QName.NS_JCR_PREFIX, QName.NS_JCR_URI);
-        RESERVED_NAMESPACES.put(QName.NS_NT_PREFIX, QName.NS_NT_URI);
-        RESERVED_NAMESPACES.put(QName.NS_MIX_PREFIX, QName.NS_MIX_URI);
-        RESERVED_NAMESPACES.put(QName.NS_SV_PREFIX, QName.NS_SV_URI);
-        RESERVED_NAMESPACES.put(QName.NS_EMPTY_PREFIX, QName.NS_DEFAULT_URI);
+        RESERVED_NAMESPACES.put(Name.NS_XML_PREFIX, Name.NS_XML_URI);
+        RESERVED_NAMESPACES.put(Name.NS_XMLNS_PREFIX, Name.NS_XMLNS_URI);
+        RESERVED_NAMESPACES.put(Name.NS_REP_PREFIX, Name.NS_REP_URI);
+        RESERVED_NAMESPACES.put(Name.NS_JCR_PREFIX, Name.NS_JCR_URI);
+        RESERVED_NAMESPACES.put(Name.NS_NT_PREFIX, Name.NS_NT_URI);
+        RESERVED_NAMESPACES.put(Name.NS_MIX_PREFIX, Name.NS_MIX_URI);
+        RESERVED_NAMESPACES.put(Name.NS_SV_PREFIX, Name.NS_SV_URI);
+        RESERVED_NAMESPACES.put(Name.NS_EMPTY_PREFIX, Name.NS_DEFAULT_URI);
     }
 
     private final Set listeners = new HashSet();
@@ -220,7 +220,7 @@ public class NamespaceCache {
         if (prefix == null || uri == null) {
             throw new IllegalArgumentException("prefix/uri can not be null");
         }
-        if (QName.NS_EMPTY_PREFIX.equals(prefix) || QName.NS_DEFAULT_URI.equals(uri)) {
+        if (Name.NS_EMPTY_PREFIX.equals(prefix) || Name.NS_DEFAULT_URI.equals(uri)) {
             throw new NamespaceException("default namespace is reserved and can not be changed");
         }
         if (RESERVED_URIS.contains(uri)) {
@@ -232,7 +232,7 @@ public class NamespaceCache {
                 + prefix + " -> " + uri + ": reserved prefix");
         }
         // special case: prefixes xml*
-        if (prefix.toLowerCase().startsWith(QName.NS_XML_PREFIX)) {
+        if (prefix.toLowerCase().startsWith(Name.NS_XML_PREFIX)) {
             throw new NamespaceException("failed to register namespace "
                 + prefix + " -> " + uri + ": reserved prefix");
         }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/NamespaceRegistryImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/NamespaceRegistryImpl.java
index f893253..fff62bc 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/NamespaceRegistryImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/name/NamespaceRegistryImpl.java
@@ -16,18 +16,10 @@
  */
 package org.apache.jackrabbit.jcr2spi.name;
 
-import org.apache.jackrabbit.name.AbstractNamespaceResolver;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameCache;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.CachingNameResolver;
-import org.apache.jackrabbit.name.ParsingNameResolver;
-import org.apache.jackrabbit.name.NameResolver;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.NamespaceListener;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.PathFactory;
+import org.apache.jackrabbit.namespace.AbstractNamespaceResolver;
+import org.apache.jackrabbit.namespace.NamespaceListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
@@ -41,11 +33,10 @@ import javax.jcr.RepositoryException;
  * NamespaceRegistry.
  */
 public class NamespaceRegistryImpl extends AbstractNamespaceResolver
-    implements NamespaceRegistry, NameCache {
+    implements NamespaceRegistry {
 
     private static Logger log = LoggerFactory.getLogger(NamespaceRegistryImpl.class);
 
-    private final NameResolver resolver;
     private final NamespaceStorage storage;
     private final NamespaceCache nsCache;
 
@@ -53,15 +44,16 @@ public class NamespaceRegistryImpl extends AbstractNamespaceResolver
      * Create a new <code>NamespaceRegistryImpl</code>.
      *
      * @param storage
+     * @param pathFactory
      */
     public NamespaceRegistryImpl(NamespaceStorage storage,
-                                 NamespaceCache nsCache) {
+                                 NamespaceCache nsCache,
+                                 NameFactory nameFactory, PathFactory pathFactory) {
         // listener support in AbstractNamespaceResolver is not needed
         // because we delegate listeners to NamespaceCache
         super(false);
         this.storage = storage;
         this.nsCache = nsCache;
-        this.resolver = new CachingNameResolver(new ParsingNameResolver(this));
     }
 
     //--------------------------------------------------< NamespaceRegistry >---
@@ -95,7 +87,7 @@ public class NamespaceRegistryImpl extends AbstractNamespaceResolver
 
     /**
      * @see javax.jcr.NamespaceRegistry#getURI(String)
-     * @see org.apache.jackrabbit.name.NamespaceResolver#getURI(String)
+     * @see org.apache.jackrabbit.namespace.NamespaceResolver#getURI(String)
      */
     public String getURI(String prefix) throws NamespaceException {
         // try to load the uri
@@ -109,7 +101,7 @@ public class NamespaceRegistryImpl extends AbstractNamespaceResolver
 
     /**
      * @see javax.jcr.NamespaceRegistry#getPrefix(String)
-     * @see org.apache.jackrabbit.name.NamespaceResolver#getPrefix(String)
+     * @see org.apache.jackrabbit.namespace.NamespaceResolver#getPrefix(String)
      */
     public String getPrefix(String uri) throws NamespaceException {
         // try to load the prefix
@@ -121,60 +113,6 @@ public class NamespaceRegistryImpl extends AbstractNamespaceResolver
         }
     }
 
-    /**
-     * @see org.apache.jackrabbit.name.NamespaceResolver#getQName(String)
-     * @deprecated
-     */
-    public QName getQName(String name)
-            throws IllegalNameException, UnknownPrefixException {
-        return NameFormat.parse(name, this);
-    }
-
-    /**
-     * @see org.apache.jackrabbit.name.NamespaceResolver#getJCRName(QName)
-     * @deprecated
-     */
-    public String getJCRName(QName name) throws NoPrefixDeclaredException {
-        return NameFormat.format(name, this);
-    }
-
-    //----------------------------------------------------------< NameCache >---
-    /**
-     * {@inheritDoc}
-     */
-    public QName retrieveName(String jcrName) {
-        try {
-            return resolver.getQName(jcrName);
-        } catch (NameException e) {
-            return null;
-        } catch (NamespaceException e) {
-            return null;
-        }
-    }
-
-    /**
-     * {@inheritDoc}
-     */
-    public String retrieveName(QName name) {
-        try {
-            return resolver.getJCRName(name);
-        } catch (NamespaceException e) {
-            return null;
-        }
-    }
-
-    /**
-     * {@inheritDoc}
-     */
-    public void cacheName(String jcrName, QName name) {
-    }
-
-    /**
-     * {@inheritDoc}
-     */
-    public void evictAllNames() {
-    }
-
     //-----------------------< AbstractNamespaceResolver >----------------------
 
     /**
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/BitsetENTCacheImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/BitsetENTCacheImpl.java
index db16d9d..e963480 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/BitsetENTCacheImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/BitsetENTCacheImpl.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 
 import java.util.TreeSet;
 import java.util.HashMap;
@@ -69,7 +69,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
     /**
      * The reverse lookup table for bit numbers to names
      */
-    private QName[] names = new QName[1024];
+    private Name[] names = new Name[1024];
 
     /**
      * Creates a new bitset effective node type cache
@@ -82,7 +82,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
     /**
      * {@inheritDoc}
      */
-    public Key getKey(QName[] ntNames) {
+    public Key getKey(Name[] ntNames) {
         return new BitsetKey(ntNames, nameIndex.size() + ntNames.length);
     }
 
@@ -122,7 +122,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
     /**
      * {@inheritDoc}
      */
-    public void invalidate(QName name) {
+    public void invalidate(Name name) {
         /**
          * remove all affected effective node types from aggregates cache
          * (copy keys first to prevent ConcurrentModificationException)
@@ -167,7 +167,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
      * @param name the name to lookup
      * @return the bit number for the given name
      */
-    private int getBitNumber(QName name) {
+    private int getBitNumber(Name name) {
         Integer i = (Integer) nameIndex.get(name);
         if (i == null) {
             synchronized (nameIndex) {
@@ -177,7 +177,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
                     i = new Integer(idx);
                     nameIndex.put(name, i);
                     if (idx >= names.length) {
-                        QName[] newNames = new QName[names.length*2];
+                        Name[] newNames = new Name[names.length*2];
                         System.arraycopy(names, 0, newNames, 0, names.length);
                         names = newNames;
                     }
@@ -193,7 +193,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
      * @param n the bit number to lookup
      * @return the node type name
      */
-    private QName getName(int n) {
+    private Name getName(int n) {
         return names[n];
     }
 
@@ -222,7 +222,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
         BitsetENTCacheImpl clone = new BitsetENTCacheImpl();
         clone.sortedKeys.addAll(sortedKeys);
         clone.aggregates.putAll(aggregates);
-        clone.names = new QName[names.length];
+        clone.names = new Name[names.length];
         System.arraycopy(names, 0, clone.names, 0, names.length);
         clone.nameIndex.putAll(nameIndex);
         return clone;
@@ -255,7 +255,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
         /**
          * The names of the node types that form this key.
          */
-        private final QName[] names;
+        private final Name[] names;
 
         /**
          * The array of longs that hold the bit information.
@@ -272,7 +272,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
          * @param names the node type names
          * @param maxBit the approximative number of the geatest bit
          */
-        public BitsetKey(QName[] names, int maxBit) {
+        public BitsetKey(Name[] names, int maxBit) {
             this.names = names;
             bits = new long[maxBit/BPW+1];
 
@@ -290,7 +290,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
          */
         private BitsetKey(long[] bits, int numBits) {
             this.bits = bits;
-            names = new QName[numBits];
+            names = new Name[numBits];
             int i = nextSetBit(0);
             int j=0;
             while (i >= 0) {
@@ -303,7 +303,7 @@ class BitsetENTCacheImpl implements EffectiveNodeTypeCache {
         /**
          * {@inheritDoc}
          */
-        public QName[] getNames() {
+        public Name[] getNames() {
             return names;
         }
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/DefinitionValidator.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/DefinitionValidator.java
index 870bc36..ddc4928 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/DefinitionValidator.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/DefinitionValidator.java
@@ -16,11 +16,15 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeTypeDefinition;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QValue;
+import org.apache.jackrabbit.nodetype.InvalidNodeTypeDefException;
+import org.apache.jackrabbit.nodetype.NodeTypeConflictException;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.name.NameFactoryImpl;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -67,7 +71,7 @@ class DefinitionValidator {
         Map tmpMap = new HashMap(validatedDefs);
         for (Iterator it = ntDefs.iterator(); it.hasNext();) {
             QNodeTypeDefinition ntd = (QNodeTypeDefinition) it.next();
-            tmpMap.put(ntd.getQName(), ntd);
+            tmpMap.put(ntd.getName(), ntd);
         }
 
         // map of nodetype definitions and effective nodetypes to be registered
@@ -101,7 +105,7 @@ class DefinitionValidator {
             msg.append("the following node types could not be registered because of unresolvable dependencies: ");
             Iterator iterator = list.iterator();
             while (iterator.hasNext()) {
-                msg.append(((QNodeTypeDefinition) iterator.next()).getQName());
+                msg.append(((QNodeTypeDefinition) iterator.next()).getName());
                 msg.append(" ");
             }
             log.error(msg.toString());
@@ -132,7 +136,7 @@ class DefinitionValidator {
          */
         EffectiveNodeTypeImpl ent = null;
 
-        QName name = ntDef.getQName();
+        Name name = ntDef.getName();
         if (name == null) {
             String msg = "no name specified";
             log.debug(msg);
@@ -141,7 +145,7 @@ class DefinitionValidator {
         checkNamespace(name);
 
         // validate supertypes
-        QName[] supertypes = ntDef.getSupertypes();
+        Name[] supertypes = ntDef.getSupertypes();
         if (supertypes.length > 0) {
             for (int i = 0; i < supertypes.length; i++) {
                 checkNamespace(supertypes[i]);
@@ -187,8 +191,8 @@ class DefinitionValidator {
             try {
                 EffectiveNodeType est = entProvider.getEffectiveNodeType(supertypes, validatedDefs);
                 // make sure that all primary types except nt:base extend from nt:base
-                if (!ntDef.isMixin() && !QName.NT_BASE.equals(ntDef.getQName())
-                        && !est.includesNodeType(QName.NT_BASE)) {
+                if (!ntDef.isMixin() && !NameConstants.NT_BASE.equals(ntDef.getName())
+                        && !est.includesNodeType(NameConstants.NT_BASE)) {
                     String msg = "[" + name + "] all primary node types except"
                         + " nt:base itself must be (directly or indirectly) derived from nt:base";
                     log.debug(msg);
@@ -205,7 +209,7 @@ class DefinitionValidator {
             }
         } else {
             // no supertypes specified: has to be either a mixin type or nt:base
-            if (!ntDef.isMixin() && !QName.NT_BASE.equals(ntDef.getQName())) {
+            if (!ntDef.isMixin() && !NameConstants.NT_BASE.equals(ntDef.getName())) {
                 String msg = "[" + name
                         + "] all primary node types except nt:base itself must be (directly or indirectly) derived from nt:base";
                 log.debug(msg);
@@ -224,20 +228,20 @@ class DefinitionValidator {
              * make sure declaring node type matches name of node type definition
              */
             if (!name.equals(pd.getDeclaringNodeType())) {
-                String msg = "[" + name + "#" + pd.getQName() + "] invalid declaring node type specified";
+                String msg = "[" + name + "#" + pd.getName() + "] invalid declaring node type specified";
                 log.debug(msg);
                 throw new InvalidNodeTypeDefException(msg);
             }
-            checkNamespace(pd.getQName());
+            checkNamespace(pd.getName());
             // check that auto-created properties specify a name
             if (pd.definesResidual() && pd.isAutoCreated()) {
-                String msg = "[" + name + "#" + pd.getQName() + "] auto-created properties must specify a name";
+                String msg = "[" + name + "#" + pd.getName() + "] auto-created properties must specify a name";
                 log.debug(msg);
                 throw new InvalidNodeTypeDefException(msg);
             }
             // check that auto-created properties specify a type
             if (pd.getRequiredType() == PropertyType.UNDEFINED && pd.isAutoCreated()) {
-                String msg = "[" + name + "#" + pd.getQName() + "] auto-created properties must specify a type";
+                String msg = "[" + name + "#" + pd.getName() + "] auto-created properties must specify a type";
                 log.debug(msg);
                 throw new InvalidNodeTypeDefException(msg);
             }
@@ -263,10 +267,11 @@ class DefinitionValidator {
 
                 if (pd.getRequiredType() == PropertyType.REFERENCE) {
                     for (int j = 0; j < constraints.length; j++) {
-                        QName ntName = QName.valueOf(constraints[j]);
+                        // TODO improve. don't rely on a specific factory impl
+                        Name ntName = NameFactoryImpl.getInstance().create(constraints[j]);
                         /* compare to given ntd map and not registered nts only */
                         if (!name.equals(ntName) && !validatedDefs.containsKey(ntName)) {
-                            String msg = "[" + name + "#" + pd.getQName()
+                            String msg = "[" + name + "#" + pd.getName()
                                     + "] invalid REFERENCE value constraint '"
                                     + ntName + "' (unknown node type)";
                             log.debug(msg);
@@ -283,15 +288,15 @@ class DefinitionValidator {
             QNodeDefinition cnd = cnda[i];
             /* make sure declaring node type matches name of node type definition */
             if (!name.equals(cnd.getDeclaringNodeType())) {
-                String msg = "[" + name + "#" + cnd.getQName()
+                String msg = "[" + name + "#" + cnd.getName()
                         + "] invalid declaring node type specified";
                 log.debug(msg);
                 throw new InvalidNodeTypeDefException(msg);
             }
-            checkNamespace(cnd.getQName());
+            checkNamespace(cnd.getName());
             // check that auto-created child-nodes specify a name
             if (cnd.definesResidual() && cnd.isAutoCreated()) {
-                String msg = "[" + name + "#" + cnd.getQName()
+                String msg = "[" + name + "#" + cnd.getName()
                         + "] auto-created child-nodes must specify a name";
                 log.debug(msg);
                 throw new InvalidNodeTypeDefException(msg);
@@ -299,13 +304,13 @@ class DefinitionValidator {
             // check that auto-created child-nodes specify a default primary type
             if (cnd.getDefaultPrimaryType() == null
                     && cnd.isAutoCreated()) {
-                String msg = "[" + name + "#" + cnd.getQName()
+                String msg = "[" + name + "#" + cnd.getName()
                         + "] auto-created child-nodes must specify a default primary type";
                 log.debug(msg);
                 throw new InvalidNodeTypeDefException(msg);
             }
             // check default primary type
-            QName dpt = cnd.getDefaultPrimaryType();
+            Name dpt = cnd.getDefaultPrimaryType();
             checkNamespace(dpt);
             boolean referenceToSelf = false;
             EffectiveNodeType defaultENT = null;
@@ -319,7 +324,7 @@ class DefinitionValidator {
                  * exception: the node type just being registered
                  */
                 if (!name.equals(dpt) && !validatedDefs.containsKey(dpt)) {
-                    String msg = "[" + name + "#" + cnd.getQName()
+                    String msg = "[" + name + "#" + cnd.getName()
                             + "] invalid default primary type '" + dpt + "'";
                     log.debug(msg);
                     throw new InvalidNodeTypeDefException(msg);
@@ -330,7 +335,7 @@ class DefinitionValidator {
                  */
                 try {
                     if (!referenceToSelf) {
-                        defaultENT = entProvider.getEffectiveNodeType(new QName[] {dpt}, validatedDefs);
+                        defaultENT = entProvider.getEffectiveNodeType(new Name[] {dpt}, validatedDefs);
                     } else {
                         /**
                          * the default primary type is identical with the node
@@ -351,12 +356,12 @@ class DefinitionValidator {
                         checkForCircularNodeAutoCreation(defaultENT, definingNTs, validatedDefs);
                     }
                 } catch (NodeTypeConflictException ntce) {
-                    String msg = "[" + name + "#" + cnd.getQName()
+                    String msg = "[" + name + "#" + cnd.getName()
                             + "] failed to validate default primary type";
                     log.debug(msg);
                     throw new InvalidNodeTypeDefException(msg, ntce);
                 } catch (NoSuchNodeTypeException nsnte) {
-                    String msg = "[" + name + "#" + cnd.getQName()
+                    String msg = "[" + name + "#" + cnd.getName()
                             + "] failed to validate default primary type";
                     log.debug(msg);
                     throw new InvalidNodeTypeDefException(msg, nsnte);
@@ -364,10 +369,10 @@ class DefinitionValidator {
             }
 
             // check required primary types
-            QName[] reqTypes = cnd.getRequiredPrimaryTypes();
+            Name[] reqTypes = cnd.getRequiredPrimaryTypes();
             if (reqTypes != null && reqTypes.length > 0) {
                 for (int n = 0; n < reqTypes.length; n++) {
-                    QName rpt = reqTypes[n];
+                    Name rpt = reqTypes[n];
                     checkNamespace(rpt);
                     referenceToSelf = false;
                     /**
@@ -382,7 +387,7 @@ class DefinitionValidator {
                      * notable exception: the node type just being registered
                      */
                     if (!name.equals(rpt) && !validatedDefs.containsKey(rpt)) {
-                        String msg = "[" + name + "#" + cnd.getQName()
+                        String msg = "[" + name + "#" + cnd.getName()
                                 + "] invalid required primary type: " + rpt;
                         log.debug(msg);
                         throw new InvalidNodeTypeDefException(msg);
@@ -392,7 +397,7 @@ class DefinitionValidator {
                      * primary type constraint
                      */
                     if (defaultENT != null && !defaultENT.includesNodeType(rpt)) {
-                        String msg = "[" + name + "#" + cnd.getQName()
+                        String msg = "[" + name + "#" + cnd.getName()
                                 + "] default primary type does not satisfy required primary type constraint "
                                 + rpt;
                         log.debug(msg);
@@ -404,7 +409,7 @@ class DefinitionValidator {
                      */
                     try {
                         if (!referenceToSelf) {
-                            entProvider.getEffectiveNodeType(new QName[] {rpt}, validatedDefs);
+                            entProvider.getEffectiveNodeType(new Name[] {rpt}, validatedDefs);
                         } else {
                             /**
                              * the required primary type is identical with the
@@ -416,12 +421,12 @@ class DefinitionValidator {
                             }
                         }
                     } catch (NodeTypeConflictException ntce) {
-                        String msg = "[" + name + "#" + cnd.getQName()
+                        String msg = "[" + name + "#" + cnd.getName()
                                 + "] failed to validate required primary type constraint";
                         log.debug(msg);
                         throw new InvalidNodeTypeDefException(msg, ntce);
                     } catch (NoSuchNodeTypeException nsnte) {
-                        String msg = "[" + name + "#" + cnd.getQName()
+                        String msg = "[" + name + "#" + cnd.getName()
                                 + "] failed to validate required primary type constraint";
                         log.debug(msg);
                         throw new InvalidNodeTypeDefException(msg, nsnte);
@@ -459,10 +464,10 @@ class DefinitionValidator {
      * @throws InvalidNodeTypeDefException
      * @throws RepositoryException
      */
-    private void checkForCircularInheritance(QName[] supertypes, Stack inheritanceChain, Map ntdMap)
+    private void checkForCircularInheritance(Name[] supertypes, Stack inheritanceChain, Map ntdMap)
         throws InvalidNodeTypeDefException, RepositoryException {
         for (int i = 0; i < supertypes.length; i++) {
-            QName stName = supertypes[i];
+            Name stName = supertypes[i];
             int pos = inheritanceChain.lastIndexOf(stName);
             if (pos >= 0) {
                 StringBuffer buf = new StringBuffer();
@@ -479,7 +484,7 @@ class DefinitionValidator {
             }
 
             if (ntdMap.containsKey(stName)) {
-                QName[] sta = ((QNodeTypeDefinition)ntdMap.get(stName)).getSupertypes();
+                Name[] sta = ((QNodeTypeDefinition)ntdMap.get(stName)).getSupertypes();
                 if (sta.length > 0) {
                     // check recursively
                     inheritanceChain.push(stName);
@@ -504,9 +509,9 @@ class DefinitionValidator {
         throws InvalidNodeTypeDefException {
         // check for circularity through default node types of auto-created child nodes
         // (node type 'a' defines auto-created child node with default node type 'a')
-        QName[] childNodeNTs = childNodeENT.getAllNodeTypes();
+        Name[] childNodeNTs = childNodeENT.getAllNodeTypes();
         for (int i = 0; i < childNodeNTs.length; i++) {
-            QName nt = childNodeNTs[i];
+            Name nt = childNodeNTs[i];
             int pos = definingParentNTs.lastIndexOf(nt);
             if (pos >= 0) {
                 StringBuffer buf = new StringBuffer();
@@ -528,22 +533,22 @@ class DefinitionValidator {
 
         QNodeDefinition[] nodeDefs = childNodeENT.getAutoCreateQNodeDefinitions();
         for (int i = 0; i < nodeDefs.length; i++) {
-            QName dnt = nodeDefs[i].getDefaultPrimaryType();
-            QName definingNT = nodeDefs[i].getDeclaringNodeType();
+            Name dnt = nodeDefs[i].getDefaultPrimaryType();
+            Name definingNT = nodeDefs[i].getDeclaringNodeType();
             try {
                 if (dnt != null) {
                     // check recursively
                     definingParentNTs.push(definingNT);
-                    EffectiveNodeType ent = entProvider.getEffectiveNodeType(new QName[] {dnt}, ntdMap);
+                    EffectiveNodeType ent = entProvider.getEffectiveNodeType(new Name[] {dnt}, ntdMap);
                     checkForCircularNodeAutoCreation(ent, definingParentNTs, ntdMap);
                     definingParentNTs.pop();
                 }
             } catch (NoSuchNodeTypeException e) {
-                String msg = definingNT + " defines invalid default node type for child node " + nodeDefs[i].getQName();
+                String msg = definingNT + " defines invalid default node type for child node " + nodeDefs[i].getName();
                 log.debug(msg);
                 throw new InvalidNodeTypeDefException(msg, e);
             } catch (NodeTypeConflictException e) {
-                String msg = definingNT + " defines invalid default node type for child node " + nodeDefs[i].getQName();
+                String msg = definingNT + " defines invalid default node type for child node " + nodeDefs[i].getName();
                 log.debug(msg);
                 throw new InvalidNodeTypeDefException(msg, e);
             }
@@ -551,13 +556,13 @@ class DefinitionValidator {
     }
 
     /**
-     * Utility method for verifying that the namespace of a <code>QName</code>
+     * Utility method for verifying that the namespace of a <code>Name</code>
      * is registered; a <code>null</code> argument is silently ignored.
      * @param name name whose namespace is to be checked
      * @throws RepositoryException if the namespace of the given name is not
      *                             registered or if an unspecified error occured
      */
-    private void checkNamespace(QName name) throws RepositoryException {
+    private void checkNamespace(Name name) throws RepositoryException {
         if (name != null) {
             // make sure namespace uri denotes a registered namespace
             nsRegistry.getPrefix(name.getNamespaceURI());
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeType.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeType.java
index 4cccaa3..253ad57 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeType.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeType.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 
@@ -28,11 +28,11 @@ import javax.jcr.nodetype.NoSuchNodeTypeException;
  */
 public interface EffectiveNodeType {
 
-    public QName[] getAllNodeTypes();
+    public Name[] getAllNodeTypes();
 
-    public QName[] getInheritedNodeTypes();
+    public Name[] getInheritedNodeTypes();
 
-    public QName[] getMergedNodeTypes();
+    public Name[] getMergedNodeTypes();
 
     /**
      * Determines whether this effective node type representation includes
@@ -42,7 +42,7 @@ public interface EffectiveNodeType {
      * @return <code>true</code> if the given node type is included, otherwise
      *         <code>false</code>
      */
-    public boolean includesNodeType(QName nodeTypeName);
+    public boolean includesNodeType(Name nodeTypeName);
 
     /**
      * Determines whether this effective node type representation includes
@@ -52,7 +52,7 @@ public interface EffectiveNodeType {
      * @return <code>true</code> if all of the given node types are included,
      *         otherwise <code>false</code>
      */
-    public boolean includesNodeTypes(QName[] nodeTypeNames);
+    public boolean includesNodeTypes(Name[] nodeTypeNames);
 
     public QNodeDefinition[] getAllQNodeDefinitions();
 
@@ -66,9 +66,9 @@ public interface EffectiveNodeType {
 
     public QPropertyDefinition[] getMandatoryQPropertyDefinitions();
 
-    public QNodeDefinition[] getNamedQNodeDefinitions(QName name);
+    public QNodeDefinition[] getNamedQNodeDefinitions(Name name);
 
-    public QPropertyDefinition[] getNamedQPropertyDefinitions(QName name);
+    public QPropertyDefinition[] getNamedQPropertyDefinitions(Name name);
 
     public QNodeDefinition[] getUnnamedQNodeDefinitions();
 
@@ -79,7 +79,7 @@ public interface EffectiveNodeType {
      * @param definitionProvider
      * @throws ConstraintViolationException
      */
-    public void checkAddNodeConstraints(QName name, ItemDefinitionProvider definitionProvider)
+    public void checkAddNodeConstraints(Name name, ItemDefinitionProvider definitionProvider)
             throws ConstraintViolationException;
 
     /**
@@ -89,12 +89,12 @@ public interface EffectiveNodeType {
      * @throws ConstraintViolationException
      * @throws NoSuchNodeTypeException
      */
-    public void checkAddNodeConstraints(QName name, QName nodeTypeName, ItemDefinitionProvider definitionProvider)
+    public void checkAddNodeConstraints(Name name, Name nodeTypeName, ItemDefinitionProvider definitionProvider)
             throws ConstraintViolationException, NoSuchNodeTypeException;
 
     /**
      * @param name
      * @throws ConstraintViolationException
      */
-    public void checkRemoveItemConstraints(QName name) throws ConstraintViolationException;
+    public void checkRemoveItemConstraints(Name name) throws ConstraintViolationException;
 }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeCache.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeCache.java
index 0f8d2f7..448fe4f 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeCache.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeCache.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.jcr2spi.util.Dumpable;
 
 /**
@@ -61,14 +61,14 @@ public interface EffectiveNodeTypeCache extends Cloneable, Dumpable {
      * @param ntNames the array of node type names for the effective node type
      * @return the key to an effective node type.
      */
-    Key getKey(QName[] ntNames);
+    Key getKey(Name[] ntNames);
 
     /**
      * Removes all effective node types that are aggregated with the node type
      * of the given name.
      * @param name the name of the node type.
      */
-    void invalidate(QName name);
+    void invalidate(Name name);
 
     /**
      * {@inheritDoc}
@@ -101,7 +101,7 @@ public interface EffectiveNodeTypeCache extends Cloneable, Dumpable {
          * Returns the node type names of this key.
          * @return the node type names of this key.
          */
-        QName[] getNames();
+        Name[] getNames();
 
         /**
          * Checks if the <code>otherKey</code> is contained in this one. I.e. if
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeImpl.java
index 7df83b9..08753e7 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeImpl.java
@@ -19,8 +19,9 @@ package org.apache.jackrabbit.jcr2spi.nodetype;
 import org.apache.jackrabbit.spi.QItemDefinition;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeTypeDefinition;
+import org.apache.jackrabbit.nodetype.NodeTypeConflictException;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -86,7 +87,7 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
             throws NodeTypeConflictException, NoSuchNodeTypeException {
         // create empty effective node type instance
         EffectiveNodeTypeImpl ent = new EffectiveNodeTypeImpl();
-        QName ntName = ntd.getQName();
+        Name ntName = ntd.getName();
 
         // prepare new instance
         ent.mergedNodeTypes.add(ntName);
@@ -108,7 +109,7 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
                     msg = ntName + " contains ambiguous residual child node definitions";
                 } else {
                     msg = ntName + " contains ambiguous definitions for child node named "
-                            + cnda[i].getQName();
+                            + cnda[i].getName();
                 }
                 log.debug(msg);
                 throw new NodeTypeConflictException(msg);
@@ -120,7 +121,7 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
                 ent.unnamedItemDefs.add(cnda[i]);
             } else {
                 // named node definition
-                QName name = cnda[i].getQName();
+                Name name = cnda[i].getName();
                 List defs = (List) ent.namedItemDefs.get(name);
                 if (defs == null) {
                     defs = new ArrayList();
@@ -156,7 +157,7 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
                     msg = ntName + " contains ambiguous residual property definitions";
                 } else {
                     msg = ntName + " contains ambiguous definitions for property named "
-                            + pda[i].getQName();
+                            + pda[i].getName();
                 }
                 log.debug(msg);
                 throw new NodeTypeConflictException(msg);
@@ -168,7 +169,7 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
                 ent.unnamedItemDefs.add(pda[i]);
             } else {
                 // named property definition
-                QName name = pda[i].getQName();
+                Name name = pda[i].getName();
                 List defs = (List) ent.namedItemDefs.get(name);
                 if (defs == null) {
                     defs = new ArrayList();
@@ -195,7 +196,7 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
         }
 
         // resolve supertypes recursively
-        QName[] supertypes = ntd.getSupertypes();
+        Name[] supertypes = ntd.getSupertypes();
         if (supertypes.length > 0) {
             EffectiveNodeTypeImpl effSuperType = (EffectiveNodeTypeImpl)entProvider.getEffectiveNodeType(supertypes, ntdMap);
             ent.internalMerge(effSuperType, true);
@@ -218,22 +219,22 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
     /**
      * @see EffectiveNodeType#getInheritedNodeTypes()
      */
-    public QName[] getInheritedNodeTypes() {
-        return (QName[]) inheritedNodeTypes.toArray(new QName[inheritedNodeTypes.size()]);
+    public Name[] getInheritedNodeTypes() {
+        return (Name[]) inheritedNodeTypes.toArray(new Name[inheritedNodeTypes.size()]);
     }
 
     /**
      * @see EffectiveNodeType#getAllNodeTypes()
      */
-    public QName[] getAllNodeTypes() {
-        return (QName[]) allNodeTypes.toArray(new QName[allNodeTypes.size()]);
+    public Name[] getAllNodeTypes() {
+        return (Name[]) allNodeTypes.toArray(new Name[allNodeTypes.size()]);
     }
 
     /**
      * @see EffectiveNodeType#getMergedNodeTypes()
      */
-    public QName[] getMergedNodeTypes() {
-        return (QName[]) mergedNodeTypes.toArray(new QName[mergedNodeTypes.size()]);
+    public Name[] getMergedNodeTypes() {
+        return (Name[]) mergedNodeTypes.toArray(new Name[mergedNodeTypes.size()]);
     }
 
     /**
@@ -409,9 +410,9 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
     }
 
     /**
-     * @see EffectiveNodeType#getNamedQNodeDefinitions(QName)
+     * @see EffectiveNodeType#getNamedQNodeDefinitions(Name)
      */
-    public QNodeDefinition[] getNamedQNodeDefinitions(QName name) {
+    public QNodeDefinition[] getNamedQNodeDefinitions(Name name) {
         List list = (List) namedItemDefs.get(name);
         if (list == null || list.size() == 0) {
             return QNodeDefinition.EMPTY_ARRAY;
@@ -452,9 +453,9 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
     }
 
     /**
-     * @see EffectiveNodeType#getNamedQPropertyDefinitions(QName)
+     * @see EffectiveNodeType#getNamedQPropertyDefinitions(Name)
      */
-    public QPropertyDefinition[] getNamedQPropertyDefinitions(QName name) {
+    public QPropertyDefinition[] getNamedQPropertyDefinitions(Name name) {
         List list = (List) namedItemDefs.get(name);
         if (list == null || list.size() == 0) {
             return QPropertyDefinition.EMPTY_ARRAY;
@@ -497,22 +498,22 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
     /**
      * @inheritDoc
      */
-    public boolean includesNodeType(QName nodeTypeName) {
+    public boolean includesNodeType(Name nodeTypeName) {
         return allNodeTypes.contains(nodeTypeName);
     }
 
     /**
      * @inheritDoc
      */
-    public boolean includesNodeTypes(QName[] nodeTypeNames) {
+    public boolean includesNodeTypes(Name[] nodeTypeNames) {
         return allNodeTypes.containsAll(Arrays.asList(nodeTypeNames));
     }
 
     /**
      * @inheritDoc
-     * @see EffectiveNodeType#checkAddNodeConstraints(QName, ItemDefinitionProvider)
+     * @see EffectiveNodeType#checkAddNodeConstraints(Name, ItemDefinitionProvider)
      */
-    public void checkAddNodeConstraints(QName name, ItemDefinitionProvider definitionProvider)
+    public void checkAddNodeConstraints(Name name, ItemDefinitionProvider definitionProvider)
             throws ConstraintViolationException {
         try {
             definitionProvider.getQNodeDefinition(this, name, null);
@@ -525,9 +526,9 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
 
     /**
      * @inheritDoc
-     * @see EffectiveNodeType#checkAddNodeConstraints(QName, ItemDefinitionProvider)
+     * @see EffectiveNodeType#checkAddNodeConstraints(Name, ItemDefinitionProvider)
      */
-    public void checkAddNodeConstraints(QName name, QName nodeTypeName, ItemDefinitionProvider definitionProvider)
+    public void checkAddNodeConstraints(Name name, Name nodeTypeName, ItemDefinitionProvider definitionProvider)
             throws ConstraintViolationException, NoSuchNodeTypeException {
         QNodeDefinition nd = definitionProvider.getQNodeDefinition(this, name, nodeTypeName);
         if (nd.isProtected()) {
@@ -540,9 +541,9 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
 
     /**
      * @inheritDoc
-     * @see EffectiveNodeType#checkRemoveItemConstraints(QName)
+     * @see EffectiveNodeType#checkRemoveItemConstraints(Name)
      */
-    public void checkRemoveItemConstraints(QName name) throws ConstraintViolationException {
+    public void checkRemoveItemConstraints(Name name) throws ConstraintViolationException {
         /**
          * as there might be multiple definitions with the same name and we
          * don't know which one is applicable, we check all of them
@@ -577,7 +578,7 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
         return (QItemDefinition[]) defs.toArray(new QItemDefinition[defs.size()]);
     }
 
-    private QItemDefinition[] getNamedItemDefs(QName name) {
+    private QItemDefinition[] getNamedItemDefs(Name name) {
         List list = (List) namedItemDefs.get(name);
         if (list == null || list.size() == 0) {
             return QNodeDefinition.EMPTY_ARRAY;
@@ -626,7 +627,7 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
      */
     private synchronized void internalMerge(EffectiveNodeTypeImpl other, boolean supertype)
             throws NodeTypeConflictException {
-        QName[] nta = other.getAllNodeTypes();
+        Name[] nta = other.getAllNodeTypes();
         int includedCount = 0;
         for (int i = 0; i < nta.length; i++) {
             if (includesNodeType(nta[i])) {
@@ -648,7 +649,7 @@ public class EffectiveNodeTypeImpl implements Cloneable, EffectiveNodeType {
                 // ignore redundant definitions
                 continue;
             }
-            QName name = qDef.getQName();
+            Name name = qDef.getName();
             List existingDefs = (List) namedItemDefs.get(name);
             if (existingDefs != null) {
                 if (existingDefs.size() > 0) {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeProvider.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeProvider.java
index b3e6995..bdd40cd 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeProvider.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/EffectiveNodeTypeProvider.java
@@ -16,8 +16,9 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
+import org.apache.jackrabbit.nodetype.NodeTypeConflictException;
 
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.nodetype.ConstraintViolationException;
@@ -36,7 +37,7 @@ public interface EffectiveNodeTypeProvider {
      * @return
      * @throws NoSuchNodeTypeException
      */
-    public EffectiveNodeType getEffectiveNodeType(QName ntName)
+    public EffectiveNodeType getEffectiveNodeType(Name ntName)
             throws NoSuchNodeTypeException;
 
     /**
@@ -48,7 +49,7 @@ public interface EffectiveNodeTypeProvider {
      * @throws NodeTypeConflictException
      * @throws NoSuchNodeTypeException
      */
-    public EffectiveNodeType getEffectiveNodeType(QName[] ntNames)
+    public EffectiveNodeType getEffectiveNodeType(Name[] ntNames)
             throws NodeTypeConflictException, NoSuchNodeTypeException;
 
     /**
@@ -58,7 +59,7 @@ public interface EffectiveNodeTypeProvider {
      * @throws NodeTypeConflictException
      * @throws NoSuchNodeTypeException
      */
-    public EffectiveNodeType getEffectiveNodeType(QName[] ntNames, Map ntdMap)
+    public EffectiveNodeType getEffectiveNodeType(Name[] ntNames, Map ntdMap)
             throws NodeTypeConflictException, NoSuchNodeTypeException;
 
     /**
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionImpl.java
index 9add079..25f44b4 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionImpl.java
@@ -16,21 +16,20 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.NameFormat;
 import org.apache.jackrabbit.spi.QItemDefinition;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 import javax.jcr.nodetype.ItemDefinition;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.nodetype.NodeType;
+import javax.jcr.NamespaceException;
 
 /**
  * This class implements the <code>ItemDefinition</code> interface.
  * All method calls are delegated to the wrapped {@link QItemDefinition},
- * performing the translation from <code>QName</code>s to JCR names
+ * performing the translation from <code>Name</code>s to JCR names
  * (and vice versa) where necessary.
  */
 abstract class ItemDefinitionImpl implements ItemDefinition {
@@ -53,7 +52,7 @@ abstract class ItemDefinitionImpl implements ItemDefinition {
     /**
      * The namespace resolver used to translate qualified names to JCR names.
      */
-    protected final NamespaceResolver nsResolver;
+    protected final NamePathResolver resolver;
 
     /**
      * The wrapped item definition.
@@ -65,13 +64,13 @@ abstract class ItemDefinitionImpl implements ItemDefinition {
      *
      * @param itemDef    item definition
      * @param ntMgr      node type manager
-     * @param nsResolver namespace resolver
+     * @param resolver
      */
     ItemDefinitionImpl(QItemDefinition itemDef, NodeTypeManagerImpl ntMgr,
-                       NamespaceResolver nsResolver) {
+                       NamePathResolver resolver) {
         this.itemDef = itemDef;
         this.ntMgr = ntMgr;
-        this.nsResolver = nsResolver;
+        this.resolver = resolver;
     }
 
     //-------------------------------------------------------< ItemDefinition >
@@ -96,13 +95,12 @@ abstract class ItemDefinitionImpl implements ItemDefinition {
             return ANY_NAME;
         } else {
             try {
-                return NameFormat.format(itemDef.getQName(), nsResolver);
-            } catch (NoPrefixDeclaredException npde) {
+                return resolver.getJCRName(itemDef.getName());
+            } catch (NamespaceException e) {
                 // should never get here
-                log.error("encountered unregistered namespace in property name",
-                        npde);
+                log.error("encountered unregistered namespace in property name", e);
                 // not correct, but an acceptable fallback
-                return itemDef.getQName().toString();
+                return itemDef.getName().toString();
             }
         }
     }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionProvider.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionProvider.java
index 03ed4e8..10c1d0e 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionProvider.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionProvider.java
@@ -20,7 +20,7 @@ import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.state.PropertyState;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
@@ -47,7 +47,7 @@ public interface ItemDefinitionProvider {
      * @throws ConstraintViolationException if no applicable child node definition
      * could be found
      */
-    public QNodeDefinition getQNodeDefinition(NodeState parentState, QName name, QName nodeTypeName)
+    public QNodeDefinition getQNodeDefinition(NodeState parentState, Name name, Name nodeTypeName)
             throws NoSuchNodeTypeException, ConstraintViolationException;
 
     /**
@@ -62,7 +62,7 @@ public interface ItemDefinitionProvider {
      * @throws ConstraintViolationException if no applicable child node definition
      * could be found
      */
-    public QNodeDefinition getQNodeDefinition(EffectiveNodeType ent, QName name, QName nodeTypeName)
+    public QNodeDefinition getQNodeDefinition(EffectiveNodeType ent, Name name, Name nodeTypeName)
             throws NoSuchNodeTypeException, ConstraintViolationException;
 
     public QPropertyDefinition getQPropertyDefinition(PropertyState propertyState) throws RepositoryException;
@@ -83,8 +83,8 @@ public interface ItemDefinitionProvider {
      * @throws ConstraintViolationException if no applicable property definition
      *                                      could be found
      */
-    public QPropertyDefinition getQPropertyDefinition(QName ntName,
-                                                      QName propName, int type,
+    public QPropertyDefinition getQPropertyDefinition(Name ntName,
+                                                      Name propName, int type,
                                                       boolean multiValued)
             throws ConstraintViolationException, NoSuchNodeTypeException;
 
@@ -107,7 +107,7 @@ public interface ItemDefinitionProvider {
      *                                      could be found
      */
     public QPropertyDefinition getQPropertyDefinition(NodeState parentState,
-                                                      QName name, int type,
+                                                      Name name, int type,
                                                       boolean multiValued)
             throws ConstraintViolationException, NoSuchNodeTypeException;
 
@@ -115,7 +115,7 @@ public interface ItemDefinitionProvider {
      * Returns the applicable property definition for a property with the
      * specified name and type. The multiValued flag is not taken into account
      * in the selection algorithm. Other than
-     * <code>{@link #getApplicablePropertyDefinition(QName, int, boolean)}</code>
+     * <code>{@link #getApplicablePropertyDefinition(Name, int, boolean)}</code>
      * this method does not take the multiValued flag into account in the
      * selection algorithm. If there more than one applicable definitions then
      * the following rules are applied:
@@ -133,6 +133,6 @@ public interface ItemDefinitionProvider {
      * @throws ConstraintViolationException if no applicable property definition
      *                                      could be found
      */
-    public QPropertyDefinition getQPropertyDefinition(NodeState parentState, QName name, int type)
+    public QPropertyDefinition getQPropertyDefinition(NodeState parentState, Name name, int type)
             throws ConstraintViolationException, NoSuchNodeTypeException;
 }
\ No newline at end of file
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionProviderImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionProviderImpl.java
index 9618623..7fee105 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionProviderImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ItemDefinitionProviderImpl.java
@@ -26,7 +26,7 @@ import org.apache.jackrabbit.spi.QItemDefinition;
 import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.state.PropertyState;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.PropertyType;
@@ -85,11 +85,9 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
              */
             EffectiveNodeType ent = entProvider.getEffectiveNodeType(nodeState.getParent().getNodeTypeNames());
             EffectiveNodeType entTarget = getEffectiveNodeType(nodeState.getNodeTypeName());
-            definition = getQNodeDefinition(ent, entTarget, nodeState.getQName());
+            definition = getQNodeDefinition(ent, entTarget, nodeState.getName());
         } catch (RepositoryException e) {
             definition = service.getNodeDefinition(sessionInfo, nodeState.getNodeEntry().getWorkspaceId());
-        } catch (NodeTypeConflictException e) {
-            definition = service.getNodeDefinition(sessionInfo, nodeState.getNodeEntry().getWorkspaceId());
         }
         return definition;
     }
@@ -97,7 +95,7 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
    /**
      * @inheritDoc
      */
-    public QNodeDefinition getQNodeDefinition(NodeState parentState, QName name, QName nodeTypeName)
+    public QNodeDefinition getQNodeDefinition(NodeState parentState, Name name, Name nodeTypeName)
             throws NoSuchNodeTypeException, ConstraintViolationException {
        EffectiveNodeType ent = entProvider.getEffectiveNodeType(parentState);
        EffectiveNodeType entTarget = getEffectiveNodeType(nodeTypeName);
@@ -107,7 +105,7 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
     /**
      * @inheritDoc
      */
-    public QNodeDefinition getQNodeDefinition(EffectiveNodeType ent, QName name, QName nodeTypeName) throws NoSuchNodeTypeException, ConstraintViolationException {
+    public QNodeDefinition getQNodeDefinition(EffectiveNodeType ent, Name name, Name nodeTypeName) throws NoSuchNodeTypeException, ConstraintViolationException {
         EffectiveNodeType entTarget = getEffectiveNodeType(nodeTypeName);
         return getQNodeDefinition(ent, entTarget, name);
     }
@@ -129,11 +127,9 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
              evaluated upon creating the workspace state.
              */
             EffectiveNodeType ent = entProvider.getEffectiveNodeType(propertyState.getParent().getNodeTypeNames());
-            definition = getQPropertyDefinition(ent, propertyState.getQName(), propertyState.getType(), propertyState.isMultiValued(), true);
+            definition = getQPropertyDefinition(ent, propertyState.getName(), propertyState.getType(), propertyState.isMultiValued(), true);
         } catch (RepositoryException e) {
             definition = service.getPropertyDefinition(sessionInfo, ((PropertyEntry) propertyState.getHierarchyEntry()).getWorkspaceId());
-        } catch (NodeTypeConflictException e) {
-            definition = service.getPropertyDefinition(sessionInfo, ((PropertyEntry) propertyState.getHierarchyEntry()).getWorkspaceId());
         }
         return definition;
     }
@@ -141,7 +137,7 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
     /**
      * @inheritDoc
      */
-    public QPropertyDefinition getQPropertyDefinition(QName ntName, QName propName,
+    public QPropertyDefinition getQPropertyDefinition(Name ntName, Name propName,
                                                       int type, boolean multiValued)
             throws ConstraintViolationException, NoSuchNodeTypeException {
         EffectiveNodeType ent = entProvider.getEffectiveNodeType(ntName);
@@ -152,7 +148,7 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
      * @inheritDoc
      */
     public QPropertyDefinition getQPropertyDefinition(NodeState parentState,
-                                                      QName name, int type,
+                                                      Name name, int type,
                                                       boolean multiValued)
             throws ConstraintViolationException, NoSuchNodeTypeException {
         EffectiveNodeType ent = entProvider.getEffectiveNodeType(parentState);
@@ -163,14 +159,14 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
      * @inheritDoc
      */
     public QPropertyDefinition getQPropertyDefinition(NodeState parentState,
-                                                      QName name, int type)
+                                                      Name name, int type)
             throws ConstraintViolationException, NoSuchNodeTypeException {
         EffectiveNodeType ent = entProvider.getEffectiveNodeType(parentState);
         return getQPropertyDefinition(ent, name, type);
     }
 
     //--------------------------------------------------------------------------
-    private EffectiveNodeType getEffectiveNodeType(QName ntName) throws NoSuchNodeTypeException {
+    private EffectiveNodeType getEffectiveNodeType(Name ntName) throws NoSuchNodeTypeException {
         if (ntName != null) {
             return entProvider.getEffectiveNodeType(ntName);
         } else {
@@ -188,7 +184,7 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
      */
     static QNodeDefinition getQNodeDefinition(EffectiveNodeType ent,
                                               EffectiveNodeType entTarget,
-                                              QName name)
+                                              Name name)
             throws ConstraintViolationException {
 
         // try named node definitions first
@@ -247,7 +243,7 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
      * @throws ConstraintViolationException
      */
     private static QPropertyDefinition getQPropertyDefinition(EffectiveNodeType ent,
-                                                              QName name, int type,
+                                                              Name name, int type,
                                                               boolean multiValued, boolean throwWhenAmbiguous)
            throws ConstraintViolationException {
         // try named property definitions first
@@ -278,7 +274,7 @@ public class ItemDefinitionProviderImpl implements ItemDefinitionProvider {
      * @throws ConstraintViolationException
      */
     private static QPropertyDefinition getQPropertyDefinition(EffectiveNodeType ent,
-                                                              QName name, int type)
+                                                              Name name, int type)
             throws ConstraintViolationException {
         // try named property definitions first
         QPropertyDefinition[] defs = ent.getNamedQPropertyDefinitions(name);
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeDefinitionImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeDefinitionImpl.java
index 04570d7..f242124 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeDefinitionImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeDefinitionImpl.java
@@ -16,9 +16,10 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeDefinition;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.name.NameConstants;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -29,7 +30,7 @@ import javax.jcr.nodetype.NodeType;
 /**
  * This class implements the <code>NodeDefinition</code> interface.
  * All method calls are delegated to the wrapped {@link QNodeDefinition},
- * performing the translation from <code>QName</code>s to JCR names
+ * performing the translation from <code>Name</code>s to JCR names
  * where necessary.
  */
 public class NodeDefinitionImpl extends ItemDefinitionImpl implements NodeDefinition {
@@ -44,11 +45,11 @@ public class NodeDefinitionImpl extends ItemDefinitionImpl implements NodeDefini
      *
      * @param nodeDef    child node definition
      * @param ntMgr      node type manager
-     * @param nsResolver namespace resolver
+     * @param resolver
      */
     NodeDefinitionImpl(QNodeDefinition nodeDef, NodeTypeManagerImpl ntMgr,
-                NamespaceResolver nsResolver) {
-        super(nodeDef, ntMgr, nsResolver);
+                       NamePathResolver resolver) {
+        super(nodeDef, ntMgr, resolver);
     }
 
     //-------------------------------------------------------< NodeDefinition >
@@ -56,7 +57,7 @@ public class NodeDefinitionImpl extends ItemDefinitionImpl implements NodeDefini
      * {@inheritDoc}
      */
     public NodeType getDefaultPrimaryType() {
-        QName ntName = ((QNodeDefinition) itemDef).getDefaultPrimaryType();
+        Name ntName = ((QNodeDefinition) itemDef).getDefaultPrimaryType();
         if (ntName == null) {
             return null;
         }
@@ -73,11 +74,11 @@ public class NodeDefinitionImpl extends ItemDefinitionImpl implements NodeDefini
      * {@inheritDoc}
      */
     public NodeType[] getRequiredPrimaryTypes() {
-        QName[] ntNames = ((QNodeDefinition) itemDef).getRequiredPrimaryTypes();
+        Name[] ntNames = ((QNodeDefinition) itemDef).getRequiredPrimaryTypes();
         try {
             if (ntNames == null || ntNames.length == 0) {
                 // return "nt:base"
-                return new NodeType[] { ntMgr.getNodeType(QName.NT_BASE) };
+                return new NodeType[] { ntMgr.getNodeType(NameConstants.NT_BASE) };
             } else {
                 NodeType[] nodeTypes = new NodeType[ntNames.length];
                 for (int i = 0; i < ntNames.length; i++) {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeCache.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeCache.java
index 485ac8e..3cfcfc5 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeCache.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeCache.java
@@ -17,7 +17,7 @@
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
 import org.apache.commons.collections.map.ReferenceMap;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeTypeDefinition;
 import org.apache.jackrabbit.spi.RepositoryService;
 
@@ -42,7 +42,7 @@ public class NodeTypeCache {
     private static final Map CACHES_PER_SERVICE = new WeakHashMap();
 
     /**
-     * Maps node type QNames to QNodeTypeDefinition
+     * Maps node type Names to QNodeTypeDefinition
      */
     private final Map nodeTypes = new HashMap();
 
@@ -91,7 +91,7 @@ public class NodeTypeCache {
         Map allNts = new HashMap();
         for (Iterator it = storage.getAllDefinitions(); it.hasNext(); ) {
             QNodeTypeDefinition def = (QNodeTypeDefinition) it.next();
-            allNts.put(def.getQName(), def);
+            allNts.put(def.getName(), def);
         }
         // update the cache
         synchronized (nodeTypes) {
@@ -111,7 +111,7 @@ public class NodeTypeCache {
      * @throws javax.jcr.nodetype.NoSuchNodeTypeException
      * @throws RepositoryException
      */
-    public Iterator getDefinitions(NodeTypeStorage storage, QName[] nodeTypeNames)
+    public Iterator getDefinitions(NodeTypeStorage storage, Name[] nodeTypeNames)
             throws NoSuchNodeTypeException, RepositoryException {
         List nts = new ArrayList();
         List missing = null;
@@ -129,13 +129,13 @@ public class NodeTypeCache {
             }
         }
         if (missing != null) {
-            QName[] ntNames = (QName[]) missing.toArray(new QName[missing.size()]);
+            Name[] ntNames = (Name[]) missing.toArray(new Name[missing.size()]);
             Iterator it = storage.getDefinitions(ntNames);
             synchronized (nodeTypes) {
                 while (it.hasNext()) {
                     QNodeTypeDefinition def = (QNodeTypeDefinition) it.next();
                     nts.add(def);
-                    nodeTypes.put(def.getQName(), def);
+                    nodeTypes.put(def.getName(), def);
                 }
             }
         }
@@ -155,7 +155,7 @@ public class NodeTypeCache {
     }
 
     public void unregisterNodeTypes(NodeTypeStorage storage,
-                                    QName[] nodeTypeNames)
+                                    Name[] nodeTypeNames)
             throws NoSuchNodeTypeException, RepositoryException {
         throw new UnsupportedOperationException("NodeType registration not yet defined by the SPI");
     }
@@ -173,7 +173,7 @@ public class NodeTypeCache {
             public Iterator getAllDefinitions() throws RepositoryException {
                 return NodeTypeCache.this.getAllDefinitions(storage);
             }
-            public Iterator getDefinitions(QName[] nodeTypeNames)
+            public Iterator getDefinitions(Name[] nodeTypeNames)
                     throws NoSuchNodeTypeException, RepositoryException {
                 return NodeTypeCache.this.getDefinitions(storage, nodeTypeNames);
             }
@@ -185,7 +185,7 @@ public class NodeTypeCache {
                     throws NoSuchNodeTypeException, RepositoryException {
                 NodeTypeCache.this.reregisterNodeTypes(storage, nodeTypeDefs);
             }
-            public void unregisterNodeTypes(QName[] nodeTypeNames)
+            public void unregisterNodeTypes(Name[] nodeTypeNames)
                     throws NoSuchNodeTypeException, RepositoryException {
                 NodeTypeCache.this.unregisterNodeTypes(storage, nodeTypeNames);
             }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeImpl.java
index 56d60e7..20124f2 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeImpl.java
@@ -16,13 +16,9 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QNodeTypeDefinition;
@@ -31,6 +27,7 @@ import org.apache.jackrabbit.spi.QValueFactory;
 import org.apache.jackrabbit.value.ValueHelper;
 import org.apache.jackrabbit.value.ValueFormat;
 import org.apache.jackrabbit.jcr2spi.ManagerProvider;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -38,6 +35,7 @@ import javax.jcr.PropertyType;
 import javax.jcr.RepositoryException;
 import javax.jcr.Value;
 import javax.jcr.ValueFactory;
+import javax.jcr.NamespaceException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.nodetype.NodeDefinition;
@@ -67,7 +65,7 @@ public class NodeTypeImpl implements NodeType {
      * @param ent        the effective (i.e. merged and resolved) node type representation
      * @param ntd        the definition of this node type
      * @param ntMgr      the node type manager associated with this node type
-     * @param nsResolver namespace resolver
+     * @param mgrProvider
      */
     NodeTypeImpl(EffectiveNodeType ent, QNodeTypeDefinition ntd,
                  NodeTypeManagerImpl ntMgr, ManagerProvider mgrProvider) {
@@ -81,6 +79,10 @@ public class NodeTypeImpl implements NodeType {
         return mgrProvider.getNamespaceResolver();
     }
 
+    private NamePathResolver resolver() {
+        return mgrProvider.getNamePathResolver();
+    }
+
     private ItemDefinitionProvider definitionProvider() {
         return mgrProvider.getItemDefinitionProvider();
     }
@@ -115,7 +117,7 @@ public class NodeTypeImpl implements NodeType {
      * @throws RepositoryException if no applicable property definition
      *                             could be found
      */
-    private QPropertyDefinition getApplicablePropDef(QName propertyName, int type, boolean multiValued)
+    private QPropertyDefinition getApplicablePropDef(Name propertyName, int type, boolean multiValued)
             throws RepositoryException {
         return definitionProvider().getQPropertyDefinition(getQName(), propertyName, type, multiValued);
     }
@@ -128,8 +130,8 @@ public class NodeTypeImpl implements NodeType {
      * @return true if this node type is equal or directly or indirectly derived
      * from the specified node type, otherwise false.
      */
-    public boolean isNodeType(QName nodeTypeName) {
-        return getQName().equals(nodeTypeName) ||  ent.includesNodeType(nodeTypeName);
+    public boolean isNodeType(Name nodeTypeName) {
+        return getName().equals(nodeTypeName) ||  ent.includesNodeType(nodeTypeName);
     }
 
     /**
@@ -156,8 +158,8 @@ public class NodeTypeImpl implements NodeType {
      *
      * @return the qualified name
      */
-    private QName getQName() {
-        return ntd.getQName();
+    private Name getQName() {
+        return ntd.getName();
     }
 
     //-----------------------------------------------------------< NodeType >---
@@ -166,11 +168,11 @@ public class NodeTypeImpl implements NodeType {
      */
     public String getName() {
         try {
-            return NameFormat.format(ntd.getQName(), nsResolver());
-        } catch (NoPrefixDeclaredException npde) {
+            return resolver().getJCRName(ntd.getName());
+        } catch (NamespaceException e) {
             // should never get here
-            log.error("encountered unregistered namespace in node type name", npde);
-            return ntd.getQName().toString();
+            log.error("encountered unregistered namespace in node type name", e);
+            return ntd.getName().toString();
         }
     }
 
@@ -179,16 +181,16 @@ public class NodeTypeImpl implements NodeType {
      */
     public String getPrimaryItemName() {
         try {
-            QName piName = ntd.getPrimaryItemName();
+            Name piName = ntd.getPrimaryItemName();
             if (piName != null) {
-                return NameFormat.format(piName, nsResolver());
+                return resolver().getJCRName(piName);
             } else {
                 return null;
             }
-        } catch (NoPrefixDeclaredException npde) {
+        } catch (NamespaceException e) {
             // should never get here
-            log.error("encountered unregistered namespace in name of primary item", npde);
-            return ntd.getQName().toString();
+            log.error("encountered unregistered namespace in name of primary item", e);
+            return ntd.getName().toString();
         }
     }
 
@@ -203,14 +205,14 @@ public class NodeTypeImpl implements NodeType {
      * {@inheritDoc}
      */
     public boolean isNodeType(String nodeTypeName) {
-        QName ntName;
+        Name ntName;
         try {
-            ntName = NameFormat.parse(nodeTypeName, nsResolver());
-        } catch (IllegalNameException ine) {
-            log.warn("invalid node type name: " + nodeTypeName, ine);
+            ntName = resolver().getQName(nodeTypeName);
+        } catch (NamespaceException e) {
+            log.warn("invalid node type name: " + nodeTypeName, e);
             return false;
-        } catch (UnknownPrefixException upe) {
-            log.warn("invalid node type name: " + nodeTypeName, upe);
+        } catch (NameException e) {
+            log.warn("invalid node type name: " + nodeTypeName, e);
             return false;
         }
         return isNodeType(ntName);
@@ -227,7 +229,7 @@ public class NodeTypeImpl implements NodeType {
      * {@inheritDoc}
      */
     public NodeType[] getSupertypes() {
-        QName[] ntNames = ent.getInheritedNodeTypes();
+        Name[] ntNames = ent.getInheritedNodeTypes();
         NodeType[] supertypes = new NodeType[ntNames.length];
         for (int i = 0; i < ntNames.length; i++) {
             try {
@@ -269,7 +271,7 @@ public class NodeTypeImpl implements NodeType {
      * {@inheritDoc}
      */
     public NodeType[] getDeclaredSupertypes() {
-        QName[] ntNames = ntd.getSupertypes();
+        Name[] ntNames = ntd.getSupertypes();
         NodeType[] supertypes = new NodeType[ntNames.length];
         for (int i = 0; i < ntNames.length; i++) {
             try {
@@ -304,7 +306,7 @@ public class NodeTypeImpl implements NodeType {
             return canRemoveItem(propertyName);
         }
         try {
-            QName name = NameFormat.parse(propertyName, nsResolver());
+            Name name = resolver().getQName(propertyName);
             QPropertyDefinition def;
             try {
                 // try to get definition that matches the given value type
@@ -329,12 +331,12 @@ public class NodeTypeImpl implements NodeType {
                 v = value;
             }
             // create QValue from Value
-            QValue qValue = ValueFormat.getQValue(v, nsResolver(), qValueFactory());
+            QValue qValue = ValueFormat.getQValue(v, resolver(), qValueFactory());
             checkSetPropertyValueConstraints(def, new QValue[]{qValue});
             return true;
-        } catch (NameException be) {
-            // implementation specific exception, fall through
-        } catch (RepositoryException re) {
+        } catch (NameException re) {
+            // fall through
+        } catch (RepositoryException e) {
             // fall through
         }
         return false;
@@ -349,7 +351,7 @@ public class NodeTypeImpl implements NodeType {
             return canRemoveItem(propertyName);
         }
         try {
-            QName name = NameFormat.parse(propertyName, nsResolver());
+            Name name = resolver().getQName(propertyName);
             // determine type of values
             int type = PropertyType.UNDEFINED;
             for (int i = 0; i < values.length; i++) {
@@ -397,7 +399,7 @@ public class NodeTypeImpl implements NodeType {
                     // create QValue from Value and perform
                     // type conversion as necessary
                     Value v = ValueHelper.convert(values[i], targetType, valueFactory());
-                    QValue qValue = ValueFormat.getQValue(v, nsResolver(), qValueFactory());
+                    QValue qValue = ValueFormat.getQValue(v, resolver(), qValueFactory());
                     list.add(qValue);
                 }
             }
@@ -417,7 +419,7 @@ public class NodeTypeImpl implements NodeType {
      */
     public boolean canAddChildNode(String childNodeName) {
         try {
-            ent.checkAddNodeConstraints(NameFormat.parse(childNodeName, nsResolver()), definitionProvider());
+            ent.checkAddNodeConstraints(resolver().getQName(childNodeName), definitionProvider());
             return true;
         } catch (NameException be) {
             // implementation specific exception, fall through
@@ -432,8 +434,8 @@ public class NodeTypeImpl implements NodeType {
      */
     public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
         try {
-            ent.checkAddNodeConstraints(NameFormat.parse(childNodeName, nsResolver()),
-                NameFormat.parse(nodeTypeName, nsResolver()), definitionProvider());
+            ent.checkAddNodeConstraints(resolver().getQName(childNodeName),
+                resolver().getQName(nodeTypeName), definitionProvider());
             return true;
         } catch (NameException be) {
             // implementation specific exception, fall through
@@ -448,7 +450,7 @@ public class NodeTypeImpl implements NodeType {
      */
     public boolean canRemoveItem(String itemName) {
         try {
-            ent.checkRemoveItemConstraints(NameFormat.parse(itemName, nsResolver()));
+            ent.checkRemoveItemConstraints(resolver().getQName(itemName));
             return true;
         } catch (NameException be) {
             // implementation specific exception, fall through
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeManagerImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeManagerImpl.java
index bf049d2..04d9685 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeManagerImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeManagerImpl.java
@@ -17,12 +17,10 @@
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
 import org.apache.commons.collections.map.ReferenceMap;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.UnknownPrefixException;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.util.IteratorHelper;
 import org.apache.jackrabbit.jcr2spi.util.Dumpable;
 import org.apache.jackrabbit.jcr2spi.ManagerProvider;
@@ -36,6 +34,7 @@ import javax.jcr.RepositoryException;
 import javax.jcr.PropertyType;
 import javax.jcr.Value;
 import javax.jcr.ValueFactory;
+import javax.jcr.NamespaceException;
 import javax.jcr.version.OnParentVersionAction;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.nodetype.NodeType;
@@ -118,6 +117,10 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
         return mgrProvider.getNamespaceResolver();
     }
 
+    private NamePathResolver resolver() {
+        return mgrProvider.getNamePathResolver();
+    }
+
     private EffectiveNodeTypeProvider entProvider() {
         return mgrProvider.getEffectiveNodeTypeProvider();
     }
@@ -127,7 +130,7 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
      * @return
      * @throws NoSuchNodeTypeException
      */
-    public NodeTypeImpl getNodeType(QName name) throws NoSuchNodeTypeException {
+    public NodeTypeImpl getNodeType(Name name) throws NoSuchNodeTypeException {
         synchronized (ntCache) {
             NodeTypeImpl nt = (NodeTypeImpl) ntCache.get(name);
             if (nt == null) {
@@ -145,7 +148,7 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
      * @param nodeTypeName
      * @return
      */
-    public boolean hasNodeType(QName nodeTypeName) {
+    public boolean hasNodeType(Name nodeTypeName) {
         boolean isRegistered = ntCache.containsKey(nodeTypeName);
         if (!isRegistered) {
             isRegistered = ntReg.isRegistered(nodeTypeName);
@@ -164,7 +167,7 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
         synchronized (ndCache) {
             NodeDefinition ndi = (NodeDefinition) ndCache.get(def);
             if (ndi == null) {
-                ndi = new NodeDefinitionImpl(def, this, nsResolver());
+                ndi = new NodeDefinitionImpl(def, this, resolver());
                 ndCache.put(def, ndi);
             }
             return ndi;
@@ -182,7 +185,7 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
         synchronized (pdCache) {
             PropertyDefinition pdi = (PropertyDefinition) pdCache.get(def);
             if (pdi == null) {
-                pdi = new PropertyDefinitionImpl(def, this, nsResolver(), valueFactory);
+                pdi = new PropertyDefinitionImpl(def, this, resolver(), valueFactory);
                 pdCache.put(def, pdi);
             }
             return pdi;
@@ -199,18 +202,18 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
     /**
      * {@inheritDoc}
      */
-    public void nodeTypeRegistered(QName ntName) {
+    public void nodeTypeRegistered(Name ntName) {
         // not interested, ignore
     }
 
     /**
      * {@inheritDoc}
      */
-    public void nodeTypeReRegistered(QName ntName) {
+    public void nodeTypeReRegistered(Name ntName) {
         // flush all affected cache entries
         ntCache.remove(ntName);
         try {
-            String name = NameFormat.format(ntName, nsResolver());
+            String name = resolver().getJCRName(ntName);
             synchronized (pdCache) {
                 Iterator iter = pdCache.values().iterator();
                 while (iter.hasNext()) {
@@ -229,7 +232,7 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
                     }
                 }
             }
-        } catch (NameException e) {
+        } catch (NamespaceException e) {
             log.warn(e.getMessage() + " -> clear definition cache." );
             synchronized (pdCache) {
                 pdCache.clear();
@@ -243,11 +246,11 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
     /**
      * {@inheritDoc}
      */
-    public void nodeTypeUnregistered(QName ntName) {
+    public void nodeTypeUnregistered(Name ntName) {
         // flush all affected cache entries
         ntCache.remove(ntName);
         try {
-            String name = NameFormat.format(ntName, nsResolver());
+            String name = resolver().getJCRName(ntName);
             synchronized (pdCache) {
                 Iterator iter = pdCache.values().iterator();
                 while (iter.hasNext()) {
@@ -266,7 +269,7 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
                     }
                 }
             }
-        } catch (NameException e) {
+        } catch (NamespaceException e) {
             log.warn(e.getMessage() + " -> clear definition cache." );
             synchronized (pdCache) {
                 pdCache.clear();
@@ -282,7 +285,7 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
      * {@inheritDoc}
      */
     public NodeTypeIterator getAllNodeTypes() throws RepositoryException {
-        QName[] ntNames = ntReg.getRegisteredNodeTypes();
+        Name[] ntNames = ntReg.getRegisteredNodeTypes();
         ArrayList list = new ArrayList(ntNames.length);
         for (int i = 0; i < ntNames.length; i++) {
             list.add(getNodeType(ntNames[i]));
@@ -294,7 +297,7 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
      * {@inheritDoc}
      */
     public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
-        QName[] ntNames = ntReg.getRegisteredNodeTypes();
+        Name[] ntNames = ntReg.getRegisteredNodeTypes();
         ArrayList list = new ArrayList(ntNames.length);
         for (int i = 0; i < ntNames.length; i++) {
             NodeType nt = getNodeType(ntNames[i]);
@@ -309,7 +312,7 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
      * {@inheritDoc}
      */
     public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
-        QName[] ntNames = ntReg.getRegisteredNodeTypes();
+        Name[] ntNames = ntReg.getRegisteredNodeTypes();
         ArrayList list = new ArrayList(ntNames.length);
         for (int i = 0; i < ntNames.length; i++) {
             NodeType nt = getNodeType(ntNames[i]);
@@ -326,12 +329,12 @@ public class NodeTypeManagerImpl implements NodeTypeManager, NodeTypeRegistryLis
     public NodeType getNodeType(String nodeTypeName)
             throws NoSuchNodeTypeException {
         try {
-            QName qName = NameFormat.parse(nodeTypeName, nsResolver());
+            Name qName = resolver().getQName(nodeTypeName);
             return getNodeType(qName);
-        } catch (UnknownPrefixException upe) {
-            throw new NoSuchNodeTypeException(nodeTypeName, upe);
-        } catch (IllegalNameException ine) {
-            throw new NoSuchNodeTypeException(nodeTypeName, ine);
+        } catch (NamespaceException e) {
+            throw new NoSuchNodeTypeException(nodeTypeName, e);
+        } catch (NameException e) {
+            throw new NoSuchNodeTypeException(nodeTypeName, e);
         }
     }
     
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistry.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistry.java
index ab011f1..71a658e 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistry.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistry.java
@@ -16,8 +16,9 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeTypeDefinition;
+import org.apache.jackrabbit.nodetype.InvalidNodeTypeDefException;
 
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.RepositoryException;
@@ -37,7 +38,7 @@ public interface NodeTypeRegistry {
      * @throws NoSuchNodeTypeException if a node type with the given name
      *                                 does not exist
      */
-    QNodeTypeDefinition getNodeTypeDefinition(QName nodeTypeName)
+    QNodeTypeDefinition getNodeTypeDefinition(Name nodeTypeName)
             throws NoSuchNodeTypeException;
 
     /**
@@ -59,7 +60,7 @@ public interface NodeTypeRegistry {
      * @param ntName
      * @return
      */
-    boolean isRegistered(QName ntName);
+    boolean isRegistered(Name ntName);
 
     /**
      * Returns the names of all registered node types. That includes primary
@@ -67,7 +68,7 @@ public interface NodeTypeRegistry {
      *
      * @return the names of all registered node types.
      */
-    public QName[] getRegisteredNodeTypes() throws RepositoryException;
+    public Name[] getRegisteredNodeTypes() throws RepositoryException;
 
     /**
      * Validates the <code>NodeTypeDef</code> and returns
@@ -131,22 +132,22 @@ public interface NodeTypeRegistry {
      * @throws NoSuchNodeTypeException
      * @throws RepositoryException
      */
-    public void unregisterNodeType(QName nodeTypeName)
+    public void unregisterNodeType(Name nodeTypeName)
             throws NoSuchNodeTypeException, RepositoryException;
 
     /**
-     * Same as <code>{@link #unregisterNodeType(QName)}</code> except
+     * Same as <code>{@link #unregisterNodeType(Name)}</code> except
      * that a set of node types is unregistered instead of just one.
      * <p/>
      * This method can be used to unregister a set of node types that depend on
      * each other.
      *
-     * @param nodeTypeNames a collection of <code>QName</code> objects denoting the
+     * @param nodeTypeNames a collection of <code>Name</code> objects denoting the
      *                node types to be unregistered
      * @throws NoSuchNodeTypeException if any of the specified names does not
      *                                 denote a registered node type.
      * @throws RepositoryException if another error occurs
-     * @see #unregisterNodeType(QName)
+     * @see #unregisterNodeType(Name)
      */
     public void unregisterNodeTypes(Collection nodeTypeNames)
         throws NoSuchNodeTypeException, RepositoryException;
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistryImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistryImpl.java
index 818b99c..06bdfc5 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistryImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistryImpl.java
@@ -22,11 +22,14 @@ import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.state.Status;
 import org.apache.jackrabbit.jcr2spi.state.PropertyState;
 import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QNodeTypeDefinition;
 import org.apache.jackrabbit.spi.QValue;
+import org.apache.jackrabbit.nodetype.InvalidNodeTypeDefException;
+import org.apache.jackrabbit.nodetype.NodeTypeConflictException;
+import org.apache.jackrabbit.name.NameConstants;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -141,16 +144,16 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
     /**
      * @see NodeTypeRegistry#getRegisteredNodeTypes()
      */
-    public QName[] getRegisteredNodeTypes() throws RepositoryException {
+    public Name[] getRegisteredNodeTypes() throws RepositoryException {
         Set qNames = registeredNTDefs.keySet();
-        return (QName[]) qNames.toArray(new QName[registeredNTDefs.size()]);
+        return (Name[]) qNames.toArray(new Name[registeredNTDefs.size()]);
     }
 
 
     /**
-     * @see NodeTypeRegistry#isRegistered(QName)
+     * @see NodeTypeRegistry#isRegistered(Name)
      */
-    public boolean isRegistered(QName nodeTypeName) {
+    public boolean isRegistered(Name nodeTypeName) {
         return registeredNTDefs.containsKey(nodeTypeName);
     }
 
@@ -169,7 +172,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
         internalRegister(ntDef, ent);
 
         // notify listeners
-        notifyRegistered(ntDef.getQName());
+        notifyRegistered(ntDef.getName());
         return ent;
     }
 
@@ -188,15 +191,15 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
 
         // notify listeners
         for (Iterator iter = ntDefs.iterator(); iter.hasNext();) {
-            QName ntName = ((QNodeTypeDefinition)iter.next()).getQName();
+            Name ntName = ((QNodeTypeDefinition)iter.next()).getName();
             notifyRegistered(ntName);
         }
     }
 
     /**
-     * @see NodeTypeRegistry#unregisterNodeType(QName)
+     * @see NodeTypeRegistry#unregisterNodeType(Name)
      */
-    public void unregisterNodeType(QName nodeTypeName) throws NoSuchNodeTypeException, RepositoryException {
+    public void unregisterNodeType(Name nodeTypeName) throws NoSuchNodeTypeException, RepositoryException {
         HashSet ntNames = new HashSet();
         ntNames.add(nodeTypeName);
         unregisterNodeTypes(ntNames);
@@ -209,7 +212,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
             throws NoSuchNodeTypeException, RepositoryException {
         // do some preliminary checks
         for (Iterator iter = nodeTypeNames.iterator(); iter.hasNext();) {
-            QName ntName = (QName) iter.next();
+            Name ntName = (Name) iter.next();
             
             // Best effort check for node types other than those to be
             // unregistered that depend on the given node types
@@ -228,7 +231,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
 
         // persist removal of node type definitions
         // NOTE: conflict with existing content not asserted on client
-        storage.unregisterNodeTypes((QName[]) nodeTypeNames.toArray(new QName[nodeTypeNames.size()]));
+        storage.unregisterNodeTypes((Name[]) nodeTypeNames.toArray(new Name[nodeTypeNames.size()]));
 
 
         // all preconditions are met, node types can now safely be unregistered
@@ -236,7 +239,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
 
         // notify listeners
         for (Iterator iter = nodeTypeNames.iterator(); iter.hasNext();) {
-            QName ntName = (QName) iter.next();
+            Name ntName = (Name) iter.next();
             notifyUnregistered(ntName);
         }
     }
@@ -247,7 +250,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
     public synchronized EffectiveNodeType reregisterNodeType(QNodeTypeDefinition ntd)
             throws NoSuchNodeTypeException, InvalidNodeTypeDefException,
             RepositoryException {
-        QName name = ntd.getQName();
+        Name name = ntd.getName();
         if (!registeredNTDefs.containsKey(name)) {
             throw new NoSuchNodeTypeException(name.toString());
         }
@@ -268,9 +271,9 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
     }
 
     /**
-     * @see NodeTypeRegistry#getNodeTypeDefinition(QName)
+     * @see NodeTypeRegistry#getNodeTypeDefinition(Name)
      */
-    public QNodeTypeDefinition getNodeTypeDefinition(QName nodeTypeName)
+    public QNodeTypeDefinition getNodeTypeDefinition(Name nodeTypeName)
         throws NoSuchNodeTypeException {
         QNodeTypeDefinition def = (QNodeTypeDefinition) registeredNTDefs.get(nodeTypeName);
         if (def == null) {
@@ -280,25 +283,25 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
     }
     //------------------------------------------< EffectiveNodeTypeProvider >---
     /**
-     * @see EffectiveNodeTypeProvider#getEffectiveNodeType(QName)
+     * @see EffectiveNodeTypeProvider#getEffectiveNodeType(Name)
      */
-    public synchronized EffectiveNodeType getEffectiveNodeType(QName ntName)
+    public synchronized EffectiveNodeType getEffectiveNodeType(Name ntName)
             throws NoSuchNodeTypeException {
         return getEffectiveNodeType(ntName, entCache, registeredNTDefs);
     }
 
     /**
-     * @see EffectiveNodeTypeProvider#getEffectiveNodeType(QName[])
+     * @see EffectiveNodeTypeProvider#getEffectiveNodeType(Name[])
      */
-    public synchronized EffectiveNodeType getEffectiveNodeType(QName[] ntNames)
+    public synchronized EffectiveNodeType getEffectiveNodeType(Name[] ntNames)
             throws NodeTypeConflictException, NoSuchNodeTypeException {
         return getEffectiveNodeType(ntNames, entCache, registeredNTDefs);
     }
 
     /**
-     * @see EffectiveNodeTypeProvider#getEffectiveNodeType(QName[], Map)
+     * @see EffectiveNodeTypeProvider#getEffectiveNodeType(Name[], Map)
      */
-    public EffectiveNodeType getEffectiveNodeType(QName[] ntNames, Map ntdMap)
+    public EffectiveNodeType getEffectiveNodeType(Name[] ntNames, Map ntdMap)
         throws NodeTypeConflictException, NoSuchNodeTypeException {
         return getEffectiveNodeType(ntNames, entCache, ntdMap);
     }
@@ -311,21 +314,21 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
      */
     public EffectiveNodeType getEffectiveNodeType(NodeState nodeState) throws ConstraintViolationException, NoSuchNodeTypeException {
         try {
-            QName[] allNtNames;
+            Name[] allNtNames;
             if (nodeState.getStatus() == Status.EXISTING) {
                 allNtNames = nodeState.getNodeTypeNames();
             } else {
                 // TODO: check if correct (and only used for creating new)
-                QName primaryType = nodeState.getNodeTypeName();
-                allNtNames = new QName[] { primaryType }; // default
+                Name primaryType = nodeState.getNodeTypeName();
+                allNtNames = new Name[] { primaryType }; // default
                 try {
-                    PropertyEntry pe = nodeState.getNodeEntry().getPropertyEntry(QName.JCR_MIXINTYPES, true);
+                    PropertyEntry pe = nodeState.getNodeEntry().getPropertyEntry(NameConstants.JCR_MIXINTYPES, true);
                     if (pe != null) {
                         PropertyState mixins = pe.getPropertyState();
                         QValue[] values = mixins.getValues();
-                        allNtNames = new QName[values.length + 1];
+                        allNtNames = new Name[values.length + 1];
                         for (int i = 0; i < values.length; i++) {
-                            allNtNames[i] = values[i].getQName();
+                            allNtNames[i] = values[i].getName();
                         }
                         allNtNames[values.length] = primaryType;
                     } // else: no jcr:mixinTypes property exists -> ignore
@@ -349,12 +352,12 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
      * @return
      * @throws NoSuchNodeTypeException
      */
-    private EffectiveNodeType getEffectiveNodeType(QName ntName,
+    private EffectiveNodeType getEffectiveNodeType(Name ntName,
                                                    EffectiveNodeTypeCache entCache,
                                                    Map ntdCache)
         throws NoSuchNodeTypeException {
         // 1. check if effective node type has already been built
-        EffectiveNodeTypeCache.Key key = entCache.getKey(new QName[]{ntName});
+        EffectiveNodeTypeCache.Key key = entCache.getKey(new Name[]{ntName});
         EffectiveNodeType ent = entCache.get(key);
         if (ent != null) {
             return ent;
@@ -390,7 +393,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
      * @throws NodeTypeConflictException
      * @throws NoSuchNodeTypeException
      */
-    private EffectiveNodeType getEffectiveNodeType(QName[] ntNames,
+    private EffectiveNodeType getEffectiveNodeType(Name[] ntNames,
                                                    EffectiveNodeTypeCache entCache,
                                                    Map ntdCache)
         throws NodeTypeConflictException, NoSuchNodeTypeException {
@@ -432,7 +435,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
                      * no matching sub-aggregates found:
                      * build aggregate of remaining node types through iteration
                      */
-                    QName[] remainder = key.getNames();
+                    Name[] remainder = key.getNames();
                     for (int i = 0; i < remainder.length; i++) {
                         QNodeTypeDefinition ntd = (QNodeTypeDefinition) ntdCache.get(remainder[i]);
                         EffectiveNodeTypeImpl ent = EffectiveNodeTypeImpl.create(this, ntd, ntdCache);
@@ -465,7 +468,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
     /**
      * Notify the listeners that a node type <code>ntName</code> has been registered.
      */
-    private void notifyRegistered(QName ntName) {
+    private void notifyRegistered(Name ntName) {
         // copy listeners to array to avoid ConcurrentModificationException
         NodeTypeRegistryListener[] la =
                 new NodeTypeRegistryListener[listeners.size()];
@@ -484,7 +487,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
     /**
      * Notify the listeners that a node type <code>ntName</code> has been re-registered.
      */
-    private void notifyReRegistered(QName ntName) {
+    private void notifyReRegistered(Name ntName) {
         // copy listeners to array to avoid ConcurrentModificationException
         NodeTypeRegistryListener[] la = new NodeTypeRegistryListener[listeners.size()];
         Iterator iter = listeners.values().iterator();
@@ -502,7 +505,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
     /**
      * Notify the listeners that a node type <code>ntName</code> has been unregistered.
      */
-    private void notifyUnregistered(QName ntName) {
+    private void notifyUnregistered(Name ntName) {
         // copy listeners to array to avoid ConcurrentModificationException
         NodeTypeRegistryListener[] la = new NodeTypeRegistryListener[listeners.size()];
         Iterator iter = listeners.values().iterator();
@@ -534,7 +537,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
             log.debug("Effective node type for " + ntd + " not yet built.");
         }
         // register nt-definition
-        registeredNTDefs.put(ntd.getQName(), ntd);
+        registeredNTDefs.put(ntd.getName(), ntd);
 
         // store property & child node definitions of new node type by id
         QPropertyDefinition[] pda = ntd.getPropertyDefs();
@@ -551,7 +554,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
         }
     }
 
-    private void internalUnregister(QName name) {
+    private void internalUnregister(Name name) {
         QNodeTypeDefinition ntd = (QNodeTypeDefinition) registeredNTDefs.remove(name);
         entCache.invalidate(name);
 
@@ -574,7 +577,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
 
     private void internalUnregister(Collection ntNames) {
         for (Iterator iter = ntNames.iterator(); iter.hasNext();) {
-            QName name = (QName) iter.next();
+            Name name = (Name) iter.next();
             internalUnregister(name);
         }
     }
@@ -612,10 +615,10 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
          * will only contain those node type definitions that are known so far.
          *
          * @param nodeTypeName node type name
-         * @return a set of node type <code>QName</code>s
+         * @return a set of node type <code>Name</code>s
          * @throws NoSuchNodeTypeException
          */
-        private Set getDependentNodeTypes(QName nodeTypeName) throws NoSuchNodeTypeException {
+        private Set getDependentNodeTypes(Name nodeTypeName) throws NoSuchNodeTypeException {
             if (!nodetypeDefinitions.containsKey(nodeTypeName)) {
                 throw new NoSuchNodeTypeException(nodeTypeName.toString());
             }
@@ -626,7 +629,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
             while (iter.hasNext()) {
                 QNodeTypeDefinition ntd = (QNodeTypeDefinition) iter.next();
                 if (ntd.getDependencies().contains(nodeTypeName)) {
-                    names.add(ntd.getQName());
+                    names.add(ntd.getName());
                 }
             }
             return names;
@@ -655,7 +658,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
         }
 
         public boolean containsKey(Object key) {
-            if (!(key instanceof QName)) {
+            if (!(key instanceof Name)) {
                 return false;
             }
             return get(key) != null;
@@ -665,7 +668,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
             if (!(value instanceof QNodeTypeDefinition)) {
                 return false;
             }
-            return get(((QNodeTypeDefinition)value).getQName()) != null;
+            return get(((QNodeTypeDefinition)value).getName()) != null;
         }
 
         public Set keySet() {
@@ -703,14 +706,14 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
         }
 
         public Object get(Object key) {
-            if (!(key instanceof QName)) {
+            if (!(key instanceof Name)) {
                 throw new IllegalArgumentException();
             }
             QNodeTypeDefinition def = (QNodeTypeDefinition) nodetypeDefinitions.get(key);
             if (def == null) {
                 try {
                     // node type does either not exist or hasn't been loaded yet
-                    Iterator it = storage.getDefinitions(new QName[] {(QName) key});
+                    Iterator it = storage.getDefinitions(new Name[] {(Name) key});
                     updateInternalMap(it);
                 } catch (RepositoryException e) {
                     log.debug(e.getMessage());
@@ -729,8 +732,8 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
             Iterator iter = nodetypeDefinitions.values().iterator();
             while (iter.hasNext()) {
                 QNodeTypeDefinition ntd = (QNodeTypeDefinition) iter.next();
-                ps.println(ntd.getQName());
-                QName[] supertypes = ntd.getSupertypes();
+                ps.println(ntd.getName());
+                Name[] supertypes = ntd.getSupertypes();
                 ps.println("\tSupertypes");
                 for (int i = 0; i < supertypes.length; i++) {
                     ps.println("\t\t" + supertypes[i]);
@@ -742,7 +745,7 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
                 for (int i = 0; i < pd.length; i++) {
                     ps.print("\tPropertyDefinition");
                     ps.println(" (declared in " + pd[i].getDeclaringNodeType() + ") ");
-                    ps.println("\t\tName\t\t" + (pd[i].definesResidual() ? "*" : pd[i].getQName().toString()));
+                    ps.println("\t\tName\t\t" + (pd[i].definesResidual() ? "*" : pd[i].getName().toString()));
                     String type = pd[i].getRequiredType() == 0 ? "null" : PropertyType.nameFromValue(pd[i].getRequiredType());
                     ps.println("\t\tRequiredType\t" + type);                  
                     String[] vca = pd[i].getValueConstraints();
@@ -785,14 +788,14 @@ public class NodeTypeRegistryImpl implements Dumpable, NodeTypeRegistry, Effecti
                 for (int i = 0; i < nd.length; i++) {
                     ps.print("\tNodeDefinition");
                     ps.println(" (declared in " + nd[i].getDeclaringNodeType() + ") ");
-                    ps.println("\t\tName\t\t" + (nd[i].definesResidual() ? "*" : nd[i].getQName().toString()));
-                    QName[] reqPrimaryTypes = nd[i].getRequiredPrimaryTypes();
+                    ps.println("\t\tName\t\t" + (nd[i].definesResidual() ? "*" : nd[i].getName().toString()));
+                    Name[] reqPrimaryTypes = nd[i].getRequiredPrimaryTypes();
                     if (reqPrimaryTypes != null && reqPrimaryTypes.length > 0) {
                         for (int n = 0; n < reqPrimaryTypes.length; n++) {
                             ps.print("\t\tRequiredPrimaryType\t" + reqPrimaryTypes[n]);
                         }
                     }
-                    QName defPrimaryType = nd[i].getDefaultPrimaryType();
+                    Name defPrimaryType = nd[i].getDefaultPrimaryType();
                     if (defPrimaryType != null) {
                         ps.print("\n\t\tDefaultPrimaryType\t" + defPrimaryType);
                     }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistryListener.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistryListener.java
index 775288d..aeb1585 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistryListener.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeRegistryListener.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 
 /**
  * The <code>NodeTypeRegistryListener</code> interface allows an implementing
@@ -32,19 +32,19 @@ public interface NodeTypeRegistryListener {
      *
      * @param ntName name of the node type that has been registered
      */
-    void nodeTypeRegistered(QName ntName);
+    void nodeTypeRegistered(Name ntName);
 
     /**
      * Called when a node type has been re-registered.
      *
      * @param ntName name of the node type that has been registered
      */
-    void nodeTypeReRegistered(QName ntName);
+    void nodeTypeReRegistered(Name ntName);
 
     /**
      * Called when a node type has been deregistered.
      *
      * @param ntName name of the node type that has been unregistered
      */
-    void nodeTypeUnregistered(QName ntName);
+    void nodeTypeUnregistered(Name ntName);
 }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeStorage.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeStorage.java
index 4d4e698..823b79d 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeStorage.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeStorage.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeTypeDefinition;
 
 import javax.jcr.nodetype.NoSuchNodeTypeException;
@@ -46,11 +46,11 @@ public interface NodeTypeStorage {
      * @throws NoSuchNodeTypeException
      * @throws RepositoryException
      */
-    public Iterator getDefinitions(QName[] nodeTypeNames) throws NoSuchNodeTypeException, RepositoryException;
+    public Iterator getDefinitions(Name[] nodeTypeNames) throws NoSuchNodeTypeException, RepositoryException;
 
     public void registerNodeTypes(QNodeTypeDefinition[] nodeTypeDefs) throws NoSuchNodeTypeException, RepositoryException;
 
     public void reregisterNodeTypes(QNodeTypeDefinition[] nodeTypeDefs) throws NoSuchNodeTypeException, RepositoryException;
 
-    public void unregisterNodeTypes(QName[] nodeTypeNames) throws NoSuchNodeTypeException, RepositoryException;
+    public void unregisterNodeTypes(Name[] nodeTypeNames) throws NoSuchNodeTypeException, RepositoryException;
 }
\ No newline at end of file
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/PropertyDefinitionImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/PropertyDefinitionImpl.java
index 64f6203..007ac72 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/PropertyDefinitionImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/PropertyDefinitionImpl.java
@@ -16,10 +16,11 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.value.ValueFormat;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.nodetype.InvalidConstraintException;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -31,7 +32,7 @@ import javax.jcr.nodetype.PropertyDefinition;
 /**
  * This class implements the <code>PropertyDefinition</code> interface.
  * All method calls are delegated to the wrapped {@link QPropertyDefinition},
- * performing the translation from <code>QName</code>s to JCR names
+ * performing the translation from <code>Name</code>s to JCR names
  * (and vice versa) where necessary.
  */
 public class PropertyDefinitionImpl extends ItemDefinitionImpl implements PropertyDefinition {
@@ -48,11 +49,11 @@ public class PropertyDefinitionImpl extends ItemDefinitionImpl implements Proper
      *
      * @param propDef    property definition
      * @param ntMgr      node type manager
-     * @param nsResolver namespace resolver
+     * @param resolver
      */
     PropertyDefinitionImpl(QPropertyDefinition propDef, NodeTypeManagerImpl ntMgr,
-                           NamespaceResolver nsResolver, ValueFactory valueFactory) {
-        super(propDef, ntMgr, nsResolver);
+                           NamePathResolver resolver, ValueFactory valueFactory) {
+        super(propDef, ntMgr, resolver);
         this.valueFactory = valueFactory;
     }
 
@@ -70,7 +71,7 @@ public class PropertyDefinitionImpl extends ItemDefinitionImpl implements Proper
         Value[] values = new Value[defVals.length];
         for (int i = 0; i < defVals.length; i++) {
             try {
-                values[i] = ValueFormat.getJCRValue(defVals[i], nsResolver, valueFactory);
+                values[i] = ValueFormat.getJCRValue(defVals[i], resolver, valueFactory);
             } catch (RepositoryException e) {
                 // should never get here
                 String propName = (getName() == null) ? "[null]" : getName();
@@ -101,7 +102,7 @@ public class PropertyDefinitionImpl extends ItemDefinitionImpl implements Proper
             String[] vca = new String[constraints.length];
             for (int i = 0; i < constraints.length; i++) {
                 ValueConstraint constr = ValueConstraint.create(pd.getRequiredType(), constraints[i]);
-                vca[i] = constr.getDefinition(nsResolver);
+                vca[i] = constr.getDefinition(resolver);
             }
             return vca;
         } catch (InvalidConstraintException e) {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ValueConstraint.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ValueConstraint.java
index 7dc9845..36ccd19 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ValueConstraint.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/ValueConstraint.java
@@ -16,25 +16,25 @@
  */
 package org.apache.jackrabbit.jcr2spi.nodetype;
 
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.PathResolver;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.value.DateValue;
+import org.apache.jackrabbit.nodetype.InvalidConstraintException;
+import org.apache.jackrabbit.name.PathFactoryImpl;
+import org.apache.jackrabbit.name.NameFactoryImpl;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 import javax.jcr.PropertyType;
 import javax.jcr.RepositoryException;
 import javax.jcr.ValueFormatException;
+import javax.jcr.NamespaceException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import java.util.Calendar;
 import java.util.regex.Matcher;
@@ -72,8 +72,9 @@ public abstract class ValueConstraint {
      *
      * @return the definition of this constraint.
      * @see #getQualifiedDefinition()
+     * @param resolver
      */
-    public String getDefinition(NamespaceResolver nsResolver) {
+    public String getDefinition(NamePathResolver resolver) {
         return qualifiedDefinition;
     }
 
@@ -81,7 +82,7 @@ public abstract class ValueConstraint {
      * By default the qualified definition is the same as the JCR definition.
      *
      * @return the qualified definition String
-     * @see #getDefinition(NamespaceResolver)
+     * @see #getDefinition(NamePathResolver)
      */
     public String getQualifiedDefinition() {
         return qualifiedDefinition;
@@ -171,12 +172,12 @@ public abstract class ValueConstraint {
      *
      * @param type
      * @param definition
-     * @param nsResolver
+     * @param resolver
      * @return
      * @throws InvalidConstraintException
      */
     public static ValueConstraint create(int type, String definition,
-                                         NamespaceResolver nsResolver)
+                                         NamePathResolver resolver)
             throws InvalidConstraintException {
         if (definition == null) {
             throw new IllegalArgumentException("Illegal definition (null) for ValueConstraint.");
@@ -199,13 +200,13 @@ public abstract class ValueConstraint {
                 return new NumericConstraint(definition);
 
             case PropertyType.NAME:
-                return new NameConstraint(definition, nsResolver);
+                return new NameConstraint(definition, resolver);
 
             case PropertyType.PATH:
-                return new PathConstraint(definition, nsResolver);
+                return new PathConstraint(definition, resolver);
 
             case PropertyType.REFERENCE:
-                return new ReferenceConstraint(definition, nsResolver);
+                return new ReferenceConstraint(definition, resolver);
 
             default:
                 throw new IllegalArgumentException("Unknown/unsupported target type for constraint: " + PropertyType.nameFromValue(type));
@@ -621,6 +622,7 @@ class DateConstraint extends ValueConstraint {
  * <code>PathConstraint</code> ...
  */
 class PathConstraint extends ValueConstraint {
+
     final Path path;
     final boolean deep;
 
@@ -628,10 +630,11 @@ class PathConstraint extends ValueConstraint {
         super(qualifiedDefinition);
         // constraint format: qualified absolute or relative path with optional trailing wildcard
         deep = qualifiedDefinition.endsWith("*");
-        path = Path.valueOf(qualifiedDefinition);
+        // TODO improve. don't rely on a specific factory impl
+        path = PathFactoryImpl.getInstance().create(qualifiedDefinition);
     }
 
-    PathConstraint(String definition, NamespaceResolver nsResolver)
+    PathConstraint(String definition, PathResolver resolver)
             throws InvalidConstraintException {
         super(definition);
 
@@ -642,23 +645,28 @@ class PathConstraint extends ValueConstraint {
             definition = definition.substring(0, definition.length() - 1);
         }
         try {
-            path = PathFormat.parse(definition, nsResolver);
-        } catch (MalformedPathException mpe) {
+            path = resolver.getQPath(definition);
+        } catch (NameException e) {
+            String msg = "Invalid path expression specified as value constraint: " + definition;
+            log.debug(msg);
+            throw new InvalidConstraintException(msg, e);
+        } catch (NamespaceException e) {
             String msg = "Invalid path expression specified as value constraint: " + definition;
             log.debug(msg);
-            throw new InvalidConstraintException(msg, mpe);
+            throw new InvalidConstraintException(msg, e);
         }
     }
 
     /**
-     * Uses {@link PathFormat#format(Path, NamespaceResolver)} to convert the
+     * Uses {@link NamePathResolver#getJCRPath(Path)} to convert the
      * qualified <code>Path</code> into a JCR path.
      *
-     * @see ValueConstraint#getDefinition(NamespaceResolver)
+     * @see ValueConstraint#getDefinition(NamePathResolver)
+     * @param resolver
      */
-    public String getDefinition(NamespaceResolver nsResolver) {
+    public String getDefinition(NamePathResolver resolver) {
         try {
-            String p = PathFormat.format(path, nsResolver);
+            String p = resolver.getJCRPath(path);
             if (!deep) {
                 return p;
             } else if (path.denotesRoot()) {
@@ -666,7 +674,7 @@ class PathConstraint extends ValueConstraint {
             } else {
                 return p + "/*";
             }
-        } catch (NoPrefixDeclaredException npde) {
+        } catch (NamespaceException e) {
             // should never get here, return raw definition as fallback
             return getQualifiedDefinition();
         }
@@ -697,7 +705,7 @@ class PathConstraint extends ValueConstraint {
                 try {
                     p0 = path.getNormalizedPath();
                     p1 = p.getNormalizedPath();
-                } catch (MalformedPathException e) {
+                } catch (RepositoryException e) {
                     throw new ConstraintViolationException("path not valid: " + e);
                 }
                 if (deep) {
@@ -707,7 +715,7 @@ class PathConstraint extends ValueConstraint {
                                 + " does not satisfy the constraint '"
                                 + getQualifiedDefinition() + "'");
                         }
-                    } catch (MalformedPathException e) {
+                    } catch (RepositoryException e) {
                         // can't compare relative with absolute path
                         throw new ConstraintViolationException(p
                             + " does not satisfy the constraint '"
@@ -738,44 +746,45 @@ class PathConstraint extends ValueConstraint {
  */
 class NameConstraint extends ValueConstraint {
 
-    private final QName name;
+    private final Name name;
 
     NameConstraint(String qualifiedDefinition) {
         super(qualifiedDefinition);
         // constraint format: String representation of qualified name
-        name = QName.valueOf(qualifiedDefinition);
+        // TODO improve. don't rely on a specific factory impl
+        name = NameFactoryImpl.getInstance().create(qualifiedDefinition);
     }
 
-    NameConstraint(String definition, NamespaceResolver nsResolver)
+    NameConstraint(String definition, NameResolver resolver)
             throws InvalidConstraintException {
         super(definition);
         // constraint format: JCR name in prefix form
         try {
-            NameFormat.checkFormat(definition);
-            name = NameFormat.parse(definition, nsResolver);
-        } catch (IllegalNameException ine) {
+            name = resolver.getQName(definition);
+        } catch (NameException e) {
             String msg = "invalid name specified as value constraint: "
                     + definition;
             log.debug(msg);
-            throw new InvalidConstraintException(msg, ine);
-        } catch (NameException upe) {
+            throw new InvalidConstraintException(msg, e);
+        } catch (NamespaceException e) {
             String msg = "invalid name specified as value constraint: "
                     + definition;
             log.debug(msg);
-            throw new InvalidConstraintException(msg, upe);
+            throw new InvalidConstraintException(msg, e);
         }
     }
 
     /**
-     * Uses {@link NameFormat#format(QName, NamespaceResolver)} to convert the
-     * qualified <code>QName</code> into a JCR name.
+     * Uses {@link NamePathResolver#getJCRName(Name)} to convert the
+     * qualified <code>Name</code> into a JCR name.
      *
-     * @see ValueConstraint#getDefinition(NamespaceResolver)
+     * @see ValueConstraint#getDefinition(NamePathResolver)
+     * @param resolver
      */
-    public String getDefinition(NamespaceResolver nsResolver) {
+    public String getDefinition(NamePathResolver resolver) {
         try {
-            return NameFormat.format(name, nsResolver);
-        } catch (NoPrefixDeclaredException npde) {
+            return resolver.getJCRName(name);
+        } catch (NamespaceException e) {
             // should never get here, return raw definition as fallback
             return getQualifiedDefinition();
         }
@@ -800,7 +809,7 @@ class NameConstraint extends ValueConstraint {
         }
         switch (value.getType()) {
             case PropertyType.NAME:
-                QName n = value.getQName();
+                Name n = value.getName();
                 if (!name.equals(n)) {
                     throw new ConstraintViolationException(n
                             + " does not satisfy the constraint '"
@@ -823,43 +832,50 @@ class NameConstraint extends ValueConstraint {
  */
 class ReferenceConstraint extends ValueConstraint {
 
-    private final QName ntName;
+    private final Name ntName;
 
     ReferenceConstraint(String qualifiedDefinition) {
         super(qualifiedDefinition);
         // format: qualified node type name
-        ntName = QName.valueOf(qualifiedDefinition);
+        // TODO improve. don't rely on a specific factory impl
+        ntName = NameFactoryImpl.getInstance().create(qualifiedDefinition);
     }
 
-    ReferenceConstraint(String definition, NamespaceResolver nsResolver) throws InvalidConstraintException {
+    ReferenceConstraint(String definition, NamePathResolver resolver) throws InvalidConstraintException {
         super(definition);
 
         // format: node type name
         try {
-            ntName = NameFormat.parse(definition, nsResolver);
-        } catch (IllegalNameException ine) {
+            ntName = resolver.getQName(definition);
+        } catch (org.apache.jackrabbit.conversion.IllegalNameException ine) {
             String msg = "invalid node type name specified as value constraint: "
                     + definition;
             log.debug(msg);
             throw new InvalidConstraintException(msg, ine);
-        } catch (UnknownPrefixException upe) {
+        } catch (NameException e) {
+            String msg = "invalid node type name specified as value constraint: "
+                    + definition;
+            log.debug(msg);
+            throw new InvalidConstraintException(msg, e);
+        } catch (NamespaceException e) {
             String msg = "invalid node type name specified as value constraint: "
                     + definition;
             log.debug(msg);
-            throw new InvalidConstraintException(msg, upe);
+            throw new InvalidConstraintException(msg, e);
         }
     }
 
     /**
-     * Uses {@link NameFormat#format(QName, NamespaceResolver)} to convert the
+     * Uses {@link NamePathResolver#getJCRName(Name)} to convert the
      * qualified nodetype name into a JCR name.
      *
-     * @see ValueConstraint#getDefinition(NamespaceResolver)
+     * @see ValueConstraint#getDefinition(NamePathResolver)
+     * @param resolver
      */
-    public String getDefinition(NamespaceResolver nsResolver) {
+    public String getDefinition(NamePathResolver resolver) {
         try {
-            return NameFormat.format(ntName, nsResolver);
-        } catch (NoPrefixDeclaredException npde) {
+            return resolver.getJCRName(ntName);
+        } catch (NamespaceException npde) {
             // should never get here, return raw definition as fallback
             return getQualifiedDefinition();
         }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/EventImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/EventImpl.java
index 5c38e02..ecf1f74 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/EventImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/EventImpl.java
@@ -16,9 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.observation;
 
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -39,7 +37,7 @@ final class EventImpl implements Event {
      * The session of the {@link javax.jcr.observation.EventListener} this
      * event will be delivered to.
      */
-    private final NamespaceResolver nsResolver;
+    private final NamePathResolver resolver;
 
     /**
      * The underlying SPI event.
@@ -55,13 +53,11 @@ final class EventImpl implements Event {
      * Creates a new {@link javax.jcr.observation.Event} instance based on an
      * {@link org.apache.jackrabbit.spi.Event SPI Event}.
      *
-     * @param nsResolver <code>NamespaceResolver</code> attached to the session
-     * of the registerd <code>EventListener</code>, where this <code>Event</code>
-     * will be delivered to.
+     * @param resolver
      * @param event   the underlying SPI <code>Event</code>.
      */
-    EventImpl(NamespaceResolver nsResolver, org.apache.jackrabbit.spi.Event event) {
-        this.nsResolver = nsResolver;
+    EventImpl(NamePathResolver resolver, org.apache.jackrabbit.spi.Event event) {
+        this.resolver = resolver;
         this.event = event;
     }
 
@@ -76,13 +72,7 @@ final class EventImpl implements Event {
      * {@inheritDoc}
      */
     public String getPath() throws RepositoryException {
-        try {
-            return PathFormat.format(event.getQPath(), nsResolver);
-        } catch (NoPrefixDeclaredException e) {
-            String msg = "internal error: encountered unregistered namespace in path";
-            log.debug(msg);
-            throw new RepositoryException(msg, e);
-        }
+        return resolver.getJCRPath(event.getPath());
     }
 
     /**
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/FilteredEventIterator.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/FilteredEventIterator.java
index 91983ac..51277fd 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/FilteredEventIterator.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/FilteredEventIterator.java
@@ -20,7 +20,7 @@ import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 import org.apache.jackrabbit.spi.EventBundle;
 import org.apache.jackrabbit.spi.EventFilter;
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 
 import javax.jcr.observation.Event;
 import javax.jcr.observation.EventIterator;
@@ -56,7 +56,7 @@ class FilteredEventIterator implements EventIterator {
     /**
      * The namespace resolver of the session that created this event iterator.
      */
-    private final NamespaceResolver nsResolver;
+    private final NamePathResolver resolver;
 
     /**
      * The next {@link javax.jcr.observation.Event} in this iterator
@@ -75,16 +75,15 @@ class FilteredEventIterator implements EventIterator {
      *                   bundle.
      * @param filter     only event that pass the filter will be dispatched to
      *                   the event listener.
-     * @param nsResolver the namespace resolver of the session that created this
-     *                   <code>FilteredEventIterator</code>.
+     * @param resolver
      */
     public FilteredEventIterator(EventBundle events,
                                  EventFilter filter,
-                                 NamespaceResolver nsResolver) {
+                                 NamePathResolver resolver) {
         this.actualEvents = events.getEvents();
         this.filter = filter;
         this.isLocal = events.isLocal();
-        this.nsResolver = nsResolver;
+        this.resolver = resolver;
         fetchNext();
     }
 
@@ -162,7 +161,7 @@ class FilteredEventIterator implements EventIterator {
         next = null;
         while (next == null && actualEvents.hasNext()) {
             event = (org.apache.jackrabbit.spi.Event) actualEvents.next();
-            next = filter.accept(event, isLocal) ? new EventImpl(nsResolver, event) : null;
+            next = filter.accept(event, isLocal) ? new EventImpl(resolver, event) : null;
         }
     }
 }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/ObservationManagerImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/ObservationManagerImpl.java
index 736edcf..a02d6cf 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/ObservationManagerImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/observation/ObservationManagerImpl.java
@@ -18,16 +18,13 @@ package org.apache.jackrabbit.jcr2spi.observation;
 
 import org.apache.jackrabbit.jcr2spi.nodetype.NodeTypeRegistry;
 import org.apache.jackrabbit.jcr2spi.WorkspaceManager;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.EventBundle;
 import org.apache.jackrabbit.spi.EventFilter;
 import org.apache.jackrabbit.util.IteratorHelper;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
@@ -60,7 +57,7 @@ public class ObservationManagerImpl implements ObservationManager, InternalEvent
     /**
      * The session this observation manager belongs to.
      */
-    private final NamespaceResolver nsResolver;
+    private final NamePathResolver resolver;
 
     /**
      * The <code>NodeTypeRegistry</code> of the session.
@@ -86,13 +83,13 @@ public class ObservationManagerImpl implements ObservationManager, InternalEvent
     /**
      * Creates a new observation manager for <code>session</code>.
      * @param wspManager the WorkspaceManager.
-     * @param nsResolver NamespaceResolver to be used by this observation manager
-     * is based on.
+     * @param resolver
      * @param ntRegistry The <code>NodeTypeRegistry</code> of the session.
      */
-    public ObservationManagerImpl(WorkspaceManager wspManager, NamespaceResolver nsResolver, NodeTypeRegistry ntRegistry) {
+    public ObservationManagerImpl(WorkspaceManager wspManager, NamePathResolver resolver,
+                                  NodeTypeRegistry ntRegistry) {
         this.wspManager = wspManager;
-        this.nsResolver = nsResolver;
+        this.resolver = resolver;
         this.ntRegistry = ntRegistry;
     }
 
@@ -112,31 +109,31 @@ public class ObservationManagerImpl implements ObservationManager, InternalEvent
         }
         Path path;
         try {
-            path = PathFormat.parse(absPath, nsResolver).getCanonicalPath();
-        } catch (MalformedPathException e) {
+            path = resolver.getQPath(absPath).getCanonicalPath();
+        } catch (NameException e) {
             throw new RepositoryException("Malformed path: " + absPath);
         }
 
         // create NodeType instances from names
-        QName[] nodeTypeQNames;
+        Name[] qNodeTypeNames;
         if (nodeTypeNames == null) {
-            nodeTypeQNames = null;
+            qNodeTypeNames = null;
         } else {
             try {
-                nodeTypeQNames = new QName[nodeTypeNames.length];
+                qNodeTypeNames = new Name[nodeTypeNames.length];
                 for (int i = 0; i < nodeTypeNames.length; i++) {
-                    QName ntName = NameFormat.parse(nodeTypeNames[i], nsResolver);
+                    Name ntName = resolver.getQName(nodeTypeNames[i]);
                     if (!ntRegistry.isRegistered(ntName)) {
                         throw new RepositoryException("unknown node type: " + nodeTypeNames[i]);
                     }
-                    nodeTypeQNames[i] = ntName;
+                    qNodeTypeNames[i] = ntName;
                 }
             } catch (NameException e) {
                 throw new RepositoryException(e.getMessage());
             }
         }
 
-        EventFilter filter = wspManager.createEventFilter(eventTypes, path, isDeep, uuids, nodeTypeQNames, noLocal);
+        EventFilter filter = wspManager.createEventFilter(eventTypes, path, isDeep, uuids, qNodeTypeNames, noLocal);
         synchronized (subscriptions) {
             subscriptions.put(listener, filter);
             readOnlySubscriptions = null;
@@ -188,7 +185,7 @@ public class ObservationManagerImpl implements ObservationManager, InternalEvent
             Map.Entry entry = (Map.Entry) it.next();
             EventListener listener = (EventListener) entry.getKey();
             EventFilter filter = (EventFilter) entry.getValue();
-            FilteredEventIterator eventIter = new FilteredEventIterator(eventBundle, filter, nsResolver);
+            FilteredEventIterator eventIter = new FilteredEventIterator(eventBundle, filter, resolver);
             if (eventIter.hasNext()) {
                 try {
                     listener.onEvent(eventIter);
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AbstractCopy.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AbstractCopy.java
index 27880ba..516acf7 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AbstractCopy.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AbstractCopy.java
@@ -20,8 +20,8 @@ import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.ManagerProvider;
 import org.apache.jackrabbit.jcr2spi.util.LogUtil;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.NodeId;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
@@ -38,7 +38,7 @@ public abstract class AbstractCopy extends AbstractOperation {
 
     final NodeState destParentState;
     private final NodeState srcState;
-    private final QName destName;
+    private final Name destName;
     private final String srcWorkspaceName;
 
     /**
@@ -53,13 +53,13 @@ public abstract class AbstractCopy extends AbstractOperation {
 
         ItemState srcItemState = srcMgrProvider.getHierarchyManager().getItemState(srcPath);
         if (!srcItemState.isNode()) {
-            throw new PathNotFoundException("Source path " + LogUtil.safeGetJCRPath(srcPath, srcMgrProvider.getNamespaceResolver()) + " is not a valid path.");
+            throw new PathNotFoundException("Source path " + LogUtil.safeGetJCRPath(srcPath, srcMgrProvider.getPathResolver()) + " is not a valid path.");
         }
         this.srcState = (NodeState)srcItemState;
-        this.destParentState = getNodeState(destPath.getAncestor(1), destMgrProvider.getHierarchyManager(), destMgrProvider.getNamespaceResolver());
+        this.destParentState = getNodeState(destPath.getAncestor(1), destMgrProvider.getHierarchyManager(), destMgrProvider.getNamePathResolver());
 
         // check for illegal index present in destination path
-        Path.PathElement destElement = destPath.getNameElement();
+        Path.Element destElement = destPath.getNameElement();
         int index = destElement.getIndex();
         if (index > Path.INDEX_UNDEFINED) {
             // subscript in name element
@@ -96,7 +96,7 @@ public abstract class AbstractCopy extends AbstractOperation {
         return destParentState.getNodeId();
     }
 
-    public QName getDestinationName() {
+    public Name getDestinationName() {
         return destName;
     }
 }
\ No newline at end of file
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AbstractOperation.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AbstractOperation.java
index a594358..46f8522 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AbstractOperation.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AbstractOperation.java
@@ -16,12 +16,12 @@
  */
 package org.apache.jackrabbit.jcr2spi.operation;
 
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManager;
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.util.LogUtil;
+import org.apache.jackrabbit.conversion.PathResolver;
 
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
@@ -69,15 +69,15 @@ public abstract class AbstractOperation implements Operation {
      * 
      * @param nodePath
      * @param hierMgr
-     * @param nsResolver
+     * @param resolver
      * @return
      * @throws PathNotFoundException
      * @throws RepositoryException
      */
-    protected static NodeState getNodeState(Path nodePath, HierarchyManager hierMgr, NamespaceResolver nsResolver) throws PathNotFoundException, RepositoryException {
+    protected static NodeState getNodeState(Path nodePath, HierarchyManager hierMgr, PathResolver resolver) throws PathNotFoundException, RepositoryException {
         ItemState itemState = hierMgr.getItemState(nodePath);
         if (!itemState.isNode()) {
-            throw new PathNotFoundException(LogUtil.safeGetJCRPath(nodePath, nsResolver));
+            throw new PathNotFoundException(LogUtil.safeGetJCRPath(nodePath, resolver));
         }
         return (NodeState) itemState;
     }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddLabel.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddLabel.java
index c6407eb..4181cc5 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddLabel.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddLabel.java
@@ -18,11 +18,12 @@ package org.apache.jackrabbit.jcr2spi.operation;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 import org.apache.jackrabbit.spi.NodeId;
+import org.apache.jackrabbit.name.NameConstants;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.AccessDeniedException;
@@ -41,10 +42,10 @@ public class AddLabel extends AbstractOperation {
 
     private final NodeState versionHistoryState;
     private final NodeState versionState;
-    private final QName label;
+    private final Name label;
     private final boolean moveLabel;
 
-    private AddLabel(NodeState versionHistoryState, NodeState versionState, QName label, boolean moveLabel) {
+    private AddLabel(NodeState versionHistoryState, NodeState versionState, Name label, boolean moveLabel) {
         this.versionHistoryState = versionHistoryState;
         this.versionState = versionState;
         this.label = label;
@@ -78,7 +79,7 @@ public class AddLabel extends AbstractOperation {
     public void persisted() {
         try {
             NodeEntry vhEntry = (NodeEntry) versionHistoryState.getHierarchyEntry();
-            NodeEntry lnEntry = vhEntry.getNodeEntry(QName.JCR_VERSIONLABELS, Path.INDEX_DEFAULT);
+            NodeEntry lnEntry = vhEntry.getNodeEntry(NameConstants.JCR_VERSIONLABELS, Path.INDEX_DEFAULT);
             if (lnEntry != null) {
                 lnEntry.invalidate(moveLabel);
             }
@@ -95,7 +96,7 @@ public class AddLabel extends AbstractOperation {
         return versionState.getNodeEntry().getWorkspaceId();
     }
 
-    public QName getLabel() {
+    public Name getLabel() {
         return label;
     }
 
@@ -112,7 +113,7 @@ public class AddLabel extends AbstractOperation {
      * @param moveLabel
      * @return
      */
-    public static Operation create(NodeState versionHistoryState, NodeState versionState, QName label, boolean moveLabel) {
+    public static Operation create(NodeState versionHistoryState, NodeState versionState, Name label, boolean moveLabel) {
         return new AddLabel(versionHistoryState, versionState, label, moveLabel);
     }
 }
\ No newline at end of file
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddNode.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddNode.java
index 70e1cb9..8f4e30f 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddNode.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddNode.java
@@ -17,7 +17,7 @@
 package org.apache.jackrabbit.jcr2spi.operation;
 
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.NodeId;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
@@ -40,11 +40,11 @@ public class AddNode extends AbstractOperation {
 
     private final NodeId parentId;
     private final NodeState parentState;
-    private final QName nodeName;
-    private final QName nodeTypeName;
+    private final Name nodeName;
+    private final Name nodeTypeName;
     private final String uuid;
 
-    private AddNode(NodeState parentState, QName nodeName, QName nodeTypeName, String uuid) {
+    private AddNode(NodeState parentState, Name nodeName, Name nodeTypeName, String uuid) {
         this.parentId = parentState.getNodeId();
         this.parentState = parentState;
         this.nodeName = nodeName;
@@ -80,11 +80,11 @@ public class AddNode extends AbstractOperation {
         return parentState;
     }
 
-    public QName getNodeName() {
+    public Name getNodeName() {
         return nodeName;
     }
 
-    public QName getNodeTypeName() {
+    public Name getNodeTypeName() {
         return nodeTypeName;
     }
 
@@ -94,8 +94,8 @@ public class AddNode extends AbstractOperation {
 
     //------------------------------------------------------------< Factory >---
 
-    public static Operation create(NodeState parentState, QName nodeName,
-                                   QName nodeTypeName, String uuid) {
+    public static Operation create(NodeState parentState, Name nodeName,
+                                   Name nodeTypeName, String uuid) {
         AddNode an = new AddNode(parentState, nodeName, nodeTypeName, uuid);
         return an;
     }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddProperty.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddProperty.java
index 0b6acb7..b08f5ad 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddProperty.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/AddProperty.java
@@ -19,7 +19,7 @@ package org.apache.jackrabbit.jcr2spi.operation;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.NodeId;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 
 import javax.jcr.RepositoryException;
@@ -38,13 +38,13 @@ public class AddProperty extends AbstractOperation {
 
     private final NodeId parentId;
     private final NodeState parentState;
-    private final QName propertyName;
+    private final Name propertyName;
     private final int propertyType;
     private final QValue[] values;
 
     private final QPropertyDefinition definition;
 
-    private AddProperty(NodeState parentState, QName propName, int propertyType, QValue[] values, QPropertyDefinition definition) {
+    private AddProperty(NodeState parentState, Name propName, int propertyType, QValue[] values, QPropertyDefinition definition) {
         this.parentId = parentState.getNodeId();
         this.parentState = parentState;
         this.propertyName = propName;
@@ -81,7 +81,7 @@ public class AddProperty extends AbstractOperation {
         return parentState;
     }
 
-    public QName getPropertyName() {
+    public Name getPropertyName() {
         return propertyName;
     }
 
@@ -111,7 +111,7 @@ public class AddProperty extends AbstractOperation {
      * @param values
      * @return
      */
-    public static Operation create(NodeState parentState, QName propName, int propertyType,
+    public static Operation create(NodeState parentState, Name propName, int propertyType,
                                    QPropertyDefinition def, QValue[] values) {
         AddProperty ap = new AddProperty(parentState, propName, propertyType, values, def);
         return ap;
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Clone.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Clone.java
index 45d7788..5e91195 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Clone.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Clone.java
@@ -18,7 +18,7 @@ package org.apache.jackrabbit.jcr2spi.operation;
 
 import org.apache.jackrabbit.jcr2spi.ManagerProvider;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyEntry;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Copy.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Copy.java
index 20e1d16..7c58d4d 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Copy.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Copy.java
@@ -17,7 +17,7 @@
 package org.apache.jackrabbit.jcr2spi.operation;
 
 import org.apache.jackrabbit.jcr2spi.ManagerProvider;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.AccessDeniedException;
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java
index 44e00b8..26f9d07 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java
@@ -20,11 +20,10 @@ import org.apache.jackrabbit.jcr2spi.util.LogUtil;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManager;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.NodeId;
+import org.apache.jackrabbit.conversion.PathResolver;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -47,7 +46,7 @@ public class Move extends AbstractOperation {
 
     private final NodeId srcId;
     private final NodeId destParentId;
-    private final QName destName;
+    private final Name destName;
 
     private final NodeState srcState;
     private final NodeState srcParentState;
@@ -55,7 +54,8 @@ public class Move extends AbstractOperation {
 
     private final boolean sessionMove;
 
-    private Move(NodeState srcNodeState, NodeState srcParentState, NodeState destParentState, QName destName, boolean sessionMove) {
+    private Move(NodeState srcNodeState, NodeState srcParentState, NodeState destParentState, Name destName, boolean sessionMove) {
+
         this.srcId = (NodeId) srcNodeState.getId();
         this.destParentId = destParentState.getNodeId();
         this.destName = destName;
@@ -124,48 +124,42 @@ public class Move extends AbstractOperation {
         return destParentState;
     }
 
-    public QName getDestinationName() {
+    public Name getDestinationName() {
         return destName;
     }
 
     //------------------------------------------------------------< Factory >---
     public static Operation create(Path srcPath, Path destPath,
                                    HierarchyManager hierMgr,
-                                   NamespaceResolver nsResolver,
-                                   boolean sessionMove)
+                                                    PathResolver resolver,
+                                                    boolean sessionMove)
         throws ItemExistsException, NoSuchNodeTypeException, RepositoryException {
         // src must not be ancestor of destination
-        try {
-            if (srcPath.isAncestorOf(destPath)) {
-                String msg = "Invalid destination path: cannot be descendant of source path (" + LogUtil.safeGetJCRPath(destPath, nsResolver) + "," + LogUtil.safeGetJCRPath(srcPath, nsResolver) + ")";
-                log.debug(msg);
-                throw new RepositoryException(msg);
-            }
-        } catch (MalformedPathException e) {
-            String msg = "Invalid destination path: cannot be descendant of source path (" +LogUtil.safeGetJCRPath(destPath, nsResolver) + "," + LogUtil.safeGetJCRPath(srcPath, nsResolver) + ")";
+        if (srcPath.isAncestorOf(destPath)) {
+            String msg = "Invalid destination path: cannot be descendant of source path (" + LogUtil.safeGetJCRPath(destPath, resolver) + "," + LogUtil.safeGetJCRPath(srcPath, resolver) + ")";
             log.debug(msg);
-            throw new RepositoryException(msg, e);
+            throw new RepositoryException(msg);
         }
-        Path.PathElement destElement = destPath.getNameElement();
+        Path.Element destElement = destPath.getNameElement();
         // destination must not contain an index
         int index = destElement.getIndex();
         if (index > Path.INDEX_UNDEFINED) {
             // subscript in name element
-            String msg = "Invalid destination path: subscript in name element is not allowed (" + LogUtil.safeGetJCRPath(destPath, nsResolver) + ")";
+            String msg = "Invalid destination path: subscript in name element is not allowed (" + LogUtil.safeGetJCRPath(destPath, resolver) + ")";
             log.debug(msg);
             throw new RepositoryException(msg);
         }
         // root node cannot be moved:
-        if (Path.ROOT.equals(srcPath) || Path.ROOT.equals(destPath)) {
+        if (srcPath.denotesRoot() || destPath.denotesRoot()) {
             String msg = "Cannot move the root node.";
             log.debug(msg);
             throw new RepositoryException(msg);
         }
 
-        NodeState srcState = getNodeState(srcPath, hierMgr, nsResolver);
-        NodeState srcParentState = getNodeState(srcPath.getAncestor(1), hierMgr, nsResolver);
-        NodeState destParentState = getNodeState(destPath.getAncestor(1), hierMgr, nsResolver);
-        QName destName = destElement.getName();
+        NodeState srcState = getNodeState(srcPath, hierMgr, resolver);
+        NodeState srcParentState = getNodeState(srcPath.getAncestor(1), hierMgr, resolver);
+        NodeState destParentState = getNodeState(destPath.getAncestor(1), hierMgr, resolver);
+        Name destName = destElement.getName();
 
         // for session-move perform a lazy check for existing items at destination.
         // since the hierarchy may not be complete it is possible that an conflict
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/RemoveLabel.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/RemoveLabel.java
index 6e74c0d..cd58356 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/RemoveLabel.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/RemoveLabel.java
@@ -18,11 +18,12 @@ package org.apache.jackrabbit.jcr2spi.operation;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 import org.apache.jackrabbit.spi.NodeId;
+import org.apache.jackrabbit.name.NameConstants;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.AccessDeniedException;
@@ -41,9 +42,9 @@ public class RemoveLabel extends AbstractOperation {
 
     private final NodeState versionHistoryState;
     private final NodeState versionState;
-    private final QName label;
+    private final Name label;
 
-    private RemoveLabel(NodeState versionHistoryState, NodeState versionState, QName label) {
+    private RemoveLabel(NodeState versionHistoryState, NodeState versionState, Name label) {
         this.versionHistoryState = versionHistoryState;
         this.versionState = versionState;
         this.label = label;
@@ -75,7 +76,7 @@ public class RemoveLabel extends AbstractOperation {
     public void persisted() {
         try {
             NodeEntry vhEntry = (NodeEntry) versionHistoryState.getHierarchyEntry();
-            NodeEntry lnEntry = vhEntry.getNodeEntry(QName.JCR_VERSIONLABELS, Path.INDEX_DEFAULT);
+            NodeEntry lnEntry = vhEntry.getNodeEntry(NameConstants.JCR_VERSIONLABELS, Path.INDEX_DEFAULT);
             if (lnEntry != null) {
                 lnEntry.invalidate(true);
             }
@@ -93,7 +94,7 @@ public class RemoveLabel extends AbstractOperation {
         return versionState.getNodeEntry().getWorkspaceId();
     }
 
-    public QName getLabel() {
+    public Name getLabel() {
         return label;
     }
 
@@ -105,7 +106,7 @@ public class RemoveLabel extends AbstractOperation {
      * @param label
      * @return
      */
-    public static Operation create(NodeState versionHistoryState, NodeState versionState, QName label) {
+    public static Operation create(NodeState versionHistoryState, NodeState versionState, Name label) {
         return new RemoveLabel(versionHistoryState, versionState, label);
     }
 }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/ReorderNodes.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/ReorderNodes.java
index d5bb636..bea5d7b 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/ReorderNodes.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/ReorderNodes.java
@@ -17,7 +17,7 @@
 package org.apache.jackrabbit.jcr2spi.operation;
 
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.NodeId;
 
 import javax.jcr.nodetype.ConstraintViolationException;
@@ -97,8 +97,8 @@ public class ReorderNodes extends AbstractOperation {
 
     //------------------------------------------------------------< Factory >---
 
-    public static Operation create(NodeState parentState, Path.PathElement srcName,
-                                   Path.PathElement beforeName) throws ItemNotFoundException, RepositoryException {
+    public static Operation create(NodeState parentState, Path.Element srcName,
+                                   Path.Element beforeName) throws ItemNotFoundException, RepositoryException {
         NodeState insert = parentState.getChildNodeState(srcName.getName(), srcName.getNormalizedIndex());
         NodeState before = (beforeName == null) ? null : parentState.getChildNodeState(beforeName.getName(), beforeName.getNormalizedIndex());
         return new ReorderNodes(parentState, insert, before);
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Restore.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Restore.java
index 3e2ec33..8d4a89c 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Restore.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Restore.java
@@ -18,7 +18,7 @@ package org.apache.jackrabbit.jcr2spi.operation;
 
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.NodeId;
 
 import javax.jcr.RepositoryException;
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/SetMixin.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/SetMixin.java
index e544e25..25b6b47 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/SetMixin.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/SetMixin.java
@@ -16,9 +16,10 @@
  */
 package org.apache.jackrabbit.jcr2spi.operation;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.spi.NodeId;
+import org.apache.jackrabbit.name.NameConstants;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.AccessDeniedException;
@@ -33,9 +34,9 @@ public class SetMixin extends AbstractOperation {
 
     private final NodeId nodeId;
     private final NodeState nodeState;
-    private final QName[] mixinNames;
+    private final Name[] mixinNames;
 
-    private SetMixin(NodeState nodeState, QName[] mixinNames) {
+    private SetMixin(NodeState nodeState, Name[] mixinNames) {
         this.nodeState = nodeState;
         this.nodeId = nodeState.getNodeId();
         this.mixinNames = mixinNames;
@@ -45,7 +46,7 @@ public class SetMixin extends AbstractOperation {
         // add the jcr:mixinTypes property state as affected if it already exists
         // and therefore gets modified by this operation.
         try {
-            addAffectedItemState(nodeState.getPropertyState(QName.JCR_MIXINTYPES));
+            addAffectedItemState(nodeState.getPropertyState(NameConstants.JCR_MIXINTYPES));
         } catch (RepositoryException e) {
             // jcr:mixinTypes does not exist -> ignore
         }
@@ -79,13 +80,13 @@ public class SetMixin extends AbstractOperation {
         return nodeId;
     }
     
-    public QName[] getMixinNames() {
+    public Name[] getMixinNames() {
         return mixinNames;
     }
 
     //------------------------------------------------------------< Factory >---
 
-    public static Operation create(NodeState nodeState, QName[] mixinNames) {
+    public static Operation create(NodeState nodeState, Name[] mixinNames) {
         SetMixin sm = new SetMixin(nodeState, mixinNames);
         return sm;
     }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryImpl.java
index b24b804..4a9d73b 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryImpl.java
@@ -20,13 +20,10 @@ import org.apache.jackrabbit.jcr2spi.ItemManager;
 import org.apache.jackrabbit.jcr2spi.WorkspaceManager;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManager;
 import org.apache.jackrabbit.jcr2spi.name.LocalNamespaceMappings;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.jackrabbit.spi.QueryInfo;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 
 import javax.jcr.ItemExistsException;
 import javax.jcr.ItemNotFoundException;
@@ -58,6 +55,11 @@ public class QueryImpl implements Query {
     private final LocalNamespaceMappings nsResolver;
 
     /**
+     * Name and Path resolver
+     */
+    private final NamePathResolver resolver;
+
+    /**
      * The item manager of the session that executes this query.
      */
     private final ItemManager itemManager;
@@ -93,6 +95,7 @@ public class QueryImpl implements Query {
      *
      * @param session          the session that created this query.
      * @param nsResolver       the namespace resolver to be used.
+     * @param resolver
      * @param itemMgr          the item manager of that session.
      * @param hierarchyManager the HierarchyManager of that session.
      * @param wspManager       the workspace manager that belongs to the
@@ -101,12 +104,13 @@ public class QueryImpl implements Query {
      * @param language         the language of the query statement.
      * @throws InvalidQueryException if the query is invalid.
      */
-    public QueryImpl(Session session, LocalNamespaceMappings nsResolver,
+    public QueryImpl(Session session, LocalNamespaceMappings nsResolver, NamePathResolver resolver,
                      ItemManager itemMgr, HierarchyManager hierarchyManager,
                      WorkspaceManager wspManager,
                      String statement, String language)
             throws InvalidQueryException, RepositoryException {
         this.session = session;
+        this.resolver = resolver;
         this.nsResolver = nsResolver;
         this.itemManager = itemMgr;
         this.hierarchyManager = hierarchyManager;
@@ -121,6 +125,7 @@ public class QueryImpl implements Query {
      *
      * @param session    the session that created this query.
      * @param nsResolver the namespace resolver to be used.
+     * @param resolver
      * @param itemMgr    the item manager of that session.
      * @param hierarchyManager
      * @param wspManager the workspace manager that belongs to the session.
@@ -129,32 +134,29 @@ public class QueryImpl implements Query {
      * @throws RepositoryException   if another error occurs while reading from
      *                               the node.
      */
-    public QueryImpl(Session session, LocalNamespaceMappings nsResolver,
+    public QueryImpl(Session session, LocalNamespaceMappings nsResolver, NamePathResolver resolver,
                      ItemManager itemMgr, HierarchyManager hierarchyManager,
                      WorkspaceManager wspManager, Node node)
         throws InvalidQueryException, RepositoryException {
 
         this.session = session;
+        this.resolver = resolver;
         this.nsResolver = nsResolver;
         this.itemManager = itemMgr;
         this.hierarchyManager = hierarchyManager;
         this.node = node;
         this.wspManager = wspManager;
 
-        try {
-            if (!node.isNodeType(NameFormat.format(QName.NT_QUERY, nsResolver))) {
-                throw new InvalidQueryException("Node is not of type nt:query");
-            }
-            if (node.getSession() != session) {
-                throw new InvalidQueryException("Node belongs to a different session.");
-            }
-            statement = node.getProperty(NameFormat.format(QName.JCR_STATEMENT, nsResolver)).getString();
-            language = node.getProperty(NameFormat.format(QName.JCR_LANGUAGE, nsResolver)).getString();
-            this.wspManager.checkQueryStatement(statement, language,
-                    nsResolver.getLocalNamespaceMappings());
-        } catch (NoPrefixDeclaredException e) {
-            throw new RepositoryException(e.getMessage(), e);
+        if (!node.isNodeType(resolver.getJCRName(NameConstants.NT_QUERY))) {
+            throw new InvalidQueryException("Node is not of type nt:query");
+        }
+        if (node.getSession() != session) {
+            throw new InvalidQueryException("Node belongs to a different session.");
         }
+        statement = node.getProperty(resolver.getJCRName(NameConstants.JCR_STATEMENT)).getString();
+        language = node.getProperty(resolver.getJCRName(NameConstants.JCR_LANGUAGE)).getString();
+        this.wspManager.checkQueryStatement(statement, language,
+                    nsResolver.getLocalNamespaceMappings());
     }
 
     /**
@@ -164,7 +166,7 @@ public class QueryImpl implements Query {
         QueryInfo qI = wspManager.executeQuery(statement, language,
                 nsResolver.getLocalNamespaceMappings());
         return new QueryResultImpl(itemManager, hierarchyManager,
-                qI, nsResolver, session.getValueFactory());
+                qI, resolver, session.getValueFactory());
     }
 
     /**
@@ -199,25 +201,23 @@ public class QueryImpl implements Query {
         LockException, UnsupportedRepositoryOperationException, RepositoryException {
 
         try {
-            Path p = PathFormat.parse(absPath, nsResolver).getNormalizedPath();
+            Path p = resolver.getQPath(absPath).getNormalizedPath();
             if (!p.isAbsolute()) {
                 throw new RepositoryException(absPath + " is not an absolute path");
             }
-            String jcrParent = PathFormat.format(p.getAncestor(1), nsResolver);
+            String jcrParent = resolver.getJCRPath(p.getAncestor(1));
             if (!session.itemExists(jcrParent)) {
                 throw new PathNotFoundException(jcrParent);
             }
-            String relPath = PathFormat.format(p, nsResolver).substring(1);
-            String ntName = NameFormat.format(QName.NT_QUERY, nsResolver);
+            String relPath = resolver.getJCRPath(p).substring(1);
+            String ntName = resolver.getJCRName(NameConstants.NT_QUERY);
             Node queryNode = session.getRootNode().addNode(relPath, ntName);
             // set properties
-            queryNode.setProperty(NameFormat.format(QName.JCR_LANGUAGE, nsResolver), language);
-            queryNode.setProperty(NameFormat.format(QName.JCR_STATEMENT, nsResolver), statement);
+            queryNode.setProperty(resolver.getJCRName(NameConstants.JCR_LANGUAGE), language);
+            queryNode.setProperty(resolver.getJCRName(NameConstants.JCR_STATEMENT), statement);
             node = queryNode;
             return node;
-        } catch (MalformedPathException e) {
-            throw new RepositoryException(e.getMessage(), e);
-        } catch (NoPrefixDeclaredException e) {
+        } catch (org.apache.jackrabbit.conversion.NameException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryManagerImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryManagerImpl.java
index e633418..7b5e027 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryManagerImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryManagerImpl.java
@@ -20,6 +20,7 @@ import org.apache.jackrabbit.jcr2spi.ItemManager;
 import org.apache.jackrabbit.jcr2spi.WorkspaceManager;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManager;
 import org.apache.jackrabbit.jcr2spi.name.LocalNamespaceMappings;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 
 import javax.jcr.Node;
 import javax.jcr.RepositoryException;
@@ -44,6 +45,11 @@ public class QueryManagerImpl implements QueryManager {
     private final LocalNamespaceMappings nsResolver;
 
     /**
+     * Name and Path resolver
+     */
+    private final NamePathResolver resolver;
+
+    /**
      * The <code>ItemManager</code> of for item retrieval in search results
      */
     private final ItemManager itemMgr;
@@ -70,11 +76,13 @@ public class QueryManagerImpl implements QueryManager {
      */
     public QueryManagerImpl(Session session,
                             LocalNamespaceMappings nsResolver,
+                            NamePathResolver resolver,
                             ItemManager itemMgr,
                             HierarchyManager hierarchyManager,
                             WorkspaceManager wspManager) {
         this.session = session;
         this.nsResolver = nsResolver;
+        this.resolver = resolver;
         this.itemMgr = itemMgr;
         this.hierarchyManager = hierarchyManager;
         this.wspManager = wspManager;
@@ -86,7 +94,7 @@ public class QueryManagerImpl implements QueryManager {
     public Query createQuery(String statement, String language)
             throws InvalidQueryException, RepositoryException {
         checkIsAlive();
-        QueryImpl query = new QueryImpl(session, nsResolver, itemMgr, hierarchyManager, wspManager, statement, language);
+        QueryImpl query = new QueryImpl(session, nsResolver, resolver, itemMgr, hierarchyManager, wspManager, statement, language);
         return query;
     }
 
@@ -96,7 +104,7 @@ public class QueryManagerImpl implements QueryManager {
     public Query getQuery(Node node)
             throws InvalidQueryException, RepositoryException {
         checkIsAlive();
-        QueryImpl query = new QueryImpl(session, nsResolver, itemMgr, hierarchyManager, wspManager, node);
+        QueryImpl query = new QueryImpl(session, nsResolver, resolver, itemMgr, hierarchyManager, wspManager, node);
         return query;
     }
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryResultImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryResultImpl.java
index 989e316..306e703 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryResultImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/QueryResultImpl.java
@@ -18,11 +18,9 @@ package org.apache.jackrabbit.jcr2spi.query;
 
 import org.apache.jackrabbit.jcr2spi.ItemManager;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyManager;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QueryInfo;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -58,9 +56,9 @@ class QueryResultImpl implements QueryResult {
     private final QueryInfo queryInfo;
 
     /**
-     * The namespace nsResolver of the session executing the query
+     * The namespace nameResolver of the session executing the query
      */
-    private final NamespaceResolver nsResolver;
+    private final NamePathResolver resolver;
 
     /**
      * The JCR value factory.
@@ -74,17 +72,16 @@ class QueryResultImpl implements QueryResult {
      * @param hierarchyMgr the HierarchyManager of the session executing the
      *                     query.
      * @param queryInfo    the spi query result.
-     * @param nsResolver   the namespace nsResolver of the session executing the
-     *                     query.
+     * @param resolver
      * @param valueFactory the JCR value factory.
      */
     QueryResultImpl(ItemManager itemMgr, HierarchyManager hierarchyMgr,
-                    QueryInfo queryInfo, NamespaceResolver nsResolver,
+                    QueryInfo queryInfo, NamePathResolver resolver,
                     ValueFactory valueFactory) {
         this.itemMgr = itemMgr;
         this.hierarchyMgr = hierarchyMgr;
         this.queryInfo = queryInfo;
-        this.nsResolver = nsResolver;
+        this.resolver = resolver;
         this.valueFactory = valueFactory;
     }
 
@@ -92,19 +89,12 @@ class QueryResultImpl implements QueryResult {
      * {@inheritDoc}
      */
     public String[] getColumnNames() throws RepositoryException {
-        try {
-            QName[] names = queryInfo.getColumnNames();
-            String[] propNames = new String[names.length];
-            for (int i = 0; i < names.length; i++) {
-                propNames[i] = NameFormat.format(names[i], nsResolver);
-            }
-            return propNames;
-        } catch (NoPrefixDeclaredException npde) {
-            String msg = "encountered invalid property name";
-            log.debug(msg);
-            throw new RepositoryException(msg, npde);
-
+        Name[] names = queryInfo.getColumnNames();
+        String[] propNames = new String[names.length];
+        for (int i = 0; i < names.length; i++) {
+            propNames[i] = resolver.getJCRName(names[i]);
         }
+        return propNames;
     }
 
     /**
@@ -118,7 +108,7 @@ class QueryResultImpl implements QueryResult {
      * {@inheritDoc}
      */
     public RowIterator getRows() throws RepositoryException {
-        return new RowIteratorImpl(queryInfo, nsResolver, valueFactory);
+        return new RowIteratorImpl(queryInfo, resolver, valueFactory);
     }
 
     /**
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/RowIteratorImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/RowIteratorImpl.java
index 5b5c535..354ef68 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/RowIteratorImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/query/RowIteratorImpl.java
@@ -29,15 +29,12 @@ import javax.jcr.ValueFactory;
 import javax.jcr.query.Row;
 import javax.jcr.query.RowIterator;
 
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.UnknownPrefixException;
-import org.apache.jackrabbit.value.ValueFormat;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QueryInfo;
 import org.apache.jackrabbit.spi.QueryResultRow;
 import org.apache.jackrabbit.spi.QValue;
+import org.apache.jackrabbit.value.ValueFormat;
 
 /**
  * Implements the {@link javax.jcr.query.RowIterator} interface returned by
@@ -53,12 +50,12 @@ class RowIteratorImpl implements RowIterator {
     /**
      * The column names.
      */
-    private final QName[] columnNames;
+    private final Name[] columnNames;
 
     /**
-     * The <code>NamespaceResolver</code> of the user <code>Session</code>.
+     * The <code>NamePathResolver</code> of the user <code>Session</code>.
      */
-    private final NamespaceResolver nsResolver;
+    private final org.apache.jackrabbit.conversion.NamePathResolver resolver;
 
     /**
      * The JCR value factory.
@@ -70,14 +67,14 @@ class RowIteratorImpl implements RowIterator {
      * nodes.
      *
      * @param queryInfo the query info.
-     * @param resolver  <code>NamespaceResolver</code> of the user
+     * @param nameResolver  <code>NameResolver</code> of the user
      *                  <code>Session</code>.
      * @param vFactory  the JCR value factory.
      */
-    RowIteratorImpl(QueryInfo queryInfo, NamespaceResolver resolver, ValueFactory vFactory) {
+    RowIteratorImpl(QueryInfo queryInfo, NamePathResolver resolver, ValueFactory vFactory) {
         this.rows = queryInfo.getRows();
         this.columnNames = queryInfo.getColumnNames();
-        this.nsResolver = resolver;
+        this.resolver = resolver;
         this.vFactory = vFactory;
     }
 
@@ -180,7 +177,7 @@ class RowIteratorImpl implements RowIterator {
         private Value[] values;
 
         /**
-         * Map of select property <code>QName</code>s. Key: QName, Value:
+         * Map of select property <code>Name</code>s. Key: Name, Value:
          * Integer, which refers to the array index in {@link #values}.
          */
         private Map propertyMap;
@@ -215,7 +212,7 @@ class RowIteratorImpl implements RowIterator {
                         tmp[i] = null;
                     } else {
                         tmp[i] = ValueFormat.getJCRValue(
-                                qVals[i], nsResolver, vFactory);
+                                qVals[i], resolver, vFactory);
                     }
                 }
                 values = tmp;
@@ -249,7 +246,7 @@ class RowIteratorImpl implements RowIterator {
                 propertyMap = tmp;
             }
             try {
-                QName prop = NameFormat.parse(propertyName, nsResolver);
+                Name prop = resolver.getQName(propertyName);
                 Integer idx = (Integer) propertyMap.get(prop);
                 if (idx == null) {
                     throw new ItemNotFoundException(propertyName);
@@ -259,9 +256,7 @@ class RowIteratorImpl implements RowIterator {
                     getValues();
                 }
                 return values[idx.intValue()];
-            } catch (IllegalNameException e) {
-                throw new RepositoryException(e.getMessage(), e);
-            } catch (UnknownPrefixException e) {
+            } catch (org.apache.jackrabbit.conversion.NameException e) {
                 throw new RepositoryException(e.getMessage(), e);
             }
         }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/security/AccessManager.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/security/AccessManager.java
index d13c6d9..0f856f7 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/security/AccessManager.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/security/AccessManager.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.security;
 
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ChangeLog.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ChangeLog.java
index bc3c0a3..36f1ab0 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ChangeLog.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ChangeLog.java
@@ -21,7 +21,7 @@ import org.apache.jackrabbit.jcr2spi.operation.AddNode;
 import org.apache.jackrabbit.jcr2spi.operation.AddProperty;
 import org.apache.jackrabbit.jcr2spi.operation.SetMixin;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.commons.collections.iterators.IteratorChain;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
@@ -308,7 +308,7 @@ public class ChangeLog {
             if (op instanceof AddNode) {
                 AddNode operation = (AddNode) op;
                 if (operation.getParentState() == parent
-                        && operation.getNodeName().equals(state.getQName())) {
+                        && operation.getNodeName().equals(state.getName())) {
                     // TODO: this will not work for name name siblings!
                     it.remove();
                     break;
@@ -316,12 +316,12 @@ public class ChangeLog {
             } else if (op instanceof AddProperty) {
                 AddProperty operation = (AddProperty) op;
                 if (operation.getParentState() == parent
-                        && operation.getPropertyName().equals(state.getQName())) {
+                        && operation.getPropertyName().equals(state.getName())) {
                     it.remove();
                     break;
                 }
             } else if (op instanceof SetMixin &&
-                    QName.JCR_MIXINTYPES.equals(state.getQName()) &&
+                    NameConstants.JCR_MIXINTYPES.equals(state.getName()) &&
                     ((SetMixin)op).getNodeState() == parent) {
                 it.remove();
                 break;
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ItemState.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ItemState.java
index 53092cf..d5dcdd4 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ItemState.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ItemState.java
@@ -20,8 +20,8 @@ import org.apache.jackrabbit.util.WeakIdentityCollection;
 import org.apache.jackrabbit.spi.ItemId;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.PropertyId;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyEntry;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
@@ -166,13 +166,13 @@ public abstract class ItemState {
 
     /**
      * Utility method:
-     * Returns the name of this state. Shortcut for calling 'getQName' on the
+     * Returns the name of this state. Shortcut for calling 'getName' on the
      * {@link ItemState#getHierarchyEntry() hierarchy entry}.
      *
      * @return name of this state
      */
-    public QName getQName() {
-        return getHierarchyEntry().getQName();
+    public Name getName() {
+        return getHierarchyEntry().getName();
     }
 
     /**
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ItemStateValidator.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ItemStateValidator.java
index f311e4f..858beb3 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ItemStateValidator.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/ItemStateValidator.java
@@ -36,9 +36,9 @@ import javax.jcr.version.VersionException;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QItemDefinition;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.PathFactory;
 
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
@@ -92,14 +92,16 @@ public class ItemStateValidator {
      * manager provider
      */
     private final ManagerProvider mgrProvider;
+    private final PathFactory pathFactory;
 
     /**
      * Creates a new <code>ItemStateValidator</code> instance.
      *
      * @param mgrProvider manager provider
      */
-    public ItemStateValidator(ManagerProvider mgrProvider) {
+    public ItemStateValidator(ManagerProvider mgrProvider, PathFactory pathFactory) {
         this.mgrProvider = mgrProvider;
+        this.pathFactory = pathFactory;
     }
 
     /**
@@ -125,7 +127,7 @@ public class ItemStateValidator {
         QNodeDefinition def = nodeState.getDefinition();
 
         // check if primary type satisfies the 'required node types' constraint
-        QName[] requiredPrimaryTypes = def.getRequiredPrimaryTypes();
+        Name[] requiredPrimaryTypes = def.getRequiredPrimaryTypes();
         for (int i = 0; i < requiredPrimaryTypes.length; i++) {
             if (!entPrimary.includesNodeType(requiredPrimaryTypes[i])) {
                 String msg = safeGetJCRPath(nodeState)
@@ -141,9 +143,9 @@ public class ItemStateValidator {
         QPropertyDefinition[] pda = entPrimaryAndMixins.getMandatoryQPropertyDefinitions();
         for (int i = 0; i < pda.length; i++) {
             QPropertyDefinition pd = pda[i];
-            if (!nodeState.hasPropertyName(pd.getQName())) {
+            if (!nodeState.hasPropertyName(pd.getName())) {
                 String msg = safeGetJCRPath(nodeState)
-                        + ": mandatory property " + pd.getQName()
+                        + ": mandatory property " + pd.getName()
                         + " does not exist";
                 log.debug(msg);
                 throw new ConstraintViolationException(msg);
@@ -153,9 +155,9 @@ public class ItemStateValidator {
         QNodeDefinition[] cnda = entPrimaryAndMixins.getMandatoryQNodeDefinitions();
         for (int i = 0; i < cnda.length; i++) {
             QNodeDefinition cnd = cnda[i];
-            if (!nodeState.getNodeEntry().hasNodeEntry(cnd.getQName())) {
+            if (!nodeState.getNodeEntry().hasNodeEntry(cnd.getName())) {
                 String msg = safeGetJCRPath(nodeState)
-                        + ": mandatory child node " + cnd.getQName()
+                        + ": mandatory child node " + cnd.getName()
                         + " does not exist";
                 log.debug(msg);
                 throw new ConstraintViolationException(msg);
@@ -170,10 +172,10 @@ public class ItemStateValidator {
      *
      * @param itemState
      * @return JCR path
-     * @see LogUtil#safeGetJCRPath(ItemState,NamespaceResolver)
+     * @see LogUtil#safeGetJCRPath(ItemState,org.apache.jackrabbit.conversion.PathResolver)
      */
     private String safeGetJCRPath(ItemState itemState) {
-        return LogUtil.safeGetJCRPath(itemState, mgrProvider.getNamespaceResolver());
+        return LogUtil.safeGetJCRPath(itemState, mgrProvider.getPathResolver());
     }
 
     //------------------------------------------------------< check methods >---
@@ -247,7 +249,7 @@ public class ItemStateValidator {
 
         NodeState parent = propState.getParent();
         QPropertyDefinition def = propState.getDefinition();
-        checkWriteProperty(parent, propState.getQName(), def, options);
+        checkWriteProperty(parent, propState.getName(), def, options);
     }
 
     /**
@@ -279,7 +281,7 @@ public class ItemStateValidator {
      * @throws PathNotFoundException
      * @throws RepositoryException
      */
-    public void checkAddProperty(NodeState parentState, QName propertyName, QPropertyDefinition definition, int options)
+    public void checkAddProperty(NodeState parentState, Name propertyName, QPropertyDefinition definition, int options)
         throws ConstraintViolationException, AccessDeniedException,
         VersionException, LockException, ItemNotFoundException,
         ItemExistsException, PathNotFoundException, RepositoryException {
@@ -302,7 +304,7 @@ public class ItemStateValidator {
      * @throws PathNotFoundException
      * @throws RepositoryException
      */
-    private void checkWriteProperty(NodeState parentState, QName propertyName, QPropertyDefinition definition, int options)
+    private void checkWriteProperty(NodeState parentState, Name propertyName, QPropertyDefinition definition, int options)
         throws ConstraintViolationException, AccessDeniedException,
         VersionException, LockException, ItemNotFoundException,
         ItemExistsException, PathNotFoundException, RepositoryException {
@@ -312,7 +314,7 @@ public class ItemStateValidator {
         // access restriction on prop.
         if ((options & CHECK_ACCESS) == CHECK_ACCESS) {
             // make sure current session is granted write access on new prop
-            Path relPath = Path.create(propertyName, Path.INDEX_UNDEFINED);
+            Path relPath = pathFactory.create(propertyName);
             if (!mgrProvider.getAccessManager().isGranted(parentState, relPath, new String[] {AccessManager.SET_PROPERTY_ACTION})) {
                 throw new AccessDeniedException(safeGetJCRPath(parentState) + ": not allowed to create property with name " + propertyName);
             }
@@ -360,8 +362,8 @@ public class ItemStateValidator {
      * @throws ItemExistsException
      * @throws RepositoryException
      */
-    public void checkAddNode(NodeState parentState, QName nodeName,
-                             QName nodeTypeName, int options)
+    public void checkAddNode(NodeState parentState, Name nodeName,
+                             Name nodeTypeName, int options)
             throws ConstraintViolationException, AccessDeniedException,
             VersionException, LockException, ItemNotFoundException,
             ItemExistsException, RepositoryException {
@@ -371,7 +373,7 @@ public class ItemStateValidator {
         // access restrictions on new node
         if ((options & CHECK_ACCESS) == CHECK_ACCESS) {
             // make sure current session is granted write access on parent node
-            Path relPath = Path.create(nodeName, Path.INDEX_UNDEFINED);
+            Path relPath = pathFactory.create(nodeName);
             if (!mgrProvider.getAccessManager().isGranted(parentState, relPath, new String[] {AccessManager.ADD_NODE_ACTION})) {
                 throw new AccessDeniedException(safeGetJCRPath(parentState) + ": not allowed to add child node '" + nodeName +"'");
             }
@@ -545,7 +547,7 @@ public class ItemStateValidator {
      * @throws ItemExistsException
      * @throws RepositoryException
      */
-    private void checkCollision(NodeState parentState, QName propertyName) throws ItemExistsException, RepositoryException {
+    private void checkCollision(NodeState parentState, Name propertyName) throws ItemExistsException, RepositoryException {
         NodeEntry parentEntry = (NodeEntry) parentState.getHierarchyEntry();
         // check for name collisions with existing child nodes
         if (parentEntry.hasNodeEntry(propertyName)) {
@@ -558,7 +560,7 @@ public class ItemStateValidator {
         if (pe != null) {
             try {
                 pe.getPropertyState();
-                throw new ItemExistsException("Property '" + pe.getQName() + "' already exists.");
+                throw new ItemExistsException("Property '" + pe.getName() + "' already exists.");
             } catch (ItemNotFoundException e) {
                 // apparently conflicting entry does not exist any more
                 // ignore and return
@@ -575,7 +577,7 @@ public class ItemStateValidator {
      * @throws ConstraintViolationException
      * @throws NoSuchNodeTypeException
      */
-    private void checkCollision(NodeState parentState, QName nodeName, QName nodeTypeName) throws RepositoryException, ConstraintViolationException, NoSuchNodeTypeException {
+    private void checkCollision(NodeState parentState, Name nodeName, Name nodeTypeName) throws RepositoryException, ConstraintViolationException, NoSuchNodeTypeException {
         if (parentState.hasPropertyName(nodeName)) {
             // there's already a property with that name
             throw new ItemExistsException("cannot add child node '"
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/NodeState.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/NodeState.java
index 95e3ec2..478b23e 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/NodeState.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/NodeState.java
@@ -21,11 +21,12 @@ import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
 import org.apache.jackrabbit.jcr2spi.util.StateUtility;
 import org.apache.jackrabbit.jcr2spi.nodetype.ItemDefinitionProvider;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.ItemId;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.NodeInfo;
+import org.apache.jackrabbit.name.NameConstants;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -49,7 +50,7 @@ public class NodeState extends ItemState {
     /**
      * the name of this node's primary type
      */
-    private final QName nodeTypeName;
+    private final Name nodeTypeName;
 
     /**
      * Definition of this node state
@@ -59,7 +60,7 @@ public class NodeState extends ItemState {
     /**
      * the names of this node's mixin types
      */
-    private QName[] mixinTypeNames = QName.EMPTY_ARRAY;
+    private Name[] mixinTypeNames = Name.EMPTY_ARRAY;
 
     /**
      * Constructs a NEW NodeState
@@ -71,7 +72,7 @@ public class NodeState extends ItemState {
      * @param definition
      * @param definitionProvider
      */
-    protected NodeState(NodeEntry entry, QName nodeTypeName, QName[] mixinTypeNames,
+    protected NodeState(NodeEntry entry, Name nodeTypeName, Name[] mixinTypeNames,
                         ItemStateFactory isf, QNodeDefinition definition,
                         ItemDefinitionProvider definitionProvider) {
         super(Status.NEW, entry, isf, definitionProvider);
@@ -255,7 +256,7 @@ public class NodeState extends ItemState {
 
                 // if property state defines a modified jcr:mixinTypes the parent
                 // is listed as modified state and needs to be processed at the end.
-                if (QName.JCR_MIXINTYPES.equals(modState.getQName())) {
+                if (NameConstants.JCR_MIXINTYPES.equals(modState.getName())) {
                     try {
                         modifiedParent(modState.getParent(), modState, modParents);
                     } catch (RepositoryException e) {
@@ -332,7 +333,7 @@ public class NodeState extends ItemState {
      *
      * @return the name of this node's node type.
      */
-    public QName getNodeTypeName() {
+    public Name getNodeTypeName() {
         return nodeTypeName;
     }
 
@@ -341,7 +342,7 @@ public class NodeState extends ItemState {
      *
      * @return a set of the names of this node's mixin types.
      */
-    public QName[] getMixinTypeNames() {
+    public Name[] getMixinTypeNames() {
         return mixinTypeNames;
     }
 
@@ -351,7 +352,7 @@ public class NodeState extends ItemState {
      *
      * @param mixinTypeNames
      */
-    public void setMixinTypeNames(QName[] mixinTypeNames) {
+    public void setMixinTypeNames(Name[] mixinTypeNames) {
         if (mixinTypeNames != null) {
             this.mixinTypeNames = mixinTypeNames;
         }
@@ -363,10 +364,10 @@ public class NodeState extends ItemState {
      *
      * @return array of NodeType names
      */
-    public synchronized QName[] getNodeTypeNames() {
+    public synchronized Name[] getNodeTypeNames() {
         // mixin types
-        QName[] mixinNames = getMixinTypeNames();
-        QName[] types = new QName[mixinNames.length + 1];
+        Name[] mixinNames = getMixinTypeNames();
+        Name[] types = new Name[mixinNames.length + 1];
         System.arraycopy(mixinNames, 0, types, 0, mixinNames.length);
         // primary type
         types[types.length - 1] = getNodeTypeName();
@@ -402,12 +403,12 @@ public class NodeState extends ItemState {
      * Determines if there is a valid <code>NodeEntry</code> with the
      * specified <code>name</code> and <code>index</code>.
      *
-     * @param name  <code>QName</code> object specifying a node name.
+     * @param name  <code>Name</code> object specifying a node name.
      * @param index 1-based index if there are same-name child node entries.
      * @return <code>true</code> if there is a <code>NodeEntry</code> with
      * the specified <code>name</code> and <code>index</code>.
      */
-    public boolean hasChildNodeEntry(QName name, int index) {
+    public boolean hasChildNodeEntry(Name name, int index) {
         return getNodeEntry().hasNodeEntry(name, index);
     }
 
@@ -417,13 +418,13 @@ public class NodeState extends ItemState {
      * and index. Throws <code>ItemNotFoundException</code> if there's no
      * matching, valid entry.
      *
-     * @param nodeName <code>QName</code> object specifying a node name.
+     * @param nodeName <code>Name</code> object specifying a node name.
      * @param index 1-based index if there are same-name child node entries.
      * @return The <code>NodeState</code> with the specified name and index
      * @throws ItemNotFoundException
      * @throws RepositoryException
      */
-    public NodeState getChildNodeState(QName nodeName, int index) throws ItemNotFoundException, RepositoryException {
+    public NodeState getChildNodeState(Name nodeName, int index) throws ItemNotFoundException, RepositoryException {
         NodeEntry ne = getNodeEntry().getNodeEntry(nodeName, index, true);
         if (ne != null) {
             return ne.getNodeState();
@@ -436,11 +437,11 @@ public class NodeState extends ItemState {
     /**
      * Utility
      *
-     * @param propName <code>QName</code> object specifying a property name
+     * @param propName <code>Name</code> object specifying a property name
      * @return <code>true</code> if there is a valid property entry with the
-     * specified <code>QName</code>.
+     * specified <code>Name</code>.
      */
-    public boolean hasPropertyName(QName propName) {
+    public boolean hasPropertyName(Name propName) {
         return getNodeEntry().hasPropertyEntry(propName);
     }
 
@@ -455,10 +456,10 @@ public class NodeState extends ItemState {
      * @throws RepositoryException If an error occurs while retrieving the
      * property state.
      *
-     * @see NodeEntry#getPropertyEntry(QName, boolean)
+     * @see NodeEntry#getPropertyEntry(Name, boolean)
      * @see PropertyEntry#getPropertyState()
      */
-    public PropertyState getPropertyState(QName propertyName) throws ItemNotFoundException, RepositoryException {
+    public PropertyState getPropertyState(Name propertyName) throws ItemNotFoundException, RepositoryException {
         PropertyEntry pe = getNodeEntry().getPropertyEntry(propertyName, true);
         if (pe != null) {
             return pe.getPropertyState();
@@ -495,12 +496,12 @@ public class NodeState extends ItemState {
      *
      * @param newParent
      * @param childState
-     * @param newName <code>QName</code> object specifying the entry's new name
+     * @param newName <code>Name</code> object specifying the entry's new name
      * @throws RepositoryException if the given child state is not a child
      * of this node state.
      */
     synchronized void moveChildNodeEntry(NodeState newParent, NodeState childState,
-                                         QName newName, QNodeDefinition newDefinition)
+                                         Name newName, QNodeDefinition newDefinition)
         throws RepositoryException {
         // move child entry
         childState.getNodeEntry().move(newName, newParent.getNodeEntry(), true);
@@ -525,7 +526,7 @@ public class NodeState extends ItemState {
             l = new ArrayList(2);
             modParents.put(parent, l);
         }
-        if (childState != null && !childState.isNode() && StateUtility.isUuidOrMixin(childState.getQName())) {
+        if (childState != null && !childState.isNode() && StateUtility.isUuidOrMixin(childState.getName())) {
             l.add(childState);
         }
     }
@@ -538,15 +539,15 @@ public class NodeState extends ItemState {
     private static void adjustNodeState(NodeState parent, PropertyState[] props) {
         for (int i = 0; i < props.length; i++) {
             PropertyState propState = props[i];
-            if (QName.JCR_UUID.equals(propState.getQName())) {
+            if (NameConstants.JCR_UUID.equals(propState.getName())) {
                 if (propState.getStatus() == Status.REMOVED) {
                     parent.getNodeEntry().setUniqueID(null);
                 } else {
                     // retrieve uuid from persistent layer
                     propState.reload(false);
                 }
-            } else if (QName.JCR_MIXINTYPES.equals(propState.getQName())) {
-                QName[] mixins = StateUtility.getMixinNames(propState);
+            } else if (NameConstants.JCR_MIXINTYPES.equals(propState.getName())) {
+                Name[] mixins = StateUtility.getMixinNames(propState);
                 parent.setMixinTypeNames(mixins);
             } // else: ignore.
         }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/SessionItemStateManager.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/SessionItemStateManager.java
index dc1d079..2ae5cf9 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/SessionItemStateManager.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/SessionItemStateManager.java
@@ -49,12 +49,13 @@ import org.apache.jackrabbit.jcr2spi.ManagerProvider;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.QValueFactory;
 import org.apache.jackrabbit.uuid.UUID;
+import org.apache.jackrabbit.name.NameConstants;
 
 import javax.jcr.InvalidItemStateException;
 import javax.jcr.ReferentialIntegrityException;
@@ -271,7 +272,7 @@ public class SessionItemStateManager implements UpdatableItemStateManager, Opera
      */
     public void visit(AddProperty operation) throws ValueFormatException, LockException, ConstraintViolationException, AccessDeniedException, ItemExistsException, UnsupportedRepositoryOperationException, VersionException, RepositoryException {
         NodeState parent = operation.getParentState();
-        QName propertyName = operation.getPropertyName();
+        Name propertyName = operation.getPropertyName();
         QPropertyDefinition pDef = operation.getDefinition();
         int targetType = pDef.getRequiredType();
         if (targetType == PropertyType.UNDEFINED) {
@@ -347,12 +348,12 @@ public class SessionItemStateManager implements UpdatableItemStateManager, Opera
      */
     public void visit(SetMixin operation) throws ConstraintViolationException, AccessDeniedException, NoSuchNodeTypeException, UnsupportedRepositoryOperationException, VersionException, RepositoryException {
         // NOTE: nodestate is only modified upon save of the changes!
-        QName[] mixinNames = operation.getMixinNames();
+        Name[] mixinNames = operation.getMixinNames();
         NodeState nState = operation.getNodeState();
         NodeEntry nEntry = (NodeEntry) nState.getHierarchyEntry();
 
         // new array of mixinNames to be set on the nodestate (and corresponding property state)
-        PropertyEntry mixinEntry = nEntry.getPropertyEntry(QName.JCR_MIXINTYPES);
+        PropertyEntry mixinEntry = nEntry.getPropertyEntry(NameConstants.JCR_MIXINTYPES);
         if (mixinNames != null && mixinNames.length > 0) {
             // update/create corresponding property state
             if (mixinEntry != null) {
@@ -363,10 +364,10 @@ public class SessionItemStateManager implements UpdatableItemStateManager, Opera
             } else {
                 // create new jcr:mixinTypes property
                 ItemDefinitionProvider defProvider = mgrProvider.getItemDefinitionProvider();
-                QPropertyDefinition pd = defProvider.getQPropertyDefinition(nState, QName.JCR_MIXINTYPES, PropertyType.NAME, true);
+                QPropertyDefinition pd = defProvider.getQPropertyDefinition(nState, NameConstants.JCR_MIXINTYPES, PropertyType.NAME, true);
                 QValue[] mixinValue = getQValues(mixinNames, qValueFactory);
                 int options = ItemStateValidator.CHECK_LOCK | ItemStateValidator.CHECK_VERSIONING;
-                addPropertyState(nState, pd.getQName(), pd.getRequiredType(), mixinValue, pd, options);
+                addPropertyState(nState, pd.getName(), pd.getRequiredType(), mixinValue, pd, options);
             }
             nState.markModified();
             transientStateMgr.addOperation(operation);
@@ -587,7 +588,7 @@ public class SessionItemStateManager implements UpdatableItemStateManager, Opera
      * @throws VersionException
      * @throws RepositoryException
      */
-    private void addPropertyState(NodeState parent, QName propertyName,
+    private void addPropertyState(NodeState parent, Name propertyName,
                                   int propertyType, QValue[] values,
                                   QPropertyDefinition pDef, int options)
         throws LockException, ConstraintViolationException, AccessDeniedException, ItemExistsException, NoSuchNodeTypeException, UnsupportedRepositoryOperationException, VersionException, RepositoryException {
@@ -598,7 +599,7 @@ public class SessionItemStateManager implements UpdatableItemStateManager, Opera
         transientStateMgr.createNewPropertyState(propertyName, parent, pDef, values, propertyType);
     }
 
-    private void addNodeState(NodeState parent, QName nodeName, QName nodeTypeName,
+    private void addNodeState(NodeState parent, Name nodeName, Name nodeTypeName,
                               String uuid, QNodeDefinition definition, int options)
         throws RepositoryException, ConstraintViolationException, AccessDeniedException,
         UnsupportedRepositoryOperationException, NoSuchNodeTypeException,
@@ -628,20 +629,20 @@ public class SessionItemStateManager implements UpdatableItemStateManager, Opera
         if (uuid != null) {
             QValue[] value = getQValues(uuid, qValueFactory);
             ItemDefinitionProvider defProvider = mgrProvider.getItemDefinitionProvider();
-            QPropertyDefinition pDef = defProvider.getQPropertyDefinition(QName.MIX_REFERENCEABLE, QName.JCR_UUID, PropertyType.STRING, false);
-            addPropertyState(nodeState, QName.JCR_UUID, PropertyType.STRING, value, pDef, 0);
+            QPropertyDefinition pDef = defProvider.getQPropertyDefinition(NameConstants.MIX_REFERENCEABLE, NameConstants.JCR_UUID, PropertyType.STRING, false);
+            addPropertyState(nodeState, NameConstants.JCR_UUID, PropertyType.STRING, value, pDef, 0);
         }
 
         // add 'auto-create' properties defined in node type
         QPropertyDefinition[] pda = ent.getAutoCreateQPropertyDefinitions();
         for (int i = 0; i < pda.length; i++) {
             QPropertyDefinition pd = pda[i];
-            if (!nodeState.hasPropertyName(pd.getQName())) {
+            if (!nodeState.hasPropertyName(pd.getName())) {
                 QValue[] autoValue = computeSystemGeneratedPropertyValues(nodeState, pd);
                 if (autoValue != null) {
                     int propOptions = ItemStateValidator.CHECK_NONE;
                     // execute 'addProperty' without adding operation.
-                    addPropertyState(nodeState, pd.getQName(), pd.getRequiredType(), autoValue, pd, propOptions);
+                    addPropertyState(nodeState, pd.getName(), pd.getRequiredType(), autoValue, pd, propOptions);
                 }
             }
         }
@@ -652,7 +653,7 @@ public class SessionItemStateManager implements UpdatableItemStateManager, Opera
             QNodeDefinition nd = nda[i];
             // execute 'addNode' without adding the operation.
             int opt = ItemStateValidator.CHECK_LOCK | ItemStateValidator.CHECK_COLLISION;
-            addNodeState(nodeState, nd.getQName(), nd.getDefaultPrimaryType(), null, nd, opt);
+            addNodeState(nodeState, nd.getName(), nd.getDefaultPrimaryType(), null, nd, opt);
         }
     }
 
@@ -709,33 +710,33 @@ public class SessionItemStateManager implements UpdatableItemStateManager, Opera
         } else if (def.isAutoCreated()) {
             // handle known predefined nodetypes that declare auto-created
             // properties without default values
-            QName declaringNT = def.getDeclaringNodeType();
-            QName name = def.getQName();
-            if (QName.MIX_REFERENCEABLE.equals(declaringNT) && QName.JCR_UUID.equals(name)) {
+            Name declaringNT = def.getDeclaringNodeType();
+            Name name = def.getName();
+            if (NameConstants.MIX_REFERENCEABLE.equals(declaringNT) && NameConstants.JCR_UUID.equals(name)) {
                 // mix:referenceable node type defines jcr:uuid
                 genValues = getQValues(parent.getUniqueID(), qValueFactory);
-            } else if (QName.NT_BASE.equals(declaringNT)) {
+            } else if (NameConstants.NT_BASE.equals(declaringNT)) {
                 // nt:base node type
-                if (QName.JCR_PRIMARYTYPE.equals(name)) {
+                if (NameConstants.JCR_PRIMARYTYPE.equals(name)) {
                     // jcr:primaryType property
                     genValues = new QValue[]{qValueFactory.create(parent.getNodeTypeName())};
-                } else if (QName.JCR_MIXINTYPES.equals(name)) {
+                } else if (NameConstants.JCR_MIXINTYPES.equals(name)) {
                     // jcr:mixinTypes property
-                    QName[] mixins = parent.getMixinTypeNames();
+                    Name[] mixins = parent.getMixinTypeNames();
                     genValues = getQValues(mixins, qValueFactory);
                 }
-            } else if (QName.NT_HIERARCHYNODE.equals(declaringNT) && QName.JCR_CREATED.equals(name)) {
+            } else if (NameConstants.NT_HIERARCHYNODE.equals(declaringNT) && NameConstants.JCR_CREATED.equals(name)) {
                 // nt:hierarchyNode node type defines jcr:created property
                 genValues = new QValue[]{qValueFactory.create(Calendar.getInstance())};
-            } else if (QName.NT_RESOURCE.equals(declaringNT) && QName.JCR_LASTMODIFIED.equals(name)) {
+            } else if (NameConstants.NT_RESOURCE.equals(declaringNT) && NameConstants.JCR_LASTMODIFIED.equals(name)) {
                 // nt:resource node type defines jcr:lastModified property
                 genValues = new QValue[]{qValueFactory.create(Calendar.getInstance())};
-            } else if (QName.NT_VERSION.equals(declaringNT) && QName.JCR_CREATED.equals(name)) {
+            } else if (NameConstants.NT_VERSION.equals(declaringNT) && NameConstants.JCR_CREATED.equals(name)) {
                 // nt:version node type defines jcr:created property
                 genValues = new QValue[]{qValueFactory.create(Calendar.getInstance())};
             } else {
                 // TODO: TOBEFIXED. other nodetype -> build some default value
-                log.warn("Missing implementation. Nodetype " + def.getDeclaringNodeType() + " defines autocreated property " + def.getQName() + " without default value.");
+                log.warn("Missing implementation. Nodetype " + def.getDeclaringNodeType() + " defines autocreated property " + def.getName() + " without default value.");
             }
         }
         return genValues;
@@ -744,9 +745,9 @@ public class SessionItemStateManager implements UpdatableItemStateManager, Opera
     /**
      * @param qNames
      * @param factory
-     * @return An array of QValue objects from the given <code>QName</code>s
+     * @return An array of QValue objects from the given <code>Name</code>s
      */
-    private static QValue[] getQValues(QName[] qNames, QValueFactory factory) {
+    private static QValue[] getQValues(Name[] qNames, QValueFactory factory) {
         QValue[] ret = new QValue[qNames.length];
         for (int i = 0; i < qNames.length; i++) {
             ret[i] = factory.create(qNames[i]);
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientISFactory.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientISFactory.java
index 00b4a0d..66e2b4b 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientISFactory.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientISFactory.java
@@ -22,7 +22,7 @@ import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.PropertyId;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
 import org.apache.jackrabbit.jcr2spi.nodetype.ItemDefinitionProvider;
@@ -53,12 +53,12 @@ public final class TransientISFactory extends AbstractItemStateFactory implement
     //------------------------------------------< TransientItemStateFactory >---
     /**
      * @inheritDoc
-     * @see TransientItemStateFactory#createNewNodeState(NodeEntry , QName, QNodeDefinition)
+     * @see TransientItemStateFactory#createNewNodeState(NodeEntry , Name, QNodeDefinition)
      */
-    public NodeState createNewNodeState(NodeEntry entry, QName nodetypeName,
+    public NodeState createNewNodeState(NodeEntry entry, Name nodetypeName,
                                         QNodeDefinition definition) {
 
-        NodeState nodeState = new NodeState(entry, nodetypeName, QName.EMPTY_ARRAY, this, definition, defProvider);
+        NodeState nodeState = new NodeState(entry, nodetypeName, Name.EMPTY_ARRAY, this, definition, defProvider);
 
         // notify listeners that a node state has been created
         notifyCreated(nodeState);
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientItemStateFactory.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientItemStateFactory.java
index d408f61..fcef28d 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientItemStateFactory.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientItemStateFactory.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.state;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
 import org.apache.jackrabbit.spi.QNodeDefinition;
@@ -38,7 +38,7 @@ public interface TransientItemStateFactory extends ItemStateFactory {
      * @return the created <code>NodeState</code>
      */
     public NodeState createNewNodeState(NodeEntry entry,
-                                        QName nodeTypeName,
+                                        Name nodeTypeName,
                                         QNodeDefinition definition);
 
     /**
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientItemStateManager.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientItemStateManager.java
index a79a778..7555c62 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientItemStateManager.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/TransientItemStateManager.java
@@ -18,7 +18,7 @@ package org.apache.jackrabbit.jcr2spi.state;
 
 import org.apache.jackrabbit.jcr2spi.operation.Operation;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QValue;
@@ -35,9 +35,9 @@ import java.util.Iterator;
  * {@link ItemState}s and also provides methods to create new item states.
  * While all other modifications can be invoked on the item state instances itself,
  * creating a new node state is done using
- * {@link #createNewNodeState(QName, String, QName, QNodeDefinition, NodeState)}
+ * {@link #createNewNodeState(Name, String, Name, QNodeDefinition, NodeState)}
  * and
- * {@link #createNewPropertyState(QName, NodeState, QPropertyDefinition, QValue[], int)}.
+ * {@link #createNewPropertyState(Name, NodeState, QPropertyDefinition, QValue[], int)}.
  */
 public class TransientItemStateManager implements ItemStateCreationListener {
 
@@ -96,7 +96,7 @@ public class TransientItemStateManager implements ItemStateCreationListener {
      * @param parent       the parent of the new node state.
      * @return a new transient {@link NodeState}.
      */
-    NodeState createNewNodeState(QName nodeName, String uniqueID, QName nodeTypeName,
+    NodeState createNewNodeState(Name nodeName, String uniqueID, Name nodeTypeName,
                                  QNodeDefinition definition, NodeState parent)
             throws RepositoryException {
         NodeState nodeState = ((NodeEntry) parent.getHierarchyEntry()).addNewNodeEntry(nodeName, uniqueID, nodeTypeName, definition);
@@ -118,7 +118,7 @@ public class TransientItemStateManager implements ItemStateCreationListener {
      * @throws ConstraintViolationException
      * @throws RepositoryException
      */
-    PropertyState createNewPropertyState(QName propName, NodeState parent,
+    PropertyState createNewPropertyState(Name propName, NodeState parent,
                                          QPropertyDefinition definition,
                                          QValue[] values, int propertyType)
         throws ItemExistsException, ConstraintViolationException, RepositoryException {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/WorkspaceItemStateFactory.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/WorkspaceItemStateFactory.java
index 0d1eaa0..fcfb7e9 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/WorkspaceItemStateFactory.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/WorkspaceItemStateFactory.java
@@ -18,6 +18,11 @@ package org.apache.jackrabbit.jcr2spi.state;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
+import org.apache.jackrabbit.jcr2spi.nodetype.ItemDefinitionProvider;
+import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
+import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
+import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyEntry;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.PropertyId;
 import org.apache.jackrabbit.spi.NodeInfo;
@@ -25,13 +30,8 @@ import org.apache.jackrabbit.spi.PropertyInfo;
 import org.apache.jackrabbit.spi.SessionInfo;
 import org.apache.jackrabbit.spi.RepositoryService;
 import org.apache.jackrabbit.spi.ItemInfo;
-import org.apache.jackrabbit.jcr2spi.nodetype.ItemDefinitionProvider;
-import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
-import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
-import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyEntry;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
@@ -163,7 +163,7 @@ public class WorkspaceItemStateFactory extends AbstractItemStateFactory implemen
     public NodeReferences getNodeReferences(NodeState nodeState) {
         NodeEntry entry = nodeState.getNodeEntry();
         // shortcut
-        if (entry.getUniqueID() == null || !entry.hasPropertyEntry(QName.JCR_UUID)) {
+        if (entry.getUniqueID() == null || !entry.hasPropertyEntry(NameConstants.JCR_UUID)) {
             // for sure not referenceable
             return EmptyNodeReferences.getInstance();
         }
@@ -263,7 +263,7 @@ public class WorkspaceItemStateFactory extends AbstractItemStateFactory implemen
         List propNames = new ArrayList();
         for (Iterator it = info.getPropertyIds(); it.hasNext(); ) {
             PropertyId pId = (PropertyId) it.next();
-            QName propertyName = pId.getQName();
+            Name propertyName = pId.getName();
             propNames.add(propertyName);
         }
         try {
@@ -315,11 +315,11 @@ public class WorkspaceItemStateFactory extends AbstractItemStateFactory implemen
             // entries are missing -> calculate relative path.
             Path anyParentPath = anyParent.getPath();
             Path relPath = anyParentPath.computeRelativePath(info.getPath());
-            Path.PathElement[] missingElems = relPath.getElements();
+            Path.Element[] missingElems = relPath.getElements();
 
             NodeEntry entry = anyParent;
             for (int i = 0; i < missingElems.length; i++) {
-                QName name = missingElems[i].getName();
+                Name name = missingElems[i].getName();
                 int index = missingElems[i].getNormalizedIndex();
                 entry = createIntermediateNodeEntry(entry, name, index);
             }
@@ -329,8 +329,6 @@ public class WorkspaceItemStateFactory extends AbstractItemStateFactory implemen
             return createNodeState(info, entry);
         } catch (PathNotFoundException e) {
             throw new ItemNotFoundException(e.getMessage(), e);
-        } catch (MalformedPathException e) {
-            throw new RepositoryException(e.getMessage(), e);
         }
     }
 
@@ -348,18 +346,18 @@ public class WorkspaceItemStateFactory extends AbstractItemStateFactory implemen
             // entries are missing -> calculate relative path.
             Path anyParentPath = anyParent.getPath();
             Path relPath = anyParentPath.computeRelativePath(info.getPath());
-            Path.PathElement[] missingElems = relPath.getElements();
+            Path.Element[] missingElems = relPath.getElements();
             NodeEntry entry = anyParent;
             int i = 0;
             // NodeEntries except for the very last 'missingElem'
             while (i < missingElems.length - 1) {
-                QName name = missingElems[i].getName();
+                Name name = missingElems[i].getName();
                 int index = missingElems[i].getNormalizedIndex();
                 entry = createIntermediateNodeEntry(entry, name, index);
                 i++;
             }
             // create PropertyEntry for the last element if not existing yet
-            QName propName = missingElems[i].getName();
+            Name propName = missingElems[i].getName();
             PropertyEntry propEntry = entry.getPropertyEntry(propName);
             if (propEntry == null) {
                 propEntry = entry.addPropertyEntry(propName);
@@ -367,8 +365,6 @@ public class WorkspaceItemStateFactory extends AbstractItemStateFactory implemen
             return createPropertyState(info, propEntry);
         } catch (PathNotFoundException e) {
             throw new ItemNotFoundException(e.getMessage());
-        } catch (MalformedPathException e) {
-            throw new RepositoryException(e.getMessage());
         }
     }
 
@@ -380,7 +376,7 @@ public class WorkspaceItemStateFactory extends AbstractItemStateFactory implemen
      * @return
      * @throws RepositoryException
      */
-    private static NodeEntry createIntermediateNodeEntry(NodeEntry parentEntry, QName name, int index) throws RepositoryException {
+    private static NodeEntry createIntermediateNodeEntry(NodeEntry parentEntry, Name name, int index) throws RepositoryException {
         NodeEntry entry;
         if (parentEntry.hasNodeEntry(name, index)) {
             entry = parentEntry.getNodeEntry(name, index);
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/util/LogUtil.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/util/LogUtil.java
index bf501bb..d7b4695 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/util/LogUtil.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/util/LogUtil.java
@@ -18,16 +18,15 @@ package org.apache.jackrabbit.jcr2spi.util;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.jcr2spi.state.ItemState;
 import org.apache.jackrabbit.spi.ItemId;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.conversion.PathResolver;
 
 import javax.jcr.RepositoryException;
+import javax.jcr.NamespaceException;
 
 /**
  * <code>LogUtil</code>...
@@ -41,13 +40,13 @@ public class LogUtil {
      * error messages etc.
      *
      * @param qPath path to convert
-     * @param nsResolver
+     * @param pathResolver
      * @return JCR path
      */
-    public static String safeGetJCRPath(Path qPath, NamespaceResolver nsResolver) {
+    public static String safeGetJCRPath(Path qPath, PathResolver pathResolver) {
         try {
-            return PathFormat.format(qPath, nsResolver);
-        } catch (NoPrefixDeclaredException npde) {
+            return pathResolver.getJCRPath(qPath);
+        } catch (NamespaceException e) {
             log.error("failed to convert " + qPath + " to JCR path.");
             // return string representation of internal path as a fallback
             return qPath.toString();
@@ -59,12 +58,12 @@ public class LogUtil {
      * error messages etc.
      *
      * @param itemState
-     * @param nsResolver
+     * @param pathResolver
      * @return JCR path
      */
-    public static String safeGetJCRPath(ItemState itemState, NamespaceResolver nsResolver) {
+    public static String safeGetJCRPath(ItemState itemState, PathResolver pathResolver) {
         try {
-            return safeGetJCRPath(itemState.getHierarchyEntry().getPath(), nsResolver);
+            return safeGetJCRPath(itemState.getHierarchyEntry().getPath(), pathResolver);
         } catch (RepositoryException e) {
             ItemId id = itemState.getId();
             log.error("failed to convert " + id + " to JCR path.");
@@ -73,18 +72,18 @@ public class LogUtil {
     }
 
     /**
-     * Failsafe conversion of a <code>QName</code> to a JCR name for use in
+     * Failsafe conversion of a <code>Name</code> to a JCR name for use in
      * error messages etc.
      *
      * @param qName
-     * @param nsResolver
-     * @return JCR name or String representation of the given <code>QName</code>
+     * @param nameResolver
+     * @return JCR name or String representation of the given <code>Name</code>
      * in case the resolution fails.
      */
-    public static String saveGetJCRName(QName qName, NamespaceResolver nsResolver) {
+    public static String saveGetJCRName(Name qName, NameResolver nameResolver) {
         try {
-            return NameFormat.format(qName, nsResolver);
-        } catch (NoPrefixDeclaredException e) {
+            return nameResolver.getJCRName(qName);
+        } catch (NamespaceException e) {
             log.error("failed to convert " + qName + " to JCR name.");
             return qName.toString();
         }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/util/StateUtility.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/util/StateUtility.java
index a0d833e..b992372 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/util/StateUtility.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/util/StateUtility.java
@@ -18,12 +18,13 @@ package org.apache.jackrabbit.jcr2spi.util;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.jcr2spi.state.PropertyState;
 import org.apache.jackrabbit.jcr2spi.state.Status;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 import org.apache.jackrabbit.spi.QValue;
+import org.apache.jackrabbit.name.NameConstants;
 
 import javax.jcr.RepositoryException;
 
@@ -39,20 +40,20 @@ public class StateUtility {
      * @param ps
      * @return
      * @throws IllegalArgumentException if the name of the PropertyState is NOT
-     * {@link QName#JCR_MIXINTYPES}
+     * {@link NameConstants#JCR_MIXINTYPES}
      */
-    public static QName[] getMixinNames(PropertyState ps) {
-        if (!QName.JCR_MIXINTYPES.equals(ps.getQName())) {
+    public static Name[] getMixinNames(PropertyState ps) {
+        if (!NameConstants.JCR_MIXINTYPES.equals(ps.getName())) {
             throw new IllegalArgumentException();
         }
         if (ps.getStatus() == Status.REMOVED) {
-            return QName.EMPTY_ARRAY;
+            return Name.EMPTY_ARRAY;
         } else {
             QValue[] values = ps.getValues();
-            QName[] newMixins = new QName[values.length];
+            Name[] newMixins = new Name[values.length];
             for (int i = 0; i < values.length; i++) {
                 try {
-                    newMixins[i] = values[i].getQName();
+                    newMixins[i] = values[i].getName();
                 } catch (RepositoryException e) {
                     // ignore: should never occur.
                 }
@@ -62,8 +63,8 @@ public class StateUtility {
     }
 
 
-    public static boolean isUuidOrMixin(QName propName) {
-        return QName.JCR_UUID.equals(propName) || QName.JCR_MIXINTYPES.equals(propName);
+    public static boolean isUuidOrMixin(Name propName) {
+        return NameConstants.JCR_UUID.equals(propName) || NameConstants.JCR_MIXINTYPES.equals(propName);
     }
 
     public static boolean isMovedState(NodeState state) {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/DefaultVersionManager.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/DefaultVersionManager.java
index c26fece..4fe074d 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/DefaultVersionManager.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/DefaultVersionManager.java
@@ -23,8 +23,8 @@ import javax.jcr.RepositoryException;
 import javax.jcr.UnsupportedRepositoryOperationException;
 import javax.jcr.version.VersionException;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 
@@ -58,11 +58,11 @@ public class DefaultVersionManager implements VersionManager {
         throw new UnsupportedRepositoryOperationException("Versioning ist not supported by this repository.");
     }
 
-    public void addVersionLabel(NodeState versionHistoryState, NodeState versionState, QName qLabel, boolean moveLabel) throws RepositoryException {
+    public void addVersionLabel(NodeState versionHistoryState, NodeState versionState, Name qLabel, boolean moveLabel) throws RepositoryException {
         throw new UnsupportedRepositoryOperationException("Versioning ist not supported by this repository.");
     }
 
-    public void removeVersionLabel(NodeState versionHistoryState, NodeState versionState, QName qLabel) throws RepositoryException {
+    public void removeVersionLabel(NodeState versionHistoryState, NodeState versionState, Name qLabel) throws RepositoryException {
         throw new UnsupportedRepositoryOperationException("Versioning ist not supported by this repository.");
     }
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionHistoryImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionHistoryImpl.java
index 4d74a6c..0ad5925 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionHistoryImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionHistoryImpl.java
@@ -26,11 +26,10 @@ import org.apache.jackrabbit.jcr2spi.LazyItemIterator;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 import org.apache.jackrabbit.jcr2spi.hierarchy.PropertyEntry;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.name.NameConstants;
 
 import javax.jcr.version.VersionHistory;
 import javax.jcr.version.Version;
@@ -65,7 +64,7 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
         this.vhEntry = (NodeEntry) state.getHierarchyEntry();
 
         // retrieve hierarchy entry of the jcr:versionLabels node
-        labelNodeEntry = vhEntry.getNodeEntry(QName.JCR_VERSIONLABELS, Path.INDEX_DEFAULT, true);
+        labelNodeEntry = vhEntry.getNodeEntry(NameConstants.JCR_VERSIONLABELS, Path.INDEX_DEFAULT, true);
         if (labelNodeEntry == null) {
             String msg = "Unexpected error: nt:versionHistory requires a mandatory, autocreated child node jcr:versionLabels.";
             log.error(msg);
@@ -82,7 +81,7 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      */
     public String getVersionableUUID() throws RepositoryException {
         checkStatus();
-        return getProperty(QName.JCR_VERSIONABLEUUID).getString();
+        return getProperty(NameConstants.JCR_VERSIONABLEUUID).getString();
     }
 
     /**
@@ -93,7 +92,7 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      */
     public Version getRootVersion() throws RepositoryException {
         checkStatus();
-        NodeEntry vEntry = vhEntry.getNodeEntry(QName.JCR_ROOTVERSION, Path.INDEX_DEFAULT, true);
+        NodeEntry vEntry = vhEntry.getNodeEntry(NameConstants.JCR_ROOTVERSION, Path.INDEX_DEFAULT, true);
         if (vEntry == null) {
             String msg = "Unexpected error: VersionHistory state does not contain a root version child node entry.";
             log.error(msg);
@@ -116,7 +115,7 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
         // all child-nodes except from jcr:versionLabels point to Versions.
         while (childIter.hasNext()) {
             NodeEntry entry = (NodeEntry) childIter.next();
-            if (!QName.JCR_VERSIONLABELS.equals(entry.getQName())) {
+            if (!NameConstants.JCR_VERSIONLABELS.equals(entry.getName())) {
                 versionEntries.add(entry);
             }
         }
@@ -160,7 +159,7 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      */
     public void addVersionLabel(String versionName, String label, boolean moveLabel) throws VersionException, RepositoryException {
         checkStatus();
-        QName qLabel = getQLabel(label);
+        Name qLabel = getQLabel(label);
         NodeState vState = getVersionState(versionName);
         // delegate to version manager that operates on workspace directely
         session.getVersionManager().addVersionLabel((NodeState) getItemState(), vState, qLabel, moveLabel);
@@ -175,7 +174,7 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      */
     public void removeVersionLabel(String label) throws VersionException, RepositoryException {
         checkStatus();
-        QName qLabel = getQLabel(label);
+        Name qLabel = getQLabel(label);
         Version version = getVersionByLabel(qLabel);
         NodeState vState = getVersionState(version.getName());
         // delegate to version manager that operates on workspace directely
@@ -191,8 +190,8 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      */
     public boolean hasVersionLabel(String label) throws RepositoryException {
         checkStatus();
-        QName l = getQLabel(label);
-        QName[] qLabels = getQLabels();
+        Name l = getQLabel(label);
+        Name[] qLabels = getQLabels();
         for (int i = 0; i < qLabels.length; i++) {
             if (qLabels[i].equals(l)) {
                 return true;
@@ -213,9 +212,9 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
         // check-status performed within checkValidVersion
         checkValidVersion(version);
         String vUUID = version.getUUID();
-        QName l = getQLabel(label);
+        Name l = getQLabel(label);
 
-        QName[] qLabels = getQLabels();
+        Name[] qLabels = getQLabels();
         for (int i = 0; i < qLabels.length; i++) {
             if (qLabels[i].equals(l)) {
                 String uuid = getVersionByLabel(qLabels[i]).getUUID();
@@ -233,16 +232,11 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      */
     public String[] getVersionLabels() throws RepositoryException {
         checkStatus();
-        QName[] qLabels = getQLabels();
+        Name[] qLabels = getQLabels();
         String[] labels = new String[qLabels.length];
 
         for (int i = 0; i < qLabels.length; i++) {
-            try {
-                labels[i] = NameFormat.format(qLabels[i], session.getNamespaceResolver());
-            } catch (NoPrefixDeclaredException e) {
-                // unexpected error. should not occur.
-                throw new RepositoryException(e);
-            }
+            labels[i] = session.getNameResolver().getJCRName(qLabels[i]);
         }
         return labels;
     }
@@ -261,16 +255,11 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
         String vUUID = version.getUUID();
 
         List vlabels = new ArrayList();
-        QName[] qLabels = getQLabels();
+        Name[] qLabels = getQLabels();
         for (int i = 0; i < qLabels.length; i++) {
             String uuid = getVersionByLabel(qLabels[i]).getUUID();
             if (vUUID.equals(uuid)) {
-                try {
-                    vlabels.add(NameFormat.format(qLabels[i], session.getNamespaceResolver()));
-                } catch (NoPrefixDeclaredException e) {
-                    // should never occur
-                    throw new RepositoryException("Unexpected error while accessing version label", e);
-                }
+                vlabels.add(session.getNameResolver().getJCRName(qLabels[i]));
             }
         }
         return (String[]) vlabels.toArray(new String[vlabels.size()]);
@@ -339,17 +328,17 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      *
      * @return
      */
-    private QName[] getQLabels() throws RepositoryException {
+    private Name[] getQLabels() throws RepositoryException {
         refreshEntry(labelNodeEntry);
-        List labelQNames = new ArrayList();
+        List labelNames = new ArrayList();
         for (Iterator it = labelNodeEntry.getPropertyEntries(); it.hasNext(); ) {
             PropertyEntry pe = (PropertyEntry) it.next();
-            if (! QName.JCR_PRIMARYTYPE.equals(pe.getQName()) &&
-                ! QName.JCR_MIXINTYPES.equals(pe.getQName())) {
-                labelQNames.add(pe.getQName());
+            if (! NameConstants.JCR_PRIMARYTYPE.equals(pe.getName()) &&
+                ! NameConstants.JCR_MIXINTYPES.equals(pe.getName())) {
+                labelNames.add(pe.getName());
             }
         }
-        return (QName[]) labelQNames.toArray(new QName[labelQNames.size()]);
+        return (Name[]) labelNames.toArray(new Name[labelNames.size()]);
     }
 
     /**
@@ -361,15 +350,15 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      */
     private NodeState getVersionState(String versionName) throws VersionException, RepositoryException {
         try {
-            QName vQName = NameFormat.parse(versionName, session.getNamespaceResolver());
+            Name vName = session.getNameResolver().getQName(versionName);
             refreshEntry(vhEntry);
-            NodeEntry vEntry = vhEntry.getNodeEntry(vQName, Path.INDEX_DEFAULT, true);
+            NodeEntry vEntry = vhEntry.getNodeEntry(vName, Path.INDEX_DEFAULT, true);
             if (vEntry == null) {
                 throw new VersionException("Version '" + versionName + "' does not exist in this version history.");
             } else {
                 return vEntry.getNodeState();
             }
-        } catch (NameException e) {
+        } catch (org.apache.jackrabbit.conversion.NameException e) {
             throw new RepositoryException(e);
         }
     }
@@ -381,7 +370,7 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      * @throws VersionException
      * @throws RepositoryException
      */
-    private Version getVersionByLabel(QName qLabel) throws VersionException, RepositoryException {
+    private Version getVersionByLabel(Name qLabel) throws VersionException, RepositoryException {
          refreshEntry(labelNodeEntry);
         // retrieve reference property value -> and retrieve referenced node
         PropertyEntry pEntry = labelNodeEntry.getPropertyEntry(qLabel, true);
@@ -398,9 +387,9 @@ public class VersionHistoryImpl extends NodeImpl implements VersionHistory {
      * @return
      * @throws RepositoryException
      */
-    private QName getQLabel(String label) throws RepositoryException {
+    private Name getQLabel(String label) throws RepositoryException {
         try {
-            return NameFormat.parse(label, session.getNamespaceResolver());
+            return session.getNameResolver().getQName(label);
         } catch (NameException e) {
             String error = "Invalid version label: " + e.getMessage();
             log.error(error);
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionImpl.java
index 451eb5d..fd78524 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionImpl.java
@@ -23,7 +23,8 @@ import org.apache.jackrabbit.jcr2spi.ItemManager;
 import org.apache.jackrabbit.jcr2spi.SessionImpl;
 import org.apache.jackrabbit.jcr2spi.ItemLifeCycleListener;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.name.NameConstants;
 
 import javax.jcr.version.Version;
 import javax.jcr.version.VersionHistory;
@@ -65,7 +66,7 @@ public class VersionImpl extends NodeImpl implements Version {
      * @see Version#getCreated()
      */
     public Calendar getCreated() throws RepositoryException {
-        return getProperty(QName.JCR_CREATED).getDate();
+        return getProperty(NameConstants.JCR_CREATED).getDate();
     }
 
     /**
@@ -75,7 +76,7 @@ public class VersionImpl extends NodeImpl implements Version {
      * @see Version#getSuccessors()
      */
     public Version[] getSuccessors() throws RepositoryException {
-        return getVersions(QName.JCR_SUCCESSORS);
+        return getVersions(NameConstants.JCR_SUCCESSORS);
     }
 
     /**
@@ -85,7 +86,7 @@ public class VersionImpl extends NodeImpl implements Version {
      * @see Version#getPredecessors()
      */
     public Version[] getPredecessors() throws RepositoryException {
-        return getVersions(QName.JCR_PREDECESSORS);
+        return getVersions(NameConstants.JCR_PREDECESSORS);
     }
 
     //---------------------------------------------------------------< Item >---
@@ -141,7 +142,7 @@ public class VersionImpl extends NodeImpl implements Version {
      * @param propertyName
      * @return
      */
-    private Version[] getVersions(QName propertyName) throws RepositoryException {
+    private Version[] getVersions(Name propertyName) throws RepositoryException {
         Version[] versions;
         Value[] values = getProperty(propertyName).getValues();
         if (values != null) {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionManager.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionManager.java
index 98c3cfa..b7f59fd 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionManager.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionManager.java
@@ -16,8 +16,8 @@
  */
 package org.apache.jackrabbit.jcr2spi.version;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.jcr2spi.state.NodeState;
 import org.apache.jackrabbit.jcr2spi.hierarchy.NodeEntry;
 
@@ -97,7 +97,7 @@ public interface VersionManager {
      * @throws RepositoryException
      * @see javax.jcr.version.VersionHistory#addVersionLabel(String, String, boolean)
      */
-    public void addVersionLabel(NodeState versionHistoryState, NodeState versionState, QName qLabel, boolean moveLabel) throws VersionException, RepositoryException;
+    public void addVersionLabel(NodeState versionHistoryState, NodeState versionState, Name qLabel, boolean moveLabel) throws VersionException, RepositoryException;
 
     /**
      * @param versionHistoryState
@@ -107,7 +107,7 @@ public interface VersionManager {
      * @throws RepositoryException
      * @see javax.jcr.version.VersionHistory#removeVersionLabel(String)
      */
-    public void removeVersionLabel(NodeState versionHistoryState, NodeState versionState, QName qLabel) throws VersionException, RepositoryException;
+    public void removeVersionLabel(NodeState versionHistoryState, NodeState versionState, Name qLabel) throws VersionException, RepositoryException;
 
     /**
      * @param nodeState
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionManagerImpl.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionManagerImpl.java
index b3e0241..5ba7ff0 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionManagerImpl.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionManagerImpl.java
@@ -37,10 +37,11 @@ import javax.jcr.RepositoryException;
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.version.VersionException;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.QValue;
+import org.apache.jackrabbit.name.NameConstants;
 
 import java.util.Iterator;
 
@@ -87,7 +88,7 @@ public class VersionManagerImpl implements VersionManager {
             // entry might even not be accessible, the check may not detect
             // a checked-in parent. ok, as long as the 'server' finds out upon
             // save or upon executing the workspace operation.
-            while (!nodeEntry.hasPropertyEntry(QName.JCR_ISCHECKEDOUT)) {
+            while (!nodeEntry.hasPropertyEntry(NameConstants.JCR_ISCHECKEDOUT)) {
                 NodeEntry parent = nodeEntry.getParent();
                 if (parent == null) {
                     // reached root state without finding a jcr:isCheckedOut property
@@ -95,7 +96,7 @@ public class VersionManagerImpl implements VersionManager {
                 }
                 nodeEntry = parent;
             }
-            PropertyState propState = nodeEntry.getPropertyEntry(QName.JCR_ISCHECKEDOUT).getPropertyState();
+            PropertyState propState = nodeEntry.getPropertyEntry(NameConstants.JCR_ISCHECKEDOUT).getPropertyState();
             Boolean b = Boolean.valueOf(propState.getValue().getString());
             return b.booleanValue();
         } catch (ItemNotFoundException e) {
@@ -117,12 +118,12 @@ public class VersionManagerImpl implements VersionManager {
         workspaceManager.execute(op);
     }
 
-    public void addVersionLabel(NodeState versionHistoryState, NodeState versionState, QName qLabel, boolean moveLabel) throws RepositoryException {
+    public void addVersionLabel(NodeState versionHistoryState, NodeState versionState, Name qLabel, boolean moveLabel) throws RepositoryException {
         Operation op = AddLabel.create(versionHistoryState, versionState, qLabel, moveLabel);
         workspaceManager.execute(op);
     }
 
-    public void removeVersionLabel(NodeState versionHistoryState, NodeState versionState, QName qLabel) throws RepositoryException {
+    public void removeVersionLabel(NodeState versionHistoryState, NodeState versionState, Name qLabel) throws RepositoryException {
         Operation op = RemoveLabel.create(versionHistoryState, versionState, qLabel);
         workspaceManager.execute(op);
     }
@@ -147,7 +148,7 @@ public class VersionManagerImpl implements VersionManager {
                                      boolean done) throws RepositoryException {
         NodeId vId = versionState.getNodeId();
 
-        PropertyState mergeFailedState = nodeState.getPropertyState(QName.JCR_MERGEFAILED);
+        PropertyState mergeFailedState = nodeState.getPropertyState(NameConstants.JCR_MERGEFAILED);
         QValue[] vs = mergeFailedState.getValues();
 
         NodeId[] mergeFailedIds = new NodeId[vs.length - 1];
@@ -161,7 +162,7 @@ public class VersionManagerImpl implements VersionManager {
             // part of 'jcr:mergefailed' any more
         }
 
-        PropertyState predecessorState = nodeState.getPropertyState(QName.JCR_PREDECESSORS);
+        PropertyState predecessorState = nodeState.getPropertyState(NameConstants.JCR_PREDECESSORS);
         vs = predecessorState.getValues();
 
         int noOfPredecessors = (done) ? vs.length + 1 : vs.length;
@@ -180,8 +181,8 @@ public class VersionManagerImpl implements VersionManager {
     }
 
     public NodeEntry getVersionableNodeEntry(NodeState versionState) throws RepositoryException {
-        NodeState ns = versionState.getChildNodeState(QName.JCR_FROZENNODE, Path.INDEX_DEFAULT);
-        PropertyState ps = ns.getPropertyState(QName.JCR_FROZENUUID);
+        NodeState ns = versionState.getChildNodeState(NameConstants.JCR_FROZENNODE, Path.INDEX_DEFAULT);
+        PropertyState ps = ns.getPropertyState(NameConstants.JCR_FROZENUUID);
         String uniqueID = ps.getValue().getString();
 
         NodeId versionableId = workspaceManager.getIdFactory().createNodeId(uniqueID);
@@ -189,7 +190,7 @@ public class VersionManagerImpl implements VersionManager {
     }
 
     public NodeEntry getVersionHistoryEntry(NodeState versionableState) throws RepositoryException {
-        PropertyState ps = versionableState.getPropertyState(QName.JCR_VERSIONHISTORY);
+        PropertyState ps = versionableState.getPropertyState(NameConstants.JCR_VERSIONHISTORY);
         String uniqueID = ps.getValue().getString();
         NodeId vhId = workspaceManager.getIdFactory().createNodeId(uniqueID);
         return (NodeEntry) workspaceManager.getHierarchyManager().getHierarchyEntry(vhId);
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/AbstractSAXEventGenerator.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/AbstractSAXEventGenerator.java
index 9111f82..6cfa68d 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/AbstractSAXEventGenerator.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/AbstractSAXEventGenerator.java
@@ -16,11 +16,13 @@
  */
 package org.apache.jackrabbit.jcr2spi.xml;
 
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.SessionNamespaceResolver;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.namespace.SessionNamespaceResolver;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.name.NameFactoryImpl;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.conversion.ParsingNameResolver;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
@@ -63,9 +65,9 @@ abstract class AbstractSAXEventGenerator {
      */
     protected final Session session;
     /**
-     * the session's namespace resolver
+     * the name resolver
      */
-    protected final NamespaceResolver nsResolver;
+    protected final NameResolver nameResolver;
 
     /**
      * the content handler to feed the SAX events to
@@ -123,7 +125,8 @@ abstract class AbstractSAXEventGenerator {
             throws RepositoryException {
         startNode = node;
         session = node.getSession();
-        nsResolver = new SessionNamespaceResolver(session);
+        NamespaceResolver nsResolver = new SessionNamespaceResolver(session);
+        nameResolver = new ParsingNameResolver(NameFactoryImpl.getInstance(), nsResolver);
 
         this.contentHandler = contentHandler;
         this.skipBinary = skipBinary;
@@ -133,19 +136,12 @@ abstract class AbstractSAXEventGenerator {
 
         // resolve the names of some wellknown properties
         // allowing for session-local prefix mappings
-        try {
-            jcrPrimaryType = NameFormat.format(QName.JCR_PRIMARYTYPE, nsResolver);
-            jcrMixinTypes = NameFormat.format(QName.JCR_MIXINTYPES, nsResolver);
-            jcrUUID = NameFormat.format(QName.JCR_UUID, nsResolver);
-            jcrRoot = NameFormat.format(QName.JCR_ROOT, nsResolver);
-            jcrXMLText = NameFormat.format(QName.JCR_XMLTEXT, nsResolver);
-            jcrXMLCharacters = NameFormat.format(QName.JCR_XMLCHARACTERS, nsResolver);
-        } catch (NameException e) {
-            // should never get here...
-            String msg = "internal error: failed to resolve namespace mappings";
-            log.error(msg, e);
-            throw new RepositoryException(msg, e);
-        }
+        jcrPrimaryType = nameResolver.getJCRName(NameConstants.JCR_PRIMARYTYPE);
+        jcrMixinTypes = nameResolver.getJCRName(NameConstants.JCR_MIXINTYPES);
+        jcrUUID = nameResolver.getJCRName(NameConstants.JCR_UUID);
+        jcrRoot = nameResolver.getJCRName(NameConstants.JCR_ROOT);
+        jcrXMLText = nameResolver.getJCRName(NameConstants.JCR_XMLTEXT);
+        jcrXMLCharacters = nameResolver.getJCRName(NameConstants.JCR_XMLCHARACTERS);
     }
 
     /**
@@ -178,7 +174,7 @@ abstract class AbstractSAXEventGenerator {
         String[] prefixes = session.getNamespacePrefixes();
         for (int i = 0; i < prefixes.length; i++) {
             String prefix = prefixes[i];
-            if (QName.NS_XML_PREFIX.equals(prefix)) {
+            if (Name.NS_XML_PREFIX.equals(prefix)) {
                 // skip 'xml' prefix as this would be an illegal namespace declaration
                 continue;
             }
@@ -197,7 +193,7 @@ abstract class AbstractSAXEventGenerator {
         String[] prefixes = session.getNamespacePrefixes();
         for (int i = 0; i < prefixes.length; i++) {
             String prefix = prefixes[i];
-            if (QName.NS_XML_PREFIX.equals(prefix)) {
+            if (Name.NS_XML_PREFIX.equals(prefix)) {
                 // skip 'xml' prefix as this would be an illegal namespace declaration
                 continue;
             }
@@ -225,7 +221,7 @@ abstract class AbstractSAXEventGenerator {
             String prefix = prefixes[i];
 
             if (prefix.length() > 0
-                    && !QName.NS_XML_PREFIX.equals(prefix)) {
+                    && !Name.NS_XML_PREFIX.equals(prefix)) {
                 String uri = session.getNamespaceURI(prefix);
 
                 // get the matching namespace from previous declarations
@@ -234,9 +230,9 @@ abstract class AbstractSAXEventGenerator {
                 if (!uri.equals(mappedToNs)) {
                     // when not the same, add a declaration
                     attributes.addAttribute(
-                        QName.NS_XMLNS_URI,
+                        Name.NS_XMLNS_URI,
                         prefix,
-                        QName.NS_XMLNS_PREFIX + ":" + prefix,
+                        Name.NS_XMLNS_PREFIX + ":" + prefix,
                         "CDATA",
                         uri);
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/DocViewImportHandler.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/DocViewImportHandler.java
index 3cb2055..5aa0bc4 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/DocViewImportHandler.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/DocViewImportHandler.java
@@ -16,11 +16,12 @@
  */
 package org.apache.jackrabbit.jcr2spi.xml;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.conversion.NameResolver;
 import org.apache.jackrabbit.util.ISO9075;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.slf4j.LoggerFactory;
@@ -28,6 +29,7 @@ import org.slf4j.Logger;
 
 import javax.jcr.PropertyType;
 import javax.jcr.RepositoryException;
+import javax.jcr.NamespaceException;
 
 import java.io.IOException;
 import java.io.Reader;
@@ -42,6 +44,7 @@ class DocViewImportHandler extends TargetImportHandler {
 
     private static Logger log = LoggerFactory.getLogger(DocViewImportHandler.class);
 
+    private final NameFactory nameFactory;
     /**
      * stack of NodeInfo instances; an instance is pushed onto the stack
      * in the startElement method and is popped from the stack in the
@@ -57,8 +60,10 @@ class DocViewImportHandler extends TargetImportHandler {
      * @param importer
      * @param nsContext
      */
-    DocViewImportHandler(Importer importer, NamespaceResolver nsContext) {
-        super(importer, nsContext);
+    DocViewImportHandler(Importer importer, NamespaceResolver nsContext,
+                         NameResolver nameResolver, NameFactory nameFactory) {
+        super(importer, nsContext, nameResolver);
+        this.nameFactory = nameFactory;
     }
 
     /**
@@ -125,12 +130,12 @@ class DocViewImportHandler extends TargetImportHandler {
                 }
 
                 Importer.NodeInfo node =
-                        new Importer.NodeInfo(QName.JCR_XMLTEXT, null, null, null);
+                        new Importer.NodeInfo(NameConstants.JCR_XMLTEXT, null, null, null);
                 Importer.TextValue[] values =
                         new Importer.TextValue[]{textHandler};
                 ArrayList props = new ArrayList();
                 Importer.PropInfo prop =
-                        new Importer.PropInfo(QName.JCR_XMLCHARACTERS, PropertyType.STRING, values);
+                        new Importer.PropInfo(NameConstants.JCR_XMLCHARACTERS, PropertyType.STRING, values);
                 props.add(prop);
                 // call Importer
                 importer.startNode(node, props, nsContext);
@@ -164,42 +169,41 @@ class DocViewImportHandler extends TargetImportHandler {
         processCharacters();
 
         try {
-            QName nodeName = new QName(namespaceURI, localName);
-            // decode node name
-            nodeName = ISO9075.decode(nodeName);
+            String dcdLocalName = ISO9075.decode(localName);
+            Name nodeName = nameFactory.create(namespaceURI, dcdLocalName);
 
             // properties
             String uuid = null;
-            QName nodeTypeName = null;
-            QName[] mixinTypes = null;
+            Name nodeTypeName = null;
+            Name[] mixinTypes = null;
 
             ArrayList props = new ArrayList(atts.getLength());
             for (int i = 0; i < atts.getLength(); i++) {
-                if (atts.getURI(i).equals(QName.NS_XMLNS_URI)) {
+                if (atts.getURI(i).equals(Name.NS_XMLNS_URI)) {
                     // skip namespace declarations reported as attributes
                     // see http://issues.apache.org/jira/browse/JCR-620#action_12448164
                     continue;
                 }
-                QName propName = new QName(atts.getURI(i), atts.getLocalName(i));
-                // decode property name
-                propName = ISO9075.decode(propName);
+
+                dcdLocalName = ISO9075.decode(atts.getLocalName(i));
+                Name propName = nameFactory.create(atts.getURI(i), dcdLocalName);
 
                 // attribute value
                 String attrValue = atts.getValue(i);
-                if (propName.equals(QName.JCR_PRIMARYTYPE)) {
+                if (propName.equals(NameConstants.JCR_PRIMARYTYPE)) {
                     // jcr:primaryType
                     if (attrValue.length() > 0) {
                         try {
-                            nodeTypeName = NameFormat.parse(attrValue, nsContext);
-                        } catch (NameException ne) {
+                            nodeTypeName = nameResolver.getQName(attrValue);
+                        } catch (org.apache.jackrabbit.conversion.NameException ne) {
                             throw new SAXException("illegal jcr:primaryType value: "
                                     + attrValue, ne);
                         }
                     }
-                } else if (propName.equals(QName.JCR_MIXINTYPES)) {
+                } else if (propName.equals(NameConstants.JCR_MIXINTYPES)) {
                     // jcr:mixinTypes
                     mixinTypes = parseNames(attrValue);
-                } else if (propName.equals(QName.JCR_UUID)) {
+                } else if (propName.equals(NameConstants.JCR_UUID)) {
                     // jcr:uuid
                     if (attrValue.length() > 0) {
                         uuid = attrValue;
@@ -230,20 +234,22 @@ class DocViewImportHandler extends TargetImportHandler {
      * Parses the given string as a list of JCR names. Any whitespace sequence
      * is supported as a names separator instead of just a single space to
      * be more liberal in what we accept. The current namespace context is
-     * used to convert the prefixed name strings to QNames.
+     * used to convert the prefixed name strings to Names.
      *
      * @param value string value
      * @return the parsed names
      * @throws SAXException if an invalid name was encountered
      */
-    private QName[] parseNames(String value) throws SAXException {
+    private Name[] parseNames(String value) throws SAXException {
         String[] names = value.split("\\p{Space}+");
-        QName[] qnames = new QName[names.length];
+        Name[] qnames = new Name[names.length];
         for (int i = 0; i < names.length; i++) {
             try {
-                qnames[i] = NameFormat.parse(names[i], nsContext);
-            } catch (NameException ne) {
+                qnames[i] = nameResolver.getQName(names[i]);
+            } catch (org.apache.jackrabbit.conversion.NameException ne) {
                 throw new SAXException("Invalid name: " + names[i], ne);
+            } catch (NamespaceException e) {
+                throw new SAXException("Invalid name: " + names[i], e);
             }
         }
         return qnames;
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/DocViewSAXEventGenerator.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/DocViewSAXEventGenerator.java
index e89d022..ef05e3b 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/DocViewSAXEventGenerator.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/DocViewSAXEventGenerator.java
@@ -16,9 +16,8 @@
  */
 package org.apache.jackrabbit.jcr2spi.xml;
 
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.util.ISO9075;
 import org.apache.jackrabbit.value.ValueHelper;
 import org.xml.sax.ContentHandler;
@@ -69,9 +68,9 @@ public class DocViewSAXEventGenerator extends AbstractSAXEventGenerator {
         props = new ArrayList();
     }
 
-    private QName getQName(String rawName) throws RepositoryException {
+    private Name getName(String rawName) throws RepositoryException {
         try {
-            return NameFormat.parse(rawName, nsResolver);
+            return nameResolver.getQName(rawName);
         } catch (NameException e) {
             // should never get here...
             String msg = "internal error: failed to resolve namespace mappings";
@@ -154,7 +153,7 @@ public class DocViewSAXEventGenerator extends AbstractSAXEventGenerator {
 
                 // attribute name (encode property name to make sure it's a valid xml name)
                 String attrName = ISO9075.encode(propName);
-                QName qName = getQName(attrName);
+                Name qName = getName(attrName);
 
                 // attribute value
                 if (prop.getType() == PropertyType.BINARY && skipBinary) {
@@ -172,7 +171,7 @@ public class DocViewSAXEventGenerator extends AbstractSAXEventGenerator {
             }
 
             // start element (node)
-            QName qName = getQName(elemName);
+            Name qName = getName(elemName);
             contentHandler.startElement(qName.getNamespaceURI(),
                     qName.getLocalName(), elemName, attrs);
         }
@@ -200,7 +199,7 @@ public class DocViewSAXEventGenerator extends AbstractSAXEventGenerator {
         }
 
         // end element (node)
-        QName qName = getQName(elemName);
+        Name qName = getName(elemName);
         contentHandler.endElement(qName.getNamespaceURI(), qName.getLocalName(),
                 elemName);
     }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/ImportHandler.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/ImportHandler.java
index a47102c..eaab0c9 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/ImportHandler.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/ImportHandler.java
@@ -16,12 +16,14 @@
  */
 package org.apache.jackrabbit.jcr2spi.xml;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.AbstractNamespaceResolver;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.namespace.AbstractNamespaceResolver;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.conversion.ParsingNameResolver;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
-import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
@@ -56,13 +58,14 @@ public class ImportHandler extends DefaultHandler {
     private final Importer importer;
     private final NamespaceRegistry nsReg;
     private final NamespaceResolver nsResolver;
+    private final NameFactory nameFactory;
 
-    private Locator locator;
     private ContentHandler targetHandler;
     private boolean systemViewXML;
     private boolean initialized;
 
     private final NamespaceContext nsContext;
+    private final NameResolver nameResolver;
 
     /**
      * this flag is used to determine whether a namespace context needs to be
@@ -74,12 +77,14 @@ public class ImportHandler extends DefaultHandler {
     protected boolean nsContextStarted;
 
     public ImportHandler(Importer importer, NamespaceResolver nsResolver,
-                         NamespaceRegistry nsReg) {
+                         NamespaceRegistry nsReg, NameFactory nameFactory) {
         this.importer = importer;
         this.nsResolver = nsResolver;
         this.nsReg = nsReg;
+        this.nameFactory = nameFactory;
 
         nsContext = new NamespaceContext();
+        nameResolver = new ParsingNameResolver(nameFactory, nsContext);
     }
 
     //---------------------------------------------------------< ErrorHandler >
@@ -220,12 +225,12 @@ public class ImportHandler extends DefaultHandler {
         if (!initialized) {
             // the namespace of the first element determines the type of XML
             // (system view/document view)
-            systemViewXML = QName.NS_SV_URI.equals(namespaceURI);
+            systemViewXML = Name.NS_SV_URI.equals(namespaceURI);
 
             if (systemViewXML) {
-                targetHandler = new SysViewImportHandler(importer, nsContext);
+                targetHandler = new SysViewImportHandler(importer, nsContext, nameResolver);
             } else {
-                targetHandler = new DocViewImportHandler(importer, nsContext);
+                targetHandler = new DocViewImportHandler(importer, nsContext, nameResolver, nameFactory);
             }
             targetHandler.startDocument();
             initialized = true;
@@ -255,13 +260,6 @@ public class ImportHandler extends DefaultHandler {
         targetHandler.endElement(namespaceURI, localName, qName);
     }
 
-    /**
-     * {@inheritDoc}
-     */
-    public void setDocumentLocator(Locator locator) {
-        this.locator = locator;
-    }
-
     //--------------------------------------------------------< inner classes >
     /**
      * <code>NamespaceContext</code> supports scoped namespace declarations.
@@ -293,7 +291,7 @@ public class ImportHandler extends DefaultHandler {
         }
 
         boolean declarePrefix(String prefix, String uri) {
-            if (QName.NS_DEFAULT_URI.equals(uri)) {
+            if (Name.NS_DEFAULT_URI.equals(uri)) {
                 uri = DUMMY_DEFAULT_URI;
             }
             return nsContext.declarePrefix(prefix, uri);
@@ -308,7 +306,7 @@ public class ImportHandler extends DefaultHandler {
             if (uri == null) {
                 throw new NamespaceException("unknown prefix");
             } else if (DUMMY_DEFAULT_URI.equals(uri)) {
-                return QName.NS_DEFAULT_URI;
+                return Name.NS_DEFAULT_URI;
             } else {
                 return uri;
             }
@@ -318,7 +316,7 @@ public class ImportHandler extends DefaultHandler {
          * {@inheritDoc}
          */
         public String getPrefix(String uri) throws NamespaceException {
-            if (QName.NS_DEFAULT_URI.equals(uri)) {
+            if (Name.NS_DEFAULT_URI.equals(uri)) {
                 uri = DUMMY_DEFAULT_URI;
             }
             String prefix = nsContext.getPrefix(uri);
@@ -328,8 +326,8 @@ public class ImportHandler extends DefaultHandler {
                  * (default) prefix; we have to do a reverse-lookup to check
                  * whether it's the current default namespace
                  */
-                if (uri.equals(nsContext.getURI(QName.NS_EMPTY_PREFIX))) {
-                    return QName.NS_EMPTY_PREFIX;
+                if (uri.equals(nsContext.getURI(Name.NS_EMPTY_PREFIX))) {
+                    return Name.NS_EMPTY_PREFIX;
                 }
                 throw new NamespaceException("unknown uri");
             }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/Importer.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/Importer.java
index 47717d7..53e3a3f 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/Importer.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/Importer.java
@@ -16,8 +16,8 @@
  */
 package org.apache.jackrabbit.jcr2spi.xml;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
 
 import javax.jcr.RepositoryException;
 
@@ -57,27 +57,27 @@ public interface Importer {
 
     //--------------------------------------------------------< inner classes >
     static class NodeInfo {
-        private final QName name;
-        private final QName nodeTypeName;
-        private final QName[] mixinNames;
+        private final Name name;
+        private final Name nodeTypeName;
+        private final Name[] mixinNames;
         private String uuid;
 
-        public NodeInfo(QName name, QName nodeTypeName, QName[] mixinNames, String uuid) {
+        public NodeInfo(Name name, Name nodeTypeName, Name[] mixinNames, String uuid) {
             this.name = name;
             this.nodeTypeName = nodeTypeName;
             this.mixinNames = mixinNames;
             this.uuid = uuid;
         }
 
-        public QName getName() {
+        public Name getName() {
             return name;
         }
 
-        public QName getNodeTypeName() {
+        public Name getNodeTypeName() {
             return nodeTypeName;
         }
 
-        public QName[] getMixinNames() {
+        public Name[] getMixinNames() {
             return mixinNames;
         }
 
@@ -91,17 +91,17 @@ public interface Importer {
     }
 
     static class PropInfo {
-        private final QName name;
+        private final Name name;
         private final int type;
         private final TextValue[] values;
 
-        public PropInfo(QName name, int type, TextValue[] values) {
+        public PropInfo(Name name, int type, TextValue[] values) {
             this.name = name;
             this.type = type;
             this.values = values;
         }
 
-        public QName getName() {
+        public Name getName() {
             return name;
         }
 
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SessionImporter.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SessionImporter.java
index fb81217..5c1146d 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SessionImporter.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SessionImporter.java
@@ -30,7 +30,7 @@ import org.apache.jackrabbit.jcr2spi.hierarchy.HierarchyEntry;
 import org.apache.jackrabbit.jcr2spi.util.ReferenceChangeTracker;
 import org.apache.jackrabbit.jcr2spi.util.LogUtil;
 import org.apache.jackrabbit.jcr2spi.nodetype.EffectiveNodeType;
-import org.apache.jackrabbit.jcr2spi.nodetype.NodeTypeConflictException;
+import org.apache.jackrabbit.nodetype.NodeTypeConflictException;
 import org.apache.jackrabbit.jcr2spi.nodetype.EffectiveNodeTypeProvider;
 import org.apache.jackrabbit.jcr2spi.operation.AddNode;
 import org.apache.jackrabbit.jcr2spi.operation.Remove;
@@ -38,8 +38,7 @@ import org.apache.jackrabbit.jcr2spi.operation.AddProperty;
 import org.apache.jackrabbit.jcr2spi.operation.SetPropertyValue;
 import org.apache.jackrabbit.jcr2spi.operation.SetMixin;
 import org.apache.jackrabbit.jcr2spi.operation.Operation;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.util.Base64;
 import org.apache.jackrabbit.util.TransientFileFactory;
 import org.slf4j.LoggerFactory;
@@ -56,8 +55,8 @@ import javax.jcr.Value;
 import javax.jcr.lock.LockException;
 import javax.jcr.version.VersionException;
 import javax.jcr.nodetype.ConstraintViolationException;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.NodeId;
@@ -65,6 +64,7 @@ import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.value.ValueHelper;
 import org.apache.jackrabbit.value.ValueFormat;
 import org.apache.jackrabbit.uuid.UUID;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
@@ -130,7 +130,7 @@ public class SessionImporter implements Importer, SessionListener {
         try {
             ItemState itemState = session.getHierarchyManager().getItemState(parentPath);
             if (!itemState.isNode()) {
-                throw new PathNotFoundException(LogUtil.safeGetJCRPath(parentPath, session.getNamespaceResolver()));
+                throw new PathNotFoundException(LogUtil.safeGetJCRPath(parentPath, session.getPathResolver()));
             }
             importTarget = (NodeState) itemState;
 
@@ -142,7 +142,7 @@ public class SessionImporter implements Importer, SessionListener {
             parents = new Stack();
             parents.push(importTarget);
         } catch (ItemNotFoundException e) {
-            throw new PathNotFoundException(LogUtil.safeGetJCRPath(parentPath, session.getNamespaceResolver()));
+            throw new PathNotFoundException(LogUtil.safeGetJCRPath(parentPath, session.getPathResolver()));
         }
     }
 
@@ -190,14 +190,14 @@ public class SessionImporter implements Importer, SessionListener {
                    if (def.isProtected() && entExisting.includesNodeType(nodeInfo.getNodeTypeName())) {
                        // skip protected node
                        parents.push(null); // push null onto stack for skipped node
-                       log.debug("skipping protected node " + LogUtil.safeGetJCRPath(existing, session.getNamespaceResolver()));
+                       log.debug("skipping protected node " + LogUtil.safeGetJCRPath(existing, session.getPathResolver()));
                        return;
                    }
                    if (def.isAutoCreated() && entExisting.includesNodeType(nodeInfo.getNodeTypeName())) {
                        // this node has already been auto-created, no need to create it
                        nodeState = existing;
                    } else {
-                       throw new ItemExistsException(LogUtil.safeGetJCRPath(existing, session.getNamespaceResolver()));
+                       throw new ItemExistsException(LogUtil.safeGetJCRPath(existing, session.getPathResolver()));
                    }
                }
            } catch (ItemNotFoundException e) {
@@ -347,17 +347,10 @@ public class SessionImporter implements Importer, SessionListener {
                 // make sure conflicting node is not importTarget or an ancestor thereof
                 Path p0 = importTarget.getQPath();
                 Path p1 = conflicting.getPath();
-                try {
-                    if (p1.equals(p0) || p1.isAncestorOf(p0)) {
-                        msg = "cannot remove ancestor node";
-                        log.debug(msg);
-                        throw new ConstraintViolationException(msg);
-                    }
-                } catch (MalformedPathException e) {
-                    // should never get here...
-                    msg = "internal error: failed to determine degree of relationship";
-                    log.error(msg, e);
-                    throw new RepositoryException(msg, e);
+                if (p1.equals(p0) || p1.isAncestorOf(p0)) {
+                    msg = "cannot remove ancestor node";
+                    log.debug(msg);
+                    throw new ConstraintViolationException(msg);
                 }
                 // do remove conflicting (recursive) including validation check
                 try {
@@ -419,9 +412,9 @@ public class SessionImporter implements Importer, SessionListener {
                 // assume this property has been imported as well;
                 // rename conflicting property
                 // TODO: use better reversible escaping scheme to create unique name
-                QName newName = new QName(nodeInfo.getName().getNamespaceURI(), nodeInfo.getName().getLocalName() + "_");
+                Name newName = session.getNameFactory().create(nodeInfo.getName().getNamespaceURI(), nodeInfo.getName().getLocalName() + "_");
                 if (parent.hasPropertyName(newName)) {
-                    newName = new QName(newName.getNamespaceURI(), newName.getLocalName() + "_");
+                    newName = session.getNameFactory().create(newName.getNamespaceURI(), newName.getLocalName() + "_");
                 }
                 // since name changes, need to find new applicable definition
                 QPropertyDefinition propDef;
@@ -452,7 +445,7 @@ public class SessionImporter implements Importer, SessionListener {
             log.debug("Skipping protected nodeState (" + nodeInfo.getName() + ")");
             return null;
         } else {
-            QName ntName = nodeInfo.getNodeTypeName();
+            Name ntName = nodeInfo.getNodeTypeName();
             if (ntName == null) {
                 // use default node type
                 ntName = def.getDefaultPrimaryType();
@@ -485,8 +478,8 @@ public class SessionImporter implements Importer, SessionListener {
      * @throws RepositoryException
      * @throws ConstraintViolationException
      */
-    private void importProperty(PropInfo pi, NodeState parentState, NamespaceResolver nsResolver) throws RepositoryException, ConstraintViolationException {
-        QName propName = pi.getName();
+    private void importProperty(PropInfo pi, NodeState parentState, org.apache.jackrabbit.namespace.NamespaceResolver nsResolver) throws RepositoryException, ConstraintViolationException {
+        Name propName = pi.getName();
         TextValue[] tva = pi.getValues();
         int infoType = pi.getType();
 
@@ -502,7 +495,7 @@ public class SessionImporter implements Importer, SessionListener {
                 def = existing.getDefinition();
                 if (def.isProtected()) {
                     // skip protected property
-                    log.debug("skipping protected property " + LogUtil.safeGetJCRPath(existing, session.getNamespaceResolver()));
+                    log.debug("skipping protected property " + LogUtil.safeGetJCRPath(existing, session.getPathResolver()));
                     return;
                 }
                 if (def.isAutoCreated()
@@ -511,7 +504,7 @@ public class SessionImporter implements Importer, SessionListener {
                     // this property has already been auto-created, no need to create it
                     propState = existing;
                 } else {
-                    throw new ItemExistsException(LogUtil.safeGetJCRPath(existing, session.getNamespaceResolver()));
+                    throw new ItemExistsException(LogUtil.safeGetJCRPath(existing, session.getPathResolver()));
                 }
             } catch (ItemNotFoundException e) {
                 // property apperently doesn't exist any more
@@ -574,7 +567,7 @@ public class SessionImporter implements Importer, SessionListener {
      * @return
      * @throws RepositoryException
      */
-    private QValue[] getPropertyValues(PropInfo propertyInfo, int targetType, boolean isMultiple, NamespaceResolver nsResolver) throws RepositoryException {
+    private QValue[] getPropertyValues(PropInfo propertyInfo, int targetType, boolean isMultiple, org.apache.jackrabbit.namespace.NamespaceResolver nsResolver) throws RepositoryException {
         TextValue[] tva = propertyInfo.getValues();
         // check multi-valued characteristic
         if ((tva.length == 0 || tva.length > 1) && !isMultiple) {
@@ -596,7 +589,7 @@ public class SessionImporter implements Importer, SessionListener {
      * @return
      * @throws RepositoryException
      */
-    private QValue buildQValue(TextValue tv, int targetType, NamespaceResolver nsResolver) throws RepositoryException {
+    private QValue buildQValue(TextValue tv, int targetType, org.apache.jackrabbit.namespace.NamespaceResolver nsResolver) throws RepositoryException {
         QValue iv;
         try {
             switch (targetType) {
@@ -628,7 +621,7 @@ public class SessionImporter implements Importer, SessionListener {
                 default:
                     // build iv using namespace context of xml document
                     Value v = ValueHelper.convert(tv.retrieve(), targetType, session.getValueFactory());
-                    iv = ValueFormat.getQValue(v, nsResolver, session.getQValueFactory());
+                    iv = ValueFormat.getQValue(v, session.getNamePathResolver(), session.getQValueFactory());
                     break;
             }
             return iv;
@@ -650,14 +643,14 @@ public class SessionImporter implements Importer, SessionListener {
         List l = new ArrayList();
         l.add(nodeInfo.getNodeTypeName());
         l.addAll(Arrays.asList(nodeInfo.getMixinNames()));
-        if (l.contains(QName.MIX_REFERENCEABLE)) {
+        if (l.contains(NameConstants.MIX_REFERENCEABLE)) {
             // shortcut
             return;
         }
-        QName[] ntNames = (QName[]) l.toArray(new QName[l.size()]);
+        Name[] ntNames = (Name[]) l.toArray(new Name[l.size()]);
         try {
             EffectiveNodeType ent = session.getEffectiveNodeTypeProvider().getEffectiveNodeType(ntNames);
-            if (!ent.includesNodeType(QName.MIX_REFERENCEABLE)) {
+            if (!ent.includesNodeType(NameConstants.MIX_REFERENCEABLE)) {
                 throw new ConstraintViolationException("XML defines jcr:uuid without defining import node to be referenceable.");
             }
         } catch (NodeTypeConflictException e) {
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SysViewImportHandler.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SysViewImportHandler.java
index 4dd9efe..846ab3d 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SysViewImportHandler.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SysViewImportHandler.java
@@ -16,17 +16,17 @@
  */
 package org.apache.jackrabbit.jcr2spi.xml;
 
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.UnknownPrefixException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NameResolver;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 import javax.jcr.InvalidSerializedDataException;
 import javax.jcr.PropertyType;
 import javax.jcr.RepositoryException;
+import javax.jcr.NamespaceException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
@@ -48,7 +48,7 @@ class SysViewImportHandler extends TargetImportHandler {
     /**
      * fields used temporarily while processing sv:property and sv:value elements
      */
-    private QName currentPropName;
+    private Name currentPropName;
     private int currentPropType = PropertyType.UNDEFINED;
     // list of AppendableValue objects
     private ArrayList currentPropValues = new ArrayList();
@@ -60,8 +60,8 @@ class SysViewImportHandler extends TargetImportHandler {
      * @param importer
      * @param nsContext
      */
-    SysViewImportHandler(Importer importer, NamespaceResolver nsContext) {
-        super(importer, nsContext);
+    SysViewImportHandler(Importer importer, org.apache.jackrabbit.namespace.NamespaceResolver nsContext, NameResolver nameResolver) {
+        super(importer, nsContext, nameResolver);
     }
 
     private void processNode(ImportState state, boolean start, boolean end)
@@ -69,9 +69,9 @@ class SysViewImportHandler extends TargetImportHandler {
         if (!start && !end) {
             return;
         }
-        QName[] mixins = null;
+        Name[] mixins = null;
         if (state.mixinNames != null) {
-            mixins = (QName[]) state.mixinNames.toArray(new QName[state.mixinNames.size()]);
+            mixins = (Name[]) state.mixinNames.toArray(new Name[state.mixinNames.size()]);
         }
         Importer.NodeInfo nodeInfo = new Importer.NodeInfo(state.nodeName, state.nodeTypeName, mixins, state.uuid);
 
@@ -104,7 +104,7 @@ class SysViewImportHandler extends TargetImportHandler {
                              String qName, Attributes atts)
             throws SAXException {
         // check namespace
-        if (!QName.NS_SV_URI.equals(namespaceURI)) {
+        if (!Name.NS_SV_URI.equals(namespaceURI)) {
             throw new SAXException(new InvalidSerializedDataException("invalid namespace for element in system view xml document: "
                     + namespaceURI));
         }
@@ -132,11 +132,11 @@ class SysViewImportHandler extends TargetImportHandler {
             // push new ImportState instance onto the stack
             ImportState state = new ImportState();
             try {
-                state.nodeName = NameFormat.parse(name, nsContext);
-            } catch (IllegalNameException ine) {
-                throw new SAXException(new InvalidSerializedDataException("illegal node name: " + name, ine));
-            } catch (UnknownPrefixException upe) {
-                throw new SAXException(new InvalidSerializedDataException("illegal node name: " + name, upe));
+                state.nodeName = nameResolver.getQName(name);
+            } catch (NameException e) {
+                throw new SAXException(new InvalidSerializedDataException("illegal node name: " + name, e));
+            } catch (NamespaceException e) {
+                throw new SAXException(new InvalidSerializedDataException("illegal node name: " + name, e));
             }
             stack.push(state);
         } else if (SysViewSAXEventGenerator.PROPERTY_ELEMENT.equals(localName)) {
@@ -152,11 +152,11 @@ class SysViewImportHandler extends TargetImportHandler {
                         "missing mandatory sv:name attribute of element sv:property"));
             }
             try {
-                currentPropName = NameFormat.parse(name, nsContext);
-            } catch (IllegalNameException ine) {
-                throw new SAXException(new InvalidSerializedDataException("illegal property name: " + name, ine));
-            } catch (UnknownPrefixException upe) {
-                throw new SAXException(new InvalidSerializedDataException("illegal property name: " + name, upe));
+                currentPropName = nameResolver.getQName(name);
+            } catch (org.apache.jackrabbit.conversion.NameException e) {
+                throw new SAXException(new InvalidSerializedDataException("illegal property name: " + name, e));
+            } catch (NamespaceException e) {
+                throw new SAXException(new InvalidSerializedDataException("illegal property name: " + name, e));
             }
             // property type (sv:type attribute)
             String type = atts.getValue(SysViewSAXEventGenerator.PREFIXED_TYPE_ATTRIBUTE);
@@ -235,20 +235,20 @@ class SysViewImportHandler extends TargetImportHandler {
 
             // check if all system properties (jcr:primaryType, jcr:uuid etc.)
             // have been collected and create node as necessary
-            if (currentPropName.equals(QName.JCR_PRIMARYTYPE)) {
+            if (currentPropName.equals(NameConstants.JCR_PRIMARYTYPE)) {
                 AppendableValue val = (AppendableValue) currentPropValues.get(0);
                 String s = null;
                 try {
                     s = val.retrieve();
-                    state.nodeTypeName = NameFormat.parse(s, nsContext);
+                    state.nodeTypeName = nameResolver.getQName(s);
                 } catch (IOException ioe) {
                     throw new SAXException("error while retrieving value", ioe);
-                } catch (IllegalNameException ine) {
-                    throw new SAXException(new InvalidSerializedDataException("illegal node type name: " + s, ine));
-                } catch (UnknownPrefixException upe) {
-                    throw new SAXException(new InvalidSerializedDataException("illegal node type name: " + s, upe));
+                } catch (org.apache.jackrabbit.conversion.NameException e) {
+                    throw new SAXException(new InvalidSerializedDataException("illegal node type name: " + s, e));
+                } catch (NamespaceException e) {
+                    throw new SAXException(new InvalidSerializedDataException("illegal node type name: " + s, e));
                 }
-            } else if (currentPropName.equals(QName.JCR_MIXINTYPES)) {
+            } else if (currentPropName.equals(NameConstants.JCR_MIXINTYPES)) {
                 if (state.mixinNames == null) {
                     state.mixinNames = new ArrayList(currentPropValues.size());
                 }
@@ -258,17 +258,17 @@ class SysViewImportHandler extends TargetImportHandler {
                     String s = null;
                     try {
                         s = val.retrieve();
-                        QName mixin = NameFormat.parse(s, nsContext);
+                        Name mixin = nameResolver.getQName(s);
                         state.mixinNames.add(mixin);
                     } catch (IOException ioe) {
                         throw new SAXException("error while retrieving value", ioe);
-                    } catch (IllegalNameException ine) {
-                        throw new SAXException(new InvalidSerializedDataException("illegal mixin type name: " + s, ine));
-                    } catch (UnknownPrefixException upe) {
-                        throw new SAXException(new InvalidSerializedDataException("illegal mixin type name: " + s, upe));
+                    } catch (org.apache.jackrabbit.conversion.NameException e) {
+                        throw new SAXException(new InvalidSerializedDataException("illegal mixin type name: " + s, e));
+                    } catch (NamespaceException e) {
+                        throw new SAXException(new InvalidSerializedDataException("illegal mixin type name: " + s, e));
                     }
                 }
-            } else if (currentPropName.equals(QName.JCR_UUID)) {
+            } else if (currentPropName.equals(NameConstants.JCR_UUID)) {
                 AppendableValue val = (AppendableValue) currentPropValues.get(0);
                 try {
                     state.uuid = val.retrieve();
@@ -297,11 +297,11 @@ class SysViewImportHandler extends TargetImportHandler {
         /**
          * name of current node
          */
-        QName nodeName;
+        Name nodeName;
         /**
          * primary type of current node
          */
-        QName nodeTypeName;
+        Name nodeTypeName;
         /**
          * list of mixin types of current node
          */
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SysViewSAXEventGenerator.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SysViewSAXEventGenerator.java
index 345068f..90ce8ba 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SysViewSAXEventGenerator.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SysViewSAXEventGenerator.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.jcr2spi.xml;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.value.ValueHelper;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
@@ -42,23 +42,23 @@ public class SysViewSAXEventGenerator extends AbstractSAXEventGenerator {
      */
     public static final String NODE_ELEMENT = "node";
     public static final String PREFIXED_NODE_ELEMENT =
-        QName.NS_SV_PREFIX + ":" + NODE_ELEMENT;
+        Name.NS_SV_PREFIX + ":" + NODE_ELEMENT;
 
     public static final String PROPERTY_ELEMENT = "property";
     public static final String PREFIXED_PROPERTY_ELEMENT =
-        QName.NS_SV_PREFIX + ":" + PROPERTY_ELEMENT;
+        Name.NS_SV_PREFIX + ":" + PROPERTY_ELEMENT;
 
     public static final String VALUE_ELEMENT = "value";
     public static final String PREFIXED_VALUE_ELEMENT =
-        QName.NS_SV_PREFIX + ":" + VALUE_ELEMENT;
+        Name.NS_SV_PREFIX + ":" + VALUE_ELEMENT;
 
     public static final String NAME_ATTRIBUTE = "name";
     public static final String PREFIXED_NAME_ATTRIBUTE =
-        QName.NS_SV_PREFIX + ":" + NAME_ATTRIBUTE;
+        Name.NS_SV_PREFIX + ":" + NAME_ATTRIBUTE;
 
     public static final String TYPE_ATTRIBUTE = "type";
     public static final String PREFIXED_TYPE_ATTRIBUTE =
-        QName.NS_SV_PREFIX + ":" + TYPE_ATTRIBUTE;
+        Name.NS_SV_PREFIX + ":" + TYPE_ATTRIBUTE;
 
     public static final String CDATA_TYPE = "CDATA";
     public static final String ENUMERATION_TYPE = "ENUMERATION";
@@ -72,8 +72,8 @@ public class SysViewSAXEventGenerator extends AbstractSAXEventGenerator {
     private static final Attributes ATTRS_BINARY_ENCODED_VALUE;
     static {
         AttributesImpl attrs = new AttributesImpl();
-        attrs.addAttribute(QName.NS_XMLNS_URI, NS_XMLSCHEMA_INSTANCE_PREFIX, "xmlns:" + NS_XMLSCHEMA_INSTANCE_PREFIX, CDATA_TYPE, NS_XMLSCHEMA_INSTANCE_URI);
-        attrs.addAttribute(QName.NS_XMLNS_URI, NS_XMLSCHEMA_PREFIX, "xmlns:" + NS_XMLSCHEMA_PREFIX, CDATA_TYPE, NS_XMLSCHEMA_URI);
+        attrs.addAttribute(Name.NS_XMLNS_URI, NS_XMLSCHEMA_INSTANCE_PREFIX, "xmlns:" + NS_XMLSCHEMA_INSTANCE_PREFIX, CDATA_TYPE, NS_XMLSCHEMA_INSTANCE_URI);
+        attrs.addAttribute(Name.NS_XMLNS_URI, NS_XMLSCHEMA_PREFIX, "xmlns:" + NS_XMLSCHEMA_PREFIX, CDATA_TYPE, NS_XMLSCHEMA_URI);
         attrs.addAttribute(NS_XMLSCHEMA_INSTANCE_URI, "type", NS_XMLSCHEMA_INSTANCE_PREFIX + ":type", "CDATA", NS_XMLSCHEMA_PREFIX + ":base64Binary");
         ATTRS_BINARY_ENCODED_VALUE = attrs;
     }
@@ -113,10 +113,10 @@ public class SysViewSAXEventGenerator extends AbstractSAXEventGenerator {
             nodeName = node.getName();
         }
 
-        attrs.addAttribute(QName.NS_SV_URI, NAME_ATTRIBUTE, PREFIXED_NAME_ATTRIBUTE,
+        attrs.addAttribute(Name.NS_SV_URI, NAME_ATTRIBUTE, PREFIXED_NAME_ATTRIBUTE,
                 CDATA_TYPE, nodeName);
         // start node element
-        contentHandler.startElement(QName.NS_SV_URI, NODE_ELEMENT,
+        contentHandler.startElement(Name.NS_SV_URI, NODE_ELEMENT,
                 PREFIXED_NODE_ELEMENT, attrs);
     }
 
@@ -142,7 +142,7 @@ public class SysViewSAXEventGenerator extends AbstractSAXEventGenerator {
     protected void leaving(Node node, int level)
             throws RepositoryException, SAXException {
         // end node element
-        contentHandler.endElement(QName.NS_SV_URI, NODE_ELEMENT, PREFIXED_NODE_ELEMENT);
+        contentHandler.endElement(Name.NS_SV_URI, NODE_ELEMENT, PREFIXED_NODE_ELEMENT);
     }
 
     /**
@@ -153,7 +153,7 @@ public class SysViewSAXEventGenerator extends AbstractSAXEventGenerator {
         String propName = prop.getName();
         AttributesImpl attrs = new AttributesImpl();
         // name attribute
-        attrs.addAttribute(QName.NS_SV_URI, NAME_ATTRIBUTE, PREFIXED_NAME_ATTRIBUTE,
+        attrs.addAttribute(Name.NS_SV_URI, NAME_ATTRIBUTE, PREFIXED_NAME_ATTRIBUTE,
                 CDATA_TYPE, propName);
         // type attribute
         int type = prop.getType();
@@ -165,19 +165,19 @@ public class SysViewSAXEventGenerator extends AbstractSAXEventGenerator {
             throw new RepositoryException("unexpected property-type ordinal: "
                     + type, iae);
         }
-        attrs.addAttribute(QName.NS_SV_URI, TYPE_ATTRIBUTE, PREFIXED_TYPE_ATTRIBUTE,
+        attrs.addAttribute(Name.NS_SV_URI, TYPE_ATTRIBUTE, PREFIXED_TYPE_ATTRIBUTE,
                 ENUMERATION_TYPE, typeName);
 
         // start property element
-        contentHandler.startElement(QName.NS_SV_URI, PROPERTY_ELEMENT,
+        contentHandler.startElement(Name.NS_SV_URI, PROPERTY_ELEMENT,
                 PREFIXED_PROPERTY_ELEMENT, attrs);
 
         // values
         if (prop.getType() == PropertyType.BINARY && skipBinary) {
             // empty value element
-            contentHandler.startElement(QName.NS_SV_URI, VALUE_ELEMENT,
+            contentHandler.startElement(Name.NS_SV_URI, VALUE_ELEMENT,
                     PREFIXED_VALUE_ELEMENT, new AttributesImpl());
-            contentHandler.endElement(QName.NS_SV_URI, VALUE_ELEMENT,
+            contentHandler.endElement(Name.NS_SV_URI, VALUE_ELEMENT,
                     PREFIXED_VALUE_ELEMENT);
         } else {
             boolean multiValued = prop.getDefinition().isMultiple();
@@ -210,7 +210,7 @@ public class SysViewSAXEventGenerator extends AbstractSAXEventGenerator {
                 }
 
                 // start value element
-                contentHandler.startElement(QName.NS_SV_URI, VALUE_ELEMENT,
+                contentHandler.startElement(Name.NS_SV_URI, VALUE_ELEMENT,
                         PREFIXED_VALUE_ELEMENT, attributes);
 
                 // characters
@@ -245,7 +245,7 @@ public class SysViewSAXEventGenerator extends AbstractSAXEventGenerator {
                 }
 
                 // end value element
-                contentHandler.endElement(QName.NS_SV_URI, VALUE_ELEMENT,
+                contentHandler.endElement(Name.NS_SV_URI, VALUE_ELEMENT,
                         PREFIXED_VALUE_ELEMENT);
 
                 if (mustSendBinary) {
@@ -261,7 +261,7 @@ public class SysViewSAXEventGenerator extends AbstractSAXEventGenerator {
      */
     protected void leaving(Property prop, int level)
             throws RepositoryException, SAXException {
-        contentHandler.endElement(QName.NS_SV_URI, PROPERTY_ELEMENT,
+        contentHandler.endElement(Name.NS_SV_URI, PROPERTY_ELEMENT,
                 PREFIXED_PROPERTY_ELEMENT);
     }
 }
diff --git a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/TargetImportHandler.java b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/TargetImportHandler.java
index ca1d222..4fec3da 100644
--- a/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/TargetImportHandler.java
+++ b/contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/TargetImportHandler.java
@@ -16,8 +16,9 @@
  */
 package org.apache.jackrabbit.jcr2spi.xml;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
 import org.apache.jackrabbit.util.TransientFileFactory;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.SAXException;
 import org.slf4j.LoggerFactory;
@@ -44,10 +45,12 @@ abstract class TargetImportHandler extends DefaultHandler {
 
     protected final Importer importer;
     protected final NamespaceResolver nsContext;
+    protected final NameResolver nameResolver;
 
-    protected TargetImportHandler(Importer importer, NamespaceResolver nsContext) {
+    protected TargetImportHandler(Importer importer, NamespaceResolver nsContext, org.apache.jackrabbit.conversion.NameResolver nameResolver) {
         this.importer = importer;
         this.nsContext = nsContext;
+        this.nameResolver = nameResolver;
     }
 
     /**
diff --git a/contrib/spi/spi-logger/src/main/java/org/apache/jackrabbit/spi/logger/RepositoryServiceLogger.java b/contrib/spi/spi-logger/src/main/java/org/apache/jackrabbit/spi/logger/RepositoryServiceLogger.java
index 75e8dd3..2ed7a4d 100644
--- a/contrib/spi/spi-logger/src/main/java/org/apache/jackrabbit/spi/logger/RepositoryServiceLogger.java
+++ b/contrib/spi/spi-logger/src/main/java/org/apache/jackrabbit/spi/logger/RepositoryServiceLogger.java
@@ -32,8 +32,10 @@ import org.apache.jackrabbit.spi.LockInfo;
 import org.apache.jackrabbit.spi.QueryInfo;
 import org.apache.jackrabbit.spi.EventFilter;
 import org.apache.jackrabbit.spi.EventBundle;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.PathFactory;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.Credentials;
@@ -75,6 +77,22 @@ public class RepositoryServiceLogger implements RepositoryService {
         this.log = log;
     }
 
+    public NameFactory getNameFactory() throws RepositoryException {
+        return (NameFactory) execute(new Callable() {
+            public Object call() throws RepositoryException {
+                return service.getNameFactory();
+            }
+        }, "getNameFactory()", new Object[]{});
+    }
+
+    public PathFactory getPathFactory() throws RepositoryException {
+        return (PathFactory) execute(new Callable() {
+            public Object call() throws RepositoryException {
+                return service.getPathFactory();
+            }
+        }, "PathFactory()", new Object[]{});
+    }
+
     public IdFactory getIdFactory() throws RepositoryException {
         return (IdFactory) execute(new Callable() {
             public Object call() throws RepositoryException {
@@ -267,13 +285,13 @@ public class RepositoryServiceLogger implements RepositoryService {
     public void move(final SessionInfo sessionInfo,
                      final NodeId nodeId,
                      final NodeId nodeId1,
-                     final QName name) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+                     final Name name) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
         execute(new Callable() {
             public Object call() throws RepositoryException {
                 service.move(sessionInfo, nodeId, nodeId1, name);
                 return null;
             }
-        }, "move(SessionInfo,NodeId,NodeId,QName)",
+        }, "move(SessionInfo,NodeId,NodeId,Name)",
                 new Object[]{nodeId, nodeId1, name});
     }
 
@@ -281,13 +299,13 @@ public class RepositoryServiceLogger implements RepositoryService {
                      final String s,
                      final NodeId nodeId,
                      final NodeId nodeId1,
-                     final QName name) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
+                     final Name name) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
         execute(new Callable() {
             public Object call() throws RepositoryException {
                 service.copy(sessionInfo, s, nodeId, nodeId1, name);
                 return null;
             }
-        }, "copy(SessionInfo,String,NodeId,NodeId,QName)",
+        }, "copy(SessionInfo,String,NodeId,NodeId,Name)",
                 new Object[]{s, nodeId, nodeId1, name});
     }
 
@@ -307,14 +325,14 @@ public class RepositoryServiceLogger implements RepositoryService {
                       final String s,
                       final NodeId nodeId,
                       final NodeId nodeId1,
-                      final QName name,
+                      final Name name,
                       final boolean b) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
         execute(new Callable() {
             public Object call() throws RepositoryException {
                 service.clone(sessionInfo, s, nodeId, nodeId1, name, b);
                 return null;
             }
-        }, "clone(SessionInfo,String,NodeId,NodeId,QName,boolean)",
+        }, "clone(SessionInfo,String,NodeId,NodeId,Name,boolean)",
                 new Object[]{s, nodeId, nodeId1, name, new Boolean(b)});
     }
 
@@ -449,27 +467,27 @@ public class RepositoryServiceLogger implements RepositoryService {
     public void addVersionLabel(final SessionInfo sessionInfo,
                                 final NodeId nodeId,
                                 final NodeId nodeId1,
-                                final QName name,
+                                final Name name,
                                 final boolean b) throws VersionException, RepositoryException {
         execute(new Callable() {
             public Object call() throws RepositoryException {
                 service.addVersionLabel(sessionInfo, nodeId, nodeId1, name, b);
                 return null;
             }
-        }, "addVersionLabel(SessionInfo,NodeId,NodeId,QName,boolean)",
+        }, "addVersionLabel(SessionInfo,NodeId,NodeId,Name,boolean)",
                 new Object[]{nodeId, nodeId1, name, new Boolean(b)});
     }
 
     public void removeVersionLabel(final SessionInfo sessionInfo,
                                    final NodeId nodeId,
                                    final NodeId nodeId1,
-                                   final QName name) throws VersionException, RepositoryException {
+                                   final Name name) throws VersionException, RepositoryException {
         execute(new Callable() {
             public Object call() throws RepositoryException {
                 service.removeVersionLabel(sessionInfo, nodeId, nodeId1, name);
                 return null;
             }
-        }, "removeVersionLabel(SessionInfo,NodeId,NodeId,QName)",
+        }, "removeVersionLabel(SessionInfo,NodeId,NodeId,Name)",
                 new Object[]{nodeId, nodeId1, name});
     }
 
@@ -512,14 +530,14 @@ public class RepositoryServiceLogger implements RepositoryService {
                                          final Path path,
                                          final boolean b,
                                          final String[] strings,
-                                         final QName[] qNames,
+                                         final Name[] qNames,
                                          final boolean b1)
             throws UnsupportedRepositoryOperationException, RepositoryException {
         return (EventFilter) execute(new Callable() {
             public Object call() throws RepositoryException {
                 return service.createEventFilter(sessionInfo, i, path, b, strings, qNames, b1);
             }
-        }, "createEventFilter(SessionInfo,int,Path,boolean,String[],QName[],boolean)",
+        }, "createEventFilter(SessionInfo,int,Path,boolean,String[],Name[],boolean)",
                 new Object[]{new Integer(i), path, new Boolean(b), strings, qNames, new Boolean(b1)});
     }
 
@@ -605,13 +623,13 @@ public class RepositoryServiceLogger implements RepositoryService {
     }
 
     public Iterator getQNodeTypeDefinitions(
-            final SessionInfo sessionInfo,final QName[] ntNames)
+            final SessionInfo sessionInfo,final Name[] ntNames)
             throws RepositoryException {
         return (Iterator) execute(new Callable() {
             public Object call() throws RepositoryException {
                 return service.getQNodeTypeDefinitions(sessionInfo, ntNames);
             }
-        }, "getQNodeTypeDefinitions(SessionInfo,QName[])", new Object[]{ntNames});
+        }, "getQNodeTypeDefinitions(SessionInfo,Name[])", new Object[]{ntNames});
     }
 
     private Object execute(Callable callable, String methodName, Object[] args)
diff --git a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientBatch.java b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientBatch.java
index e1ef992..35ff9b2 100644
--- a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientBatch.java
+++ b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientBatch.java
@@ -21,9 +21,9 @@ import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.PropertyId;
 import org.apache.jackrabbit.spi.ItemId;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.commons.SerializableBatch;
 import org.apache.jackrabbit.spi.rmi.remote.RemoteSessionInfo;
-import org.apache.jackrabbit.name.QName;
 
 /**
  * <code>ClientBatch</code> implements a SPI {@link Batch} which wraps a
@@ -64,8 +64,8 @@ class ClientBatch implements Batch {
      * {@inheritDoc}
      */
     public void addNode(NodeId parentId,
-                        QName nodeName,
-                        QName nodetypeName,
+                        Name nodeName,
+                        Name nodetypeName,
                         String uuid) {
         batch.addNode(parentId, nodeName, nodetypeName, uuid);
     }
@@ -73,7 +73,7 @@ class ClientBatch implements Batch {
     /**
      * {@inheritDoc}
      */
-    public void addProperty(NodeId parentId, QName propertyName, QValue value) {
+    public void addProperty(NodeId parentId, Name propertyName, QValue value) {
         batch.addProperty(parentId, propertyName, value);
     }
 
@@ -81,7 +81,7 @@ class ClientBatch implements Batch {
      * {@inheritDoc}
      */
     public void addProperty(NodeId parentId,
-                            QName propertyName,
+                            Name propertyName,
                             QValue[] values) {
         batch.addProperty(parentId, propertyName, values);
     }
@@ -119,7 +119,7 @@ class ClientBatch implements Batch {
     /**
      * {@inheritDoc}
      */
-    public void setMixins(NodeId nodeId, QName[] mixinNodeTypeIds) {
+    public void setMixins(NodeId nodeId, Name[] mixinNodeTypeIds) {
         batch.setMixins(nodeId, mixinNodeTypeIds);
     }
 
@@ -128,7 +128,7 @@ class ClientBatch implements Batch {
      */
     public void move(NodeId srcNodeId,
                      NodeId destParentNodeId,
-                     QName destName) {
+                     Name destName) {
         batch.move(srcNodeId, destParentNodeId, destName);
     }
 }
diff --git a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientQueryInfo.java b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientQueryInfo.java
index 3e89ea4..0aac216 100644
--- a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientQueryInfo.java
+++ b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientQueryInfo.java
@@ -17,8 +17,8 @@
 package org.apache.jackrabbit.spi.rmi.client;
 
 import org.apache.jackrabbit.spi.QueryInfo;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.rmi.remote.RemoteQueryInfo;
-import org.apache.jackrabbit.name.QName;
 
 import javax.jcr.RangeIterator;
 import java.rmi.RemoteException;
@@ -57,7 +57,7 @@ class ClientQueryInfo implements QueryInfo {
     /**
      * {@inheritDoc}
      */
-    public QName[] getColumnNames() {
+    public Name[] getColumnNames() {
         try {
             return queryInfo.getColumnNames();
         } catch (RemoteException e) {
diff --git a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientRepositoryService.java b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientRepositoryService.java
index d189c5c..c58097f 100644
--- a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientRepositoryService.java
+++ b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/client/ClientRepositoryService.java
@@ -32,15 +32,20 @@ import org.apache.jackrabbit.spi.QueryInfo;
 import org.apache.jackrabbit.spi.EventFilter;
 import org.apache.jackrabbit.spi.EventBundle;
 import org.apache.jackrabbit.spi.NodeInfo;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.PathFactory;
 import org.apache.jackrabbit.spi.rmi.remote.RemoteRepositoryService;
 import org.apache.jackrabbit.spi.rmi.remote.RemoteSessionInfo;
 import org.apache.jackrabbit.spi.rmi.remote.RemoteIterator;
 import org.apache.jackrabbit.spi.rmi.remote.RemoteQueryInfo;
 import org.apache.jackrabbit.spi.rmi.common.SerializableInputStream;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+
 import org.apache.jackrabbit.value.QValueFactoryImpl;
 import org.apache.jackrabbit.identifier.IdFactoryImpl;
+import org.apache.jackrabbit.name.PathFactoryImpl;
+import org.apache.jackrabbit.name.NameFactoryImpl;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.Credentials;
@@ -105,6 +110,14 @@ public class ClientRepositoryService implements RepositoryService {
         return idFactory;
     }
 
+    public NameFactory getNameFactory() {
+        return NameFactoryImpl.getInstance();
+    }
+
+    public PathFactory getPathFactory() {
+        return PathFactoryImpl.getInstance();
+    }
+
     /**
      * {@inheritDoc}
      */
@@ -346,7 +359,7 @@ public class ClientRepositoryService implements RepositoryService {
     public void move(SessionInfo sessionInfo,
                      NodeId srcNodeId,
                      NodeId destParentNodeId,
-                     QName destName) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+                     Name destName) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
         try {
             remoteService.move(getRemoteSessionInfo(sessionInfo), srcNodeId,
                     destParentNodeId, destName);
@@ -362,7 +375,7 @@ public class ClientRepositoryService implements RepositoryService {
                      String srcWorkspaceName,
                      NodeId srcNodeId,
                      NodeId destParentNodeId,
-                     QName destName) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
+                     Name destName) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
         try {
             remoteService.copy(getRemoteSessionInfo(sessionInfo),
                     srcWorkspaceName, srcNodeId, destParentNodeId, destName);
@@ -393,7 +406,7 @@ public class ClientRepositoryService implements RepositoryService {
                       String srcWorkspaceName,
                       NodeId srcNodeId,
                       NodeId destParentNodeId,
-                      QName destName,
+                      Name destName,
                       boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
         try {
             remoteService.clone(getRemoteSessionInfo(sessionInfo),
@@ -562,7 +575,7 @@ public class ClientRepositoryService implements RepositoryService {
     public void addVersionLabel(SessionInfo sessionInfo,
                                 NodeId versionHistoryId,
                                 NodeId versionId,
-                                QName label,
+                                Name label,
                                 boolean moveLabel) throws VersionException, RepositoryException {
         try {
             remoteService.addVersionLabel(getRemoteSessionInfo(sessionInfo),
@@ -578,7 +591,7 @@ public class ClientRepositoryService implements RepositoryService {
     public void removeVersionLabel(SessionInfo sessionInfo,
                                    NodeId versionHistoryId,
                                    NodeId versionId,
-                                   QName label) throws VersionException, RepositoryException {
+                                   Name label) throws VersionException, RepositoryException {
         try {
             remoteService.removeVersionLabel(getRemoteSessionInfo(sessionInfo), versionHistoryId, versionId, label);
         } catch (RemoteException e) {
@@ -645,7 +658,7 @@ public class ClientRepositoryService implements RepositoryService {
                                          Path absPath,
                                          boolean isDeep,
                                          String[] uuid,
-                                         QName[] nodeTypeName,
+                                         Name[] nodeTypeName,
                                          boolean noLocal)
             throws UnsupportedRepositoryOperationException, RepositoryException {
         try {
@@ -752,7 +765,7 @@ public class ClientRepositoryService implements RepositoryService {
     /**
      * {@inheritDoc}
      */
-    public Iterator getQNodeTypeDefinitions(SessionInfo sessionInfo, QName[] nodetypeNames) throws RepositoryException {
+    public Iterator getQNodeTypeDefinitions(SessionInfo sessionInfo, Name[] nodetypeNames) throws RepositoryException {
         try {
             RemoteIterator it = remoteService.getQNodeTypeDefinitions(getRemoteSessionInfo(sessionInfo), nodetypeNames);
             return new ClientIterator(it);
diff --git a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/common/ItemInfoImpl.java b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/common/ItemInfoImpl.java
index d298600..d2e43a1 100644
--- a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/common/ItemInfoImpl.java
+++ b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/common/ItemInfoImpl.java
@@ -18,8 +18,8 @@ package org.apache.jackrabbit.spi.rmi.common;
 
 import org.apache.jackrabbit.spi.ItemInfo;
 import org.apache.jackrabbit.spi.NodeId;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 
 import java.io.Serializable;
 
@@ -38,7 +38,7 @@ abstract class ItemInfoImpl implements ItemInfo, Serializable {
     /**
      * The name of this item info.
      */
-    private final QName name;
+    private final Name name;
 
     /**
      * The path of this item info.
@@ -59,7 +59,7 @@ abstract class ItemInfoImpl implements ItemInfo, Serializable {
      * @param path     the path to this item.
      * @param isNode   if this item is a node.
      */
-    public ItemInfoImpl(NodeId parentId, QName name, Path path, boolean isNode) {
+    public ItemInfoImpl(NodeId parentId, Name name, Path path, boolean isNode) {
         this.parentId = parentId;
         this.name = name;
         this.path = path;
@@ -76,7 +76,7 @@ abstract class ItemInfoImpl implements ItemInfo, Serializable {
     /**
      * {@inheritDoc}
      */
-    public QName getQName() {
+    public Name getName() {
         return name;
     }
 
diff --git a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/remote/RemoteQueryInfo.java b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/remote/RemoteQueryInfo.java
index 4d2be09..6f0abe4 100644
--- a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/remote/RemoteQueryInfo.java
+++ b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/remote/RemoteQueryInfo.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.spi.rmi.remote;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
@@ -33,9 +33,9 @@ public interface RemoteQueryInfo extends Remote {
     public RemoteIterator getRows() throws RemoteException;
 
     /**
-     * @return an array of QName representing the column names of the query
+     * @return an array of Name representing the column names of the query
      *         result.
      * @see javax.jcr.query.QueryResult#getColumnNames()
      */
-    public QName[] getColumnNames() throws RemoteException;
+    public Name[] getColumnNames() throws RemoteException;
 }
diff --git a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/remote/RemoteRepositoryService.java b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/remote/RemoteRepositoryService.java
index a89d8ba..2e47a9b 100644
--- a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/remote/RemoteRepositoryService.java
+++ b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/remote/RemoteRepositoryService.java
@@ -28,8 +28,8 @@ import org.apache.jackrabbit.spi.EventBundle;
 import org.apache.jackrabbit.spi.SessionInfo;
 import org.apache.jackrabbit.spi.NodeInfo;
 import org.apache.jackrabbit.spi.commons.SerializableBatch;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
+import org.apache.jackrabbit.spi.Name;
 
 import javax.jcr.Credentials;
 import javax.jcr.RepositoryException;
@@ -265,12 +265,12 @@ public interface RemoteRepositoryService extends Remote {
      * @param destName
      * @throws javax.jcr.RepositoryException
      * @throws RemoteException if an error occurs.
-     * @see org.apache.jackrabbit.spi.RepositoryService#move(org.apache.jackrabbit.spi.SessionInfo, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.name.QName)
+     * @see org.apache.jackrabbit.spi.RepositoryService#move(org.apache.jackrabbit.spi.SessionInfo, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.Name)
      */
     public void move(RemoteSessionInfo sessionInfo,
                      NodeId srcNodeId,
                      NodeId destParentNodeId,
-                     QName destName)
+                     Name destName)
             throws RepositoryException, RemoteException;
 
     /**
@@ -281,13 +281,13 @@ public interface RemoteRepositoryService extends Remote {
      * @param destName
      * @throws javax.jcr.RepositoryException
      * @throws RemoteException if an error occurs.
-     * @see org.apache.jackrabbit.spi.RepositoryService#copy(org.apache.jackrabbit.spi.SessionInfo, String, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.name.QName)
+     * @see org.apache.jackrabbit.spi.RepositoryService#copy(org.apache.jackrabbit.spi.SessionInfo, String, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.Name)
      */
     public void copy(RemoteSessionInfo sessionInfo,
                      String srcWorkspaceName,
                      NodeId srcNodeId,
                      NodeId destParentNodeId,
-                     QName destName)
+                     Name destName)
             throws RepositoryException, RemoteException;
 
     /**
@@ -312,13 +312,13 @@ public interface RemoteRepositoryService extends Remote {
      * @param removeExisting
      * @throws javax.jcr.RepositoryException
      * @throws RemoteException if an error occurs.
-     * @see org.apache.jackrabbit.spi.RepositoryService#clone(org.apache.jackrabbit.spi.SessionInfo, String, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.name.QName, boolean)
+     * @see org.apache.jackrabbit.spi.RepositoryService#clone(org.apache.jackrabbit.spi.SessionInfo, String, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.Name, boolean)
      */
     public void clone(RemoteSessionInfo sessionInfo,
                       String srcWorkspaceName,
                       NodeId srcNodeId,
                       NodeId destParentNodeId,
-                      QName destName,
+                      Name destName,
                       boolean removeExisting)
             throws RepositoryException, RemoteException;
 
@@ -479,12 +479,12 @@ public interface RemoteRepositoryService extends Remote {
      * @param moveLabel
      * @throws javax.jcr.RepositoryException
      * @throws RemoteException if an error occurs.
-     * @see org.apache.jackrabbit.spi.RepositoryService#addVersionLabel(org.apache.jackrabbit.spi.SessionInfo, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.name.QName, boolean)
+     * @see org.apache.jackrabbit.spi.RepositoryService#addVersionLabel(org.apache.jackrabbit.spi.SessionInfo, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.Name, boolean)
      */
     public void addVersionLabel(RemoteSessionInfo sessionInfo,
                                 NodeId versionHistoryId,
                                 NodeId versionId,
-                                QName label,
+                                Name label,
                                 boolean moveLabel)
             throws RepositoryException, RemoteException;
 
@@ -494,12 +494,12 @@ public interface RemoteRepositoryService extends Remote {
      * @param label
      * @throws javax.jcr.RepositoryException
      * @throws RemoteException if an error occurs.
-     * @see org.apache.jackrabbit.spi.RepositoryService#removeVersionLabel(org.apache.jackrabbit.spi.SessionInfo, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.name.QName)
+     * @see org.apache.jackrabbit.spi.RepositoryService#removeVersionLabel(org.apache.jackrabbit.spi.SessionInfo, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.NodeId, org.apache.jackrabbit.spi.Name)
      */
     public void removeVersionLabel(RemoteSessionInfo sessionInfo,
                                    NodeId versionHistoryId,
                                    NodeId versionId,
-                                   QName label)
+                                   Name label)
             throws RepositoryException, RemoteException;
 
     /**
@@ -564,11 +564,11 @@ public interface RemoteRepositoryService extends Remote {
      * @throws RepositoryException if an error occurs while creating the
      * EventFilter.
      * @throws RemoteException if an error occurs.
-     * @see org.apache.jackrabbit.spi.RepositoryService#createEventFilter(org.apache.jackrabbit.spi.SessionInfo, int, org.apache.jackrabbit.name.Path, boolean, String[], org.apache.jackrabbit.name.QName[], boolean)
+     * @see org.apache.jackrabbit.spi.RepositoryService#createEventFilter(org.apache.jackrabbit.spi.SessionInfo, int, org.apache.jackrabbit.spi.Path, boolean, String[], org.apache.jackrabbit.spi.Name[], boolean)
      */
     public EventFilter createEventFilter(RemoteSessionInfo sessionInfo, int eventTypes,
                                          Path absPath, boolean isDeep,
-                                         String[] uuid, QName[] nodeTypeName,
+                                         String[] uuid, Name[] nodeTypeName,
                                          boolean noLocal)
             throws RepositoryException, RemoteException;
 
@@ -680,7 +680,7 @@ public interface RemoteRepositoryService extends Remote {
      * @return
      * @throws javax.jcr.RepositoryException
      * @throws RemoteException if an error occurs.
-     * @see org.apache.jackrabbit.spi.RepositoryService#getQNodeTypeDefinition(org.apache.jackrabbit.spi.SessionInfo, QName)
+     * @see org.apache.jackrabbit.spi.RepositoryService#getQNodeTypeDefinition(org.apache.jackrabbit.spi.SessionInfo, Name)
      */
-    public RemoteIterator getQNodeTypeDefinitions(RemoteSessionInfo sessionInfo, QName[] ntNames) throws RepositoryException, RemoteException;
+    public RemoteIterator getQNodeTypeDefinitions(RemoteSessionInfo sessionInfo, Name[] ntNames) throws RepositoryException, RemoteException;
 }
diff --git a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/server/ServerQueryInfo.java b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/server/ServerQueryInfo.java
index f06c740..c5f2d08 100644
--- a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/server/ServerQueryInfo.java
+++ b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/server/ServerQueryInfo.java
@@ -23,7 +23,7 @@ import org.apache.jackrabbit.spi.QueryInfo;
 import org.apache.jackrabbit.spi.QueryResultRow;
 import org.apache.jackrabbit.spi.IdFactory;
 import org.apache.jackrabbit.spi.NodeId;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.util.IteratorHelper;
 
 import java.rmi.RemoteException;
@@ -76,7 +76,7 @@ class ServerQueryInfo extends ServerObject implements RemoteQueryInfo {
     /**
      * {@inheritDoc}
      */
-    public QName[] getColumnNames() throws RemoteException {
+    public Name[] getColumnNames() throws RemoteException {
         return queryInfo.getColumnNames();
     }
 }
diff --git a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/server/ServerRepositoryService.java b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/server/ServerRepositoryService.java
index 177a814..fbfbcd4 100644
--- a/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/server/ServerRepositoryService.java
+++ b/contrib/spi/spi-rmi/src/main/java/org/apache/jackrabbit/spi/rmi/server/ServerRepositoryService.java
@@ -39,6 +39,8 @@ import org.apache.jackrabbit.spi.ItemInfo;
 import org.apache.jackrabbit.spi.Event;
 import org.apache.jackrabbit.spi.IdFactory;
 import org.apache.jackrabbit.spi.Batch;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.commons.EventFilterImpl;
 import org.apache.jackrabbit.spi.commons.QPropertyDefinitionImpl;
 import org.apache.jackrabbit.spi.commons.QNodeDefinitionImpl;
@@ -50,8 +52,6 @@ import org.apache.jackrabbit.spi.commons.NodeInfoImpl;
 import org.apache.jackrabbit.spi.commons.PropertyInfoImpl;
 import org.apache.jackrabbit.spi.commons.LockInfoImpl;
 import org.apache.jackrabbit.spi.commons.SerializableBatch;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
 import org.apache.jackrabbit.identifier.IdFactoryImpl;
 import org.apache.jackrabbit.util.IteratorHelper;
 
@@ -383,7 +383,7 @@ public class ServerRepositoryService extends ServerObject implements RemoteRepos
     public void move(RemoteSessionInfo sessionInfo,
                      NodeId srcNodeId,
                      NodeId destParentNodeId,
-                     QName destName) throws RepositoryException, RemoteException {
+                     Name destName) throws RepositoryException, RemoteException {
         try {
             service.move(getSessionInfo(sessionInfo),
                     srcNodeId, destParentNodeId, destName);
@@ -399,7 +399,7 @@ public class ServerRepositoryService extends ServerObject implements RemoteRepos
                      String srcWorkspaceName,
                      NodeId srcNodeId,
                      NodeId destParentNodeId,
-                     QName destName) throws RepositoryException, RemoteException {
+                     Name destName) throws RepositoryException, RemoteException {
         try {
             service.copy(getSessionInfo(sessionInfo), srcWorkspaceName,
                     srcNodeId, destParentNodeId, destName);
@@ -429,7 +429,7 @@ public class ServerRepositoryService extends ServerObject implements RemoteRepos
                       String srcWorkspaceName,
                       NodeId srcNodeId,
                       NodeId destParentNodeId,
-                      QName destName,
+                      Name destName,
                       boolean removeExisting) throws RepositoryException, RemoteException {
         try {
             service.clone(getSessionInfo(sessionInfo), srcWorkspaceName,
@@ -612,7 +612,7 @@ public class ServerRepositoryService extends ServerObject implements RemoteRepos
     public void addVersionLabel(RemoteSessionInfo sessionInfo,
                                 NodeId versionHistoryId,
                                 NodeId versionId,
-                                QName label,
+                                Name label,
                                 boolean moveLabel) throws RepositoryException, RemoteException {
         try {
             service.addVersionLabel(getSessionInfo(sessionInfo),
@@ -628,7 +628,7 @@ public class ServerRepositoryService extends ServerObject implements RemoteRepos
     public void removeVersionLabel(RemoteSessionInfo sessionInfo,
                                    NodeId versionHistoryId,
                                    NodeId versionId,
-                                   QName label) throws RepositoryException, RemoteException {
+                                   Name label) throws RepositoryException, RemoteException {
         try {
             service.removeVersionLabel(getSessionInfo(sessionInfo),
                     versionHistoryId, versionId, label);
@@ -685,7 +685,7 @@ public class ServerRepositoryService extends ServerObject implements RemoteRepos
                                          Path absPath,
                                          boolean isDeep,
                                          String[] uuid,
-                                         QName[] nodeTypeName,
+                                         Name[] nodeTypeName,
                                          boolean noLocal)
             throws RepositoryException, RemoteException {
         try {
@@ -734,10 +734,10 @@ public class ServerRepositoryService extends ServerObject implements RemoteRepos
                         id = idFactory.createNodeId(nodeId.getUniqueID(), nodeId.getPath());
                     } else {
                         PropertyId propId = (PropertyId) e.getItemId();
-                        id = idFactory.createPropertyId(parentId, propId.getQName());
+                        id = idFactory.createPropertyId(parentId, propId.getName());
                     }
                     Event serEvent = new EventImpl(e.getType(),
-                            e.getQPath(), id, parentId,
+                            e.getPath(), id, parentId,
                             e.getPrimaryNodeTypeName(),
                             e.getMixinTypeNames(), e.getUserID());
                     events.add(serEvent);
@@ -829,7 +829,7 @@ public class ServerRepositoryService extends ServerObject implements RemoteRepos
      * {@inheritDoc}
      */
     public RemoteIterator getQNodeTypeDefinitions(RemoteSessionInfo sessionInfo,
-                                                 QName[] ntNames)
+                                                 Name[] ntNames)
             throws RepositoryException, RemoteException {
         Iterator it = service.getQNodeTypeDefinitions(getSessionInfo(sessionInfo), ntNames);
         return getQNodeTypeDefinitionIterator(it);
@@ -893,10 +893,10 @@ public class ServerRepositoryService extends ServerObject implements RemoteRepos
             if (filters[i] instanceof EventFilterImpl) {
                 EventFilterImpl e = (EventFilterImpl) filters[i];
                 Set nodeTypeNames = e.getNodeTypeNames();
-                QName[] ntNames = null;
+                Name[] ntNames = null;
                 if (nodeTypeNames != null) {
-                    ntNames = (QName[]) nodeTypeNames.toArray(
-                            new QName[nodeTypeNames.size()]);
+                    ntNames = (Name[]) nodeTypeNames.toArray(
+                            new Name[nodeTypeNames.size()]);
                 }
                 filters[i] = service.createEventFilter(sInfo,
                         e.getEventTypes(), e.getAbsPath(), e.isDeep(),
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/EventImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/EventImpl.java
index ac17025..ba20a1b 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/EventImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/EventImpl.java
@@ -18,7 +18,7 @@ package org.apache.jackrabbit.spi2dav;
 
 import org.apache.jackrabbit.spi.Event;
 import org.apache.jackrabbit.spi.ItemId;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.webdav.xml.DomUtil;
 import org.apache.jackrabbit.webdav.observation.ObservationConstants;
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/ItemInfoImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/ItemInfoImpl.java
index f39f9cb..66403a1 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/ItemInfoImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/ItemInfoImpl.java
@@ -18,16 +18,17 @@ package org.apache.jackrabbit.spi2dav;
 
 import org.apache.jackrabbit.spi.ItemInfo;
 import org.apache.jackrabbit.spi.NodeId;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.webdav.property.DavPropertySet;
 import org.apache.jackrabbit.webdav.property.DavProperty;
 import org.apache.jackrabbit.webdav.jcr.ItemResourceConstants;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
+import javax.jcr.NamespaceException;
+
 /**
  * <code>ItemInfoImpl</code>...
  */
@@ -38,14 +39,14 @@ abstract class ItemInfoImpl implements ItemInfo {
     private final NodeId parentId;
     private final Path path;
 
-    public ItemInfoImpl(NodeId parentId, DavPropertySet propSet, NamespaceResolver nsResolver)
-        throws MalformedPathException {
+    public ItemInfoImpl(NodeId parentId, DavPropertySet propSet, NamePathResolver resolver)
+            throws NameException, NamespaceException {
         // set parentId
         this.parentId = parentId;
 
         DavProperty pathProp = propSet.get(ItemResourceConstants.JCR_PATH);
         String jcrPath = pathProp.getValue().toString();
-        path = PathFormat.parse(jcrPath, nsResolver);
+        path = resolver.getQPath(jcrPath);
 
     }
 
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/NamespaceResolverImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/NamespaceResolverImpl.java
index 2287a2b..61d1bc1 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/NamespaceResolverImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/NamespaceResolverImpl.java
@@ -18,8 +18,7 @@ package org.apache.jackrabbit.spi2dav;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
-import org.apache.jackrabbit.name.AbstractNamespaceResolver;
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
 
 import javax.jcr.NamespaceException;
 import java.util.Map;
@@ -29,7 +28,7 @@ import java.util.Collections;
 /**
  * <code>NamespaceResolverImpl</code>...
  */
-class NamespaceResolverImpl extends AbstractNamespaceResolver {
+class NamespaceResolverImpl extends org.apache.jackrabbit.namespace.AbstractNamespaceResolver {
 
     private static Logger log = LoggerFactory.getLogger(NamespaceResolverImpl.class);
 
@@ -65,7 +64,7 @@ class NamespaceResolverImpl extends AbstractNamespaceResolver {
     }
 
     /**
-     * @see NamespaceResolver#getPrefix(String)
+     * @see org.apache.jackrabbit.namespace.NamespaceResolver#getPrefix(String)
      */
     public String getPrefix(String uri) throws NamespaceException {
         String prefix = (String) uriToPrefix.get(uri);
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/NodeInfoImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/NodeInfoImpl.java
index 0902a38..f6b18e8 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/NodeInfoImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/NodeInfoImpl.java
@@ -20,15 +20,14 @@ import org.apache.jackrabbit.webdav.jcr.nodetype.NodeTypeProperty;
 import org.apache.jackrabbit.webdav.jcr.ItemResourceConstants;
 import org.apache.jackrabbit.webdav.property.DavPropertySet;
 import org.apache.jackrabbit.webdav.property.DavProperty;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 import org.apache.jackrabbit.spi.NodeInfo;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.PropertyId;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -46,18 +45,18 @@ public class NodeInfoImpl extends ItemInfoImpl implements NodeInfo {
     private static Logger log = LoggerFactory.getLogger(NodeInfoImpl.class);
 
     private final NodeId id;
-    private final QName qName;
+    private final Name qName;
     private final int index;
 
-    private final QName primaryNodeTypeName;
-    private final QName[] mixinNodeTypeNames;
+    private final Name primaryNodeTypeName;
+    private final Name[] mixinNodeTypeNames;
 
     private final List references = new ArrayList();
     private final List propertyIds = new ArrayList();
 
     public NodeInfoImpl(NodeId id, NodeId parentId, DavPropertySet propSet,
-                        NamespaceResolver nsResolver) throws RepositoryException, MalformedPathException {
-        super(parentId, propSet, nsResolver);
+                        NamePathResolver resolver) throws RepositoryException, NameException {
+        super(parentId, propSet, resolver);
 
         // set id
         this.id = id;
@@ -71,17 +70,17 @@ public class NodeInfoImpl extends ItemInfoImpl implements NodeInfo {
                 // note, that unescaping is not required.
                 String jcrName = nameProp.getValue().toString();
                 try {
-                    qName = NameFormat.parse(jcrName, nsResolver);
+                    qName = resolver.getQName(jcrName);
                 } catch (NameException e) {
                     throw new RepositoryException("Unable to build ItemInfo object, invalid name found: " + jcrName);
                 }
             } else {
                 // root
-                qName = QName.ROOT;
+                qName = NameConstants.ROOT;
             }
         } else {
-            Path.PathElement el = id.getPath().getNameElement();
-            qName = (Path.ROOT_ELEMENT == el) ? QName.ROOT : el.getName();
+            Path.Element el = id.getPath().getNameElement();
+            qName = (el.denotesRoot()) ? NameConstants.ROOT : el.getName();
         }
 
         DavProperty indexProp = propSet.get(ItemResourceConstants.JCR_INDEX);
@@ -97,7 +96,7 @@ public class NodeInfoImpl extends ItemInfoImpl implements NodeInfo {
                 Iterator it = new NodeTypeProperty(propSet.get(ItemResourceConstants.JCR_PRIMARYNODETYPE)).getNodeTypeNames().iterator();
                 if (it.hasNext()) {
                     String jcrName = it.next().toString();
-                    primaryNodeTypeName = NameFormat.parse(jcrName, nsResolver);
+                    primaryNodeTypeName = resolver.getQName(jcrName);
                 } else {
                     throw new RepositoryException("Missing primary nodetype for node " + id + ".");
                 }
@@ -106,16 +105,16 @@ public class NodeInfoImpl extends ItemInfoImpl implements NodeInfo {
             }
             if (propSet.contains(ItemResourceConstants.JCR_MIXINNODETYPES)) {
                 Set mixinNames = new NodeTypeProperty(propSet.get(ItemResourceConstants.JCR_MIXINNODETYPES)).getNodeTypeNames();
-                mixinNodeTypeNames = new QName[mixinNames.size()];
+                mixinNodeTypeNames = new Name[mixinNames.size()];
                 Iterator it = mixinNames.iterator();
                 int i = 0;
                 while(it.hasNext()) {
                     String jcrName = it.next().toString();
-                    mixinNodeTypeNames[i] = NameFormat.parse(jcrName, nsResolver);
+                    mixinNodeTypeNames[i] = resolver.getQName(jcrName);
                     i++;
                 }
             } else {
-                mixinNodeTypeNames = QName.EMPTY_ARRAY;
+                mixinNodeTypeNames = Name.EMPTY_ARRAY;
             }
         } catch (NameException e) {
             throw new RepositoryException("Error while resolving nodetype names: " + e.getMessage());
@@ -127,7 +126,7 @@ public class NodeInfoImpl extends ItemInfoImpl implements NodeInfo {
         return true;
     }
 
-    public QName getQName() {
+    public Name getName() {
         return qName;
     }
 
@@ -140,11 +139,11 @@ public class NodeInfoImpl extends ItemInfoImpl implements NodeInfo {
         return index;
     }
 
-    public QName getNodetype() {
+    public Name getNodetype() {
         return primaryNodeTypeName;
     }
 
-    public QName[] getMixins() {
+    public Name[] getMixins() {
         return mixinNodeTypeNames;
     }
 
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/PropertyInfoImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/PropertyInfoImpl.java
index 956e12b..4739256 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/PropertyInfoImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/PropertyInfoImpl.java
@@ -20,15 +20,15 @@ import org.apache.jackrabbit.webdav.DavException;
 import org.apache.jackrabbit.webdav.jcr.property.ValuesProperty;
 import org.apache.jackrabbit.webdav.jcr.ItemResourceConstants;
 import org.apache.jackrabbit.webdav.property.DavPropertySet;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 import org.apache.jackrabbit.value.ValueFormat;
 import org.apache.jackrabbit.spi.PropertyId;
 import org.apache.jackrabbit.spi.PropertyInfo;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.QValueFactory;
+import org.apache.jackrabbit.spi.Name;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -52,11 +52,11 @@ public class PropertyInfoImpl extends ItemInfoImpl implements PropertyInfo {
     private QValue[] values;
 
     public PropertyInfoImpl(PropertyId id, NodeId parentId, DavPropertySet propSet,
-                            NamespaceResolver nsResolver, ValueFactory valueFactory,
+                            NamePathResolver resolver, ValueFactory valueFactory,
                             QValueFactory qValueFactory)
-        throws RepositoryException, DavException, IOException, MalformedPathException {
+            throws RepositoryException, DavException, IOException, NameException {
 
-        super(parentId, propSet, nsResolver);
+        super(parentId, propSet, resolver);
         // set id
         this.id = id;
 
@@ -76,7 +76,7 @@ public class PropertyInfoImpl extends ItemInfoImpl implements PropertyInfo {
                 if (type == PropertyType.BINARY) {
                     qv = qValueFactory.create(jcrValue.getStream());
                 } else {
-                    qv = ValueFormat.getQValue(jcrValue, nsResolver, qValueFactory);
+                    qv = ValueFormat.getQValue(jcrValue, resolver, qValueFactory);
                 }
                 values = new QValue[] {qv};
             }
@@ -89,7 +89,7 @@ public class PropertyInfoImpl extends ItemInfoImpl implements PropertyInfo {
                 if (type == PropertyType.BINARY) {
                     values[i] = qValueFactory.create(jcrValues[i].getStream());
                 } else {
-                    values[i] = ValueFormat.getQValue(jcrValues[i], nsResolver, qValueFactory);
+                    values[i] = ValueFormat.getQValue(jcrValues[i], resolver, qValueFactory);
                 }
             }
         }
@@ -100,8 +100,8 @@ public class PropertyInfoImpl extends ItemInfoImpl implements PropertyInfo {
         return false;
     }
 
-    public QName getQName() {
-        return id.getQName();
+    public Name getName() {
+        return id.getName();
     }
 
     //-------------------------------------------------------< PropertyInfo >---
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QItemDefinitionImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QItemDefinitionImpl.java
index c3768ea..01cf3b9 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QItemDefinitionImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QItemDefinitionImpl.java
@@ -17,13 +17,14 @@
 package org.apache.jackrabbit.spi2dav;
 
 import org.w3c.dom.Element;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.name.NameFactoryImpl;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.jackrabbit.spi.QItemDefinition;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.webdav.jcr.nodetype.NodeTypeConstants;
 
 import javax.jcr.version.OnParentVersionAction;
@@ -39,17 +40,17 @@ public abstract class QItemDefinitionImpl implements QItemDefinition, NodeTypeCo
     /**
      * The special wildcard name used as the name of residual item definitions.
      */
-    public static final QName ANY_NAME = new QName("", "*");
+    public static final Name ANY_NAME = NameFactoryImpl.getInstance().create("", "*");
 
     /**
      * The name of the child item.
      */
-    private final QName name;
+    private final Name name;
 
     /**
      * The name of the declaring node type.
      */
-    private final QName declaringNodeType;
+    private final Name declaringNodeType;
 
     /**
      * The 'autoCreated' flag.
@@ -80,15 +81,15 @@ public abstract class QItemDefinitionImpl implements QItemDefinition, NodeTypeCo
      *
      * @param declaringNodeType
      * @param itemDefElement
-     * @param nsResolver
+     * @param resolver
      * @throws RepositoryException
      */
-    QItemDefinitionImpl(QName declaringNodeType, Element itemDefElement, NamespaceResolver nsResolver)
+    QItemDefinitionImpl(Name declaringNodeType, Element itemDefElement, NamePathResolver resolver)
         throws RepositoryException {
         try {
             String attr = itemDefElement.getAttribute(DECLARINGNODETYPE_ATTRIBUTE);
             if (attr != null) {
-                QName dnt = NameFormat.parse(attr, nsResolver);
+                Name dnt = resolver.getQName(attr);
                 if (declaringNodeType != null && !declaringNodeType.equals(dnt)) {
                     throw new RepositoryException("Declaring nodetype mismatch: In element = '" + dnt + "', Declaring nodetype = '" + declaringNodeType + "'");
                 }
@@ -100,9 +101,9 @@ public abstract class QItemDefinitionImpl implements QItemDefinition, NodeTypeCo
             if (itemDefElement.hasAttribute(NAME_ATTRIBUTE)) {
                 String nAttr = itemDefElement.getAttribute(NAME_ATTRIBUTE);
                 if (nAttr.length() > 0) {
-                    name = (isAnyName(nAttr)) ? ANY_NAME : NameFormat.parse(nAttr, nsResolver);
+                    name = (isAnyName(nAttr)) ? ANY_NAME : resolver.getQName(nAttr);
                 } else {
-                    name = QName.ROOT;
+                    name = NameConstants.ROOT;
                 }
             } else {
                 // TODO: check if correct..
@@ -139,14 +140,14 @@ public abstract class QItemDefinitionImpl implements QItemDefinition, NodeTypeCo
     /**
      * {@inheritDoc}
      */
-    public QName getDeclaringNodeType() {
+    public Name getDeclaringNodeType() {
         return declaringNodeType;
     }
 
     /**
      * {@inheritDoc}
      */
-    public QName getQName() {
+    public Name getName() {
         return name;
     }
 
@@ -205,7 +206,7 @@ public abstract class QItemDefinitionImpl implements QItemDefinition, NodeTypeCo
             return (declaringNodeType == null
                     ? other.getDeclaringNodeType() == null
                     : declaringNodeType.equals(other.getDeclaringNodeType()))
-                    && (name == null ? other.getQName() == null : name.equals(other.getQName()))
+                    && (name == null ? other.getName() == null : name.equals(other.getName()))
                     && autoCreated == other.isAutoCreated()
                     && onParentVersion == other.getOnParentVersion()
                     && writeProtected == other.isProtected()
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QNodeDefinitionImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QNodeDefinitionImpl.java
index 65df7a8..db98731 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QNodeDefinitionImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QNodeDefinitionImpl.java
@@ -17,11 +17,11 @@
 package org.apache.jackrabbit.spi2dav;
 
 import org.w3c.dom.Element;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameException;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 import org.apache.jackrabbit.spi.QNodeDefinition;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.jackrabbit.webdav.xml.DomUtil;
 import org.apache.jackrabbit.webdav.xml.ElementIterator;
 
@@ -40,12 +40,12 @@ public class QNodeDefinitionImpl extends QItemDefinitionImpl implements QNodeDef
     /**
      * The name of the default primary type.
      */
-    private final QName defaultPrimaryType;
+    private final Name defaultPrimaryType;
 
     /**
      * The names of the required primary types.
      */
-    private final QName[] requiredPrimaryTypes;
+    private final Name[] requiredPrimaryTypes;
 
     /**
      * The 'allowsSameNameSiblings' flag.
@@ -57,18 +57,18 @@ public class QNodeDefinitionImpl extends QItemDefinitionImpl implements QNodeDef
      *
      * @param declaringNodeType
      * @param ndefElement
-     * @param nsResolver
+     * @param resolver
      * @throws RepositoryException
      */
-    QNodeDefinitionImpl(QName declaringNodeType, Element ndefElement, NamespaceResolver nsResolver)
+    QNodeDefinitionImpl(Name declaringNodeType, Element ndefElement, NamePathResolver resolver)
         throws RepositoryException  {
-        super(declaringNodeType, ndefElement, nsResolver);
+        super(declaringNodeType, ndefElement, resolver);
         // TODO: webdav server sends jcr names -> nsResolver required. improve this.
         // NOTE: the server should send the namespace-mappings as addition ns-defininitions
         try {
 
             if (ndefElement.hasAttribute(DEFAULTPRIMARYTYPE_ATTRIBUTE)) {
-                defaultPrimaryType = NameFormat.parse(ndefElement.getAttribute(DEFAULTPRIMARYTYPE_ATTRIBUTE), nsResolver);
+                defaultPrimaryType = resolver.getQName(ndefElement.getAttribute(DEFAULTPRIMARYTYPE_ATTRIBUTE));
             } else {
                 defaultPrimaryType = null;
             }
@@ -78,11 +78,11 @@ public class QNodeDefinitionImpl extends QItemDefinitionImpl implements QNodeDef
                 List qNames = new ArrayList();
                 ElementIterator it = DomUtil.getChildren(reqPrimaryTypes, REQUIREDPRIMARYTYPE_ELEMENT, null);
                 while (it.hasNext()) {
-                    qNames.add(NameFormat.parse(DomUtil.getTextTrim(it.nextElement()), nsResolver));
+                    qNames.add(resolver.getQName(DomUtil.getTextTrim(it.nextElement())));
                 }
-                requiredPrimaryTypes = (QName[]) qNames.toArray(new QName[qNames.size()]);
+                requiredPrimaryTypes = (Name[]) qNames.toArray(new Name[qNames.size()]);
             } else {
-                requiredPrimaryTypes = new QName[] { QName.NT_BASE };
+                requiredPrimaryTypes = new Name[] { NameConstants.NT_BASE };
             }
 
             if (ndefElement.hasAttribute(SAMENAMESIBLINGS_ATTRIBUTE)) {
@@ -99,14 +99,14 @@ public class QNodeDefinitionImpl extends QItemDefinitionImpl implements QNodeDef
     /**
      * {@inheritDoc}
      */
-    public QName getDefaultPrimaryType() {
+    public Name getDefaultPrimaryType() {
         return defaultPrimaryType;
     }
 
     /**
      * {@inheritDoc}
      */
-    public QName[] getRequiredPrimaryTypes() {
+    public Name[] getRequiredPrimaryTypes() {
         return requiredPrimaryTypes;
     }
 
@@ -170,12 +170,12 @@ public class QNodeDefinitionImpl extends QItemDefinitionImpl implements QNodeDef
             if (definesResidual()) {
                 sb.append('*');
             } else {
-                sb.append(getQName().toString());
+                sb.append(getName().toString());
             }
             sb.append('/');
             // set of required node type names, sorted in ascending order
             TreeSet set = new TreeSet();
-            QName[] names = getRequiredPrimaryTypes();
+            Name[] names = getRequiredPrimaryTypes();
             for (int i = 0; i < names.length; i++) {
                 set.add(names[i]);
             }
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QNodeTypeDefinitionImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QNodeTypeDefinitionImpl.java
index 047456b..3cdce1b 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QNodeTypeDefinitionImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QNodeTypeDefinitionImpl.java
@@ -20,14 +20,15 @@ import org.w3c.dom.Element;
 import org.apache.jackrabbit.webdav.jcr.nodetype.NodeTypeConstants;
 import org.apache.jackrabbit.webdav.xml.DomUtil;
 import org.apache.jackrabbit.webdav.xml.ElementIterator;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameException;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 import org.apache.jackrabbit.spi.QNodeTypeDefinition;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.name.NameFactoryImpl;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QValueFactory;
+import org.apache.jackrabbit.spi.Name;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -47,11 +48,11 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
 
     private static Logger log = LoggerFactory.getLogger(QNodeTypeDefinitionImpl.class);
 
-    private final QName name;
-    private final QName[] supertypes;
+    private final Name name;
+    private final Name[] supertypes;
     private final boolean mixin;
     private final boolean orderableChildNodes;
-    private final QName primaryItemName;
+    private final Name primaryItemName;
     private final QPropertyDefinition[] propDefs;
     private final QNodeDefinition[] nodeDefs;
     private Set dependencies;
@@ -59,20 +60,20 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
     /**
      * Default constructor.
      */
-    public QNodeTypeDefinitionImpl(Element ntdElement, NamespaceResolver nsResolver,
+    public QNodeTypeDefinitionImpl(Element ntdElement, NamePathResolver resolver,
                                    QValueFactory qValueFactory)
         throws RepositoryException {
         // TODO: webdav-server currently sends jcr-names -> conversion needed
         // NOTE: the server should send the namespace-mappings as addition ns-defininitions
         try {
         if (ntdElement.hasAttribute(NAME_ATTRIBUTE)) {
-            name = NameFormat.parse(ntdElement.getAttribute(NAME_ATTRIBUTE), nsResolver);
+            name = resolver.getQName(ntdElement.getAttribute(NAME_ATTRIBUTE));
         } else {
             name = null;
         }
 
         if (ntdElement.hasAttribute(PRIMARYITEMNAME_ATTRIBUTE)) {
-            primaryItemName = NameFormat.parse(ntdElement.getAttribute(PRIMARYITEMNAME_ATTRIBUTE), nsResolver);
+            primaryItemName = resolver.getQName(ntdElement.getAttribute(PRIMARYITEMNAME_ATTRIBUTE));
         } else {
             primaryItemName = null;
         }
@@ -82,12 +83,12 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
             ElementIterator stIter = DomUtil.getChildren(child, SUPERTYPE_ELEMENT, null);
             List qNames = new ArrayList();
             while (stIter.hasNext()) {
-                QName st = NameFormat.parse(DomUtil.getTextTrim(stIter.nextElement()), nsResolver);
+                Name st = resolver.getQName(DomUtil.getTextTrim(stIter.nextElement()));
                 qNames.add(st);
             }
-            supertypes = (QName[]) qNames.toArray(new QName[qNames.size()]);
+            supertypes = (Name[]) qNames.toArray(new Name[qNames.size()]);
         } else {
-            supertypes = QName.EMPTY_ARRAY;
+            supertypes = Name.EMPTY_ARRAY;
         }
         if (ntdElement.hasAttribute(ISMIXIN_ATTRIBUTE)) {
             mixin = Boolean.valueOf(ntdElement.getAttribute(ISMIXIN_ATTRIBUTE)).booleanValue();
@@ -104,7 +105,7 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
         ElementIterator it = DomUtil.getChildren(ntdElement, CHILDNODEDEFINITION_ELEMENT, null);
         List itemDefs = new ArrayList();
         while (it.hasNext()) {
-            itemDefs.add(new QNodeDefinitionImpl(name, it.nextElement(), nsResolver));
+            itemDefs.add(new QNodeDefinitionImpl(name, it.nextElement(), resolver));
         }
         nodeDefs = (QNodeDefinition[]) itemDefs.toArray(new QNodeDefinition[itemDefs.size()]);
 
@@ -113,7 +114,7 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
         it = DomUtil.getChildren(ntdElement, PROPERTYDEFINITION_ELEMENT, null);
         itemDefs = new ArrayList();
         while (it.hasNext()) {
-            itemDefs.add(new QPropertyDefinitionImpl(name, it.nextElement(), nsResolver, qValueFactory));
+            itemDefs.add(new QPropertyDefinitionImpl(name, it.nextElement(), resolver, qValueFactory));
         }
         propDefs = (QPropertyDefinition[]) itemDefs.toArray(new QPropertyDefinition[itemDefs.size()]);
         } catch (NameException e) {
@@ -124,21 +125,21 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
 
     //------------------------------------------------< QNodeTypeDefinition >---
     /**
-     * @see QNodeTypeDefinition#getQName() 
+     * @see QNodeTypeDefinition#getName()
      */
-    public QName getQName() {
+    public Name getName() {
         return name;
     }
 
     /**
      * @see QNodeTypeDefinition#getSupertypes()
      */
-    public QName[] getSupertypes() {
+    public Name[] getSupertypes() {
         if (supertypes.length > 0
-                || isMixin() || QName.NT_BASE.equals(getQName())) {
+                || isMixin() || NameConstants.NT_BASE.equals(getName())) {
             return supertypes;
         } else {
-            return new QName[] { QName.NT_BASE };
+            return new Name[] { NameConstants.NT_BASE };
         }
     }
 
@@ -159,7 +160,7 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
     /**
      * @see QNodeTypeDefinition#getPrimaryItemName()
      */
-    public QName getPrimaryItemName() {
+    public Name getPrimaryItemName() {
         return primaryItemName;
     }
 
@@ -190,12 +191,12 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
             // child node definitions
             for (int i = 0; i < nodeDefs.length; i++) {
                 // default primary type
-                QName ntName = nodeDefs[i].getDefaultPrimaryType();
+                Name ntName = nodeDefs[i].getDefaultPrimaryType();
                 if (ntName != null && !name.equals(ntName)) {
                     dependencies.add(ntName);
                 }
                 // required primary type
-                QName[] ntNames = nodeDefs[i].getRequiredPrimaryTypes();
+                Name[] ntNames = nodeDefs[i].getRequiredPrimaryTypes();
                 for (int j = 0; j < ntNames.length; j++) {
                     if (ntNames[j] != null && !name.equals(ntNames[j])) {
                         dependencies.add(ntNames[j]);
@@ -209,7 +210,8 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
                     String[] ca = propDefs[i].getValueConstraints();
                     if (ca != null) {
                         for (int j = 0; j < ca.length; j++) {
-                            QName ntName = QName.valueOf(ca[j]);
+                            // TODO: don't rely on a specific factory
+                            Name ntName = NameFactoryImpl.getInstance().create(ca[j]);
                             if (!name.equals(ntName)) {
                                 dependencies.add(ntName);
                             }
@@ -231,7 +233,7 @@ public class QNodeTypeDefinitionImpl implements QNodeTypeDefinition, NodeTypeCon
         }
         if (obj instanceof QNodeTypeDefinition) {
             QNodeTypeDefinition other = (QNodeTypeDefinition) obj;
-            return (name == null ? other.getQName() == null : name.equals(other.getQName()))
+            return (name == null ? other.getName() == null : name.equals(other.getName()))
                 && (primaryItemName == null ? other.getPrimaryItemName() == null : primaryItemName.equals(other.getPrimaryItemName()))
                 && Arrays.equals(supertypes, other.getSupertypes())
                 && mixin == other.isMixin()
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QPropertyDefinitionImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QPropertyDefinitionImpl.java
index 8b0fcfc..601d80b 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QPropertyDefinitionImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QPropertyDefinitionImpl.java
@@ -17,15 +17,15 @@
 package org.apache.jackrabbit.spi2dav;
 
 import org.w3c.dom.Element;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.QValueFactory;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.webdav.xml.DomUtil;
 import org.apache.jackrabbit.webdav.xml.ElementIterator;
 import org.apache.jackrabbit.value.ValueFormat;
 import org.apache.jackrabbit.value.ValueFactoryImplEx;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
@@ -70,12 +70,12 @@ public class QPropertyDefinitionImpl extends QItemDefinitionImpl implements QPro
     /**
      * Default constructor.
      */
-    QPropertyDefinitionImpl(QName declaringNodeType, Element pdefElement,
-                            NamespaceResolver nsResolver, QValueFactory qValueFactory)
+    QPropertyDefinitionImpl(Name declaringNodeType, Element pdefElement,
+                            NamePathResolver resolver, QValueFactory qValueFactory)
         throws RepositoryException {
         // TODO: webdav server sends jcr names -> nsResolver required. improve this.
         // NOTE: the server should send the namespace-mappings as addition ns-defininitions
-        super(declaringNodeType, pdefElement, nsResolver);
+        super(declaringNodeType, pdefElement, resolver);
 
         if (pdefElement.hasAttribute(REQUIREDTYPE_ATTRIBUTE)) {
             requiredType = PropertyType.valueFromName(pdefElement.getAttribute(REQUIREDTYPE_ATTRIBUTE));
@@ -102,9 +102,9 @@ public class QPropertyDefinitionImpl extends QItemDefinitionImpl implements QPro
                 if (requiredType == PropertyType.BINARY) {
                     // TODO: improve
                     Value v = ValueFactoryImplEx.getInstance().createValue(jcrVal, requiredType);
-                    qValue = ValueFormat.getQValue(v, nsResolver, qValueFactory);
+                    qValue = ValueFormat.getQValue(v, resolver, qValueFactory);
                 } else {
-                    qValue = ValueFormat.getQValue(jcrVal, requiredType, nsResolver, qValueFactory);
+                    qValue = ValueFormat.getQValue(jcrVal, requiredType, resolver, qValueFactory);
                 }
                 vs.add(qValue);
             }
@@ -123,7 +123,7 @@ public class QPropertyDefinitionImpl extends QItemDefinitionImpl implements QPro
                 // in case of name and path constraint, the value must be
                 // converted to be in qualified format
                 if (constType == PropertyType.NAME || constType == PropertyType.PATH) {
-                   qValue = ValueFormat.getQValue(qValue, constType, nsResolver, qValueFactory).getString();
+                   qValue = ValueFormat.getQValue(qValue, constType, resolver, qValueFactory).getString();
                 }
                 vc.add(qValue);
             }
@@ -210,7 +210,7 @@ public class QPropertyDefinitionImpl extends QItemDefinitionImpl implements QPro
             if (definesResidual()) {
                 sb.append('*');
             } else {
-                sb.append(getQName().toString());
+                sb.append(getName().toString());
             }
             sb.append('/');
             sb.append(getRequiredType());
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QueryInfoImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QueryInfoImpl.java
index 404fcd4..2a59f0c 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QueryInfoImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/QueryInfoImpl.java
@@ -25,16 +25,14 @@ import javax.jcr.ValueFactory;
 import javax.jcr.Value;
 import javax.jcr.RangeIterator;
 
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.QueryInfo;
 import org.apache.jackrabbit.spi.SessionInfo;
 import org.apache.jackrabbit.spi.QValueFactory;
 import org.apache.jackrabbit.spi.QueryResultRow;
 import org.apache.jackrabbit.spi.QValue;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.util.ISO9075;
 import org.apache.jackrabbit.webdav.DavServletResponse;
 import org.apache.jackrabbit.webdav.MultiStatus;
@@ -43,6 +41,8 @@ import org.apache.jackrabbit.webdav.jcr.search.SearchResultProperty;
 import org.apache.jackrabbit.webdav.property.DavProperty;
 import org.apache.jackrabbit.webdav.property.DavPropertySet;
 import org.apache.jackrabbit.value.ValueFormat;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
@@ -58,24 +58,24 @@ public class QueryInfoImpl implements QueryInfo {
 
     private static final double UNDEFINED_SCORE = -1;
 
-    private final QName[] columnNames;
+    private final Name[] columnNames;
     private int scoreIndex = -1;
     private final Map results = new LinkedHashMap();
 
     public QueryInfoImpl(MultiStatus ms, SessionInfo sessionInfo, URIResolver uriResolver,
-                         NamespaceResolver nsResolver, ValueFactory valueFactory,
+                         NamePathResolver resolver, ValueFactory valueFactory,
                          QValueFactory qValueFactory)
         throws RepositoryException {
 
         String responseDescription = ms.getResponseDescription();
         if (responseDescription != null) {
             String[] cn = responseDescription.split(" ");
-            this.columnNames = new QName[cn.length];
+            this.columnNames = new Name[cn.length];
             for (int i = 0; i < cn.length; i++) {
                 String jcrColumnNames = ISO9075.decode(cn[i]);
                 try {
-                    columnNames[i] = NameFormat.parse(jcrColumnNames, nsResolver);
-                    if (QName.JCR_SCORE.equals(columnNames[i])) {
+                    columnNames[i] = resolver.getQName(jcrColumnNames);
+                    if (NameConstants.JCR_SCORE.equals(columnNames[i])) {
                         scoreIndex = i;
                     }
                 } catch (NameException e) {
@@ -98,7 +98,7 @@ public class QueryInfoImpl implements QueryInfo {
             QValue[] qValues = new QValue[values.length];
             for (int j = 0; j < values.length; j++) {
                 try {
-                    qValues[j] = (values[j] == null) ?  null : ValueFormat.getQValue(values[j], nsResolver, qValueFactory);
+                    qValues[j] = (values[j] == null) ?  null : ValueFormat.getQValue(values[j], resolver, qValueFactory);
                 } catch (RepositoryException e) {
                     // should not occur
                     log.error("Malformed value: " + values[j].toString());
@@ -120,7 +120,7 @@ public class QueryInfoImpl implements QueryInfo {
     /**
      * @see QueryInfo#getColumnNames()
      */
-    public QName[] getColumnNames() {
+    public Name[] getColumnNames() {
         return columnNames;
     }
 
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/RepositoryServiceImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/RepositoryServiceImpl.java
index 2e1bf0c..716618a 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/RepositoryServiceImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/RepositoryServiceImpl.java
@@ -99,16 +99,17 @@ import org.apache.jackrabbit.webdav.jcr.property.ValuesProperty;
 import org.apache.jackrabbit.webdav.jcr.property.NamespacesProperty;
 import org.apache.jackrabbit.webdav.jcr.observation.SubscriptionImpl;
 import org.apache.jackrabbit.webdav.jcr.ItemResourceConstants;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
-import org.apache.jackrabbit.name.AbstractNamespaceResolver;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
+import org.apache.jackrabbit.namespace.AbstractNamespaceResolver;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.ParsingNameResolver;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.conversion.PathResolver;
+import org.apache.jackrabbit.conversion.ParsingPathResolver;
+import org.apache.jackrabbit.conversion.IllegalNameException;
+import org.apache.jackrabbit.conversion.MalformedPathException;
 import org.apache.jackrabbit.spi.Batch;
 import org.apache.jackrabbit.spi.RepositoryService;
 import org.apache.jackrabbit.spi.SessionInfo;
@@ -130,6 +131,10 @@ import org.apache.jackrabbit.spi.ChildInfo;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.QValueFactory;
 import org.apache.jackrabbit.spi.NodeInfo;
+import org.apache.jackrabbit.spi.PathFactory;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.commons.EventFilterImpl;
 import org.apache.jackrabbit.spi.commons.EventBundleImpl;
 import org.apache.jackrabbit.spi.commons.ChildInfoImpl;
@@ -198,6 +203,8 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     private static final SubscriptionInfo S_INFO = new SubscriptionInfo(ALL_EVENTS, true, DavConstants.INFINITE_TIMEOUT);
 
     private final IdFactory idFactory;
+    private final NameFactory nameFactory;
+    private final PathFactory pathFactory;
     private final ValueFactory valueFactory;
 
     private final Document domFactory;
@@ -212,7 +219,10 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
 
     private Map descriptors;
 
-    public RepositoryServiceImpl(String uri, IdFactory idFactory, ValueFactory valueFactory) throws RepositoryException {
+    public RepositoryServiceImpl(String uri, IdFactory idFactory,
+                                 NameFactory nameFactory,
+                                 PathFactory pathFactory,
+                                 ValueFactory valueFactory) throws RepositoryException {
         if (uri == null || "".equals(uri)) {
             throw new RepositoryException("Invalid repository uri '" + uri + "'.");
         }
@@ -220,6 +230,8 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
             throw new RepositoryException("IdFactory and ValueFactory may not be null.");
         }
         this.idFactory = idFactory;
+        this.nameFactory = nameFactory;
+        this.pathFactory = pathFactory;
         this.valueFactory = valueFactory;
 
         try {
@@ -234,7 +246,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
             hostConfig.setHost(repositoryUri);
 
             nsCache = new NamespaceCache();
-            uriResolver = new URIResolverImpl(repositoryUri, this, nsCache, domFactory);
+            uriResolver = new URIResolverImpl(repositoryUri, this, new NamePathResolverImpl(nsCache), domFactory);
 
         } catch (URIException e) {
             throw new RepositoryException(e);
@@ -314,14 +326,10 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
         return uriResolver.getItemUri(itemId, sessionInfo.getWorkspaceName(), sessionInfo);
     }
 
-    private String getItemUri(NodeId parentId, QName childName, SessionInfo sessionInfo) throws RepositoryException {
+    private String getItemUri(NodeId parentId, Name childName, SessionInfo sessionInfo) throws RepositoryException {
         String parentUri = uriResolver.getItemUri(parentId, sessionInfo.getWorkspaceName(), sessionInfo);
-        try {
-            NamespaceResolver resolver = new NamespaceResolverImpl(sessionInfo);
-            return parentUri + "/" + Text.escape(NameFormat.format(childName, resolver));
-        } catch (NoPrefixDeclaredException e) {
-            throw new RepositoryException(e);
-        }
+        NamePathResolver resolver = new NamePathResolverImpl(sessionInfo);
+        return parentUri + "/" + Text.escape(resolver.getJCRName(childName));
     }
 
     private NodeId getParentId(DavPropertySet propSet, SessionInfo sessionInfo)
@@ -345,19 +353,19 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
         }
     }
 
-    QName getQName(DavPropertySet propSet, NamespaceResolver nsResolver) throws RepositoryException {
+    Name getQName(DavPropertySet propSet, NamePathResolver resolver) throws RepositoryException {
         DavProperty nameProp = propSet.get(ItemResourceConstants.JCR_NAME);
         if (nameProp != null && nameProp.getValue() != null) {
             // not root node. Note that 'unespacing' is not required since
             // the jcr:name property does not provide the value in escaped form.
             String jcrName = nameProp.getValue().toString();
             try {
-                return NameFormat.parse(jcrName, nsResolver);
-            } catch (NameException e) {
+                return resolver.getQName(jcrName);
+            } catch (org.apache.jackrabbit.conversion.NameException e) {
                 throw new RepositoryException(e);
             }
         } else {
-            return QName.ROOT;
+            return NameConstants.ROOT;
         }
     }
 
@@ -404,6 +412,20 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
         return idFactory;
     }
 
+    /**
+     * @see RepositoryService#getNameFactory()
+     */
+    public NameFactory getNameFactory() {
+        return nameFactory;
+    }
+
+    /**
+     * @see RepositoryService#getPathFactory()
+     */
+    public PathFactory getPathFactory() {
+        return pathFactory;
+    }
+
     public QValueFactory getQValueFactory() {
         return QValueFactoryImpl.getInstance();
     }
@@ -657,7 +679,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 throw new RepositoryException("Internal error: requested node definition and got property definition.");
             }
 
-            NamespaceResolver resolver = new NamespaceResolverImpl(sessionInfo);
+            NamePathResolver resolver = new NamePathResolverImpl(sessionInfo);
 
             // build the definition
             QItemDefinition definition = null;
@@ -761,7 +783,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 throw new ItemNotFoundException("No node for id " + nodeId);
             }
 
-            NamespaceResolver resolver = new NamespaceResolverImpl(sessionInfo);
+            NamePathResolver resolver = new NamePathResolverImpl(sessionInfo);
             NodeId parentId = getParentId(propSet, sessionInfo);
 
             NodeInfoImpl nInfo = buildNodeInfo(nodeResponse, parentId, propSet, sessionInfo, resolver);
@@ -783,7 +805,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
             throw new RepositoryException(e);
         } catch (DavException e) {
             throw ExceptionConverter.generate(e);
-        } catch (MalformedPathException e) {
+        } catch (NameException e) {
             throw new RepositoryException(e);
         } finally {
             if (method != null) {
@@ -807,7 +829,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     private NodeInfoImpl buildNodeInfo(MultiStatusResponse nodeResponse,
                                        NodeId parentId, DavPropertySet propSet,
                                        SessionInfo sessionInfo,
-                                       NamespaceResolver resolver) throws MalformedPathException, RepositoryException {
+                                       NamePathResolver resolver) throws NameException, RepositoryException {
         NodeId id = uriResolver.buildNodeId(parentId, nodeResponse, sessionInfo.getWorkspaceName());
         NodeInfoImpl nInfo = new NodeInfoImpl(id, parentId, propSet, resolver);
         if (propSet.contains(ItemResourceConstants.JCR_REFERENCES)) {
@@ -853,7 +875,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 return Collections.EMPTY_LIST.iterator();
             }
 
-            NamespaceResolver resolver = new NamespaceResolverImpl(sessionInfo);
+            NamePathResolver resolver = new NamePathResolverImpl(sessionInfo);
 
             List childEntries = new ArrayList();
             for (int i = 0; i < responses.length; i++) {
@@ -863,7 +885,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                     if (childProps.contains(DavPropertyName.RESOURCETYPE) &&
                         childProps.get(DavPropertyName.RESOURCETYPE).getValue() != null) {
 
-                        QName qName = getQName(childProps, resolver);
+                        Name qName = getQName(childProps, resolver);
                         int index = getIndex(childProps);
                         String uuid = getUniqueID(childProps);
 
@@ -914,7 +936,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
             NodeId parentId = getParentId(propSet, sessionInfo);
             PropertyId id = uriResolver.buildPropertyId(parentId, responses[0], sessionInfo.getWorkspaceName());
 
-            NamespaceResolver resolver = new NamespaceResolverImpl(sessionInfo);
+            NamePathResolver resolver = new NamePathResolverImpl(sessionInfo);
             PropertyInfo pInfo = new PropertyInfoImpl(id, parentId, propSet,
                     resolver, valueFactory, getQValueFactory());
             return pInfo;
@@ -922,7 +944,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
             throw new RepositoryException(e);
         } catch (DavException e) {
             throw ExceptionConverter.generate(e);
-        } catch (MalformedPathException e) {
+        } catch (NameException e) {
             throw new RepositoryException(e);
         } finally {
             if (method != null) {
@@ -988,7 +1010,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
      */
     public void importXml(SessionInfo sessionInfo, NodeId parentId, InputStream xmlStream, int uuidBehaviour) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
         // TODO: improve. currently random name is built instead of retrieving name of new resource from top-level xml element within stream
-        QName nodeName = new QName(QName.NS_DEFAULT_URI, UUID.randomUUID().toString());
+        Name nodeName = getNameFactory().create(Name.NS_DEFAULT_URI, UUID.randomUUID().toString());
         String uri = getItemUri(parentId, nodeName, sessionInfo);
         MkColMethod method = new MkColMethod(uri);
         method.addRequestHeader(ItemResourceConstants.IMPORT_UUID_BEHAVIOR, new Integer(uuidBehaviour).toString());
@@ -997,9 +1019,9 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     }
 
     /**
-     * @see RepositoryService#move(SessionInfo, NodeId, NodeId, QName)
+     * @see RepositoryService#move(SessionInfo, NodeId, NodeId, Name)
      */
-    public void move(SessionInfo sessionInfo, NodeId srcNodeId, NodeId destParentNodeId, QName destName) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+    public void move(SessionInfo sessionInfo, NodeId srcNodeId, NodeId destParentNodeId, Name destName) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
         String uri = getItemUri(srcNodeId, sessionInfo);
         String destUri = getItemUri(destParentNodeId, destName, sessionInfo);
         MoveMethod method = new MoveMethod(uri, destUri, true);
@@ -1007,9 +1029,9 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     }
 
     /**
-     * @see RepositoryService#copy(SessionInfo, String, NodeId, NodeId, QName)
+     * @see RepositoryService#copy(SessionInfo, String, NodeId, NodeId, Name)
      */
-    public void copy(SessionInfo sessionInfo, String srcWorkspaceName, NodeId srcNodeId, NodeId destParentNodeId, QName destName) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
+    public void copy(SessionInfo sessionInfo, String srcWorkspaceName, NodeId srcNodeId, NodeId destParentNodeId, Name destName) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
         String uri = uriResolver.getItemUri(srcNodeId, srcWorkspaceName, sessionInfo);
         String destUri = getItemUri(destParentNodeId, destName, sessionInfo);
         CopyMethod method = new CopyMethod(uri, destUri, true, false);
@@ -1027,9 +1049,9 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     }
 
     /**
-     * @see RepositoryService#clone(SessionInfo, String, NodeId, NodeId, QName, boolean)
+     * @see RepositoryService#clone(SessionInfo, String, NodeId, NodeId, Name, boolean)
      */
-    public void clone(SessionInfo sessionInfo, String srcWorkspaceName, NodeId srcNodeId, NodeId destParentNodeId, QName destName, boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
+    public void clone(SessionInfo sessionInfo, String srcWorkspaceName, NodeId srcNodeId, NodeId destParentNodeId, Name destName, boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
         // TODO: missing implementation
         throw new UnsupportedOperationException("Missing implementation");
     }
@@ -1290,34 +1312,30 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     }
 
     /**
-     * @see RepositoryService#addVersionLabel(SessionInfo,NodeId,NodeId,QName,boolean)
+     * @see RepositoryService#addVersionLabel(SessionInfo,NodeId,NodeId,Name,boolean)
      */
-    public void addVersionLabel(SessionInfo sessionInfo, NodeId versionHistoryId, NodeId versionId, QName label, boolean moveLabel) throws VersionException, RepositoryException {
+    public void addVersionLabel(SessionInfo sessionInfo, NodeId versionHistoryId, NodeId versionId, Name label, boolean moveLabel) throws VersionException, RepositoryException {
         try {
             String uri = getItemUri(versionId, sessionInfo);
-            String strLabel = NameFormat.format(label, new NamespaceResolverImpl(sessionInfo));
+            String strLabel = new NamePathResolverImpl(sessionInfo).getJCRName(label);
             LabelMethod method = new LabelMethod(uri, strLabel, (moveLabel) ? LabelInfo.TYPE_SET : LabelInfo.TYPE_ADD);
             execute(method, sessionInfo);
         } catch (IOException e) {
             throw new RepositoryException(e);
-        } catch (NoPrefixDeclaredException e) {
-            throw new RepositoryException(e);
         }
     }
 
     /**
-     * @see RepositoryService#removeVersionLabel(SessionInfo,NodeId,NodeId,QName)
+     * @see RepositoryService#removeVersionLabel(SessionInfo,NodeId,NodeId,Name)
      */
-    public void removeVersionLabel(SessionInfo sessionInfo, NodeId versionHistoryId, NodeId versionId, QName label) throws VersionException, RepositoryException {
+    public void removeVersionLabel(SessionInfo sessionInfo, NodeId versionHistoryId, NodeId versionId, Name label) throws VersionException, RepositoryException {
         try {
             String uri = getItemUri(versionId, sessionInfo);
-            String strLabel = NameFormat.format(label, new NamespaceResolverImpl(sessionInfo));
+            String strLabel = new NamePathResolverImpl(sessionInfo).getJCRName(label);
             LabelMethod method = new LabelMethod(uri, strLabel, LabelInfo.TYPE_REMOVE);
             execute(method, sessionInfo);
         } catch (IOException e) {
             throw new RepositoryException(e);
-        } catch (NoPrefixDeclaredException e) {
-            throw new RepositoryException(e);
         }
     }
 
@@ -1364,9 +1382,8 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
             method.checkSuccess();
 
             MultiStatus ms = method.getResponseBodyAsMultiStatus();
-            NamespaceResolver resolver = new NamespaceResolverImpl(sessionInfo);
-            return new QueryInfoImpl(ms, sessionInfo, uriResolver,
-                resolver, valueFactory, getQValueFactory());
+            NamePathResolver resolver = new NamePathResolverImpl(sessionInfo);
+            return new QueryInfoImpl(ms, sessionInfo, uriResolver, resolver, valueFactory, getQValueFactory());
         } catch (IOException e) {
             throw new RepositoryException(e);
         } catch (DavException e) {
@@ -1379,14 +1396,14 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     }
 
     /**
-     * @see RepositoryService#createEventFilter(SessionInfo, int, org.apache.jackrabbit.name.Path, boolean, String[], org.apache.jackrabbit.name.QName[], boolean)
+     * @see RepositoryService#createEventFilter(SessionInfo, int, Path, boolean, String[], Name[], boolean)
      */
     public EventFilter createEventFilter(SessionInfo sessionInfo,
                                          int eventTypes,
                                          Path absPath,
                                          boolean isDeep,
                                          String[] uuids,
-                                         QName[] nodeTypeNames,
+                                         Name[] nodeTypeNames,
                                          boolean noLocal)
             throws UnsupportedRepositoryOperationException, RepositoryException {
         // resolve node type names
@@ -1469,13 +1486,13 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
         }
     }
 
-    private void resolveNodeType(Set resolved, QName ntName) {
+    private void resolveNodeType(Set resolved, Name ntName) {
         if (!resolved.add(ntName)) {
             return;
         }
         QNodeTypeDefinition def = (QNodeTypeDefinition) nodeTypeDefinitions.get(ntName);
         if (def != null) {
-            QName[] supertypes = def.getSupertypes();
+            Name[] supertypes = def.getSupertypes();
             for (int i = 0; i < supertypes.length; i++) {
                 resolveNodeType(resolved, supertypes[i]);
             }
@@ -1753,15 +1770,15 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     /**
      * {@inheritDoc}
      */
-    public Iterator getQNodeTypeDefinitions(SessionInfo sessionInfo, QName[] nodetypeNames) throws RepositoryException {
+    public Iterator getQNodeTypeDefinitions(SessionInfo sessionInfo, Name[] nodetypeNames) throws RepositoryException {
         ReportMethod method = null;
         try {
-            NamespaceResolver resolver = new NamespaceResolverImpl(sessionInfo);
+            NamePathResolver resolver = new NamePathResolverImpl(sessionInfo);
 
             ReportInfo info = new ReportInfo(NodeTypesReport.NODETYPES_REPORT, DEPTH_0);
             for (int i = 0; i < nodetypeNames.length; i++) {
                 Element el = DomUtil.createElement(domFactory, NodeTypeConstants.XML_NODETYPE, NodeTypeConstants.NAMESPACE);
-                String jcrName = NameFormat.format(nodetypeNames[i], resolver);
+                String jcrName = resolver.getJCRName(nodetypeNames[i]);
                 DomUtil.addChildElement(el, NodeTypeConstants.XML_NODETYPENAME, NodeTypeConstants.NAMESPACE, jcrName);
                 info.setContentElement(el);
             }
@@ -1777,8 +1794,6 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
             throw new RepositoryException(e);
         } catch (DavException e) {
             throw ExceptionConverter.generate(e);
-        } catch (NoPrefixDeclaredException e) {
-            throw new RepositoryException(e);
         } finally {
             if (method != null) {
                 method.releaseConnection();
@@ -1796,7 +1811,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     private Iterator retrieveQNodeTypeDefinitions(SessionInfo sessionInfo, Document reportDoc) throws RepositoryException {
         ElementIterator it = DomUtil.getChildren(reportDoc.getDocumentElement(), NodeTypeConstants.NODETYPE_ELEMENT, null);
             List ntDefs = new ArrayList();
-            NamespaceResolver resolver = new NamespaceResolverImpl(sessionInfo);
+            NamePathResolver resolver = new NamePathResolverImpl(sessionInfo);
             while (it.hasNext()) {
                 ntDefs.add(new QNodeTypeDefinitionImpl(it.nextElement(), resolver, getQValueFactory()));
             }
@@ -1805,7 +1820,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 nodeTypeDefinitions.clear();
                 for (Iterator defIt = ntDefs.iterator(); defIt.hasNext(); ) {
                     QNodeTypeDefinition def = (QNodeTypeDefinition) defIt.next();
-                    nodeTypeDefinitions.put(def.getQName(), def);
+                    nodeTypeDefinitions.put(def.getName(), def);
                 }
             }
             return ntDefs.iterator();
@@ -1814,7 +1829,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
     /**
      * The XML elements and attributes used in serialization
      */
-    private static final Namespace SV_NAMESPACE = Namespace.getNamespace(QName.NS_SV_PREFIX, QName.NS_SV_URI);
+    private static final Namespace SV_NAMESPACE = Namespace.getNamespace(Name.NS_SV_PREFIX, Name.NS_SV_URI);
     private static final String NODE_ELEMENT = "node";
     private static final String PROPERTY_ELEMENT = "property";
     private static final String VALUE_ELEMENT = "value";
@@ -1827,7 +1842,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
         private final SessionInfo sessionInfo;
         private final ItemId targetId;
         private final List methods = new ArrayList();
-        private final NamespaceResolver nsResolver;
+        private final NamePathResolver resolver;
 
         private String batchId;
 
@@ -1836,7 +1851,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
         private BatchImpl(ItemId targetId, SessionInfo sessionInfo) {
             this.targetId = targetId;
             this.sessionInfo = sessionInfo;
-            this.nsResolver = new NamespaceResolverImpl(sessionInfo);
+            this.resolver = new NamePathResolverImpl(sessionInfo);
         }
 
         private HttpClient start() throws RepositoryException {
@@ -1916,32 +1931,35 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
 
         //----------------------------------------------------------< Batch >---
         /**
-         * @see Batch#addNode(NodeId, QName, QName, String)
+         * @see Batch#addNode(NodeId, Name, Name, String)
          */
-        public void addNode(NodeId parentId, QName nodeName, QName nodetypeName, String uuid) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, NoSuchNodeTypeException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+        public void addNode(NodeId parentId, Name nodeName, Name nodetypeName, String uuid) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, NoSuchNodeTypeException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
             checkConsumed();
             try {
                 // TODO: TOBEFIXED. WebDAV does not allow MKCOL for existing resource -> problem with SNS
                 // use fake name instead (see also #importXML)
-                QName fakeName = new QName(QName.NS_DEFAULT_URI, UUID.randomUUID().toString());
+                Name fakeName = getNameFactory().create(Name.NS_DEFAULT_URI, UUID.randomUUID().toString());
                 String uri = getItemUri(parentId, fakeName, sessionInfo);
                 MkColMethod method = new MkColMethod(uri);
 
                 // build 'sys-view' for the node to create and append it as request body
                 Document body = DomUtil.BUILDER_FACTORY.newDocumentBuilder().newDocument();
                 Element nodeElement = DomUtil.addChildElement(body, NODE_ELEMENT, SV_NAMESPACE);
-                String nameAttr = NameFormat.format(nodeName, nsResolver);
+                String nameAttr = resolver.getJCRName(nodeName);
                 DomUtil.setAttribute(nodeElement, NAME_ATTRIBUTE, SV_NAMESPACE, nameAttr);
 
                 // nodetype must never be null
                 Element propElement = DomUtil.addChildElement(nodeElement, PROPERTY_ELEMENT, SV_NAMESPACE);
-                DomUtil.setAttribute(propElement, NAME_ATTRIBUTE, SV_NAMESPACE, NameFormat.format(QName.JCR_PRIMARYTYPE, nsResolver));
+                String name = resolver.getJCRName(NameConstants.JCR_PRIMARYTYPE);
+                DomUtil.setAttribute(propElement, NAME_ATTRIBUTE, SV_NAMESPACE, name);
                 DomUtil.setAttribute(propElement, TYPE_ATTRIBUTE, SV_NAMESPACE, PropertyType.nameFromValue(PropertyType.NAME));
-                DomUtil.addChildElement(propElement, VALUE_ELEMENT, SV_NAMESPACE, NameFormat.format(nodetypeName, nsResolver));
+                name = resolver.getJCRName(nodetypeName);
+                DomUtil.addChildElement(propElement, VALUE_ELEMENT, SV_NAMESPACE, name);
                 // optional uuid
                 if (uuid != null) {
                     propElement = DomUtil.addChildElement(nodeElement, PROPERTY_ELEMENT, SV_NAMESPACE);
-                    DomUtil.setAttribute(propElement, NAME_ATTRIBUTE, SV_NAMESPACE, NameFormat.format(QName.JCR_UUID, nsResolver));
+                    name = resolver.getJCRName(NameConstants.JCR_UUID);
+                    DomUtil.setAttribute(propElement, NAME_ATTRIBUTE, SV_NAMESPACE, name);
                     DomUtil.setAttribute(propElement, TYPE_ATTRIBUTE, SV_NAMESPACE, PropertyType.nameFromValue(PropertyType.STRING));
                     DomUtil.addChildElement(propElement, VALUE_ELEMENT, SV_NAMESPACE, uuid);
                 }
@@ -1952,35 +1970,33 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 throw new RepositoryException(e);
             } catch (ParserConfigurationException e) {
                 throw new RepositoryException(e);
-            } catch (NoPrefixDeclaredException e) {
-                throw new RepositoryException(e);
             }
         }
 
         /**
-         * @see Batch#addProperty(NodeId, QName, QValue)
+         * @see Batch#addProperty(NodeId, Name, QValue)
          */
-        public void addProperty(NodeId parentId, QName propertyName, QValue value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, ItemExistsException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+        public void addProperty(NodeId parentId, Name propertyName, QValue value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, ItemExistsException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
             checkConsumed();
-            Value jcrValue = ValueFormat.getJCRValue(value, nsResolver, valueFactory);
+            Value jcrValue = ValueFormat.getJCRValue(value, resolver, valueFactory);
             ValuesProperty vp = new ValuesProperty(jcrValue);
             internalAddProperty(parentId, propertyName, vp);
         }
 
         /**
-         * @see Batch#addProperty(NodeId, QName, QValue[])
+         * @see Batch#addProperty(NodeId, Name, QValue[])
          */
-        public void addProperty(NodeId parentId, QName propertyName, QValue[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, ItemExistsException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+        public void addProperty(NodeId parentId, Name propertyName, QValue[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, ItemExistsException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
             checkConsumed();
             Value[] jcrValues = new Value[values.length];
             for (int i = 0; i < values.length; i++) {
-                jcrValues[i] = ValueFormat.getJCRValue(values[i], nsResolver, valueFactory);
+                jcrValues[i] = ValueFormat.getJCRValue(values[i], resolver, valueFactory);
             }
             ValuesProperty vp = new ValuesProperty(jcrValues);
             internalAddProperty(parentId, propertyName, vp);
         }
 
-        private void internalAddProperty(NodeId parentId, QName propertyName, ValuesProperty vp) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, ItemExistsException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+        private void internalAddProperty(NodeId parentId, Name propertyName, ValuesProperty vp) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, ItemExistsException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
             try {
                 String uri = getItemUri(parentId, propertyName, sessionInfo);
                 PutMethod method = new PutMethod(uri);
@@ -2004,7 +2020,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 remove(propertyId);
             } else {
                 // qualified value must be converted to jcr value
-                Value jcrValue = ValueFormat.getJCRValue(value, nsResolver, valueFactory);
+                Value jcrValue = ValueFormat.getJCRValue(value, resolver, valueFactory);
                 ValuesProperty vp = new ValuesProperty(jcrValue);
                 setProperties.add(vp);
             }
@@ -2024,7 +2040,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 // qualified values must be converted to jcr values
                 Value[] jcrValues = new Value[values.length];
                 for (int i = 0; i < values.length; i++) {
-                    jcrValues[i] = ValueFormat.getJCRValue(values[i], nsResolver, valueFactory);
+                    jcrValues[i] = ValueFormat.getJCRValue(values[i], resolver, valueFactory);
                 }
                 setProperties.add(new ValuesProperty(jcrValues));
             }
@@ -2092,9 +2108,9 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
         }
 
         /**
-         * @see Batch#setMixins(NodeId, QName[])
+         * @see Batch#setMixins(NodeId, Name[])
          */
-        public void setMixins(NodeId nodeId, QName[] mixinNodeTypeIds) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+        public void setMixins(NodeId nodeId, Name[] mixinNodeTypeIds) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
             checkConsumed();
             try {
                 DavPropertySet setProperties;
@@ -2106,7 +2122,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 } else {
                     String[] ntNames = new String[mixinNodeTypeIds.length];
                     for (int i = 0; i < mixinNodeTypeIds.length; i++) {
-                        ntNames[i] = NameFormat.format(mixinNodeTypeIds[i], nsResolver);
+                        ntNames[i] = resolver.getJCRName(mixinNodeTypeIds[i]);
                     }
                     setProperties = new DavPropertySet();
                     setProperties.add(new NodeTypeProperty(ItemResourceConstants.JCR_MIXINNODETYPES, ntNames, false));
@@ -2119,16 +2135,13 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 methods.add(method);
             } catch (IOException e) {
                 throw new RepositoryException(e);
-            } catch (NoPrefixDeclaredException e) {
-                // should not occur.
-                throw new RepositoryException(e);
             }
         }
 
         /**
-         * @see Batch#move(NodeId, NodeId, QName)
+         * @see Batch#move(NodeId, NodeId, Name)
          */
-        public void move(NodeId srcNodeId, NodeId destParentNodeId, QName destName) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+        public void move(NodeId srcNodeId, NodeId destParentNodeId, Name destName) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
             checkConsumed();
             String uri = getItemUri(srcNodeId, sessionInfo);
             String destUri = getItemUri(destParentNodeId, destName, sessionInfo);
@@ -2138,11 +2151,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
         }
     }
 
-    //----------------------------------------------< NamespaceResolverImpl >---
 
-    /**
-     * Implements a namespace resolver based on a session info.
-     */
     private class NamespaceResolverImpl implements NamespaceResolver {
 
         private final SessionInfo sessionInfo;
@@ -2152,7 +2161,7 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
          *
          * @param sessionInfo the session info to contact the repository.
          */
-        NamespaceResolverImpl(SessionInfo sessionInfo) {
+        private NamespaceResolverImpl(SessionInfo sessionInfo) {
             this.sessionInfo = sessionInfo;
         }
 
@@ -2179,19 +2188,54 @@ public class RepositoryServiceImpl implements RepositoryService, DavConstants {
                 throw new NamespaceException(msg, e);
             }
         }
+    }
+
+    //-----------------------------------------------< NamePathResolverImpl >---
+    /**
+     * Implements a namespace resolver based on a session info.
+     */
+    private class NamePathResolverImpl implements NamePathResolver {
+
+        private final NameResolver nResolver;
+        private final PathResolver pResolver;
+
+        private NamePathResolverImpl(SessionInfo sessionInfo) {
+            NamespaceResolver nsResolver = new NamespaceResolverImpl(sessionInfo);
+            this.nResolver = new ParsingNameResolver(getNameFactory(), nsResolver);
+            this.pResolver = new ParsingPathResolver(getPathFactory(), nResolver);
+        }
+
+        private NamePathResolverImpl(NamespaceResolver nsResolver) {
+            this.nResolver = new ParsingNameResolver(getNameFactory(), nsResolver);
+            this.pResolver = new ParsingPathResolver(getPathFactory(), nResolver);
+        }
+
+        /**
+         * @inheritDoc
+         */
+        public Name getQName(String jcrName) throws IllegalNameException, NamespaceException {
+            return nResolver.getQName(jcrName);
+        }
+
+        /**
+         * @inheritDoc
+         */
+        public String getJCRName(Name qName) throws NamespaceException {
+            return nResolver.getJCRName(qName);
+        }
 
         /**
          * @inheritDoc
          */
-        public QName getQName(String jcrName) throws IllegalNameException, UnknownPrefixException {
-            return NameFormat.parse(jcrName, this);
+        public Path getQPath(String path) throws MalformedPathException, IllegalNameException, NamespaceException {
+            return pResolver.getQPath(path);
         }
 
         /**
          * @inheritDoc
          */
-        public String getJCRName(QName qName) throws NoPrefixDeclaredException {
-            return NameFormat.format(qName, this);
+        public String getJCRPath(Path path) throws NamespaceException {
+            return pResolver.getJCRPath(path);
         }
     }
 
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/URIResolver.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/URIResolver.java
index 1898cb4..115edde 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/URIResolver.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/URIResolver.java
@@ -18,7 +18,7 @@ package org.apache.jackrabbit.spi2dav;
 
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.PropertyId;
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.SessionInfo;
 
 import javax.jcr.RepositoryException;
diff --git a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/URIResolverImpl.java b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/URIResolverImpl.java
index 6b66817..c983f78 100644
--- a/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/URIResolverImpl.java
+++ b/contrib/spi/spi2dav/src/main/java/org/apache/jackrabbit/spi2dav/URIResolverImpl.java
@@ -18,18 +18,14 @@ package org.apache.jackrabbit.spi2dav;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NameException;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.spi.SessionInfo;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.PropertyId;
 import org.apache.jackrabbit.spi.ItemId;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.util.Text;
 import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
 import org.apache.jackrabbit.webdav.property.DavPropertySet;
@@ -45,6 +41,7 @@ import org.apache.jackrabbit.webdav.DavServletResponse;
 import org.apache.jackrabbit.webdav.DavConstants;
 import org.apache.jackrabbit.webdav.xml.DomUtil;
 import org.apache.jackrabbit.webdav.version.report.ReportInfo;
+import org.apache.jackrabbit.name.NameConstants;
 import org.apache.commons.httpclient.URI;
 import org.w3c.dom.Document;
 
@@ -63,7 +60,7 @@ class URIResolverImpl implements URIResolver {
 
     private final URI repositoryUri;
     private final RepositoryServiceImpl service;
-    private final NamespaceResolver nsResolver;
+    private final NamePathResolver resolver;
     private final Document domFactory;
 
     // TODO: to-be-fixed. uri/id-caches don't get updated
@@ -71,10 +68,10 @@ class URIResolverImpl implements URIResolver {
     private final Map idURICaches = new HashMap();
 
     URIResolverImpl(URI repositoryUri, RepositoryServiceImpl service,
-                    NamespaceResolver nsResolver, Document domFactory) {
+                    NamePathResolver resolver, Document domFactory) {
         this.repositoryUri = repositoryUri;
         this.service = service;
-        this.nsResolver = nsResolver;
+        this.resolver = resolver;
         this.domFactory = domFactory;
     }
 
@@ -158,15 +155,11 @@ class URIResolverImpl implements URIResolver {
             }
             // resolve relative-path part unless it denotes the root-item
             if (path != null && !path.denotesRoot()) {
-                try {
-                    String jcrPath = PathFormat.format(path, nsResolver);
-                    if (!path.isAbsolute() && !uriBuffer.toString().endsWith("/")) {
-                        uriBuffer.append("/");
-                    }
-                    uriBuffer.append(Text.escapePath(jcrPath));
-                } catch (NoPrefixDeclaredException e) {
-                    throw new RepositoryException(e);
+                String jcrPath = resolver.getJCRPath(path);
+                if (!path.isAbsolute() && !uriBuffer.toString().endsWith("/")) {
+                    uriBuffer.append("/");
                 }
+                uriBuffer.append(Text.escapePath(jcrPath));
             }
             String itemUri = uriBuffer.toString();
             if (!cache.containsItemId(itemId)) {
@@ -187,12 +180,12 @@ class URIResolverImpl implements URIResolver {
         if (uniqueID != null) {
             nodeId = service.getIdFactory().createNodeId(uniqueID);
         } else {
-            QName qName = service.getQName(propSet, nsResolver);
-            if (qName == QName.ROOT) {
-                nodeId = service.getIdFactory().createNodeId((String) null, Path.ROOT);
+            Name qName = service.getQName(propSet, resolver);
+            if (NameConstants.ROOT.equals(qName)) {
+                nodeId = service.getIdFactory().createNodeId((String) null, service.getPathFactory().getRootPath());
             } else {
                 int index = service.getIndex(propSet);
-                nodeId = service.getIdFactory().createNodeId(parentId, Path.create(qName, index));
+                nodeId = service.getIdFactory().createNodeId(parentId, service.getPathFactory().create(qName, index));
             }
         }
         // cache
@@ -212,7 +205,7 @@ class URIResolverImpl implements URIResolver {
 
         try {
             DavPropertySet propSet = response.getProperties(DavServletResponse.SC_OK);
-            QName name = NameFormat.parse(propSet.get(ItemResourceConstants.JCR_NAME).getValue().toString(), nsResolver);
+            Name name = resolver.getQName(propSet.get(ItemResourceConstants.JCR_NAME).getValue().toString());
             PropertyId propertyId = service.getIdFactory().createPropertyId(parentId, name);
 
             cache.add(response.getHref(), propertyId);
@@ -242,8 +235,8 @@ class URIResolverImpl implements URIResolver {
             jcrPath = uri;
         }
         try {
-            return PathFormat.parse(Text.unescape(jcrPath), nsResolver);
-        } catch (MalformedPathException e) {
+            return resolver.getQPath(Text.unescape(jcrPath));
+        } catch (NameException e) {
             throw new RepositoryException(e);
         }
     }
@@ -316,7 +309,8 @@ class URIResolverImpl implements URIResolver {
         NodeId parentId = getNodeId(parentUri, sessionInfo);
         // build property id
         try {
-            PropertyId propertyId = service.getIdFactory().createPropertyId(parentId, NameFormat.parse(propName, nsResolver));
+            Name name = resolver.getQName(propName);
+            PropertyId propertyId = service.getIdFactory().createPropertyId(parentId, name);
             cache.add(uri, propertyId);
 
             return propertyId;
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/BatchReadConfig.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/BatchReadConfig.java
index 40c24c7..b7ac1a6 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/BatchReadConfig.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/BatchReadConfig.java
@@ -13,7 +13,7 @@
  */
 package org.apache.jackrabbit.spi2jcr;
 
-import org.apache.jackrabbit.name.QName;
+import org.apache.jackrabbit.spi.Name;
 
 import java.util.Map;
 import java.util.HashMap;
@@ -42,7 +42,7 @@ public class BatchReadConfig {
      * in this configuration, the default depth {@link #DEPTH_DEFAULT 0} will
      * be returned.
      */
-    public int getDepth(QName ntName) {
+    public int getDepth(Name ntName) {
         if (depthMap.containsKey(ntName)) {
             return ((Integer) (depthMap.get(ntName))).intValue();
         } else {
@@ -56,7 +56,7 @@ public class BatchReadConfig {
      * @param ntName
      * @param depth
      */
-    public void setDepth(QName ntName, int depth) {
+    public void setDepth(Name ntName, int depth) {
         if (ntName == null || depth < DEPTH_INFINITE) {
             throw new IllegalArgumentException();
         }
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/ChildInfoImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/ChildInfoImpl.java
index bd12b70..96f0aa4 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/ChildInfoImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/ChildInfoImpl.java
@@ -16,14 +16,13 @@
  */
 package org.apache.jackrabbit.spi2jcr;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 
 import javax.jcr.Node;
 import javax.jcr.RepositoryException;
 import javax.jcr.UnsupportedRepositoryOperationException;
+import javax.jcr.NamespaceException;
 
 /**
  * <code>ChildInfoImpl</code> implements a <code>ChildInfo</code> and provides
@@ -35,17 +34,17 @@ class ChildInfoImpl extends org.apache.jackrabbit.spi.commons.ChildInfoImpl {
      * Creates a new <code>ChildInfoImpl</code> for <code>node</code>.
      *
      * @param node       the JCR node.
-     * @param nsResolver the namespace resolver in use.
+     * @param resolver
      * @throws RepositoryException    if an error occurs while reading from
      *                                <code>node</code>.
-     * @throws IllegalNameException   if the <code>node</code> name is illegal.
-     * @throws UnknownPrefixException if the name of the <code>node</code>
+     * @throws NameException   if the <code>node</code> name is illegal.
+     * @throws NamespaceException if the name of the <code>node</code>
      *                                contains a prefix not known to
      *                                <code>nsResolver</code>.
      */
-    public ChildInfoImpl(Node node, NamespaceResolver nsResolver)
-            throws RepositoryException, IllegalNameException, UnknownPrefixException {
-        super(NameFormat.parse(node.getName(), nsResolver),
+    public ChildInfoImpl(Node node, NamePathResolver resolver)
+            throws NamespaceException, NameException, RepositoryException {
+        super(resolver.getQName(node.getName()),
                 getUniqueId(node), node.getIndex());
     }
 
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/EventSubscription.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/EventSubscription.java
index 7a18cf6..ac16096 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/EventSubscription.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/EventSubscription.java
@@ -22,15 +22,13 @@ import org.apache.jackrabbit.spi.Event;
 import org.apache.jackrabbit.spi.ItemId;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.IdFactory;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.commons.EventImpl;
 import org.apache.jackrabbit.spi.commons.EventBundleImpl;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
@@ -38,6 +36,7 @@ import javax.jcr.observation.EventListener;
 import javax.jcr.Session;
 import javax.jcr.Node;
 import javax.jcr.UnsupportedRepositoryOperationException;
+import javax.jcr.NamespaceException;
 import javax.jcr.nodetype.NodeType;
 import java.util.ArrayList;
 import java.util.List;
@@ -69,12 +68,12 @@ class EventSubscription implements EventListener {
 
     private final SessionInfoImpl sessionInfo;
 
-    private final NamespaceResolver nsResolver;
+    private final NamePathResolver resolver;
 
     EventSubscription(IdFactory idFactory, SessionInfoImpl sessionInfo) {
         this.idFactory = idFactory;
         this.sessionInfo = sessionInfo;
-        this.nsResolver = sessionInfo.getNamespaceResolver();
+        this.resolver = sessionInfo.getNamePathResolver();
     }
 
     /**
@@ -145,7 +144,7 @@ class EventSubscription implements EventListener {
             try {
                 Session session = sessionInfo.getSession();
                 javax.jcr.observation.Event e = events.nextEvent();
-                Path p = PathFormat.parse(e.getPath(), nsResolver);
+                Path p = resolver.getQPath(e.getPath());
                 Path parent = p.getAncestor(1);
                 NodeId parentId = idFactory.createNodeId((String) null, parent);
                 ItemId itemId = null;
@@ -164,18 +163,16 @@ class EventSubscription implements EventListener {
                                 p.getNameElement().getName());
                         break;
                 }
-                QName nodeTypeName = null;
-                QName[] mixinTypes = new QName[0];
+                Name nodeTypeName = null;
+                Name[] mixinTypes = new Name[0];
                 if (node != null) {
                     try {
                         parentId = idFactory.createNodeId(node.getUUID(), null);
                     } catch (UnsupportedRepositoryOperationException ex) {
                         // not referenceable
                     }
-                    nodeTypeName = NameFormat.parse(
-                            node.getPrimaryNodeType().getName(), nsResolver);
-                    mixinTypes = getNodeTypeNames(
-                            node.getMixinNodeTypes(), nsResolver);
+                    nodeTypeName = resolver.getQName(node.getPrimaryNodeType().getName());
+                    mixinTypes = getNodeTypeNames(node.getMixinNodeTypes(), resolver);
                 }
                 Event spiEvent = new EventImpl(e.getType(), p, itemId, parentId,
                         nodeTypeName, mixinTypes, e.getUserID());
@@ -196,18 +193,17 @@ class EventSubscription implements EventListener {
      * resolver to parse the names.
      *
      * @param nt         the node types
-     * @param nsResolver the namespace resolver.
+     * @param resolver
      * @return the qualified names of the node types.
-     * @throws IllegalNameException   if a node type returns an illegal name.
-     * @throws UnknownPrefixException if the nameo of a node type contains a
-     *                                prefix that is not known to <code>nsResolver</code>.
+     * @throws NameException if a node type returns an illegal name.
+     * @throws NamespaceException if the name of a node type contains a
+     * prefix that is not known to <code>resolver</code>.
      */
-    private static QName[] getNodeTypeNames(NodeType[] nt,
-                                     NamespaceResolver nsResolver)
-            throws IllegalNameException, UnknownPrefixException {
-        QName[] names = new QName[nt.length];
+    private static Name[] getNodeTypeNames(NodeType[] nt, NameResolver resolver)
+            throws NameException, NamespaceException {
+        Name[] names = new Name[nt.length];
         for (int i = 0; i < nt.length; i++) {
-            QName ntName = NameFormat.parse(nt[i].getName(), nsResolver);
+            Name ntName = resolver.getQName(nt[i].getName());
             names[i] = ntName;
         }
         return names;
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/IdFactoryImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/IdFactoryImpl.java
index 0037328..d35bb82 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/IdFactoryImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/IdFactoryImpl.java
@@ -16,15 +16,15 @@
  */
 package org.apache.jackrabbit.spi2jcr;
 
-import org.apache.jackrabbit.name.Path;
+import org.apache.jackrabbit.name.PathFactoryImpl;
+import org.apache.jackrabbit.name.PathBuilder;
 import org.apache.jackrabbit.spi.IdFactory;
 import org.apache.jackrabbit.spi.PropertyId;
 import org.apache.jackrabbit.spi.NodeId;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NameException;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.PathFactory;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 import org.apache.jackrabbit.identifier.AbstractIdFactory;
 
 import javax.jcr.Node;
@@ -45,18 +45,21 @@ class IdFactoryImpl extends AbstractIdFactory {
         return INSTANCE;
     }
 
+    protected PathFactory getPathFactory() {
+        return PathFactoryImpl.getInstance();
+    }
     /**
      * Creates a <code>NodeId</code> for the given <code>node</code>.
      *
      * @param node       the JCR Node.
-     * @param nsResolver the namespace resolver in use.
+     * @param resolver
      * @return the <code>NodeId</code> for <code>node</code>.
      * @throws RepositoryException if an error occurs while reading from
      *                             <code>node</code>.
      */
-    public NodeId createNodeId(Node node, NamespaceResolver nsResolver)
+    public NodeId createNodeId(Node node, NamePathResolver resolver)
             throws RepositoryException {
-        Path.PathBuilder builder = new Path.PathBuilder();
+        PathBuilder builder = new PathBuilder();
         int pathElements = 0;
         String uniqueId = null;
         while (uniqueId == null) {
@@ -68,12 +71,12 @@ class IdFactoryImpl extends AbstractIdFactory {
                 String jcrName = node.getName();
                 if (jcrName.equals("")) {
                     // root node
-                    builder.addFirst(QName.ROOT);
+                    builder.addRoot();
                     break;
                 } else {
-                    QName name;
+                    Name name;
                     try {
-                        name = NameFormat.parse(node.getName(), nsResolver);
+                        name = resolver.getQName(node.getName());
                     } catch (NameException ex) {
                        throw new RepositoryException(ex.getMessage(), ex);
                     }
@@ -87,11 +90,7 @@ class IdFactoryImpl extends AbstractIdFactory {
             }
         }
         if (pathElements > 0) {
-            try {
-                return createNodeId(uniqueId, builder.getPath());
-            } catch (MalformedPathException e) {
-                throw new RepositoryException(e.getMessage(), e);
-            }
+            return createNodeId(uniqueId, builder.getPath());
         } else {
             return createNodeId(uniqueId);
         }
@@ -101,20 +100,20 @@ class IdFactoryImpl extends AbstractIdFactory {
      * Creates a <code>PropertyId</code> for the given <code>property</code>.
      *
      * @param property   the JCR Property.
-     * @param nsResolver the namespace resolver in use.
+     * @param resolver
      * @return the <code>PropertyId</code> for <code>property</code>.
      * @throws RepositoryException if an error occurs while reading from
      *                             <code>property</code>.
      */
     public PropertyId createPropertyId(Property property,
-                                       NamespaceResolver nsResolver)
+                                       NamePathResolver resolver)
             throws RepositoryException {
         Node parent = property.getParent();
-        NodeId nodeId = createNodeId(parent, nsResolver);
+        NodeId nodeId = createNodeId(parent, resolver);
         String jcrName = property.getName();
-        QName name;
+        Name name;
         try {
-            name = NameFormat.parse(jcrName, nsResolver);
+            name = resolver.getQName(jcrName);
         } catch (NameException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/LockInfoImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/LockInfoImpl.java
index 9d2c4e2..9cfbe55 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/LockInfoImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/LockInfoImpl.java
@@ -16,7 +16,7 @@
  */
 package org.apache.jackrabbit.spi2jcr;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.lock.Lock;
@@ -32,17 +32,17 @@ class LockInfoImpl extends org.apache.jackrabbit.spi.commons.LockInfoImpl {
      *
      * @param lock       the lock.
      * @param idFactory  the id factory.
-     * @param nsResolver the namespace resolver in use.
+     * @param resolver
      * @throws RepositoryException if an error occurs while reading from
      *                             <code>node</code> or if <code>node</code> is
      *                             not locked.
      */
     public LockInfoImpl(Lock lock,
                         IdFactoryImpl idFactory,
-                        NamespaceResolver nsResolver)
+                        NamePathResolver resolver)
             throws RepositoryException {
         super(lock.getLockToken(), lock.getLockOwner(),
                 lock.isDeep(), lock.isSessionScoped(),
-                idFactory.createNodeId(lock.getNode(), nsResolver));
+                idFactory.createNodeId(lock.getNode(), resolver));
     }
 }
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/NodeInfoImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/NodeInfoImpl.java
index ac0f292..68fa5d6 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/NodeInfoImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/NodeInfoImpl.java
@@ -16,17 +16,15 @@
  */
 package org.apache.jackrabbit.spi2jcr;
 
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.spi.Name;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.Node;
 import javax.jcr.PropertyIterator;
+import javax.jcr.NamespaceException;
 import javax.jcr.nodetype.NodeType;
 import java.util.List;
 import java.util.ArrayList;
@@ -43,22 +41,22 @@ class NodeInfoImpl extends org.apache.jackrabbit.spi.commons.NodeInfoImpl {
      *
      * @param node       the JCR node.
      * @param idFactory  the id factory.
-     * @param nsResolver the namespace resolver in use.
+     * @param resolver
      * @throws RepositoryException if an error occurs while reading from
      *                             <code>node</code>.
      */
     public NodeInfoImpl(Node node,
                         IdFactoryImpl idFactory,
-                        NamespaceResolver nsResolver)
-            throws RepositoryException, IllegalNameException, UnknownPrefixException, MalformedPathException {
-        super(node.getName().length() == 0 ? null : idFactory.createNodeId(node.getParent(), nsResolver),
-                node.getName().length() == 0 ? QName.ROOT : NameFormat.parse(node.getName(), nsResolver),
-                PathFormat.parse(node.getPath(), nsResolver),
-                idFactory.createNodeId(node, nsResolver), node.getIndex(),
-                NameFormat.parse(node.getPrimaryNodeType().getName(), nsResolver),
-                getNodeTypeNames(node.getMixinNodeTypes(), nsResolver),
-                getPropertyIds(node.getReferences(), nsResolver, idFactory),
-                getPropertyIds(node.getProperties(), nsResolver, idFactory));
+                        NamePathResolver resolver)
+            throws RepositoryException, NameException {
+        super(node.getName().length() == 0 ? null : idFactory.createNodeId(node.getParent(), resolver),
+                node.getName().length() == 0 ? NameConstants.ROOT : resolver.getQName(node.getName()),
+                resolver.getQPath(node.getPath()),
+                idFactory.createNodeId(node, resolver), node.getIndex(),
+                resolver.getQName(node.getPrimaryNodeType().getName()),
+                getNodeTypeNames(node.getMixinNodeTypes(), resolver),
+                getPropertyIds(node.getReferences(), resolver, idFactory),
+                getPropertyIds(node.getProperties(), resolver, idFactory));
     }
 
     /**
@@ -66,18 +64,18 @@ class NodeInfoImpl extends org.apache.jackrabbit.spi.commons.NodeInfoImpl {
      * resolver to parse the names.
      *
      * @param nt         the node types
-     * @param nsResolver the namespace resolver.
+     * @param resolver
      * @return the qualified names of the node types.
-     * @throws IllegalNameException   if a node type returns an illegal name.
-     * @throws UnknownPrefixException if the nameo of a node type contains a
-     *                                prefix that is not known to <code>nsResolver</code>.
+     * @throws NameException   if a node type returns an illegal name.
+     * @throws NamespaceException if the name of a node type contains a
+     *                            prefix that is not known to <code>resolver</code>.
      */
-    private static QName[] getNodeTypeNames(NodeType[] nt,
-                                     NamespaceResolver nsResolver)
-            throws IllegalNameException, UnknownPrefixException {
-        QName[] names = new QName[nt.length];
+    private static Name[] getNodeTypeNames(NodeType[] nt,
+                                           NamePathResolver resolver)
+            throws NameException, NamespaceException {
+        Name[] names = new Name[nt.length];
         for (int i = 0; i < nt.length; i++) {
-            QName ntName = NameFormat.parse(nt[i].getName(), nsResolver);
+            Name ntName = resolver.getQName(nt[i].getName());
             names[i] = ntName;
         }
         return names;
@@ -87,19 +85,19 @@ class NodeInfoImpl extends org.apache.jackrabbit.spi.commons.NodeInfoImpl {
      * Returns property ids for the passed JCR properties.
      *
      * @param props      the JCR properties.
-     * @param nsResolver the namespace resolver.
+     * @param resolver
      * @param idFactory  the id factory.
      * @return the property ids for the passed JCR properties.
      * @throws RepositoryException if an error occurs while reading from the
      *                             properties.
      */
     private static Iterator getPropertyIds(PropertyIterator props,
-                                              NamespaceResolver nsResolver,
+                                              NamePathResolver resolver,
                                               IdFactoryImpl idFactory)
             throws RepositoryException {
         List references = new ArrayList();
         while (props.hasNext()) {
-            references.add(idFactory.createPropertyId(props.nextProperty(), nsResolver));
+            references.add(idFactory.createPropertyId(props.nextProperty(), resolver));
         }
         return references.iterator();
     }
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/PropertyInfoImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/PropertyInfoImpl.java
index 655b3c8..05e543d 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/PropertyInfoImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/PropertyInfoImpl.java
@@ -18,12 +18,8 @@ package org.apache.jackrabbit.spi2jcr;
 
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.QValueFactory;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 import org.apache.jackrabbit.value.ValueFormat;
 
 import javax.jcr.RepositoryException;
@@ -42,35 +38,35 @@ class PropertyInfoImpl
      *
      * @param property      the JCR property.
      * @param idFactory     the id factory.
-     * @param nsResolver    the namespace resolver in use.
+     * @param resolver
      * @param qValueFactory the QValue factory.
      * @throws RepositoryException if an error occurs while reading from
      *                             <code>property</code>.
      */
     public PropertyInfoImpl(Property property,
                             IdFactoryImpl idFactory,
-                            NamespaceResolver nsResolver,
+                            NamePathResolver resolver,
                             QValueFactory qValueFactory)
-            throws RepositoryException, MalformedPathException, IllegalNameException, UnknownPrefixException {
-        super(idFactory.createNodeId(property.getParent(), nsResolver),
-                NameFormat.parse(property.getName(), nsResolver),
-                PathFormat.parse(property.getPath(), nsResolver),
-                idFactory.createPropertyId(property, nsResolver),
+            throws RepositoryException, NameException {
+        super(idFactory.createNodeId(property.getParent(), resolver),
+                resolver.getQName(property.getName()),
+                resolver.getQPath(property.getPath()),
+                idFactory.createPropertyId(property, resolver),
                 property.getType(), property.getDefinition().isMultiple(),
-                getValues(property, nsResolver, qValueFactory)); // TODO: build QValues upon (first) usage only.
+                getValues(property, resolver, qValueFactory)); // TODO: build QValues upon (first) usage only.
     }
 
     /**
      * Returns the QValues for the <code>property</code>.
      *
      * @param property   the property.
-     * @param nsResolver the namespace resolver.
+     * @param resolver   the name and path resolver.
      * @param factory    the value factory.
      * @return the values of the property.
      * @throws RepositoryException if an error occurs while reading the values.
      */
     private static QValue[] getValues(Property property,
-                                      NamespaceResolver nsResolver,
+                                      NamePathResolver resolver,
                                       QValueFactory factory)
             throws RepositoryException {
         boolean isMultiValued = property.getDefinition().isMultiple();
@@ -79,12 +75,11 @@ class PropertyInfoImpl
             Value[] jcrValues = property.getValues();
             values = new QValue[jcrValues.length];
             for (int i = 0; i < jcrValues.length; i++) {
-                values[i] = ValueFormat.getQValue(jcrValues[i],
-                        nsResolver, factory);
+                values[i] = ValueFormat.getQValue(jcrValues[i], resolver, factory);
             }
         } else {
             values = new QValue[]{
-                ValueFormat.getQValue(property.getValue(), nsResolver, factory)};
+                ValueFormat.getQValue(property.getValue(), resolver, factory)};
         }
         return values;
     }
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QNodeDefinitionImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QNodeDefinitionImpl.java
index 3b98ddf..ae77008 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QNodeDefinitionImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QNodeDefinitionImpl.java
@@ -16,14 +16,13 @@
  */
 package org.apache.jackrabbit.spi2jcr;
 
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.spi.Name;
 
 import javax.jcr.nodetype.NodeDefinition;
 import javax.jcr.nodetype.NodeType;
+import javax.jcr.NamespaceException;
 
 /**
  * <code>QNodeDefinitionImpl</code> implements a <code>QNodeDefinition</code>.
@@ -35,22 +34,22 @@ class QNodeDefinitionImpl
      * Creates a new qualified node definition based on a JCR NodeDefinition.
      *
      * @param nodeDef    the node definition.
-     * @param nsResolver the namespace resolver in use.
-     * @throws IllegalNameException   if <code>nodeDef</code> contains an
+     * @param resolver
+     * @throws NameException   if <code>nodeDef</code> contains an
      *                                illegal name.
-     * @throws UnknownPrefixException if <code>nodeDef</code> contains a name
+     * @throws NamespaceException if <code>nodeDef</code> contains a name
      *                                with an namespace prefix that is unknown
-     *                                to <code>nsResolver</code>.
+     *                                to <code>resolver</code>.
      */
     QNodeDefinitionImpl(NodeDefinition nodeDef,
-                        NamespaceResolver nsResolver)
-            throws IllegalNameException, UnknownPrefixException {
-        super(nodeDef.getName().equals(ANY_NAME.getLocalName()) ? ANY_NAME : NameFormat.parse(nodeDef.getName(), nsResolver),
-                nodeDef.getDeclaringNodeType() != null ? NameFormat.parse(nodeDef.getDeclaringNodeType().getName(), nsResolver) : null,
+                        NamePathResolver resolver)
+            throws NameException, NamespaceException {
+        super(nodeDef.getName().equals(ANY_NAME.getLocalName()) ? ANY_NAME : resolver.getQName(nodeDef.getName()),
+                nodeDef.getDeclaringNodeType() != null ? resolver.getQName(nodeDef.getDeclaringNodeType().getName()) : null,
                 nodeDef.isAutoCreated(), nodeDef.isMandatory(),
                 nodeDef.getOnParentVersion(), nodeDef.isProtected(),
-                nodeDef.getDefaultPrimaryType() != null ? NameFormat.parse(nodeDef.getDefaultPrimaryType().getName(), nsResolver) : null,
-                getNodeTypeNames(nodeDef.getRequiredPrimaryTypes(), nsResolver),
+                nodeDef.getDefaultPrimaryType() != null ? resolver.getQName(nodeDef.getDefaultPrimaryType().getName()) : null,
+                getNodeTypeNames(nodeDef.getRequiredPrimaryTypes(), resolver),
                 nodeDef.allowsSameNameSiblings());
     }
 
@@ -59,18 +58,18 @@ class QNodeDefinitionImpl
      * resolver to parse the names.
      *
      * @param nt         the node types
-     * @param nsResolver the namespace resolver.
+     * @param resolver
      * @return the qualified names of the node types.
-     * @throws IllegalNameException   if a node type returns an illegal name.
-     * @throws UnknownPrefixException if the nameo of a node type contains a
-     *                                prefix that is not known to <code>nsResolver</code>.
+     * @throws NameException   if a node type returns an illegal name.
+     * @throws NamespaceException if the name of a node type contains a
+     *                            prefix that is not known to <code>resolver</code>.
      */
-    private static QName[] getNodeTypeNames(NodeType[] nt,
-                                     NamespaceResolver nsResolver)
-            throws IllegalNameException, UnknownPrefixException {
-        QName[] names = new QName[nt.length];
+    private static Name[] getNodeTypeNames(NodeType[] nt,
+                                           NamePathResolver resolver)
+            throws NameException, NamespaceException {
+        Name[] names = new Name[nt.length];
         for (int i = 0; i < nt.length; i++) {
-            QName ntName = NameFormat.parse(nt[i].getName(), nsResolver);
+            Name ntName = resolver.getQName(nt[i].getName());
             names[i] = ntName;
         }
         return names;
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QNodeTypeDefinitionImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QNodeTypeDefinitionImpl.java
index 2683108..703a653 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QNodeTypeDefinitionImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QNodeTypeDefinitionImpl.java
@@ -19,16 +19,16 @@ package org.apache.jackrabbit.spi2jcr;
 import org.apache.jackrabbit.spi.QPropertyDefinition;
 import org.apache.jackrabbit.spi.QNodeDefinition;
 import org.apache.jackrabbit.spi.QValueFactory;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.conversion.IllegalNameException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 
 import javax.jcr.nodetype.NodeType;
 import javax.jcr.nodetype.PropertyDefinition;
 import javax.jcr.nodetype.NodeDefinition;
 import javax.jcr.RepositoryException;
+import javax.jcr.NamespaceException;
 
 /**
  * <code>QNodeTypeDefinitionImpl</code> implements a qualified node type
@@ -42,26 +42,27 @@ class QNodeTypeDefinitionImpl
      * <code>NodeType</code>.
      *
      * @param nt            the JCR node type.
-     * @param nsResolver    the namespace resolver in use.
+     * @param resolver
      * @param qValueFactory the QValue factory.
-     * @throws RepositoryException    if an error occurs while reading from
-     *                                <code>nt</code>.
-     * @throws IllegalNameException   if <code>nt</code> contains an illegal
+     *
+     * @throws NameException   if <code>nt</code> contains an illegal
      *                                name.
-     * @throws UnknownPrefixException if <code>nt</code> contains a name with an
+     * @throws NamespaceException if <code>nt</code> contains a name with an
      *                                namespace prefix that is unknown to
      *                                <code>nsResolver</code>.
+     * @throws RepositoryException    if an error occurs while reading from
+     *                                <code>nt</code>.
      */
     public QNodeTypeDefinitionImpl(NodeType nt,
-                                   NamespaceResolver nsResolver,
+                                   NamePathResolver resolver,
                                    QValueFactory qValueFactory)
-            throws RepositoryException, IllegalNameException, UnknownPrefixException {
-        super(NameFormat.parse(nt.getName(), nsResolver),
-                getNodeTypeNames(nt.getDeclaredSupertypes(), nsResolver),
+            throws NamespaceException, RepositoryException, NameException {
+        super(resolver.getQName(nt.getName()),
+                getNodeTypeNames(nt.getDeclaredSupertypes(), resolver),
                 nt.isMixin(), nt.hasOrderableChildNodes(),
-                nt.getPrimaryItemName() != null ? NameFormat.parse(nt.getPrimaryItemName(), nsResolver) : null,
-                getQPropertyDefinitions(nt.getDeclaredPropertyDefinitions(), nsResolver, qValueFactory),
-                getQNodeDefinitions(nt.getDeclaredChildNodeDefinitions(), nsResolver));
+                nt.getPrimaryItemName() != null ? resolver.getQName(nt.getPrimaryItemName()) : null,
+                getQPropertyDefinitions(nt.getDeclaredPropertyDefinitions(), resolver, qValueFactory),
+                getQNodeDefinitions(nt.getDeclaredChildNodeDefinitions(), resolver));
     }
 
     /**
@@ -69,18 +70,18 @@ class QNodeTypeDefinitionImpl
      * resolver to parse the names.
      *
      * @param nt         the node types
-     * @param nsResolver the namespace resolver.
+     * @param resolver
      * @return the qualified names of the node types.
      * @throws IllegalNameException   if a node type returns an illegal name.
-     * @throws UnknownPrefixException if the nameo of a node type contains a
-     *                                prefix that is not known to <code>nsResolver</code>.
+     * @throws NamespaceException if the name of a node type contains a
+     *                            prefix that is not known to <code>rResolver</code>.
      */
-    private static QName[] getNodeTypeNames(NodeType[] nt,
-                                     NamespaceResolver nsResolver)
-            throws IllegalNameException, UnknownPrefixException {
-        QName[] names = new QName[nt.length];
+    private static Name[] getNodeTypeNames(NodeType[] nt,
+                                           NamePathResolver resolver)
+            throws NameException, NamespaceException {
+        Name[] names = new Name[nt.length];
         for (int i = 0; i < nt.length; i++) {
-            QName ntName = NameFormat.parse(nt[i].getName(), nsResolver);
+            Name ntName = resolver.getQName(nt[i].getName());
             names[i] = ntName;
         }
         return names;
@@ -90,46 +91,41 @@ class QNodeTypeDefinitionImpl
      * Returns qualified property definitions for JCR property definitions.
      *
      * @param propDefs   the JCR property definitions.
-     * @param nsResolver the namespace resolver.
+     * @param resolver
      * @param factory    the value factory.
      * @return qualified property definitions.
      * @throws RepositoryException    if an error occurs while converting the
      *                                definitions.
-     * @throws IllegalNameException   if a property definition contains an
-     *                                illegal name.
-     * @throws UnknownPrefixException if the name of a property definition
-     *                                contains a namespace prefix that is now
-     *                                known to <code>nsResolver</code>.
      */
     private static QPropertyDefinition[] getQPropertyDefinitions(
             PropertyDefinition[] propDefs,
-            NamespaceResolver nsResolver,
-            QValueFactory factory) throws RepositoryException, IllegalNameException, UnknownPrefixException {
+            NamePathResolver resolver,
+            QValueFactory factory) throws RepositoryException, NameException {
         QPropertyDefinition[] propertyDefs = new QPropertyDefinition[propDefs.length];
         for (int i = 0; i < propDefs.length; i++) {
-            propertyDefs[i] = new QPropertyDefinitionImpl(propDefs[i], nsResolver, factory);
+            propertyDefs[i] = new QPropertyDefinitionImpl(propDefs[i], resolver, factory);
         }
         return propertyDefs;
     }
-    
+
     /**
      * Returns qualified node definitions for JCR node definitions.
      *
-     * @param nodeDefs   the JCR node definitions.
-     * @param nsResolver the namespace resolver.
+     * @param nodeDefs the JCR node definitions.
+     * @param resolver the name and path resolver.
      * @return qualified node definitions.
      * @throws IllegalNameException   if the node definition contains an illegal
      *                                name.
-     * @throws UnknownPrefixException if the name of a node definition contains
+     * @throws NamespaceException if the name of a node definition contains
      *                                a namespace prefix that is now known to
      *                                <code>nsResolver</code>.
      */
-    private static QNodeDefinition[] getQNodeDefinitions(
-            NodeDefinition[] nodeDefs,
-            NamespaceResolver nsResolver) throws IllegalNameException, UnknownPrefixException {
+    private static QNodeDefinition[] getQNodeDefinitions (NodeDefinition[] nodeDefs,
+                                                          NamePathResolver resolver)
+            throws NameException, NamespaceException {
         QNodeDefinition[] childNodeDefs = new QNodeDefinition[nodeDefs.length];
         for (int i = 0; i < nodeDefs.length; i++) {
-            childNodeDefs[i] = new QNodeDefinitionImpl(nodeDefs[i], nsResolver);
+            childNodeDefs[i] = new QNodeDefinitionImpl(nodeDefs[i], resolver);
         }
         return childNodeDefs;
     }
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QPropertyDefinitionImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QPropertyDefinitionImpl.java
index b69fc77..26148e5 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QPropertyDefinitionImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QPropertyDefinitionImpl.java
@@ -18,10 +18,8 @@ package org.apache.jackrabbit.spi2jcr;
 
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.QValueFactory;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.IllegalNameException;
-import org.apache.jackrabbit.name.UnknownPrefixException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 import org.apache.jackrabbit.value.ValueFormat;
 
 import javax.jcr.nodetype.PropertyDefinition;
@@ -41,43 +39,43 @@ class QPropertyDefinitionImpl
      * <code>propDef</code>.
      *
      * @param propDef       the JCR property definition.
-     * @param nsResolver    the namespace resolver in use.
+     * @param resolver
      * @param qValueFactory the QValue factory.
      * @throws RepositoryException if an error occurs while reading from
      *                             <code>propDef</code>.
      */
     QPropertyDefinitionImpl(PropertyDefinition propDef,
-                            NamespaceResolver nsResolver,
+                            NamePathResolver resolver,
                             QValueFactory qValueFactory)
-            throws RepositoryException, IllegalNameException, UnknownPrefixException {
-        super(propDef.getName().equals(ANY_NAME.getLocalName()) ? ANY_NAME : NameFormat.parse(propDef.getName(), nsResolver),
-                NameFormat.parse(propDef.getDeclaringNodeType().getName(), nsResolver),
+            throws RepositoryException, NameException {
+        super(propDef.getName().equals(ANY_NAME.getLocalName()) ? ANY_NAME : resolver.getQName(propDef.getName()),
+                resolver.getQName(propDef.getDeclaringNodeType().getName()),
                 propDef.isAutoCreated(), propDef.isMandatory(),
                 propDef.getOnParentVersion(), propDef.isProtected(),
-                convertValues(propDef.getDefaultValues(), nsResolver, qValueFactory),
+                convertValues(propDef.getDefaultValues(), resolver, qValueFactory),
                 propDef.isMultiple(), propDef.getRequiredType(),
-                convertConstraints(propDef.getValueConstraints(), nsResolver, qValueFactory, propDef.getRequiredType()));
+                convertConstraints(propDef.getValueConstraints(), resolver, qValueFactory, propDef.getRequiredType()));
     }
 
     /**
      * Convers JCR {@link Value}s to {@link QValue}s.
      *
      * @param values     the JCR values.
-     * @param nsResolver the namespace resolver.
+     * @param resolver
      * @param factory    the QValue factory.
      * @return the converted values.
      * @throws RepositoryException if an error occurs while converting the
      *                             values.
      */
     private static QValue[] convertValues(Value[] values,
-                                          NamespaceResolver nsResolver,
+                                          NamePathResolver resolver,
                                           QValueFactory factory)
             throws RepositoryException {
         QValue[] defaultValues = null;
         if (values != null) {
             defaultValues = new QValue[values.length];
             for (int i = 0; i < values.length; i++) {
-                defaultValues[i] = ValueFormat.getQValue(values[i], nsResolver, factory);
+                defaultValues[i] = ValueFormat.getQValue(values[i], resolver, factory);
             }
         }
         return defaultValues;
@@ -89,7 +87,7 @@ class QPropertyDefinitionImpl
      *
      * @param constraints  the constraint strings from the JCR property
      *                     definition.
-     * @param nsResolver   the namespace resolver.
+     * @param resolver
      * @param factory      the QValueFactory.
      * @param requiredType the required type of the property definition.
      * @return SPI formatted constraint strings.
@@ -97,7 +95,7 @@ class QPropertyDefinitionImpl
      *                             constraint strings.
      */
     private static String[] convertConstraints(String[] constraints,
-                                               NamespaceResolver nsResolver,
+                                               NamePathResolver resolver,
                                                QValueFactory factory, 
                                                int requiredType)
             throws RepositoryException {
@@ -107,7 +105,7 @@ class QPropertyDefinitionImpl
             int type = requiredType == PropertyType.REFERENCE ? PropertyType.NAME : requiredType;
             for (int i = 0; i < constraints.length; i++) {
                 constraints[i] = ValueFormat.getQValue(
-                        constraints[i], type, nsResolver, factory).getString();
+                        constraints[i], type, resolver, factory).getString();
             }
         }
         return constraints;
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QueryInfoImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QueryInfoImpl.java
index 235fe8d..df36ef3 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QueryInfoImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QueryInfoImpl.java
@@ -18,11 +18,11 @@ package org.apache.jackrabbit.spi2jcr;
 
 import org.apache.jackrabbit.spi.QueryInfo;
 import org.apache.jackrabbit.spi.QValueFactory;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.NameException;
+import org.apache.jackrabbit.spi.Name;
 import org.apache.jackrabbit.util.IteratorHelper;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
@@ -58,7 +58,7 @@ class QueryInfoImpl implements QueryInfo {
     /**
      * The namespace resolver.
      */
-    private final NamespaceResolver nsResolver;
+    private final NamePathResolver resolver;
 
     /**
      * The QValue factory.
@@ -68,7 +68,7 @@ class QueryInfoImpl implements QueryInfo {
     /**
      * The names of the columns in the query result.
      */
-    private final QName[] columnNames;
+    private final Name[] columnNames;
 
     /**
      * The resolved name of the jcr:score column.
@@ -85,28 +85,28 @@ class QueryInfoImpl implements QueryInfo {
      *
      * @param result        the JCR query result.
      * @param idFactory     the id factory.
-     * @param nsResolver    the namespace resolver in use.
+     * @param resolver
      * @param qValueFactory the QValue factory.
      * @throws RepositoryException if an error occurs while reading from
      *                             <code>result</code>.
      */
     public QueryInfoImpl(QueryResult result,
                          IdFactoryImpl idFactory,
-                         NamespaceResolver nsResolver,
+                         NamePathResolver resolver,
                          QValueFactory qValueFactory)
             throws RepositoryException {
         this.result = result;
         this.idFactory = idFactory;
-        this.nsResolver = nsResolver;
+        this.resolver = resolver;
         this.qValueFactory = qValueFactory;
         String[] jcrNames = result.getColumnNames();
-        this.columnNames = new QName[jcrNames.length];
+        this.columnNames = new Name[jcrNames.length];
         try {
             for (int i = 0; i < jcrNames.length; i++) {
-                columnNames[i] = NameFormat.parse(jcrNames[i], nsResolver);
+                columnNames[i] = resolver.getQName(jcrNames[i]);
             }
-            this.scoreName = NameFormat.format(QName.JCR_SCORE, nsResolver);
-            this.pathName = NameFormat.format(QName.JCR_PATH, nsResolver);
+            this.scoreName = resolver.getJCRName(NameConstants.JCR_SCORE);
+            this.pathName = resolver.getJCRName(NameConstants.JCR_PATH);
         } catch (NameException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
@@ -137,7 +137,7 @@ class QueryInfoImpl implements QueryInfo {
                 try {
                     Row row = rows.nextRow();
                     return new QueryResultRowImpl(row, columnJcrNames, scoreName,
-                            pathName, idFactory, nsResolver, qValueFactory);
+                            pathName, idFactory, resolver, qValueFactory);
                 } catch (RepositoryException e) {
                     log.warn("Exception when creating QueryResultRowImpl: " +
                             e.getMessage(), e);
@@ -150,8 +150,8 @@ class QueryInfoImpl implements QueryInfo {
     /**
      * {@inheritDoc}
      */
-    public QName[] getColumnNames() {
-        QName[] names = new QName[columnNames.length];
+    public Name[] getColumnNames() {
+        Name[] names = new Name[columnNames.length];
         System.arraycopy(columnNames, 0, names, 0, columnNames.length);
         return names;
     }
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QueryResultRowImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QueryResultRowImpl.java
index 9a9a4f4..aa5de96 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QueryResultRowImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/QueryResultRowImpl.java
@@ -20,11 +20,10 @@ import org.apache.jackrabbit.spi.QueryResultRow;
 import org.apache.jackrabbit.spi.NodeId;
 import org.apache.jackrabbit.spi.QValue;
 import org.apache.jackrabbit.spi.QValueFactory;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.NamespaceResolver;
-import org.apache.jackrabbit.name.MalformedPathException;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.value.ValueFormat;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.NameException;
 
 import javax.jcr.query.Row;
 import javax.jcr.RepositoryException;
@@ -59,7 +58,7 @@ class QueryResultRowImpl implements QueryResultRow {
      * @param scoreName     the name of the jcr:score column.
      * @param pathName      the name of the jcr:path column
      * @param idFactory     the id factory.
-     * @param nsResolver    the namespace resolver in use.
+     * @param resolver
      * @param qValueFactory the QValue factory.
      * @throws RepositoryException if an error occurs while reading from
      *                             <code>row</code>.
@@ -69,13 +68,13 @@ class QueryResultRowImpl implements QueryResultRow {
                               String scoreName,
                               String pathName,
                               IdFactoryImpl idFactory,
-                              NamespaceResolver nsResolver,
+                              NamePathResolver resolver,
                               QValueFactory qValueFactory) throws RepositoryException {
         String jcrPath = row.getValue(pathName).getString();
         Path path;
         try {
-            path = PathFormat.parse(jcrPath, nsResolver);
-        } catch (MalformedPathException e) {
+            path = resolver.getQPath(jcrPath);
+        } catch (NameException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
         this.nodeId = idFactory.createNodeId((String) null, path);
@@ -86,7 +85,7 @@ class QueryResultRowImpl implements QueryResultRow {
             if (v == null) {
                 values[i] = null;
             } else {
-                values[i] = ValueFormat.getQValue(v, nsResolver, qValueFactory);
+                values[i] = ValueFormat.getQValue(v, resolver, qValueFactory);
             }
         }
     }
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/RepositoryServiceImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/RepositoryServiceImpl.java
index 67cf5a8..648fba7 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/RepositoryServiceImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/RepositoryServiceImpl.java
@@ -33,14 +33,18 @@ import org.apache.jackrabbit.spi.QueryInfo;
 import org.apache.jackrabbit.spi.EventFilter;
 import org.apache.jackrabbit.spi.EventBundle;
 import org.apache.jackrabbit.spi.QValue;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.PathFactory;
+import org.apache.jackrabbit.spi.Name;
+import org.apache.jackrabbit.spi.Path;
 import org.apache.jackrabbit.spi.commons.EventFilterImpl;
-import org.apache.jackrabbit.name.QName;
-import org.apache.jackrabbit.name.Path;
-import org.apache.jackrabbit.name.PathFormat;
-import org.apache.jackrabbit.name.NoPrefixDeclaredException;
-import org.apache.jackrabbit.name.NameFormat;
-import org.apache.jackrabbit.name.MalformedPathException;
-import org.apache.jackrabbit.name.NameException;
+import org.apache.jackrabbit.name.NameFactoryImpl;
+import org.apache.jackrabbit.name.PathFactoryImpl;
+import org.apache.jackrabbit.name.NameConstants;
+import org.apache.jackrabbit.name.PathBuilder;
+import org.apache.jackrabbit.conversion.NameException;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.MalformedPathException;
 import org.apache.jackrabbit.value.QValueFactoryImpl;
 import org.apache.jackrabbit.value.ValueFormat;
 import org.apache.jackrabbit.JcrConstants;
@@ -157,6 +161,20 @@ public class RepositoryServiceImpl implements RepositoryService {
     /**
      * {@inheritDoc}
      */
+    public NameFactory getNameFactory() {
+        return NameFactoryImpl.getInstance();
+    }
+
+    /**
+     * {@inheritDoc}
+     */
+    public PathFactory getPathFactory() {
+        return PathFactoryImpl.getInstance();
+    }
+
+    /**
+     * {@inheritDoc}
+     */
     public QValueFactory getQValueFactory() {
         return QValueFactoryImpl.getInstance();
     }
@@ -183,7 +201,7 @@ public class RepositoryServiceImpl implements RepositoryService {
     public SessionInfo obtain(Credentials credentials, String workspaceName)
             throws LoginException, NoSuchWorkspaceException, RepositoryException {
         Credentials duplicate = SessionInfoImpl.duplicateCredentials(credentials);
-        return new SessionInfoImpl(repository.login(credentials, workspaceName), duplicate);
+        return new SessionInfoImpl(repository.login(credentials, workspaceName), duplicate, getNameFactory(), getPathFactory());
     }
 
     /**
@@ -193,7 +211,7 @@ public class RepositoryServiceImpl implements RepositoryService {
             throws LoginException, NoSuchWorkspaceException, RepositoryException {
         SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         Session s = repository.login(sInfo.getCredentials(), workspaceName);
-        return new SessionInfoImpl(s, sInfo.getCredentials());
+        return new SessionInfoImpl(s, sInfo.getCredentials(), getNameFactory(), getPathFactory());
     }
 
     /**
@@ -202,7 +220,7 @@ public class RepositoryServiceImpl implements RepositoryService {
     public SessionInfo impersonate(SessionInfo sessionInfo, Credentials credentials) throws LoginException, RepositoryException {
         Credentials duplicate = SessionInfoImpl.duplicateCredentials(credentials);
         SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
-        return new SessionInfoImpl(sInfo.getSession().impersonate(credentials), duplicate);
+        return new SessionInfoImpl(sInfo.getSession().impersonate(credentials), duplicate, getNameFactory(), getPathFactory());
     }
 
     /**
@@ -258,9 +276,10 @@ public class RepositoryServiceImpl implements RepositoryService {
      */
     public NodeId getRootId(SessionInfo sessionInfo)
             throws RepositoryException {
+
         SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         return idFactory.createNodeId(sInfo.getSession().getRootNode(),
-                sInfo.getNamespaceResolver());
+                sInfo.getNamePathResolver());
     }
 
     /**
@@ -272,8 +291,8 @@ public class RepositoryServiceImpl implements RepositoryService {
         SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         try {
             return new QNodeDefinitionImpl(getNode(nodeId, sInfo).getDefinition(),
-                    sInfo.getNamespaceResolver());
-        } catch (NameException e) {
+                    sInfo.getNamePathResolver());
+        } catch (org.apache.jackrabbit.conversion.NameException e) {
             throw new RepositoryException(e);
         }
     }
@@ -288,7 +307,7 @@ public class RepositoryServiceImpl implements RepositoryService {
         try {
             return new QPropertyDefinitionImpl(
                     getProperty(propertyId, sInfo).getDefinition(),
-                    sInfo.getNamespaceResolver(),
+                    sInfo.getNamePathResolver(),
                     getQValueFactory());
         } catch (NameException e) {
             throw new RepositoryException(e);
@@ -323,8 +342,8 @@ public class RepositoryServiceImpl implements RepositoryService {
         SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         Node node = getNode(nodeId, sInfo);
         try {
-            return new NodeInfoImpl(node, idFactory, sInfo.getNamespaceResolver());
-        } catch (NameException e) {
+            return new NodeInfoImpl(node, idFactory, sInfo.getNamePathResolver());
+        } catch (org.apache.jackrabbit.conversion.NameException e) {
             throw new RepositoryException(e);
         }
     }
@@ -336,9 +355,9 @@ public class RepositoryServiceImpl implements RepositoryService {
             throws ItemNotFoundException, RepositoryException {
         final SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         Node node = getNode(nodeId, sInfo);
-        QName ntName = null;
+        Name ntName = null;
         try {
-            ntName = NameFormat.parse(node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString(), sInfo.getNamespaceResolver());
+            ntName = sInfo.getNamePathResolver().getQName(node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString());
         } catch (NameException e) {
             // ignore. should never occur
         }
@@ -346,8 +365,8 @@ public class RepositoryServiceImpl implements RepositoryService {
         if (depth == BatchReadConfig.DEPTH_DEFAULT) {
             NodeInfo info;
             try {
-                info = new NodeInfoImpl(node, idFactory, sInfo.getNamespaceResolver());
-            } catch (NameException e) {
+                info = new NodeInfoImpl(node, idFactory, sInfo.getNamePathResolver());
+            } catch (org.apache.jackrabbit.conversion.NameException e) {
                 throw new RepositoryException(e);
             }
             return Collections.singletonList(info).iterator();
@@ -356,15 +375,15 @@ public class RepositoryServiceImpl implements RepositoryService {
             ItemVisitor visitor = new TraversingItemVisitor(false, depth) {
                 protected void entering(Property property, int i) throws RepositoryException {
                     try {
-                        itemInfos.add(new PropertyInfoImpl(property, idFactory, sInfo.getNamespaceResolver(), getQValueFactory()));
-                    } catch (NameException e) {
+                        itemInfos.add(new PropertyInfoImpl(property, idFactory, sInfo.getNamePathResolver(), getQValueFactory()));
+                    } catch (org.apache.jackrabbit.conversion.NameException e) {
                         throw new RepositoryException(e);
                     }
                 }
                 protected void entering(Node node, int i) throws RepositoryException {
                     try {
-                        itemInfos.add(new NodeInfoImpl(node, idFactory, sInfo.getNamespaceResolver()));
-                    } catch (NameException e) {
+                        itemInfos.add(new NodeInfoImpl(node, idFactory, sInfo.getNamePathResolver()));
+                    } catch (org.apache.jackrabbit.conversion.NameException e) {
                         throw new RepositoryException(e);
                     }
                 }
@@ -391,9 +410,9 @@ public class RepositoryServiceImpl implements RepositoryService {
         try {
             while (children.hasNext()) {
                 childInfos.add(new ChildInfoImpl(children.nextNode(),
-                        sInfo.getNamespaceResolver()));
+                        sInfo.getNamePathResolver()));
             }
-        } catch (NameException e) {
+        } catch (org.apache.jackrabbit.conversion.NameException e) {
             throw new RepositoryException(e);
         }
         return childInfos.iterator();
@@ -408,8 +427,8 @@ public class RepositoryServiceImpl implements RepositoryService {
         SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         try {
             return new PropertyInfoImpl(getProperty(propertyId, sInfo), idFactory,
-                    sInfo.getNamespaceResolver(), getQValueFactory());
-        } catch (NameException e) {
+                    sInfo.getNamePathResolver(), getQValueFactory());
+        } catch (org.apache.jackrabbit.conversion.NameException e) {
             throw new RepositoryException(e);
         }
     }
@@ -461,20 +480,16 @@ public class RepositoryServiceImpl implements RepositoryService {
     public void move(final SessionInfo sessionInfo,
                      final NodeId srcNodeId,
                      final NodeId destParentNodeId,
-                     final QName destName) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
+                     final Name destName) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
         final SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         executeWithLocalEvents(new Callable() {
             public Object run() throws RepositoryException {
                 String srcPath = pathForId(srcNodeId, sInfo);
                 StringBuffer destPath = new StringBuffer(pathForId(destParentNodeId, sInfo));
-                try {
-                    if (destPath.length() > 1) {
-                        destPath.append("/");
-                    }
-                    destPath.append(NameFormat.format(destName, sInfo.getNamespaceResolver()));
-                } catch (NoPrefixDeclaredException e) {
-                    throw new RepositoryException(e.getMessage(), e);
+                if (destPath.length() > 1) {
+                    destPath.append("/");
                 }
+                destPath.append(sInfo.getNamePathResolver().getJCRName(destName));
                 sInfo.getSession().getWorkspace().move(srcPath, destPath.toString());
                 return null;
             }
@@ -488,7 +503,7 @@ public class RepositoryServiceImpl implements RepositoryService {
                      final String srcWorkspaceName,
                      final NodeId srcNodeId,
                      final NodeId destParentNodeId,
-                     final QName destName) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
+                     final Name destName) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
         final SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         executeWithLocalEvents(new Callable() {
             public Object run() throws RepositoryException {
@@ -535,7 +550,7 @@ public class RepositoryServiceImpl implements RepositoryService {
                       final String srcWorkspaceName,
                       final NodeId srcNodeId,
                       final NodeId destParentNodeId,
-                      final QName destName,
+                      final Name destName,
                       final boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
         final SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         executeWithLocalEvents(new Callable() {
@@ -563,7 +578,7 @@ public class RepositoryServiceImpl implements RepositoryService {
         SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         try {
             Lock lock = getNode(nodeId, sInfo).getLock();
-            return new LockInfoImpl(lock, idFactory, sInfo.getNamespaceResolver());
+            return new LockInfoImpl(lock, idFactory, sInfo.getNamePathResolver());
         } catch (LockException e) {
             // no lock present on this node.
             return null;
@@ -583,7 +598,7 @@ public class RepositoryServiceImpl implements RepositoryService {
             public Object run() throws RepositoryException {
                 Node n = getNode(nodeId, sInfo);
                 Lock lock = n.lock(deep, sessionScoped);
-                return new LockInfoImpl(lock, idFactory, sInfo.getNamespaceResolver());
+                return new LockInfoImpl(lock, idFactory, sInfo.getNamePathResolver());
             }
         }, sInfo);
     }
@@ -675,35 +690,29 @@ public class RepositoryServiceImpl implements RepositoryService {
                     Node n = getNode(nodeId, sInfo);
                     n.restore(v, removeExisting);
                 } else {
-                    try {
-                        // restore with rel-Path part
-                        Node n = null;
-                        Path relPath = null;
-                        Path path = nodeId.getPath();
-                        if (nodeId.getUniqueID() != null) {
-                            n = getNode(idFactory.createNodeId(nodeId.getUniqueID()), sInfo);
-                            relPath = (path.isAbsolute()) ? Path.ROOT.computeRelativePath(nodeId.getPath()) : path;
-                        } else {
-                            int degree = 0;
-                            while (degree < path.getLength()) {
-                                Path ancestorPath = path.getAncestor(degree);
-                                NodeId parentId = idFactory.createNodeId(nodeId.getUniqueID(), ancestorPath);
-                                if (exists(sessionInfo, parentId)) {
-                                    n = getNode(parentId, sInfo);
-                                    relPath = ancestorPath.computeRelativePath(path);
-                                }
-                                degree++;
+                    // restore with rel-Path part
+                    Node n = null;
+                    Path relPath = null;
+                    Path path = nodeId.getPath();
+                    if (nodeId.getUniqueID() != null) {
+                        n = getNode(idFactory.createNodeId(nodeId.getUniqueID()), sInfo);
+                        relPath = (path.isAbsolute()) ? getPathFactory().getRootPath().computeRelativePath(nodeId.getPath()) : path;
+                    } else {
+                        int degree = 0;
+                        while (degree < path.getLength()) {
+                            Path ancestorPath = path.getAncestor(degree);
+                            NodeId parentId = idFactory.createNodeId(nodeId.getUniqueID(), ancestorPath);
+                            if (exists(sessionInfo, parentId)) {
+                                n = getNode(parentId, sInfo);
+                                relPath = ancestorPath.computeRelativePath(path);
                             }
+                            degree++;
                         }
-                        if (n == null) {
-                            throw new PathNotFoundException("Path not found " + nodeId);
-                        } else {
-                            n.restore(v, PathFormat.format(relPath, sInfo.getNamespaceResolver()), removeExisting);
-                        }
-                    } catch (MalformedPathException e) {
-                        throw new RepositoryException(e);
-                    } catch (NoPrefixDeclaredException e) {
-                        throw new RepositoryException(e);
+                    }
+                    if (n == null) {
+                        throw new PathNotFoundException("Path not found " + nodeId);
+                    } else {
+                        n.restore(v, sInfo.getNamePathResolver().getJCRPath(relPath), removeExisting);
                     }
                 }
                 return null;
@@ -752,7 +761,7 @@ public class RepositoryServiceImpl implements RepositoryService {
                 List ids = new ArrayList();
                 while (it.hasNext()) {
                     ids.add(idFactory.createNodeId(it.nextNode(),
-                            sInfo.getNamespaceResolver()));
+                            sInfo.getNamePathResolver()));
                 }
                 return ids.iterator();
             }
@@ -773,30 +782,27 @@ public class RepositoryServiceImpl implements RepositoryService {
                 Node node = getNode(nodeId, sInfo);
                 Version version = null;
                 boolean cancel;
-                try {
-                    List l = Arrays.asList(mergeFailedIds);
-                    Property mergeFailed = node.getProperty(NameFormat.format(QName.JCR_MERGEFAILED, sInfo.getNamespaceResolver()));
-                    Value[] values = mergeFailed.getValues();
-                    for (int i = 0; i < values.length; i++) {
-                        String uuid = values[i].getString();
-                        if (!l.contains(idFactory.createNodeId(uuid))) {
-                            version = (Version) sInfo.getSession().getNodeByUUID(uuid);
-                            break;
-                        }
+                NamePathResolver resolver = sInfo.getNamePathResolver();
+                List l = Arrays.asList(mergeFailedIds);
+                Property mergeFailed = node.getProperty(resolver.getJCRName(NameConstants.JCR_MERGEFAILED));
+                Value[] values = mergeFailed.getValues();
+                for (int i = 0; i < values.length; i++) {
+                    String uuid = values[i].getString();
+                    if (!l.contains(idFactory.createNodeId(uuid))) {
+                        version = (Version) sInfo.getSession().getNodeByUUID(uuid);
+                        break;
                     }
+                }
 
-                    l =  new ArrayList(predecessorIds.length);
-                    l.addAll(Arrays.asList(predecessorIds));
-                    Property predecessors = node.getProperty(NameFormat.format(QName.JCR_PREDECESSORS, sInfo.getNamespaceResolver()));
-                    values = predecessors.getValues();
-                    for (int i = 0; i < values.length; i++) {
-                        NodeId vId = idFactory.createNodeId(values[i].getString());
-                        l.remove(vId);
-                    }
-                    cancel = l.isEmpty();
-                } catch (NoPrefixDeclaredException e) {
-                    throw new RepositoryException (e);
+                l = new ArrayList(predecessorIds.length);
+                l.addAll(Arrays.asList(predecessorIds));
+                Property predecessors = node.getProperty(resolver.getJCRName(NameConstants.JCR_PREDECESSORS));
+                values = predecessors.getValues();
+                for (int i = 0; i < values.length; i++) {
+                    NodeId vId = idFactory.createNodeId(values[i].getString());
+                    l.remove(vId);
                 }
+                cancel = l.isEmpty();
                 if (cancel) {
                     node.cancelMerge(version);
                 } else {
@@ -813,17 +819,13 @@ public class RepositoryServiceImpl implements RepositoryService {
     public void addVersionLabel(final SessionInfo sessionInfo,
                                 final NodeId versionHistoryId,
                                 final NodeId versionId,
-                                final QName label,
+                                final Name label,
                                 final boolean moveLabel) throws VersionException, RepositoryException {
         final SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         executeWithLocalEvents(new Callable() {
             public Object run() throws RepositoryException {
                 String jcrLabel;
-                try {
-                    jcrLabel = NameFormat.format(label, sInfo.getNamespaceResolver());
-                } catch (NoPrefixDeclaredException e) {
-                    throw new RepositoryException(e.getMessage(), e);
-                }
+                jcrLabel = sInfo.getNamePathResolver().getJCRName(label);
                 Node version = getNode(versionId, sInfo);
                 Node vHistory = getNode(versionHistoryId, sInfo);
                 if (vHistory instanceof VersionHistory) {
@@ -843,16 +845,12 @@ public class RepositoryServiceImpl implements RepositoryService {
     public void removeVersionLabel(final SessionInfo sessionInfo,
                                    final NodeId versionHistoryId,
                                    final NodeId versionId,
-                                   final QName label) throws VersionException, RepositoryException {
+                                   final Name label) throws VersionException, RepositoryException {
         final SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         executeWithLocalEvents(new Callable() {
             public Object run() throws RepositoryException {
                 String jcrLabel;
-                try {
-                    jcrLabel = NameFormat.format(label, sInfo.getNamespaceResolver());
-                } catch (NoPrefixDeclaredException e) {
-                    throw new RepositoryException(e.getMessage(), e);
-                }
+                jcrLabel = sInfo.getNamePathResolver().getJCRName((label));
                 Node vHistory = getNode(versionHistoryId, sInfo);
                 if (vHistory instanceof VersionHistory) {
                     ((VersionHistory) vHistory).removeVersionLabel(jcrLabel);
@@ -896,7 +894,7 @@ public class RepositoryServiceImpl implements RepositoryService {
         Query query = createQuery(sInfo.getSession(), statement,
                 language, namespaces);
         return new QueryInfoImpl(query.execute(), idFactory,
-                sInfo.getNamespaceResolver(), getQValueFactory());
+                sInfo.getNamePathResolver(), getQValueFactory());
     }
 
     /**
@@ -907,7 +905,7 @@ public class RepositoryServiceImpl implements RepositoryService {
                                          Path absPath,
                                          boolean isDeep,
                                          String[] uuid,
-                                         QName[] nodeTypeName,
+                                         Name[] nodeTypeName,
                                          boolean noLocal)
             throws UnsupportedRepositoryOperationException, RepositoryException {
         // make sure there is an event subscription for this session info
@@ -995,9 +993,9 @@ public class RepositoryServiceImpl implements RepositoryService {
             for (NodeTypeIterator it = ntMgr.getAllNodeTypes(); it.hasNext(); ) {
                 NodeType nt = it.nextNodeType();
                 nodeTypes.add(new QNodeTypeDefinitionImpl(nt,
-                        sInfo.getNamespaceResolver(), getQValueFactory()));
+                        sInfo.getNamePathResolver(), getQValueFactory()));
             }
-        } catch (NameException e) {
+        } catch (org.apache.jackrabbit.conversion.NameException e) {
             throw new RepositoryException(e);
         }
         return nodeTypes.iterator();
@@ -1006,20 +1004,20 @@ public class RepositoryServiceImpl implements RepositoryService {
     /**
      * {@inheritDoc}
      */
-    public Iterator getQNodeTypeDefinitions(SessionInfo sessionInfo, QName[] nodetypeNames) throws RepositoryException {
+    public Iterator getQNodeTypeDefinitions(SessionInfo sessionInfo, Name[] nodetypeNames) throws RepositoryException {
         SessionInfoImpl sInfo = getSessionInfoImpl(sessionInfo);
         NodeTypeManager ntMgr = sInfo.getSession().getWorkspace().getNodeTypeManager();
         List defs = new ArrayList();
         for (int i = 0; i < nodetypeNames.length; i++) {
             try {
-                String ntName = NameFormat.format(nodetypeNames[i], sInfo.getNamespaceResolver());
+                String ntName = sInfo.getNamePathResolver().getJCRName(nodetypeNames[i]);
                 NodeType nt = ntMgr.getNodeType(ntName);
-                defs.add(new QNodeTypeDefinitionImpl(nt, sInfo.getNamespaceResolver(), getQValueFactory()));
+                defs.add(new QNodeTypeDefinitionImpl(nt, sInfo.getNamePathResolver(), getQValueFactory()));
 
                 // in addition pack all supertypes into the return value
                 NodeType[] supertypes = nt.getSupertypes();
                 for (int st = 0; st < supertypes.length; st++) {
-                    defs.add(new QNodeTypeDefinitionImpl(supertypes[i], sInfo.getNamespaceResolver(), getQValueFactory()));
+                    defs.add(new QNodeTypeDefinitionImpl(supertypes[i], sInfo.getNamePathResolver(), getQValueFactory()));
                 }
             } catch (NameException e) {
                 throw new RepositoryException(e);
@@ -1065,8 +1063,8 @@ public class RepositoryServiceImpl implements RepositoryService {
         }
 
         public void addNode(final NodeId parentId,
-                            final QName nodeName,
-                            final QName nodetypeName,
+                            final Name nodeName,
+                            final Name nodetypeName,
                             final String uuid) throws RepositoryException {
             executeGuarded(new Callable() {
                 public Object run() throws RepositoryException {
@@ -1096,7 +1094,7 @@ public class RepositoryServiceImpl implements RepositoryService {
         }
 
         public void addProperty(final NodeId parentId,
-                                final QName propertyName,
+                                final Name propertyName,
                                 final QValue value)
                 throws ValueFormatException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, ItemExistsException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
             executeGuarded(new Callable() {
@@ -1104,7 +1102,7 @@ public class RepositoryServiceImpl implements RepositoryService {
                     Session s = sInfo.getSession();
                     Node parent = getParent(parentId, sInfo);
                     Value jcrValue = ValueFormat.getJCRValue(value,
-                            sInfo.getNamespaceResolver(), s.getValueFactory());
+                            sInfo.getNamePathResolver(), s.getValueFactory());
                     parent.setProperty(getJcrName(propertyName), jcrValue);
                     return null;
                 }
@@ -1112,7 +1110,7 @@ public class RepositoryServiceImpl implements RepositoryService {
         }
 
         public void addProperty(final NodeId parentId,
-                                final QName propertyName,
+                                final Name propertyName,
                                 final QValue[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, ItemExistsException, AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
             executeGuarded(new Callable() {
                 public Object run() throws RepositoryException {
@@ -1121,7 +1119,7 @@ public class RepositoryServiceImpl implements RepositoryService {
                     Value[] jcrValues = new Value[values.length];
                     for (int i = 0; i < jcrValues.length; i++) {
                         jcrValues[i] = ValueFormat.getJCRValue(values[i],
-                                sInfo.getNamespaceResolver(), s.getValueFactory());
+                                sInfo.getNamePathResolver(), s.getValueFactory());
                     }
                     n.setProperty(getJcrName(propertyName), jcrValues);
                     return null;
@@ -1135,7 +1133,7 @@ public class RepositoryServiceImpl implements RepositoryService {
                 public Object run() throws RepositoryException {
                     Session s = sInfo.getSession();
                     Value jcrValue = ValueFormat.getJCRValue(value,
-                            sInfo.getNamespaceResolver(), s.getValueFactory());
+                            sInfo.getNamePathResolver(), s.getValueFactory());
                     getProperty(propertyId, sInfo).setValue(jcrValue);
                     return null;
                 }
@@ -1150,7 +1148,7 @@ public class RepositoryServiceImpl implements RepositoryService {
                     Value[] jcrValues = new Value[values.length];
                     for (int i = 0; i < jcrValues.length; i++) {
                         jcrValues[i] = ValueFormat.getJCRValue(values[i],
-                                sInfo.getNamespaceResolver(), s.getValueFactory());
+                                sInfo.getNamePathResolver(), s.getValueFactory());
                     }
                     getProperty(propertyId, sInfo).setValue(jcrValues);
                     return null;
@@ -1182,28 +1180,25 @@ public class RepositoryServiceImpl implements RepositoryService {
             });
         }
 
-        private NodeId calcRemoveNodeId(ItemId itemId) {
+        private NodeId calcRemoveNodeId(ItemId itemId) throws MalformedPathException {
             NodeId nodeId = (NodeId) itemId;
-            try {
-                Path p = itemId.getPath();
-                if (p != null) {
-                    removedNodeIds.add(itemId);
-                    int index = p.getNameElement().getNormalizedIndex();
-                    if (index > Path.INDEX_DEFAULT && !removedNodeIds.isEmpty()) {
-                        Path.PathElement[] elems = p.getElements();
-                        Path.PathBuilder pb = new Path.PathBuilder();
-                        for (int i = 0; i <= elems.length - 2; i++) {
-                            pb.addLast(elems[i]);
-                        }
-                        pb.addLast(p.getNameElement().getName(), index - 1);
-                        NodeId prevSibling = idFactory.createNodeId(itemId.getUniqueID(), pb.getPath());
-                        if (removedNodeIds.contains(prevSibling)) {
-                            nodeId = prevSibling;
-                        }
+            Path p = itemId.getPath();
+            if (p != null) {
+                removedNodeIds.add(itemId);
+                int index = p.getNameElement().getNormalizedIndex();
+                if (index > Path.INDEX_DEFAULT && !removedNodeIds.isEmpty()) {
+                    Path.Element[] elems = p.getElements();
+                    PathBuilder pb = new PathBuilder();
+                    for (int i = 0; i <= elems.length - 2; i++) {
+                        pb.addLast(elems[i]);
+                    }
+                    pb.addLast(p.getNameElement().getName(), index - 1);
+
+                    NodeId prevSibling = idFactory.createNodeId(itemId.getUniqueID(), pb.getPath());
+                    if (removedNodeIds.contains(prevSibling)) {
+                        nodeId = prevSibling;
                     }
                 }
-            } catch (MalformedPathException e) {
-                // ignore
             }
             return nodeId;
         }
@@ -1238,7 +1233,7 @@ public class RepositoryServiceImpl implements RepositoryService {
         }
 
         public void setMixins(final NodeId nodeId,
-                              final QName[] mixinNodeTypeIds)
+                              final Name[] mixinNodeTypeIds)
                 throws RepositoryException {
             executeGuarded(new Callable() {
                 public Object run() throws RepositoryException {
@@ -1268,7 +1263,7 @@ public class RepositoryServiceImpl implements RepositoryService {
 
         public void move(final NodeId srcNodeId,
                          final NodeId destParentNodeId,
-                         final QName destName) throws RepositoryException {
+                         final Name destName) throws RepositoryException {
             executeGuarded(new Callable() {
                 public Object run() throws RepositoryException {
                     String srcPath = pathForId(srcNodeId, sInfo);
@@ -1300,15 +1295,11 @@ public class RepositoryServiceImpl implements RepositoryService {
             }
         }
 
-        private String getJcrName(QName name) throws RepositoryException {
+        private String getJcrName(Name name) throws RepositoryException {
             if (name == null) {
                 return null;
             }
-            try {
-                return NameFormat.format(name, sInfo.getNamespaceResolver());
-            } catch (NoPrefixDeclaredException e) {
-                throw new RepositoryException(e.getMessage(), e);
-            }
+            return sInfo.getNamePathResolver().getJCRName((name));
         }
 
         private String createXMLFragment(String nodeName, String ntName, String uuid) {
@@ -1360,16 +1351,12 @@ public class RepositoryServiceImpl implements RepositoryService {
         }
     }
 
-    private String getDestinationPath(NodeId destParentNodeId, QName destName, SessionInfoImpl sessionInfo) throws RepositoryException {
+    private String getDestinationPath(NodeId destParentNodeId, Name destName, SessionInfoImpl sessionInfo) throws RepositoryException {
         StringBuffer destPath = new StringBuffer(pathForId(destParentNodeId, sessionInfo));
-        try {
-            if (destPath.length() > 1) {
-                destPath.append("/");
-            }
-            destPath.append(NameFormat.format(destName, sessionInfo.getNamespaceResolver()));
-        } catch (NoPrefixDeclaredException e) {
-            throw new RepositoryException(e.getMessage(), e);
+        if (destPath.length() > 1) {
+            destPath.append("/");
         }
+        destPath.append(sessionInfo.getNamePathResolver().getJCRName(destName));
         return destPath.toString();
     }
 
@@ -1388,23 +1375,18 @@ public class RepositoryServiceImpl implements RepositoryService {
             return path.toString();
         }
 
-        try {
-            if (id.getPath().isAbsolute()) {
-                if (path.length() == 1) {
-                    // root path ends with slash
-                    path.setLength(0);
-                }
-            } else {
-                // path is relative
-                if (path.length() > 1) {
-                    path.append("/");
-                }
+        if (id.getPath().isAbsolute()) {
+            if (path.length() == 1) {
+                // root path ends with slash
+                path.setLength(0);
+            }
+        } else {
+            // path is relative
+            if (path.length() > 1) {
+                path.append("/");
             }
-            path.append(PathFormat.format(id.getPath(),
-                    sessionInfo.getNamespaceResolver()));
-        } catch (NoPrefixDeclaredException e) {
-            throw new RepositoryException(e.getMessage());
         }
+        path.append(sessionInfo.getNamePathResolver().getJCRPath(id.getPath()));
         return path.toString();
     }
 
@@ -1431,11 +1413,7 @@ public class RepositoryServiceImpl implements RepositoryService {
             return n;
         }
         String jcrPath;
-        try {
-            jcrPath = PathFormat.format(path, sessionInfo.getNamespaceResolver());
-        } catch (NoPrefixDeclaredException e) {
-            throw new RepositoryException(e.getMessage(), e);
-        }
+        jcrPath = sessionInfo.getNamePathResolver().getJCRPath(path);
         if (path.isAbsolute()) {
             jcrPath = jcrPath.substring(1, jcrPath.length());
         }
@@ -1451,12 +1429,7 @@ public class RepositoryServiceImpl implements RepositoryService {
             n = session.getRootNode();
         }
         Path path = id.getPath();
-        String jcrPath;
-        try {
-            jcrPath = PathFormat.format(path, sessionInfo.getNamespaceResolver());
-        } catch (NoPrefixDeclaredException e) {
-            throw new RepositoryException(e.getMessage(), e);
-        }
+        String jcrPath = sessionInfo.getNamePathResolver().getJCRPath(path);
         if (path.isAbsolute()) {
             jcrPath = jcrPath.substring(1, jcrPath.length());
         }
diff --git a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/SessionInfoImpl.java b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/SessionInfoImpl.java
index e3f2394..965f1e4 100644
--- a/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/SessionInfoImpl.java
+++ b/contrib/spi/spi2jcr/src/main/java/org/apache/jackrabbit/spi2jcr/SessionInfoImpl.java
@@ -17,8 +17,16 @@
 package org.apache.jackrabbit.spi2jcr;
 
 import org.apache.jackrabbit.spi.SessionInfo;
-import org.apache.jackrabbit.name.AbstractNamespaceResolver;
-import org.apache.jackrabbit.name.NamespaceResolver;
+import org.apache.jackrabbit.spi.NameFactory;
+import org.apache.jackrabbit.spi.PathFactory;
+import org.apache.jackrabbit.namespace.NamespaceResolver;
+import org.apache.jackrabbit.conversion.NamePathResolver;
+import org.apache.jackrabbit.conversion.ParsingNameResolver;
+import org.apache.jackrabbit.conversion.NameResolver;
+import org.apache.jackrabbit.conversion.PathResolver;
+import org.apache.jackrabbit.conversion.ParsingPathResolver;
+import org.apache.jackrabbit.conversion.DefaultNamePathResolver;
+import org.apache.jackrabbit.namespace.AbstractNamespaceResolver;
 
 import javax.jcr.NamespaceException;
 import javax.jcr.NamespaceRegistry;
@@ -44,7 +52,7 @@ class SessionInfoImpl implements SessionInfo {
     /**
      * The namespace resolver for this session info.
      */
-    private final NamespaceResolver resolver;
+    private final NamePathResolver resolver;
 
     /**
      * A copy of the credentials that were used to obtain the JCR session.
@@ -56,16 +64,17 @@ class SessionInfoImpl implements SessionInfo {
      *
      * @param session     the JCR session.
      * @param credentials a copy of the credentials that were used to obtain the
-     *                    JCR session.
+     * @param nameFactory
+     * @param pathFactory
      * @throws RepositoryException 
      */
-    SessionInfoImpl(Session session, Credentials credentials) throws RepositoryException {
+    SessionInfoImpl(Session session, Credentials credentials,
+                    NameFactory nameFactory, PathFactory pathFactory) throws RepositoryException {
         this.session = session;
         this.credentials = credentials;
         
         final NamespaceRegistry nsReg = session.getWorkspace().getNamespaceRegistry();
-       
-        this.resolver = new AbstractNamespaceResolver() {
+        final NamespaceResolver nsResolver = new AbstractNamespaceResolver() {
             public String getPrefix(String uri) throws NamespaceException {
                 try {
                     return nsReg.getPrefix(uri);
@@ -86,7 +95,11 @@ class SessionInfoImpl implements SessionInfo {
                 }
             }
         };
-        
+
+        final NameResolver nResolver = new ParsingNameResolver(nameFactory, nsResolver);
+        final PathResolver pResolver = new ParsingPathResolver(pathFactory, nResolver);
+
+        this.resolver = new DefaultNamePathResolver(nResolver, pResolver);
     }
 
     /**
@@ -99,7 +112,7 @@ class SessionInfoImpl implements SessionInfo {
     /**
      * @return the namespace resolver for this session info.
      */
-    NamespaceResolver getNamespaceResolver() {
+    NamePathResolver getNamePathResolver() {
         return resolver;
     }
 

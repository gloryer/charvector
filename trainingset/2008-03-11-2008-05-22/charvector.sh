#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-04-01-2009-05-30 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2008-03-11-2008-05-22
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in 501225931cab97746519ee96db9927216517849a:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/config/BeanConfig.java d6ebca9a72ad7a91f615365fa13e3924eeae691d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockManagerImpl.java 53eab86601a833c14846edf0fc921af610d8fee5:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/InternalVersionImpl.java 0ebc28583fb019bfe43c2b2b742b1156753b7045:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/DefaultSecurityManager.java 2a917f691654f6d96bb8ecf0a0172f98a031fd4c:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/user/IndexNodeResolver.java 6c1bc0932f3f083acf3937326518c137e27986a4:jackrabbit-text-extractors/src/main/java/org/apache/jackrabbit/extractor/MsExcelTextExtractor.java 310a06ae6bac67a472fea16101359bef234364dc:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/MultiIndex.java d89078305a29d296b9eef15a6c169f235750110e:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/AbstractAccessControlManager.java 359115cdb9e87656ed34f4e062afb37ff3ebf5ad:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/NodeImpl.java ad36e0f859e45cc6f6b935a7dfed7ee894a96c5b:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/nodetype/NodeTypeDefDiff.java e8d4352e7ccc6663293652cd1099381e5ace7c00:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/state/SessionItemStateManager.java 92f09a7c44a052d5cfaff404384177903d02e746:jackrabbit-ocm/src/main/java/org/apache/jackrabbit/ocm/manager/collectionconverter/ManageableObjectsUtil.java 70692da68c515e611b38d2f597f81289e682967e:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/NodeImpl.java 6111f3e076f8ae722e7083c47088d3c28becf1e9:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/ItemManager.java 78659b1fb14a324a6e984b95def83c8b3b9c3f64:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/NodeImpl.java c434614e025fcd203f31e34c56e349d03cdbda8f:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/db/DbDataRecord.java 4851c4b68951b5c5e593bc0a0af743257e7fa6e2:jackrabbit-ocm/src/main/java/org/apache/jackrabbit/ocm/manager/collectionconverter/ManageableObjectsUtil.java b728a001476a511e3e925a5baebb34f1526b2880:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/NodeState.java de4db99c2d98a3e024cf6bd0bea026c2b5d304f4:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/ItemManager.java 0f4718ef765a61b70f3d80eb8cb9e43153db4a01:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/CachingHierarchyManager.java b57708dbf24eec62c53e141f1f64cf780b0d1842:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/principal/DefaultPrincipalProvider.java a3b50979a2edf2addcc0b1efe21198025314787e:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/NodeImpl.java 27f518aa816ab0805b28c95391f79aa95160aec2:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/authentication/CryptedSimpleCredentials.java 3d5742984b4a1c158400adf8d27355df2f096677:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/user/UserImpl.java f9892bfbe612f050e824b9e86c02cadea2b370aa:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntriesImpl.java 139579627d01a798fed150d7af7d8a472d6b6068:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java 96f7a900b3b6c6cbab57de32ebf4b558300b69d6:jackrabbit-jcr-commons/src/main/java/org/apache/jackrabbit/commons/xml/Exporter.java f074a9e07f05ee0f235562ad430401d8eb3d74bb:jackrabbit-jcr-commons/src/main/java/org/apache/jackrabbit/commons/xml/Exporter.java 22739810f8e89efd0fd30a3c505a93898ff67dc8:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/state/DefaultISMLocking.java 9c0d0bbdfcaf77bc9a749005995033622e383056:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/nodetype/NodeTypeManagerImpl.java 6e5a60eb509f98b20e2f3df65d006dfa56630037:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/nodetype/NodeTypeDefinitionImpl.java bc280cd2accd8f048a9987a9d46e8b8ea0d78467:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/simple/SimpleSecurityManager.java

do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/trainingset/2008-03-11-2008-05-22/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/trainingset/2008-03-11-2008-05-22/beforechange/$SUBSTRING~1.java 
done

#run Deckard, you need to change to the directory where the config file located
cd /home/shuaili/charvector/trainingset/2008-03-11-2008-05-22
#/path/to/Deckard/scripts/clonedetect/deckard.sh
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


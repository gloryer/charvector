#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-04-01-2009-05-30 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2009-07-30-2009-09-27
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in b0d5cb4674e7fd5ca8546439a50a623b07a200c9:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/observation/EventStateCollection.java 5886e46f18c16b8ce41b771e7744da78991d0bfb:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/observation/EventImpl.java e766759489681b2c9f7a3e24a1b61618201c5f9c:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/InternalVersionHistoryImpl.java 29cec96d7dbcffe4fe7a09e5262fc04f5e0c9812:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/SessionImpl.java 7e69c1313a0077d33379a3c7c4d4039cdca4c74a:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/FileDataStore.java 49bab858a5f8038f141f779694a404b8f5994ac8:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/JoinQuery.java 6487410f88c305c3f5fca92f78d8aa881c9441e9:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/LazyFileInputStream.java e5d3209d843ae1099c23a56e856491dc301230ee:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/nodetype/NodeDefinitionTemplateImpl.java dc2ccd9ef24de23f010244fc2da77027d7d4dceb:jackrabbit-jca/src/main/java/org/apache/jackrabbit/jca/JCARepositoryHandle.java e8ccc11d81b191924a01068da6e09a707c256894:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/FileDataStore.java cb7917706870971ed3de2ddbf7f1e82d4b070e18:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/value/InternalValue.java 16da47b13589b1a92dd96e95eb12ce9d487bfc21:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/nodetype/NodeTypeManagerImpl.java 5b0bd179f071b4cdff38716f85065fe5ee3ad5f7:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/state/ChangeLog.java 9cfecacc00340ab2e680ddd92512f935e473a286:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/QPropertyDefinitionImpl.java 2187dd2527fa4d36dbc7d0f2f8408fb525608485:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/nodetype/AbstractNodeTypeManager.java 557ea6dcf56c3c47dbcfe7042dbf6d202a4995b3:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/state/ChildNodeEntries.java b83b29fd33245f5f914bde1a1b56fea8506a4226:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/GarbageCollector.java 5af01b3984432e594e4688db7c31ebb47c47e862:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/JcrVersionManager.java 7b9454879e000b31649385db0fef013ff2395a2a:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/nodetype/NodeTypeDefinitionImpl.java ff7a62c0a8768714f8c36e3f8f07d2f017eebbfe:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/RepositoryCopier.java 924908b182a089b37291316728d9f1aea1c4cff7:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/RepositoryCopier.java d4bb66270f563e14053ff8b6f9c03d8c35d737da:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/NodeImpl.java 8b2c9a7cedeb6c2b4abeddded92462366ad67cbd:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/SearchIndex.java 0a8ede0255907b36f8069a5527a98b569cdd2306:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/InternalVersionHistoryImpl.java 4545b0e25bb5985ac6c904d9eb5141d4d0783b8e:jackrabbit-spi2dav/src/main/java/org/apache/jackrabbit/spi2davex/RepositoryServiceImpl.java 6fa27b02a959692f072da119147473886d1f0a51:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/NodeIndexer.java d1cd4d5cb6b847beaac905604856b183e2e2fafd:jackrabbit-spi2dav/src/main/java/org/apache/jackrabbit/spi2davex/RepositoryServiceImpl.java addb5a1e34e2aed1d578c907454542c746a362cb:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/value/BLOBFileValue.java

do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/testset/2009-04-01-2009-05-30/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/testset/2009-04-01-2009-05-30/beforechange/$SUBSTRING~1.java 
done
#run Deckard, you need to change to the directory where the config file located
cd /home/shuaili/charvector/testset/2009-04-01-2009-05-30
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


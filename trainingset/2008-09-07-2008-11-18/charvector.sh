#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-04-01-2009-05-30 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2008-09-07-2008-11-18
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in f3c0427a88c42dfa632349dd4c4ab293c8300b41:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/config/RepositoryConfigurationParser.java e40b44b29a5774dd50742732b5c02ae96dd49ec9:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/IndexMigration.java 600e5290b62ca79914ffe0fb572db9070335d21e:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockManagerImpl.java ae257bc191a8084bbb9597114e2b7b3fac767823:jackrabbit-webdav/src/main/java/org/apache/jackrabbit/webdav/WebdavRequestImpl.java fdaf256946eb20e87c2f8851189f267ab58c34ee:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/WorkspaceItemStateFactory.java 6cbc271530d933d4c9910537fd8a83f089281ae0:jackrabbit-ocm/src/main/java/org/apache/jackrabbit/ocm/manager/objectconverter/impl/ObjectConverterImpl.java 41d550458c2143436c20ce2d259fa6100c71ea4e:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/AbstractVersionManager.java 52e05b706d6dda27275611ab5b1f1c953bab880f:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/AbstractVersionManager.java 8c6ea318bb7a1687894b9c1c2a67412469d0bdc2:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/cluster/ClusterNode.java d7af86e4eb429fd1325d3a515c44cbef24ac15c4:jackrabbit-jcr-commons/src/main/java/org/apache/jackrabbit/commons/AbstractSession.java 35a64172d8da7fc7cc1663f71fba95e254963f10:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/user/UserAccessControlProvider.java 7cb31bd79e8509f52d4803c74fed708f0257b546:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/lock/LockManagerImpl.java 72d84c7db6959aa7dc3b6935cf16ecc499fe4bd3:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/NodeImpl.java d6a34ae2e1ee82e8c5003124e513fb054794c692:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/SessionImpl.java 741b0f3a20b6b437f51689a293efbb5510cf8763:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/NamespaceRegistryImpl.java 7cb696539dfa7196b3bf6f2f6ef495486ca78505:jackrabbit-jcr-commons/src/main/java/org/apache/jackrabbit/util/Text.java 099c34f6cfb27059a292f02092398e0221ae3059:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/db/DbDataStore.java aaef40ce53866141f676128da11549b109678cc4:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/state/SharedItemStateManager.java e8145063987e21d50a182b61500dced0cba56c44:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEntryImpl.java d4c3e867a2c1b30378e39e4b961ba4222febfb1f:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockImpl.java 7870c6b16c8104d99530872ff6b5ae36c0378ffa:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/PersistentIndex.java fe9fb852531289950b415c6446d857167ab8d497:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/db/DbInputStream.java 
do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/trainingset/2008-09-07-2008-11-18/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/trainingset/2008-09-07-2008-11-18/beforechange/$SUBSTRING~1.java 
done

#run Deckard, you need to change to the directory where the config file located
cd /home/shuaili/charvector/trainingset/2008-09-07-2008-11-18
#/path/to/Deckard/scripts/clonedetect/deckard.sh
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


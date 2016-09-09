#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-04-01-2009-05-30 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2009-07-30-2009-09-27
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in ae3450635ba79628f03cf14edd9ef14b2a0c242c:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/DefaultHighlighter.java 508a401c432282c7c533c3013e75bc8d4f00e7dd:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/SearchIndex.java 6b05b2998d58967e00831c656f1fb2f50d82408b:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/DefaultHighlighter.java 1fc3ebfa3d15317d8ad9913f2704340d0a7532b5:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/InternalXAVersionManager.java 2806d20d1b0662927285fe95d6053339407eb386:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/Recovery.java cf6c28974b93f5f03d0d32762d613b996d8ef94e:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/MultiIndex.java f0605812e09dd78aaf4278b3cb020d80ccfde09c:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/VersionManagerImplRestore.java 11d922e97fcce0569fb15eccd0396ba26dc2a603:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/IndexingConfigurationImpl.java 3cce89f4d9c62d28f1d25bb1095d84cbc8f556a2:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/value/RefCountingBLOBFileValue.java e5f27265fcdb97bfca007b7a25afc0303b844835:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/value/DefaultQValue.java a2cd5f7afd9463887f86dc8276761e88249c021d:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/value/AbstractQValue.java e3c5481682ba43fc8d87ae3a2abd5eaec562fc7f:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/DefaultAccessManager.java 262558046d8b7137b5da4bd63b85c571ff19d0b6:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/CachingHierarchyManager.java 9e51a4a1501a822facdcc8c15d686b031424ca8b:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionManagerImpl.java c8355b3d9f541a464012f5d46be0c7ebf757f649:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/InternalVersionManagerImpl.java 29e633a609a96732b19c772b79174d8c7a12e239:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/XAEnvironment.java 7a91c7bbe9840d08d782d71c4f5f6695a42a3730:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockInfo.java 24a6ef8c06f0d33bae422fefc458f49235559b2d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockInfo.java 1b793c9d8942ab6d7864697e46988d262dac4839:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/authorization/acl/ACLProvider.java 517d2de8fa1f92ad4fe3ecd7081f109896af55e2:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/xml/SysViewImportHandler.java a5af5d3cb4f5e68c38c2a1941f45dbd02b0763c9:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/SessionImpl.java 701e7ab30e58b1e3c228f1a27719fda002f3e051:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/user/AuthorizableImpl.java c8406704341b2e01a196d452fcbbb4f343607823	contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java c4cd6a2a734a8e65f1db0a18a6349671e3b68c42:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/SearchManager.java 05c027c32938514731a370c10c41adac48e5940d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/persistence/bundle/BundleDbPersistenceManager.java 424a73092583026b6ac36df3d8def0f427f77f82:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/NodeImpl.java 

do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/testset/2009-07-30-2009-09-27/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/testset/2009-07-30-2009-09-27/beforechange/$SUBSTRING~1.java 
done
#run Deckard, you need to change to the directory where the config file located
cd /home/shuaili/charvector/testset/2009-07-30-2009-09-27
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


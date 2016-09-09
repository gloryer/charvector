#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-04-01-2009-05-30 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2007-09-13-2007-11-24
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in 1895e7104bfb933f0142304ef709c9b9c6c4c19f:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/xml/SessionImporter.java c8406704341b2e01a196d452fcbbb4f343607823:contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java 05c027c32938514731a370c10c41adac48e5940d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/persistence/bundle/BundleDbPersistenceManager.java 424a73092583026b6ac36df3d8def0f427f77f82:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/NodeImpl.java 54283d4882219a6c83ca09483ab55b847c70dea6:contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java 57f84d9fb571dc8152f10321dcbb6ff4b40c539e:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/nodetype/NodeTypeTemplateImpl.java 04fc14eb06ea74e253d1b0180374089c883cb4e1:contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/operation/Move.java 0687708aafe13ebbd8cd1a49da34634aed40bd96:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/FileDataStore.java 08d7af52d295ae3f2e873a871c7147843fa3823b:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/fs/mem/MemoryFileSystem.java fcb4f412fe630e8d3aef510699136ee56b7a8448:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/EventFilterImpl.java 39fbc0b00381bdc2425d557e2ea85d8d6d3c0a37:contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/xml/SessionImporter.java c19ca554066ec224eea765399ccf4501f5aa59c4:contrib/spi/jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java 65b71dde8dc0b344011dcec0d6b22e525edbe197:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/AbstractIndex.java 5266ed857e0fa93e2f15595329a390eaded8ac09:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/BatchedItemOperations.java c7ca013118fb158f012894a95ab8d5fab48cfee3:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/value/QValueValue.java 9989750fbcbf6a6588963826f1e7b7ee53d82cd2:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/PropertyImpl.java e361fe50643df1670408c7943843b5fca3978aa1:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/HierarchyEventListener.java d5c16db89b37085abb75d7c399e2040708588877:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/InternalVersionHistoryImpl.java e5e58ca99c4ed7fbd1732923afa945a7e33a4a6d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/AggregateRuleImpl.java 432d1c37bb8a231abd787320f38604627cc7c063:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/DynamicPooledExecutor.java b94ed0352faa192cea2d4d40418f221028eba9ab:jackrabbit-webdav/src/main/java/org/apache/jackrabbit/webdav/server/AbstractWebdavServlet.java 2c5dbb9f1bfd882be6a219510f78b0e7691a302f:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/GarbageCollector.java ec542e2224004709cc0625e7ec1a00344c3c9547:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/nodetype/PropertyDefinitionTemplateImpl.java 9aeffd02ed86ab81bac889a4d731c14fb0567a6f:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java 
do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/trainingset/2007-09-13-2007-11-24/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/trainingset/2007-09-13-2007-11-24/beforechange/$SUBSTRING~1.java 
done

#run Deckard, you need to change to the directory where the config file located
cd /home/shuaili/charvector/trainingset/2007-09-13-2007-11-24
#/path/to/Deckard/scripts/clonedetect/deckard.sh
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


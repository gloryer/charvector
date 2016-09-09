#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-04-01-2009-05-30 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2008-01-11-2008-03-23
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in 5511cfa40fdf8c4643d804458fd142c06f8655b4:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/WeightedHighlighter.java f882ee3b5cde4e0c3804668d110ccca08533b402:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/persistence/bundle/util/ConnectionFactory.java 9bfdda5d1d6f46e5754d5980c079812642b06126:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/NodeIndexer.java 43cfdb8979fd080908437da21b09fc45e64beb41:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/db/DbDataStore.java a5e84322181d748a7772c4e99155afde1c3aa3cc:jackrabbit-jcr-commons/src/main/java/org/apache/jackrabbit/commons/SerializingContentHandler.java a094af04ad7d12d2161c96490b371b5d00861e6a:jackrabbit-jcr-commons/src/main/java/org/apache/jackrabbit/commons/SerializingContentHandler.java 9c4c7e275269f063b79991c0e72952ed9ac15b5a:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/value/InternalValue.java 2c00125ddd09af75fefbea69e2db53658b1616fb:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/cluster/ClusterNode.java a58b75a822eb9ee6847cee23d49ebafef6c09086:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManagerImpl.java 075ec6817a2b32af7ff68aa13d97e02c9e4b9d35:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/NodeImpl.java 6dd65210b2aecf1a4170fcd125adf78706c7d637:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/journal/DatabaseJournal.java 95c9b319ec9deb11dd3d604d533bdc9edb5e422b:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/query/xpath/QueryFormat.java 762748184decc015b66925e8637945a2d2382b95:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/query/sql2/Parser.java e4738b07db992b658b2c958efb078bc1c92cd851:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/QueryResultImpl.java 2fc7e19faa7cac6c46dce7d67cb95d1d902c7e1c:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/DescendantSelfAxisQuery.java d92da3547d42e3c909c4b7e310972bd304232311:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/FileDataStore.java 31776ff504d4fefef26f0635cab717984173e534:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/RepositoryImpl.java 4734ee329d398a79fb8347f700a01144167ed0aa:jackrabbit-ocm/src/main/java/org/apache/jackrabbit/ocm/mapper/model/ClassDescriptor.java b06988c870873b51df45409d623dfcb12f9ebd0c:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/persistence/bundle/BundleDbPersistenceManager.java 7961828046997df1ae97cfee59fdc15424c6962d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/QueryResultImpl.java 6f223a0c39829cd89e4904cc54056cfc5045e7de:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/SessionImpl.java 501225931cab97746519ee96db9927216517849a:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/config/BeanConfig.java d6ebca9a72ad7a91f615365fa13e3924eeae691d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockManagerImpl.java 53eab86601a833c14846edf0fc921af610d8fee5:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/InternalVersionImpl.java 0ebc28583fb019bfe43c2b2b742b1156753b7045:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/DefaultSecurityManager.java 
do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/trainingset/2008-01-11-2008-03-23/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/trainingset/2008-01-11-2008-03-23/beforechange/$SUBSTRING~1.java 
done

#run Deckard, you need to change to the directory where the config file located
cd /home/shuaili/charvector/trainingset/2008-01-11-2008-03-23
#/path/to/Deckard/scripts/clonedetect/deckard.sh
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


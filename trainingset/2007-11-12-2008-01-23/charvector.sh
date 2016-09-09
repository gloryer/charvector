#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-04-01-2009-05-30 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2007-11-12-2008-01-23
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in e5e58ca99c4ed7fbd1732923afa945a7e33a4a6d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/AggregateRuleImpl.java 432d1c37bb8a231abd787320f38604627cc7c063:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/DynamicPooledExecutor.java b94ed0352faa192cea2d4d40418f221028eba9ab:jackrabbit-webdav/src/main/java/org/apache/jackrabbit/webdav/server/AbstractWebdavServlet.java 2c5dbb9f1bfd882be6a219510f78b0e7691a302f:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/GarbageCollector.java ec542e2224004709cc0625e7ec1a00344c3c9547:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/nodetype/PropertyDefinitionTemplateImpl.java 9aeffd02ed86ab81bac889a4d731c14fb0567a6f:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/NodeEntryImpl.java d1abdb29bd74211a79e3b46b6374b4cf1801ea9c:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/version/VersionHistoryImpl.java 727efc3b3f26952148920864dc7215d6f7d90a66:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/persistence/bundle/util/ConnectionFactory.java d3e782ee49c70c851877a0ac373ece011a2d92f0:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java f80484114508b042291fdb6621b70bf6748075e3:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/journal/DatabaseJournal.java bd4aa628bc16d97a67c98fe2de365a59a15da081:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/SearchIndex.java 9e5a9ebc0bb7412e31ffc75d980a92a964b969ab:jackrabbit-jcr-server/src/main/java/org/apache/jackrabbit/webdav/jcr/JCRWebdavServerServlet.java 84caf67735562daef1a2e004fa97246a7416a935:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/db/TempFileInputStream.java af003ba8cd33dd580877a888e3afa23ad9adfdb0:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/GarbageCollector.java 1978ced6af88b2cc8c87e4304d2ea3941773887b:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/PropertyImpl.java a0fc78edb0b0e8d4ceff09247d29af683e3eaaf5:jackrabbit-text-extractors/src/main/java/org/apache/jackrabbit/extractor/PlainTextExtractor.java 7940553f7846291a28c1cd89e51c801b2e4b1f9d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/SessionImpl.java f1c45d303c366cce50487b7fce681900cc15a902:jackrabbit-ocm/src/main/java/org/apache/jackrabbit/ocm/manager/collectionconverter/CollectionConverter.java 5511cfa40fdf8c4643d804458fd142c06f8655b4:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/WeightedHighlighter.java f882ee3b5cde4e0c3804668d110ccca08533b402:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/persistence/bundle/util/ConnectionFactory.java

do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/trainingset/2007-11-12-2008-01-23/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/trainingset/2007-11-12-2008-01-23/beforechange/$SUBSTRING~1.java 
done

#run Deckard, you need to change to the directory where the config file located
cd /home/shuaili/charvector/trainingset/2007-11-12-2008-01-23
#/path/to/Deckard/scripts/clonedetect/deckard.sh
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


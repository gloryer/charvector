#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-04-01-2009-05-30 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2008-05-10-2008-07-21
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in f9892bfbe612f050e824b9e86c02cadea2b370aa:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/hierarchy/ChildNodeEntriesImpl.java 139579627d01a798fed150d7af7d8a472d6b6068:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/WorkspaceImpl.java 96f7a900b3b6c6cbab57de32ebf4b558300b69d6:jackrabbit-jcr-commons/src/main/java/org/apache/jackrabbit/commons/xml/Exporter.java f074a9e07f05ee0f235562ad430401d8eb3d74bb:jackrabbit-jcr-commons/src/main/java/org/apache/jackrabbit/commons/xml/Exporter.java 22739810f8e89efd0fd30a3c505a93898ff67dc8:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/state/DefaultISMLocking.java 9c0d0bbdfcaf77bc9a749005995033622e383056:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/nodetype/NodeTypeManagerImpl.java 6e5a60eb509f98b20e2f3df65d006dfa56630037:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/nodetype/NodeTypeDefinitionImpl.java bc280cd2accd8f048a9987a9d46e8b8ea0d78467:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/simple/SimpleSecurityManager.java d631e0725f4a170b463347b95995414d8cb8509f:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/IndexingConfigurationImpl.java  
do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/trainingset/2008-05-10-2008-07-21/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/trainingset/2008-05-10-2008-07-21/beforechange/$SUBSTRING~1.java 
done

#run Deckard, you need to change to the directory where the config file located
cd /home/shuaili/charvector/trainingset/2008-05-10-2008-07-21
#/path/to/Deckard/scripts/clonedetect/deckard.sh
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


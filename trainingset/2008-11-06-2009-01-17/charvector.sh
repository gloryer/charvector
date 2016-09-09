#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-04-01-2009-05-30 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2008-11-06-2009-01-17
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in d4c3e867a2c1b30378e39e4b961ba4222febfb1f:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockImpl.java 7870c6b16c8104d99530872ff6b5ae36c0378ffa:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/PersistentIndex.java fe9fb852531289950b415c6446d857167ab8d497:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/db/DbInputStream.java 7bfbc3f983f8f47cc1f4add3336f96bda8a08375:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/SessionImpl.java 5ad8486ce0121b33cb9d8c9923ee32be83a69768:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/CachingIndexReader.java 736515fadd21e47768d39a3f4ab1b626ce3fe96b:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/state/WorkspaceItemStateFactory.java b3f3cd82137fb0f2f4ed61cf4aa0c81cee65ebfc:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/observation/ObservationManagerImpl.java 7dd81f1e53016bb71324a9df51a732f19135902b:jackrabbit-jcr-servlet/src/main/java/org/apache/jackrabbit/servlet/remote/RMIRemoteBindingServlet.java 9f324ee548f185eaba70ca25582c0ca19b4ead68:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/db/DbDataStore.java 483ac4e0a61444a2afa336b0a7e04ddc414addc7:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/ItemImpl.java 8478f6ffd6da90bf8ce9ae2416ec37e6a39cdc55:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/state/DefaultISMLocking.java
do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/trainingset/2008-11-06-2009-01-17/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/trainingset/2008-11-06-2009-01-17/beforechange/$SUBSTRING~1.java 
done

#run Deckard, you need to change to the directory where the config file located
cd /home/shuaili/charvector/trainingset/2008-11-06-2009-01-17
#/path/to/Deckard/scripts/clonedetect/deckard.sh
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


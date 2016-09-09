#!/bin/bash

#creat the directories of 10 date's pair for testset
#for i in 2008-02-06-2008-04-05 2008-04-06-2008-06-04 2008-06-05-2008-08-03 2008-08-04-2008-10-02 2008-10-03-2008-12-01 2008-12-02-2009-01-30 2009-01-31-2009-03-31 2009-04-01-2009-05-30 2009-05-31-2009-07-29 2009-07-30-2009-09-27
#do 
  #mkdir $i
#done

#change path to the location of your jackrabbit repository    
cd /home/shuaili/BI/jackrabbit

# the [commit_id]:[path] within the corresponing time period, for example, here are all the[commit_id]:[path] from 2008-02-06 to 2008-04-05, you need to modify it if you change the time period. I have attached different charvector.py in each directories.
for i in a58b75a822eb9ee6847cee23d49ebafef6c09086:jackrabbit-jcr2spi/src/main/java/org/apache/jackrabbit/jcr2spi/ItemManagerImpl.java 075ec6817a2b32af7ff68aa13d97e02c9e4b9d35:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/NodeImpl.java 6dd65210b2aecf1a4170fcd125adf78706c7d637:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/journal/DatabaseJournal.java 95c9b319ec9deb11dd3d604d533bdc9edb5e422b:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/query/NAryQueryNode.java 762748184decc015b66925e8637945a2d2382b95:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/query/sql2/Parser.java e4738b07db992b658b2c958efb078bc1c92cd851:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/QueryResultImpl.java 2fc7e19faa7cac6c46dce7d67cb95d1d902c7e1c:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/DescendantSelfAxisQuery.java d92da3547d42e3c909c4b7e310972bd304232311:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/data/FileDataStore.java 31776ff504d4fefef26f0635cab717984173e534:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/RepositoryImpl.java 4734ee329d398a79fb8347f700a01144167ed0aa:jackrabbit-ocm/src/main/java/org/apache/jackrabbit/ocm/mapper/model/ClassDescriptor.java b06988c870873b51df45409d623dfcb12f9ebd0c:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/persistence/bundle/BundleDbPersistenceManager.java 7961828046997df1ae97cfee59fdc15424c6962d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/QueryResultImpl.java 6f223a0c39829cd89e4904cc54056cfc5045e7de:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/SessionImpl.java 501225931cab97746519ee96db9927216517849a:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/config/BeanConfig.java d6ebca9a72ad7a91f615365fa13e3924eeae691d:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockManagerImpl.java 53eab86601a833c14846edf0fc921af610d8fee5:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/version/InternalVersionImpl.java 0ebc28583fb019bfe43c2b2b742b1156753b7045:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/DefaultSecurityManager.java 2a917f691654f6d96bb8ecf0a0172f98a031fd4c:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/user/IndexNodeResolver.java 6c1bc0932f3f083acf3937326518c137e27986a4:jackrabbit-text-extractors/src/main/java/org/apache/jackrabbit/extractor/MsExcelTextExtractor.java 310a06ae6bac67a472fea16101359bef234364dc:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/MultiIndex.java d89078305a29d296b9eef15a6c169f235750110e:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/security/AbstractAccessControlManager.java 359115cdb9e87656ed34f4e062afb37ff3ebf5ad:jackrabbit-core/src/main/java/org/apache/jackrabbit/core/NodeImpl.java ad36e0f859e45cc6f6b935a7dfed7ee894a96c5b:jackrabbit-spi-commons/src/main/java/org/apache/jackrabbit/spi/commons/nodetype/NodeTypeDefDiff.java 

do 
#remove everything except digits, extract the commit id from the string.
SUBSTRING=$(echo $i| tr -cd '[[:digit:]]') 
#you can change the path where you want to store the after change source java code files 
git show $i >/home/shuaili/charvector/testset/2008-02-06-2008-04-05/afterchange/$SUBSTRING.java
#store the latest version of file before changes 
PREVIOUS=$(echo $i| sed 's/:/~1:/')
#you can change the path where you want to store the beforechange source java code files
git show $PREVIOUS >/home/shuaili/charvector/testset/2008-02-06-2008-04-05/beforechange/$SUBSTRING~1.java 
done

cd /home/shuaili/charvector/testset/2008-02-06-2008-04-05
/home/shuaili/Deckard/scripts/clonedetect/deckard.sh


##Instructions of extracting characteristic feature:

Take testdata for example, now we have test date as following,which means we need to collect features from 2008-02-06 00:00:00 to 2008-04-05 23:59:59 for testdata
        String[] jackrabbitTestStartDates = {
    			"2008-02-06",
    			"2008-04-06",
    			"2008-06-05",
    			"2008-08-04",
    			"2008-10-03",
    			"2008-12-02",
    			"2009-01-31",
    			"2009-04-01",
    			"2009-05-31",
    			"2009-07-30",
    	};
    	
    	String[] jackrabbitTestEndDates = {
    			"2008-04-05",
    			"2008-06-04",
    			"2008-08-03",
    			"2008-10-02",
    			"2008-12-01",
    			"2009-01-30",
    			"2009-03-31",
    			"2009-05-30",
    			"2009-07-29",
    			"2009-09-27",
    	};
 - Store the source code files within each time period.
 
   - Specify the location of your testset, say /path/to/testset.

   - modify the charvecto.sh file in charvector/script/charvector.sh.
 
   - run charvector.sh to 
     - creat derectories of 10 date's pairs
     - store the files before change and files after change as source code files which are used by
       Deckerd
     - the last two lines are combined with the command of runing Deckard.
       
 - Using Deckard to extract characteristic vectors from source code files
 
   - creat a cofig file in current path
   - run /path/to/Deckard/clonetest/deckard.sh<br />
  I extract all the files which are stored in above steps in one output file(see the vdb_50_2).  Deckard aslo compare the similarity of different files(see cluster files). The usage of Deckard please see the links below.
 
 
 Here are the link of Deckard<br />
 Deckard<br />
 https://github.com/skyhover/Deckard<br />
 http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=4222572&tag=1
 
 
  

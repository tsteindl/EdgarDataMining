# FORM4DataRetrieval

## How it works
Details on how to access EDGAR data: https://www.sec.gov/os/accessing-edgar-data

Parse JSON from FTP server: 
Visit: https://www.sec.gov/Archives/edgar/daily-index/index.json which gives tree structure to navigate the daily index files.

Recursively traverse tree from index.json files until .idx files are found. They contain the daily indices of submitted FORM 4s (and all other forms). 

Create dictionary/HashMap to assign ArrayList of all found DailyData objects to form type (as of now only Form 4 mapped to decrease memory overhead).

DailyData objects include formType, companyName, CIK, dateFiled, folderPath to Form and fileName. Some examples of form can be looked at in data\forms directory.

Stride the ArrayList for all DailyData objects and visit the url from folderPath. Download this file and create XML from it.

Parse XML using XML-tags from the metatable and use this data to create CSV file.

#Example input
/edgar/daily-index/2022/QTR1/ -conc=false


## Technologies used
- Generic class hierarchies including interfaces and abstract classes enabling building on this implementation (dependency injection)
- Accessing remote FTP server using REST/HTTP requests
- Recursion over Directory tree and scraping data
- Processing strings with Java (parallel) Streams
- Using utility libraries
- Multithreading/Concurrency to attain speedup of x of my first implementation
- FileIO
- JSON/XML Parsing
- Reflection
- Generics (Table)
- Guava
- Building a Parser from XML schema


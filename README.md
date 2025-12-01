# EDGAR DATA Mining
Concurrent mining of Forms available on the public EDGAR Database of the SEC. Supports "Form 4" mining and output to CSV or PostgreSQL. A docker-compose for running PostgreSQL over Docker is included.
## How it works
1. Parse JSON from FTP server: Visit: https://www.sec.gov/Archives/edgar/daily-index/index.json which gives tree structure to navigate the daily index files.
2. Recursively traverse tree from index.json files until .idx files are found. They contain the daily indices of submitted FORM 4s (and all other forms). 
3. Create dictionary/HashMap to assign ArrayList of all found daily data objects to form type.
4. Stride the daily data list and visit the respective URLs.
5. Parse XML.
6. Output parsed form. 

## Example program args
- /edgar/daily-index/2022/QTR1/ -conc=false
- -files=edgar/data/1708176/0001209191-22-020174.txt -conc=false

## EDGAR documentation
https://www.sec.gov/os/accessing-edgar-data

## Technologies used
- Generic class hierarchies including interfaces and abstract classes enabling building on this implementation (dependency injection)
- Accessing remote FTP server using HTTP requests (REST)
- Java FileIO, Streams, Multithreading, Interfaces, Generics, Reflection, JDBC
- Multithreaded/Concurrent implementation is ~3x faster than sequential implementation
- JSON/XML Parsing
- Implemented XML Parser from XML-Schema (maybe this can be generated in the future)

# Todo
- Scanner on demand


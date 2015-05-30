## Dragon Nest Tools
Here is where I make some tools I plan to be using for the Dragon Nest Skill Simulator.

## Current Tools
Pak Extracter

DNT Processor
SQL Collector

## How To Extract Template - Basis
See ResourceUnpacker.bms

See http://aluigi.altervista.org/papers/quickbms.txt

## DDS Image Converter
http://www.imagemagick.org/

## File Locations of Interest
| info            | path                        |
| ----------------|---------------------------- |
| icons           | \resource\ui\mainbar        |
| skill-req icons | \resource\ui\skill          |
| more icons      | \resource\uitemplatetexture |
| description db  | \resource\uistring          |
| skill dnt       | \resource\ext               |
| skill videos    | \resource\movie             |

## Extra Notes
### File Compression
Files are compressed using ZLIB.
Refer to: https://docs.oracle.com/javase/7/docs/api/java/util/zip/Inflater.html

### Skill Image Grid
As a note to self:
The 20 x 10 equally spaced skill icon grid is referenced by an index.
Index 0 refers to the first icon, 1 refers to to the one to the right of it, etc.
Index 10 refers to the second row, first item. Index 11 refers to the one to the right of it, etc.

### Installing nokogiri gem on Windows using Cygwin
`apt-cyg install ruby-nokogiri`

### Installing pg gem (do not use cygwin's ruby-pg) on Windows using Cygwin
`apt-cyg install postgresql postgresql-devel libpq-devel make gcc-core`

`gem install pg`

Note: Using postgresql is only for the support of the Heroku branch of DNSS. Can either use MySQL, PostgreSQL, or SQLite.
COPY datex2MSTDataS 
from E'\\\\peters-w7\\LCL\\datex2MSTDataSource.csv' 
DELIMITER ';' NULL as E'';

COPY datex2MSTItem 
from E'\\\\peters-w7\\LCL\\datex2MSTItem.csv' 
DELIMITER ';' NULL as E'';

COPY datex2MSTLocat 
from E'\\\\peters-w7\\LCL\\datex2MSTLocation.csv' 
DELIMITER ';' NULL as E'';

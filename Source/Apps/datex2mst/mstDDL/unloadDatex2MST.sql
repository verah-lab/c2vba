COPY
(
select 
		s.status_, 
		s.time_, 
		s.system_time, 
		s.mst,
		m.version,
		id,	
		s.datakind, 
		s.valueCol, 
		s.valueColCharac,
		s.idColumn,
		s.validColumn,
		s.invalidValues,
		s.d2VehType,
		s.d2DataType, 
		s.d2BasicDataTyp,
		s.d2ValueElement,
		s.d2Value,
		s.d2ValueInner,
		s.d2ValuePath,
		s.mappingType,
		s.factor,
		s.valueDataType,
		s.validInterval   
from datex2MSTDataS as s, datex2MST as m
where 
	m.mst = s.mst
)
TO
E'\\\\peters-w7\\LCL\\datex2MSTDataSource.csv' 
DELIMITER ';' NULL as E'';

COPY
(
select 
		i.status_, 
		i.time_, 
		i.system_time, 
		i.mst,
		m.version,
		i.id,	
		index_,
		name,
		'',
		lane,
		period,
		dkRef,
		dkId,
		'',
		locId
		
from datex2MSTItem as i, datex2MST as m
where 
	m.mst = i.mst
)
TO
E'\\\\peters-w7\\LCL\\datex2MSTItem.csv' 
DELIMITER ';' NULL as E'';


COPY
(
select 
		l.status_, 
		l.time_, 
		l.system_time, 
		l.mst,
		m.version,
		l.id,
		
		l.d2Id,		
		l.locName,		
		l.equipment,
		l.rdsLocTblRef,
 		l.rdsLocTblVer,
		l.rdsPrimLocCode,
		l.rdsSecLocCode,
		l.rdsDirection,
		l.rdsPrimLocDist,
		l.rdsSecLocDist,
		l.startCoordX, 
		l.startCoordY,
		l.endCoordX,
		l.endCoordY,
		l.geoDynId,
		'',
		''
		
from datex2MSTLocat as l, datex2MST as m
where 
	m.mst = l.mst
)
TO
E'\\\\peters-w7\\LCL\\datex2MSTLocation.csv' 
DELIMITER ';' NULL as E'';





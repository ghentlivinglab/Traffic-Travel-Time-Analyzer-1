use verkeer1;
SET SQL_SAFE_UPDATES = 0;

create table temp as 
	SELECT 
		x.id as id,
		(SELECT 
				AVG(y.traveltime)
			FROM
				trafficdata y
			WHERE
						x.providerID = y.providerID
					AND x.routeID = y.routeID
					AND y.timestamp between x.timestamp - INTERVAL 60 DAY and x.timestamp
					AND ABS(MINUTE(TIMEDIFF(TIME(x.timestamp), TIME(y.timestamp)))) < 10
					AND WEEKDAY(timestamp) = WEEKDAY(MAX(x.timestamp))) AS avgdag
	FROM
		trafficdata x
	where x.avgtraveltimeday = 0
	group by x.id;

update trafficdata a
set a.avgtraveltimeday=(select avgdag from temp b where a.id = b.id);

drop table temp;

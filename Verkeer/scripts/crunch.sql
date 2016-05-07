use verkeer1;
SET SQL_SAFE_UPDATES = 0;

create temporary table temp as 
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
                    AND abs(TIME_TO_SEC(TIMEDIFF(time(x.timestamp), TIME(y.timestamp)))/60) < 15
                    AND WEEKDAY(y.timestamp) = WEEKDAY(x.timestamp)
            ) AS avgdag
    FROM
        trafficdata x
    where x.avgtraveltimeday = 0 or x.avgtraveltimeday is null;

update trafficdata x
set x.avgtraveltimeday=(select avgdag from temp b where x.id = b.id)
where x.avgtraveltimeday = 0 or x.avgtraveltimeday is null;

drop table temp;
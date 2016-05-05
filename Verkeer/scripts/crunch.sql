use verkeer1;
SET SQL_SAFE_UPDATES = 0;

create TEMPORARY table temp as 
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
                    AND abs(TIMESTAMPDIFF(minute, time(x.timestamp), TIME(y.timestamp))) < 15
                    AND WEEKDAY(timestamp) = WEEKDAY(x.timestamp)
            ) AS avgdag
    FROM
        trafficdata x
    where x.avgtraveltimeday = 0 or x.avgtraveltimeday is null
    group by x.id;
update trafficdata x

set x.avgtraveltimeday=(select avgdag from temp b where x.id = b.id)
where x.avgtraveltimeday = 0 or x.avgtraveltimeday is null;

drop table temp;
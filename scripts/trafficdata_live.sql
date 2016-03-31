select
	x.routeID,
	x.timestamp,
	y.length,
	x.traveltime live,
	round((	
		select 	avg(traveltime) 
		from 		trafficdata 
		where 	providerID=x.providerID and 
					routeID=x.routeID and
					timestamp > now() - interval 30 day and
					abs(TIMESTAMPDIFF(minute,time(timestamp),time(x.timestamp))) < 30 and
					weekday(timestamp) = weekday(x.timestamp)			
	),0) avg
from trafficdata x join routes y on x.routeID=y.id
where x.providerID=1 and
		(	select max(timestamp) 
			from trafficdata 
			where providerID=x.providerID 
				and routeID=x.routeID
		) = x.timestamp
SELECT x.routeID, x.timestamp, y.length, x.traveltime, ROUND((
SELECT AVG(traveltime)
FROM trafficdata
WHERE providerID=x.providerID AND routeID=x.routeID AND TIMESTAMP > NOW() - INTERVAL 30 DAY AND ABS(TIMESTAMPDIFF(MINUTE, TIME(TIMESTAMP), TIME(x.timestamp))) < 30 AND WEEKDAY(TIMESTAMP) = WEEKDAY(x.timestamp)),0)
FROM trafficdata x
JOIN routes y ON x.routeID=y.id
WHERE x.providerID=1 AND (
SELECT MAX(TIMESTAMP)
FROM trafficdata
WHERE providerID=x.providerID AND routeID=x.routeID) = x.timestamp
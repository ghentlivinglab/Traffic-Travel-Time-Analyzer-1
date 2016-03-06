# verkeer-1
### Indeling repository 
In de verkeer-1 directory van de masterbranch bevinden zich naast de logboeken en de directory van het java-project 'Verkeer'
ook nog een Perl bestand voor de Coyoteprovider de data op te halen en enkele scripts.

#### Scripts
Om de scripts uit te voeren op een Windows besturingssysteem zal je een bash (Bourne Again Shell) moeten downloaden 
om deze scripts te kunnen uitvoeren, anders kan je het ssh en scp commando niet uitvoeren. (bv: Cygwin64 Terminal)
 1. pushNaarVM : script om het project automatisch naar je lokale VM te pushen.
Hiervoor moet je wel in je VM's netwerkinstelling portforwarding doen van je ssh-poort (poort 122 in ons script) en
moet je ook de directory van waar je project's dist bestand staat en de directory van waar je het project wil 
plaatsen op je VM veranderen.
Om te zorgen dat we geen wachtwoord meer nodig hebben, zullen we een ssh-key zonder wachtwoord moeten genereren. (-> zie script sshAuthorize)
 2. pushNaarProductie : script om het project automatisch naar de productieomgeving te pushen.
net als bij het pushen naar je lokale VM moet je de directory van waar je dist bestand staat en de directory
van waar je het wil in je productieomgeving veranderen.
De poort van onze productieomgeving die we hebben meegekregen is 30022.
Omdat we een wachtwoord nodig hebben om op de proxy en op de productieomgeving in te loggen is het nodig om lokaal en 
op de proxy een ssh-key te generaten zonder wachtwoord. (-> hiervoor hebben we nog geen script) 
 3. sshAuthorize : dient dus om ssh-keys zonder wachtwoord te genereren. (Voorlopig enkel voor naar je lokale VM)

#### Verkeer 
Dit is het project zelf. 
Hierin bevinden zich de libraries, die zullen toegevoegd worden aan het dist bestand bij builden van het project.
Het belangrijkste is natuurlijk de src, waar al onze klassen inzitten.
De opdeling van het src bestand is als volgt:
 1. connectors
  1. Database. De klassen nodig om verbinding te maken met de database en gegevens op te halen.
  Eigenlijk zijn dit geen connectors maar DAO's (Data Access Object), dus de naamgeving is niet heel correct.
  De belangrijkste is de MariaDbConnector klasse die dus via MySQL Query's data ophaald uit onze databank.
  De connectie-gegevens en de query's worden opgehaald hierbij uit het database.properties bestand.
  Indien er geen connectie kan gemaakt worden wordt een ConnectionException opgeworpen.
  2. Provider. Dit bevat alle connectors nodig om data op te halen van onze providers en die dan vervolgens
  op te slaan in onze database.
  Alle providers erven over van één abstracte AProviderConnector klasse.
  Gegevens nodig om de connectie mogelijk te maken met de providers worden opgehaald uit het providers.properties bestand.
  3. De entry's. Dit zijn de objecten die worden opgeslaan en opgehaald uit onze databank.
  Dit onder de vorm van Data (de trafficdata in onze databank, dus routeId, providerId, timestamp en traveltime),
  Provider (de providers in onze databank, opgehaald via id en naam)  en Route (de routes in onze databank, bevat de id, 
  naam en andere nuttige gegevens over de route).
 2. verkeer 
  1. ConsoleParser. nodig om een input van de gebruiker te verwerken op de commandolijnen van de Console.
  Hierdoor kunnen tijdens het lopen van het programma, eigenschappen van het programma 
  (zoals gebruikersnaam, wachtwoord, ...) worden aangepast met behulp van de commando's app en db.
  Hiernaast kunnen ook eigenschappen opgevraagd worden, kan de applicatie gestart worden enz. .
  2. MyLogger. Dit dient om logging te verwezenlijken (uitschrijven naar de Console en naar logbestanden) van de
  applicatie. Enerzijds foutmeldingen die gebeuren maar ook gewoon informatie over wat er gaande is in de applicatie.
  Elke dag wordt een nieuw logbestand aangemaakt. Om dit te verwezenlijken erven alle klassen over 
  van deze interface MyLogger.
  3. Pollthread. Zorgt dat er om de zoveel minuten gepolld, data opgehaald, wordt van de providers. 
  En deze data op zijn buurt in de databank wordt opgeslaan.
  4. Verkeer. Dit is het main-programma. Dit wordt gerund als onze applicatie gestart wordt.
  bevat een Pollthread Object en verwezenlijkt ook centraal het loggen.
  5. app.properties. bevat de providers waar er naar gepolld wordt en het pollinterval.
 3. test/connectors.
 Testen van de database en provider-klassen.
  
  



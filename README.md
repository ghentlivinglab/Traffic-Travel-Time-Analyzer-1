# verkeer-1
### Richtlijnen toegang tot applicatie (in de productieomgeving)
De applicatie is opgebouwd in een linux-omgeving. Hierdoor is een linux-besturingssysteem vereist ofwel
een bash (Bourne Again Shell, dit is een tekstuele console waar linux-commando's kunnen ingevoerd worden).
Een mogelijkheid is 'Cygwin64 Terminal'.
 1. Downloaden van cygwin64 terminal: (link: https://www.cygwin.com/) kiezen voor de opties ->net->ssh.
 2. Eerst moeten op de proxy kunnen. Omdat deze zich op het ugent netwerk bevindt zullen we eerst een VPN-verbinding maken met UGent ASA VPN (via Cisco AnyConnect Secure Mobility Client).
 3. commando 'ssh student@tiwibp1.ugent.be', wachtwoord is 'vop2016'. Hierdoor bevinden we ons op de proxy.
 4. Voer in de proxy-console het commando 'ssh root@localhost -p 3022' uit. -p 30022 omdat de ssh-poort van onze productieomgeving op 30022 is ingesteld. Wachtwoord is 'aeSqFPbpUl'.
Nu bevinden we ons op de productieomgeving waar de applicatie zich bevindt. Verdere instructies met het uitvoeren van de applicatie zijn hieronder te vinden.

### Richtlijnen testen in lokale omgeving
Om de applicatie lokaal te kunnen uittesten is een virtuele machine noodzakelijk. Hierop dient MariaDB reeds geïnstalleerd te zijn. Een andere mogelijkheid is om deze database lokaal te installeren: een lokaal mysql process zou normaal gezien ook moeten werken, mits het properties bestand correct wordt ingesteld.

In [connectors.database/database.properties](Verkeer/src/connectors/database/database.properties) vind je een lijst met allerlei properties die gebruikt worden om de connectie te starten met *jdbc*. Het kan zijn dat hier aanpassingen aan moeten gebeuren (bv het wachtwoord, de database naam, poort of ip adres...).

Belangrijk om te weten is dat de applicatie momenteel niet instaat voor het initialiseren van de database als de tabellen nog niet aangemaakt zijn. Daarom kan je [scripts/dbStructure.sql](scripts/dbStructure.sql) gebruiken om de database te initialiseren. Dit SQL script maakt de tabellen aan en voegt reeds 30 routes toe die gebruikt kunnen worden om te testen.

### Richtlijnen uitvoeren programma
In onze productieomgeving bevindt zich een bestand verkeer. Hierin hebben wij het dist-bestand van ons project geïmporteerd. Wij hebben een alias gecreeërd die zorgt dat je gewoon 'verkeer' moet intypen in de tekstuele console
die dan het executable jar bestand van ons project uitvoerd.

###### stappen
 1. typ 'verkeer', dit zorgt dat ons hoofdprogramma gestart wordt. Verbinding met de databank wordt gerealiseerd en connectie met de providers wordt ook reeds getest. Nu bevinden we ons in de tekstuele console van ons project.
 2. Met het commando 'start' wordt de applicatie gestart. Polling wordt nu gedaan met een tijdspanne van 5 minuten (standaard, dit is verstelbaar en zien we later). Wanneer de applicatie heeft gepolld zal er 'triggering update:' en de hoeveelste update sinds de start van het programma verschijnen op de console.
 3. Met het commando 'status' kan je steeds controleren in welke staat de applicatie is.
 Indien je het nog niet gestart hebt, zal dit NEW zijn, anders is het TIMED_WAITING.
 4. Het opvragen van eigenschappen van de applicatie kan via het commando 'properties'.
 5. Deze 'properties' van enerzijds de database en anderzijds de providers kunnen aangepast worden via het commando:
'properties db|app get|set propertyname propertyvalue' waarbij propertyname de eigenschap is die je wil veranderen (bijvoorbeeld het pollinginterval) en propertyvalue de waarde die je aan de eigenschap wil meegeven.
(Deze eigenschap-aanpassen instructies worden ook meegegeven indien foute syntax gehanteerd werd.)

###### opmerking
Voorlopig treden er wel nog wat problemen op met sommige providers (teveel requests of problemen met JSON).
Problemen worden via logging naar de console en ook de logbestanden geschreven. 
Desalniettemin wordt onze databank toch met de gelukte data-ophalingen opgevuld.

###### Bekijken van de databank-gegevens 
 1.Dit kan via het commando 'mysql -u test -p', waarbij je dan het passwoord 'test' moet meegeven.
Nu hebben we verbonden met de MariaDb (er staat MariaDb[(none)].
 2.Om de juiste databank te raadplegen voeren we nu 'use verkeer1;' uit (er staat MariaDb[verkeer1]).
 3.Met 'show tables;' zie je wel tabellen zijn opgenomen in de databank.
 4.met 'select * from trafficdata;' kan je eens testen of er wel degelijk data is opgehaald van de providers.
 Hierbij stelt de kolom routeId de trajecten 1-30 voor en providerId van welke provider de traject-data komt.

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
  
  



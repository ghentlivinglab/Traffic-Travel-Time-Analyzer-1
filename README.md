# Verkeercentrum Gent (groep 1)
## Installatie handleiding

Onze applicatie bestaat uit 2 delen: de REST api, die zorgt voor de communicatie met de server, en de polling applicatie: deze zorgt dat we de data van de verschillende providers elke 5 minuten aanvragen en opslaan.

### Eigen computer benodigdheden

* Windows gebruikers hebben een bash nodig (Bourne Again Shell, dit is een tekstuele console waar unix-commando's kunnen ingevoerd worden).
Een mogelijkheid is 'Cygwin64 Terminal': download de cygwin64 terminal: (link: https://www.cygwin.com/) kies voor de opties ->net->ssh.
* Netbeans
* Zorg dat Perl op je eigen computer staat geïnstalleerd
* Installeer SASS op je eigen computer. Hoe dit moet vind je op http://sass-lang.com/install
* Mac OSX: Installatie met ```sudo ``` moet vermeden worden, dit kan voor problemen zorgen. Gebruik bij voorkeur [rbenv](https://github.com/rbenv/rbenv)  om een nieuwe ruby environment aan te maken waarin je de SASS gem zonder sudo kan installeren.

### Server benodigdheden
* Bij voorkeur met Ubuntu 14.04.4 of een gelijkaardige linux distributie op geïnstalleerd.
* MySQL moet zijn geïnstalleerd. Wij gebruiken versie 5.5.47, andere versies zouden normaal gezien ook moeten werken. Tenzij ze erg afwijken van onze gebruikte versie. MariaDB werkt ook.
* Perl v5.18 of nieuwer. Komt standaard met Ubuntu, kan geïnstalleerd worden / geüpdate worden met ```sudo apt-get install perl```
* De JSON library voor Perl moet geïnstalleerd zijn. Als ```perl -MJSON -e 1``` geen errors geeft, is deze al geïnstalleerd. Anders kan dit via 
  ```sudo perl -MCPAN -e 'install JSON'```
* Glassfish is noodzakelijk
* Java

### Database aanmaken

*  Open een terminal (of cygwin64 terminal in Windows) venster in de scripts map van onze repostory (navigeer hierheen met het cd commando). 
*  Voer het commando ``` ./init-database.sh ipadreshier mysqlusername mysqlpassword ``` uit, vul hierbij eerst het ipadres of domeinnaam van de server in en de mysql logingegevens. Dit moet het root account zijn. bv. ``` ./init-database.sh 146.185.150.100 root wachtwoord ``` Indien een fout 'permission denied' voorkomt, probeer dan eerst ```chmod 500 init-database.sh``` uit te voeren en probeer het nog eens opnieuw. **Dit script zal het root wachtwoord veranderen in root.** Dit is voorlopig noodzakelijk omdat dit zo in onze app configuratie opgeslagen staat.
*  **Voer dit script slechst 1 keer uit, aangezien het mogelijk data kan wissen als de database al eens aangemaakt is geweest.**

### Polling app deployen

* Open het Verkeer netbeans project uit deze repository (Open netbeans > bestand > open project en selecteer /Verkeer) 
* Klik op het hamertje met de bezem ervoor. De gecompileerde bestanden en andere scripts vinden we nu in /Verkeer/dist.
* Open een terminal (of cygwin64 terminal in Windows) venster in de scripts map van onze repostory (navigeer hierheen met het cd commando).
* Voer het commando ``` ./deploy-polling.sh ipadreshier ``` uit, vul hierbij eerst het ipadres of domeinnaam van de server in, bv. ``` ./deploy-polling.sh 146.185.150.100 ``` Indien een fout 'permission denied' voorkomt, probeer dan eerst ```chmod 500 deploy-polling.sh``` uit te voeren en probeer het nog eens opnieuw.
* Als dit is gelukt dan staan de bestanden van uit de beide /dist folders op de server in /root/verkeer
* Het aanzetten moet voorlopig nog manueel gebeuren
* Open je terminal venster en voer ``` ssh root@jouwserverip  ``` uit
* Gebruik ``` cd /root/verkeer  ``` om in de map te belanden met onze jar.
* Voer``` screen java -Xmx256m -jar ./Verkeer.jar  ``` uit om onze jar uit te voeren in een nieuwe 'venster'.
* Nu zie je de output van het programma en kan je een 'poll' forceren met het commando poll.
* Je kan de status zien met het commando 'status'.
* Om dit in de achtergrond te houden is het heel belangrijk om het volgende te doen: druk ctrl + a (hiervan zal je geen visuele feedback zien) en druk daarna op de 'd' toets. Daarna zal je terug in de terminal belanden.
* Gebruik ```screen -ls ``` om te zien of de polling applicatie draait. Normaal gezien staat '(detached)' achter verkeer-1.
* Gebruik ``` screen -r ``` om terug in de console van onze app te gaan. Het kan zijn dat om een id wordt gevraagd, die bekom je door ```screen -ls```. 
* **Vergeet ook hier niet om ctrl + a, d te gebruiken om terug te keren.** 
* We willen dit process later vereenvoudigen en in ons deploy-polling.sh script plaatsen, maar dit is ons momenteel nog niet gelukt. Onze web app deployen verloopt wel volledig automatisch.

### Web app deployen

* Open een nieuw terminal venster (of cygwin64) en gebruik het ```cd``` commando om naar de map /scripts uit onze reposiotory te navigeren.
* Voer daar ``` perl sass.pl ``` uit. Dit script compileert onze scss code aangezien we onze css code (bewust) niet in onze repository opslaan. Het commando geeft 'klaar' als alles gelukt is. Los anders de aangeggeven problemen op. 
* Zorg ervoor dat Netbeans geïnstalleerd is op je eigen computer, en open VerkeerREST uit onze repository. 
* Klik op het hamer symbool met de bezem erbij. Het project zou nu zonder problemen moeten compileren en in de map /VerkeerREST/dist geplaatst worden: VerkeerREST.war Het is dit bestand dat we straks zullen deployen op glassfish (automatisch met scripts).
* Open terug hetzelfde terminal venster, en voer ``` ./deploy-rest.sh ipadreshier gebruikersnaam wachtwoord ``` uit, vul hierbij eerst het ipadres of domeinnaam van de server in en de glassfish gebruikersnaam en wachtwoord. Bv. ``` ./deploy-rest.sh 146.185.150.100 admin aeSqFPbpUl ``` voor onze DigitalOcean server. Indien een fout 'permission denied' voorkomt, voer dan eerst ```chmod 500 deploy-rest.sh``` uit te voeren en probeer het nog eens opnieuw.
* Als deployment lukt, dan kan je naar je server surfen http://mijndomein.com/VerkeerREST/ om het controle paneel te bekijken.

## Richtlijnen testen in lokale omgeving
Om de applicatie lokaal te kunnen uittesten is een virtuele machine noodzakelijk. Hierop dient MariaDB reeds geïnstalleerd te zijn. Een andere mogelijkheid is om deze database lokaal te installeren: een lokaal mysql process zou normaal gezien ook moeten werken, mits het properties bestand correct wordt ingesteld.

In [connectors.database/database.properties](Verkeer/src/connectors/database/database.properties) vind je een lijst met allerlei properties die gebruikt worden om de connectie te starten met *jdbc*. Het kan zijn dat hier aanpassingen aan moeten gebeuren (bv het wachtwoord, de database naam, poort of ip adres...).

Belangrijk om te weten is dat de applicatie momenteel niet instaat voor het initialiseren van de database als de tabellen nog niet aangemaakt zijn. Daarom kan je [scripts/dbStructure.sql](scripts/dbStructure.sql) gebruiken om de database te initialiseren. Dit SQL script maakt de tabellen aan en voegt reeds 30 routes toe die gebruikt kunnen worden om te testen.

Omdat we de console gebruiken is het essentieel dat het gebuilde programma wordt uitgevoerd wordt via de command line via java. Niet via de Netbeans 'play' knop.

Bovendien moet manueel (omdat er momenteel iets misloopt in het programma - wat we niet op tijd kunnen oplossen) de map voor de logbestanden worden aangemaakt via
```
mdir /root/verkeer/logs
```
in de Terminal.

## Richtlijnen uitvoeren programma
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
  
  



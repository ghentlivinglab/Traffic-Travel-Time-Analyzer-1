# Verkeercentrum Gent (groep 1)
## Installatie handleiding

Onze applicatie bestaat uit 2 delen: de REST api, die zorgt voor de communicatie met de server, en de polling applicatie: deze zorgt dat we de data van de verschillende providers elke 5 minuten opvragen en opslaan. Dit stuk legt uit hoe we de code kunnen compileren en op een server uitvoeren.

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
* Cron
* Screen commando. Installeren kan via ```sudo apt-get install screen```. [Meer info](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-screen-on-an-ubuntu-cloud-server)

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

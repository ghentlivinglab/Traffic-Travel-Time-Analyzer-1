# Verkeercentrum Gent (groep 1)
## Installatie handleiding

Onze applicatie bestaat uit 2 delen: de REST api, die zorgt voor de communicatie met de server, en de polling applicatie: deze zorgt dat we de data van de verschillende providers elke 5 minuten opvragen en opslaan. Dit stuk legt uit hoe we de code kunnen compileren en op een server uitvoeren.

### Vereisten
* Ubuntu 16.04 LTS / Fedora 23 
* MySQL/MariaDb
* Glassfish 4.0 of 4.1 (geen 4.1.1!)
* Java 8 (of hoger)
* Laatste release: verkeer_v_2.tar.gz

Informatie over de installatie van Java 8, Glassfish 4.0, Glassfish 4.1 en MySQL/MariaDb vind je onderaan.

### Installatie

Uitpakken van de laatste release
``` 
tar -xvzf path_to/verkeer_v_2.tar.gz 
```
De huidige workdirectory bevat nu een directory 'verkeer' met een script 'configure'. Voer dit bestand als root uit.
``` 
cd verkeer
sudo bash configure 
```
Voer het root wachwoord van MySQL/MariaDb in (2 keer) voor de initialisatie van de databank.
```
Creating database. Prepare to enter your MySQL root password.
Enter password:
Enter password:
```
Voer indien gevraagd het Glassfish admin wachtwoord in om een connectionpool aan te maken en te testen.

Geef aan of je al dan niet wenst om de applicatie in een andere directory te installeren.
```
Do you want to change the install directory (default: /opt/verkeer)? [yes/no] 
```
Geef aan of je de cronjobs aan crontab wil toevoegen. Doe dit alleen als dit nog niet is gebeurd!
```
Are you sure you want to add cronjobs? Mare sure you didn't do this already. [yes/no] 
```
Als laatste heb je de keuze om de applicatie meteen in Glassfish te deployen. (niet verplicht)
``` 
Do you wish to deploy the application now? [yes/no] 
```

### Starten van de applicatie
Om de polling applicatie te starten voer je het volgende commando uit:
```
verkeer
```
De applicatie zal gestart worden in een andere terminal, gebruik maken van het programma 'screen'.
Op deze manier blijft de applicatie actief wanneer de ssh-verbinding wordt gesloten.

Om de webapplicatie de deployen:
```
verkeer -d
```

Extra opties om aan het 'verkeer'-script mee te geven:
* -a : verbind met de terminal waar de polling applicatie in draait.
* -i : start de applicatie en verbind meteen met de terminal waar de polling applicatie in draait.
* -f : forceer de start van de polling applicatie. Als de applicatie al draait wordt deze herstart.
* -s : stop de polling applicatie
* -d : deploy de webapplicatie in glassfish

# Verkeercentrum Gent (groep 1)
## Installatie handleiding

Onze applicatie bestaat uit 2 delen: de REST api, die zorgt voor de communicatie met de server, en de polling applicatie: deze zorgt dat we de data van de verschillende providers elke 5 minuten opvragen en opslaan. Dit stuk legt uit hoe we de code kunnen compileren en op een server uitvoeren.

### Vereisten
* Ubuntu 16.04 LTS / Fedora 23 
* MySQL/MariaDb
* Glassfish 4.0 of 4.1 (geen 4.1.1!)
* Java 8 (of hoger)
* Laatste release: verkeer_v_2.tar.gz

### Installatie

Uitpakken van het archive:
``` 
tar -xvzf verkeer_v_2.tar.gz 
```
In de 'verkeer' directory, voer het 'configure' bestand uit:
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
Als laatste heb je de keuze om de applicatie meteen in Glassfish te deployen.
``` 
Do you wish to deploy the application now? [yes/no] 
```






Installatie Ubuntu: 
``` 
sudo apt-get install mysql-server 
```
Installatie Fedora: 
``` 
sudo yum install mysql-server 
```

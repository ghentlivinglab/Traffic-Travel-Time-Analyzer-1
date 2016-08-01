if [ $# -eq 3 ]
  then
    echo "Mysql database initialiseren op $1, $2:$3"
else
    echo "Paramters ontbreken: ./init-database.sh server-ip mysql-username mysql-password"
    exit
fi


echo "Set password = root, en maak nieuwe database verkeer1"
(mysql --user=$2 --password=$2 < sql/init.sql) > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "Done."
else
    echo "Failed.\nInitialiseren mislukt."
    exit
fi

echo "Tabellen en inhoud importeren"
(mysql --user=$2 --password=$2 verkeer1 < sql/dump.sql) > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "Done."
else
    echo "Failed.\nInitialiseren mislukt."
    exit
fi
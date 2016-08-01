if [ $# -eq 3 ]
  then
  echo "Deployen op server $1\nGlassfish:\nUsername=$2\nPassword=$3\n"
else
    echo "Onjuist aantal parameters. \n\nVoorbeeld\n./deploy-rest.sh 146.185.150.100 username wachtwoord"
    exit
fi

ssh root@$1 'mkdir -p /root/verkeerREST/ > /dev/null 2>&1 | rm /root/verkeerREST/VerkeerREST.war > /dev/null 2>&1' 
echo "WAR bestand verplaatsen..."
scp -rp ../VerkeerREST/dist/VerkeerREST.war root@$1:/root/verkeerREST/VerkeerREST.war &> /dev/null

if [ $? -eq 0 ]; then
    echo "Done."
else
    echo "Failed.\nDeployment mislukt."
    exit
fi

echo "Deployment starten."

ssh root@$1 "echo 'AS_ADMIN_PASSWORD=$3' > /root/verkeerREST/password.txt | /opt/glassfish4/bin/asadmin --user '$2' --passwordfile /root/verkeerREST/password.txt deploy --force=true /root/verkeerREST/VerkeerREST.war &> /dev/null"


if [ $? -eq 0 ]; then
    echo "Done.\nDeployment gelukt"
else
    echo "Failed.\nDeployment mislukt. Check de gebruikersnaam en wachtwoord."
fi

ssh root@$1 'rm /root/verkeerREST/password.txt'

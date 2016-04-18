if [ $# -eq 0 ]
  then
    echo "Geen ip adres van de server opgegeven.\nGebruik bv. ./deploy-polling.sh 146.185.150.100"
    exit
fi

echo "Bestanden verplaatsen..."
scp -rp ../Verkeer/dist/* root@$1:/root/verkeer

if [ $? -eq 0 ]; then
    echo "Done.\nManuele deployment nog noodzakelijk."
else
    echo "Failed.\nDeployment mislukt."
fi

# CD is noodzakelijk -> log bestanden naar juiste plaats (working directory)
#ssh -t root@$1 "screen -ls | grep pts | cut -d. -f1 | awk '{print $1}' | xargs kill"

#ssh -t root@$1 "cd /root/verkeer/ | screen java -Xmx256m -jar ./Verkeer.jar"
#ssh root@$1 "cd /root/verkeer; nohup java -Xmx256m -jar ./Verkeer.jar > /dev/null 2>&1 &"
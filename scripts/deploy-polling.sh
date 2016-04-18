if [ $# -eq 0 ]
  then
    echo "Geen ip adres van de server opgegeven.\nGebruik bv. ./deploy-polling.sh 146.185.150.100"
    exit
fi

scp -rp ../Verkeer/dist/* root@$1:/root/verkeer

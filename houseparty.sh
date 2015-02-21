cd /root/indiedisco
timestamp=$( date +%y%m%d )
echo $timestamp >> /var/log/houseparty.log
path="/var/www/houseparty/House_Party_"$timestamp"_Part_1"
path2="/var/www/houseparty/House_Party_"$timestamp"_Part_2"
log=$path".log"
echo $log >> /var/log/houseparty.log
echo $path >> /var/log/houseparty.log
echo $path2 >> /var/log/houseparty.log
cd streamripper-1.64.6
./streamripper http://pri.gocaster.net/sp -l 3600 -a $path > $log
./streamripper http://pri.gocaster.net/sp -l 3600 -a $path2 > $log
cd ../java
ls /var/www/houseparty | while read -r FILE
do
    mv -v "/var/www/houseparty/$FILE" `echo /var/www/houseparty/$FILE | tr ' ' '_' `
done
echo "generating feed.xml" >> /var/log/houseparty.log
/root/jdk1.7.0_60/bin/java -cp bin CreateRssFeed /root/indiedisco/cfg/houseparty/houseparty.properties true
cp feed.xml /var/www/houseparty/feed.xml


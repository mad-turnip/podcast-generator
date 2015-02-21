cd /root/indiedisco
timestamp=$( date +%y%m%d )
echo $timestamp >> /var/log/indiedisco.log
path="/var/www/indiedisco/Indie_Disco_"$timestamp"_Part_1"
path2="/var/www/indiedisco/Indie_Disco_"$timestamp"_Part_2"
log=$path".log"
echo $log >> /var/log/indiedisco.log
echo $path >> /var/log/indiedisco.log
echo $path2 >> /var/log/indiedisco.log
cd streamripper-1.64.6
./streamripper http://pri.gocaster.net/sp -l 3600 -a $path > $log
./streamripper http://pri.gocaster.net/sp -l 3600 -a $path2 > $log
cd ../java
ls /var/www/indiedisco | while read -r FILE
do
    mv -v "/var/www/indiedisco/$FILE" `echo /var/www/indiedisco/$FILE | tr ' ' '_' `
done
echo "generating feed.xml" >> /var/log/indiedisco.log
/root/jdk1.7.0_60/bin/java -cp bin CreateRssFeed
cp feed.xml /var/www/indiedisco/feed.xml


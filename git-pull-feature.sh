echo "dailyfeed-code"
cd dailyfeed-code
git pull origin $1
cd ..
echo ""

echo "dailyfeed-batch"
cd dailyfeed-batch
git pull origin $1
cd ..
echo ""


echo "dailyfeed-deadletter-support"
cd dailyfeed-deadletter-support
git pull origin $1
cd ..
echo ""


echo "dailyfeed-kafka-support"
cd dailyfeed-kafka-support
git pull origin $1
cd ..
echo ""


echo "dailyfeed-pvc-support"
cd dailyfeed-pvc-support
git pull origin $1
cd ..
echo ""


echo "dailyfeed-redis-support"
cd dailyfeed-redis-support
git pull origin $1
cd ..
echo ""
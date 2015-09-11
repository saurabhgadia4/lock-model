#!/bin/sh

if [ ! -e "${JPF_HOME}/build/jpf.jar" ]
then
	echo "Make sure JPF_HOME is set and that JPF is compiled to jpf.jar."
	exit
fi

if [ ! -e "$1" ]
then
	echo "Usage: ./jpf.sh <file with lock permutations>"
	exit
fi

[ -d logs ] || mkdir logs

for p in `cat "$1"`
do
	p1="`echo $p | sed -e 's/...$//'`"
	p2="`echo $p | sed -e 's/^...//'`"
	echo ====== Lock indices uses by threads 1, 2 = ${p1} ${p2} ======
	time $JPF_HOME/bin/jpf +verbose Locks.jpf +target=harness.Environment2 +target.args=${p1},${p2} >& logs/jpf-${p1}-${p2}.log
done

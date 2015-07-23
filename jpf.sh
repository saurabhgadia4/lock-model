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
	p1="`echo $p | sed -e 's/....$//'`"
	p2="`echo $p | sed -e 's/^..//' -e 's/..$//'`"
	p3="`echo $p | sed -e 's/^....//'`"
	echo ====== Lock indices uses by threads 1, 2, 3 = ${p1} ${p2} ${p3} ======
	time $JPF_HOME/bin/jpf +verbose Locks.jpf +target.args=${p1},${p2},${p3} >& log1/jpf-${p1}-${p2}-${p3}.log
done

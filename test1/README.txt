
Test Configuration			
	Thread Id	Mutex	Priority
	thread 0 	l0, l1	3
	thread 1	l0	    2

Trace Summary:
 https://docs.google.com/spreadsheets/d/1daZNurvQkJFFZYmuWv1SwLPrV1-E1zUk4ARcDMxIAPo/edit#gid=0

Result: 
 Detects data race condition for RTEMSThread.currentPriority data member.
APPS=simpletest.c sleeptest.c queuetest.c

CFLAGS=-Wall -g -I ../include 
DYNLIB=/usr/local/lib/libuthread.so
LIB=/usr/local/lib/libuthread.a

dynamic: $(APPS)
	gcc -o simpletest $(CFLAGS) simpletest.c $(DYNLIB)
	gcc -o sleeptest $(CFLAGS) sleeptest.c $(DYNLIB)
	gcc -o queuetest $(CFLAGS) queuetest.c $(DYNLIB)
	
static : $(APPS)
	gcc -o simpletest $(CFLAGS) simpletest.c $(LIB)
	gcc -o sleeptest $(CFLAGS) sleeptest.c $(LIB)
	gcc -o queuetest $(CFLAGS) queuetest.c $(DYNLIB)
	
clean:
	rm -rf simpletest sleeptest queuetest

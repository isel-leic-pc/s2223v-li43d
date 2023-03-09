APPS=simpletest.c event_test.c queue_test.c

CFLAGS=-Wall -g -I ../include 
DYNLIB=/usr/local/lib/libuthread.so
LIB=/usr/local/lib/libuthread.a

dynamic: $(APPS)
	gcc -o simpletest $(CFLAGS) simpletest.c $(DYNLIB)
	gcc -o event_test $(CFLAGS) event_test.c $(DYNLIB)
	gcc -o queue_test $(CFLAGS) queue_test.c $(DYNLIB)
	
	
static : $(APPS)
	gcc -o simpletest $(CFLAGS) simpletest.c $(LIB)
	gcc -o event_test $(CFLAGS) event_test.c $(LIB)
	gcc -o queue_test $(CFLAGS) queue_test.c $(LIB)
	
clean:
	rm -rf simpletest event_test queue_test

all: libuthread.so libuthread.a

libuthread.so: uthread.o ctxswitch.o semaphore.o intqueue.o
	gcc -o libuthread.so -shared uthread.o semaphore.o intqueue.o ctxswitch.o

libuthread.a: uthread_nogot.o ctxswitch.o semaphore_nogot.o intqueue_nogot.o
	ar rcv libuthread.a uthread_nogot.o semaphore_nogot.o  intqueue_nogot.o ctxswitch.o

#
# uthread core
#
uthread.o: uthread.c
	gcc -c -g -fPIC -Wall -I ../include uthread.c
	
semaphore.o: semaphore.c
	gcc -c -g -fPIC -Wall -I ../include semaphore.c

intqueue.o: intqueue.c
	gcc -c -g -fPIC -Wall -I ../include intqueue.c
	
uthread_nogot.o : uthread.c
	gcc -c -o uthread_nogot.o -g -Wall -I ../include uthread.c

semaphore_nogot.o: semaphore.c
	gcc -c -o semaphore_nogot.o -g  -Wall -I ../include semaphore.c
	
intqueue_nogot.o: intqueue.c
	gcc -c -o intqueue_nogot.o -g  -Wall -I ../include intqueue.c
	
ctxswitch.o: ctxswitch.s
	as -o ctxswitch.o -c ctxswitch.s

install:
	sudo cp libuthread.a libuthread.so /usr/local/lib
		
clean:
	rm -rf *.o *.so *.a

	

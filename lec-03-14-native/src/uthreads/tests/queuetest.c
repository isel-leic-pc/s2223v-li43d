#include <unistd.h>
#include <stdio.h>

#include "uthread.h"
#include "usynch.h"

#define MAX_OPERS 30

void sender(void * arg) {
	intqueue_t * q  = (intqueue_t *) arg;

	printf("sender thread start\n");
	for(int i=0; i < MAX_OPERS; ++i) {
		
	    iq_put(q, i + 1);
	    printf("sender put %d\n", i + 1);
	}

	printf("sender thread end\n");
}


void receiver(void * arg) {
	intqueue_t * q  = (intqueue_t *) arg;

	printf("receiver thread start\n");

	for(int i=0; i < MAX_OPERS; ++i) {
		sleep(2);
        int v = iq_get(q);
        printf("receiver  get %d\n", v);
    }

	printf("receiver thread end\n");
}

void queue_test() {
	intqueue_t queue;

	printf("queue test  begin\n");

	iq_init(&queue);

	ut_create(sender, &queue);
	ut_create(receiver, &queue);

	ut_run();

	printf("queue test  end\n");
}


int main() {
	ut_init();
	queue_test();
	ut_end();
    return 0;
}

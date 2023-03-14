#include <string.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define NTRIES 100

pthread_t pthread1, pthread2;
	
void *func(void * arg) {
	char *msg = (char*) arg;
	
	printf("worker thread = %ld\n", pthread_self());
	sleep(2);
	
	for(int i=0; i < NTRIES; ++i) {
		//if (pthread_self() == pthread1) sleep(1);
		for(int j=0; j < strlen(msg); j++) {
			putc(msg[j], stdout);
			fflush(stdout);
			//usleep(100000);
		}		
	}
		
	return NULL;
}

int main() {

	
	
	printf("main thread = %ld\n", pthread_self());
	
	pthread_create(&pthread1, NULL, func, "first");  
	pthread_create(&pthread2, NULL, func, "second");  
 
    pthread_join(pthread1, NULL);
	pthread_join(pthread2, NULL);
	
	return 0;
}

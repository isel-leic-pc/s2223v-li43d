#include <unistd.h>
#include <stdio.h>
#include <stdbool.h>
#include <limits.h>
#include <string.h>
#include <stdlib.h>

#include "uthread.h"
#include "usynch.h"
#include "list.h"


#define MAX_MSG 128

typedef struct queue {
	list_entry_t head;
	semaphore_t elem_avaiable;
	semaphore_t space_avaiable;
} queue_t;


typedef struct node {
	list_entry_t link;
	char msg[MAX_MSG];
} node_t;


void queue_init(queue_t *q, int max_size) {
	init_list_head(&q->head);
	sem_init(&q->elem_avaiable, 0, max_size);
	sem_init(&q->space_avaiable, max_size, max_size);
}

node_t * node_create(char *msg) {
	printf("sizeof(node_t)=%ld\n", sizeof(node_t));
	node_t *node = malloc(sizeof(node_t));
	strncpy(node->msg, msg, MAX_MSG);
	node->msg[MAX_MSG-1] = 0; // force terminator
	return node;
}

void queue_put(queue_t *q, char *msg) {
	sem_wait(&q->space_avaiable,1);
	node_t *node = node_create(msg);
	insert_list_last(&q->head, &node->link);
	sem_post(&q->elem_avaiable, 1);
}	


void queue_get(queue_t *q, char *msg) {
	sem_wait(&q->elem_avaiable,1);
	node_t *node = container_of(remove_list_first(&q->head), node_t, link);
	sem_post(&q->space_avaiable, 1);
	strncpy(msg, node->msg, MAX_MSG);
	free(node);
	
}	

void sender(void * arg) {
	queue_t *queue = arg;
	
	printf("sender thread begin\n");
	 

	queue_put(queue, "hello, ");
	ut_yield();
	
	char msg[MAX_MSG];
	
	sprintf(msg, "from thread %p", ut_self());
	queue_put(queue, msg);
	queue_put(queue, "");
	printf("sender thread end\n");
}


void receiver(void * arg) {
	queue_t *queue = arg;
	char msg[MAX_MSG];
	
	printf("receiver thread  begin\n");
	
	while(true) {
		printf("receiver: ask for more...\n");
		queue_get(queue, msg);
		if (msg[0] == 0) break; // empty msg
		printf("received: '%s'\n", msg);
		
	}
	
	printf("receiver thread  end\n");
}

void queue_test() {
	queue_t queue;
	
	queue_init(&queue, 100);
	
	printf("queue test  begin\n");
	
	 
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

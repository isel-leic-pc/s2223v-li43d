// simpletest.c : Defines the entry point for the console application.
//

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include "uthread.h"


/////////////////////////////////////////////
//
// CCISEL 
// 2007-2019
//
// UThread Library First Test
//
// Jorge Martins, 2019
//
////////////////////////////////////////////

#define DEBUG

#define MAX_THREADS 10


/////////////////////////////////////////////////////////////
//                                                         //
// Test 1: N threads, each one printing its number M times //
//                                                         //
/////////////////////////////////////////////////////////////
 

 
// Sleep test

void func1(void * arg) {

	printf("Start func1\n");
	
	for(int i=1; i < 20; ++i) {
		ut_sleep(100);
		printf("%d\n", i);
	}
 
	printf("End func1\n");

}

void func2(void * arg) {
	printf("Start func2\n");
	ut_sleep(1000);
	printf("End func2\n");
}

void test2() {
	printf("\n :: Test 2 - BEGIN :: \n\n");
	
	ut_create(func1, NULL);
	ut_create(func2, NULL);
	ut_run();

	printf("\n\n :: Test 2 - END :: \n");
}



int main () {
	ut_init();
 
	test2();	
	 
	ut_end();
	return 0;
}



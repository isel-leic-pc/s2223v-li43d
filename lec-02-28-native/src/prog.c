#include <stdio.h>
#include <stdlib.h>
int global = 5;

int main(int argc, char *argv[]) {
	int local = 2;
	
	printf("&main=%p\n", main);
	printf("&global=%p\n", &global);
	printf("&local=%p\n", &local);
	printf("global value before set=%d\n", global);
	if (argc == 2) 
		global = atoi(argv[1]);
	
	printf("press return to continue...");
	getchar();
	
	printf("global value after set=%d\n", global);
	printf("press return to continue...");
	getchar();
	return 0;
	
}
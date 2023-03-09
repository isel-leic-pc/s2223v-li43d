/////////////////////////////////////////////////////////////////
//
// CCISEL 
// 2007-2021
//
// UThread library:
//   User threads supporting cooperative multithreading.
//
// Authors:
//   Carlos Martins, João Trindade, Duarte Nunes, Jorge Martins
// 

#include "uthread.h"
#include "usynch.h"
#include "waitblock.h"

void auto_event_init(auto_reset_event_t * event, bool initial_state) {
	event->signaled = initial_state;
	init_list_head(&event->waiters);
}


void auto_event_wait (auto_reset_event_t * event) {
	if (event->signaled == true) {
		event->signaled = false;
	}
	else {
		waitblock_t wblock;
		init_waitblock(&wblock);
		insert_list_last(&event->waiters, &wblock.entry);
		ut_deactivate();
	}
	
}


void auto_event_set(auto_reset_event_t * event) {
	 
	if (is_list_empty(&event->waiters)) {
		event->signaled = true;
	}
	else {
		waitblock_t *wb = container_of(remove_list_first(&event->waiters), waitblock_t, entry);
		ut_activate(wb->thread);
	}
	 
}

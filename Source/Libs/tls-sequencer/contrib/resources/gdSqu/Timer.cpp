#include <hbtimewrap.h>
#include "Timer.h"
#include <time.h>
#include <stdio.h>
#include <limits.h>
/* deprecated.. #include <Tracey.h>*/
#ifndef WIN32
#include <iostream.h>
#else
#include "iostream"
#endif
//#include "misc.h"

extern FILE *dbg;

Timer::Timer (TimerMode mode)
{
	//StartFunc();
	last = 0;
	delete_one_shots = 0;
	in_update = 0;
	switch (mode) {
		case TimerNormal: break;
		case TimerDeletesSingleShots: delete_one_shots = 1;
	}
}

int	Timer::insert (TimerRequest &rq, TimerDrain* td)
{
	//StartFunc();
	TimerRequest_inTimer *neu = new TimerRequest_inTimer;
	neu->sec_from_now	= rq.sec_from_now;
	neu->sec_repeat	= rq.sec_repeat;
	neu->user_val		= rq.user_val;
	neu->td				= td;
	neu->delta_sec		= rq.sec_from_now;
	
	return (_insert (neu));
}

int Timer::_insert (TimerRequest_inTimer *neu)
{
	//StartFunc();
	//cout << "TQ_inl: " << TimerQueue.GetNum() << endl;
	register unsigned long i, limit = TimerQueue.GetNum ();
	if (!limit) {
		//das erste einfuegen in den timer
		start ();
	}
	for ( i = 0; i < limit; i++ ) {
		if ( neu->delta_sec < TimerQueue[i]->delta_sec ) {
			TimerQueue[i]->delta_sec -= neu->delta_sec;
			// would abort()... if ( !TimerQueue.Add () ) return (0);
			abort();
			TimerQueue.ShiftInsert (i);
			TimerQueue[i] = neu;
			break;
		}
		neu->delta_sec -= TimerQueue[i]->delta_sec;
	}
	if ( i >= limit ) TimerQueue.Add (neu);
	//fprintf ( dbg, "inserted td=0x%p %ld\n", neu->td, neu->delta_sec ); fflush (dbg);
	return (1);
}

void Timer::start ()
{
	//StartFunc();
	last = hb_time (0L);
}

void Timer::update ()
{
	//StartFunc();
	if (in_update) return;
	in_update = 1;
	long now = hb_time (0L);
	long secondsGoneBy = now - last;
	last = now;
	register unsigned long i, limit = TimerQueue.GetNum ();
	i = 0;
	while ( secondsGoneBy > 0 && i < limit ) {
		if ( TimerQueue[i]->delta_sec < secondsGoneBy ) {
			secondsGoneBy -= TimerQueue[i]->delta_sec;
			TimerQueue[i]->delta_sec = 0;
		}
		else {
			TimerQueue[i]->delta_sec -= secondsGoneBy;
			secondsGoneBy = 0;
		}
		i++;
	}
	i = 0;
	while ( i < TimerQueue.GetNum () && TimerQueue[i]->delta_sec == 0 ) {
		//fprintf ( dbg, "timeout td=0x%p\n", TimerQueue[i]->td ); fflush (dbg);
		removed = 0;
		TimerQueue[i]->td->TimerExpired (now, TimerQueue[i]->user_val);
		if (removed) i--; //fuer den fall, dass TimerExpired sich loescht
		i++;
	}
	//cout << "TQup-cl: " << TimerQueue.GetNum() << endl;
	while ( TimerQueue.GetNum () && TimerQueue[0]->delta_sec == 0 ) {
		TimerRequest_inTimer *tmp = TimerQueue[0];
		TimerQueue.Rem (0UL);
		//fprintf ( dbg, "removed td=0x%p\n", tmp->td ); fflush (dbg);
		if ( tmp->sec_repeat > 0 ) {
			tmp->delta_sec = tmp->sec_repeat;
			_insert (tmp);
			//fprintf ( dbg, "repeated td=0x%p %ld\n", tmp->td, tmp->sec_repeat ); fflush (dbg);
		}
		else {
			//fprintf ( dbg, "deleted td=0x%p\n", tmp->td ); fflush (dbg);
			if (delete_one_shots) {
				delete tmp->td;
			}
			delete tmp;
		}
	}
	TimerQueue.Shrink2Fit ();
	in_update = 0;
}

void	Timer::remove (TimerDrain* td)
{
	//StartFunc();
	register unsigned long i;
	update ();
	for ( i = 0; i < TimerQueue.GetNum (); i++ ) {
		if ( TimerQueue[i]->td == td ) {
			if ( i < TimerQueue.GetNum () - 1 )
				TimerQueue[i+1]->delta_sec += TimerQueue[i]->delta_sec;
			delete TimerQueue[i];
			removed = 1;
			TimerQueue.Rem (i);
			i--;
		}
	}
}

void	Timer::remove (TimerDrain* td, unsigned long uval)
{
	//StartFunc();
	register unsigned long i;
	update ();
	for ( i = 0; i < TimerQueue.GetNum (); i++ ) {
		if ( TimerQueue[i]->td == td && uval == TimerQueue[i]->user_val ) {
			if ( i < TimerQueue.GetNum () - 1 )
				TimerQueue[i+1]->delta_sec += TimerQueue[i]->delta_sec;
			delete TimerQueue[i];
			removed = 1;
			TimerQueue.Rem (i);
			i--;
		}
	}
}

unsigned long	Timer::getNextDelay ()
{
	//StartFunc();
	if ( !TimerQueue.GetNum () ) return (LONG_MAX);
	return (TimerQueue[0]->delta_sec);
}

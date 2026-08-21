#ifndef I_TIMER_H
#define I_TIMER_H

#include "SEArrayT.h"

class TimerDrain {
	private:
		char dummy;
	public:
		virtual ~TimerDrain( ) {}
		virtual void TimerExpired( long now, unsigned long uval) =0;
#ifdef TIMER_DEBUG
		virtual void Dump( FILE *f =stdout) =0;
#endif
};

struct TimerRequest {
	long sec_from_now;
	long sec_repeat;
	unsigned long user_val;
	TimerRequest( ) { sec_from_now = sec_repeat = 0; user_val = 0; }
};

struct TimerRequest_inTimer : public TimerRequest {
	long delta_sec;
	TimerDrain *td;
};

enum TimerMode { TimerNormal, TimerDeletesSingleShots };
class Timer {
	private:
		SEArray<TimerRequest_inTimer*> TimerQueue;
		int _insert( TimerRequest_inTimer* );
		long last;
		char delete_one_shots:1;
		char in_update:1;
		char removed:1;
	public:
		Timer( TimerMode =TimerNormal );
		int	insert( TimerRequest &rq, TimerDrain* td );
		void	remove( TimerDrain* td );
		void	remove( TimerDrain* td, unsigned long uval );
		void	update();
		unsigned long	getNextDelay();
		unsigned long	getDelayFor( TimerDrain* td );
		unsigned long	getDelayFor( TimerDrain* td, unsigned long uval );
		void	start( );
#ifdef TIMER_DEBUG
		void Dump( FILE *f =stdout )
		{
			register unsigned long i;
			for (i = 0; i < TimerQueue.GetNum(); i++) {
				fprintf( f, "in %ld seconds:\n", TimerQueue[i]->delta_sec );
				TimerQueue[i]->td->Dump( f );
			}
		}
#endif
};

#endif

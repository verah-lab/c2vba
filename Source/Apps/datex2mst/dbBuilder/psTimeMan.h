#ifndef _PSTIMEMAN_H_
#define _PSTIMEMAN_H_


#include <string>
#include "time.h"

using namespace std;



class TimeManager

{
public:
	enum kind_of_time
	{
		TM_HOUR,
		TM_DAY,	
		TM_MONTH,	
		TM_YEAR,

		TM_5MINUTE,
		TM_QUARTERHOUR,
		TM_MINUTE
	};

	static const string TSPEC_HOUR; 
	static const string TSPEC_DAY;
	static const string TSPEC_MONTH;
	static const string TSPEC_YEAR;
	static const string TSPEC_QUARTER_HOUR;
	static const string TSPEC_5MINUTE;
	static const string TSPEC_MINUTE;

	static const int TSPAN_YEAR;
	static const int TSPAN_HALF_YEAR;
	static const int TSPAN_MONTH;
	static const int TSPAN_HALF_MONTH;
	static const int TSPAN_DAY;
	static const int TSPAN_HALF_DAY;
	static const int TSPAN_HOUR;
	static const int TSPAN_HALF_HOUR;
	static const int TSPAN_QUARTER_HOUR;
	static const int TSPAN_HALF_QUARTER_HOUR;
	static const int TSPAN_5MINUTE;
	static const int TSPAN_HALF_5MINUTE;
	static const int TSPAN_MINUTE;
	static const int TSPAN_HALF_MINUTE;

	time_t locSec_to_UTCSec(time_t T); 
	time_t UTCSec_to_locSec(time_t T);

	string get_time_str( time_t t, kind_of_time kind ) const;
	
	static TimeManager* getTimeManager()
	{
		if( !_instance )
			_instance = new TimeManager();
		return _instance;
	};
	
	string get_date_str( time_t t = 0 ) const;
	string get_time_str( time_t t = 0 ) const;
	string get_date_time_str( time_t t = 0 ) const;

	string get_time_str_raw( time_t t = 0 ) const;
	string get_date_str_raw( time_t t );
	string get_date_time_str_raw( time_t t = 0 ) const;

	string get_1interval_string( time_t from, time_t to,
										  const string& tSpec, int tLim ) const;
	string get_interval_string( time_t from, time_t to,
										 const string& tSpec, int tLim,
										 bool round = true ) const;
	void adjust_times( time_t& from, time_t& to,
							 const string& tSpec, int tLim ) const;


	time_t begin_of_day_localtime( time_t t1 , int days = 0 ) const;
	time_t begin_of_month_localtime( time_t t1 , int months = 0 ) const;
	time_t begin_of_year_localtime( time_t t1 , int years = 0 ) const;

private:
	TimeManager() {};
	static TimeManager* _instance;  
	
	string get_month_str( unsigned m ) const;
	string get_day_str( unsigned d ) const;
};


#define TIME_MAN (*(TimeManager::getTimeManager()))


#endif

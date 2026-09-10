#include "hbtimewrap.h"
#include "psTimeMan.h"
#include "psString.h"
#include <iostream>

#include <algorithm>



static char* gr_mon_str[12] =
{
   "Januar",
   "Februar",
   "März",
   "April",
   "Mai",
   "Juni",
   "Juli",
   "August",
   "September",
   "Oktober",
   "November",
   "Dezember"
};


static char* gr_day_str[7] =
{
   "So",
	"Mo",
	"Di",
	"Mi",
	"Do",
	"Fr",
	"Sa"
};




TimeManager* TimeManager::_instance = 0;  

const string TimeManager::TSPEC_HOUR			= "tfHour";
const string TimeManager::TSPEC_DAY				= "tfDay";
const string TimeManager::TSPEC_MONTH			= "tfMonth";
const string TimeManager::TSPEC_YEAR			= "tfYear";
const string TimeManager::TSPEC_QUARTER_HOUR	= "tfQuarterHour"; 
const string TimeManager::TSPEC_MINUTE			= "tfMinute";
const string TimeManager::TSPEC_5MINUTE		= "tf5Minute";

const int TimeManager::TSPAN_YEAR = (365 * 86400);
const int TimeManager::TSPAN_HALF_YEAR = (365 * 43400);
const int TimeManager::TSPAN_MONTH = (30 * 86400);
const int TimeManager::TSPAN_HALF_MONTH = (30 * 43400);
const int TimeManager::TSPAN_DAY = 86400;
const int TimeManager::TSPAN_HALF_DAY = 43000;
const int TimeManager::TSPAN_HOUR = 3600;
const int TimeManager::TSPAN_HALF_HOUR = 1800;
const int TimeManager::TSPAN_QUARTER_HOUR = 900;
const int TimeManager::TSPAN_HALF_QUARTER_HOUR = 450;
const int TimeManager::TSPAN_5MINUTE = 300;
const int TimeManager::TSPAN_HALF_5MINUTE = 150;
const int TimeManager::TSPAN_MINUTE = 60;
const int TimeManager::TSPAN_HALF_MINUTE = 30;



time_t TimeManager::locSec_to_UTCSec(time_t T) 

{
  tm tstruct1 = *hb_gmtime(&T);
  tm tstruct2 = *hb_localtime(&T);

  long altzone;
  short sign;

  sign = 1;
  altzone = tstruct1.tm_hour - tstruct2.tm_hour;
  if ( altzone < 0 )
    sign = -1;
  altzone = abs(altzone);

  if ( altzone > 12 )
  {
    altzone = 24 - altzone;
    if( sign == 1 )
       sign = -1;
  }

  return T + sign * altzone * 3600;
};


time_t TimeManager::UTCSec_to_locSec(time_t T) 

{
  time_t ht=locSec_to_UTCSec(T);
  return T-(ht-T);
};



string TimeManager::get_time_str( time_t t, kind_of_time kind ) const
{
	tm *T = hb_localtime ( &t );

	if( !T )
		return "";

   switch( kind )
	{
	  case TM_HOUR:
		  return get_date_time_str_raw( t );
	  case TM_DAY:
		  return T->tm_mday + string(". ") + get_month_str( T->tm_mon ) + " " + (T->tm_year + 1900);
	  case TM_MONTH:
		  return get_month_str( T->tm_mon ) + " " + (T->tm_year + 1900);
	  case TM_YEAR:
		  return string("") + (T->tm_year + 1900);
	  default:
		  return "";
	}

	return "";
};

string TimeManager::get_month_str( unsigned m ) const
{
	if( m < 12 )
		return gr_mon_str[m];

	cerr << "!!! error in TimeManager::get_month_str() !!!" << endl;
	cerr << "invalid tm_mon: " << m << endl;
	return "";  
}

string TimeManager::get_day_str( unsigned d ) const
{
	if( d < 7 )
		return gr_day_str[d];

	cerr << "!!! error in TimeManager::get_day_str() !!!" << endl;
	cerr << "error: invalid tm_wday: " << d << endl;
	return "";  
}


string TimeManager::get_date_str( time_t t ) const
{
	time_t tt = t;
	if( !tt )
		tt = ::time(0);
	tm* T = localtime ( & tt );

	if( !T )
		return "";

	return get_day_str( T->tm_wday ) + ", " +
			 T->tm_mday + ". " +
			 get_month_str(T->tm_mon) + " " +
			 (T->tm_year + 1900);
}

string TimeManager::get_time_str( time_t t ) const
{
	time_t tt = t;
	if( !tt )
		tt = ::time(0);
	tm* T = localtime ( & tt );

	if( !T )
		return "";

	return fillLeft( string("") + (T->tm_hour) , 2 , '0' ) + ":" +
			 fillLeft( string("") + (T->tm_min), 2 , '0' ) + " Uhr";
}

string TimeManager::get_date_time_str( time_t t ) const
{
	time_t tt = t;
	if( !tt )
		tt = ::time(0);
	tm* T = localtime ( & tt );

	if( !T )
		return "";

	return string("") + T->tm_mday + ". " +
			 get_month_str(T->tm_mon) + " " +
			 (T->tm_year + 1900) + ", " +
			 fillLeft( string("") + (T->tm_hour) , 2 , '0' ) + ":" +
			 fillLeft( string("") + (T->tm_min), 2 , '0' );
}


string TimeManager::get_date_time_str_raw( time_t t ) const

{
   tm* T = hb_localtime ( & t );

	if( !T )
		return "";

   return fillLeft( string("") + T->tm_mday , 2 , '0' ) + "." +
          fillLeft( string("") + (T->tm_mon + 1) , 2 , '0' ) + "." +
          fillLeft( string("") + (T->tm_year % 100) , 2 , '0' ) + " " +
          fillLeft( string("") + (T->tm_hour) , 2 , '0' ) + ":" +
          fillLeft( string("") + (T->tm_min), 2 , '0' ) + ":" +
          fillLeft( string("") + (T->tm_sec) , 2 , '0' );
};


string TimeManager::get_time_str_raw( time_t t ) const

{
   tm* T = hb_localtime ( & t );

	if( !T )
		return "";

   return fillLeft( string("") + (T->tm_hour) , 2 , '0' ) + ":" +
          fillLeft( string("") + (T->tm_min), 2 , '0' ) + ":" +
          fillLeft( string("") + (T->tm_sec) , 2 , '0' );
};


string TimeManager::get_date_str_raw( time_t t )
{
   tm* T = hb_localtime ( & t );

	if( !T )
		return "";

   return fillLeft( string("") + T->tm_mday , 2 , '0' ) + "." +
          fillLeft( string("") + (T->tm_mon + 1) , 2 , '0' ) + "." +
          fillLeft( string("") + (T->tm_year % 100) , 2 , '0' );
};



time_t TimeManager::begin_of_day_localtime( time_t t1 , int days ) const

{
	tm* T = hb_localtime ( & t1 );
	
	if( T )
	{
		tm TH = *T;
		
		TH.tm_hour	= 0;
		TH.tm_min 	= 0;
		TH.tm_sec 	= 0;
		
		TH.tm_mday   += days; 
		TH.tm_isdst   = -1;
		
		return hb_mktime ( &TH );
	}
	else
	{
		cerr << "!!! error in TimeManager::begin_of_day_localtime() !!!" << endl;
		cerr << "invalid time stamp: " << t1 << endl;
		return 0;
	}
	
	// wrong is:
	
	//return t1 - ( ( T->tm_hour *60 + T->tm_min  ) * 60 + T->tm_sec ); 
} 

time_t TimeManager::begin_of_month_localtime( time_t t1 , int months ) const

{
	tm* T = hb_localtime ( & t1 );
	
	if( T )
	{
		tm TH = *T;
		
		TH.tm_hour	= 0;
		TH.tm_min 	= 0;
		TH.tm_sec 	= 0;
		
		TH.tm_mday   = 1; 
		TH.tm_mon    += months; 
		TH.tm_isdst   = -1;
		
		return hb_mktime ( &TH );
	}
	else
	{
		cerr << "!!! error in TimeManager::begin_of_month_localtime() !!!" << endl;
		cerr << "invalid time stamp: " << t1 << endl;
		return 0;
	}
} 


time_t TimeManager::begin_of_year_localtime( time_t t1 , int years ) const

{
	tm* T = hb_localtime ( & t1 );
	
	if( T )
	{
		tm TH = *T;
		
		TH.tm_hour	= 0;
		TH.tm_min 	= 0;
		TH.tm_sec 	= 0;
		
		TH.tm_mday   = 1; 
		TH.tm_mon    = 0; 
		TH.tm_year   += years; 
		TH.tm_isdst   = -1;
		
		return hb_mktime ( &TH );
	}
	else
	{
		cerr << "!!! error in TimeManager::begin_of_year_localtime() !!!" << endl;
		cerr << "invalid time stamp: " << t1 << endl;
		return 0;
	}
} 



string TimeManager::get_1interval_string( time_t from, time_t to,
													   const string& tSpec, int tLim ) const
{
   time_t t;

	if( tSpec == TSPEC_YEAR )
	   t = begin_of_year_localtime( from, 1 );
	else if( tSpec == TSPEC_MONTH )
	   t = begin_of_month_localtime( from, 1 );
	else if( tSpec == TSPEC_DAY )
	   t = begin_of_day_localtime( from, 1 );
	else if( tSpec == TSPEC_HOUR )
      t = from / TSPAN_HOUR * TSPAN_HOUR + TSPAN_HOUR;
	else
		t = from;

	tm* T = localtime( &t );
	if( !T )
		return "";

	tm TM = *T;


	if( tSpec == TSPEC_YEAR )
		return string("") + ( 1900 + TM.tm_year);
	else if( tSpec == TSPEC_MONTH )
		return get_month_str( TM.tm_mon ) + " " + (1900 + TM.tm_year);
	else if( tSpec == TSPEC_DAY )
		return get_date_str( t );
//		fillLeft( string("") + TM.tm_mday, 2, '0' ) + "." + 
//				 fillLeft( string("") + (TM.tm_mon + 1), 2, '0' ) + "." + 
//           ( 1900 + TM.tm_year);
	else if( tSpec == TSPEC_HOUR )
		return 
				 get_date_time_str( t ) + " - " +
				 fillLeft( string("") + (TM.tm_hour + 1), 2, '0' ) + ":00";
/*
				 fillLeft( string("") + TM.tm_hour, 2, '0' ) + ":00, " + 
				 fillLeft( string("") + TM.tm_mday, 2, '0' ) + "." + 
				 fillLeft( string("") + (TM.tm_mon + 1), 2, '0' ) + "." +
				 (1900 + TM.tm_year) + " - " +
				 fillLeft( string("") + (TM.tm_hour + 1), 2, '0' ) + ":00, " + 
				 fillLeft( string("") + TM.tm_mday, 2, '0' ) + "." + 
				 fillLeft( string("") + (TM.tm_mon + 1), 2, '0' ) + "." +
				 (1900 + TM.tm_year);
*/
   else		
		return "";
}



string TimeManager::get_interval_string( time_t from, time_t to,
													  const string& tSpec, int tLim,
													  bool round ) const
{
	if( tLim == 1 )
		return get_1interval_string( from, to , tSpec, tLim );

   time_t t;

	if( tSpec == TSPEC_YEAR )
		t = TIME_MAN.begin_of_year_localtime( from, round ? 1 : 0 );
	else if( tSpec == TSPEC_MONTH )
	   t = TIME_MAN.begin_of_month_localtime( from, round ? 1 : 0 );
	else if( tSpec == TSPEC_DAY )
	   t = TIME_MAN.begin_of_day_localtime( from, round ? 1 : 0 );
	else if( tSpec == TSPEC_HOUR )
      t = from / TSPAN_HOUR * TSPAN_HOUR + (round ? TSPAN_HOUR : 0);
	else
		t = from;

	tm* T = localtime( &t );
	if( !T )
		return "";

	tm TM = *T;

	string retstr;

	if( tSpec == TSPEC_YEAR )
		retstr = string("") + ( 1900 + TM.tm_year);
	else if( tSpec == TSPEC_MONTH )
		retstr = TIME_MAN.get_time_str( t, TimeManager::TM_MONTH );
	else if( tSpec == TSPEC_DAY )
		retstr = TIME_MAN.get_time_str( t, TimeManager::TM_DAY );
	else if( tSpec == TSPEC_HOUR )
		retstr = get_date_time_str( t );
   else		
		retstr = get_date_time_str( t );


	T = localtime( &to );
	if( !T )
		return "";
	TM = *T;
	
	if( tSpec == TSPEC_YEAR )
	{
		if( TIME_MAN.begin_of_year_localtime( to ) != TIME_MAN.begin_of_year_localtime( t ) )
			retstr += string(" - ") + ( 1900 + TM.tm_year);
	}
	else if( tSpec == TSPEC_MONTH )
	{	
		if( TIME_MAN.begin_of_month_localtime( to ) != TIME_MAN.begin_of_month_localtime( t ) )
			retstr += " - " +  get_time_str( to, TM_MONTH );
	}
	else if( tSpec == TSPEC_DAY )
	{
		if( TIME_MAN.begin_of_day_localtime( to ) != TIME_MAN.begin_of_day_localtime( t ) )
			retstr += " - " + TIME_MAN.get_time_str( to, TimeManager::TM_DAY );
	}
	else if( tSpec == TSPEC_HOUR )
	{
		if( TIME_MAN.begin_of_day_localtime( to ) == TIME_MAN.begin_of_day_localtime( t ) )
			retstr += " - " + fillLeft( string("") + TM.tm_hour, 2, '0' ) + ":00";
		else
			retstr += " - " + get_date_time_str( to / TSPAN_HOUR * TSPAN_HOUR );
	}
	else		
	{
		if( TIME_MAN.begin_of_day_localtime( to ) == TIME_MAN.begin_of_day_localtime( t ) )
		{
			retstr += " - " + 
				 fillLeft( string("") + TM.tm_hour, 2, '0' ) + ":" +
				 fillLeft( string("") + TM.tm_min, 2, '0' );
		}
		else
			retstr += " - " + get_date_time_str( to );
	}

	return retstr;
}



void TimeManager::adjust_times( time_t& from, time_t& to,
										  const string& tSpec, int tLim ) const
{
	if( tSpec == TSPEC_YEAR )
	{
 		if( tLim == 1 )
		{
			from -= TSPAN_HALF_MONTH;
			to += TSPAN_HALF_MONTH;
		}
		else
		{
			from -= TSPAN_HALF_YEAR;
			to = TIME_MAN.begin_of_year_localtime(to) + TSPAN_HALF_YEAR;
		}
	}
	else if( tSpec == TSPEC_MONTH )
	{
 		if( tLim == 1 )
		{
			from -= TSPAN_HALF_DAY;
			to = TIME_MAN.begin_of_day_localtime(to) + TSPAN_HALF_DAY;
		}
		else
		{
			from -= TSPAN_HALF_MONTH;
			to = TIME_MAN.begin_of_month_localtime(to) + TSPAN_HALF_MONTH;
		}
	}
	else if( tSpec == TSPEC_DAY )
	{
 		if( tLim == 1 )
		{
			from -= TSPAN_HALF_HOUR;
			to -= TSPAN_HALF_HOUR;
		}
		else
		{
			from -= TSPAN_HALF_DAY;
			to = TIME_MAN.begin_of_day_localtime(to) + TSPAN_HALF_DAY;
		}
	}
	else if( tSpec == TSPEC_HOUR )
	{
		from -= TSPAN_HALF_HOUR;
		to += TSPAN_HALF_HOUR;
	}
	else if( tSpec == TSPEC_QUARTER_HOUR ) 
	{
		time_t t = from / TSPAN_QUARTER_HOUR * TSPAN_QUARTER_HOUR;
		if( t < from )
			t += TSPAN_QUARTER_HOUR;

		t -= TSPAN_HALF_QUARTER_HOUR;
		from = min( from, t );

		t = to / TSPAN_QUARTER_HOUR * TSPAN_QUARTER_HOUR;

		t += TSPAN_HALF_QUARTER_HOUR;
		to = max( to, t );
	}
	else if( tSpec == TSPEC_MINUTE )
	{
		time_t t = from / TSPAN_MINUTE * TSPAN_MINUTE;
		if( t < from )
			t += TSPAN_MINUTE;

		t -= TSPAN_HALF_MINUTE;
		from = t;

		t = to / TSPAN_MINUTE * TSPAN_MINUTE;

		t += TSPAN_HALF_MINUTE;
		to = t;
	}
	else if( tSpec == TSPEC_5MINUTE )
	{
		time_t t = from / TSPAN_5MINUTE * TSPAN_5MINUTE;
		if( t < from )
			t += TSPAN_5MINUTE;

		t -= TSPAN_HALF_5MINUTE;
		from = t;

		t = to / TSPAN_5MINUTE * TSPAN_5MINUTE;

		t += TSPAN_HALF_5MINUTE;
		to = t;
	}
};

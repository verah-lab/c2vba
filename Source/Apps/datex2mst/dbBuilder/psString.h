#ifndef _PSSTRING_H_
#define _PSSTRING_H_

#include <string>
using namespace std;

#include "time.h"


using namespace std;

string trim(const string& str);
string stripstr(const string& str);
string get_word_from_wlist(const string& str,char sep,unsigned int num);
string num_to_str(double d);
string operator+(const string& str,double d);
string operator+(const string& str,long d);
string operator+(const string& str,short d);
string operator+(const string& str,int d);

string operator+(const string& str,unsigned long d);
string operator+(const string& str,unsigned short d);
string operator+(const string& str,unsigned int d);


string operator+(double d,const string& str);
string operator+(unsigned long d,const string& str);
string operator+(unsigned short d,const string& str);
string operator+(unsigned int d,const string& str);

string operator+(long d,const string& str);
string operator+(short d,const string& str);
string operator+(int d,const string& str);

double ps_atof( const string& str );


time_t locSec_to_UTCSec(time_t T); 
time_t UTCSec_to_locSec(time_t T); 
long make_ascii_hex_to_long(string hexnum_str);
void make_long_to_4ascii_hex ( long num, char* buffer);
char make_long_to_1ascii_hex( short num );
string fillLeft( string str1, unsigned long len, char c );
string fillRight( string str1, unsigned long len, char c );

string ucase( const string& str1 );
string eraseChar( string s , char c );
time_t begin_of_day_localtime( time_t t1 , int days = 0 );
time_t begin_of_month_localtime( time_t t1 , int months = 0 );
time_t begin_of_year_localtime( time_t t1 , int years = 0 );

string get_date_str( time_t t );
string get_date_time_str( time_t t );
string get_time_str( time_t t );

bool is_number( const string& nstr );



#endif




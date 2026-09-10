#include <string>
#include "psString.h"
#include "psTimeMan.h"

//#if defined(WIN32)
#include <strstream>
#include <iostream>
//#else
//#include <strstream.h>
//#endif

#include <time.h>
#include <stdlib.h>
#include "HBTime.h"

using namespace std;



string trim(const string& str)

// Entfernt fuehrende und endstaendige Leerzeichen und Tabs

{
   string Retstr=str; 

   
   string::size_type blPos;
   if (Retstr.length())
   {
	  string::size_type pos = Retstr.find('\0');

	  // Falls die 1. \0 nicht am Ende des Strings (Laenge)
	  // steht, werden alle \0 bis zu Retstr.length() durch Blanks
	  // ersetzt (MK)
	  if (pos < Retstr.length())
	  {
        for (int i = pos; i < Retstr.length(); i++)
		{
          Retstr.at(i) = ' ';
		}
      }
      do
	  {

        if ( (Retstr.at(0)==' ') || (Retstr.at(0)=='\t') )
        {
		  blPos=0;
		  Retstr=Retstr.substr(1,Retstr.length()-1);
		}
	    else if ( (Retstr.at(Retstr.length()-1)==' ') || 
                  (Retstr.at(Retstr.length()-1)=='\t') ) 
		{
	      blPos=Retstr.length()-1;
		  Retstr=Retstr.substr(0,Retstr.length()-1);
		}
        else
		  blPos=string::npos;
      } while ( (blPos!=string::npos) && Retstr.length() );
   }  
   return Retstr; 
};


string stripstr(const string& str)

// Entfernt einleitendes und endstaendiges '"'

{
   string Retstr=str;

   if (Retstr=="")
     return "";
   if (Retstr.at(0)=='"')
     Retstr=Retstr.substr(1,Retstr.length()-1);
   if (Retstr.at(Retstr.length()-1)=='"') 
	 Retstr=Retstr.substr(0,Retstr.length()-1);

   return trim(Retstr);
};



string get_word_from_wlist(const string& str,char sep,unsigned int num)

// Gibt aus einer (mit Trennzeichen sep) verketteten Liste von strings den 
// num.ten zurueck (leere Zeichenkette, fuer den Fall, dass es weniger als num 
// strings gibt.)  

{
  unsigned int i;
  unsigned int sep_Pos;

  unsigned int j;
  const char* cpt;
  
  cpt=str.c_str();
  i=0;j=0;
  while ((j<num-1) && (i<str.length()))
  {
    if ( cpt[i]==sep )
      j++;
    i++;
  };
  if (j==num-1)
  {
    sep_Pos=i;
    while ( (cpt[i]!=sep) && (i<str.length()) )
      i++;
    return trim(str.substr(sep_Pos,i-sep_Pos));
  }
  else
    return "";
};






string num_to_str(double d)

// double d in string umsetzen

{
   strstream strm;
   string str;

   strm << d;
   strm >> str;

   return str;

};


string operator+(const string& str,double d)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return str + hstr; 
}


string operator+(const string& str,long d)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return str + hstr; 
}


string operator+(const string& str,short d)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return str + hstr; 
}


string operator+(const string& str,int d)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return str + hstr; 
}




string operator+(double d,const string& str)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return hstr + str; 
};


string operator+(long d,const string& str)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return hstr + str; 
};

string operator+(short d,const string& str)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return hstr + str; 
};


string operator+(int d,const string& str)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return hstr + str; 
};





// 'unsigned' versions of operator +


string operator+(const string& str,unsigned long d)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return str + hstr; 
}


string operator+(const string& str,unsigned short d)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return str + hstr; 
}


string operator+(const string& str,unsigned int d)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return str + hstr; 
}



string operator+(unsigned long d,const string& str)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return hstr + str; 
};

string operator+(unsigned short d,const string& str)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return hstr + str; 
};


string operator+(unsigned int d,const string& str)

{
   string hstr;
   strstream strm;

   strm << d;
   strm >> hstr;

   return hstr + str; 
};


double ps_atof( const string& str )

{
   strstream strm;
	double d;

   strm << str;
   strm >> d;

	return d;
}


long make_ascii_hex_to_long(string hexnum_str)

{
  long retlong;

  string hstr = "0X" + hexnum_str; 
  sscanf(hstr.c_str(),"%li",&retlong);
  return retlong;
};


char make_long_to_1ascii_hex( short num )

{
  char hbuf[10];

  sprintf(hbuf,"%hX",num);
  return *hbuf;
};


string fillLeft( string str1, unsigned long len, char c )

{
   if ( str1.length() >= len ) 
     return str1.substr(0,len);
   else
     return string(len-str1.length(),c) + str1;
}


string fillRight( string str1, unsigned long len, char c )

{
   if ( str1.length() >= len ) 
     return str1.substr(0,len);
   else
     return  str1 + string(len-str1.length(),c);
}



void make_long_to_4ascii_hex ( long num, char* buffer)

{
  char hbuf[20];

  sprintf(hbuf,"%lX",num);
  string hstr = hbuf;
  hstr = fillLeft( hstr, 4, '0');

  memcpy( buffer, hstr.c_str(), 4);
};



string ucase( const string& str1 )

{
  char* hstr = new char[str1.length() + 1];

  const char* pts = str1.c_str();
  char* ptd = &(hstr[0]);
  
  while( *pts )
  {
    *ptd = *pts; 
    if ( ( *pts >= 'a' ) && ( *pts <= 'z' ) )
      *ptd = ( 'A' + ( *ptd - 'a' ) );      
    else
      *ptd = *pts; 
    pts++;ptd++;   
  }
  *ptd = '\0';

  string ret_str = hstr;
  delete hstr;

  return ret_str;

}


string eraseChar( string s , char c )

{
   string retstr = s;
   unsigned pos;
   while( ( pos = retstr.find( c ) ) != string::npos )
   {
      retstr = retstr.substr( 0 , pos ) +      
               retstr.substr( pos + 1 , retstr.length() - ( pos + 1 ) );
   }
   return retstr;
}

bool is_number( const string& num )

{
	string nstr = trim( num );

	if( !nstr.size() )
		return false;

	if( nstr[0] == '-' )
		nstr = nstr.substr( 1 , nstr.size() - 1 );

	for( unsigned i = 0 ; i < nstr.size() ; i++ )
		if( ( nstr[i] < '0' ) || ( nstr[i] > '9' ) )
			return false;

	return true;
};



time_t locSec_to_UTCSec(time_t T) 

{
	return TIME_MAN.locSec_to_UTCSec( T );
};


time_t UTCSec_to_locSec(time_t T) 

{
	return TIME_MAN.UTCSec_to_locSec( T );
};


time_t begin_of_day_localtime( time_t t1 , int days )
{
	return TIME_MAN.begin_of_day_localtime( t1, days );
} 

time_t begin_of_month_localtime( time_t t1 , int months )
{
	return TIME_MAN.begin_of_month_localtime( t1, months );
} 


time_t begin_of_year_localtime( time_t t1 , int years )
{
	return TIME_MAN.begin_of_year_localtime( t1, years );
} 


string get_date_str( time_t t )

{
	return TIME_MAN.get_date_str_raw( t );
};


string get_date_time_str( time_t t )

{
	return TIME_MAN.get_date_time_str_raw( t );
};


string get_time_str( time_t t )

{
	return TIME_MAN.get_time_str_raw( t );
};



/****************************************************************************/
/*
   Project      TOSCA

   File:        VBase.h


   Lib/Process  TRAILS out
   Author:      Peter Schmitz                  
   Comment:     

   Date        |   Comment
   ----------------------------------------------------
   23.04.1999  |   initial version
               |      

*/   
/****************************************************************************/
#ifndef _VBASE_H_
#define _VBASE_H_

#include <string> 
#include <iostream>

//#if defined(WIN32)
using namespace std;
//#endif

class VBaseT

{
public:
	virtual ~VBaseT() {};
};


template <class T>
class VBaseSkalarC : public VBaseT 
{
private:
  T value;
public:
  VBaseSkalarC(const VBaseSkalarC<T>& attr);
  VBaseSkalarC(T attr_value);
  VBaseSkalarC<T>&  operator=(const VBaseSkalarC<T>& attr);

  bool operator==(const VBaseSkalarC<T>& attr);

  void setValue(const T& p);
  T getValue(void) const;

  VBaseSkalarC<T> operator+(const VBaseSkalarC<T>& attr);

};

template <class T>
class VBaseSimpleC : public VBaseT 
{
private:
  T value;
public:
  VBaseSimpleC(const VBaseSimpleC<T>& attr);
  VBaseSimpleC(T attr_value);
  VBaseSimpleC<T>&  operator=(const VBaseSimpleC<T>& attr);

  bool operator==(const VBaseSimpleC<T>& attr);

  void setValue(const T& p);
  T getValue(void) const;
};


template <class T>
class VBaseSkalarCalcC : public VBaseSkalarC<T>
{
public:
  VBaseSkalarCalcC(const VBaseSkalarCalcC<T>& attr);
  VBaseSkalarCalcC(const VBaseSkalarC<T>& attr);
  VBaseSkalarCalcC(T attr_value);

  friend VBaseSkalarCalcC<T>
         operator* (const VBaseSkalarCalcC& attr,long lval)
  {
	  return  VBaseSkalarCalcC<T>( attr.getValue() * lval );
  }
		 
  friend VBaseSkalarCalcC<T> 
         operator* (long lval,const VBaseSkalarCalcC<T>& attr)
  {
	  return  VBaseSkalarCalcC<T>( lval * attr.getValue() );
  };
};



typedef VBaseSkalarCalcC<short> VshortBaseT;
typedef VBaseSkalarCalcC<long> VlongBaseT;
typedef VBaseSkalarCalcC<double> VdoubleBaseT;
typedef VBaseSkalarC<string> VstringBaseT;
typedef VBaseSimpleC<bool> VboolBaseT;
typedef VBaseSimpleC<char> VcharBaseT;

#endif








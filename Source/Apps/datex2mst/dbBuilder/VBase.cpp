/****************************************************************************/
/*
   Project      TOSCA

   File:        VBase.cpp


   Lib/Process  TRAILS out
   Author:      Peter Schmitz                  
   Comment:     

   Date        |   Comment
   ----------------------------------------------------
   25.04.1999  |   initial version
               |      

*/   
/****************************************************************************/
#ifdef WIN32
#pragma warning (disable : 4786)
#endif



#include "VBase.h" 




// template for simple types

template<class T>
VBaseSimpleC<T>::VBaseSimpleC(const VBaseSimpleC<T>& attr)
{
   value = attr.value;
};

template<class T>
VBaseSimpleC<T>::VBaseSimpleC(T attr_value)
{
   value = attr_value;
};

template<class T>
VBaseSimpleC<T>&  
VBaseSimpleC<T>::operator=(const VBaseSimpleC<T>& attr)
{
   value = attr.value;
   return *this;
};

template<class T>
bool VBaseSimpleC<T>::operator==(const VBaseSimpleC<T>& attr)
{
  return ( value == attr.value );   
};

template<class T>
void VBaseSimpleC<T>::setValue(const T& p) 
{
  value = p;
};

template<class T>
T VBaseSimpleC<T>::getValue(void) const
{
  return value;
};




// template for skalar types

template<class T>
VBaseSkalarC<T>::VBaseSkalarC(const VBaseSkalarC<T>& attr)
{
   value = attr.value;
};

template<class T>
VBaseSkalarC<T>::VBaseSkalarC(T attr_value)
{
   value = attr_value;
};

template<class T>
VBaseSkalarC<T>&  
VBaseSkalarC<T>::operator=(const VBaseSkalarC<T>& attr)
{
   value = attr.value;
   return *this;
};

template<class T>
bool VBaseSkalarC<T>::operator==(const VBaseSkalarC<T>& attr)
{
  return ( value == attr.value );   
};

template<class T>
void VBaseSkalarC<T>::setValue(const T& p) 
{
  value = p;
};

template<class T>
T VBaseSkalarC<T>::getValue(void) const
{
  return value;
};

template<class T>
VBaseSkalarC<T>
VBaseSkalarC<T>::operator+(const VBaseSkalarC<T>& attr)
{
  return VBaseSkalarC<T>(value + attr.value);
};


template<class T>
VBaseSkalarCalcC<T>::VBaseSkalarCalcC(
                             const VBaseSkalarCalcC<T>& attr) : 
    VBaseSkalarC<T>(VBaseSkalarC<T>(attr)) {};

template<class T>
VBaseSkalarCalcC<T>::VBaseSkalarCalcC(
                             const VBaseSkalarC<T>& attr) :
    VBaseSkalarC<T>(VBaseSkalarC<T>(attr)) {};

template<class T>
VBaseSkalarCalcC<T>::VBaseSkalarCalcC(T attr_value) : 
    VBaseSkalarC<T>(attr_value) {};

/*
template<class T>
VBaseSkalarCalcC<T> operator*<T>(const VBaseSkalarCalcC<T>& attr,
                                   long lval)
{
  return  VBaseSkalarCalcC<T>( attr.getValue() * lval );
};

template<class T> 
VBaseSkalarCalcC<T> operator*<T>(long lval,
                                   const VBaseSkalarCalcC<T>& attr)
{
  return  VBaseSkalarCalcC<T>( lval * attr.getValue() );
};
*/

template class VBaseSkalarC<short>;
template class VBaseSkalarC<long>;
template class VBaseSkalarC<double>;


template class VBaseSimpleC<bool>;
template class VBaseSimpleC<char>;

template class VBaseSkalarCalcC<short>;
template class VBaseSkalarCalcC<long>;
template class VBaseSkalarCalcC<double>;
template class VBaseSkalarC<string>;

/*
template 
VBaseSkalarCalcC<long> operator*(const VBaseSkalarCalcC<long>& attr,
                                      long lval);
template
VBaseSkalarCalcC<long> operator*(long lval,
                                      const VBaseSkalarCalcC<long>& attr);

*/








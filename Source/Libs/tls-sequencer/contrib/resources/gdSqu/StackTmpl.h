#ifndef I_STACKTMPL_H
#define I_STACKTMPL_H

#include <stdio.h>

template <class T> class Stack;

template <class T>
class StackElem {
	friend class Stack<T>;
	private:
		T elem;
		StackElem *nxt;
		StackElem () { abort(); }
		StackElem (const StackElem&) { abort(); }
		StackElem & operator = (const StackElem&) { abort(); }
	public: 
		StackElem(const T &t) { elem = t; nxt = 0; }
		~StackElem () {}
		void prepend( StackElem *pred ) const { pred->nxt = (StackElem*) this; }
		StackElem *next() const { return nxt; }
};

template <class T>
class Stack {
	private: 
		StackElem<T> *topPtr;

		Stack (const Stack&) { abort(); }
		Stack & operator = (const Stack&)  { abort(); }

		void checkTop()
		{ if (!topPtr) { fprintf( stderr, "Empty stack\n" ); abort(); } }
	public: 

		void push(const T &pa)
		{
			StackElem<T> *t = new StackElem<T> (pa);
			if (topPtr) topPtr->prepend( t );
			topPtr = t;
		}

		T pop()
		{
			checkTop();
			StackElem<T> *out = (StackElem<T>*) topPtr;
			T res = out->elem;
			topPtr = (StackElem<T>*) topPtr->next();
			delete out;
			return res; }

		T top()
		{ checkTop(); T res = topPtr->elem; return res; }

		bool empty() const
		{ return topPtr ? false : true; }

		Stack () { topPtr = 0; }
		~Stack () { while (!empty()) pop(); }
};

#endif

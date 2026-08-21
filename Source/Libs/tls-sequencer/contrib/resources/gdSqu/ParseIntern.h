#ifndef I_PARSEINTERN_H
#define I_PARSEINTERN_H

#include "ZD.h"
#include <PermAlloc.h>
#include <avlTree.h>

class SymbolTable {
	private: 
		PermanentAllocator *palloc;
		avlTree<ZDString,Value*> table;
	public: 
		SymbolTable() { palloc = new PermanentAllocator (64 * 1024); }
		~SymbolTable()
		{
			//avlTreeIterator<ZDString,Value*> itTbl( table );
			//while (itTbl.next()) {
			//	Value *sym = itTbl.data ();
			//	delete sym;
			//}
			delete palloc;
		}
		char* setStr(const char *s)
		{ char *d = (char*) palloc->permAlloc (strlen (s)+1); strcpy( d, s ); return d; }

		void* getMem(unsigned long sz) { return( palloc->permAlloc( sz ) ); }

		bool insert( ZDString name, Value *sym )
		{ return (table.insert (name, sym) == 0) ? true : false; }

		Value* find( ZDString name )
		{
			Value *res=0;
			if (table.find( name, res ) == 0) return (res);
			return (0);
		}

		//bool symHasType( char *name, SymbolType st);

		avlTree<ZDString,Value*> &getTbl() { return (table); }
};

class Context {
	private: 
		Context *outerContext;
		SymbolTable symTbl;
		Context () { abort(); }
		Context (const Context&) { abort(); }
		Context & operator = (const Context&) { abort(); }
	public: 
		Context( Context *outer ) { outerContext = outer; }
		~Context () {}
		bool insert( ZDString name, Value *sym )
		{
			return symTbl.insert( symTbl.setStr( name ), sym );
		}
		Value* find( ZDString name )
		{
			Context *seekCon = this;
			while (seekCon) {
				Value *res;
				if ((res = seekCon->symTbl.find( name ))) return (res);
				seekCon = seekCon->outerContext;
			}
			return (0);
		}
		avlTree<ZDString,Value*> &getTbl() { return (symTbl.getTbl()); }
};

#endif

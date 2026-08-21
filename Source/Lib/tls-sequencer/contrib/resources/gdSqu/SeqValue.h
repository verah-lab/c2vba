#ifndef I_SEQVALUE_H
#define I_SEQVALUE_H

#include <Value.h>
#include <string.h>
#include <stdlib.h>

typedef ValueCarrier Value;

class ValueLocalArray : public Value {
	friend class ValueLocalArrayIndex;
	private:
		static Property *SpeciesDum;
	public: 
		char *name;
		long *value;
		unsigned long size;
		
		ValueLocalArray (unsigned long sz)
		{
			value = new long[sz];
			size = sz;
			memset( value, 0, sz * sizeof( long ) );
		}
		virtual ~ValueLocalArray ()
		{
			if (name) free (name);
			delete[] value;
		}
		
		long GetVal (unsigned char *base);
		long PutVal (unsigned char *base, long val);
		unsigned long Species() const;
		Property GetProp () const;
		int operator == (const ValueCarrier& other);
		void dump ();
		unsigned char *GetAdr(unsigned char *) { return ((unsigned char *) value); }
};

class ValueLocalArrayIndex : public Value {
	private:
		static Property *SpeciesDum;
	public: 
		Value *indexExpression;
		ValueLocalArray *base;
		
		ValueLocalArrayIndex (ValueLocalArray *p_base, Value *indx)
		{
			base = p_base;
			indexExpression = indx;
		}
		virtual ~ValueLocalArrayIndex ()
		{
			delete indexExpression;
		}
		
		long GetVal (unsigned char *base);
		long PutVal (unsigned char *base, long val);
		unsigned long Species() const;
		Property GetProp () const;
		int operator == (const ValueCarrier& other);
		void dump ();
		unsigned char *GetAdr(unsigned char *);
};

class ValueRdsLoc : public BinExpr {
	private:
		static Property *SpeciesDum;
	public:
		BinOpFunc BinOp ();
		unsigned long Species() const;
		Property GetProp () const;
		void dump ();
};

class ValueAssign : public BinExpr {
	private:
		static Property *SpeciesDum;
	public:
		long GetVal (unsigned char *base);
		bool atomic:1;
		bool elemByElem:1;

		BinOpFunc BinOp ();
		unsigned long Species() const;
		Property GetProp () const;
		void dump ();
};

class ValueExists : public UnExpr { // in fact bool
	private:
		static Property *SpeciesDum;
	public:
		long GetVal (unsigned char *base);
		
		UnOpFunc UnOp ();
		unsigned long Species() const;
		Property GetProp () const;
		void dump ();
};

class CommonInfo;

class ValueSeqCommon : public Value {
	private:
		static Property *SpeciesDum;
	public: 
		Value *realValue;
		CommonInfo *info;
		bool atomic:1;

		ValueSeqCommon()
		{ realValue = 0; info = 0; atomic = true; }
		~ValueSeqCommon ();
		//{ if (realValue) delete realValue; if (info) delete info; }

		long GetVal (unsigned char *base);
		long PutVal (unsigned char *base, long val);
		Bool APutVal (unsigned char *base, unsigned char *data, unsigned size);
		unsigned char *GetAdr (unsigned char *base);
		Property GetProp () const;
		unsigned long Species() const;
		int operator == (const ValueCarrier& other);
		void dump ();
};

class InternValue : public ValueCanGetAdr {
	private:
		static Property *SpeciesDum;
	public:
		long *VarPtr;
		char *name;
		long ResetVal;

		virtual ~InternValue () {}
		Property GetProp () const;
		unsigned long Species() const;
		int operator == (const ValueCarrier& other);
		long GetVal (unsigned char *base);
		unsigned char *GetAdr (unsigned char *) { return ((unsigned char*) VarPtr); }
		virtual void Reset ();
		virtual void dump ();
};

class InternValueRO : public InternValue {
	private:
		static Property *SpeciesDum;
		static InternValueRO *backup;
	public:

		InternValueRO(long *vp, char *nm, long rstv)
			{ VarPtr = vp; name = nm; ResetVal = rstv; }
		void operator delete (void *m);
		~InternValueRO ();
		long PutVal (unsigned char *base, long val);
		Property GetProp () const;
		unsigned long Species() const;
};

class InternValueRW : public InternValue {
	private:
		static Property *SpeciesDum;
		static InternValueRW *backup;
	public:
		char sig_for_check;

		void operator delete (void*m);
		~InternValueRW ();
		long PutVal (unsigned char *base, long val);
		Property GetProp () const;
		unsigned long Species() const;
};

class ValueSeqInternal : public InternValueRO {
	public: 
		ValueSeqInternal(long *vp, char *nm, long rstv)
			: InternValueRO(vp, nm, rstv) {}
};

#endif

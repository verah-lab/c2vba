#ifndef I_SEQOP_H
#define I_SEQOP_H

#include "SeqValue.h"
#include <SEArrayT.h>
#include <TransfBase.h>

struct CaseVal2Inst {
	long constVal;
	unsigned long instructionIndex;
	int operator == (const CaseVal2Inst&) const { return (0); }
};

class TransOpBlock : public TransBase {
	friend class TransOpSwitch;
	private: 
		static Property *SpeciesDum;
	public:
		Bool OpDo (DEData2&, DEData2&, unsigned long&);
		Property GetProp() const;
		unsigned long Species () const;
};

class TransOpBreakableBlock : public TransOpBlock {
	//friend class TransOpSwitch;
	private: 
		static Property *SpeciesDum;
	protected: 
		static TransOpBreakableBlock *broken;
	public:
		Bool OpDo (DEData2&, DEData2&, unsigned long&);
		void unBroken() { broken = 0; }
		void doBreak()  { broken = this; }
		TransOpBreakableBlock *isBroken() { return broken; }
		Property GetProp() const;
		unsigned long Species () const;
		//NON_ABSTRACT_MUSTHAVE;
};

class TransOpCLoop : public TransOpBreakableBlock {
	private: 
		static Property *SpeciesDum;
	protected: 
		static TransOpCLoop *continued;
	public: 
		Value *checkExpression;
		virtual bool preCheckExpression() = 0;
		void unContinued() { continued = 0; }
		void doContinue()  { continued = this; }
		TransOpBreakableBlock *isContinued() { return continued; }
		Property GetProp() const;
		unsigned long Species () const;

		Bool OpDo (DEData2&, DEData2&, unsigned long&);
};

class TransOpCLoopWhile : public TransOpCLoop {
	private: 
		static Property *SpeciesDum;
	public: 
		bool preCheckExpression() { return (true); }
		Property GetProp() const;
		unsigned long Species () const;
		void dump (char *);\
};

class TransOpCLoopDoWhile : public TransOpCLoop {
	private: 
		static Property *SpeciesDum;
	public: 
		bool preCheckExpression() { return (false); }
		Property GetProp() const;
		unsigned long Species () const;
		void dump (char *);
};

class TransOpSwitch : public TransOpBreakableBlock {
	private:
		static Property *SpeciesDum;
		SEArrayS<CaseVal2Inst> jmpTable;
		CaseVal2Inst defaultIndex;
		Value *checkExpression;

	public:
		TransOpSwitch();
		~TransOpSwitch ();
		bool addCase(long p_caseVal, TransOpBlock *ins);
		void addDefault(TransOpBlock *ins);
		void setExpr( Value *expr);
		NON_ABSTRACT_MUSTHAVE;
		void Compact ();
		void addOps( TransOpBlock *ins );
};

class TransOpBreak : public TransOp {
	private:
		static Property *SpeciesDum;
	public: 
		TransOpBreakableBlock *breakBlock;
		TransOpBreak() { breakBlock = 0; }
		NON_ABSTRACT_MUSTHAVE;
};

class TransOpContinue : public TransOp {
	private:
		static Property *SpeciesDum;
	public: 
		TransOpCLoop *continueBlock;
		TransOpContinue() { continueBlock = 0; }
		NON_ABSTRACT_MUSTHAVE;
};

class TransOpDoAssign : public TransOp {
	private:
		static Property *SpeciesDum;
	public: 
		ValueAssign *value;
		TransOpDoAssign() { value = 0; }
		~TransOpDoAssign() { if (value) delete value; }
		NON_ABSTRACT_MUSTHAVE;
};

class TransOpWrite : public TransOp {
	private:
		static Property *SpeciesDum;
	public: 
		NON_ABSTRACT_MUSTHAVE;
};

class TransOpNop : public TransOp {
	private:
		static Property *SpeciesDum;
	public: 
		NON_ABSTRACT_MUSTHAVE;
};

class ValueList;

class TransOpWithValueList : public TransOp {
	//private:
	//	static Property *SpeciesDum;
	public: 
		char *fmtString;
		ValueList *valueList;
		TransOpWithValueList() { fmtString = 0; }
		~TransOpWithValueList() { if (fmtString) free( fmtString ); }
		//NON_ABSTRACT_MUSTHAVE;
};

class TransOpErrMsg : public TransOpWithValueList {
	private:
		static Property *SpeciesDum;
	public: 
		NON_ABSTRACT_MUSTHAVE;
};

class TransOpSysCmd : public TransOpWithValueList {
	private:
		static Property *SpeciesDum;
	public: 
		NON_ABSTRACT_MUSTHAVE;
};

class FlopContainer;

class TransOpMonoflop : public TransOp {
	private:
		static Property *SpeciesDum;
		bool trigger( long key, unsigned char *basedata );
	protected: 
		FlopContainer *realFlops;
	public: 
		long entryDa;
		Value *timeout;
		TransOp *action;
		unsigned char mfType;
		bool exclusive:1;
		NON_ABSTRACT_MUSTHAVE;
		TransOpMonoflop();
};

class TransOpRtrMonoflop : public TransOpMonoflop {
	private:
		static Property *SpeciesDum;
		bool reTrigger( long key, unsigned char *basedata );
	public: 
		Value *maxTimeout;

		TransOpRtrMonoflop() { maxTimeout = 0; }
		Property GetProp() const;
		unsigned long Species () const;
		void dump (char *);
		Bool OpDo (DEData2&, DEData2&, unsigned long&);
};

#define MF_T_EA		1
#define MF_T_ONCE		2
#define MF_T_NODE		3
#define MF_T_EA2CL	4

#endif

#ifndef I_CURENV_H
#define I_CURENV_H

class CurrentEnv {
	public: 
		long currentEaid;
		long currentDa;
		long currentDe;
		long currentNode;
		long currentTime;
		long currentASTime;
		long currentEaIndx;
		short currentBlock;				// ???
		unsigned char *currentData;	// ???
		long letzterAusloeseZeitpunkt;
		long ersterAusloeseZeitpunt;
};

#endif

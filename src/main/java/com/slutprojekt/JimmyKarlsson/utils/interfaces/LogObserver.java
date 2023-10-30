package com.slutprojekt.JimmyKarlsson.utils.interfaces;

import com.slutprojekt.JimmyKarlsson.model.WorkerLogDTO;

public interface LogObserver {
	void updateLog(WorkerLogDTO logData);
}

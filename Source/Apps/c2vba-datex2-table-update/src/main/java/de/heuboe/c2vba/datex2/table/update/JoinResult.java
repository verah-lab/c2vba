package de.heuboe.c2vba.datex2.table.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinResult<T> {

	private String tableId;
	private String tableName;
	private String tableVersion;
	
	T table;
}

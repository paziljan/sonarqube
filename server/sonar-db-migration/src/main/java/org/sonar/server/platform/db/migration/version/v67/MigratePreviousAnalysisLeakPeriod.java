/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.platform.db.migration.version.v67;

import java.sql.SQLException;
import org.sonar.db.Database;
import org.sonar.db.dialect.Oracle;
import org.sonar.server.platform.db.migration.step.DataChange;
import org.sonar.server.platform.db.migration.step.MassUpdate;

public class MigratePreviousAnalysisLeakPeriod extends DataChange {

  private static final String ORACLE_SQL = "select id from properties " +
    " where prop_key = 'sonar.leak.period' and (text_value='previous_analysis' or dbms_lob.compare(clob_value, to_clob('previous_analysis')) = 0)";
  private static final String SQL = "select id from properties " +
    " where prop_key = 'sonar.leak.period' and (text_value='previous_analysis' or clob_value='previous_analysis')";

  private final boolean isOracle;

  public MigratePreviousAnalysisLeakPeriod(Database db) {
    super(db);
    isOracle = db.getDialect().getId().equals(Oracle.ID);
  }

  @Override
  protected void execute(Context context) throws SQLException {
    MassUpdate massUpdate = context.prepareMassUpdate();
    massUpdate.select(isOracle ? ORACLE_SQL : SQL);
    massUpdate.update("update properties " +
      " set text_value='previous_version', " +
      " clob_value = null " +
      " where id = ?");
    massUpdate.rowPluralName("leak periods");
    massUpdate.execute((row, update) -> {
      update.setLong(1, row.getLong(1));
      return true;
    });
  }
}

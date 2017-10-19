package org.sonar.server.platform.db.migration.version.v67;/*
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

import com.google.common.collect.ImmutableList;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.db.CoreDbTester;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

public class MigratePreviousAnalysisLeakPeriodTest {

  private final static String SELECT_PROPERTIES = "SELECT prop_key, is_empty, text_value, clob_value FROM properties";

  @Rule
  public CoreDbTester db = CoreDbTester.createForSchema(MigratePreviousAnalysisLeakPeriodTest.class, "properties.sql");

  private MigratePreviousAnalysisLeakPeriod underTest = new MigratePreviousAnalysisLeakPeriod(db.database());

  @Test
  public void migration_must_update_the_database() throws SQLException {
    List<Property> properties = generateAndInsertProperties();

    underTest.execute();

    assertDatabaseContainsExactly(applyChanges(properties));
  }

  @Test
  public void migration_must_be_reentrant() throws SQLException {
    List<Property> properties = generateAndInsertProperties();

    underTest.execute();
    underTest.execute();

    assertDatabaseContainsExactly(applyChanges(properties));
  }

  @Test
  public void migration_is_doing_nothing_when_no_data() throws SQLException {
    assertThat(db.countRowsOfTable("properties")).isEqualTo(0);
    underTest.execute();
    assertThat(db.countRowsOfTable("properties")).isEqualTo(0);
  }

  @Test
  public void update_must_look_in_clob() throws SQLException {
    Property property = new Property("sonar.leak.period", null,"previous_analysis", false);
    property.insert();

    underTest.execute();

    assertThat(db.select(SELECT_PROPERTIES)).isEqualTo(
      ImmutableList.of(new Property("sonar.leak.period", "previous_version",null, false).toMap())
    );
  }

  @Test
  public void update_must_look_in_text_value() throws SQLException {
    Property property = new Property("sonar.leak.period", "previous_analysis", null,false);
    property.insert();

    underTest.execute();

    assertThat(db.select(SELECT_PROPERTIES)).isEqualTo(
      ImmutableList.of(new Property("sonar.leak.period", "previous_version",null, false).toMap())
    );
  }

  private void assertDatabaseContainsExactly(List<Property> expectedProperties) {
    assertThat(db.select(SELECT_PROPERTIES)).isEqualTo(
      expectedProperties.stream().map(Property::toMap).collect(Collectors.toList())
    );
  }

  private List<Property> generateAndInsertProperties() {
    List<Property> properties = ImmutableList.of(
      new Property("sonar.leak.period", randomAlphanumeric(1000), null, false),
      new Property("sonar.leak.period", "previous_version", null, false),
      new Property("sonar.leak.period", null, "previous_analysis", false),
      new Property("sonar.leak.period", "previous_analysis", null, false),
      new Property(randomAlphanumeric(50), randomAlphanumeric(1000), null, false),
      new Property(randomAlphanumeric(50), null, randomAlphanumeric(10000), false)
    );

    properties.forEach(Property::insert);

    return properties;
  }

  private List<Property> applyChanges(List<Property> properties) {
    return properties.stream().map(
      p -> {
        Property result = p.clone();

        // If key is not "sonar.leak.period" => no change
        if (!"sonar.leak.period".equals(result.propKey)) {
          return result;
        }

        if ("previous_analysis".equals(result.textValue) || "previous_analysis".equals(result.clobValue)) {
          result.clobValue = null;
          result.textValue = "previous_version";
        }

        return result;
      }).collect(Collectors.toList());
  }

  private class Property {
    private String propKey;
    private boolean isEmpty;
    private String textValue;
    private String clobValue;

    private Property(String propKey, @Nullable String textValue, @Nullable String clobValue, boolean isEmpty) {
      this.propKey = propKey;
      this.isEmpty = isEmpty;
      this.textValue = textValue;
      this.clobValue = clobValue;
    }

    private void insert() {
      db.executeInsert("PROPERTIES", toMap());
    }

    @Override
    public Property clone() {
      return new Property(this.propKey, this.textValue, this.clobValue, this.isEmpty);
    }

    private Map<String, Object> toMap() {
      HashMap<String, Object> map = new HashMap<>();
      map.put("PROP_KEY", propKey);
      map.put("TEXT_VALUE", textValue);
      map.put("CLOB_VALUE", clobValue);
      map.put("IS_EMPTY", isEmpty);

      return Collections.unmodifiableMap(map);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Property)) {
        return false;
      }
      Property property = (Property) o;
      return isEmpty == property.isEmpty &&
        Objects.equals(propKey, property.propKey) &&
        Objects.equals(textValue, property.textValue) &&
        Objects.equals(clobValue, property.clobValue);
    }

    @Override
    public int hashCode() {
      return Objects.hash(propKey, isEmpty, textValue, clobValue);
    }
  }
}

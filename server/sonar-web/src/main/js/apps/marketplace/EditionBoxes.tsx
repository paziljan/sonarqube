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
import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import EditionBox from './components/EditionBox';
import LicenseEditionForm from './components/LicenseEditionForm';
import UninstallEditionForm from './components/UninstallEditionForm';
import { Edition, EditionStatus } from '../../api/marketplace';
import { translate } from '../../helpers/l10n';

export interface Props {
  editions?: Edition[];
  editionStatus?: EditionStatus;
  editionsUrl: string;
  loading: boolean;
  sonarqubeVersion: string;
  updateCenterActive: boolean;
  updateEditionStatus: (editionStatus: EditionStatus) => void;
}

interface State {
  installEdition?: Edition;
  openUninstallForm: boolean;
}

export default class EditionBoxes extends React.PureComponent<Props, State> {
  state: State = { openUninstallForm: false };

  handleOpenLicenseForm = (edition: Edition) => this.setState({ installEdition: edition });
  handleCloseLicenseForm = () => this.setState({ installEdition: undefined });

  handleOpenUninstallForm = () => this.setState({ openUninstallForm: true });
  handleCloseUninstallForm = () => this.setState({ openUninstallForm: false });

  render() {
    const { editions, editionStatus, loading } = this.props;
    const { installEdition, openUninstallForm } = this.state;
    if (loading) {
      return <i className="big-spacer-bottom spinner" />;
    }

    if (!editions) {
      return (
        <div className="spacer-bottom marketplace-editions">
          <span className="alert alert-info">
            <FormattedMessage
              defaultMessage={translate('marketplace.editions_unavailable')}
              id="marketplace.editions_unavailable"
              values={{
                url: (
                  <a href="https://www.sonarsource.com" target="_blank">
                    SonarSource.com
                  </a>
                )
              }}
            />
          </span>
        </div>
      );
    }

    return (
      <div className="spacer-bottom marketplace-editions">
        {editions.map(edition => (
          <EditionBox
            edition={edition}
            editionStatus={editionStatus}
            key={edition.key}
            onInstall={this.handleOpenLicenseForm}
            onUninstall={this.handleOpenUninstallForm}
          />
        ))}

        {installEdition && (
          <LicenseEditionForm
            edition={installEdition}
            editions={editions}
            onClose={this.handleCloseLicenseForm}
            updateEditionStatus={this.props.updateEditionStatus}
          />
        )}

        {openUninstallForm &&
        editionStatus &&
        editionStatus.currentEditionKey && (
          <UninstallEditionForm
            edition={editions.find(edition => edition.key === editionStatus.currentEditionKey)}
            editionStatus={editionStatus}
            onClose={this.handleCloseUninstallForm}
            updateEditionStatus={this.props.updateEditionStatus}
          />
        )}
      </div>
    );
  }
}

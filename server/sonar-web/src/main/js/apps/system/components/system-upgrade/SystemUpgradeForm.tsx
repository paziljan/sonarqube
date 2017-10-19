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
import Modal from 'react-modal';
import { SystemUpgrade } from '../../../../api/system';
import { translate } from '../../../../helpers/l10n';

interface Props {
  systemUpgrades: SystemUpgrade[];
  onClose: () => void;
}

interface State {
  upgrading: boolean;
}

export default class SystemUpgradeForm extends React.PureComponent<Props, State> {
  state: State = { upgrading: false };

  handleCancelClick = (event: React.SyntheticEvent<HTMLElement>) => {
    event.preventDefault();
    this.props.onClose();
  };
  // system.version_is_availble
  // system.release_notes
  // system.released_x
  // system.show_intermediat_versions
  // system.hide_intermediat_versions
  // system.download_x
  // system.how_to_upgrade
  // system.latest_version
  // system.latest_lts_version
  // https://redirect.sonarsource.com/doc/upgrading.html
  render() {
    const { upgrading } = this.state;
    const header = translate('system.system_upgrade');
    return (
      <Modal
        isOpen={true}
        contentLabel={header}
        className="modal"
        overlayClassName="modal-overlay"
        onRequestClose={this.props.onClose}>
        <div className="modal-head">
          <h2>{header}</h2>
        </div>
        <div className="modal-body" />
        <div className="modal-foot">
          {upgrading && <i className="spinner spacer-right" />}
          <a className="pull-left" href="https://www.sonarqube.org/downloads/" target="_blank">
            {translate('system.see_sonarqube_downloads')}
          </a>
          <a href="#" onClick={this.handleCancelClick}>
            {translate('cancel')}
          </a>
        </div>
      </Modal>
    );
  }
}

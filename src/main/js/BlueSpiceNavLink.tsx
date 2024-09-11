/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Link, Repository } from "@scm-manager/ui-types";
import { ExternalNavLink } from "@scm-manager/ui-components";

type Props = {
  repository: Repository;
};

const BlueSpiceNavLink: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("plugins");

  const blueSpiceLink = (repository._links["blueSpice"] as Link | undefined)?.href ?? "";

  return <ExternalNavLink to={blueSpiceLink} icon="fas fa-book-reader" label={t("scm-bluespice-plugin.navLink")} />;
};

export default BlueSpiceNavLink;

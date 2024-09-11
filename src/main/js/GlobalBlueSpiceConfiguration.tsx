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
import { ConfigurationForm, Title, Form } from "@scm-manager/ui-core";
import { HalRepresentation } from "@scm-manager/ui-types";
import { validation } from "@scm-manager/ui-components";

type GlobalBlueSpiceConfigurationDto = HalRepresentation & {
  baseUrl: string;
};

const GlobalBlueSpiceConfiguration: FC<{ link: string }> = ({ link }) => {
  const [t] = useTranslation("plugins");

  const isValidBaseUrl = (baseUrl: string) => {
    return baseUrl === "" || validation.isUrlValid(baseUrl);
  };

  return (
    <ConfigurationForm<GlobalBlueSpiceConfigurationDto>
      link={link}
      translationPath={["plugins", "scm-bluespice-plugin.config"]}
    >
      <Title>{t("scm-bluespice-plugin.config.title")}</Title>
      <p className="mb-2">{t("scm-bluespice-plugin.config.globalDescription")}</p>
      <Form.Row>
        <Form.Input name="baseUrl" rules={{ validate: isValidBaseUrl }} />
      </Form.Row>
    </ConfigurationForm>
  );
};

export default GlobalBlueSpiceConfiguration;
